package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
 * 叮当舞花。范围里的玩家移动时，它产生魔力，并让玩家多消耗一点饥饿值。玩家移动得越快，它产生魔力越快。
 * Tinkle. When a player in the range moves, it makes mana and the player uses a little more hunger. The faster the player moves, the faster it makes mana.
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code SubTileTinkleFlower}。每 20 tick，范围里的每个玩家按速度给计数加 0 到 8；计数到 10 时产生 30 魔力，计数取余数。速度用披风位置算（1.16 的 {@code chasingPos}）。1.16 不受红石控制。
 * <p>Source: {@code SubTileTinkleFlower} of ExtraMeteorP 1.16. Each 20 ticks, each player in the range adds 0 to 8 to a count, by the speed; when the count reaches 10, the flower makes 30 mana and keeps the remainder of the count. The speed comes from the cape position (the {@code chasingPos} of 1.16). In 1.16 redstone does not stop it.
 */
public class TinkleBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_TIME = "time";

	public static final int MAX_MANA = 1000;
	private static final int RANGE = 8;
	private static final int COOLDOWN = 20;
	private static final int PRODUCE_REQUIRE = 10;
	public static final int PRODUCE_MANA = 30;

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

		var players = getLevel().getEntitiesOfClass(Player.class, getCheckAABB()).stream()
				.filter(PlayerHelper::isTruePlayer)
				.toList();

		for (var player : players) {
			time += getTimeAdd(player);

			if (time >= PRODUCE_REQUIRE) {
				if (getMana() < getMaxMana()) {
					addMana(getProduceMana());
				}

				player.causeFoodExhaustion(0.02F);
				PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.PROJECT_DIVA_DESU), "code_triggered");
				time %= PRODUCE_REQUIRE;
				syncThrottled();
			}
		}
	}

	/**
	 * 一个玩家这一次给计数加多少：披风位置到玩家位置的距离乘以 10，有速度效果时再乘 1.2，限制在 0 到 8。
	 * How much one player adds to the count this time: the distance from the cape position to the player position times 10, times 1.2 more with the Speed effect, limited to 0 to 8.
	 */
	public static int getTimeAdd(Player player) {
		double vx = player.getX() - player.xCloak;
		double vy = player.getY() - player.yCloak;
		double vz = player.getZ() - player.zCloak;
		double vel = Math.sqrt(vx * vx + vy * vy + vz * vz);
		if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
			vel *= 1.2;
		}
		return Mth.clamp((int) (vel * 10.0), 0, 8);
	}

	public int getTime() {
		return time;
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

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_TIME, time);
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		time = cmp.getInt(TAG_TIME);
	}
}
