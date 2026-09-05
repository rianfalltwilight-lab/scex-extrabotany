package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.flower.functional.DaffomillBlockEntity;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.api.block.PassiveFlower;
import io.github.lounode.extrabotany.api.level.Wind;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import io.github.lounode.extrabotany.mixin.accessor.DaffomillBlockEntityAccessor;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class BellflowerBlockEntity extends ExtraGeneratingFlowerBlockEntity implements PassiveFlower {

	public static final String TAG_DYING = "dying";

	public static int MAX_MANA = 300;
	public static final double GENERATE_MODIFY = 1;
	private static final int SPACE_REQUIRE = 2;
	private static final double WIND_LEVEL_MODIFY = 0.5D;

	private int passiveDecayTicks;
	private boolean dying;
	private int dyingTicks;

	public BellflowerBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.BELLFLOWER, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();

		if (isDying()) {
			sync();
			dyingAnime();
			dyingTicks++;
			return;
		}

		if (getLevel().isClientSide()) {
			return;
		}

		checkToDecay(this);

		if (!getLevel().canSeeSky(getEffectivePos())) {
			return;
		}

		if (ticksExisted % 5 != 0) {
			return;
		}

		if (ticksExisted % (20 * 5) == 0 && getLevel().getRandom().nextInt(2) == 0) {
			getLevel().playSound(null, getEffectivePos(), ExtraBotanySounds.BELL_FLOWER_RING, SoundSource.BLOCKS,
					0.5F, SoundEventUtil.randomPitch(getLevel()));
		}
		addMana(Math.max(0, getGenerateMana()));
		sync();
	}

	private void dyingAnime() {
		if (dyingTicks < 3 * 20) {
			if (dyingTicks % 10 == 0) {
				for (int i = 0; i < 3; i++) {
					double offsetX = getLevel().getRandom().nextDouble() * 2 - 1;
					double offsetY = getLevel().getRandom().nextDouble() * 0.5;
					double offsetZ = getLevel().getRandom().nextDouble() * 2 - 1;

					Vec3 particlePos = Vec3.atCenterOf(getBlockPos())
							.add(offsetX, offsetY, offsetZ);

					getLevel().addParticle(
							ParticleTypes.ANGRY_VILLAGER,
							particlePos.x,
							particlePos.y,
							particlePos.z,
							0, 0.05, 0
					);
				}
			}
			return;
		}
		if (getLevel().isClientSide()) {
			return;
		}
		getLevel().destroyBlock(getBlockPos(), false);
		if (Blocks.DEAD_BUSH.defaultBlockState().canSurvive(getLevel(), getBlockPos())) {
			getLevel().setBlockAndUpdate(getBlockPos(), Blocks.DEAD_BUSH.defaultBlockState());
		}

		var players = PlayerHelper.getRealPlayersIn(getLevel(), new AABB(getBlockPos()).inflate(16));
		for (var player : players) {
			if (PlayerHelper.hasAdvancement((ServerPlayer) player, prefix("main/" + LibAdvancementNames.A_BALDRUPT))) {
				continue;
			}
			PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.A_BALDRUPT), "code_triggered");
		}
	}

	public int getGenerateMana() {
		double windLevel = Wind.instance().getWindLevel(getLevel(), new Vec3(getEffectivePos().getX(), getEffectivePos().getY(), getEffectivePos().getZ()));
		double negativeBlockModify = countNegativeBlocks();

		return (int) (((windLevel - negativeBlockModify) * getWindLevelModify()) * getGenerateModify());
	}

	public int countNegativeBlocks() {
		int negative = 0;

		BlockPos pos = getEffectivePos();
		for (BlockPos pos_ : BlockPos.betweenClosed(
				pos.offset(-getSpaceRequire(), 0, -getSpaceRequire()),
				pos.offset(getSpaceRequire(), getSpaceRequire(), getSpaceRequire()))) {
			if (pos_.equals(pos)) {
				continue;
			}
			BlockState state = getLevel().getBlockState(pos_);
			if (state.is(ExtrabotanyFlowerBlocks.bellflower) || state.is(ExtrabotanyFlowerBlocks.bellflowerFloating)) {
				negative++;
			}
			if (!state.isAir() && !state.is(ExtraBotanyTags.Blocks.BELLFLOWER_IGNORE)) {
				negative++;
			}

			if (state.is(BotaniaBlocks.DAFFOMILL) || state.is(BotaniaBlocks.FLOATING_DAFFOMILL)) {
				checkBeBlew(pos_);
			}
		}

		return negative;
	}

	public void checkBeBlew(BlockPos daffomillPos) {
		var tile = getLevel().getBlockEntity(daffomillPos);
		if (!(tile instanceof DaffomillBlockEntity daffomill)) {
			return;
		}
		if (daffomill.isPowered()) {
			return;
		}
		if (((DaffomillBlockEntityAccessor) daffomill).getWindTicks() <= 0) {
			return;
		}
		var aabb = ((DaffomillBlockEntityAccessor) daffomill).extrabotany_aabbForOrientation();
		Vec3 posVec = Vec3.atCenterOf(getBlockPos());

		if (!aabb.contains(posVec)) {
			return;
		}

		dying = true;
	}

	public int getSpaceRequire() {
		return SPACE_REQUIRE;
	}

	public double getWindLevelModify() {
		return WIND_LEVEL_MODIFY;
	}

	public double getGenerateModify() {
		return ExtraBotanyConfig.common().bellflowerGenerateModify();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().bellflowerMaxMana();
	}

	@Override
	public int getColor() {
		return 0xFFFF99;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return null;
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setPassiveDecayTicks(cmp.getInt(TAG_PASSIVE_DECAY_TICKS));
		dying = cmp.getBoolean(TAG_DYING);
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_PASSIVE_DECAY_TICKS, getPassiveDecayTicks());
		cmp.putBoolean(TAG_DYING, dying);
	}

	@Override
	public int getPassiveDecayTicks() {
		return passiveDecayTicks;
	}

	@Override
	public void setPassiveDecayTicks(int ticks) {
		this.passiveDecayTicks = ticks;
	}

	public boolean isDying() {
		return dying;
	}

	public static class WandHud extends BindableFlowerWandHud<BellflowerBlockEntity> {
		public WandHud(BellflowerBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphics gui, Window window, Font font, float partialTick) {
			double windLevel = Wind.instance().getWindLevel(flower.getLevel(), new Vec3(flower.getEffectivePos().getX(), flower.getEffectivePos().getY(), flower.getEffectivePos().getZ()));

			if (flower.isDying()) {
				windLevel = 999;
			}

			Component component = Component.translatable(
					"message.extrabotany.chat.wind_level",
					String.format("%.2f", windLevel)
			);
			int halfWidth = (font.width(component) + 24) / 2;
			int centerX = window.getGuiScaledWidth() / 2;
			int centerY = window.getGuiScaledHeight() / 2;

			super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);
			gui.drawString(font, component, centerX - halfWidth + 12, centerY + 34, !flower.isDying() ? 0xFFFFFF : ChatFormatting.DARK_RED.getColor());
		}
	}
}
