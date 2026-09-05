package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.data.AdvancementProvider;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class TinkleBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_TIME = "time";

	public static final int MAX_MANA = 1000;
	private static final int RANGE = 8;
	private static final int COOLDOWN = 20;
	private static final int PRODUCE_REQUIRE = 10;
	public static final int PRODUCE_MANA = 100;

	private int time;

	public TinkleBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.TINKLE, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}

		var players = getLevel().getEntitiesOfClass(Player.class, getCheckAABB()).stream()
				.filter(PlayerHelper::isTruePlayer)
				.toList();

		for (var player : players) {
			double vx = player.getX() - player.xOld;
			double vy = player.getY() - player.yOld;
			double vz = player.getZ() - player.zOld;

			double vel = Math.max(player.getDeltaMovement().lengthSqr(), Math.sqrt(vx * vx + vy * vy + vz * vz));

			if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
				vel *= 1.2F;
			}

			int timeAdd = Mth.clamp((int) (vel * 100.0), 0, 8);
			//player.sendSystemMessage(Component.literal(String.valueOf(timeAdd)));
			time += timeAdd;

			if (time >= PRODUCE_REQUIRE) {
				addMana(getProduceMana());

				player.causeFoodExhaustion(0.02F);
				PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.PROJECT_DIVA_DESU), AdvancementProvider.CODE_TRIGGE);
				time = 0;
				sync();
			}
		}
	}

	public int getProduceMana() {
		return ExtraBotanyConfig.common().tinkleProduceMana();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().tinkleMaxMana();
	}

	@Override
	public int getColor() {
		return 0xCCFF00;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	public AABB getCheckAABB() {
		return new AABB(getEffectivePos()).inflate(RANGE);
	}
}
