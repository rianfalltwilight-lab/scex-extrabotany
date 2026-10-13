package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.handler.EgoRequirementHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.forge.ForgeExtrabotanyConfig;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import java.util.UUID;

/**
 * "没有击败本我的玩家不能使用某些遗物"这条规则的回归测试。规则默认关闭。
 * Regression tests for the rule "a player that did not defeat the Ego cannot use some relics". The rule is off by default.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class EgoRequirementGameTests {
	private static final BlockPos STAND = new BlockPos(2, 2, 2);

	private EgoRequirementGameTests() {}

	private static Player mockPlayer(GameTestHelper helper, GameType gameType) {
		Player player = helper.makeMockPlayer(gameType);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(STAND)));
		return player;
	}

	/**
	 * 标签里是原版的九件遗物。规则默认关闭，这时什么也不发生。
	 * The tag has the nine relics of the original. The rule is off by default, and then nothing occurs.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void theRuleIsOffByDefault(GameTestHelper helper) {
		List<Item> relics = List.of(ExtraBotanyItems.coreOfTheVoid, ExtraBotanyItems.moonPendant, ExtraBotanyItems.sunRing,
				ExtraBotanyItems.voidArchives, ExtraBotanyItems.camera, ExtraBotanyItems.excalibur, ExtraBotanyItems.failnaught,
				ExtraBotanyItems.firstFractal, ExtraBotanyItems.manaRingMaster);
		for (Item relic : relics) {
			helper.assertTrue(new ItemStack(relic).is(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT), relic + " must be in the tag extrabotany:needs_ego_defeat");
		}
		helper.assertTrue(!new ItemStack(ExtraBotanyItems.trueTerrablade).is(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT)
				&& !new ItemStack(Items.DIAMOND_SWORD).is(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT), "Other items must not be in the tag");

		helper.assertTrue(!ExtraBotanyConfig.common().relicsNeedEgoDefeat(), "The rule must be off by default");
		Player player = mockPlayer(helper, GameType.SURVIVAL);
		ItemStack excalibur = new ItemStack(ExtraBotanyItems.excalibur);
		player.setItemInHand(InteractionHand.MAIN_HAND, excalibur);
		helper.assertTrue(!EgoRequirementHandler.isRestricted(excalibur) && !EgoRequirementHandler.isForbidden(player, excalibur),
				"With the rule off no item is restricted");
		EgoRequirementHandler.tickPlayer(player);
		PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
		EgoRequirementHandler.onInteract(event);
		helper.assertTrue(player.getMainHandItem() == excalibur && !event.isCanceled(), "With the rule off the relic must stay and the interaction must continue");
		helper.succeed();
	}

	/**
	 * 规则打开时：没有进度的玩家拿着、穿着和戴着的受限遗物被丢出，交互被取消；别的物品和创造模式的玩家不受影响；完成进度之后一切恢复。
	 * With the rule on: the restricted relics that a player with no advancement holds and wears are dropped, and the interaction is cancelled; other items and players in creative mode are not affected; after the advancement all is as usual.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void theRuleDropsRelicsUntilTheEgoIsDefeated(GameTestHelper helper) {
		boolean before = ExtraBotanyConfig.common().relicsNeedEgoDefeat();
		try {
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(true);

			// 模拟玩家没有进度。
			// A mock player has no advancements.
			Player player = mockPlayer(helper, GameType.SURVIVAL);
			ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.excalibur));
			player.setItemInHand(InteractionHand.OFF_HAND, sword);
			player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ExtraBotanyItems.camera));
			var curios = CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values().stream()
					.filter(handler -> handler.getStacks().getSlots() > 0).findFirst().orElseThrow().getStacks();
			curios.setStackInSlot(0, new ItemStack(ExtraBotanyItems.sunRing));
			// 背包里的遗物不受影响：规则只管拿着、穿着和戴着的。
			// A relic in the backpack is not affected: the rule is only for held and worn items.
			player.getInventory().setItem(12, new ItemStack(ExtraBotanyItems.failnaught));

			helper.assertTrue(EgoRequirementHandler.isForbidden(player, player.getMainHandItem()), "A player with no advancement must not use the relic");
			PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
			EgoRequirementHandler.onInteract(event);
			helper.assertTrue(event.isCanceled(), "The interaction with a restricted relic must be cancelled");
			PlayerInteractEvent.RightClickItem offHand = new PlayerInteractEvent.RightClickItem(player, InteractionHand.OFF_HAND);
			EgoRequirementHandler.onInteract(offHand);
			helper.assertTrue(!offHand.isCanceled(), "The interaction with a usual item must continue");

			EgoRequirementHandler.tickPlayer(player);
			helper.assertTrue(player.getMainHandItem().isEmpty() && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && curios.getStackInSlot(0).isEmpty(),
					"The relics in the hand, on the head and in the accessory slot must be dropped");
			helper.assertTrue(player.getOffhandItem() == sword, "A usual item must stay");
			helper.assertTrue(player.getInventory().getItem(12).is(ExtraBotanyItems.failnaught), "A relic in the backpack must stay");

			Player creative = mockPlayer(helper, GameType.CREATIVE);
			creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.excalibur));
			EgoRequirementHandler.tickPlayer(creative);
			helper.assertTrue(creative.getMainHandItem().is(ExtraBotanyItems.excalibur) && !EgoRequirementHandler.isForbidden(creative, creative.getMainHandItem()),
					"A player in creative mode is not affected");

			// 真正的玩家完成进度之后可以使用遗物。这名玩家不加入世界，原因见 AdvancementGameTests。
			// A real player can use the relics after the advancement. This player does not enter the level; AdvancementGameTests gives the cause.
			ServerPlayer winner = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
					new GameProfile(UUID.randomUUID(), "eb-test-winner"), ClientInformation.createDefault());
			winner.setPos(Vec3.atBottomCenterOf(helper.absolutePos(STAND)));
			ItemStack relic = new ItemStack(ExtraBotanyItems.excalibur);
			winner.setItemInHand(InteractionHand.MAIN_HAND, relic);
			helper.assertTrue(EgoRequirementHandler.isForbidden(winner, relic), "Before the advancement a real player must not use the relic");
			PlayerHelper.grantCriterion(winner, EgoRequirementHandler.ADVANCEMENT, "kill_ego");
			helper.assertTrue(!EgoRequirementHandler.isForbidden(winner, relic), "After the advancement the player can use the relic");
			EgoRequirementHandler.tickPlayer(winner);
			PlayerInteractEvent.RightClickItem allowed = new PlayerInteractEvent.RightClickItem(winner, InteractionHand.MAIN_HAND);
			EgoRequirementHandler.onInteract(allowed);
			helper.assertTrue(winner.getMainHandItem() == relic && !allowed.isCanceled(), "After the advancement the relic must stay and the interaction must continue");
		} finally {
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(before);
		}
		helper.succeed();
	}
}
