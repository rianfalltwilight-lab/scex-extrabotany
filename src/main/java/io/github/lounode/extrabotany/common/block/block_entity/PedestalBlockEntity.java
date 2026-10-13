package io.github.lounode.extrabotany.common.block.block_entity;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.ExposedSimpleInventoryBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import io.github.lounode.extrabotany.api.block.Pedestal;
import io.github.lounode.extrabotany.api.gaia.BlockPatternExtend;
import io.github.lounode.extrabotany.api.gaia.BlockPatternExtendBuilder;
import io.github.lounode.extrabotany.api.gaia.BlockTagPredicate;
import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.common.block.PedestalBlock;
import io.github.lounode.extrabotany.common.item.KingGardenItem;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 祭坛。它保存一个物品，并且有下面的功能。
 * The pedestal. It holds one item and has the functions that follow.
 *
 * <ul>
 * <li>锤击加工：玩家手持配方指定的锤子右键它。敲击次数达到配方的要求后，物品变成产物。贴在祭坛侧面的物品展示框里的锤子会自动敲击。<br>
 * Hammer processing: the player uses the pedestal with the hammer that the recipe specifies. When the number of hits is that of the recipe, the item becomes the product. A hammer in an item frame on the side of the pedestal hits automatically.
 * <li>给自然能量物品充能：需要自然水晶组成的多方块结构，见 {@link #updateTier}。<br>
 * Charging of nature energy items: this needs a multiblock structure of Natura Pylons, see {@link #updateTier}.
 * <li>配置王之花园：王之花园在祭坛上时，玩家手持花右键来加入这种花。<br>
 * Configuration of the Garden of King's Law: when the garden is on the pedestal, the player uses the pedestal with a flower to add that flower.
 * <li>在白天把梦魇燃料变成精神燃料，需要 6000 tick。<br>
 * In the day it changes Nightmare Fuel into Spirit Fuel in 6000 ticks.
 * </ul>
 *
 * <p>来源：重制版（Lounode）。锤击加工对应 ExtraMeteorP 1.12 的 {@code TilePedestal}。
 * <p>Source: Reburn (Lounode). The hammer processing corresponds to {@code TilePedestal} of ExtraMeteorP 1.12.
 */
public class PedestalBlockEntity extends ExposedSimpleInventoryBlockEntity implements Pedestal {
	public static final BlockPos[] POOL_LOCATIONS = {
			new BlockPos(3, 0, 3), new BlockPos(-3, 0, 3), new BlockPos(3, 0, -3), new BlockPos(-3, 0, -3)
	};

	public static final String[][] TIER1_PATTERN = {
			{

					"P___P",
					"_____",
					"__0__",
					"_____",
					"P___P",
			}
	};

	public static final String[][] TIER2_PATTERN = {
			{
					"P_____P",
					"_______",
					"_______",
					"_______",
					"_______",
					"_______",
					"P_____P",
			},
			{
					"M_____M",
					"_P___P_",
					"_______",
					"___0___",
					"_______",
					"_P___P_",
					"M_____M",
			},
			{
					"_______",
					"_SSSSS_",
					"_S___S_",
					"_S___S_",
					"_S___S_",
					"_SSSSS_",
					"_______",
			},
	};

	public static final Supplier<IMultiblock> TIER_1_PATCHOULI = Suppliers.memoize(() -> {
		var pedestal = PatchouliAPI.get().tagMatcher(ExtraBotanyTags.Blocks.PEDESTALS);
		return PatchouliAPI.get().makeMultiblock(
				TIER1_PATTERN,
				'P', BotaniaBlocks.NATURA_PYLON,
				'0', pedestal
		);
	});

	public static final Supplier<IMultiblock> TIER_2_PATCHOULI = Suppliers.memoize(() -> {
		var pedestal = PatchouliAPI.get().tagMatcher(ExtraBotanyTags.Blocks.PEDESTALS);
		var manaPool = PatchouliAPI.get().tagMatcher(ExtraBotanyTags.Blocks.MANA_POOLS);
		var any = PatchouliAPI.get().anyMatcher();
		return PatchouliAPI.get().makeMultiblock(
				TIER2_PATTERN,
				'P', BotaniaBlocks.NATURA_PYLON,
				'0', pedestal,
				'S', BotaniaBlocks.SHIMMERROCK,
				'M', manaPool
		);
	});

	public static final Predicate<BlockInWorld>[][][] TIER_1_PATTERN = BlockPatternExtendBuilder.start()
			.aisle(TIER1_PATTERN[0])
			.where('_', BlockInWorld.hasState(BlockStatePredicate.ANY))
			.where('0', BlockInWorld.hasState(BlockTagPredicate.forTag(ExtraBotanyTags.Blocks.PEDESTALS)))
			.where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(BotaniaBlocks.NATURA_PYLON)))
			.createPattern();

	public static final Predicate<BlockInWorld>[][][] TIER_2_PATTERN = BlockPatternExtendBuilder.start()
			.aisle(TIER2_PATTERN[0])
			.aisle(TIER2_PATTERN[1])
			.aisle(TIER2_PATTERN[2])
			.where('_', BlockInWorld.hasState(BlockStatePredicate.ANY))
			.where('0', BlockInWorld.hasState(BlockTagPredicate.forTag(ExtraBotanyTags.Blocks.PEDESTALS)))
			.where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(BotaniaBlocks.NATURA_PYLON)))
			.where('S', BlockInWorld.hasState(BlockStatePredicate.forBlock(BotaniaBlocks.SHIMMERROCK)))
			.where('M', BlockInWorld.hasState(BlockTagPredicate.forTag(ExtraBotanyTags.Blocks.MANA_POOLS)))
			.createPattern();

	private static final BlockPatternExtend TIER_1_STRUCTURE = new BlockPatternExtend(TIER_1_PATTERN);
	private static final BlockPatternExtend TIER_2_STRUCTURE = new BlockPatternExtend(TIER_2_PATTERN);

	public final int FINISH_CRAFT_STRIKE_FLAG = -1;
	public int tickCount;
	private int strikes;
	private int tier;
	private int solarTicks;
	private boolean pendingSync;
	private Map<ItemStack, ItemFrame> automaticHammers = new HashMap<>();

	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		this(ExtraBotanyBlockEntities.PEDESTAL, pos, state);
	}

	public PedestalBlockEntity(BlockEntityType<? extends PedestalBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state, true);
		this.setStrikes(0);
	}

	@Override
	protected SimpleContainer createItemHandler() {
		return new SimpleContainer(1) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		};
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (hand == InteractionHand.OFF_HAND) {
			return InteractionResult.PASS;
		}

		List<Function<Void, Map.Entry<InteractionResult, Boolean>>> handlers = Arrays.asList(
				(Void v) -> handleExtractFinishItem(state, world, pos, player, hand, hit),
				(Void v) -> handleSmashNew(state, world, pos, player, hand, hit),
				(Void v) -> configureKingGarden(world, pos, player),
				(Void v) -> handleReversePlaceItem(state, world, pos, player, hand, hit),
				(Void v) -> handlePlaceItemNew(state, world, pos, player, hand, hit)
		);
		Map.Entry<InteractionResult, Boolean> result = new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);

		for (var handler : handlers) {
			result = handler.apply(null);

			if (result.getKey() != InteractionResult.CONSUME) {
				break;
			}
		}
		player.swing(result.getValue() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

		return result.getKey();
	}

	public Map.Entry<InteractionResult, Boolean> handleExtractFinishItem(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!isEmpty() && strikes == FINISH_CRAFT_STRIKE_FLAG) {
			ItemStack stack = extractPedestal();
			if (player.addItem(stack)) {
				playSound();
			} else {
				Containers.dropItemStack(world, player.getX(), player.getY(), player.getZ(), stack);
			}
			return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME_PARTIAL, false);
		}
		return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);
	}

	public Map.Entry<InteractionResult, Boolean> handleSmashNew(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		boolean swingOffHand = false;

		if (!isEmpty()) {
			ItemStack mainHandItem = player.getMainHandItem();
			ItemStack offHandItem = player.getOffhandItem();

			boolean denyInteraction = false;

			for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE)) {
				PedestalRecipe recipe = holder.value();
				if (!recipe.getSmashTools().test(mainHandItem) && !recipe.getSmashTools().test(offHandItem)) {
					continue;
				} else {
					denyInteraction = true;
				}

				if (!recipe.getInput().test(this.getItem())) {
					continue;
				}

				ItemStack smashTools = null;
				if (recipe.getSmashTools().test(mainHandItem)) {
					smashTools = mainHandItem;
				} else if (recipe.getSmashTools().test(offHandItem)) {
					smashTools = offHandItem;
					swingOffHand = true;
				}
				boolean finalSwingOffHand = swingOffHand;

				//TODO 过于OP暂未启用
				/*
				* 时运：经验加成
				* 效率：击打次数加成
				* 经验修补：自动化自我修复
				* */
				int expBoost = getEnchantmentLevel(smashTools, Enchantments.FORTUNE);
				int efficiency = getEnchantmentLevel(smashTools, Enchantments.EFFICIENCY);

				smashTools.hurtAndBreak(1, player,
						finalSwingOffHand ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);

				this.setStrikes(this.strikes + 1 /*+ efficiency*/);
				if (!level.isClientSide()) {
					PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.GOODTEK), "code_triggered");
				}

				if (strikes > 0 && strikes < recipe.getStrike()) {
					level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.PLAYERS, .8f,
							((player.level().random.nextFloat() - player.level().random.nextFloat()) * .7f + 1) * 2);
					continue;
				}

				ItemStack output = recipe.getOutput().copy();
				this.setItem(output);
				level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, .8F,
						((player.level().random.nextFloat() - player.level().random.nextFloat()) * .7f + 1) * 2);
				if (!level.isClientSide()) {

					createExperience((ServerLevel) level, player.position(), recipe.getExp() /* * expBoost*/);
				}
				setStrikes(FINISH_CRAFT_STRIKE_FLAG);
				break;
			}

			return denyInteraction ? new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME_PARTIAL, swingOffHand) : new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, swingOffHand);
		}

		return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, swingOffHand);
	}

	public Map.Entry<InteractionResult, Boolean> handleReversePlaceItem(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		boolean swingOffHand = false;
		if (isEmpty()) {
			boolean reversePriority = false;
			ItemStack mainHandItem = player.getMainHandItem();
			if (!mainHandItem.isEmpty()) {
				for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE)) {
					PedestalRecipe recipe = holder.value();
					if (recipe.getSmashTools().test(mainHandItem)) {
						reversePriority = true;
						break;
					}
				}
			}
			if (reversePriority) {
				ItemStack offHandItem = player.getOffhandItem();

				if (!offHandItem.isEmpty()) {
					insertPedestal(offHandItem);
					playSound();
					swingOffHand = true;
				} else if (!mainHandItem.isEmpty()) {
					insertPedestal(mainHandItem);
					playSound();
				}

				return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME_PARTIAL, swingOffHand);
			}
		}
		return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, swingOffHand);
	}

	public Map.Entry<InteractionResult, Boolean> handlePlaceItemNew(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		boolean swingOffHand = false;
		if (isEmpty()) {
			ItemStack mainHandItem = player.getMainHandItem();
			ItemStack offHandItem = player.getOffhandItem();

			if (!mainHandItem.isEmpty()) {
				insertPedestal(mainHandItem);
				playSound();
			} else if (!offHandItem.isEmpty()) {
				insertPedestal(offHandItem);
				playSound();
				swingOffHand = true;
			}
		} else {
			ItemStack stack = extractPedestal();
			if (player.addItem(stack)) {
				playSound();
			} else {
				Containers.dropItemStack(world, player.getX(), player.getY(), player.getZ(), stack);
			}
		}
		return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, swingOffHand);
	}

	private void playSound() {
		level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
	}

	private static void createExperience(ServerLevel world, Vec3 pos, int amount) {
		ExperienceOrb.award(world, pos, amount);
	}

	public void insertPedestal(ItemStack itemStack) {
		ItemStack stack = itemStack.split(1);
		this.setItem(stack);
	}

	public ItemStack extractPedestal() {
		ItemStack stack = getItem().copy();
		this.setItem(ItemStack.EMPTY);
		return stack;
	}

	@Override
	public ItemStack getItem() {
		return getItem(0);
	}

	@Override
	public void setItem(ItemStack itemStack) {
		setItem(0, itemStack);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		this.setStrikes(0);

		BlockState state = getBlockState();

		this.level.blockEvent(getBlockPos(), state.getBlock(), 0, 0);
		this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));

		this.level.setBlock(getBlockPos(), state.setValue(PedestalBlock.HAS_ITEM, !stack.isEmpty()), 3);

		super.setItem(index, stack);

		this.markUpdated();
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		this.setStrikes(0);

		BlockState state = getBlockState();

		this.level.blockEvent(getBlockPos(), state.getBlock(), 0, 0);
		this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));

		this.level.setBlock(getBlockPos(), state.setValue(PedestalBlock.HAS_ITEM, false), 3);

		ItemStack superItemStack = super.removeItem(index, count);
		this.markUpdated();
		return superItemStack;
	}

	private Map.Entry<InteractionResult, Boolean> configureKingGarden(Level world, BlockPos pos, Player player) {
		var garden = getItem();
		if (!(garden.getItem() instanceof KingGardenItem)
				|| KingGardenItem.getTypes(garden).length >= KingGardenItem.MAX_FLOWERS)
			return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);
		boolean offhand = KingGardenItem.getFlowerType(player.getMainHandItem()) < 0;
		var flower = offhand ? player.getOffhandItem() : player.getMainHandItem();
		if (KingGardenItem.getFlowerType(flower) < 0)
			return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);
		if (!world.isClientSide() && KingGardenItem.addFlower(garden, flower)) {
			if (!player.getAbilities().instabuild) flower.shrink(1);
			setStrikes(0); markUpdated();
			world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, .5F,
					((world.random.nextFloat() - world.random.nextFloat()) * .2F + 1) * 1.5F);
		}
		return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME_PARTIAL, offhand);
	}

	private void markUpdated() {
		this.setChanged();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
		this.pendingSync = false;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity self) {
		// The original pedestal accumulates daytime ticks, without a sky-visibility check.
		if (!self.getItem().is(ExtraBotanyItems.nightmareFuel)) {
			self.solarTicks = 0;
		} else if (level.isDay() && ++self.solarTicks >= 6000) {
			self.solarTicks = 0;
			self.setItem(new ItemStack(ExtraBotanyItems.spiritFuel));
		}
		// 结构的等级只用于给自然能量物品充能，所以只在充能时检查结构。
		// The tier of the structure is only used to charge a nature energy item, thus the structure is only examined during a charge.
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(self.getItem());
		if (natureItem != null && natureItem.getEnergy() < natureItem.getMaxEnergy()) {
			if (self.tickCount % 20 == 0) {
				self.updateTier();
			}

			int chargeAmount = self.getChargeAmount();
			if (chargeAmount > 0) {
				long energyBefore = natureItem.getEnergy();
				natureItem.addEnergy(chargeAmount);
				if (natureItem.getEnergy() != energyBefore) {
					self.setChanged();
					self.pendingSync = true;
				}
				WispParticleData data = WispParticleData.wisp(0.5F, 0.15F, 0.8F, 0.15F);
				((ServerLevel) level).sendParticles(data, pos.getX() + 0.5F, pos.getY() + 1.1F, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
			}

		}
		// Persist real changes immediately, while retaining the ten-tick client update rate.
		// This also flushes partial strikes and the final charge of a now-full orb.
		if (self.pendingSync && self.tickCount % 10 == 0) {
			self.markUpdated();
		}

		if (self.tickCount % 20 == 0) {
			int lastHammers = self.automaticHammers.size();
			self.updateAutomaticHammers();
			if (lastHammers != self.automaticHammers.size() && self.automaticHammers.size() == 4) {
				Player nearestPlayer = level.getNearestPlayer(
						pos.getX(), pos.getY(), pos.getZ(),
						10, false
				);
				if (nearestPlayer != null) {
					PlayerHelper.grantCriterion((ServerPlayer) nearestPlayer, prefix("main/" + LibAdvancementNames.KURUKURU), "code_triggered");
				}
			}
		}

		if (self.tickCount % 10 == 0 && !self.isEmpty()) {
			for (ItemStack hammer : self.automaticHammers.keySet()) {
				ItemFrame frame = self.automaticHammers.get(hammer);
				if (frame == null || !frame.isAlive()) {
					continue;
				}
				if (self.tryAutoSmash(hammer)) {
					break;
				}
			}
		}

		self.tickCount++;
	}

	public int getChargeAmount() {
		int amount = 0;

		if (getTier() >= 1) {
			amount += 4;
		}
		if (getTier() >= 2) {
			amount += 5;
			for (BlockPos pos : POOL_LOCATIONS) {
				BlockEntity tile = getLevel().getBlockEntity(getBlockPos().offset(pos.getX(), pos.getY(), pos.getZ()));
				if (tile instanceof ManaPoolBlockEntity pool && pool.getCurrentMana() >= 10) {
					pool.receiveMana(-10);
					amount += 2;
				}
			}
		}

		return amount;
	}

	/**
	 * 检查祭坛周围的多方块结构，并更新等级。
	 * Examines the multiblock structure around the pedestal and updates the tier.
	 *
	 * <p>祭坛必须在结构的中心，所以图案的起点是固定的，只需要比对一次。以前的代码在一个立方体里尝试几百个起点，而且会把结构旁边的祭坛也算作有效。
	 * <p>The pedestal must be at the center of the structure, thus the origin of the pattern is fixed and only one comparison is necessary. The code before tried some hundreds of origins in a cube, and it also accepted a pedestal that was adjacent to a structure.
	 */
	public void updateTier() {
		BlockPos pos = getBlockPos();
		if (TIER_2_STRUCTURE.matches(getLevel(), pos.offset(3, 1, 3), Direction.DOWN, Direction.SOUTH) != null) {
			setTier(2);
		} else if (TIER_1_STRUCTURE.matches(getLevel(), pos.offset(2, 0, 2), Direction.DOWN, Direction.SOUTH) != null) {
			setTier(1);
		} else {
			setTier(0);
		}
	}

	public void setTier(int tier) {
		this.tier = tier;
	}

	@Override
	public int getTier() {
		return this.tier;
	}

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state, PedestalBlockEntity self) {
		self.tickCount++;
	}

	public boolean tryAutoSmash(ItemStack hammer) {
		for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE)) {
			PedestalRecipe recipe = holder.value();
			if (!recipe.getSmashTools().test(hammer)) {
				continue;
			}
			if (!recipe.getInput().test(this.getItem())) {
				if (ItemStack.isSameItemSameComponents(recipe.getOutput(), this.getItem())) {
					return false;
				}
				continue;
			}

			int expBoost = getEnchantmentLevel(hammer, Enchantments.FORTUNE);
			int efficiency = getEnchantmentLevel(hammer, Enchantments.EFFICIENCY);

			if (hammer.isDamageableItem() && level instanceof ServerLevel serverLevel) {
				ItemFrame frame = automaticHammers.get(hammer);
				hammer.hurtAndBreak(1, serverLevel, (LivingEntity) null, brokenItem -> {
					if (frame != null) {
						frame.setItem(ItemStack.EMPTY);
					}
				});
			}

			this.setStrikes(this.strikes + 1 /*+ efficiency*/);
			if (strikes > 0 && strikes < recipe.getStrike()) {
				level.playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, .8f,
						((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
				return true;
			}

			ItemStack output = recipe.getOutput().copy();
			this.setItem(output);
			level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, .8F,
					((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
			setStrikes(FINISH_CRAFT_STRIKE_FLAG);

			//Mending
			/*
			int exp = recipe.getExp() * expBoost;
			if (hammer != null && !hammer.isEmpty() && hammer.isDamageableItem()) {
				int mendingLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MENDING, hammer);
			
				if (mendingLevel > 0 && exp > 0) {
					int repairAmount = Math.min(exp * 2, hammer.getDamageValue());
					hammer.setDamageValue(hammer.getDamageValue() - repairAmount);
				}
			}
			
			*/

			return true;
		}
		return false;
	}

	public Map<ItemStack, ItemFrame> getPerhapsHammers() {
		return level.getEntitiesOfClass(ItemFrame.class,
				new AABB(worldPosition).inflate(2.0)
		).stream()
				.filter(frame -> frame.getPos().relative(frame.getDirection().getOpposite()).equals(worldPosition))
				.filter(itemFrame -> !itemFrame.getItem().isEmpty())
				.collect(Collectors.toMap(ItemFrame::getItem, itemFrame -> itemFrame, (oldFrame, newFrame) -> newFrame));
	}

	public void updateAutomaticHammers() {
		this.automaticHammers = getPerhapsHammers();
		if (this.automaticHammers == null) {
			this.automaticHammers = new HashMap<>();
		}
	}

	@Override
	public int getStrikes() {
		return strikes;
	}

	@Override
	public void setStrikes(int strikes) {
		if (this.strikes != strikes) {
			this.strikes = strikes;
			if (level != null && !level.isClientSide()) {
				setChanged();
				pendingSync = true;
			}
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.strikes = tag.getInt("strikes");
		// Like the 1.12.2 implementation, daylight progress is local to the loaded pedestal.
		this.solarTicks = 0;
		this.pendingSync = false;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt("strikes", strikes);
	}

	private int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
		if (level == null) {
			return 0;
		}
		return stack.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(enchantment));
	}

	public int getAnalogOutputSignal() {
		return this.strikes == FINISH_CRAFT_STRIKE_FLAG ? 15 : 0;
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return this.getItem().isEmpty();
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.strikes == FINISH_CRAFT_STRIKE_FLAG;
	}
}
