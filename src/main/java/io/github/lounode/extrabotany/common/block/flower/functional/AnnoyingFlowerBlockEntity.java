package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.internal_caps.ItemLifetime;
import vazkii.botania.common.helper.EntityHelper;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.api.block.Pedestal;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 神烦花。它消耗魔力来钓鱼，钓到的物品掉在花的周围。范围里的祭坛上必须放着水桶。
 * Annoyingflower. It uses mana to fish, and the caught items drop around the flower. A pedestal in the range must hold a water bucket.
 *
 * <p>把带有 {@code annoying_flower_eatable} 标签的食物丢给它之后，后面的几次钓鱼使用宝藏战利品表，冷却也更短。红石信号让它停止工作。
 * <p>After an item with the tag {@code annoying_flower_eatable} is thrown to it, the subsequent catches use the treasure loot table and have a shorter cooldown. A redstone signal stops it.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 的 {@code SubTileAnnoyingFlower}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileAnnoyingFlower} of ExtraMeteorP.
 */
public class AnnoyingFlowerBlockEntity extends ExtraFunctionalFlowerBlockEntity {
	private long nextWaterScan;
	private long nextFoodScan;


	private static final String TAG_COOLDOWN = "cooldown";
	private static final String TAG_FOOD = "food";

	private static final int RANGE = 3;

	public static final int MAX_MANA = 1000;
	public static final int FISHING_COST = 300;
	public static final int COOLDOWN_AFTER_WORK = 20 * 45;
	public static final int FOOD_BOOST_TIMES = 3;
	public static final double FOOD_BOOST_COOLDOWN_MULTIPLIER = 0.4D;
	public static final int FOOD_BOOST_MAX = 12;

	@Nullable
	private BlockPos cachedPedestalPos;
	private int cooldown;
	private int boostLeft;

	public AnnoyingFlowerBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.ANNOYINGFLOWER, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}
		// 计时器每 tick 都变化，但是客户端不使用它。所以这里只标记区块需要保存，不发同步包。
		// The timers change on each tick, but the client does not use them. Thus this only marks the chunk for saving, and sends no packet.
		if (ticksExisted % 20 == 0) {
			markForPersisting();
		}
		// Item AABB scan for food ran every tick; after an empty scan sleep for a few ticks.
		if (getLevel().getGameTime() >= nextFoodScan && !tryEatChicken()) {
			nextFoodScan = getLevel().getGameTime() + IDLE_BACKOFF_TICKS;
		}
		if (getCooldown() > 0) {
			setCooldown(getCooldown() - 1);
			return;
		}
		if (getMana() < getFishingCost()) {
			return;
		}
		if (getLevel().getGameTime() < nextWaterScan) {
			return;
		}
		if (!hasWater()) {
			// Miss = (2R+1)^2*(R+1) getBlockEntity probes; retry once per second instead of every tick.
			nextWaterScan = getLevel().getGameTime() + 20;
			return;
		}

		boolean boosted = getBoostLeft() > 0;
		RandomSource rand = getLevel().getRandom();
		ItemStack reward = getFishingLoot(boosted);
		if (reward.isEmpty()) {
			return;
		}
		int bound = RANGE * 2 + 1;

		ItemEntity entity = new ItemEntity(getLevel(),
				getEffectivePos().getX() - RANGE + rand.nextInt(bound),
				getEffectivePos().getY() + 2,
				getEffectivePos().getZ() - RANGE + rand.nextInt(bound),
				reward
		);

		if (!getLevel().addFreshEntity(entity)) {
			return;
		}
		if (boosted) {
			setBoostLeft(getBoostLeft() - 1);
		}
		addMana(-getFishingCost());
		int cooldown = getCooldownAfterWork();
		if (boosted) {
			cooldown = (int) (cooldown * getFoodBoostCooldownMultiplier());
		}
		setCooldown(cooldown);
		getLevel().playSound(null, getEffectivePos(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 1F, 1F);
		syncThrottled();

		if (boosted) {
			var players = PlayerHelper.getRealPlayersIn(getLevel(), new AABB(getBlockPos()).inflate(16));
			for (var player : players) {
				if (PlayerHelper.hasAdvancement((ServerPlayer) player, prefix("main/" + LibAdvancementNames.MUROMI_SAN))) {
					continue;
				}
				PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.MUROMI_SAN), "code_triggered");
			}
		}
	}

	/** @return whether food was eaten. */
	public boolean tryEatChicken() {
		for (ItemEntity item : getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(
				Vec3.atLowerCornerOf(getEffectivePos().offset(-RANGE, -RANGE, -RANGE)),
				Vec3.atLowerCornerOf(getEffectivePos().offset(RANGE + 1, RANGE + 1, RANGE + 1))))) {
			if (ItemLifetime.canInteractWith(this, item)) {
				ItemStack stack = item.getItem();

				if (stack.is(ExtraBotanyTags.Items.ANNOYING_FLOWER_EATABLE)) {
					EntityHelper.shrinkItem(item);
					getLevel().playSound(null, getEffectivePos(), SoundEvents.FOX_EAT, SoundSource.BLOCKS, 1F, SoundEventUtil.randomPitch(getLevel()));
					//getLevel().blockEvent(getBlockPos(), getBlockState().getBlock(), START_BURN_EVENT, item.getId());
					//TODO 吃掉的粒子效果
					getLevel().gameEvent(null, GameEvent.BLOCK_ACTIVATE, getBlockPos());
					int boostTime = Math.max(0, getBoostLeft() + getFoodBoostTimes());
					boostTime = Math.min(getFoodBoostMax(), boostTime);
					setBoostLeft(boostTime);
					sync();
					return true;
				}
			}
		}
		return false;
	}

	public ItemStack getFishingLoot(boolean boosted) {
		ServerLevel level = (ServerLevel) getLevel();
		BlockPos pos = getEffectivePos();

		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.TOOL, Items.FISHING_ROD.getDefaultInstance())
				.create(LootContextParamSets.FISHING);
		LootContext.Builder builder = new LootContext.Builder(params);

		ResourceKey<LootTable> lootTable;

		if (boosted) {
			lootTable = BuiltInLootTables.FISHING_TREASURE;
		} else {
			lootTable = BuiltInLootTables.FISHING;
		}

		List<ItemStack> loot = level.getServer()
				.reloadableRegistries()
				.getLootTable(lootTable)
				.getRandomItems(params);

		return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);
	}

	public boolean hasWater() {
		BlockPos pos = getEffectivePos();

		if (cachedPedestalPos != null) {
			BlockEntity tile = getLevel().getBlockEntity(cachedPedestalPos);
			if (tile instanceof Pedestal pedestal && pedestal.getItem().is(Items.WATER_BUCKET)) {
				return true;
			}
			cachedPedestalPos = null;
		}

		for (BlockPos pos_ : BlockPos.betweenClosed(pos.offset(-RANGE, 0, -RANGE),
				pos.offset(RANGE, RANGE, RANGE))) {
			BlockEntity tile = getLevel().getBlockEntity(pos_);
			if (tile instanceof Pedestal pedestal && pedestal.getItem().is(Items.WATER_BUCKET)) {
				cachedPedestalPos = pos_.immutable();
				return true;
			}
		}

		return false;
	}

	public double getFoodBoostCooldownMultiplier() {
		return ExtraBotanyConfig.common().annoyingflowerFoodBoostCooldownMultiplier();
	}

	public int getFoodBoostMax() {
		return ExtraBotanyConfig.common().annoyingflowerFoodBoostMax();
	}

	public int getFoodBoostTimes() {
		return ExtraBotanyConfig.common().annoyingflowerFoodBoostTimes();
	}

	public int getCooldownAfterWork() {
		return ExtraBotanyConfig.common().annoyingflowerCooldown();
	}

	public int getFishingCost() {
		return ExtraBotanyConfig.common().annoyingflowerFishingCost();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().annoyingflowerMaxMana();
	}

	@Override
	public int getColor() {
		return 0x000000;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public int getBoostLeft() {
		return boostLeft;
	}

	public void setBoostLeft(int boostLeft) {
		this.boostLeft = boostLeft;
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_COOLDOWN, getCooldown());
		cmp.putInt(TAG_FOOD, getBoostLeft());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setCooldown(cmp.getInt(TAG_COOLDOWN));
		setBoostLeft(cmp.getInt(TAG_FOOD));
	}
}
