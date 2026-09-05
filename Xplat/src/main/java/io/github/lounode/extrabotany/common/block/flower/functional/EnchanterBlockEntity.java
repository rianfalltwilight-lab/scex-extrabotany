package io.github.lounode.extrabotany.common.block.flower.functional;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import vazkii.botania.api.BotaniaAPIClient;
import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.BotaniaBlocks;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.*;

public class EnchanterBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	private static final String TAG_CONSUMED = "consumedMana";

	public static final int RANGE = 3;

	public static final int CONSUME_SPEED = 400;
	public static final int TRANSFORM_COST = 250000;

	private int consumedMana;

	public EnchanterBlockEntity(BlockEntityType<? extends EnchanterBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public EnchanterBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.ENCHANTER, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}

		if (consumedMana < getTransformCost()) {
			int manaCanConsume = Math.min(getConsumeSpeed(), getMana());
			if (manaCanConsume > 0) {
				addMana(-manaCanConsume);
				setConsumedMana(getConsumedMana() + manaCanConsume);
				sync();
			}
			return;
		}

		if (ticksExisted % 20 != 0) {
			return;
		}

		BlockPos transformPos = getRandomPos();
		if (transformPos == null) {
			return;
		}

		getLevel().setBlockAndUpdate(transformPos, ExtraBotanyBlocks.enchantedSoil.defaultBlockState());

		setConsumedMana(0);
		sync();
	}

	@Nullable
	public BlockPos getRandomPos() {
		Map<BlockPos, Float> coordsMap = new HashMap<>();
		Random rng = new Random();
		BlockPos pos = getEffectivePos();

		for (BlockPos pos_ : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
			BlockState state = getLevel().getBlockState(pos_);
			BlockState above = getLevel().getBlockState(pos_.above());
			if (!state.is(Blocks.GRASS_BLOCK) || !above.isAir()) {
				continue;
			}
			coordsMap.put(pos_.immutable(), 2.0f * rng.nextFloat());
		}

		if (coordsMap.isEmpty()) {
			return null;
		}

		float maxDistance = 0F;
		for (float distance : coordsMap.values()) {
			maxDistance = Math.max(maxDistance, distance);
		}

		float finalMaxDistance = maxDistance;
		coordsMap.values().removeIf(dist -> dist < finalMaxDistance - 1f);
		List<BlockPos> coords = new ArrayList<>(coordsMap.keySet());

		BlockPos breakCoords = coords.get(level.getRandom().nextInt(coords.size()));
		return breakCoords;
	}

	public int getTransformCost() {
		return ExtraBotanyConfig.common().enchanterTransformCost();
	}

	public int getConsumeSpeed() {
		return ExtraBotanyConfig.common().enchanterConsumeSpeed();
	}

	@Override
	public int getMaxMana() {
		return getConsumeSpeed() * 4;
	}

	@Override
	public int getColor() {
		return 0x4B0082;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	public int getConsumedMana() {
		return consumedMana;
	}

	public void setConsumedMana(int consumedMana) {
		this.consumedMana = consumedMana;
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_CONSUMED, getConsumedMana());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setConsumedMana(cmp.getInt(TAG_CONSUMED));
	}

	public static class WandHUD extends BindableFlowerWandHud<EnchanterBlockEntity> {

		public WandHUD(EnchanterBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphics gui, Window window, Font font, int minLeft, int minRight, int minDown) {
			String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
			int color = flower.getColor();

			int centerX = window.getGuiScaledWidth() / 2;
			int centerY = window.getGuiScaledHeight() / 2;
			int left = (Math.max(102, font.width(name)) + 4) / 2;
			// padding + item
			int right = left + 20;

			left = Math.max(left, minLeft);
			right = Math.max(right, minRight);

			RenderHelper.renderHUDBox(gui, centerX - left, centerY + 8, centerX + right, centerY + Math.max(30, minDown + 52));

			BotaniaAPIClient.instance().drawComplexManaHUD(gui, window, font, color, flower.getConsumedMana(), flower.getTransformCost(),
					name, flower.getHudIcon(), flower.isValidBinding());

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			int arrowU = 0;
			int arrowV = 38;
			RenderHelper.drawTexturedModalRect(gui, HUDHandler.manaBar, centerX - 11, centerY + 34, arrowU, arrowV, 22, 15);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			ItemStack grass = new ItemStack(Items.GRASS_BLOCK);
			ItemStack enchanted = new ItemStack(ExtraBotanyBlocks.enchantedSoil);

			gui.renderItem(grass, centerX - 31, centerY + 34);
			gui.renderItem(enchanted, centerX + 15, centerY + 34);
		}
	}
}
