package io.github.lounode.extrabotany.common.entity.judah;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

import vazkii.botania.client.fx.SparkleParticleData;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.brew.effect.DivineJusticeMobEffect;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.OwnedEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;

/**
 * 犹大的誓约的光矛。装饰用的光矛每 tick 上升 0.75 格。真正的光矛每 tick 下落 0.95 格，并且只命中一个生物。
 * A spear of the Judah Oath. A spear for decoration goes up 0.75 blocks on each tick. A real spear comes down 0.95 blocks on each tick and only hits one mob.
 *
 * <p>命中：造成"伤害值 × 1.6"的雷电类型伤害和"伤害值 × 0.15"的真实伤害。不是 Boss 的目标还得到一级"神圣制裁"。
 * <p>A hit: does damage of "damage × 1.6" of the lightning type and true damage of "damage × 0.15". A target that is not a boss also gets one level of Divine Justice.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityJudahSpear}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityJudahSpear}.
 *
 * <p>和原版的差异：原版把投出者保存在静态字段里，全部光矛共用一个投出者。原版的雷电伤害没有攻击者。这里每支光矛保存自己的主人，并且把主人记为攻击者。
 * <p>Differences from the original: the original keeps the thrower in a static field, so all spears share one thrower. The lightning damage of the original has no attacker. In this port each spear keeps its own owner, and the owner is recorded as the attacker.
 */
public class JudahSpearEntity extends OwnedEntity {
	private static final String TAG_VARIANT = "Variant";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_FAKE = "Fake";
	private static final String TAG_HIT = "Hit";

	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.BOOLEAN);

	private static final float[][] COLORS = { { 0.85F, 0.6F, 0.02F }, { 0.01F, 0.6F, 0.75F }, { 0F, 0F, 0F } };
	private static final double RISE_SPEED = 0.75;
	private static final double FALL_SPEED = 0.95;
	private static final int LIFETIME_TICKS = 100;
	private static final float ATTACK_DAMAGE_FACTOR = 1.6F;
	private static final float TRUE_DAMAGE_FACTOR = 0.15F;

	private float damage = 7F;
	private boolean hit;

	public JudahSpearEntity(EntityType<? extends JudahSpearEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * @param fake {@code true} 表示装饰用的光矛，它不造成伤害。 {@code true} for a spear for decoration, which does no damage.
	 */
	public JudahSpearEntity(Level level, Entity owner, Variant variant, float damage, boolean fake) {
		this(ExtraBotanyEntityType.JUDAH_SPEAR, level);
		setOwner(owner);
		this.damage = damage;
		entityData.set(VARIANT, variant.ordinal());
		entityData.set(FAKE, fake);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(VARIANT, 0);
		builder.define(FAKE, false);
	}

	public Variant getVariant() {
		return Variant.byId(entityData.get(VARIANT));
	}

	public boolean isFake() {
		return entityData.get(FAKE);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			float[] color = COLORS[getVariant().ordinal()];
			level().addParticle(SparkleParticleData.sparkle(1F, color[0], color[1], color[2], 1), getX(), getY(), getZ(), 0.0, 0.0, 0.0);
		}
		// 客户端和服务端都移动光矛，这样画面是连续的。
		// The client and the server both move the spear, so that the picture is smooth.
		setPos(getX(), getY() + (isFake() ? RISE_SPEED : -FALL_SPEED), getZ());
		if (level().isClientSide()) {
			return;
		}
		if (!isFake() && !hit) {
			hitFirstTarget();
		}
		if (tickCount > LIFETIME_TICKS) {
			discard();
		}
	}

	private void hitFirstTarget() {
		Entity owner = getOwner();
		AABB area = new AABB(getX() - 1.3, getY() - 6.0, getZ() - 1.3, xOld + 1.3, yOld + 5.0, zOld + 1.3);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == owner || !DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			living.hurt(ExtraBotanyDamageTypes.Sources.source(level().registryAccess(), DamageTypes.LIGHTNING_BOLT, this, owner),
					damage * ATTACK_DAMAGE_FACTOR);
			if (!living.getType().is(Tags.EntityTypes.BOSSES)) {
				DivineJusticeMobEffect.stack(living);
			}
			DamageHandler.INSTANCE.dealTrueDamage(owner, living, damage * TRUE_DAMAGE_FACTOR);
			hit = true;
			return;
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_VARIANT, entityData.get(VARIANT));
		tag.putFloat(TAG_DAMAGE, damage);
		tag.putBoolean(TAG_FAKE, isFake());
		tag.putBoolean(TAG_HIT, hit);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(VARIANT, Variant.byId(tag.getInt(TAG_VARIANT)).ordinal());
		damage = tag.getFloat(TAG_DAMAGE);
		entityData.set(FAKE, tag.getBoolean(TAG_FAKE));
		hit = tag.getBoolean(TAG_HIT);
	}
}
