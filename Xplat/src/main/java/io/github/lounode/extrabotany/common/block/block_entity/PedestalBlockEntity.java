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
import net.minecraft.world.item.crafting.Recipe;
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
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

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

	public final int FINISH_CRAFT_STRIKE_FLAG = -1;
	public int tickCount;
	private int strikes;
	private int tier;
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
				(Void v) -> configureLegacyGarden(world, pos, player),
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

			for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE).values()) {
				if (!(r instanceof PedestalRecipe recipe)) {
					continue;
				}
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

				this.strikes += 1 /*+ efficiency*/;
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
				strikes = FINISH_CRAFT_STRIKE_FLAG;
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
				for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE).values()) {
					if (!(r instanceof PedestalRecipe recipe)) {
						continue;
					}
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

	private Map.Entry<InteractionResult, Boolean> configureLegacyGarden(Level world, BlockPos pos, Player player) {
		var garden = getItem();
		if (!garden.is(io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.INSTANCE)
				|| io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.types(garden).length >= 20)
			return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);
		boolean offhand = io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.flowerType(player.getMainHandItem()) < 0;
		var flower = offhand ? player.getOffhandItem() : player.getMainHandItem();
		if (io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.flowerType(flower) < 0)
			return new AbstractMap.SimpleEntry<>(InteractionResult.CONSUME, false);
		if (!world.isClientSide() && io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.addFlower(garden, flower)) {
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
	}

	//TODO 精神燃料自动化
	public static void serverTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity self) {
		if (self.tickCount % 10 == 0) {
			self.markUpdated();
		}

		//Lazy
		if (self.tickCount % (20 * 5) == 0) {
			self.updateTier();
		}

		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(self.getItem());
		if (natureItem != null) {
			//Hot
			if (self.tickCount % 20 == 0) {
				self.updateTier();
			}

			int chargeAmount = self.getChargeAmount();
			if (chargeAmount > 0) {
				natureItem.addEnergy(chargeAmount);
				WispParticleData data = WispParticleData.wisp(0.5F, 0.15F, 0.8F, 0.15F);
				((ServerLevel) level).sendParticles(data, pos.getX() + 0.5F, pos.getY() + 1.1F, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
			}

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

	public void updateTier() {
		setTier(0);
		if (new BlockPatternExtend(TIER_1_PATTERN).findFlat(getLevel(), getBlockPos()) != null) {
			setTier(1);
		}
		if (new BlockPatternExtend(TIER_2_PATTERN).findFlat(getLevel(), getBlockPos()) != null) {
			setTier(2);
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
		for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE).values()) {
			if (!(r instanceof PedestalRecipe recipe)) {
				continue;
			}
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

			this.strikes += 1 /*+ efficiency*/;
			if (strikes > 0 && strikes < recipe.getStrike()) {
				level.playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, .8f,
						((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
				return true;
			}

			ItemStack output = recipe.getOutput().copy();
			this.setItem(output);
			level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, .8F,
					((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
			strikes = FINISH_CRAFT_STRIKE_FLAG;

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
		this.strikes = strikes;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.setStrikes(tag.getInt("strikes"));
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

	//TODO 活石祭坛HUD
	public static class HUD {

	}
}
