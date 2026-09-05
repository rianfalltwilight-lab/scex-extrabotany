package io.github.lounode.extrabotany.common.block.flower.functional;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
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
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.gaia.BlockPatternExtend;
import io.github.lounode.extrabotany.api.gaia.BlockPatternExtendBuilder;
import io.github.lounode.extrabotany.api.gaia.BlockTagPredicate;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.Optional;

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

	public ManalinkBlockEntity(BlockEntityType<? extends ManalinkBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ManalinkBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.MANALINK, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
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
		if (findBoundTile() != null && findBoundTile() == pool) {
			return;
		}

		if (PATTERN_BUILTIN.findFlat(getLevel(), getBlockPos().below()) == null) {
			return;
		}

		int transferMana = Math.min(getTransferSpeed(), getMana());
		int poolCanReceiveMana = pool.getMaxMana() - pool.getCurrentMana();
		transferMana = Math.min(transferMana, poolCanReceiveMana);

		if (transferMana > 0) {
			addMana(-transferMana);
			pool.receiveMana(transferMana);
			sync();
		}
	}

	@Nullable
	private ManaPool getManaPool(@Nullable MinecraftServer server, GlobalPos pos) {
		if (server == null) {
			return null;
		}

		ResourceKey<Level> type = pos.dimension();
		Level world = server.getLevel(type);
		if (world != null) {
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

		GlobalPos.CODEC.parse(NbtOps.INSTANCE, cmp.get(TAG_LINK_POS))
				.resultOrPartial(LOGGER::error)
				.ifPresent(this::setLinkPos);
	}

	@Override
	public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
		return false;
	}

	public static class WandHUD extends BindableFlowerWandHud<ManalinkBlockEntity> {

		public WandHUD(ManalinkBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphics gui, Window window, Font font, int minLeft, int minRight, int minDown) {
			float scale = 0.8F;
			GlobalPos pos = flower.linkPos;

			if (pos == null) {
				super.renderHUD(gui, window, font, minLeft, minRight, minDown);
				return;
			}

			Component levelComponent = Component.translatable("message.extrabotany.actionbar.bind_to_pos", pos.dimension().location() + String.format(" %d, %d, %d", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()));

			int halfWidth = (int) (((float) (font.width(levelComponent) + 24) / 2) * scale);
			int centerX = window.getGuiScaledWidth() / 2;
			int centerY = window.getGuiScaledHeight() / 2;

			super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);

			gui.pose().pushPose();
			gui.pose().scale(scale, scale, 1.0F);

			gui.drawString(font, levelComponent, (int) ((centerX - halfWidth + 12) / scale), (int) ((centerY + 34) / scale), ChatFormatting.GREEN.getColor());

			gui.pose().popPose();
		}
	}
}
