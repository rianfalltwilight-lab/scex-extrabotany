package io.github.lounode.extrabotany.common.entity.judah;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import vazkii.botania.client.fx.SparkleParticleData;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.OwnedEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;

/**
 * 樱花的誓约的剑。它从五角星的一个顶点沿直线飞向另一个顶点，每 tick 移动 0.75 格。它只用粒子显示。
 * A sword of the Oath of Sakura. It flies in a straight line from one point of the star to a different point, 0.75 blocks on each tick. Only particles show it.
 *
 * <p>飞行途中：碰到的生物受到"伤害值 × 0.2"的穿透魔法伤害和同样数值的穿透火焰伤害，并且着火 5 秒。
 * <p>During the flight: a mob that it touches takes piercing magic damage of "damage × 0.2" and piercing fire damage of the same value, and burns for 5 seconds.
 *
 * <p>到达终点时：整条路径上的生物受到"伤害值 × 0.6"的穿透魔法伤害、"伤害值 × 0.3"的穿透火焰伤害和"伤害值 × 0.3"的真实伤害。
 * <p>At the end point: the mobs on the full path take piercing magic damage of "damage × 0.6", piercing fire damage of "damage × 0.3" and true damage of "damage × 0.3".
 *
 * <p>剑不伤害玩家。
 * <p>The sword does not damage players.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityJudahSword}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityJudahSword}.
 *
 * <p>和原版的差异：原版直接修改全局的魔法伤害和熔岩伤害对象，让它们永久无视护甲。这会改变整个游戏里的毒、熔岩等伤害。这里使用模组自己的伤害类型。
 * <p>Difference from the original: the original changes the global magic and lava damage objects so that they ignore armor permanently. This changes poison, lava and other damage in the full game. This port uses the damage types of the mod.
 */
public class JudahSwordEntity extends OwnedEntity {
	private static final EntityDataAccessor<Vector3f> STEP = SynchedEntityData.defineId(JudahSwordEntity.class, EntityDataSerializers.VECTOR3);

	private static final double SPEED = 0.75;
	private static final double TOUCH_RANGE = 0.75;
	private static final double PATH_WIDTH = 1.8;
	private static final int MAX_LIFETIME_TICKS = 80;
	private static final int FIRE_SECONDS = 5;

	private Vec3 start = Vec3.ZERO;
	private Vec3 end = Vec3.ZERO;
	private float damage = 6F;

	public JudahSwordEntity(EntityType<? extends JudahSwordEntity> type, Level level) {
		super(type, level);
	}

	public JudahSwordEntity(Level level, Entity owner, Vec3 start, Vec3 end, float damage) {
		this(ExtraBotanyEntityType.JUDAH_SWORD, level);
		setOwner(owner);
		this.start = start;
		this.end = end;
		this.damage = damage;
		setPos(start);
		syncStep();
	}

	private void syncStep() {
		entityData.set(STEP, end.subtract(start).normalize().scale(SPEED).toVector3f());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(STEP, new Vector3f());
	}

	@Override
	public void tick() {
		Vector3f step = entityData.get(STEP);
		setPos(getX() + step.x, getY() + step.y, getZ() + step.z);
		if (level().isClientSide()) {
			level().addParticle(SparkleParticleData.sparkle(1.8F, 1F, 0.7F, 0.7F, 25), getX(), getY(), getZ(), 0.0, 0.0, 0.0);
		} else {
			hit(new AABB(position(), position()).inflate(TOUCH_RANGE), false);
			if (position().distanceToSqr(end) <= 2.0 || tickCount > MAX_LIFETIME_TICKS) {
				hit(new AABB(start, end).inflate(PATH_WIDTH), true);
				discard();
			}
		}
		super.tick();
	}

	/**
	 * @param finisher {@code true} 表示到达终点时的最后一击。它只命中路径这条线段附近的生物。 {@code true} for the last hit at the end point. It only hits the mobs near the line segment of the path.
	 */
	private void hit(AABB area, boolean finisher) {
		Entity owner = getOwner();
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living instanceof Player || !DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			if (finisher) {
				AABB wide = living.getBoundingBox().inflate(PATH_WIDTH);
				if (!wide.contains(start) && wide.clip(start, end).isEmpty()) {
					continue;
				}
			}
			DamageHandler.INSTANCE.dmg(living, owner, damage * (finisher ? 0.6F : 0.2F), DamageKind.MAGIC_PIERCING);
			DamageHandler.INSTANCE.fireAttack(living, owner, damage * (finisher ? 0.3F : 0.2F));
			if (finisher && owner instanceof LivingEntity livingOwner) {
				DamageHandler.INSTANCE.dealTrueDamage(livingOwner, living, damage * 0.3F);
			}
			living.igniteForSeconds(FIRE_SECONDS);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putDouble("StartX", start.x);
		tag.putDouble("StartY", start.y);
		tag.putDouble("StartZ", start.z);
		tag.putDouble("EndX", end.x);
		tag.putDouble("EndY", end.y);
		tag.putDouble("EndZ", end.z);
		tag.putFloat("Damage", damage);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		start = new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
		end = new Vec3(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ"));
		damage = tag.getFloat("Damage");
		syncStep();
	}
}
