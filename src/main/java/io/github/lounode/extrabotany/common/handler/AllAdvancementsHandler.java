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

	private AllAdvancementsHandler() {}

	/**
	 * 玩家完成一个进度时调用。
	 * Call this when a player completes an advancement.
	 */
	public static void onAdvancementEarned(Player player, AdvancementHolder earned) {
		if (!(player instanceof ServerPlayer serverPlayer) || !PlayerHelper.isTruePlayer(player)) {
			return;
		}
		if (earned.id().equals(ALL_STATS)) {
			// 和原版相同：不管这个进度是怎么得到的，得到它的时候就给奖杯。
			// The same as in the original: the Trophy is given when the player gets this advancement, by any method.
			ItemEntity trophy = serverPlayer.spawnAtLocation(new ItemStack(ExtraBotanyBlocks.trophy), 0F);
			if (trophy != null) {
				trophy.setNoPickUpDelay();
			}
		} else if (isMainAdvancement(earned)) {
			check(serverPlayer);
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
