package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.generating.ReikarlilyBlockEntity;
import io.github.lounode.extrabotany.mixin.accessor.LightningBoltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P1ReikarlilyGameTests {
    @GameTest(template = "projectile_arena", timeoutTicks = 20, skyAccess = true)
    public static void emptyFlowerSummonsHarmlessLightningAndStillProducesMana(GameTestHelper h) throws ReflectiveOperationException {
        withRain(h, () -> verifySelfSummon(h, false));
        h.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20, skyAccess = true)
    public static void fullFlowerSummonsHarmlessLightningWithoutStartingProduction(GameTestHelper h) throws ReflectiveOperationException {
        withRain(h, () -> verifySelfSummon(h, true));
        h.succeed();
    }

    private static void verifySelfSummon(GameTestHelper h, boolean full) {
        var flower = flower(h);
        flower.setSpawnLightningCooldown(2);
        if (full) {
            flower.addMana(flower.getMaxMana());
        }
        List<LightningBolt> spawned = new ArrayList<>();
        List<Boolean> visualAtJoin = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == h.getLevel() && event.getEntity() instanceof LightningBolt bolt
                    && bolt.getBoundingBox().intersects(new AABB(flower.getBlockPos()).inflate(4))) {
                spawned.add(bolt);
                visualAtJoin.add(isVisual(bolt));
            }
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            assertRainReachesFlower(h, flower);
            flower.tickFlower();
            h.assertTrue(spawned.isEmpty() && flower.getSpawnLightningCooldown() == 1,
                    "Self-summon ignored its remaining cooldown");
            flower.tickFlower();
            h.assertTrue(spawned.size() == 1 && visualAtJoin.equals(List.of(true)),
                    "Self-summoned lightning was not visual-only before joining the world");
            h.assertTrue(flower.getSpawnLightningCooldown() == flower.getAfterSpawnLightningCooldown(),
                    "Successful self-summon did not reset its cooldown");
            h.assertTrue(flower.getMana() == (full ? flower.getMaxMana() : Math.min(flower.getMaxMana(), flower.getProduceMana())),
                    "Self-summon produced the wrong amount of mana");
            h.assertTrue(flower.getCooldown() == (full ? 0 : flower.getAfterWorkCooldown())
                            && flower.getResidualHeat() == (full ? 0 : flower.getResidualHeatAfterProduce()),
                    "Capacity did not control the production cooldown and residual heat");

            if (!full) {
                flower.addMana(-flower.getMana());
                flower.tickFlower();
                h.assertTrue(flower.getMana() == Math.min(flower.getMaxMana(), flower.getResidualHeatProduceMana())
                                && flower.getResidualHeat() == flower.getResidualHeatAfterProduce() - 1
                                && flower.getCooldown() == flower.getAfterWorkCooldown() - 1,
                        "Self-summon lost its residual production or work cooldown");
                h.assertTrue(spawned.size() == 1, "Work cooldown allowed another self-summon");
            }

            // Free capacity while the visual bolt is still alive: it must not be absorbed a second time.
            flower.addMana(-flower.getMana());
            flower.setCooldown(0);
            flower.setResidualHeat(0);
            flower.setSpawnLightningCooldown(100);
            flower.tickFlower();
            h.assertTrue(flower.getMana() == 0 && flower.getCooldown() == 0 && flower.getResidualHeat() == 0,
                    "An existing visual bolt produced mana again");
            verifyLightningDoesNoHarm(h, spawned.getFirst());
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            spawned.forEach(LightningBolt::discard);
        }
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void naturalLightningStillRespectsCapacityAndWorkCooldown(GameTestHelper h) {
        var flower = flower(h);
        flower.setSpawnLightningCooldown(100);
        var first = naturalBolt(h, flower.getBlockPos());
        LightningBolt second = null;
        try {
            flower.setCooldown(1);
            flower.tickFlower();
            h.assertTrue(!isVisual(first) && flower.getMana() == 0 && flower.getCooldown() == 0,
                    "Natural lightning was consumed before the work cooldown elapsed");
            flower.tickFlower();
            h.assertTrue(isVisual(first) && flower.getMana() == Math.min(flower.getMaxMana(), flower.getProduceMana())
                            && flower.getCooldown() == flower.getAfterWorkCooldown()
                            && flower.getResidualHeat() == flower.getResidualHeatAfterProduce(),
                    "Ready flower no longer absorbs natural lightning and starts production");

            second = naturalBolt(h, flower.getBlockPos());
            flower.setCooldown(0);
            flower.setResidualHeat(0);
            flower.addMana(flower.getMaxMana() - flower.getMana());
            flower.tickFlower();
            h.assertTrue(!isVisual(second) && flower.getMana() == flower.getMaxMana()
                            && flower.getCooldown() == 0 && flower.getResidualHeat() == 0,
                    "Full flower changed natural lightning absorption semantics");
            flower.addMana(-flower.getMana());
            flower.tickFlower();
            h.assertTrue(isVisual(second) && flower.getMana() == Math.min(flower.getMaxMana(), flower.getProduceMana()),
                    "Natural lightning was not absorbed after capacity became available");
        } finally {
            first.discard();
            if (second != null) {
                second.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20, skyAccess = true)
    public static void selfSummonDoesNotSkipNaturalLightningAbsorption(GameTestHelper h) throws ReflectiveOperationException {
        withRain(h, () -> {
            var flower = flower(h);
            flower.setSpawnLightningCooldown(1);
            var natural = naturalBolt(h, flower.getBlockPos());
            try {
                assertRainReachesFlower(h, flower);
                flower.tickFlower();
                var bolts = h.getLevel().getEntitiesOfClass(LightningBolt.class, new AABB(flower.getBlockPos()).inflate(4));
                h.assertTrue(bolts.size() == 2 && bolts.contains(natural) && bolts.stream().allMatch(P1ReikarlilyGameTests::isVisual),
                        "Self-summon bypassed absorption of the already present natural bolt");
                h.assertTrue(flower.getMana() == Math.min(flower.getMaxMana(), flower.getProduceMana())
                                && flower.getCooldown() == flower.getAfterWorkCooldown(),
                        "Coincident natural and self-summoned lightning changed production");
            } finally {
                h.getLevel().getEntitiesOfClass(LightningBolt.class, new AABB(flower.getBlockPos()).inflate(4))
                        .forEach(LightningBolt::discard);
            }
        });
        h.succeed();
    }

    private static ReikarlilyBlockEntity flower(GameTestHelper h) {
        var flower = new ReikarlilyBlockEntity(h.absolutePos(new BlockPos(32, 5, 32)),
                ExtrabotanyFlowerBlocks.reikarlily.defaultBlockState());
        flower.setLevel(h.getLevel());
        return flower;
    }

    private static void assertRainReachesFlower(GameTestHelper h, ReikarlilyBlockEntity flower) {
        var level = h.getLevel();
        var pos = flower.getEffectivePos();
        h.assertTrue(level.isRainingAt(pos), "Rain fixture rejected: rain=" + level.isRaining()
                + ", sky=" + level.canSeeSky(pos)
                + ", heightmapY=" + level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY()
                + ", flowerY=" + pos.getY()
                + ", precipitation=" + level.getBiome(pos).value().getPrecipitationAt(pos));
    }

    private static LightningBolt naturalBolt(GameTestHelper h, BlockPos pos) {
        var bolt = EntityType.LIGHTNING_BOLT.create(h.getLevel());
        bolt.moveTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
        h.assertTrue(h.getLevel().addFreshEntity(bolt), "Could not add the natural lightning fixture");
        return bolt;
    }

    private static boolean isVisual(LightningBolt bolt) {
        return ((LightningBoltAccessor) bolt).isVisualOnly();
    }

    private static void verifyLightningDoesNoHarm(GameTestHelper h, LightningBolt bolt) {
        var level = h.getLevel();
        var cow = EntityType.COW.create(level);
        cow.setPos(bolt.position());
        cow.setNoAi(true);
        var item = new ItemEntity(level, bolt.getX(), bolt.getY(), bolt.getZ(), new ItemStack(Items.STICK));
        var woodPos = bolt.blockPosition().below();
        var previousBlock = level.getBlockState(woodPos);
        boolean fireTick = level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK);
        try {
            level.setBlockAndUpdate(woodPos, Blocks.OAK_PLANKS.defaultBlockState());
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
            h.assertTrue(level.addFreshEntity(cow) && level.addFreshEntity(item), "Could not add lightning victims");
            float health = cow.getHealth();
            // Exercise every vanilla flash without advancing the shared world's weather or other tests.
            for (int i = 0; i < 80 && !bolt.isRemoved(); i++) {
                bolt.tick();
            }
            h.assertTrue(bolt.isRemoved(), "Lightning fixture did not finish its flashes");
            h.assertTrue(cow.isAlive() && cow.getHealth() == health && !cow.isOnFire(),
                    "Self-summoned lightning damaged or ignited a nearby mob");
            h.assertTrue(item.isAlive() && item.getItem().is(Items.STICK) && item.getItem().getCount() == 1 && !item.isOnFire(),
                    "Self-summoned lightning destroyed or ignited a dropped item");
            h.assertTrue(bolt.getBlocksSetOnFire() == 0 && level.getBlockState(woodPos).is(Blocks.OAK_PLANKS)
                            && level.getBlockState(woodPos.above()).isAir(),
                    "Self-summoned lightning ignited a flammable block");
        } finally {
            cow.discard();
            item.discard();
            level.setBlockAndUpdate(woodPos, previousBlock);
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(fireTick, level.getServer());
        }
    }

    private static void withRain(GameTestHelper h, Runnable body) throws ReflectiveOperationException {
        var level = h.getLevel();
        float previousRain = level.getRainLevel(0);
        float currentRain = level.getRainLevel(1);
        // setRainLevel also writes the interpolation endpoint; restore both, without changing weather timers/flags.
        Field previousRainField = Level.class.getDeclaredField("oRainLevel");
        previousRainField.setAccessible(true);
        try {
            level.setRainLevel(1);
            body.run();
        } finally {
            level.setRainLevel(currentRain);
            previousRainField.setFloat(level, previousRain);
        }
    }
}
