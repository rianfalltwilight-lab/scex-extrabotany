package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.brew.Brew;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import javax.annotation.Nullable;

import java.util.List;

/**
 * 飞行中的圣水手雷。效果的规则见 {@link #applySplash}。
 * A Holy Water Grenade in flight. See {@link #applySplash} for the rule of the effects.
 */
public class HolyWaterGrenadeEntity extends ThrownPotion {

	public static final double SPLASH_RANGE = 5.0D;
	public static final float SPLASH_DAMAGE = 10F;
	public static final float DIRECT_HIT_DAMAGE = 5F;
	public static final float DURATION_FACTOR = 0.6F;

	public HolyWaterGrenadeEntity(EntityType<? extends HolyWaterGrenadeEntity> entityType, Level level) {
		super(entityType, level);
	}

	public HolyWaterGrenadeEntity(Level level, LivingEntity shooter) {
		this(level, shooter.getX(), shooter.getEyeY() - (double) 0.1F, shooter.getZ());
		this.setOwner(shooter);
	}

	public HolyWaterGrenadeEntity(Level level, double x, double y, double z) {
		this(ExtraBotanyEntityType.HOLY_WATER_GRENADE, level);
		this.setPos(x, y, z);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.holyWaterGrenade;
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		BlockState blockstate = this.level().getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level(), blockstate, result, this);
	}

	@Override
	protected void onHit(HitResult result) {
		HitResult.Type hitresult$type = result.getType();
		if (hitresult$type == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult) result);
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, result.getLocation(), GameEvent.Context.of(this, (BlockState) null));
		} else if (hitresult$type == HitResult.Type.BLOCK) {
			BlockHitResult blockhitresult = (BlockHitResult) result;
			this.onHitBlock(blockhitresult);
			BlockPos blockpos = blockhitresult.getBlockPos();
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
		}

		if (!this.level().isClientSide) {
			Brew brew = getBrew();

			List<MobEffectInstance> list = BrewUtil.getPotionEffects(brew);
			this.applySplash(list, result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) result).getEntity() : null);

			int i = BrewUtil.hasInstantEffects(brew) ? 2007 : 2002;
			this.level().levelEvent(i, this.blockPosition(), BrewUtil.getColor(brew));
			this.discard();
		}
	}

	/**
	 * 1.16 {@code EntitySplashGrenade.onImpact}：中心 5 格的方框里，每个非玩家生物受 10 点魔法伤害；增益效果给玩家，负面效果给非玩家；效果时长 × 0.6，不按距离衰减。
	 * 1.16 {@code EntitySplashGrenade.onImpact}: in the box of 5 blocks around the center, each creature that is not a player takes 10 magic damage; beneficial effects go to players, harmful effects to the others; the effect duration is × 0.6, with no loss by distance.
	 */
	public void applySplash(List<MobEffectInstance> effectInstances, @Nullable Entity target) {
		AABB bounds = new AABB(getX() - SPLASH_RANGE, getY() - SPLASH_RANGE, getZ() - SPLASH_RANGE,
				getX() + SPLASH_RANGE, getY() + SPLASH_RANGE, getZ() + SPLASH_RANGE);
		Entity source = this.getEffectSource();
		for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, bounds)) {
			boolean isPlayer = victim instanceof Player;
			if (!isPlayer) {
				victim.hurt(damageSources().magic(), SPLASH_DAMAGE);
			}
			for (MobEffectInstance tmpInstance : effectInstances) {
				var mobeffect = tmpInstance.getEffect();
				if (mobeffect.value().isBeneficial() != isPlayer) {
					continue;
				}
				if (mobeffect.value().isInstantenous()) {
					mobeffect.value().applyInstantenousEffect(this, this.getOwner(), victim, tmpInstance.getAmplifier(), 1.0D);
					continue;
				}
				victim.addEffect(new MobEffectInstance(mobeffect, (int) (tmpInstance.getDuration() * DURATION_FACTOR),
						tmpInstance.getAmplifier(), true, true), source);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		// 1.16：直接命中再加 5 点投掷伤害。 / 1.16: a direct hit adds 5 thrown damage.
		result.getEntity().hurt(damageSources().thrown(this, getOwner()), DIRECT_HIT_DAMAGE);
	}

	public Brew getBrew() {
		return BrewUtil.getBrew(getItem());
	}
}
