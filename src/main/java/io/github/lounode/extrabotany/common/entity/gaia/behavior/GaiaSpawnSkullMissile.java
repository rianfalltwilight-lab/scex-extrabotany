package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.SkullMissileEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

/**
 * 盖亚守护者III用骷髅导弹代替魔法飞弹。
 * Guardian of Gaia III uses skull missiles in place of magic missiles.
 */
public class GaiaSpawnSkullMissile<E extends Gaia> extends GaiaSpawnMissile<E> {
	@Override
	protected void spawnMissile(Gaia gaia) {
		SkullMissileEntity missile = new SkullMissileEntity(gaia.level(), gaia);
		missile.setPos(
				gaia.getX() + (Math.random() - 0.5 * 0.1),
				gaia.getY() + 2.4 + (Math.random() - 0.5 * 0.1),
				gaia.getZ() + (Math.random() - 0.5 * 0.1)
		);

		if (missile.findTarget()) {
			gaia.playSound(BotaniaSounds.MISSILE, 1F, 0.8F + (float) Math.random() * 0.2F);
			gaia.level().addFreshEntity(missile);
		}
	}
}
