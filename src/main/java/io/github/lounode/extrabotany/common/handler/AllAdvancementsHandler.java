package io.github.lounode.extrabotany.common.handler;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 完成本模组全部主进度的奖励。玩家完成最后一个主进度时得到"Erase or Zero"进度；得到这个进度的玩家得到一个奖杯。
 * The reward for all main advancements of this mod. When a player completes the last main advancement, the player gets the advancement "Erase or Zero"; a player that gets this advancement gets one Trophy.
 *
 * <p>"主进度"是命名空间为 {@code extrabotany}、路径以 {@code main/} 开头、并且在进度界面里显示的进度。隐藏的进度也算。数据包可以增加或者去掉进度，这里每次都从服务器读取当前的列表。
 * <p>A "main advancement" is an advancement with the namespace {@code extrabotany}, a path that starts with {@code main/}, and a display in the advancement screen. Hidden advancements also count. A data pack can add or remove advancements, thus the current list is read from the server each time.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code StatHandler}（进度 {@code allStats}）。这个移植版以前有这个进度的数据，但是没有授予它的代码，所以奖杯没有获得的途径。
 * <p>Source: ExtraMeteorP 1.12 {@code StatHandler} (the advancement {@code allStats}). Before, this port had the data of this advancement but no code that granted it, thus there was no way to get the Trophy.
 */
public final class AllAdvancementsHandler {
	private static final String MAIN_PREFIX = "main/";
	private static final ResourceLocation ALL_STATS = prefix(MAIN_PREFIX + LibAdvancementNames.ALL_STATS);
	private static final ResourceLocation ROOT = prefix(MAIN_PREFIX + LibAdvancementNames.ROOT);
	private static final ResourceLocation ENDGAME_GOAL = prefix(MAIN_PREFIX + LibAdvancementNames.ENDGAME_GOAL);
	private static final ResourceLocation HERRSCHER_DEFEAT = prefix(MAIN_PREFIX + LibAdvancementNames.HERRSCHER_DEFEAT);
	/**
	 * 对照原版审计 E5：1.12 StatHandler。完成 6、12、18 个进度时各得到一个进度，并掉落一件物品。
	 * Parity audit E5: StatHandler of 1.12. At 6, 12 and 18 completed advancements the player gets one advancement each, and one item drops.
	 */
	private static final ResourceLocation[] COUNT_REWARDS = {
			prefix(MAIN_PREFIX + LibAdvancementNames.MANA_DRIVER_GET),
			prefix(MAIN_PREFIX + LibAdvancementNames.MAGIC_FINGER_GET),
			prefix(MAIN_PREFIX + LibAdvancementNames.JINGWEI_FEATHER_GET)
	};
	public static final int[] COUNT_NEEDED = { 6, 12, 18 };

	private AllAdvancementsHandler() {}

	/**
	 * 玩家完成一个进度时调用。
	 * Call this when a player completes an advancement.
	 */
	public static void onAdvancementEarned(Player player, AdvancementHolder earned) {
		if (!(player instanceof ServerPlayer serverPlayer) || !PlayerHelper.isTruePlayer(player)) {
			return;
		}
		ResourceLocation id = earned.id();
		if (id.equals(ALL_STATS)) {
			// 和原版相同：不管这个进度是怎么得到的，得到它的时候就给奖杯。
			// The same as in the original: the Trophy is given when the player gets this advancement, by any method.
			drop(serverPlayer, new ItemStack(ExtraBotanyBlocks.trophy));
			return;
		}
		// 1.12 StatHandler.getAchievement 的物品奖励：击败空之律者给柠檬（mask:8），"终局"给 Thug Life（mask:9）和两张唱片。
		// The item rewards of StatHandler.getAchievement of 1.12: defeating the Herrscher gives the lemon (mask:8), "End Day" gives Thug Life (mask:9) and the two discs.
		if (id.equals(COUNT_REWARDS[0])) {
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.dispersiveRing));
		} else if (id.equals(COUNT_REWARDS[1])) {
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.magicFinger));
		} else if (id.equals(COUNT_REWARDS[2])) {
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.featherOfJingwei));
		} else if (id.equals(HERRSCHER_DEFEAT)) {
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.lemon));
		} else if (id.equals(ENDGAME_GOAL)) {
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.thugLife));
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.recordGaia3));
			drop(serverPlayer, new ItemStack(ExtraBotanyItems.recordHerrscherOfTheVoid));
		}
		if (isMainAdvancement(earned)) {
			check(serverPlayer);
		}
	}

	private static void drop(ServerPlayer player, ItemStack stack) {
		ItemEntity entity = player.spawnAtLocation(stack, 0F);
		if (entity != null) {
			entity.setNoPickUpDelay();
		}
	}

	/**
	 * 数出完成的主进度，不算根进度、三个计数奖励进度和最后的进度。
	 * Counts the completed main advancements, without the root, the three counting rewards and the last advancement.
	 */
	public static int countCompleted(ServerPlayer player) {
		PlayerAdvancements progress = player.getAdvancements();
		int count = 0;
		for (AdvancementHolder holder : player.server.getAdvancements().getAllAdvancements()) {
			if (isMainAdvancement(holder) && !isCountReward(holder.id()) && !holder.id().equals(ROOT)
					&& progress.getOrStartProgress(holder).isDone()) {
				count++;
			}
		}
		return count;
	}

	private static boolean isCountReward(ResourceLocation id) {
		for (ResourceLocation reward : COUNT_REWARDS) {
			if (reward.equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static void grantCountRewards(ServerPlayer player) {
		int completed = countCompleted(player);
		for (int i = 0; i < COUNT_REWARDS.length; i++) {
			if (completed < COUNT_NEEDED[i]) {
				break;
			}
			AdvancementHolder reward = player.server.getAdvancements().get(COUNT_REWARDS[i]);
			if (reward != null && !player.getAdvancements().getOrStartProgress(reward).isDone()) {
				PlayerHelper.grantCriterion(player, COUNT_REWARDS[i], "code_triggered");
			}
		}
	}

	/**
	 * 如果玩家已经完成全部主进度，授予最后的进度。玩家登录时也调用它，这样在这个功能加入之前就完成了全部进度的玩家也能得到奖杯。
	 * If the player has completed all main advancements, this grants the last advancement. It is also called at login, so that a player who completed all advancements before this function was added also gets the Trophy.
	 */
	public static void check(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer) || !PlayerHelper.isTruePlayer(player)) {
			return;
		}
		grantCountRewards(serverPlayer);
		ServerAdvancementManager manager = serverPlayer.server.getAdvancements();
		AdvancementHolder goal = manager.get(ALL_STATS);
		if (goal == null) {
			return;
		}
		PlayerAdvancements progress = serverPlayer.getAdvancements();
		if (progress.getOrStartProgress(goal).isDone()) {
			return;
		}
		int counted = 0;
		for (AdvancementHolder holder : manager.getAllAdvancements()) {
			if (!isMainAdvancement(holder)) {
				continue;
			}
			if (!progress.getOrStartProgress(holder).isDone()) {
				return;
			}
			counted++;
		}
		// 数据包把主进度全部去掉时，什么也不给。
		// When a data pack removes all main advancements, nothing is given.
		if (counted > 0) {
			PlayerHelper.grantCriterion(serverPlayer, ALL_STATS, "code_triggered");
		}
	}

	private static boolean isMainAdvancement(AdvancementHolder holder) {
		ResourceLocation id = holder.id();
		return LibMisc.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith(MAIN_PREFIX) && !id.equals(ALL_STATS)
				&& holder.value().display().isPresent();
	}
}
