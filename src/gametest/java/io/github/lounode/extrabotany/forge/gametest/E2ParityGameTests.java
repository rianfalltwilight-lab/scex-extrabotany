package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.api.block.Pedestal;
import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.MirrowtuniaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.StonesiaBlockEntity;
import io.github.lounode.extrabotany.common.entity.AuraFireEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PureDaisyPendantItem;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.mixin.accessor.SpecialFlowerBlockEntityAccessor;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 对照原版审计第 E2 批：装备。两个原版都有的取 1.16，只有 1.12 有的取 1.12。每个测试注明原版出处。
 * Batch E2 of the parity audit with the original: the equipment. What both originals have follows 1.16, what only 1.12 has follows 1.12. Each test names its source in the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E2ParityGameTests {

	private E2ParityGameTests() {}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static List<ItemStack> wear(Player player, Item helmet, Item chest, Item legs, Item boots) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : List.of(helmet, chest, legs, boots)) {
			ItemStack stack = new ItemStack(item);
			player.setItemSlot(((ArmorItem) item).getEquipmentSlot(), stack);
			stacks.add(stack);
		}
		return stacks;
	}

	private static double sum(ItemStack stack, Holder<Attribute> attribute, AttributeModifier.Operation operation) {
		double total = 0;
		for (var entry : stack.getItem().getDefaultAttributeModifiers(stack).modifiers()) {
			if (entry.attribute().equals(attribute) && entry.modifier().operation() == operation) {
				total += entry.modifier().amount();
			}
		}
		return total;
	}

	private static void tickArmor(GameTestHelper h, Player player, List<ItemStack> stacks) {
		for (ItemStack stack : stacks) {
			stack.getItem().inventoryTick(stack, h.getLevel(), player, 0, false);
		}
	}

	// Original 1.16 ItemGoblinSlayerArmor and ItemMikuArmor: in the day with the full set, +50% base health and +4% base speed for each piece; 15% discount; piglins neutral; material 27 / {3,5,7,2} / 30 / 1.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void goblinSlayerFollows116(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		var stacks = wear(player, ExtraBotanyItems.goblinSlayerHelmet, ExtraBotanyItems.goblinSlayerChestplate,
				ExtraBotanyItems.goblinSlayerLeggings, ExtraBotanyItems.goblinSlayerBoots);
		var piece = (GoblinSlayerArmorItem) ExtraBotanyItems.goblinSlayerChestplate;
		h.assertTrue(piece.getDiscount(stacks.get(1), 0, player, null) == 0.15F, "The full set must give 15% for each piece");
		h.assertTrue(piece.makesPiglinsNeutral(stacks.get(1), player), "Piglins must stay neutral");
		tickArmor(h, player, stacks);
		h.assertTrue(GoblinSlayerArmorItem.isDayBonusActive(stacks.get(1)) == h.getLevel().isDay(), "The day flag does not follow the time");
		ItemStack chest = stacks.get(1);
		ItemNBTHelper.setBoolean(chest, GoblinSlayerArmorItem.TAG_DAY, true);
		h.assertTrue(sum(chest, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.5
				&& sum(chest, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.04, "Wrong day bonus");
		ItemNBTHelper.removeEntry(chest, GoblinSlayerArmorItem.TAG_DAY);
		h.assertTrue(sum(chest, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0, "The bonus stayed without the flag");
		player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
		tickArmor(h, player, stacks.subList(0, 3));
		h.assertFalse(GoblinSlayerArmorItem.isDayBonusActive(chest), "The bonus works without the full set");
		h.assertTrue(chest.getMaxDamage() == 27 * 16 && ((ArmorItem) chest.getItem()).getDefense() == 7, "Wrong material");
		h.succeed();
	}

	// Original 1.16 ItemShadowWarriorArmor: at night with the full set, +25% base health, +12.5% attack speed, +5% attack damage and Night Vision; no fixed +5 health.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shadowWarriorFollows116(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		var stacks = wear(player, ExtraBotanyItems.shadowWarriorHelmet, ExtraBotanyItems.shadowWarriorChestplate,
				ExtraBotanyItems.shadowWarriorLeggings, ExtraBotanyItems.shadowWarriorBoots);
		tickArmor(h, player, stacks);
		boolean night = !h.getLevel().isDay();
		h.assertTrue(ShadowWarriorArmorItem.isNightBonusActive(stacks.get(0)) == night, "The night flag does not follow the time");
		h.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION) == night, "Night Vision does not follow the night");
		ItemStack helmet = stacks.get(0);
		h.assertTrue(sum(helmet, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE) == 0, "The fixed +5 health of Reburn is still there");
		ItemNBTHelper.setBoolean(helmet, ShadowWarriorArmorItem.TAG_NIGHT, true);
		h.assertTrue(sum(helmet, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.25
				&& sum(helmet, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.125
				&& sum(helmet, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.05, "Wrong night bonus");
		h.assertTrue(helmet.getMaxDamage() == 24 * 11, "Wrong material");
		h.succeed();
	}

	// Original 1.16 ItemMaidArmor and ItemMaidHelm: +5 health for each piece; with the full set the helmet heals 1 for 20 mana and removes milk-curable harmful effects for 100 each every 40 ticks; environmental damage is ignored. Material 40 / {4,7,9,4} / 32 / 3.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void maidFollows116(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		ManaItem mana = supplyMana(player, 1000);
		var stacks = wear(player, ExtraBotanyItems.pleiadesCombatMaidHeadgear, ExtraBotanyItems.pleiadesCombatMaidSuit,
				ExtraBotanyItems.pleiadesCombatMaidSkirt, ExtraBotanyItems.pleiadesCombatMaidBoots);
		for (ItemStack stack : stacks) {
			h.assertTrue(sum(stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE) == 5, "Each piece must add 5 health");
			h.assertTrue(sum(stack, Attributes.KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE) == 0, "The knockback resistance of Reburn is still there");
		}
		var helmetItem = (PleiadesCombatMaidArmorItem) ExtraBotanyItems.pleiadesCombatMaidHeadgear;
		h.assertTrue(helmetItem.getDiscount(stacks.get(0), 0, player, null) == 0.15F, "The full set must give 15% for each piece");
		player.setHealth(10);
		player.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
		player.tickCount = 40;
		helmetItem.inventoryTick(stacks.get(0), h.getLevel(), player, 0, false);
		h.assertTrue(player.getHealth() == 11, "The helmet did not heal 1");
		h.assertFalse(player.hasEffect(MobEffects.POISON), "The helmet did not remove Poison");
		// Botania takes the discount of the four pieces (4 x 15%) from the costs, and the helmet sends 1 mana to the tablet.
		float multiplier = 1F - (0.15F + 0.15F + 0.15F + 0.15F);
		int expected = 1000 + 1 - (int) (20 * multiplier) - (int) (100 * multiplier);
		h.assertTrue(mana.getMana() == expected, "The helmet must use 20 + 100 less the discount and send 1, left " + mana.getMana() + ", expected " + expected);

		var fall = new LivingIncomingDamageEvent(player, new DamageContainer(player.damageSources().fall(), 5));
		PleiadesCombatMaidArmorItem.EventHandler.onPlayerAttacked(fall);
		h.assertTrue(fall.isCanceled(), "Fall damage was not ignored");
		var starve = new LivingIncomingDamageEvent(player, new DamageContainer(player.damageSources().starve(), 5));
		PleiadesCombatMaidArmorItem.EventHandler.onPlayerAttacked(starve);
		h.assertFalse(starve.isCanceled(), "Starvation is not in the list of 1.16");
		h.assertTrue(stacks.get(1).getMaxDamage() == 40 * 16 && ((ArmorItem) stacks.get(1).getItem()).getDefense() == 9, "Wrong material");
		h.succeed();
	}

	// Original 1.12 ItemCombatMaidChestDarkened and ItemCombatMaidArmor: at night the darkened chest gives 15 health, +30% speed, +100% attack damage and +20% attack speed.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void sanguineSuitHasTheNightBonusOf112(GameTestHelper h) {
		ItemStack suit = new ItemStack(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit);
		h.assertTrue(sum(suit, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE) == 5, "By day the suit must give 5 health");
		ItemNBTHelper.setBoolean(suit, SanguinePleiadesCombatMaidSuitItem.TAG_NIGHT, true);
		h.assertTrue(sum(suit, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE) == 15
				&& sum(suit, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.3
				&& sum(suit, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 1.0
				&& sum(suit, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE) == 0.2, "Wrong night bonus");
		h.succeed();
	}

	// Decision 6: only the Miku set stays; the Starry Idol set has no recipe and is not in the creative tab.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void starryIdolHasNoRecipeAndIsHidden(GameTestHelper h) {
		var recipes = h.getLevel().getRecipeManager();
		for (String id : List.of("starry_idol_headgear", "starry_idol_suit", "starry_idol_skirt", "starry_idol_boots")) {
			h.assertTrue(recipes.byKey(prefix(id)).isEmpty(), "The Starry Idol still has the recipe " + id);
		}
		h.assertTrue(recipes.byKey(prefix("miku_helm")).isPresent(), "The Miku set lost its recipe");
		List<ItemStack> shown = new ArrayList<>();
		CreativeModeTab.Output output = (stack, visibility) -> shown.add(stack);
		for (Item item : List.of(ExtraBotanyItems.starryIdolHeadgear, ExtraBotanyItems.starryIdolSuit, ExtraBotanyItems.mikuHelm)) {
			((StarryIdolArmorItem) item).addToCreativeTab(item, output);
		}
		h.assertTrue(shown.size() == 1 && shown.getFirst().is(ExtraBotanyItems.mikuHelm), "Only the Miku piece must be shown");
		h.succeed();
	}

	// Original 1.16 ModItems.NIGHTMAREFUEL_PROP and SPIRITFUEL_PROP.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void fuelsAreTheFoodsOf116(GameTestHelper h) {
		var nightmare = new ItemStack(ExtraBotanyItems.nightmareFuel).get(DataComponents.FOOD);
		var spirit = new ItemStack(ExtraBotanyItems.spiritFuel).get(DataComponents.FOOD);
		h.assertTrue(nightmare.nutrition() == 4 && spirit.nutrition() == 4, "Both fuels give 4 food");
		h.assertTrue(nightmare.effects().stream().anyMatch(e -> e.effect().getEffect().equals(MobEffects.HARM) && e.effect().getAmplifier() == 2),
				"Nightmare Fuel must give Instant Damage III");
		h.assertTrue(spirit.effects().stream().anyMatch(e -> e.effect().getEffect().equals(MobEffects.HEAL) && e.effect().getAmplifier() == 2),
				"Spirit Fuel must give Instant Health III");
		h.assertTrue(nightmare.effects().stream().filter(e -> !e.effect().getEffect().equals(MobEffects.HARM)).allMatch(e -> e.effect().getAmplifier() == 0),
				"The other effects must be level I");
		h.succeed();
	}

	// Original 1.16 EntityAuraFire: 5 + attack damage, armor piercing.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void jingweiFireIs5AndPiercing(GameTestHelper h) {
		h.assertTrue(AuraFireEntity.BASE_DAMAGE == 5F, "The fire must start from 5");
		var type = h.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ExtraBotanyDamageTypes.JINGWEI_PUNCH_DAMAGE);
		h.assertTrue(type.is(DamageTypeTags.BYPASSES_ARMOR), "The fire must pierce armor");
		h.assertFalse(type.is(DamageTypeTags.IS_FIRE), "1.16 did not use fire damage");
		h.succeed();
	}

	// Original 1.16 ModItems: the lenses do not stack, with the properties of 1.16.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void lensesDoNotStack(GameTestHelper h) {
		for (Item lens : List.of(ExtraBotanyItems.lensPush, ExtraBotanyItems.lensSmelt, ExtraBotanyItems.lensMana,
				ExtraBotanyItems.lensPotion, ExtraBotanyItems.lensTrace, ExtraBotanyItems.lensSuperconductor)) {
			h.assertTrue(new ItemStack(lens).getMaxStackSize() == 1, lens + " stacks");
		}
		ItemStack trace = new ItemStack(ExtraBotanyItems.lensTrace);
		h.assertTrue(((LensItem) ExtraBotanyItems.lensTrace).getProps(trace) == LensItem.PROP_CONTROL, "The Trace Lens must be a control lens");
		h.succeed();
	}

	// Original 1.16 CurioIntegration: two charm slots.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void twoCharmSlots(GameTestHelper h) {
		var charm = CuriosApi.getSlot("charm", h.getLevel());
		h.assertTrue(charm.isPresent() && charm.get().getSize() == 2, "The charm slot must have size 2");
		h.succeed();
	}

	// Original 1.12 ItemPureDaisyPendant: used on a block from the hand; 50 mana; every 64 uses a cooldown of 3000 ticks that only runs while worn.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void pureDaisyPendantIsUsedFromTheHand(GameTestHelper h) {
		BlockPos rel = new BlockPos(1, 1, 1);
		h.setBlock(rel, Blocks.STONE);
		Player player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
		ManaItem mana = supplyMana(player, 1000);
		ItemStack pendant = new ItemStack(ExtraBotanyItems.pureDaisyPendant);
		ItemNBTHelper.setInt(pendant, PureDaisyPendantItem.TAG_USE, 63);
		player.setItemInHand(InteractionHand.MAIN_HAND, pendant);
		BlockPos abs = h.absolutePos(rel);
		var context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
		pendant.getItem().useOn(context);
		h.assertTrue(h.getBlockState(rel).is(BotaniaBlocks.LIVINGROCK), "The stone did not become livingrock");
		h.assertTrue(mana.getMana() == 950, "The pendant must use 50 mana");
		h.assertTrue(PureDaisyPendantItem.getCooldown(pendant) == 3000, "The 64th use must start a cooldown of 3000");
		h.setBlock(rel, Blocks.STONE);
		pendant.getItem().useOn(context);
		h.assertTrue(h.getBlockState(rel).is(Blocks.STONE), "The pendant worked during its cooldown");
		((PureDaisyPendantItem) pendant.getItem()).onWornTick(pendant, player);
		h.assertTrue(PureDaisyPendantItem.getCooldown(pendant) == 2999, "The cooldown did not go down while worn");
		// The fake player is shared; leave its inventory empty.
		player.getInventory().clearContent();
		h.succeed();
	}

	// Original 1.12 SubTileGeneratingNature / SubTileFunctionalNature: an orb on a pedestal within 7 catalyses the Stonesia (+2, 25 energy) and the Mirrowtunia (Reflect V, 20 energy).
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void natureOrbCatalysesTheFlowersOf112(GameTestHelper h) {
		BlockPos pedestalPos = new BlockPos(6, 2, 2);
		h.setBlock(pedestalPos, ExtraBotanyBlocks.livingrockPedestal);
		ItemStack orb = new ItemStack(ExtraBotanyItems.natureOrb);
		EXplatAbstractions.INSTANCE.findNatureEnergyItem(orb).addEnergy(1000);
		((Pedestal) h.getBlockEntity(pedestalPos)).setItem(orb);
		ItemStack held = ((Pedestal) h.getBlockEntity(pedestalPos)).getItem();
		var energy = EXplatAbstractions.INSTANCE.findNatureEnergyItem(held);

		var stonesia = new StonesiaBlockEntity(h.absolutePos(new BlockPos(2, 2, 2)), ExtrabotanyFlowerBlocks.stonesia.defaultBlockState());
		stonesia.setLevel(h.getLevel());
		for (int i = 0; i < 20; i++) {
			((SpecialFlowerBlockEntityAccessor) stonesia).extrabotany_invokeTickFlower();
		}
		h.assertTrue(stonesia.isCatalysed() && energy.getEnergy() == 975, "The Stonesia must take 25 energy, left " + energy.getEnergy());

		var mirrow = new MirrowtuniaBlockEntity(h.absolutePos(new BlockPos(2, 2, 4)), ExtrabotanyFlowerBlocks.mirrowtunia.defaultBlockState());
		mirrow.setLevel(h.getLevel());
		mirrow.addMana(1000);
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		BlockPos abs = h.absolutePos(new BlockPos(3, 2, 4));
		player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
		h.getLevel().addFreshEntity(player);
		try {
			for (int i = 0; i < 50; i++) {
				((SpecialFlowerBlockEntityAccessor) mirrow).extrabotany_invokeTickFlower();
			}
			var effect = player.getEffect(ExtraBotanyMobEffects.THIRROR);
			h.assertTrue(effect != null && effect.getAmplifier() == 4, "A catalysed Mirrowtunia must give Reflect V");
		} finally {
			player.discard();
		}
		h.succeed();
	}
}
