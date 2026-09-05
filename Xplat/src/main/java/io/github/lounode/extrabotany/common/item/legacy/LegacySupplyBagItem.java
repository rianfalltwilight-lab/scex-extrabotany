package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LegacySupplyBagItem extends Item {
    public static final LegacySupplyBagItem INSTANCE = new LegacySupplyBagItem();
    private LegacySupplyBagItem() { super(new Properties().rarity(Rarity.RARE)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            var data = server.getServer().overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(PoolData::new, (tag, registries) -> PoolData.load(tag), null), "extrabotany_limited_edition_supply_bag");
            var reward = data.draw(player.getUUID(), player.getRandom());
            if (!player.isCreative()) stack.shrink(1);
            var drop = player.spawnAtLocation(reward);
            if (drop != null) drop.setNoPickUpDelay();
            level.playSound(null, player.blockPosition(), ExtraBotanySounds.REWARD_BAG_OPEN, SoundSource.PLAYERS, .8F, 1);
        }
        return InteractionResultHolder.success(stack);
    }

    /** Version 3 uses a per-player finite pool, including consumed (-1) slots. */
    public static final class PoolData extends SavedData {
        private final Map<UUID, int[]> pools = new HashMap<>();
        private static int[] fresh(RandomSource random) {
            int[] pool = new int[101];
            int cursor = 0;
            int[] counts = {1, 4, 10, 10, 20, 30, 10, 15, 1};
            // Match legacy template order before Fisher-Yates shuffling.
            for (int kind : new int[] {8, 0, 1, 2, 3, 4, 5, 6, 7}) for (int i = 0; i < counts[kind]; i++) pool[cursor++] = kind;
            for (int i = pool.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1), value = pool[i]; pool[i] = pool[j]; pool[j] = value;
            }
            return pool;
        }
        public ItemStack draw(UUID player, RandomSource random) {
            var pool = pools.get(player);
            if (pool == null) {
                pools.put(player, fresh(random)); setDirty(); return new ItemStack(ExtraBotanyItems.emptyCoreOfTheVoid);
            }
            int start = random.nextInt(pool.length);
            for (int i = 0; i < pool.length; i++) {
                int slot = (start + i) % pool.length;
                if (pool[slot] == -1) continue;
                int reward = pool[slot]; pool[slot] = -1; setDirty(); return reward(reward);
            }
            pool = fresh(random); pools.put(player, pool);
            int reward = pool[0]; pool[0] = -1; setDirty(); return reward(reward);
        }
        private static ItemStack reward(int kind) {
            String[] names = {"extrabotany:silent_eternity", "extrabotany:lens_superconductor", "extrabotany:hero_medal",
                    "extrabotany:vier_reward_bag", "extrabotany:drei_reward_bag", "extrabotany:zwei_reward_bag",
                    "extrabotany:nine_and_three_quarters_reward_bag", "minecraft:diamond", "extrabotany:empty_core_of_the_void"};
            int[] counts = {1, 1, 1, 4, 4, 6, 3, 4, 1};
            return kind < 0 || kind >= names.length ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(names[kind])), counts[kind]);
        }
        public static PoolData load(CompoundTag tag) {
            var data = new PoolData();
            if (tag.getInt("Version") == 3) for (var entry : tag.getList("Pools", 10)) {
                var saved = (CompoundTag) entry;
                var pool = saved.getIntArray("Pool");
                if (saved.hasUUID("UUID") && pool.length == 101) data.pools.put(saved.getUUID("UUID"), pool);
            }
            return data;
        }
        @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            var list = new ListTag();
            pools.forEach((uuid, pool) -> { var entry = new CompoundTag(); entry.putUUID("UUID", uuid); entry.putIntArray("Pool", pool); list.add(entry); });
            tag.putInt("Version", 3); tag.put("Pools", list); return tag;
        }
    }
}
