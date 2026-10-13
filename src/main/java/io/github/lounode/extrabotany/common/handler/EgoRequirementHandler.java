package io.github.lounode.extrabotany.common.handler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.proxy.Proxy;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * "进度要求"规则：没有击败本我的玩家不能使用某些遗物。这条规则默认关闭，用配置项 {@code relicsNeedEgoDefeat} 打开。
 * The "advancement requirement" rule: a player that did not defeat the Ego cannot use some relics. The rule is off by default, and the configuration option {@code relicsNeedEgoDefeat} turns it on.
 *
 * <p>规则打开时，对没有完成"击败本我"进度的非创造模式玩家：
 * <p>With the rule on, for a player that is not in creative mode and did not complete the advancement "defeat the Ego":
 * <ul>
 * <li>手里拿着受限物品时，交互被取消。
 * An interaction with a restricted item in the hand is cancelled.</li>
 * <li>拿在手里、穿在身上或者戴在饰品栏里的受限物品每 tick 都被丢到地上。
 * A restricted item that is held, worn or in an accessory slot is dropped on each tick.</li>
 * <li>物品的提示文字里有一行红字（见客户端的初始化代码）。
 * The tooltip of the item has a red line (see the client setup code).</li>
 * </ul>
 *
 * <p>受限的物品是标签 {@code extrabotany:needs_ego_defeat} 里的物品，默认是原版的那九件遗物。数据包可以修改这个标签。
 * <p>The restricted items are the items of the tag {@code extrabotany:needs_ego_defeat}; by default these are the nine relics of the original. A data pack can change this tag.
 *
 * <p>这个配置项不会从服务端同步到客户端。服务端决定物品能不能用；客户端的配置只影响提示文字和客户端的预判，所以两边应该设成一样。
 * <p>The option is not synchronized from the server to the client. The server decides if an item can be used; the option of the client only has an effect on the tooltip and on the prediction of the client, thus the two sides should have the same value.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code AdvancementHandler} 和 {@code IAdvancementRequirement}。
 * <p>Source: ExtraMeteorP 1.16 {@code AdvancementHandler} and {@code IAdvancementRequirement}.
 *
 * <p>和原版的差异：原版里这条规则总是生效，因为那九件遗物只能在击败本我之后得到。重制版里其中几件可以提前合成，所以这里把规则做成默认关闭的选项。原版用物品类上的接口标记受限物品，这里用标签。原版在客户端总是取消交互；这里客户端按自己知道的进度判断。
 * <p>Differences from the original: in the original the rule is always on, because the nine relics can only be obtained after the Ego is defeated. In Reburn some of them can be crafted earlier, thus the rule is an option that is off by default here. The original marks the restricted items with an interface on the item class, and this port uses a tag. The client of the original always cancels the interaction; here the client decides with the advancements that it knows.
 */
public final class EgoRequirementHandler {
	public static final ResourceLocation ADVANCEMENT = prefix("main/" + LibAdvancementNames.EGO_DEFEAT);
	public static final String MESSAGE = "extrabotany.message.needs_advancement";

	private static final EquipmentSlot[] SLOTS = { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

	private EgoRequirementHandler() {}

	public static boolean isEnabled() {
		return ExtraBotanyConfig.common().relicsNeedEgoDefeat();
	}

	/**
	 * 规则打开，并且这个物品在受限的标签里。
	 * The rule is on, and this item is in the tag of the restricted items.
	 */
	public static boolean isRestricted(ItemStack stack) {
		return !stack.isEmpty() && stack.is(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT) && isEnabled();
	}

	/**
	 * 这名玩家现在不能使用这个物品。
	 * This player cannot use this item at this time.
	 */
	public static boolean isForbidden(Player player, ItemStack stack) {
		return isRestricted(stack) && !player.isCreative() && !hasDefeatedEgo(player);
	}

	/**
	 * 提示文字里的那一行红字，也是物品被丢出时玩家看到的消息。
	 * The red line of the tooltip, which is also the message that the player sees when an item is dropped.
	 */
	public static Component message() {
		return Component.translatable(MESSAGE, Component.translatable("advancement.extrabotany:" + LibAdvancementNames.EGO_DEFEAT))
				.withStyle(ChatFormatting.RED);
	}

	private static boolean hasDefeatedEgo(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			return PlayerHelper.hasAdvancement(serverPlayer, ADVANCEMENT);
		}
		// 服务端上不是真正玩家的"玩家"（例如别的模组的假玩家）没有进度。
		// A "player" on the server that is not a real player (for example a fake player of a different mod) has no advancements.
		return player.level().isClientSide() && Proxy.INSTANCE.hasAdvancementOnClient(ADVANCEMENT);
	}

	/**
	 * 取消手里拿着受限物品时的交互。
	 * Cancels an interaction with a restricted item in the hand.
	 */
	public static <E extends PlayerInteractEvent & ICancellableEvent> void onInteract(E event) {
		Player player = event.getEntity();
		if (isForbidden(player, event.getItemStack())) {
			event.setCanceled(true);
			if (!player.level().isClientSide()) {
				player.displayClientMessage(message(), true);
			}
		}
	}

	/**
	 * 把玩家拿着、穿着和戴着的受限物品丢到地上。只在服务端调用。
	 * Drops the restricted items that the player holds and wears. Only call this on the server.
	 */
	public static void tickPlayer(Player player) {
		if (!isEnabled() || player.level().isClientSide() || player.isCreative() || player.isSpectator()) {
			return;
		}
		// 进度只在找到受限物品之后才查询，而且只查询一次。
		// The advancement is only read after a restricted item is found, and only one time.
		boolean[] allowed = { false };
		boolean[] examined = { false };
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = player.getItemBySlot(slot);
			if (mustDrop(player, stack, allowed, examined)) {
				player.setItemSlot(slot, ItemStack.EMPTY);
				drop(player, stack);
			}
		}
		if (allowed[0]) {
			return;
		}
		CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
			for (var handler : inventory.getCurios().values()) {
				var stacks = handler.getStacks();
				for (int slot = 0; slot < stacks.getSlots(); slot++) {
					ItemStack stack = stacks.getStackInSlot(slot);
					if (mustDrop(player, stack, allowed, examined)) {
						stacks.setStackInSlot(slot, ItemStack.EMPTY);
						drop(player, stack);
					}
				}
			}
		});
	}

	private static boolean mustDrop(Player player, ItemStack stack, boolean[] allowed, boolean[] examined) {
		if (allowed[0] || stack.isEmpty() || !stack.is(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT)) {
			return false;
		}
		if (!examined[0]) {
			examined[0] = true;
			allowed[0] = hasDefeatedEgo(player);
		}
		return !allowed[0];
	}

	private static void drop(Player player, ItemStack stack) {
		player.drop(stack, false);
		player.displayClientMessage(message(), true);
	}
}
