package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 原版三套盔甲和银翼射手的回归测试。
 * Regression tests for the three armor sets and the Silver Bullet of the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ArmorGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private ArmorGameTests() {}

	private static Item[] mikuSet() {
		return new Item[] { ExtraBotanyItems.mikuHelm, ExtraBotanyItems.mikuChest, ExtraBotanyItems.mikuLegs, ExtraBotanyItems.mikuBoots };
	}

	private static Item[] shootingGuardianSet() {
		return new Item[] {
				ExtraBotanyItems.shootingGuardianHelm, ExtraBotanyItems.shootingGuardianChest,
				ExtraBotanyItems.shootingGuardianLegs, ExtraBotanyItems.shootingGuardianBoots
		};
	}

	private static Item[] silentSagesSet() {
		return new Item[] {
				ExtraBotanyItems.silentSagesHelm, ExtraBotanyItems.silentSagesChest,
				ExtraBotanyItems.silentSagesLegs, ExtraBotanyItems.silentSagesBoots
		};
	}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(CENTER).getCenter());
		player.setNoGravity(true);
		return player;
	}

	/**
	 * 给玩家穿上盔甲，然后 tick 一次，让游戏应用盔甲的属性修饰符。
	 * Puts the armor on the player, then does one tick so that the game applies the attribute modifiers of the armor.
	 */
	private static void wear(Player player, Item... pieces) {
		for (Item piece : pieces) {
			player.setItemSlot(((ArmorItem) piece).getEquipmentSlot(), new ItemStack(piece));
		}
		player.tick();
	}

	private static void assertDefense(GameTestHelper helper, Item[] set, int... expected) {
		for (int i = 0; i < set.length; i++) {
			int defense = ((ArmorItem) set[i]).getDefense();
			helper.assertTrue(defense == expected[i], set[i] + " has " + defense + " armor, expected " + expected[i]);
			helper.assertTrue(((ArmorItem) set[i]).getEquipmentSlot() == ARMOR_SLOTS[i], set[i] + " is in the wrong slot");
		}
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void armorValuesMatchTheOriginal(GameTestHelper helper) {
		// 顺序是头盔、胸甲、护腿、靴子。
		// The sequence is helmet, chestplate, leggings, boots.
		assertDefense(helper, mikuSet(), 1, 5, 4, 2);
		assertDefense(helper, shootingGuardianSet(), 4, 8, 7, 3);
		assertDefense(helper, silentSagesSet(), 5, 9, 8, 4);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaDiscountNeedsTheFullSet(GameTestHelper helper) {
		ItemStack tool = new ItemStack(BotaniaItems.TERRA_BLADE);
		for (Item[] set : new Item[][] { mikuSet(), silentSagesSet(), shootingGuardianSet() }) {
			Player player = player(helper);
			wear(player, set[0], set[1], set[2]);
			float partial = ManaItemHandler.instance().getFullDiscountForTools(player, tool);
			helper.assertTrue(partial == 0F, "Three pieces of " + set[0] + " gave a discount of " + partial);

			wear(player, set[3]);
			float full = ManaItemHandler.instance().getFullDiscountForTools(player, tool);
			helper.assertTrue(Math.abs(full - 0.6F) < 1.0E-4F, "The full set of " + set[0] + " gave a discount of " + full);
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void silentSagesSetIsNotTheShadowWarriorSet(GameTestHelper helper) {
		// 原版的静默贤者引用了暗影武士的四件，所以套装永远凑不齐。
		// The Silent Sages of the original refers to the four Shadow Warrior pieces, so the set can never be complete.
		Player player = player(helper);
		wear(player, silentSagesSet());
		ItemStack tool = new ItemStack(BotaniaItems.TERRA_BLADE);
		helper.assertTrue(ManaItemHandler.instance().getFullDiscountForTools(player, tool) > 0F,
				"The full Silent Sages set is not recognized");

		wear(player, ExtraBotanyItems.mikuHelm);
		helper.assertTrue(ManaItemHandler.instance().getFullDiscountForTools(player, tool) == 0F,
				"A mixed set still gave the Silent Sages discount");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shootingGuardianPiecesGiveAttributes(GameTestHelper helper) {
		Player player = player(helper);
		player.tick();
		double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		double baseDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
		double baseAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);

		wear(player, ExtraBotanyItems.shootingGuardianChest);
		assertRatio(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED) / baseSpeed, 1.1, "movement speed with 1 piece");

		wear(player, shootingGuardianSet());
		assertRatio(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED) / baseSpeed, 1.4, "movement speed with 4 pieces");
		assertRatio(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE) / baseDamage, 1.4, "attack damage with 4 pieces");
		assertRatio(helper, player.getAttributeValue(Attributes.ATTACK_SPEED) / baseAttackSpeed, 1.12, "attack speed with 4 pieces");

		// 脱下盔甲后，加成必须消失。
		// When the armor is removed, the bonus must go away.
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			player.setItemSlot(slot, ItemStack.EMPTY);
		}
		player.tick();
		assertRatio(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED) / baseSpeed, 1.0, "movement speed with no armor");
		helper.succeed();
	}

	private static void assertRatio(GameTestHelper helper, double actual, double expected, String what) {
		helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, "Wrong " + what + ": " + actual + ", expected " + expected);
	}

	private static Zombie armoredZombie(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
		zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
		zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
		zombie.tick();
		return zombie;
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shootingGuardianSetIgnoresArmorAndStealsLife(GameTestHelper helper) {
		Player plain = player(helper);
		Zombie first = armoredZombie(helper);
		helper.assertTrue(first.getArmorValue() >= 20, "The test zombie has " + first.getArmorValue() + " armor");
		first.hurt(helper.getLevel().damageSources().playerAttack(plain), 10F);
		float reduced = first.getMaxHealth() - first.getHealth();
		helper.assertTrue(reduced > 0F && reduced < 5F, "Armor did not decrease the damage of a plain player: " + reduced);
		first.discard();

		Player guardian = player(helper);
		wear(guardian, shootingGuardianSet());
		guardian.setHealth(10F);
		Zombie second = armoredZombie(helper);
		second.hurt(helper.getLevel().damageSources().playerAttack(guardian), 10F);
		float full = second.getMaxHealth() - second.getHealth();
		helper.assertTrue(Math.abs(full - 10F) < 1.0E-4F, "The set did not ignore armor: " + full);
		helper.assertTrue(Math.abs(guardian.getHealth() - 12F) < 1.0E-4F,
				"Expected 20% life steal (12 health), found " + guardian.getHealth());
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shootingGuardianSetDecreasesHealing(GameTestHelper helper) {
		Player player = player(helper);
		wear(player, ExtraBotanyItems.shootingGuardianChest, ExtraBotanyItems.shootingGuardianLegs, ExtraBotanyItems.shootingGuardianBoots);
		player.setHealth(5F);
		player.heal(5F);
		helper.assertTrue(Math.abs(player.getHealth() - 10F) < 1.0E-4F, "Three pieces changed healing: " + player.getHealth());

		wear(player, ExtraBotanyItems.shootingGuardianHelm);
		player.setHealth(5F);
		player.heal(5F);
		helper.assertTrue(Math.abs(player.getHealth() - 6F) < 1.0E-4F,
				"Expected 20% healing (6 health), found " + player.getHealth());
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shootingGuardianSetDrawsBowFaster(GameTestHelper helper) {
		Player plain = player(helper);
		plain.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		plain.startUsingItem(InteractionHand.MAIN_HAND);
		int start = plain.getUseItemRemainingTicks();
		for (int i = 0; i < 10; i++) {
			plain.tick();
		}
		helper.assertTrue(start - plain.getUseItemRemainingTicks() == 10,
				"A plain player used " + (start - plain.getUseItemRemainingTicks()) + " ticks of draw time in 10 ticks");

		Player guardian = player(helper);
		wear(guardian, shootingGuardianSet());
		guardian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		guardian.startUsingItem(InteractionHand.MAIN_HAND);
		for (int i = 0; i < 10; i++) {
			guardian.tick();
		}
		helper.assertTrue(start - guardian.getUseItemRemainingTicks() == 20,
				"A player with the set used " + (start - guardian.getUseItemRemainingTicks()) + " ticks of draw time in 10 ticks");
		helper.succeed();
	}

	private static int burstCount(GameTestHelper helper, Player player) {
		return helper.getLevel().getEntitiesOfClass(ManaBurstEntity.class, new AABB(player.blockPosition()).inflate(16)).size();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void silverBulletIsFreeWithTheShootingGuardianSet(GameTestHelper helper) {
		Player player = player(helper);
		wear(player, shootingGuardianSet());
		ItemStack gun = new ItemStack(ExtraBotanyItems.silverBullet);
		player.setItemInHand(InteractionHand.MAIN_HAND, gun);
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(5000);
		player.getInventory().setItem(2, tablet);

		gun.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(burstCount(helper, player) == 1, "Expected 1 burst, found " + burstCount(helper, player));
		helper.assertTrue(mana.getMana() == 5000, "The shot took " + (5000 - mana.getMana()) + " mana");

		ManaBurstEntity burst = helper.getLevel().getEntitiesOfClass(ManaBurstEntity.class, new AABB(player.blockPosition()).inflate(16)).get(0);
		helper.assertTrue(burst.getMana() == 240, "The burst has " + burst.getMana() + " mana, expected 240");

		// 冷却期间不能再次射击。
		// A second shot is not possible during the cooldown.
		gun.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(burstCount(helper, player) == 1, "A shot was made during the cooldown");
		helper.succeed();
	}
}
