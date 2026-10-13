package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.entity.ThunderSlashEntity;
import io.github.lounode.extrabotany.common.handler.HerrscherHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

/**
 * 雷之律者（征服宝石）的回归测试。
 * Regression tests for the Herrscher of Thunder (Gem of Conquest).
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class HerrscherGameTests {
	private HerrscherGameTests() {}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
		return player;
	}

	private static ItemStack wearGem(Player player) {
		ItemStack gem = new ItemStack(ExtraBotanyItems.gemOfConquest);
		var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
		var handler = inventory.getCurios().values().stream()
				.filter(candidate -> candidate.getStacks().getSlots() > 0)
				.findFirst().orElseThrow();
		handler.getStacks().setStackInSlot(0, gem);
		return gem;
	}

	private static Husk husk(GameTestHelper helper, double x, double z) {
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(2, 2, 2));
		husk.setPos(helper.absoluteVec(new Vec3(x, 2, z)));
		return husk;
	}

	private static List<ThunderSlashEntity> slashes(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(ThunderSlashEntity.class,
				new AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(8));
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void gemChargesEnergyToTheMaximum(GameTestHelper helper) {
		Player player = player(helper);
		ItemStack gem = new ItemStack(ExtraBotanyItems.gemOfConquest);
		BaubleItem item = (BaubleItem) gem.getItem();
		helper.assertTrue(HerrscherHandler.getEnergy(player) == 0, "A new player has energy");

		for (int i = 0; i < 10; i++) {
			item.onWornTick(gem, player);
		}
		helper.assertTrue(HerrscherHandler.getEnergy(player) == 20, "Expected 20 energy after 10 ticks, found " + HerrscherHandler.getEnergy(player));

		for (int i = 0; i < 400; i++) {
			item.onWornTick(gem, player);
		}
		helper.assertTrue(HerrscherHandler.getEnergy(player) == HerrscherHandler.MAX_ENERGY,
				"The energy went past the maximum: " + HerrscherHandler.getEnergy(player));
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void counterattackCancelsDamageAndUsesEnergy(GameTestHelper helper) {
		Player player = player(helper);
		Husk husk = husk(helper, 3.5, 2.5);
		wearGem(player);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));

		helper.runAfterDelay(2, () -> {
			// 能量不足时不反击。
			// With too little energy there is no counterattack.
			HerrscherHandler.setEnergy(player, HerrscherHandler.COUNTER_COST - 1);
			player.hurt(helper.getLevel().damageSources().mobAttack(husk), 2F);
			helper.assertTrue(player.getHealth() < player.getMaxHealth(), "The player took no damage with too little energy");
			helper.assertTrue(husk.getHealth() == husk.getMaxHealth(), "The player made a counterattack with too little energy");

			player.setHealth(player.getMaxHealth());
			player.invulnerableTime = 0;
			HerrscherHandler.setEnergy(player, HerrscherHandler.COUNTER_COST + 50);
			player.hurt(helper.getLevel().damageSources().mobAttack(husk), 6F);
			helper.assertTrue(player.getHealth() == player.getMaxHealth(), "The counterattack did not cancel the damage");
			helper.assertTrue(husk.getHealth() < husk.getMaxHealth(), "The counterattack did not hit the attacker");
			helper.assertTrue(HerrscherHandler.getEnergy(player) == 50, "Expected 50 energy after the counterattack, found " + HerrscherHandler.getEnergy(player));

			// 挥剑动作还没有结束，所以第二次攻击不能被反击。
			// The swing is not complete, so the second attack cannot be countered.
			HerrscherHandler.setEnergy(player, HerrscherHandler.MAX_ENERGY);
			player.hurt(helper.getLevel().damageSources().mobAttack(husk), 6F);
			helper.assertTrue(player.getHealth() < player.getMaxHealth(), "A second counterattack occurred during the swing");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void counterattackNeedsASword(GameTestHelper helper) {
		Player player = player(helper);
		Husk husk = husk(helper, 3.5, 2.5);
		wearGem(player);

		helper.runAfterDelay(2, () -> {
			HerrscherHandler.setEnergy(player, HerrscherHandler.MAX_ENERGY);
			player.hurt(helper.getLevel().damageSources().mobAttack(husk), 6F);
			helper.assertTrue(player.getHealth() < player.getMaxHealth(), "A player with no sword made a counterattack");
			helper.assertTrue(HerrscherHandler.getEnergy(player) == HerrscherHandler.MAX_ENERGY, "A failed counterattack used energy");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 60)
	public static void thunderSlashNeedsFullEnergyAndKillsAHusk(GameTestHelper helper) {
		Player player = player(helper);
		// 玩家向下看，所以心斩出现在测试区域里面。
		// The player looks down, so the Thunder Slash appears in the test area.
		player.setXRot(90F);
		Husk husk = husk(helper, 2.5, 3.5);
		wearGem(player);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));

		helper.runAfterDelay(2, () -> {
			HerrscherHandler.setEnergy(player, HerrscherHandler.MAX_ENERGY - 1);
			helper.assertFalse(HerrscherHandler.useSkill(player), "The skill worked with 599 energy");
			helper.assertTrue(slashes(helper).isEmpty(), "A slash appeared with 599 energy");

			HerrscherHandler.setEnergy(player, HerrscherHandler.MAX_ENERGY);
			helper.assertTrue(HerrscherHandler.useSkill(player), "The skill did not work with full energy");
			helper.assertTrue(HerrscherHandler.getEnergy(player) == 0, "The skill did not use all the energy");
			helper.assertTrue(slashes(helper).size() == 1, "Expected 1 slash, found " + slashes(helper).size());
			helper.assertFalse(HerrscherHandler.useSkill(player), "A second slash was released with no energy");
		});
		helper.runAfterDelay(6, () ->
				helper.assertTrue(husk.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "The slash did not slow the monster"));
		helper.runAfterDelay(2 + ThunderSlashEntity.LIFETIME_TICKS + 3, () -> {
			helper.assertTrue(slashes(helper).isEmpty(), "The slash did not go away after 35 ticks");
			// 五次伤害一共 21 点，尸壳有 20 点生命值。
			// The five hits do 21 damage in total, and a husk has 20 health.
			helper.assertTrue(husk.isDeadOrDying(), "The five thunder hits did not kill the husk: " + husk.getHealth());
			helper.succeed();
		});
	}
}
