package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.neoforge.mana.ManaDiscountEvent;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.AquaStoneItem;

/**
 * 伤害系统和原版饰品的回归测试。
 * Regression tests for the damage system and the baubles of the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class BaubleGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);

	private BaubleGameTests() {}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(CENTER).getCenter());
		return player;
	}

	/**
	 * 在测试区域中心生成一个不会移动的生物。
	 * Spawns a mob that does not move at the center of the test area.
	 */
	private static <T extends Mob> T mob(GameTestHelper helper, EntityType<T> type) {
		T mob = helper.spawnWithNoFreeWill(type, CENTER);
		mob.setNoGravity(true);
		return mob;
	}

	/**
	 * 给玩家一块魔力石板，里面有指定数量的魔力。
	 * Gives the player a Mana Tablet with the given quantity of mana.
	 */
	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	/**
	 * 把饰品放进玩家的第一个饰品栏。
	 * Puts the bauble into the first bauble slot of the player.
	 */
	private static ItemStack equip(Player player, Item bauble) {
		ItemStack stack = new ItemStack(bauble);
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null) {
			relic.bindToUUID(player.getUUID());
		}
		var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
		var handler = inventory.getCurios().values().stream()
				.filter(candidate -> candidate.getStacks().getSlots() > 0)
				.findFirst().orElseThrow();
		handler.getStacks().setStackInSlot(0, stack);
		return stack;
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void piercingDamageIgnoresArmor(GameTestHelper helper) {
		Player attacker = player(helper);
		var armored = mob(helper, EntityType.ZOMBIE);
		armored.getAttribute(Attributes.ARMOR).setBaseValue(20);

		float before = armored.getHealth();
		helper.assertTrue(DamageHandler.INSTANCE.dmg(armored, attacker, 5F, DamageKind.NEUTRAL_PIERCING),
				"Piercing damage was refused");
		helper.assertTrue(Math.abs(before - armored.getHealth() - 5F) < 0.001F,
				"Armor decreased piercing damage: " + (before - armored.getHealth()));

		// 穿透伤害会清除无敌帧，所以紧接着的第二次伤害也必须生效。
		// Piercing damage clears the invulnerability time, so a second hit immediately after must also apply.
		before = armored.getHealth();
		DamageHandler.INSTANCE.dmg(armored, attacker, 4F, DamageKind.MAGIC_PIERCING);
		helper.assertTrue(Math.abs(before - armored.getHealth() - 4F) < 0.001F,
				"Invulnerability time stopped magic piercing damage: " + (before - armored.getHealth()));
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void neutralDamageRespectsArmor(GameTestHelper helper) {
		Player attacker = player(helper);
		var armored = mob(helper, EntityType.ZOMBIE);
		armored.getAttribute(Attributes.ARMOR).setBaseValue(20);

		float before = armored.getHealth();
		DamageHandler.INSTANCE.dmg(armored, attacker, 5F, DamageKind.NEUTRAL);
		float dealt = before - armored.getHealth();
		helper.assertTrue(dealt > 0F && dealt < 5F, "Armor must decrease neutral damage, dealt " + dealt);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void lifeLosingLeavesOneHealth(GameTestHelper helper) {
		Player attacker = player(helper);
		var target = mob(helper, EntityType.ZOMBIE);

		DamageHandler.INSTANCE.dmg(target, attacker, 1000F, DamageKind.LIFE_LOSING);
		helper.assertTrue(target.isAlive() && target.getHealth() <= 1F,
				"Life loss must leave the target alive with at most 1 health, but health is " + target.getHealth());
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void monstersOnlyDamagePlayers(GameTestHelper helper) {
		Player player = player(helper);
		var zombie = mob(helper, EntityType.ZOMBIE);
		var cow = mob(helper, EntityType.COW);

		helper.assertTrue(DamageHandler.INSTANCE.checkPassable(player, zombie), "A monster must be able to damage a player");
		helper.assertTrue(!DamageHandler.INSTANCE.checkPassable(cow, zombie), "A monster must not damage other mobs");
		helper.assertTrue(!DamageHandler.INSTANCE.checkPassable(zombie, zombie), "An entity must not damage itself");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void peaceAmuletProtectsPassiveMobs(GameTestHelper helper) {
		Player player = player(helper);
		var zombie = mob(helper, EntityType.ZOMBIE);
		var cow = mob(helper, EntityType.COW);
		helper.assertTrue(DamageHandler.INSTANCE.checkPassable(cow, player), "Without the amulet a player can damage a cow");

		equip(player, ExtraBotanyItems.peaceAmulet);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(!DamageHandler.INSTANCE.checkPassable(cow, player), "The amulet must protect passive mobs");
			helper.assertTrue(DamageHandler.INSTANCE.checkPassable(zombie, player), "The amulet must not protect monsters");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void elementalStonesExcludeTheCommunity(GameTestHelper helper) {
		Player player = player(helper);
		var aero = (BaubleItem) ExtraBotanyItems.aeroStone;
		var community = (BaubleItem) ExtraBotanyItems.theCommunity;
		helper.assertTrue(aero.canEquip(new ItemStack(aero), player), "A stone must be equippable on an empty player");

		equip(player, community);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(!aero.canEquip(new ItemStack(aero), player), "A stone was allowed together with The Community");
			helper.assertTrue(!community.canEquip(new ItemStack(community), player), "A second Community was allowed");

			var event = new ManaDiscountEvent(player, 0F, ItemStack.EMPTY);
			AquaStoneItem.onManaDiscount(event);
			helper.assertTrue(Math.abs(event.getDiscount() - 0.1F) < 0.0001F, "The Community must give the Aqua Stone discount");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void theCommunityCombinesStoneAttributes(GameTestHelper helper) {
		var community = (BaubleItem) ExtraBotanyItems.theCommunity;
		var modifiers = community.getEquippedAttributeModifiers(new ItemStack(community),
				ResourceLocation.fromNamespaceAndPath("extrabotany", "test_slot"));
		helper.assertTrue(modifiers.get(Attributes.ARMOR).stream().anyMatch(m -> m.amount() == 4), "Missing Earth Stone armor");
		helper.assertTrue(modifiers.get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> Math.abs(m.amount() - 0.10) < 1e-9),
				"Missing Ignis Stone damage");
		helper.assertTrue(modifiers.get(Attributes.MOVEMENT_SPEED).stream().anyMatch(m -> Math.abs(m.amount() - 0.15) < 1e-9),
				"Missing Aero Stone speed");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void deathRingChargesOnlyWhenItActs(GameTestHelper helper) {
		Player player = player(helper);
		ManaItem mana = supplyMana(player, 1000);
		var target = mob(helper, EntityType.ZOMBIE);
		var ring = (BaubleItem) ExtraBotanyItems.deathRing;
		ItemStack stack = new ItemStack(ring);

		player.tickCount = 29;
		ring.onWornTick(stack, player);
		helper.assertTrue(mana.getMana() == 1000 && !target.hasEffect(MobEffects.WITHER),
				"The ring acted or charged mana outside its interval");

		float before = target.getHealth();
		player.tickCount = 30;
		ring.onWornTick(stack, player);
		helper.assertTrue(mana.getMana() == 920, "The ring must charge 80 mana for one target, mana is " + mana.getMana());
		helper.assertTrue(target.hasEffect(MobEffects.WITHER) && target.hasEffect(MobEffects.UNLUCK), "Missing Wither or Bad Luck");
		helper.assertTrue(before - target.getHealth() >= 0.5F, "The ring did not decrease health");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void frostStarSlowsEnemiesForMana(GameTestHelper helper) {
		Player player = player(helper);
		ManaItem mana = supplyMana(player, 1000);
		var target = mob(helper, EntityType.ZOMBIE);
		var star = (BaubleItem) ExtraBotanyItems.frostStar;

		player.tickCount = 20;
		star.onWornTick(new ItemStack(star), player);
		var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
		helper.assertTrue(slow != null && slow.getAmplifier() == 4, "The star did not apply Slowness V");
		helper.assertTrue(mana.getMana() == 970, "The star must charge 30 mana for one target, mana is " + mana.getMana());
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void potatoChipsPreventDeathOnce(GameTestHelper helper) {
		Player player = player(helper);
		ManaItem mana = supplyMana(player, 5000);
		equip(player, ExtraBotanyItems.potatoChips);

		helper.runAfterDelay(2, () -> {
			player.hurt(player.damageSources().generic(), 1000F);
			helper.assertTrue(player.isAlive() && player.getHealth() == 5F, "The chips did not prevent the death, health is " + player.getHealth());
			helper.assertTrue(mana.getMana() == 2000, "The rescue must cost 3000 mana, mana is " + mana.getMana());
			helper.assertTrue(player.getCooldowns().isOnCooldown(ExtraBotanyItems.potatoChips), "The rescue did not start a cooldown");
			helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "The rescue did not give Regeneration");

			// 冷却期间不会再次保命。
			// During the cooldown the chips do not prevent a death again.
			player.invulnerableTime = 0;
			player.hurt(player.damageSources().generic(), 1000F);
			helper.assertTrue(player.isDeadOrDying(), "The chips prevented a death during the cooldown");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void moonPendantMakesTheWearerFireproof(GameTestHelper helper) {
		Player player = player(helper);
		helper.assertTrue(!player.isInvulnerableTo(player.damageSources().onFire()), "A player is fireproof without the pendant");

		equip(player, ExtraBotanyItems.moonPendant);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(player.isInvulnerableTo(player.damageSources().onFire()), "The pendant did not stop fire damage");
			helper.assertTrue(!player.isInvulnerableTo(player.damageSources().generic()), "The pendant stopped damage that is not fire");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void sunRingExtendsBlockReachOnly(GameTestHelper helper) {
		var ring = (BaubleItem) ExtraBotanyItems.sunRing;
		var modifiers = ring.getEquippedAttributeModifiers(new ItemStack(ring),
				ResourceLocation.fromNamespaceAndPath("extrabotany", "test_slot"));
		helper.assertTrue(modifiers.get(Attributes.BLOCK_INTERACTION_RANGE).stream().anyMatch(m -> m.amount() == 3.5),
				"Missing block reach bonus");
		helper.assertTrue(modifiers.get(Attributes.ENTITY_INTERACTION_RANGE).isEmpty(),
				"The original ring does not extend the attack range");
		helper.assertTrue(modifiers.get(PixieHandler.PIXIE_SPAWN_CHANCE).stream().anyMatch(m -> m.amount() == 0.25),
				"Missing pixie chance bonus");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void bottledFlamePlacesATorchInTheDark(GameTestHelper helper) {
		// 用石头围出一个没有光的小房间。
		// Make a small room with no light from stone.
		for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(3, 4, 3))) {
			boolean interior = pos.getX() == 2 && pos.getZ() == 2 && (pos.getY() == 2 || pos.getY() == 3);
			helper.setBlock(pos, interior ? Blocks.AIR : Blocks.STONE);
		}
		Player player = player(helper);
		player.getInventory().setItem(5, new ItemStack(Items.TORCH, 3));
		var flame = (BaubleItem) ExtraBotanyItems.bottledFlame;

		helper.runAfterDelay(5, () -> {
			player.tickCount = 10;
			flame.onWornTick(new ItemStack(flame), player);
			helper.assertBlockPresent(Blocks.TORCH, CENTER);
			helper.assertTrue(player.getInventory().getItem(5).getCount() == 2, "The torch did not come from the inventory");
			helper.assertTrue(player.getOffhandItem().isEmpty(), "The off hand item was not restored");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void gemOfConquestHasNoAttributeBonus(GameTestHelper helper) {
		var gem = (BaubleItem) ExtraBotanyItems.gemOfConquest;
		var modifiers = gem.getEquippedAttributeModifiers(new ItemStack(gem),
				ResourceLocation.fromNamespaceAndPath("extrabotany", "test_slot"));
		helper.assertTrue(modifiers.isEmpty(), "The original gem gives no attribute bonus");
		helper.succeed();
	}

	/**
	 * 确认新类没有改变旧存档里的注册 ID。
	 * Makes sure that the new classes did not change the registry IDs in old saves.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void baubleRegistryIdsAreStable(GameTestHelper helper) {
		assertId(helper, ExtraBotanyItems.aeroStone, "aero_stone");
		assertId(helper, ExtraBotanyItems.aquaStone, "aqua_stone");
		assertId(helper, ExtraBotanyItems.earthStone, "earth_stone");
		assertId(helper, ExtraBotanyItems.ignisStone, "ignis_stone");
		assertId(helper, ExtraBotanyItems.theCommunity, "the_community");
		assertId(helper, ExtraBotanyItems.peaceAmulet, "peace_amulet");
		assertId(helper, ExtraBotanyItems.frostStar, "frost_star");
		assertId(helper, ExtraBotanyItems.deathRing, "death_ring");
		assertId(helper, ExtraBotanyItems.powerGlove, "power_glove");
		assertId(helper, ExtraBotanyItems.potatoChips, "potato_chips");
		assertId(helper, ExtraBotanyItems.gemOfConquest, "gem_of_conquest");
		assertId(helper, ExtraBotanyItems.bottledFlame, "bottled_flame");
		assertId(helper, ExtraBotanyItems.sunRing, "sun_ring");
		assertId(helper, ExtraBotanyItems.moonPendant, "moon_pendant");
		assertId(helper, ExtraBotanyItems.superCrown, "super_crown");
		assertId(helper, ExtraBotanyItems.redScarf, "red_scarf");
		helper.succeed();
	}

	private static void assertId(GameTestHelper helper, Item item, String path) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
		helper.assertTrue(id.getNamespace().equals("extrabotany") && id.getPath().equals(path),
				"Expected extrabotany:" + path + " but found " + id);
	}
}

