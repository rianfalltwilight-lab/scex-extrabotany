package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * 虚空力场。空之律者闪避攻击时，它出现在攻击者的位置，让里面的生物几乎动不了。
 * Void field. When the Herrscher of the Void dodges an attack, the field appears at the position of the attacker, and the creatures in it can almost not move.
 *
 * <p>力场存在 {@value #LIFE} tick。每 tick 它把范围内每个生物的速度乘以 {@value #SLOW_FACTOR}。
 * <p>The field stays for {@value #LIFE} ticks. On each tick it multiplies the velocity of each creature in its range by {@value #SLOW_FACTOR}.
 *
 * <p>减速在服务端和客户端都执行，这和原版相同。玩家的移动由自己的客户端计算，所以客户端这一边才是真正让玩家慢下来的地方。
 * <p>The slow effect runs on the server and on the client, which is the same as in the original. The client of a player calculates the movement of that player, thus the client side is the part that really slows the player.
 *
 * <p>数据的键名是 {@code Life}。旧存档使用相同的键名。
 * <p>The key of the data is {@code Life}. Old saves use the same key.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityVoid}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityVoid}.
 */
public class VoidFieldEntity extends Entity {
	private static final String TAG_LIFE = "Life";
	private static final int LIFE = 60;
	private static final double RANGE = 1.5;
	private static final double SLOW_FACTOR = 0.12;

	private int life = LIFE;

	public VoidFieldEntity(EntityType<? extends VoidFieldEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public VoidFieldEntity(Level level) {
		this(ExtraBotanyEntityType.VOID_FIELD, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	public void tick() {
		super.tick();
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(RANGE), Entity::isAlive)) {
			living.setDeltaMovement(living.getDeltaMovement().scale(SLOW_FACTOR));
		}
		if (!level().isClientSide() && --life <= 0) {
			discard();
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_LIFE, life);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		life = tag.getInt(TAG_LIFE);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}
}
