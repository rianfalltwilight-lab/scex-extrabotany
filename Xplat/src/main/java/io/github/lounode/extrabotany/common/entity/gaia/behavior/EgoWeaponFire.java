package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import io.github.lounode.extrabotany.common.entity.LegacyPhantomSword;
import io.github.lounode.extrabotany.common.entity.LegacySwordProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.entity.FallingStarEntity;
import vazkii.botania.common.handler.BotaniaSounds;

/** Server attack formations used by the scex.1 guardian and its minions. */
public final class EgoWeaponFire {
    private EgoWeaponFire() {}
    public static void fire(LivingEntity shooter, LivingEntity target, int weapon) {
        if (shooter.level().isClientSide()) return;
        var aim = target.position().add(0, 1, 0);
        var random = shooter.getRandom();
        if (weapon == 0 || weapon == 2) {
            var kind = weapon == 0 ? LegacySwordProjectile.Kind.TERRA : LegacySwordProjectile.Kind.INFLUX;
            shooter.level().addFreshEntity(LegacySwordProjectile.create(kind, shooter, shooter.position().add(0, 1.1, 0), aim,
                    weapon == 0 ? .8 : .7, weapon == 0 ? 0 : 3));
        } else if (weapon == 1) {
            var flat = aim.subtract(shooter.position()).multiply(1, 0, 1);
            var forward = (flat.lengthSqr() < 1E-4 ? Vec3.directionFromRotation(0, shooter.getYRot()) : flat.normalize()).scale(1.75);
            var side = new Vec3(-forward.z, 0, forward.x).normalize();
            var base = shooter.position().add(0, shooter.getBbHeight() * .55, 0).add(forward);
            for (int index = -1; index <= 1; index++) shooter.level().addFreshEntity(LegacySwordProjectile.create(
                    LegacySwordProjectile.Kind.SHADOW, shooter, base.add(side.scale(index * 1.4)).add(0, Math.abs(index) * .2, 0), aim, .75, 0));
        } else if (weapon == 3) {
            for (int index = 0; index < 5; index++) {
                var impact = aim.add((.5 - random.nextDouble()) * 6, 0, (.5 - random.nextDouble()) * 6);
                var offset = new Vec3((.5 * random.nextDouble() - .25) * 18, 24, (.5 * random.nextDouble() - .25) * 18);
                var star = new FallingStarEntity(shooter, shooter.level());
                star.setPos(impact.add(offset)); star.setDeltaMovement(offset.normalize().scale(-1.5));
                shooter.level().addFreshEntity(star);
            }
        } else {
            double angle = -Math.PI + 2 * Math.PI * random.nextDouble();
            for (int index = 0; index < 4; index++) {
                double pitch = .37699111843077515 * random.nextDouble() + .8796459430051422;
                var start = aim.add(13 * Math.sin(pitch) * Math.cos(angle), 13 * Math.cos(pitch), 13 * Math.sin(pitch) * Math.sin(angle));
                shooter.level().addFreshEntity(LegacyPhantomSword.create(shooter, start, aim, index == 3 ? 0 : 5 + 5 * index,
                        index == 3 ? 9 : random.nextInt(10)));
                angle += 2 * Math.PI * random.nextDouble() * .08 + 1.0681415022205298;
            }
            shooter.playSound(BotaniaSounds.TERRABLADE, .4F, 1.4F);
        }
    }
}
