package io.github.lounode.extrabotany.common.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.network.clientbound.ThundercallerEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 亚空之矛（实体）。为了和同名的物品区分，注释里把它叫做"矛枪"。空之律者把它插在场地上。
 * Subspace Lance (entity). The Chinese comments call it "矛枪", to make it different from the item with the same Chinese name. The Herrscher of the Void puts it into the arena.
 *
 * <p>矛枪从空中落下，落地之后立在原地，直到寿命结束或者主人死亡。
 * <p>The lance falls from the air and then stands in its position until its life ends or its owner dies.
 *
 * <p>矛枪有两种攻击：
 * <p>The lance has two attacks:
 * <ul>
 * <li>放电：每 {@value #DISCHARGE_INTERVAL} tick，矛枪旁边每个生命值高于 {@value #DISCHARGE_MIN_HEALTH} 的生物引发一道闪电链。闪电链从这个生物出发，在 {@value #CHAIN_RANGE} 格内的生物之间跳跃，最多 {@value #CHAIN_HOPS} 次（雷暴天气 {@value #CHAIN_HOPS_THUNDER} 次）。被跳到的生物受到真实伤害并得到虚弱效果。引发闪电链的生物自己只在闪电链跳回来时才受伤。
 * Discharge: for each {@value #DISCHARGE_INTERVAL} ticks, each creature adjacent to the lance that has more than {@value #DISCHARGE_MIN_HEALTH} health starts a lightning chain. The chain starts at this creature and jumps between the creatures within {@value #CHAIN_RANGE} blocks, a maximum of {@value #CHAIN_HOPS} times ({@value #CHAIN_HOPS_THUNDER} times in a thunderstorm). A creature that the chain jumps to gets true damage and the Weakness effect. The creature that started the chain is only hurt when the chain jumps back to it.</li>
 * <li>电网：{@value #WEB_RANGE} 格内的两把矛枪之间连着一条电线。每 {@value #WEB_INTERVAL} tick，站在电线上的玩家受到伤害并得到霉运效果。
 * Web: there is an electric line between two lances that are within {@value #WEB_RANGE} blocks of each other. For each {@value #WEB_INTERVAL} ticks, a player on the line gets damage and the Bad Luck effect.</li>
 * </ul>
 *
 * <p>数据的键名是 {@code Owner}、{@code Life}（剩下的寿命）和 {@code Damage}。旧存档使用相同的键名。
 * <p>The keys of the data are {@code Owner}, {@code Life} (the remaining life) and {@code Damage}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySubspaceLance}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntitySubspaceLance}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>每一对矛枪的电网只计算一次，造成 0.8 点真实伤害。原版里两把矛枪各算一次，每次 0.4 点，总量相同。
 * The web of each pair of lances is calculated one time, with 0.8 true damage. In the original each of the two lances calculates it, with 0.4 true damage each time, which is the same total.</li>
 * <li>电网的普通伤害是带主人的魔法伤害。原版先后造成一次"通用"伤害和一次"闪电"伤害，但是第二次总是被无敌帧挡掉。
 * The usual damage of the web is magic damage with the owner as the attacker. The original gives one "generic" hit and then one "lightning" hit, but the invulnerability time always stops the second hit.</li>
 * <li>原版保存了伤害值，但是从来不使用它。闪电链的伤害固定从 {@value #CHAIN_DAMAGE} 开始。这里保留这个行为，也保留存档里的键。
 * The original saves a damage value but never uses it. The damage of the lightning chain always starts at {@value #CHAIN_DAMAGE}. This port keeps that behavior, and keeps the key in the save.</li>
 * </ul>
 *
 * <p>性能：电网每 {@value #WEB_INTERVAL} tick 才在服务端查找一次别的矛枪。原版每 tick 都查找，并且每 tick 对每一对矛枪查找一次玩家。
 * <p>Performance: the server only looks for other lances one time for each {@value #WEB_INTERVAL} ticks. The original searches on each tick, and searches for players one time for each pair of lances on each tick.
 */
public class SubspaceLanceEntity extends OwnedEntity {
	private static final String TAG_LIFE = "Life";
	private static final String TAG_DAMAGE = "Damage";

	private static final double FALL_SPEED = 0.15;
	/** 矛枪下方这个距离内有方块时，矛枪停止下落。 / The lance stops its fall when there is a block within this distance below it. */
	private static final double STAND_HEIGHT = 1.9;

	private static final int DISCHARGE_INTERVAL = 35;
	private static final float DISCHARGE_MIN_HEALTH = 20F;
	private static final double CHAIN_RANGE = 8;
	private static final int CHAIN_HOPS = 4;
	private static final int CHAIN_HOPS_THUNDER = 10;
	private static final int CHAIN_DAMAGE = 4;
	private static final float CHAIN_MIN_HEALTH = 10F;

	private static final double WEB_RANGE = 15;
	private static final int WEB_INTERVAL = 5;
	private static final float WEB_MIN_HEALTH = 8F;
	private static final float WEB_DAMAGE = 1.2F;
	private static final float WEB_TRUE_DAMAGE = 0.8F;
	/** 电线离矛枪位置的高度。 / The height of the electric line above the position of the lance. */
	private static final double WEB_HEIGHT = 1.5;

	private int life = 1200;
	private float damage = CHAIN_DAMAGE;
	private boolean standing;

	public SubspaceLanceEntity(EntityType<? extends SubspaceLanceEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public SubspaceLanceEntity(Level level, LivingEntity owner, float damage, int life) {
		this(ExtraBotanyEntityType.SUBSPACE_LANCE, level);
		setOwner(owner);
		this.damage = damage;
		this.life = life;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	public void tick() {
		super.tick();
		fall();
		if (level().isClientSide()) {
			showWeb();
			return;
		}
		LivingEntity owner = getLivingOwner();
		if (--life <= 0 || owner == null) {
			discard();
			return;
		}
		if (tickCount % DISCHARGE_INTERVAL == 0) {
			discharge(owner);
		}
		if (tickCount % WEB_INTERVAL == 0) {
			hurtPlayersOnWeb(owner);
		}
	}

	private void fall() {
		if (standing) {
			return;
		}
		if (level().getBlockState(BlockPos.containing(getX(), getY() - STAND_HEIGHT, getZ())).isAir()) {
			setPos(getX(), getY() - FALL_SPEED, getZ());
		} else {
			standing = true;
		}
	}

	/**
	 * 每个靠近矛枪的生物引发一道闪电链。
	 * Each creature adjacent to the lance starts a lightning chain.
	 */
	private void discharge(LivingEntity owner) {
		AABB area = new AABB(getX() - 1.5, getY() - 2, getZ() - 1.5, getX() + 1.5, getY() + 2, getZ() + 1.5);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living != owner && living.isAlive() && living.hurtTime == 0 && living.getHealth() > DISCHARGE_MIN_HEALTH) {
				startLightningChain(owner, living);
			}
		}
	}

	private void startLightningChain(LivingEntity owner, LivingEntity start) {
		List<LivingEntity> targeted = new ArrayList<>();
		IntList targetIds = new IntArrayList();
		LivingEntity source = start;
		int chainDamage = CHAIN_DAMAGE;
		int hops = level().isThundering() ? CHAIN_HOPS_THUNDER : CHAIN_HOPS;
		for (int hop = 0; hop < hops; hop++) {
			LivingEntity from = source;
			List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(CHAIN_RANGE),
					candidate -> candidate != from && candidate.isAlive() && !targeted.contains(candidate));
			if (candidates.isEmpty()) {
				break;
			}
			LivingEntity target = candidates.get(random.nextInt(candidates.size()));
			if (!(target instanceof VoidHerrscher)) {
				if (target.getHealth() > CHAIN_MIN_HEALTH) {
					DamageHandler.INSTANCE.dealTrueDamage(owner, target, chainDamage + target.getMaxHealth() * 0.1F);
				}
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
			}
			targeted.add(target);
			targetIds.add(target.getId());
			source = target;
			chainDamage--;
		}
		if (!targetIds.isEmpty()) {
			// 借用植物魔法的雷霆召唤者的闪电效果。它的颜色和原版相同。
			// This uses the lightning effect of the Thundercaller of Botania. Its colors are the same as in the original.
			XplatAbstractions.instance().sendToTracking(start, new ThundercallerEffectPacket(start.getId(), targetIds));
		}
	}

	/**
	 * 返回和这把矛枪组成电网的别的矛枪。每一对矛枪只由 ID 较小的那一把处理。
	 * Returns the other lances that make a web with this lance. Only the lance with the smaller ID does the work for each pair.
	 */
	private List<SubspaceLanceEntity> getWebPartners() {
		return level().getEntitiesOfClass(SubspaceLanceEntity.class, getBoundingBox().inflate(WEB_RANGE),
				other -> other.isAlive() && other.getId() > getId());
	}

	private void hurtPlayersOnWeb(LivingEntity owner) {
		Vec3 start = position().add(0, WEB_HEIGHT, 0);
		for (SubspaceLanceEntity partner : getWebPartners()) {
			Vec3 end = partner.position().add(0, WEB_HEIGHT, 0);
			for (Player player : level().getEntitiesOfClass(Player.class, new AABB(start, end).inflate(0.4))) {
				if (player.isSpectator() || player.getHealth() <= WEB_MIN_HEALTH || !isOnLine(player.getBoundingBox().inflate(0.4), start, end)) {
					continue;
				}
				player.hurt(damageSources().indirectMagic(this, owner), WEB_DAMAGE);
				DamageHandler.INSTANCE.dealTrueDamage(owner, player, WEB_TRUE_DAMAGE);
				player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 1));
			}
		}
	}

	private static boolean isOnLine(AABB box, Vec3 start, Vec3 end) {
		return box.contains(start) || box.contains(end) || box.clip(start, end).isPresent();
	}

	private void showWeb() {
		for (SubspaceLanceEntity partner : getWebPartners()) {
			Vec3 delta = partner.position().subtract(position());
			double distance = delta.length();
			for (int i = 0; i < distance; i++) {
				Vec3 point = position().add(delta.scale(i / distance));
				level().addParticle(SparkleParticleData.sparkle(1.2F, 0.1F, 0F, 1F, 5), point.x + 0.5, point.y + 2.2, point.z + 0.5, 0, 0, 0);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_LIFE, life);
		tag.putFloat(TAG_DAMAGE, damage);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		life = tag.getInt(TAG_LIFE);
		damage = tag.getFloat(TAG_DAMAGE);
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
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
