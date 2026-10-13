package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import vazkii.botania.common.helper.PlayerHelper;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 限量补给袋。空之律者掉落。每名玩家有一个自己的、有限的奖池：100 份奖励，抽一份少一份。
 * Supply Bag Limited Edition. The Herrscher of the Void drops it. Each player has a personal pool of limited size: 100 rewards, and each draw removes one.
 *
 * <p>奖池里有 1 个静籁永恒、4 个魔力透镜：超导、10 个英雄勋章、10 份Vier奖励袋、20 份Drei奖励袋、30 份Zwei奖励袋、10 份九又四分之三奖励袋和 15 份钻石。
 * <p>The pool has 1 Silent Eternity, 4 lenses of the type Mana Lens: SUPERCONDUCTOR, 10 Hero Medals, 10 lots of the Vier bag, 20 lots of the Drei bag, 30 lots of the Zwei bag, 10 lots of the Nine and Three Quarters bag, and 15 lots of diamonds.
 *
 * <p>奖池抽完之后，每次只给 2 个绿宝石。
 * <p>After the pool is empty, each draw only gives 2 emeralds.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemRewardBagSpecial}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemRewardBagSpecial}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>玩家第一次打开补给袋时，一定得到一个空的律者核心。这是这个移植版加的规则，因为制作空之律者核心需要它。奖池里另外还有一个。
 * The first bag that a player opens always gives an Empty Herrscher Core. This port adds that rule, because the Core of the Void needs it. The pool has one more.</li>
 * <li>奖池保存在主世界的存档数据里。原版把它保存在玩家的持久数据里。
 * The pool is kept in the saved data of the overworld. The original keeps it in the persistent data of the player.</li>
 * </ul>
 */
public class SupplyBagItem extends Item {
	public SupplyBagItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel serverLevel) {
			ItemStack reward = PoolData.get(serverLevel).draw(player.getUUID(), player.getRandom());
			if (reward.is(ExtraBotanyItems.silentEternity) && player instanceof ServerPlayer serverPlayer) {
				// 原版在抽到"只有一份"的奖励时授予这个进度。奖池里只有静籁永恒是一份。
				// The original grants this advancement for a reward that has only one lot. In the pool only the Silent Eternity has one lot.
				PlayerHelper.grantCriterion(serverPlayer, prefix("main/" + LibAdvancementNames.LUCKY_DRAW), "code_triggered");
			}
			stack.consume(1, player);
			ItemEntity drop = player.spawnAtLocation(reward);
			if (drop != null) {
				drop.setNoPickUpDelay();
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.REWARD_BAG_OPEN, SoundSource.PLAYERS, 0.8F, 1F);
		}
		return InteractionResultHolder.success(stack);
	}

	/**
	 * 一种奖励：一组物品，以及它在新奖池里出现的份数。
	 * One kind of reward: a stack of items, and the number of lots of it in a new pool.
	 */
	private enum Reward {
		// 这个顺序就是存档里的编号，所以不能改动。
		// This sequence gives the numbers in the save data, so it must not change.
		SILENT_ETERNITY(() -> ExtraBotanyItems.silentEternity, 1, 1),
		SUPERCONDUCTOR_LENS(() -> ExtraBotanyItems.lensSuperconductor, 1, 4),
		HERO_MEDAL(() -> ExtraBotanyItems.heroMedal, 1, 10),
		VIER_BAG(() -> ExtraBotanyItems.vierRewardBag, 4, 10),
		DREI_BAG(() -> ExtraBotanyItems.dreiRewardBag, 4, 20),
		ZWEI_BAG(() -> ExtraBotanyItems.zweiRewardBag, 6, 30),
		NINE_AND_THREE_QUARTERS_BAG(() -> ExtraBotanyItems.nineAndThreeQuartersRewardBag, 3, 10),
		DIAMOND(() -> Items.DIAMOND, 4, 15),
		EMPTY_CORE(() -> ExtraBotanyItems.emptyCoreOfTheVoid, 1, 1);

		private final Supplier<Item> item;
		private final int count;
		private final int lots;

		Reward(Supplier<Item> item, int count, int lots) {
			this.item = item;
			this.count = count;
			this.lots = lots;
		}

		ItemStack createStack() {
			return new ItemStack(item.get(), count);
		}
	}

	/**
	 * 全部玩家的奖池。它保存在主世界的存档数据里。
	 * The pools of all players. They are kept in the saved data of the overworld.
	 */
	public static final class PoolData extends SavedData {
		private static final String DATA_NAME = "extrabotany_limited_edition_supply_bag";
		private static final int VERSION = 3;
		private static final int POOL_SIZE = 101;
		private static final int DRAWN = -1;

		private final Map<UUID, int[]> pools = new HashMap<>();

		public static PoolData get(ServerLevel level) {
			return level.getServer().overworld().getDataStorage().computeIfAbsent(
					new SavedData.Factory<>(PoolData::new, (tag, registries) -> load(tag), null), DATA_NAME);
		}

		private static int[] createPool(RandomSource random) {
			int[] pool = new int[POOL_SIZE];
			int cursor = 0;
			for (Reward reward : Reward.values()) {
				for (int i = 0; i < reward.lots; i++) {
					pool[cursor++] = reward.ordinal();
				}
			}
			// 洗牌。
			// Shuffle.
			for (int i = pool.length - 1; i > 0; i--) {
				int j = random.nextInt(i + 1);
				int value = pool[i];
				pool[i] = pool[j];
				pool[j] = value;
			}
			return pool;
		}

		/**
		 * 为这名玩家抽一份奖励。
		 * Draws one reward for this player.
		 */
		public ItemStack draw(UUID player, RandomSource random) {
			int[] pool = pools.get(player);
			if (pool == null) {
				pools.put(player, createPool(random));
				setDirty();
				return Reward.EMPTY_CORE.createStack();
			}
			int start = random.nextInt(pool.length);
			for (int i = 0; i < pool.length; i++) {
				int slot = (start + i) % pool.length;
				int reward = pool[slot];
				if (reward == DRAWN) {
					continue;
				}
				pool[slot] = DRAWN;
				setDirty();
				return reward >= 0 && reward < Reward.values().length ? Reward.values()[reward].createStack() : ItemStack.EMPTY;
			}
			return new ItemStack(Items.EMERALD, 2);
		}

		public static PoolData load(CompoundTag tag) {
			PoolData data = new PoolData();
			if (tag.getInt("Version") != VERSION) {
				return data;
			}
			for (Tag entry : tag.getList("Pools", Tag.TAG_COMPOUND)) {
				CompoundTag saved = (CompoundTag) entry;
				int[] pool = saved.getIntArray("Pool");
				if (saved.hasUUID("UUID") && pool.length == POOL_SIZE) {
					data.pools.put(saved.getUUID("UUID"), pool);
				}
			}
			return data;
		}

		@Override
		public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
			ListTag list = new ListTag();
			pools.forEach((uuid, pool) -> {
				CompoundTag entry = new CompoundTag();
				entry.putUUID("UUID", uuid);
				entry.putIntArray("Pool", pool);
				list.add(entry);
			});
			tag.putInt("Version", VERSION);
			tag.put("Pools", list);
			return tag;
		}
	}
}
