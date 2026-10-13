package io.github.lounode.extrabotany.common.entity.subspace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.BotaniaDamageTypes;
import vazkii.botania.common.entity.ManaBurstEntity;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 亚空间裂隙发射的金色魔力脉冲。
 * The golden mana bursts that a Subspace rift shoots.
 *
 * <p>玩家的脉冲：命中时造成 16 点遗物伤害和 7 点真实伤害。对 Boss 是 22 点和 9 点。
 * <p>The burst of a player: a hit does 16 relic damage and 7 true damage. Against a boss the values are 22 and 9.
 *
 * <p>律者的脉冲：命中时造成 10 点遗物伤害和 4 点真实伤害。它不伤害律者自己。
 * <p>The burst of the Herrscher: a hit does 10 relic damage and 4 true damage. It does not damage the Herrscher.
 *
 * <p>两种脉冲每 10 tick 还对路径上的生物造成少量真实伤害。一道脉冲只能命中一次。
 * <p>The two bursts also do a small quantity of true damage to the mobs on the path on each 10th tick. One burst can only hit one time.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemExcaliber#getBurst} 和 {@code ItemExcaliber#updateBurst} 里处理这两种颜色的分支。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemExcaliber#getBurst}, and the branch of {@code ItemExcaliber#updateBurst} for these two colors.
 */
public final class SubspaceBursts {
	public static final int PLAYER_COLOR = 0xFFAF00;
	public static final int BOSS_COLOR = 0xFFD700;

	private static final int MANA = 160;
	private static final int MIN_MANA_LOSS = 40;
	private static final float MANA_LOSS_PER_TICK = 4F;
	private static final float PLAYER_SPEED_FACTOR = 9F;
	private static final float BOSS_PITCH_OFFSET = 15F;
	private static final double HIT_RANGE = 1.3;
	private static final int TICK_DAMAGE_INTERVAL = 10;
	private static final int HIT_INVULNERABLE_TICKS = 10;

	private SubspaceBursts() {}

	/**
	 * @param owner        发射脉冲的生物。 The mob that shoots the burst.
	 * @param aimingPlayer 决定玩家脉冲朝向的玩家。 The player that gives the direction of a player burst.
	 * @param fromBoss     {@code true} 表示律者的脉冲。 {@code true} for a burst of the Herrscher.
	 */
	public static ManaBurstEntity create(LivingEntity owner, Player aimingPlayer, Vec3 position, boolean fromBoss) {
		ManaBurstEntity burst = new ManaBurstEntity(aimingPlayer);
		burst.setOwner(owner);
		burst.setPos(position);
		burst.setColor(fromBoss ? BOSS_COLOR : PLAYER_COLOR);
		burst.setMana(MANA);
		burst.setStartingMana(MANA);
		burst.setMinManaLoss(MIN_MANA_LOSS);
		burst.setManaLossPerTick(MANA_LOSS_PER_TICK);
		burst.setGravity(0F);
		burst.setDeltaMovement(fromBoss
				? Vec3.directionFromRotation(owner.getXRot() + BOSS_PITCH_OFFSET, owner.getYRot())
				: burst.getDeltaMovement().scale(PLAYER_SPEED_FACTOR));
		// 亚空之矛物品是这道脉冲的"透镜"，所以脉冲每 tick 回调 update。
		// The Spear of Subspace item is the "lens" of this burst, so the burst calls update on each tick.
		burst.setSourceLens(new ItemStack(ExtraBotanyItems.spearOfSubspace));
		return burst;
	}

	/**
	 * 脉冲每 tick 的伤害逻辑。由亚空之矛物品的透镜回调调用。
	 * The damage logic of a burst on each tick. The lens callback of the Spear of Subspace item calls this.
	 */
	public static void update(ManaBurst burst) {
		Entity entity = burst.entity();
		if (entity.level().isClientSide()) {
			return;
		}
		if (!(burst.entity() instanceof ManaBurstEntity burstEntity) || !(burstEntity.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
			entity.discard();
			return;
		}
		boolean fromBoss = burst.getColor() == BOSS_COLOR;
		AABB area = new AABB(entity.position(), new Vec3(entity.xOld, entity.yOld, entity.zOld)).inflate(HIT_RANGE);
		for (LivingEntity living : entity.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (fromBoss && living instanceof VoidHerrscher) {
				continue;
			}
			if (!DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			if (entity.tickCount % TICK_DAMAGE_INTERVAL == 0) {
				DamageHandler.INSTANCE.dealTrueDamage(owner, living, fromBoss ? 1.4F : 0.22F);
			}
			if (living.invulnerableTime != 0 || burst.isFake()) {
				continue;
			}
			boolean boss = living.getType().is(Tags.EntityTypes.BOSSES) || living instanceof Gaia;
			living.hurt(BotaniaDamageTypes.Sources.relicDamage(entity.level().registryAccess()), fromBoss ? 10F : boss ? 22F : 16F);
			DamageHandler.INSTANCE.dealTrueDamage(owner, living, fromBoss ? 4F : boss ? 9F : 7F);
			living.invulnerableTime = HIT_INVULNERABLE_TICKS;
			burst.setFake(true);
		}
	}
}
