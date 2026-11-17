package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2EquipmentStateGameTests {
	private static final List<Holder<Attribute>> ATTRIBUTES = List.of(Attributes.MOVEMENT_SPEED, Attributes.ATTACK_DAMAGE, Attributes.ARMOR);

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void allForOneAppliesAndRemovesEveryCommunityAttribute(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack box = new ItemStack(BotaniaItems.TRINKET_CASE);
		SimpleContainer contents = BaubleBoxItem.getInventory(box);
		contents.setItem(0, new ItemStack(ExtraBotanyItems.theCommunity));
		contents.setChanged();
		player.getInventory().setItem(5, box);
		ItemStack ring = new ItemStack(ExtraBotanyItems.elvenKing);
		Relic.LOOKUP.find(ring).bindToUUID(player.getUUID());
		BaubleItem item = (BaubleItem) ring.getItem();
		double[] before = ATTRIBUTES.stream().mapToDouble(player::getAttributeValue).toArray();
		try {
			item.onWornTick(ring, player);
			for (int i = 0; i < ATTRIBUTES.size(); i++) {
				helper.assertTrue(player.getAttributeValue(ATTRIBUTES.get(i)) > before[i], "Missing simulated attribute " + ATTRIBUTES.get(i));
			}
		} finally {
			item.onUnequipped(ring, player);
		}
		for (int i = 0; i < ATTRIBUTES.size(); i++) {
			helper.assertTrue(Math.abs(player.getAttributeValue(ATTRIBUTES.get(i)) - before[i]) < 1.0E-9, "Unequip leaked a simulated attribute");
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void allForOneRebuildRemovesAllPreviousAttributes(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack box = new ItemStack(BotaniaItems.TRINKET_CASE);
		SimpleContainer contents = BaubleBoxItem.getInventory(box);
		contents.setItem(0, new ItemStack(ExtraBotanyItems.theCommunity));
		contents.setChanged();
		player.getInventory().setItem(5, box);
		ItemStack ring = new ItemStack(ExtraBotanyItems.elvenKing);
		Relic.LOOKUP.find(ring).bindToUUID(player.getUUID());
		BaubleItem item = (BaubleItem) ring.getItem();
		double[] before = ATTRIBUTES.stream().mapToDouble(player::getAttributeValue).toArray();
		item.onWornTick(ring, player);
		contents.setItem(0, ItemStack.EMPTY);
		contents.setChanged();
		helper.runAfterDelay(1, () -> {
			try {
				item.onWornTick(ring, player);
				for (int i = 0; i < ATTRIBUTES.size(); i++) {
					helper.assertTrue(Math.abs(player.getAttributeValue(ATTRIBUTES.get(i)) - before[i]) < 1.0E-9, "Box rebuild leaked a simulated attribute");
				}
				helper.succeed();
			} finally {
				item.onUnequipped(ring, player);
			}
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void repairAndCooldownTicksDoNotRestartHeldItemAnimation(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem.LOOKUP.find(tablet).addMana(100000);
		player.getInventory().setItem(2, tablet);
		for (Item item : List.of(ExtraBotanyItems.trueTerrablade, ExtraBotanyItems.shadowKatana,
				ExtraBotanyItems.manasteelHammer, ExtraBotanyItems.manasteelShield,
				ExtraBotanyItems.rodOfDiscord, ExtraBotanyItems.excalibur)) {
			ItemStack stack = new ItemStack(item);
			Relic relic = Relic.LOOKUP.find(stack);
			if (relic != null) relic.bindToUUID(player.getUUID());
			stack.setDamageValue(10);
			ItemStack before = stack.copy();
			item.inventoryTick(stack, helper.getLevel(), player, 0, true);
			helper.assertTrue(stack.getDamageValue() < before.getDamageValue(), "Fixture must execute repair/cooldown tick: " + item);
			helper.assertFalse(item.shouldCauseReequipAnimation(before, stack, false), "Damage tick restarted animation: " + item);
			helper.assertTrue(item.shouldCauseReequipAnimation(before, stack, true), "Slot change must animate: " + item);
			ItemStack renamed = stack.copy();
			renamed.set(DataComponents.CUSTOM_NAME, Component.literal("changed"));
			helper.assertTrue(item.shouldCauseReequipAnimation(stack, renamed, false), "Other component changes must animate: " + item);
			ItemStack counted = stack.copyWithCount(2);
			helper.assertTrue(item.shouldCauseReequipAnimation(stack, counted, false), "Count changes must animate: " + item);
			helper.assertTrue(item.shouldCauseReequipAnimation(stack, new ItemStack(Items.STICK), false), "Item changes must animate: " + item);
		}
		helper.succeed();
	}
}
