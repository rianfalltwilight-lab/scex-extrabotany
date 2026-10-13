package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 本我的地雷。本我从第 1 阶段开始，用很多这样的地雷在场地上排出阵型。
 * The landmine of the Ego. From stage 1, the Ego puts many of these landmines on the arena in formations.
 *
 * <p>地雷出现 {@value #FUSE_TICKS} tick 之后爆炸，伤害它正上方和正下方的玩家：5 点失去生命伤害、10 点魔法伤害、失明和凋零。
 * <p>The landmine explodes {@value #FUSE_TICKS} ticks after it appears, and hurts the players directly above and below it: 5 life-losing damage, 10 magic damage, Blindness and Wither.
 *
 * <p>地雷有三种：
 * <p>There are three types of landmine:
 * <ul>
 * <li>蓝色：没有额外效果。
 * Blue: no additional effect.</li>
 * <li>绿色：让玩家丢出主手的物品。
 * Green: makes the player drop the item in the main hand.</li>
 * <li>红色：再造成 10 点失去生命伤害。
 * Red: gives 10 more life-losing damage.</li>
 * </ul>
 *
 * <p>失去生命伤害直接扣除生命值，但是最少留下 1 点。所以重叠的地雷很危险，但是只靠它不会死。
 * <p>Life-losing damage decreases the health directly, but leaves a minimum of 1. Thus landmines that overlap are dangerous, but they cannot kill by themselves.
 *
 * <p>数据的键名是 {@code Owner} 和 {@code Type}。
 * <p>The keys of the data are {@code Owner} and {@code Type}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityEGOLandmine}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityEGOLandmine}.
 *
 * <p>和原版的差异：配置关闭缴械时，绿色地雷不让玩家丢出物品。
 * <p>Difference from the original: when the configuration disables the disarm, the green landmine does not make the player drop the item.
 */
public class EgoLandmineEntity extends OwnedEntity {
	public static final int TYPE_BLUE = 0;
	public static final int TYPE_GREEN = 1;
	public static final int TYPE_RED = 2;

	private static final String TAG_TYPE = "Type";
	private static final int FUSE_TICKS = 50;
	/** 爆炸在垂直方向上的范围。 / The vertical range of the explosion. */
	private static final double VERTICAL_RANGE = 12;

	private static final EntityDataAccessor<Integer> LANDMINE_TYPE = SynchedEntityData.defineId(EgoLandmineEntity.class, EntityDataSerializers.INT);

	public EgoLandmineEntity(EntityType<? extends EgoLandmineEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public EgoLandmineEntity(Level level, @Nullable Entity owner, int landmineType, Vec3 position) {
		this(ExtraBotanyEntityType.EGO_LANDMINE, level);
		setOwner(owner);
		entityData.set(LANDMINE_TYPE, Mth.clamp(landmineType, TYPE_BLUE, TYPE_RED));
		setPos(position);
	}

	public int getLandmineType() {
		return entityData.get(LANDMINE_TYPE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(LANDMINE_TYPE, TYPE_BLUE);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			showParticles();
			return;
		}
		if (tickCount >= FUSE_TICKS) {
			explode();
		}
	}

	private void explode() {
		level().playSound(null, getX(), getY(), getZ(), BotaniaSounds.GAIA_TRAP, SoundSource.NEUTRAL, 0.3F, 1F);
		Entity owner = getOwner();
		int type = getLandmineType();
		for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0, VERTICAL_RANGE, 0))) {
			if (player.isSpectator() || player.isCreative()) {
				continue;
			}
			DamageHandler.INSTANCE.dmg(player, owner, 5F, DamageHandler.DamageKind.LIFE_LOSING);
			player.hurt(damageSources().indirectMagic(this, owner), 10F);
			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0));
			MobEffectInstance wither = new MobEffectInstance(MobEffects.WITHER, 120, 2);
			// 和原版相同：这个凋零效果不能用牛奶解除。
			// The same as in the original: milk cannot remove this Wither effect.
			wither.getCures().clear();
			player.addEffect(wither);
			if (type == TYPE_GREEN && !ExtraBotanyConfig.common().disableGaiaDisArm()) {
				ItemStack held = player.getMainHandItem();
				if (!held.isEmpty()) {
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
					player.drop(held, false, true);
				}
			} else if (type == TYPE_RED) {
				DamageHandler.INSTANCE.dmg(player, owner, 10F, DamageHandler.DamageKind.LIFE_LOSING);
			}
		}
		discard();
	}

	private void showParticles() {
		int type = getLandmineType();
		float red = type == TYPE_RED ? 1F : 0F;
		float green = type == TYPE_GREEN ? 1F : 0F;
		float blue = type == TYPE_BLUE ? 1F : 0F;
		float range = getBbWidth() / 2;
		if (tickCount % 2 == 0) {
			for (int i = 0; i < 2; i++) {
				level().addParticle(WispParticleData.wisp(0.4F, red, green, blue, 1F),
						getX() - range + random.nextFloat() * range * 2, getY(), getZ() - range + random.nextFloat() * range * 2, 0, 0.015F, 0);
			}
		}
		if (tickCount >= FUSE_TICKS) {
			float spread = 0.35F;
			for (int i = 0; i < 4; i++) {
				level().addParticle(WispParticleData.wisp(0.5F, red, green, blue), getX(), getY() + 1, getZ(),
						(random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread, (random.nextFloat() - 0.5F) * spread);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_TYPE, getLandmineType());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(LANDMINE_TYPE, Mth.clamp(tag.getInt(TAG_TYPE), TYPE_BLUE, TYPE_RED));
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
