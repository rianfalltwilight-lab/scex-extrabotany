package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.helper.VecHelper;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.UUID;

/**
 * 审判之剑。它在游戏里的名字是"天堂裁决"，和召唤它的攻击同名。这次攻击在场地上空召唤八把这样的剑，每把剑对应一名玩家。
 * Sword of judgment. Its name in the game is "Sword Domain". The "Divine Judge" attack summons eight of these swords above the arena, and each sword has one target player.
 *
 * <p>剑出现 {@value #PULL_START} tick 之后，它把自己的目标玩家拉向场地中心，并给玩家霉运效果。每 {@value #MAGIC_INTERVAL} tick 它造成 1 点魔法伤害。第 {@value #LIFE} tick 它造成 1 点真实伤害，然后消失。
 * {@value #PULL_START} ticks after the sword appears, it pulls its target player to the center of the arena and gives the player the Bad Luck effect. For each {@value #MAGIC_INTERVAL} ticks it gives 1 magic damage. At tick {@value #LIFE} it gives 1 true damage and then goes away.
 *
 * <p>附近没有盖亚守护者或者空之律者时，剑会消失。
 * The sword goes away when there is no Guardian of Gaia and no Herrscher of the Void near it.
 *
 * <p>数据的键名是 {@code Target}、{@code Source}、{@code FloorY} 和 {@code Type}。旧存档使用相同的键名。
 * <p>The keys of the data are {@code Target}, {@code Source}, {@code FloorY} and {@code Type}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySwordDomain}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntitySwordDomain}.
 *
 * <p>性能：剑每 {@value #HOST_CHECK_INTERVAL} tick 查找一次 Boss。原版每 tick 查找两次。
 * <p>Performance: the sword looks for the boss one time for each {@value #HOST_CHECK_INTERVAL} ticks. The original searches two times on each tick.
 */
public class SwordDomainEntity extends Entity {
	/** 剑的外观种类的数量。 / The number of appearance types of the sword. */
	public static final int TYPES = 10;

	private static final String TAG_TARGET = "Target";
	private static final String TAG_SOURCE = "Source";
	private static final String TAG_FLOOR_Y = "FloorY";
	private static final String TAG_TYPE = "Type";

	private static final int PULL_START = 70;
	private static final int MAGIC_INTERVAL = 50;
	private static final int LIFE = 200;
	private static final int HOST_CHECK_INTERVAL = 20;
	private static final double HOST_RANGE = 15;
	private static final double PULL_DISTANCE = 4;
	private static final double TELEPORT_DISTANCE = 20;
	private static final double SINK_SPEED = 0.01;

	private static final EntityDataAccessor<Integer> SWORD_TYPE = SynchedEntityData.defineId(SwordDomainEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<BlockPos> SOURCE = SynchedEntityData.defineId(SwordDomainEntity.class, EntityDataSerializers.BLOCK_POS);

	@Nullable
	private UUID target;
	private int floorY;

	public SwordDomainEntity(EntityType<? extends SwordDomainEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	/**
	 * @param target 这把剑对应的玩家 / the player that this sword has as its target
	 * @param source 场地的中心 / the center of the arena
	 * @param type   剑的外观种类 / the appearance type of the sword
	 * @param floorY 剑下沉的最低高度 / the lowest height that the sword goes down to
	 */
	public SwordDomainEntity(Level level, UUID target, BlockPos source, int type, int floorY) {
		this(ExtraBotanyEntityType.SWORD_DOMAIN, level);
		this.target = target;
		this.floorY = floorY;
		entityData.set(SOURCE, source.immutable());
		entityData.set(SWORD_TYPE, Math.floorMod(type, TYPES));
	}

	public int getSwordType() {
		return entityData.get(SWORD_TYPE);
	}

	public BlockPos getSource() {
		return entityData.get(SOURCE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SWORD_TYPE, 0);
		builder.define(SOURCE, BlockPos.ZERO);
	}

	@Override
	public void tick() {
		super.tick();
		setPos(getX(), Math.max(getY() - SINK_SPEED, floorY), getZ());
		if (level().isClientSide()) {
			showParticles();
			return;
		}
		if (tickCount > LIFE || target == null || tickCount % HOST_CHECK_INTERVAL == 0 && !isBossNear()) {
			discard();
			return;
		}
		if (tickCount > PULL_START && ((ServerLevel) level()).getEntity(target) instanceof Player player && player.isAlive() && !player.isSpectator()) {
			judge(player);
		}
	}

	private boolean isBossNear() {
		return !level().getEntitiesOfClass(Gaia.class, new AABB(blockPosition()).inflate(HOST_RANGE), Entity::isAlive).isEmpty();
	}

	private void judge(Player player) {
		BlockPos source = getSource();
		Vec3 center = Vec3.atCenterOf(source);
		if (center.distanceTo(player.position()) >= PULL_DISTANCE) {
			Vec3 motion = center.subtract(VecHelper.fromEntityCenter(player)).normalize();
			player.setDeltaMovement(motion.x, 0.2, motion.z);
			player.hurtMarked = true;
			player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
		}
		double dx = player.getX() - source.getX();
		double dz = player.getZ() - source.getZ();
		if (dx * dx + dz * dz > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
			player.teleportTo(source.getX() + 0.5, source.getY(), source.getZ() + 0.5);
		}
		if (tickCount % MAGIC_INTERVAL == 0) {
			player.hurt(damageSources().magic(), 1F);
		}
		if (tickCount == LIFE) {
			DamageHandler.INSTANCE.dealTrueDamage(this, player, 1F);
		}
	}

	private void showParticles() {
		float spread = 0.45F;
		if (tickCount > PULL_START) {
			// 场地中心上方的金色光点表示玩家正在被拉过去。
			// The golden wisps above the center of the arena show that players are pulled there.
			BlockPos source = getSource();
			for (int i = 0; i < 2; i++) {
				level().addParticle(WispParticleData.wisp(0.25F, 1F, 0.9F, 0F), source.getX(), source.getY() + 3, source.getZ(),
						(random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread);
			}
		}
		if (tickCount >= LIFE) {
			for (int i = 0; i < 5; i++) {
				level().addParticle(WispParticleData.wisp(0.25F, random.nextFloat(), random.nextFloat(), random.nextFloat()), getX(), getY(), getZ(),
						(random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (target != null) {
			tag.putUUID(TAG_TARGET, target);
		}
		tag.putLong(TAG_SOURCE, getSource().asLong());
		tag.putInt(TAG_FLOOR_Y, floorY);
		tag.putInt(TAG_TYPE, getSwordType());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		target = tag.hasUUID(TAG_TARGET) ? tag.getUUID(TAG_TARGET) : null;
		entityData.set(SOURCE, BlockPos.of(tag.getLong(TAG_SOURCE)));
		floorY = tag.getInt(TAG_FLOOR_Y);
		entityData.set(SWORD_TYPE, Math.floorMod(tag.getInt(TAG_TYPE), TYPES));
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
