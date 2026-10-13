package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.*;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class UpstreamParityGameTests {
    private static Player player(GameTestHelper h, GameType mode) {
        var player = h.makeMockPlayer(mode); player.setPos(h.absoluteVec(new Vec3(3, 8, 3))); player.setNoGravity(true); return player;
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void spearFanStaysPerpendicularForEveryView(GameTestHelper h) {
        for (int yaw = -180; yaw <= 180; yaw += 15) {
            var look = Vec3.directionFromRotation(0, yaw);
            var distinct = new java.util.HashSet<String>();
            for (int i = 0; i < 24; i++) {
                var offset = io.github.lounode.extrabotany.common.item.relic.SpearOfSubspaceItem.domainOffset(look, i);
                h.assertTrue(Double.isFinite(offset.length()) && Math.abs(offset.length() - (5 + i / 8 * 3.5)) < 1E-5,
                        "Fan radius/NaN mismatch at yaw=" + yaw + " index=" + i);
                h.assertTrue(Math.abs(offset.dot(look)) < 1E-5 && offset.y >= 0, "Fan rotated around world X instead of look at yaw=" + yaw);
                distinct.add(String.format(java.util.Locale.ROOT,"%.3f,%.3f,%.3f",offset.x,offset.y,offset.z));
            }
            h.assertTrue(distinct.size() == 24, "Portals collapse onto each other at yaw=" + yaw);
        }
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void timeLockSlowsFallingMotionWithoutReplacingMoveAttribute(GameTestHelper h) {
        var mob=EntityType.HUSK.create(h.getLevel());mob.setDeltaMovement(.4,-1,.2);
        ExtraBotanyMobEffects.TIMELOCK.value().applyEffectTick(mob,0);
        h.assertTrue(mob.getDeltaMovement().distanceTo(new Vec3(.012,-.03,.006))<1E-8,"Time lock fall-motion semantics missing");
        mob.setDeltaMovement(0,1,0);ExtraBotanyMobEffects.TIMELOCK.value().applyEffectTick(mob,0);
        h.assertTrue(mob.getDeltaMovement().y==1,"Time lock destroys initial upward launch");h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void creativeTargetsCannotLoseHealthToLegacyTrueDamage(GameTestHelper h) {
        var attacker=player(h,GameType.SURVIVAL);var target=player(h,GameType.CREATIVE);float health=target.getHealth();
        h.assertTrue(!DamageHandler.INSTANCE.checkPassable(target,attacker),"Creative target admitted to direct health damage");
        io.github.lounode.extrabotany.common.handler.DamageHandler.INSTANCE.dealTrueDamage(attacker, target, 10);
        h.assertTrue(target.getHealth()==health,"Creative player lost health without a hurt event");h.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template="empty")
    public static void effect_holders_survive_serialization(net.minecraft.gametest.framework.GameTestHelper h) {
        var effects=java.util.List.of(ExtraBotanyMobEffects.ETERNITY,ExtraBotanyMobEffects.INCANDESCENCE,
                ExtraBotanyMobEffects.FLAMESCION,ExtraBotanyMobEffects.WITCH_CURSE,ExtraBotanyMobEffects.TIMELOCK,
                ExtraBotanyMobEffects.IMMOBILIZE,ExtraBotanyMobEffects.LINK,ExtraBotanyMobEffects.HEAL_REVERSE,
                ExtraBotanyMobEffects.DISCOUNT,ExtraBotanyMobEffects.WARM,ExtraBotanyMobEffects.THIRROR);
        for(var effect:effects) {
            var canonical=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(effect.unwrapKey().orElseThrow());
            h.assertTrue(effect==canonical,"Effect proxy differs from network holder: "+effect.unwrapKey());
            var instance=new net.minecraft.world.effect.MobEffectInstance(effect,80);
            var encoded=net.minecraft.world.effect.MobEffectInstance.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,instance).getOrThrow();
            var decoded=net.minecraft.world.effect.MobEffectInstance.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow();
            var mob=net.minecraft.world.entity.EntityType.VILLAGER.create(h.getLevel());
            mob.addEffect(decoded);h.assertTrue(mob.hasEffect(effect),"Saved effect cannot be queried after decode");
            h.assertTrue(mob.removeEffect(effect),"Decoded effect cannot be removed through its registered constant");
        }
        h.succeed();
    }
}
