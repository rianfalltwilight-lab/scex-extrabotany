package io.github.lounode.extrabotany.common.block.flower.functional;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import vazkii.botania.api.block.Wandable;
import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.mana.ManaPool;

import io.github.lounode.extrabotany.api.gaia.BlockPatternExtend;
import io.github.lounode.extrabotany.api.gaia.BlockPatternExtendBuilder;
import io.github.lounode.extrabotany.api.gaia.BlockTagPredicate;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.Optional;

/**
 * 魔链星。它把自己的魔力送到一个已绑定的魔力池。这个魔力池可以在别的维度，但是它所在的区块必须已加载。
 * Manalink. It sends its mana to a bound mana pool. This mana pool can be in a different dimension, but its chunk must be loaded.
 *
 * <p>花的下面必须有 5×5 的多方块结构（见 {@link #PATTERN}）。玩家用森林法杖的绑定模式潜行右键一个魔力池来选择它，再潜行右键这朵花来完成绑定。这部分逻辑在 {@code WandOfTheForestExtension} 里。红石信号让花停止工作。
 * <p>Below the flower there must be a 5×5 multiblock structure (see {@link #PATTERN}). In the bind mode of the Wand of the Forest, the player sneak-uses a mana pool to select it, and then sneak-uses this flower to complete the binding. That logic is in {@code WandOfTheForestExtension}. A redstone signal stops the flower.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileManalinkium}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileManalinkium} of ExtraMeteorP 1.12.
 */
public class ManalinkBlockEntity extends ExtraFunctionalFlowerBlockEntity implements Wandable {

	public static final String[][] PATTERN = {
			{

					"QQQQQ",
					"QLOLQ",
					"QODOQ",
					"QLOLQ",
					"QQQQQ"
			}
	};

	public static final String[][] PATCHOULI_PATTERN = {
			{
					"AAAAA",
					"AAAAA",
					"AA0AA",
					"AAAAA",
					"AAAAA"
			},
			{

					"QQQQQ",
					"QLOLQ",
					"QODOQ",
					"QLOLQ",
					"QQQQQ"
			},
	};

	public static final BlockPatternExtend PATTERN_BUILTIN = new BlockPatternExtend(BlockPatternExtendBuilder.start()
			.aisle(PATTERN[0])
			.where('D', BlockInWorld.hasState(BlockTagPredicate.forTag(BlockTags.DIRT)))
			.where('Q', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.QUARTZ_BLOCK)))
			.where('L', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.LAPIS_BLOCK)))
			.where('O', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.OBSIDIAN)))
			.createPattern());

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String TAG_LINK_POS = "linkPos";

	public static final int TRANSFER_SPEED = 400;

	@Nullable
	private GlobalPos linkPos = null;

	private static final int PATTERN_RECHECK_TICKS = 20;
	private long nextPatternCheck;
	private boolean patternValid;

	public ManalinkBlockEntity(BlockEntityType<? extends ManalinkBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ManalinkBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.MANALINK, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}

		if (getMana() <= 0) {
			return;
		}

		GlobalPos pos = linkPos;
		if (pos == null) {
			return;
		}

		ManaPool pool = getManaPool(getLevel().getServer(), pos);
		if (pool == null) {
			return;
		}
		if (findBoundTile() == pool) {
			return;
		}

		if (!isPatternValidCached()) {
			return;
		}

		int transferMana = Math.min(getTransferSpeed(), getMana());
		int poolCanReceiveMana = pool.getMaxMana() - pool.getCurrentMana();
		transferMana = Math.min(transferMana, poolCanReceiveMana);

		if (transferMana > 0) {
			addMana(-transferMana);
			pool.receiveMana(transferMana);
			// Mana only: was an immediate full update packet on every transfer tick.
			syncThrottled();
		}
	}

	/**
	 * 花下面的多方块结构是否完整。结果每 {@value #PATTERN_RECHECK_TICKS} tick 重新检查一次。
	 * Tells if the multiblock structure below the flower is complete. The result is examined again one time for each {@value #PATTERN_RECHECK_TICKS} ticks.
	 *
	 * <p>花必须在结构中心的正上方，所以图案的起点是固定的，只需要比对一次。
	 * <p>The flower must be directly above the center of the structure, thus the origin of the pattern is fixed and only one comparison is necessary.
	 */
	private boolean isPatternValidCached() {
		long now = getLevel().getGameTime();
		if (now >= nextPatternCheck) {
			patternValid = PATTERN_BUILTIN.matches(getLevel(), getBlockPos().below().offset(2, 0, 2), Direction.DOWN, Direction.SOUTH) != null;
			nextPatternCheck = now + PATTERN_RECHECK_TICKS;
		}
		return patternValid;
	}

	@Nullable
	private ManaPool getManaPool(@Nullable MinecraftServer server, GlobalPos pos) {
		if (server == null) {
			return null;
		}

		ResourceKey<Level> type = pos.dimension();
		Level world = server.getLevel(type);
		if (world != null && world.isLoaded(pos.pos())) {
			var receiver = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaReceiver(world, pos.pos(), null);
			if (receiver instanceof ManaPool pool) {
				return pool;
			}
		}

		return null;
	}

	public int getTransferSpeed() {
		return ExtraBotanyConfig.common().manalinkTransferSpeed();
	}

	@Override
	public int getMaxMana() {
		return getTransferSpeed() * 2;
	}

	@Override
	public int getColor() {
		return 0x00FFFF;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return null;
	}

	public Optional<GlobalPos> getLinkPos() {
		return Optional.ofNullable(linkPos);
	}

	public void setLinkPos(GlobalPos pos) {
		this.linkPos = pos;
		sync();
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		if (linkPos != null) {
			GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, linkPos)
					.resultOrPartial(LOGGER::error)
					.ifPresent((tag) -> cmp.put(TAG_LINK_POS, tag));
		}
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		linkPos = cmp.contains(TAG_LINK_POS)
				? GlobalPos.CODEC.parse(NbtOps.INSTANCE, cmp.get(TAG_LINK_POS))
						.resultOrPartial(LOGGER::error).orElse(null)
				: null;
	}

	@Override
	public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
		return false;
	}
}
