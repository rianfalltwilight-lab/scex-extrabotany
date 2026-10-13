package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;

/**
 * 强化斩击。玩家有"薪炎强化"效果时挥剑，一次放出三道。它飞行 15 tick，每 2 tick 对 2.5 格内的生物造成 5 点火焰伤害。
 * Strengthened Slash. A swing with the Flamescion Strength effect releases three of them. It flies for 15 ticks and does 5 flame damage to the mobs within 2.5 blocks on each 2nd tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityStrengthenSlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityStrengthenSlash}.
 *
 * <p>和原版的差异：原版的构造方法忘了保存主人，所以强化斩击从不造成伤害。这里保存主人，伤害按原版代码里写的数值生效。
 * <p>Difference from the original: the constructor of the original does not keep the owner, so the Strengthened Slash never does damage. This port keeps the owner, and the damage applies with the values that the original code gives.
 */
public class StrengthenSlashEntity extends FlameProjectileEntity {
	public StrengthenSlashEntity(EntityType<? extends StrengthenSlashEntity> type, Level level) {
		super(type, level);
	}

	public StrengthenSlashEntity(Level level, Player owner, Vec3 direction) {
		this(ExtraBotanyEntityType.STRENGTHEN_SLASH, level);
		launchFrom(owner, direction);
	}

	@Override
	protected int getLifetimeTicks() {
		return 15;
	}

	@Override
	protected int getDamageIntervalTicks() {
		return 2;
	}

	@Override
	protected float getDamageRange() {
		return 2.5F;
	}

	@Override
	protected float getDamage() {
		return 5F;
	}
}
