package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.api.recipe.EdelweissRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.TradeOrchidBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.EdelweissBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.crafting.EdelweissRecipes;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.crafting.RecipeHolder;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class FlowerHotPathGameTests {
    private static RecipeHolder<EdelweissRecipe> cowRecipe(String id, int mana) {
        return new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath("extrabotany", "hotpath/" + id),
                new EdelweissRecipes(EntityTypePredicate.of(EntityType.COW), mana));
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void typedRecipesKeepPriorityAndObserveReplacement(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<>(manager.getRecipes());
        var first = cowRecipe("first", 301);
        var second = cowRecipe("second", 709);
        var cow = EntityType.COW.create(h.getLevel());
        var flower = new EdelweissBlockEntity(h.absolutePos(new BlockPos(32, 5, 32)), ExtrabotanyFlowerBlocks.edelweiss.defaultBlockState());
        flower.setLevel(h.getLevel());
        try {
            manager.replaceRecipes(List.of(first, second));
            var holders = ExtraBotanyRecipeTypes.getRecipeHolders(h.getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE);
            h.assertTrue(holders.stream().map(RecipeHolder::value).toList().equals(
                    new ArrayList<>(ExtraBotanyRecipeTypes.getRecipes(h.getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE).values())),
                    "Typed iteration changed the public map API priority");
            h.assertTrue(flower.getManaFromEntity(cow) == 301, "First matching recipe lost priority");
            manager.replaceRecipes(List.of(second, first));
            h.assertTrue(flower.getManaFromEntity(cow) == 709, "Public lookup retained a stale recipe after replacement");
            h.assertTrue(ExtraBotanyRecipeTypes.getRecipeHolders(h.getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE).getFirst() == second,
                    "Typed recipe lookup retained the old manager list");
        } finally {
            manager.replaceRecipes(original);
        }
        h.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void edelweissPreservesVirtualSelectionAndSecondLookup(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<>(manager.getRecipes());
        var pos = h.absolutePos(new BlockPos(32, 5, 32));
        var flower = new CountingEdelweiss(pos);
        flower.setLevel(h.getLevel());
        List<Entity> cows = new ArrayList<>();
        try {
            manager.replaceRecipes(List.of(cowRecipe("selection", 301)));
            for (int i = 0; i < 3; i++) {
                var cow = EntityType.COW.create(h.getLevel());
                cow.setPos(pos.getX() + .3 + i * .2, pos.getY(), pos.getZ() + .5);
                cow.setNoAi(true);
                h.getLevel().addFreshEntity(cow);
                cows.add(cow);
            }
            flower.rejected = cows.getFirst();
            flower.tickFlower();
            h.assertTrue(!flower.rejected.isRemoved(), "Subclass veto was bypassed");
            h.assertTrue(cows.stream().filter(Entity::isRemoved).count() == 1, "Edelweiss did not consume exactly one eligible entity");
            h.assertTrue(flower.calls.values().stream().mapToInt(Integer::intValue).sum() == 4,
                    "Virtual lookup must run for each entity, then once more for the selected entity");
            h.assertTrue(flower.getMana() == 308, "Subclass mana adjustment was bypassed");
            h.assertTrue(flower.getCooldown() == flower.getAfterWorkCooldown(), "Work cooldown changed");
            manager.replaceRecipes(List.of(cowRecipe("after_tick", 607)));
            h.assertTrue(flower.getManaFromEntity(cows.get(1)) == 614, "Completed tick retained its recipe snapshot");
        } finally {
            cows.forEach(Entity::discard);
            manager.replaceRecipes(original);
        }
        h.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void edelweissReleasesSnapshotAfterOverrideFailure(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<>(manager.getRecipes());
        var pos = h.absolutePos(new BlockPos(32, 5, 32));
        var flower = new CountingEdelweiss(pos);
        flower.setLevel(h.getLevel());
        var cow = EntityType.COW.create(h.getLevel());
        cow.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        cow.setNoAi(true);
        h.getLevel().addFreshEntity(cow);
        try {
            manager.replaceRecipes(List.of(cowRecipe("before_failure", 301)));
            flower.throwOnLookup = true;
            boolean failed = false;
            try { flower.tickFlower(); }
            catch (LookupFailure expected) { failed = true; }
            h.assertTrue(failed && !cow.isRemoved() && flower.getMana() == 0, "Lookup exception changed consumption semantics");
            flower.throwOnLookup = false;
            manager.replaceRecipes(List.of(cowRecipe("after_failure", 809)));
            h.assertTrue(flower.getManaFromEntity(cow) == 816, "Failed tick leaked the old recipe snapshot");
            flower.tickFlower();
            h.assertTrue(cow.isRemoved() && flower.getMana() == 816, "Next tick did not use the replaced recipe");
        } finally {
            cow.discard();
            manager.replaceRecipes(original);
        }
        h.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void tradeOrchidKeepsBudgetOrderAndSendsNoFullSync(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(32, 5, 32));
        var flower = new CountingTradeOrchid(pos);
        flower.setLevel(h.getLevel());
        List<Villager> spawned = new ArrayList<>();
        try {
            for (int x : new int[] { 1, 2, 3, 4, 10, 15 }) {
                var villager = EntityType.VILLAGER.create(h.getLevel());
                // Rectangle.square has a zero-height Y plane; the entity box must cross it.
                villager.setPos(pos.getX() + x + .5, pos.getY() - .25, pos.getZ() + .5);
                villager.setNoAi(true);
                h.getLevel().addFreshEntity(villager);
                spawned.add(villager);
            }
            var near = h.getLevel().getEntitiesOfClass(Villager.class, ((RadiusDescriptor.Rectangle) flower.getRadius()).aabb());
            h.assertTrue(near.size() == 4, "Unexpected test villagers in the near query");
            var unchanged = near.getFirst();
            var discount = flower.getNewEffect();
            // Vanilla treats visible/showIcon changes as successful effect updates,
            // even when the existing effect is stronger and lasts longer.
            unchanged.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.DISCOUNT, 1000, 99,
                    discount.isAmbient(), discount.isVisible(), discount.showIcon()));
            h.assertTrue(!unchanged.addEffect(flower.getNewEffect()),
                    "Stronger-effect fixture must reject an unchanged discount before testing mana order");
            var far = spawned.get(4);
            var beyond = spawned.get(5);
            far.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.DISCOUNT, 200, 49));
            beyond.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.DISCOUNT, 200, 49));
            flower.addMana(2 * flower.getManaPerUse());
            flower.prepare(39);
            flower.tickFlower();
            h.assertTrue(flower.getMana() == 0, "Trade Orchid charged the wrong number of successful effects");
            h.assertTrue(near.get(1).hasEffect(ExtraBotanyMobEffects.DISCOUNT) && near.get(2).hasEffect(ExtraBotanyMobEffects.DISCOUNT)
                    && !near.get(3).hasEffect(ExtraBotanyMobEffects.DISCOUNT), "Insufficient-mana selection changed entity order");
            h.assertTrue(unchanged.getEffect(ExtraBotanyMobEffects.DISCOUNT).getAmplifier() == 99, "Unchanged stronger effect was replaced");
            h.assertTrue(!far.hasEffect(ExtraBotanyMobEffects.DISCOUNT) && beyond.hasEffect(ExtraBotanyMobEffects.DISCOUNT),
                    "Outer-range cleanup crossed the original boundary");
            // Botania saves and synchronizes the mana. The flower has no data of its own, thus it must not send a full update packet.
            h.assertTrue(flower.syncCalls == 0, "The Trade Orchid sent a full update packet for a mana change");
            flower.prepare(40);
            flower.tickFlower();
            h.assertTrue(flower.syncCalls == 0, "An idle tick sent a full update packet");
            flower.powered = true;
            flower.addMana(flower.getManaPerUse());
            flower.prepare(79);
            flower.tickFlower();
            h.assertTrue(flower.getMana() == flower.getManaPerUse() && flower.syncCalls == 0, "A powered Trade Orchid did work");
            flower.addMana(-flower.getManaPerUse());
            flower.powered = false;
            flower.cooldown = 1;
            near.forEach(v -> v.removeEffect(ExtraBotanyMobEffects.DISCOUNT));
            flower.addMana(4 * flower.getManaPerUse());
            flower.prepare(60);
            flower.tickFlower();
            h.assertTrue(flower.getMana() == 0 && near.stream().allMatch(v -> v.hasEffect(ExtraBotanyMobEffects.DISCOUNT)),
                    "Non-heartbeat work changed discount or mana semantics");
            h.assertTrue(flower.syncCalls == 0, "The Trade Orchid sent a full update packet for a mana change");
        } finally {
            spawned.forEach(Entity::discard);
        }
        h.succeed();
    }

    private static class LookupFailure extends RuntimeException {}
    private static class CountingEdelweiss extends EdelweissBlockEntity {
        private final Map<Entity, Integer> calls = new HashMap<>();
        private Entity rejected;
        private boolean throwOnLookup;
        private CountingEdelweiss(BlockPos pos) { super(pos, ExtrabotanyFlowerBlocks.edelweiss.defaultBlockState()); }
        @Override public int getManaFromEntity(Entity entity) {
            if (throwOnLookup) { throw new LookupFailure(); }
            calls.merge(entity, 1, Integer::sum);
            return entity == rejected ? 0 : super.getManaFromEntity(entity) + 7;
        }
    }
    private static class CountingTradeOrchid extends TradeOrchidBlockEntity {
        private int syncCalls;
        private int cooldown = 40;
        private boolean powered;
        private CountingTradeOrchid(BlockPos pos) { super(pos, ExtrabotanyFlowerBlocks.tradeOrchid.defaultBlockState()); }
        private void prepare(long priorTick) { ticksExisted = priorTick; syncCalls = 0; }
        @Override public int getCooldown() { return cooldown; }
        @Override public boolean isPowered() { return powered; }
        @Override public void markForImmediateSync() { syncCalls++; super.markForImmediateSync(); }
    }
}
