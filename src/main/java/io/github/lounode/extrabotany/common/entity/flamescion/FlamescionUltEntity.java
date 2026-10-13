package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

/**
 * 薪炎的终结技。它存在 85 tick，在第 10、35、60 tick 对 8 格内的生物各造成 12 点火焰伤害。
 * The ultimate of the Flamescion. It stays for 85 ticks and does 12 flame damage to the mobs within 8 blocks on ticks 10, 35 and 60.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityFlamescionUlt}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityFlamescionUlt}.
 */
public class FlamescionUltEntity extends FlamescionAreaEntity {
	private static final int LIFETIME_TICKS = 85;
	private static final int EXPLOSION_START_TICK = 40;
	private static final float RANGE = 8F;
	private static final float DAMAGE = 12F;

	public FlamescionUltEntity(EntityType<? extends FlamescionUltEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionUltEntity(Level level, Player owner) {
		this(ExtraBotanyEntityType.FLAMESCION_ULT, level);
		setOwner(owner);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			if (tickCount >= EXPLOSION_START_TICK) {
				level().addParticle(ParticleTypes.EXPLOSION,
						getX() - 2.0 + random.nextDouble() * 4.0,
						getY() - 2.0 + random.nextDouble() * 4.0,
						getZ() - 2.0 + random.nextDouble() * 4.0,
						0.0, 0.0, 0.0);
			}
			return;
		}
		if (tickCount == 1) {
			playSound(ExtraBotanySounds.FLAMESCION_ULT, 1F, 1F);
		}
		if (tickCount == 10 || tickCount == 35 || tickCount == 60) {
			damageInRange(RANGE, DAMAGE);
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
		}
	}
}
