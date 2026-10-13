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
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 叮当舞花。范围里的玩家移动时，它产生魔力，并让玩家多消耗一点饥饿值。玩家移动得越快，它产生魔力越快。红石信号让它停止工作。
 * Tinkle. When a player in the range moves, it makes mana and the player uses a little more hunger. The faster the player moves, the faster it makes mana. A redstone signal stops it.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileTinkle}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileTinkle} of ExtraMeteorP 1.12.
 */
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
		if (getLevel().isClientSide() || isPowered()) {
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
				PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.PROJECT_DIVA_DESU), "code_triggered");
				time = 0;
				syncThrottled();
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
