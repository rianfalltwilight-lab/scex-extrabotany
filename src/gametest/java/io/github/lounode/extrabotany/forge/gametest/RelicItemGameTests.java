package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.KingGardenItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.AllForOneRingItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SilentEternityItem;

import java.util.List;

/**
 * 王之花园、静籁永恒和全能指环的回归测试。
 * Regression tests for the Garden of King's Law, the Silent Eternity and the ALL FOR ONE ring.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class RelicItemGameTests {
	private static final Vec3 ARENA_CENTER = new Vec3(32, 4, 32);

	private RelicItemGameTests() {}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(ARENA_CENTER));
		player.setNoGravity(true);
		return player;
	}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static List<FlowerWeaponEntity> weapons(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(FlowerWeaponEntity.class,
				new AABB(BlockPos.containing(helper.absoluteVec(ARENA_CENTER))).inflate(30));
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
	}

	/**
	 * 把一个绑定给玩家的饰品放进玩家的第一个饰品栏。
	 * Puts an accessory that is bound to the player into the first accessory slot of the player.
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

	// ---------------------------------------------------------------------
	// 王之花园 / Garden of King's Law
	// ---------------------------------------------------------------------

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void kingGardenAcceptsEachOfTheSeventeenFlowers(GameTestHelper helper) {
		String[] flowers = {
				"extrabotany:blood_enchantress", "extrabotany:sunshine_lily", "extrabotany:moonlight_lily", "extrabotany:stardustlotus",
				"extrabotany:stonesia", "botania:entropinnyum", "botania:dreadthorne", "botania:medumone", "botania:thermalily",
				"botania:tigerseye", "botania:bellethorne", "botania:heisei_dream", "extrabotany:annoyingflower", "extrabotany:manalink",
				"extrabotany:omniviolet", "extrabotany:bellflower", "extrabotany:tinkle"
		};
		helper.assertTrue(flowers.length == KingGardenItem.WEAPON_TYPES, "The test list does not have 17 flowers");
		ItemStack garden = new ItemStack(ExtraBotanyItems.kingGarden);
		for (int type = 0; type < flowers.length; type++) {
			ItemStack flower = new ItemStack(item(flowers[type]));
			helper.assertFalse(flower.isEmpty(), "The flower item does not exist: " + flowers[type]);
			helper.assertTrue(KingGardenItem.getFlowerType(flower) == type, flowers[type] + " does not give weapon type " + type);
			helper.assertTrue(KingGardenItem.addFlower(garden, flower), "The garden did not accept " + flowers[type]);
		}
		helper.assertTrue(KingGardenItem.getTypes(garden).length == 17, "The garden did not keep all 17 flowers");
		helper.assertTrue(KingGardenItem.getFlowerType(new ItemStack(BotaniaItems.MANA_TABLET)) == -1, "The garden accepts an item that is not a flower");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void kingGardenSummonsWeaponsThatWaitForTheRelease(GameTestHelper helper) {
		Player player = player(helper);
		ManaItem mana = supplyMana(player, 5000);
		ItemStack garden = new ItemStack(ExtraBotanyItems.kingGarden);
		KingGardenItem.addFlower(garden, new ItemStack(item("extrabotany:stonesia")));
		KingGardenItem.addFlower(garden, new ItemStack(item("extrabotany:bellflower")));
		player.setItemInHand(InteractionHand.MAIN_HAND, garden);
		Item item = garden.getItem();

		garden.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(KingGardenItem.isCharging(garden), "The use did not start the charge");
		int duration = item.getUseDuration(garden, player);
		for (int i = 1; i <= 5; i++) {
			item.onUseTick(helper.getLevel(), player, garden, duration - i);
		}
		helper.assertTrue(weapons(helper).size() == 2, "Expected 2 weapons, found " + weapons(helper).size());
		helper.assertTrue(mana.getMana() == 5000 - 200, "Each weapon must take 100 mana: " + (5000 - mana.getMana()));
		List<Vec3> spawnPositions = weapons(helper).stream().map(Entity::position).toList();

		helper.runAfterDelay(5, () -> {
			// 玩家还在蓄力，所以武器停在原地。
			// The player still charges, so the weapons stay in place.
			List<Vec3> positions = weapons(helper).stream().map(Entity::position).toList();
			helper.assertTrue(positions.equals(spawnPositions), "The weapons moved during the charge");
			item.releaseUsing(garden, helper.getLevel(), player, duration - 10);
			helper.assertFalse(KingGardenItem.isCharging(garden), "The release did not end the charge");
		});
		helper.runAfterDelay(12, () -> {
			for (FlowerWeaponEntity weapon : weapons(helper)) {
				helper.assertTrue(spawnPositions.stream().noneMatch(pos -> pos.distanceTo(weapon.position()) < 1.0),
						"A weapon did not fly after the release");
			}
			helper.succeed();
		});
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void flowerWeaponDamagesItsTargetAndGivesItsEffect(GameTestHelper helper) {
		Player player = player(helper);
		Vec3 pos = player.position().add(8, 0, 0);
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(32, 4, 32));
		husk.setPos(pos);

		// 13 号武器（魔力链接花）：5 点魔法伤害，并给玩家力量 II。
		// Weapon 13 (Manalinkium): 5 magic damage, and Strength II for the player.
		FlowerWeaponEntity weapon = new FlowerWeaponEntity(helper.getLevel(), player, 13, 0, 0F);
		weapon.setPos(pos.add(0, 1, 0));
		helper.getLevel().addFreshEntity(weapon);

		helper.runAfterDelay(3, () -> {
			helper.assertTrue(weapon.isRemoved(), "The weapon did not go away after its hit");
			float lost = husk.getMaxHealth() - husk.getHealth();
			helper.assertTrue(Math.abs(lost - 5F) < 1.0E-4F, "Expected 5 magic damage, found " + lost);
			helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "The weapon did not give Strength to the player");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------------
	// 静籁永恒 / Silent Eternity
	// ---------------------------------------------------------------------

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void silentEternityIsAManaSourceAndStopsHealing(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ExtraBotanyItems.silentEternity);
		ManaItem reservoir = ManaItem.LOOKUP.find(stack);
		helper.assertTrue(reservoir instanceof SilentEternityItem.Reservoir, "The Silent Eternity has no mana interface");
		reservoir.addMana(10000);
		helper.assertTrue(reservoir.getMana() == 666 && reservoir.getMaxMana() == 666, "The reservoir does not hold exactly 666 mana");
		helper.assertTrue(reservoir.canSendRequestedManaToItem(ItemStack.EMPTY) && !reservoir.canReceiveManaFromPool(null),
				"The reservoir has the wrong mana transfer rules");

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		equip(player, ExtraBotanyItems.silentEternity);
		helper.runAfterDelay(2, () -> {
			player.setHealth(5F);
			player.heal(10F);
			helper.assertTrue(player.getHealth() == 5F, "A player with the Silent Eternity was healed: " + player.getHealth());
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------------
	// 全能指环 / ALL FOR ONE
	// ---------------------------------------------------------------------

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void allForOneSimulatesAccessoriesInTheBox(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack box = new ItemStack(BotaniaItems.TRINKET_CASE);
		SimpleContainer boxInventory = BaubleBoxItem.getInventory(box);
		boxInventory.setItem(0, new ItemStack(ExtraBotanyItems.aeroStone));
		// 精灵王指环只使用前 3 格，所以第 5 格的饰品不被模拟。
		// The Ring of Elven King only uses the first 3 slots, so the accessory in slot 5 is not simulated.
		boxInventory.setItem(4, new ItemStack(ExtraBotanyItems.aquaStone));
		boxInventory.setChanged();
		player.getInventory().setItem(5, box);
		double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);

		ItemStack ring = new ItemStack(ExtraBotanyItems.elvenKing);
		Relic.LOOKUP.find(ring).bindToUUID(player.getUUID());
		BaubleItem ringItem = (BaubleItem) ring.getItem();
		ringItem.onWornTick(ring, player);

		helper.assertTrue(AllForOneRingItem.find(stack -> stack.is(ExtraBotanyItems.aeroStone), player).is(ExtraBotanyItems.aeroStone),
				"The ring does not simulate the accessory in slot 1");
		helper.assertTrue(EquipmentHandler.findOrEmpty(ExtraBotanyItems.aeroStone, player).is(ExtraBotanyItems.aeroStone),
				"The equipment lookup of Botania does not find the simulated accessory");
		helper.assertTrue(AllForOneRingItem.find(stack -> stack.is(ExtraBotanyItems.aquaStone), player).isEmpty(),
				"The Ring of the Elven King simulates an accessory after slot 3");
		helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) > baseSpeed,
				"The attribute modifier of the simulated accessory was not applied");

		ringItem.onUnequipped(ring, player);
		helper.assertTrue(AllForOneRingItem.find(stack -> true, player).isEmpty(), "The simulation continued after the ring was taken off");
		helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed) < 1.0E-9,
				"The attribute modifier stayed after the ring was taken off");
		helper.succeed();
	}
}
