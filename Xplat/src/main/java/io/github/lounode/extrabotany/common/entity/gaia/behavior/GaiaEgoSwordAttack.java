package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import java.util.Comparator;
import java.util.Map;

public final class GaiaEgoSwordAttack<E extends Gaia> extends Behavior<E> {
    public GaiaEgoSwordAttack() { super(Map.of()); }
    @Override protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return entity instanceof GaiaIII gaia && gaia.isEgo() && gaia.getInvulTime() == 0
                && gaia.tickCount % (gaia.getEgoStage() == 0 ? 70 : gaia.getEgoStage() == 1 ? 55 : 45) == 0;
    }
    @Override protected void start(ServerLevel level, E entity, long time) {
        if (entity instanceof GaiaIII gaia) gaia.getPlayersAround().stream().filter(player -> player.isAlive() && !player.isSpectator())
                .min(Comparator.comparingDouble(gaia::distanceToSqr)).ifPresent(player -> EgoWeaponFire.fire(gaia, player,
                        gaia.getEgoStage() >= 2 ? 4 : gaia.getRandom().nextInt(gaia.getEgoStage() == 1 ? 4 : 2)));
    }
}
