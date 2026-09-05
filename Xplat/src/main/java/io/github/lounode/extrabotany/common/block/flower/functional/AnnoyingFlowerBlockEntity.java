package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class AnnoyingFlowerBlockEntity extends ExtraFunctionalFlowerBlockEntity {

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
		if (getLevel().isClientSide()) {
			return;
		}
		if (ticksExisted % 20 == 0) {
			sync();
		}
		tryEatChicken();
		if (getCooldown() > 0) {
			setCooldown(getCooldown() - 1);
			return;
		}
		if (getMaxMana() - getFishingCost() < 0) {
			return;
		}
		if (!hasWater()) {
			return;
		}

		boolean boosted = getBoostLeft() > 0;
		if (boosted) {
			setBoostLeft(getBoostLeft() - 1);
		}

		RandomSource rand = getLevel().getRandom();
		ItemStack reward = getFishingLoot(boosted);
		int bound = RANGE * 2 + 1;

		ItemEntity entity = new ItemEntity(getLevel(),
				getEffectivePos().getX() - RANGE + rand.nextInt(bound),
				getEffectivePos().getY() + 2,
				getEffectivePos().getZ() - RANGE + rand.nextInt(bound),
				reward
		);

		addMana(-getFishingCost());
		int cooldown = getCooldownAfterWork();
		if (boosted) {
			cooldown = (int) (cooldown * getFoodBoostCooldownMultiplier());
		}
		setCooldown(cooldown);
		getLevel().addFreshEntity(entity);
		getLevel().playSound(null, getEffectivePos(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 1F, 1F);
		sync();

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

	public void tryEatChicken() {
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
					return;
				}
			}
		}
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

	public static class WandHud extends BindableFlowerWandHud<AnnoyingFlowerBlockEntity> {
		public WandHud(AnnoyingFlowerBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphics gui, Window window, Font font, float partialTick) {

			Component component = Component.translatable("message.extrabotany.gui.food_level", flower.getBoostLeft());
			int halfWidth = (font.width(component) + 24) / 2;
			int centerX = window.getGuiScaledWidth() / 2;
			int centerY = window.getGuiScaledHeight() / 2;

			super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);
			gui.drawString(font, component, centerX - halfWidth + 12, centerY + 34, 0xFFFFFF);
		}
	}
}
