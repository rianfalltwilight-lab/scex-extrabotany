package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.handler.BotaniaSounds;

import java.util.List;
import java.util.UUID;

/**
 * 魔法地雷。它在倒计时 {@value #EXPLODE_COUNTDOWN} tick 之后爆炸，对范围里的玩家造成魔法伤害，并给失明和凋零效果。它不伤害玩家以外的生物，也不伤害创造模式和旁观模式的玩家。
 * Magic landmine. It explodes after a countdown of {@value #EXPLODE_COUNTDOWN} ticks, gives magic damage to the players in its area, and gives the Blindness and Wither effects. It does not hurt creatures that are not players, and it does not hurt players in creative or spectator mode.
 *
 * <p>来源：重制版（Lounode）。它对应植物魔法的魔法地雷。
 * <p>Source: Reburn (Lounode). It corresponds to the magic landmine of Botania.
 */
public class MagicLandMineEntity extends Entity {

	public static final int EXPLODE_COUNTDOWN = 55;
	public static final float EXPLODE_DAMAGE = 10;

	private static final String TAG_OWNER = "Owner";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_EXPLODE_COUNTDOWN = "ExplodeCountDown";

	private static final EntityDataAccessor<Integer> COUNTDOWN = SynchedEntityData.defineId(MagicLandMineEntity.class, EntityDataSerializers.INT);

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private float damage;

	public MagicLandMineEntity(EntityType<? extends MagicLandMineEntity> type, Level level) {
		super(type, level);
		this.setDamage(EXPLODE_DAMAGE);
		this.setExplodeCountDown(EXPLODE_COUNTDOWN);
	}

	@Override
	public void tick() {
		this.setDeltaMovement(Vec3.ZERO);
		super.tick();

		particle();

		int countDown = getExplodeCountDown();
		if (countDown > 0) {
			setExplodeCountDown(countDown - 1);
			return;
		}

		explode();
	}

	public void explode() {
		explodeParticle();

		if (!level().isClientSide()) {
			level().playSound(null, getX(), getY(), getZ(), BotaniaSounds.GAIA_TRAP, SoundSource.NEUTRAL, 1F, 1F);

			List<Player> players = getVictimPlayers();
			for (Player player : players) {
				if (player.isSpectator() || player.isCreative()) {
					continue;
				}

				player.hurt(this.damageSources().indirectMagic(this, getOwner()), getDamage());
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0));
				player.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
			}

			discard();
		}
	}

	protected void explodeParticle() {
		if (!level().isClientSide()) {
			return;
		}
		float m = 0.35F;
		float r = 0.2F;
		float g = 0.4F;
		float b = 0.2F;

		for (int i = 0; i < 25; i++) {
			WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
			level().addParticle(data, getX(), getY() + 1, getZ(), (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m);
		}
	}

	/**
	 * 爆炸之前，地雷范围里缓慢上升的粒子。只在客户端运行。
	 * The particles that go up slowly in the area of the landmine before the explosion. This only runs on the client.
	 */
	protected void particle() {
		if (!level().isClientSide()) {
			return;
		}
		float range = getParticleRange();
		WispParticleData data = getParticleData();
		for (int i = 0; i < 6; i++) {
			level().addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, 0.015F, 0);
		}
	}

	/**
	 * 粒子散布范围的一半宽度。它和地雷的伤害范围相同。
	 * The half width of the area of the particles. It is the same as the damage area of the landmine.
	 */
	protected float getParticleRange() {
		return getBbWidth() / 2;
	}

	/**
	 * 粒子的大小、颜色和不透明度。
	 * The size, the color and the opacity of the particles.
	 */
	protected WispParticleData getParticleData() {
		return WispParticleData.wisp(0.4F, 0.2F, 0F, 0.2F, 1F);
	}

	public List<? extends LivingEntity> getVictims(Class<? extends LivingEntity> entityClass) {
		return level().getEntitiesOfClass(entityClass, getBoundingBox());
	}

	@SuppressWarnings("unchecked")
	public List<Player> getVictimPlayers() {
		return (List<Player>) getVictims(Player.class);
	}

	public float getDamage() {
		return damage;
	}

	public void setDamage(float damage) {
		this.damage = damage >= 0 ? damage : 0;
	}

	public void setOwner(@Nullable Entity owner) {
		if (owner != null) {
			this.ownerUUID = owner.getUUID();
			this.cachedOwner = owner;
		}

	}

	@Nullable
	public Entity getOwner() {
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		} else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
			this.cachedOwner = ((ServerLevel) this.level()).getEntity(this.ownerUUID);
			return this.cachedOwner;
		} else {
			return null;
		}
	}

	public int getExplodeCountDown() {
		return this.entityData.get(COUNTDOWN);
	}

	public void setExplodeCountDown(int explodeCountDown) {
		this.entityData.set(COUNTDOWN, explodeCountDown);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(COUNTDOWN, EXPLODE_COUNTDOWN);
	}

	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag cmp) {
		if (cmp.hasUUID(TAG_OWNER)) {
			this.ownerUUID = cmp.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		setDamage(cmp.getFloat(TAG_DAMAGE));
		setExplodeCountDown(cmp.getInt(TAG_EXPLODE_COUNTDOWN));
	}

	@Override
	protected void addAdditionalSaveData(@NotNull CompoundTag cmp) {
		if (this.ownerUUID != null) {
			cmp.putUUID(TAG_OWNER, this.ownerUUID);
		}
		cmp.putFloat(TAG_DAMAGE, getDamage());
		cmp.putInt(TAG_EXPLODE_COUNTDOWN, getExplodeCountDown());
	}
}
