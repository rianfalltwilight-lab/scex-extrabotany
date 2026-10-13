package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.attachment.FlamescionState;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSlashEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSwordEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionVoidEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.StrengthenSlashEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.FlamescionHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

/**
 * 薪炎之钥的回归测试。
 * Regression tests for the Key of Flamescion.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class FlamescionGameTests {
	private FlamescionGameTests() {}

	/**
	 * 生成一个悬在空中的玩家，主手拿着薪炎之钥，攻击冷却已满。
	 * Makes a player that floats in the air with the Key of Flamescion in the main hand and a full attack cooldown.
	 */
	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(new Vec3(2.5, 3, 2.5)));
		player.setNoGravity(true);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.flamescionWeapon));
		chargeAttack(player);
		return player;
	}

	private static void chargeAttack(Player player) {
		for (int i = 0; i < 30; i++) {
			player.tick();
		}
	}

	/**
	 * 让玩家进入薪炎模式：离地，并且有"灼热模式"效果。
	 * Puts the player into the Flamescion mode: off the ground and with the Incandescence Mode effect.
	 */
	private static void enterMode(Player player) {
		player.setOnGround(false);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 1000));
	}

	private static Husk husk(GameTestHelper helper, double x, double y, double z) {
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(2, 2, 2));
		husk.setPos(helper.absoluteVec(new Vec3(x, y, z)));
		return husk;
	}

	private static <T extends Entity> List<T> find(GameTestHelper helper, Class<T> type) {
		return helper.getLevel().getEntitiesOfClass(type, new AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(12));
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void energyChargesToAnOverloadAndRecovers(GameTestHelper helper) {
		Player player = player(helper);
		enterMode(player);
		helper.assertTrue(FlamescionHandler.isFlamescionMode(player), "The player is not in the Flamescion mode");

		for (int i = 0; i < 300; i++) {
			FlamescionHandler.tickPlayer(player);
		}
		helper.assertTrue(FlamescionHandler.getEnergy(player) == 600 && !FlamescionHandler.isOverloaded(player),
				"Expected 600 energy and no overload after 300 ticks, found " + FlamescionHandler.getEnergy(player));

		FlamescionHandler.tickPlayer(player);
		helper.assertTrue(FlamescionHandler.isOverloaded(player) && FlamescionHandler.getEnergy(player) == 597,
				"The overload did not start at full energy: " + FlamescionHandler.getEnergy(player));
		helper.assertFalse(FlamescionHandler.isFlamescionMode(player), "An overloaded player is still in the Flamescion mode");

		for (int i = 0; i < 200; i++) {
			FlamescionHandler.tickPlayer(player);
		}
		helper.assertTrue(!FlamescionHandler.isOverloaded(player) && FlamescionHandler.getEnergy(player) == 0,
				"The overload did not end at zero energy: " + FlamescionHandler.getEnergy(player));
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void stateBelongsToThePlayerAndIsSaved(GameTestHelper helper) {
		Player player = player(helper);
		FlamescionState state = FlamescionHandler.getState(player);
		state.setEnergy(600);
		state.setOverloaded(true);

		// 换一把武器不能绕过过载。
		// A different weapon cannot bypass the overload.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.flamescionWeapon));
		helper.assertTrue(FlamescionHandler.isOverloaded(player), "A different weapon removed the overload");

		Player restored = helper.makeMockPlayer(GameType.SURVIVAL);
		restored.load(player.saveWithoutId(new CompoundTag()));
		helper.assertTrue(FlamescionHandler.isOverloaded(restored) && FlamescionHandler.getEnergy(restored) == 600,
				"The Flamescion state was lost on a save and a load");

		// 把武器收起来之后，过载仍然继续冷却。
		// The overload continues to cool down after the weapon is put away.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		for (int i = 0; i < 201; i++) {
			FlamescionHandler.tickPlayer(player);
		}
		helper.assertFalse(FlamescionHandler.isOverloaded(player), "The overload did not cool down with the weapon put away");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void oldSaveDataIsMigrated(GameTestHelper helper) {
		Player player = player(helper);
		CompoundTag legacy = new CompoundTag();
		legacy.putInt("energy", 300);
		legacy.putBoolean("overloaded", true);
		player.getPersistentData().put("extrabotany:flamescion", legacy);

		FlamescionHandler.migrateLegacyState(player);
		helper.assertTrue(FlamescionHandler.getEnergy(player) == 300 && FlamescionHandler.isOverloaded(player),
				"The old player state was not migrated");
		helper.assertFalse(player.getPersistentData().contains("extrabotany:flamescion"), "The old player state was not removed");

		ItemStack stack = new ItemStack(ExtraBotanyItems.flamescionWeapon);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putInt("flamescion_energy", 5);
			tag.putBoolean("flamescion_overloaded", true);
			tag.putInt("other", 1);
		});
		stack.getItem().inventoryTick(stack, helper.getLevel(), player, 0, true);
		CompoundTag remaining = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		helper.assertTrue(!remaining.contains("flamescion_energy") && !remaining.contains("flamescion_overloaded"),
				"The old item state was not removed");
		helper.assertTrue(remaining.getInt("other") == 1, "The cleanup removed custom data that does not belong to the Flamescion");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void ignitionThrowsThePlayerAndNearbyMobs(GameTestHelper helper) {
		Player player = player(helper);
		Husk husk = husk(helper, 3.5, 3, 2.5);
		player.setOnGround(true);
		player.setShiftKeyDown(true);
		ItemStack stack = player.getMainHandItem();

		helper.assertTrue(stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
				"The sneak-use did not start the ignition");
		helper.assertTrue(player.getDeltaMovement().y == 1.0 && !player.onGround(), "The ignition did not throw the player");
		helper.assertTrue(player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE), "The ignition did not give Incandescence");
		helper.assertTrue(husk.getDeltaMovement().y == 1.0 && husk.hasEffect(ExtraBotanyMobEffects.TIMELOCK),
				"The ignition did not throw and lock the nearby monster");
		player.setShiftKeyDown(false);
		helper.assertTrue(FlamescionHandler.isFlamescionMode(player), "The player is not in the Flamescion mode after the ignition");

		// 灼热模式效果让下落速度减到 5%，并且在落地后消失。
		// Incandescence Mode decreases the fall speed to 5%, and goes away after a landing.
		player.setDeltaMovement(0.4, -1.0, 0.2);
		helper.assertTrue(ExtraBotanyMobEffects.INCANDESCENCE.value().applyEffectTick(player, 0), "Incandescence ended in the air");
		Vec3 motion = player.getDeltaMovement();
		helper.assertTrue(Math.abs(motion.y + 0.05) < 1.0E-8 && motion.x == 0.4, "Incandescence did not slow the fall: " + motion);
		player.setOnGround(true);
		helper.assertFalse(ExtraBotanyMobEffects.INCANDESCENCE.value().applyEffectTick(player, 0), "Incandescence continued on the ground");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void swingsReleaseSwordsAndStrengthenedSlashes(GameTestHelper helper) {
		Player player = player(helper);
		LeftClickItem item = (LeftClickItem) player.getMainHandItem().getItem();

		item.onLeftClick(player, null);
		helper.assertTrue(find(helper, FlamescionSwordEntity.class).isEmpty(), "A sword was released outside the Flamescion mode");

		enterMode(player);
		item.onLeftClick(player, null);
		helper.assertTrue(find(helper, FlamescionSwordEntity.class).size() == 1, "Expected 1 Flamescion Sword");
		// 对空挥剑之后攻击冷却被重置，所以同一 tick 的第二次点击没有效果。
		// The attack cooldown is reset after a swing at the air, so a second click in the same tick has no effect.
		item.onLeftClick(player, null);
		helper.assertTrue(find(helper, FlamescionSwordEntity.class).size() == 1, "A second click in the same tick released a sword");

		chargeAttack(player);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200));
		item.onLeftClick(player, null);
		helper.assertTrue(find(helper, StrengthenSlashEntity.class).size() == 3,
				"Expected 3 Strengthened Slashes, found " + find(helper, StrengthenSlashEntity.class).size());
		helper.assertFalse(player.hasEffect(ExtraBotanyMobEffects.FLAMESCION), "The slashes did not use up the Flamescion Strength effect");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void voidAndUltimateNeedTheFlamescionMode(GameTestHelper helper) {
		Player player = player(helper);
		ItemStack stack = player.getMainHandItem();
		helper.assertFalse(FlamescionHandler.useUltimate(player), "The ultimate worked outside the Flamescion mode");
		stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(find(helper, FlamescionVoidEntity.class).isEmpty(), "A void appeared outside the Flamescion mode");

		enterMode(player);
		stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(find(helper, FlamescionVoidEntity.class).size() == 1, "Expected 1 Flamescion Void");
		helper.assertTrue(player.getCooldowns().isOnCooldown(stack.getItem()), "The void did not start the cooldown");
		stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(find(helper, FlamescionVoidEntity.class).size() == 1, "A second void appeared during the cooldown");

		helper.assertTrue(FlamescionHandler.useUltimate(player), "The ultimate did not work in the Flamescion mode");
		helper.assertTrue(find(helper, FlamescionUltEntity.class).size() == 1, "Expected 1 ultimate entity");
		helper.assertTrue(FlamescionHandler.isOverloaded(player) && FlamescionHandler.getEnergy(player) == 600,
				"The ultimate did not start an overload");
		helper.assertTrue(player.hasEffect(ExtraBotanyMobEffects.TIMELOCK), "The ultimate did not lock the player");
		helper.assertFalse(FlamescionHandler.useUltimate(player), "A second ultimate was released during the overload");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void flameDamageIgnoresArmorAndFireResistance(GameTestHelper helper) {
		Player player = player(helper);
		Husk husk = husk(helper, 3.5, 3, 2.5);
		husk.getAttribute(Attributes.ARMOR).setBaseValue(30);
		husk.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000));
		husk.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 3));
		DamageHandler.INSTANCE.flameAttack(husk, player, 12F);
		float lost = husk.getMaxHealth() - husk.getHealth();
		helper.assertTrue(Math.abs(lost - 12F) < 1.0E-4F, "Armor or an effect decreased the flame damage: " + lost);

		// 烈焰人免疫原版的火焰伤害，但是不免疫薪炎的火焰伤害。
		// A blaze is immune to vanilla fire damage, but not to the flame damage of the Flamescion.
		Blaze blaze = helper.spawnWithNoFreeWill(EntityType.BLAZE, new BlockPos(2, 2, 2));
		DamageHandler.INSTANCE.flameAttack(blaze, player, 5F);
		helper.assertTrue(Math.abs(blaze.getMaxHealth() - blaze.getHealth() - 5F) < 1.0E-4F, "A fire-immune mob took no flame damage");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void dashMovesThePlayerAndHitsMobsOnThePath(GameTestHelper helper) {
		// 这个测试用大场地，这样冲刺 4 格之后玩家仍然在测试区域里。
		// This test uses the large arena, so that the player is still in the test area after a dash of 4 blocks.
		Player player = player(helper);
		player.setPos(helper.absoluteVec(new Vec3(20.5, 5, 20.5)));
		Husk husk = husk(helper, 20.5, 5, 22.5);
		Vec3 start = player.position();
		helper.assertFalse(FlamescionHandler.dash(player), "The dash worked outside the Flamescion mode");

		enterMode(player);
		helper.assertTrue(FlamescionHandler.dash(player), "The dash did not work in the Flamescion mode");
		Vec3 moved = player.position().subtract(start);
		helper.assertTrue(Math.abs(moved.z - 4.0) < 1.0E-4 && Math.abs(moved.x) < 1.0E-4, "The dash did not move the player 4 blocks ahead: " + moved);
		float lost = husk.getMaxHealth() - husk.getHealth();
		helper.assertTrue(Math.abs(lost - 6F) < 1.0E-4F, "Expected 6 flame damage from the dash, found " + lost);
		helper.assertTrue(husk.hasEffect(ExtraBotanyMobEffects.TIMELOCK), "The dash did not lock the monster");
		helper.assertTrue(player.hasEffect(ExtraBotanyMobEffects.FLAMESCION), "A dash that hit did not give Flamescion Strength");
		helper.assertFalse(FlamescionHandler.dash(player), "A second dash worked during the cooldown");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 30)
	public static void meleeHitInTheModeReleasesOneSlash(GameTestHelper helper) {
		Player player = player(helper);
		Husk husk = husk(helper, 3.5, 3, 2.5);
		enterMode(player);

		husk.hurt(helper.getLevel().damageSources().playerAttack(player), 2F);
		helper.assertTrue(find(helper, FlamescionSlashEntity.class).size() == 1, "The melee hit did not release a Flamescion Slash");
		helper.assertTrue(husk.hasEffect(ExtraBotanyMobEffects.TIMELOCK), "The melee hit did not lock the monster");

		helper.runAfterDelay(4, () ->
				// 斩击自己造成的伤害不能再放出新的斩击。
				// The damage of the slash must not release a new slash.
				helper.assertTrue(find(helper, FlamescionSlashEntity.class).size() == 1, "The damage of the slash released a new slash"));
		helper.runAfterDelay(9, () -> {
			helper.assertTrue(find(helper, FlamescionSlashEntity.class).isEmpty(), "The slash did not go away after 6 ticks");
			// 近战 2 点（尸壳有 2 点护甲），加上斩击的两次 1 点火焰伤害。
			// 2 from the melee hit (a husk has 2 armor), plus 1 flame damage two times from the slash.
			float lost = husk.getMaxHealth() - husk.getHealth();
			helper.assertTrue(lost > 3.5F && lost <= 4F, "Unexpected total damage from the hit and the slash: " + lost);
			helper.succeed();
		});
	}
}
