package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.entity.PetPixieEntity;
import io.github.lounode.extrabotany.common.entity.projectile.InfluxWaverProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.entity.projectile.TrueShadowKatanaProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.TrueTerrabladeProjectileEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.tool.ShadowKatanaItem;
import io.github.lounode.extrabotany.common.util.AttributeUtil;

import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 原版工具和遗物剑的回归测试。
 * Regression tests for the tools and the relic swords of the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class RelicSwordGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);

	private RelicSwordGameTests() {}

	/**
	 * 生成一个攻击冷却已满的玩家，主手拿着指定的物品。
	 * Makes a player with a full attack cooldown that holds the given item in the main hand.
	 */
	private static Player playerHolding(GameTestHelper helper, ItemStack stack) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(CENTER).getCenter());
		player.setNoGravity(true);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		for (int i = 0; i < 30; i++) {
			player.tick();
		}
		return player;
	}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static <T extends Entity> int countOwned(GameTestHelper helper, Class<T> type, Player owner) {
		AABB area = new AABB(owner.blockPosition()).inflate(32);
		return helper.getLevel().getEntitiesOfClass(type, area,
				entity -> entity instanceof Projectile projectile && projectile.getOwner() == owner).size();
	}

	private static <T extends Entity> void assertSkill(GameTestHelper helper, Item item, int manaCost, Class<T> projectile, int expectedCount) {
		ItemStack stack = new ItemStack(item);
		Player player = playerHolding(helper, stack);
		ManaItem mana = supplyMana(player, 5000);
		Relic relic = Relic.LOOKUP.find(stack);
		helper.assertTrue(relic != null, "Missing relic capability on " + item);

		relic.bindToUUID(UUID.randomUUID());
		((LeftClickItem) item).onLeftClick(player, null);
		helper.assertTrue(mana.getMana() == 5000 && countOwned(helper, projectile, player) == 0,
				"A player that does not own the relic used the skill of " + item);

		relic.bindToUUID(player.getUUID());
		((LeftClickItem) item).onLeftClick(player, null);
		helper.assertTrue(mana.getMana() == 5000 - manaCost, "Wrong mana cost for " + item + ": " + (5000 - mana.getMana()));
		helper.assertTrue(countOwned(helper, projectile, player) == expectedCount,
				"Wrong projectile count for " + item + ": " + countOwned(helper, projectile, player));

		// 空挥会重置攻击冷却，所以同一 tick 的第二次请求必须被拒绝。
		// A swing at nothing resets the attack cooldown, so a second request in the same tick must be refused.
		((LeftClickItem) item).onLeftClick(player, null);
		helper.assertTrue(mana.getMana() == 5000 - manaCost, "A second swing in the same tick used the skill of " + item);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void trueTerrabladeSkill(GameTestHelper helper) {
		assertSkill(helper, ExtraBotanyItems.trueTerrablade, 400, TrueTerrabladeProjectileEntity.class, 1);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void influxWaverSkill(GameTestHelper helper) {
		assertSkill(helper, ExtraBotanyItems.influxWaver, 500, InfluxWaverProjectileEntity.class, 1);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void trueShadowKatanaSkill(GameTestHelper helper) {
		assertSkill(helper, ExtraBotanyItems.trueShadowKatana, 800, TrueShadowKatanaProjectileEntity.class, 3);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void firstFractalNeedsTheHerrscherAdvancement(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ExtraBotanyItems.firstFractal);
		Player player = playerHolding(helper, stack);
		Relic.LOOKUP.find(stack).bindToUUID(player.getUUID());
		// 玩家向下看，所以瞄准点在玩家脚下。这样幻影剑出现的位置和别的测试建造的东西无关。
		// The player looks down, thus the aim point is below the player. Then the positions of the phantom swords do not depend on the things that other tests build.
		player.setXRot(90);

		((LeftClickItem) stack.getItem()).onLeftClick(player, null);
		helper.assertTrue(countOwned(helper, PhantomSwordEntity.class, player) == 0,
				"The skill worked without the Herrscher advancement");

		// 创造模式的玩家不受进度限制。
		// The advancement limit does not apply to a player in creative mode.
		player.getAbilities().instabuild = true;
		((LeftClickItem) stack.getItem()).onLeftClick(player, null);
		helper.assertTrue(countOwned(helper, PhantomSwordEntity.class, player) == 4,
				"Expected 4 phantom swords, found " + countOwned(helper, PhantomSwordEntity.class, player));
		helper.assertTrue(!stack.isDamageableItem(), "The First Fractal of the original has no durability");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shadowKatanaNightBonusFollowsTheHeldItem(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Player player = playerHolding(helper, new ItemStack(ExtraBotanyItems.shadowKatana));
		var damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
		var nightDamageId = prefix("shadow_katana_night_damage");

		long savedTime = level.getDayTime();
		try {
			level.setDayTime(6000);
			level.updateSkyBrightness();
			ShadowKatanaItem.updateNightBonus(player);
			helper.assertTrue(!damage.hasModifier(nightDamageId), "The night bonus applied during the day");

			level.setDayTime(18000);
			level.updateSkyBrightness();
			ShadowKatanaItem.updateNightBonus(player);
			helper.assertTrue(damage.hasModifier(nightDamageId), "The night bonus did not apply at night");

			player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			ShadowKatanaItem.updateNightBonus(player);
			helper.assertTrue(!damage.hasModifier(nightDamageId), "The night bonus stayed after the katana left the hand");
		} finally {
			level.setDayTime(savedTime);
			level.updateSkyBrightness();
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shadowKatanaRepairsWithManaAndDropsLegacyModifiers(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ExtraBotanyItems.shadowKatana);
		stack.setDamageValue(10);
		// 模拟旧版本存下来的物品：夜晚加成写在物品的属性修饰符组件里。
		// Simulate an item from an old version: the night bonus is in the attribute modifier component of the item.
		AttributeUtil.addAttributeModifier(stack, Attributes.ATTACK_DAMAGE,
				new AttributeModifier(prefix("shadow_katana_night_damage"), 10,
						AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlot.MAINHAND);
		helper.assertTrue(stack.getComponentsPatch().get(DataComponents.ATTRIBUTE_MODIFIERS) != null, "Test setup failed");

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ManaItem mana = supplyMana(player, 1000);
		stack.getItem().inventoryTick(stack, helper.getLevel(), player, 0, false);

		helper.assertTrue(stack.getDamageValue() == 9 && mana.getMana() == 880,
				"One durability point must cost 120 mana, damage=" + stack.getDamageValue() + " mana=" + mana.getMana());
		helper.assertTrue(stack.getComponentsPatch().get(DataComponents.ATTRIBUTE_MODIFIERS) == null,
				"The modifiers of the old version stayed on the item");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void bottledPixieSummonsAPetThatSparesItsOwner(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ExtraBotanyItems.bottledPixie);
		Player player = playerHolding(helper, stack);
		ManaItem mana = supplyMana(player, 1000);

		stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		var pixies = helper.getLevel().getEntitiesOfClass(PetPixieEntity.class, new AABB(player.blockPosition()).inflate(8));
		helper.assertTrue(pixies.size() == 1 && mana.getMana() == 700, "Expected one pet pixie for 300 mana");
		helper.assertTrue(player.getCooldowns().isOnCooldown(stack.getItem()), "The bottle has no cooldown");

		PetPixieEntity pixie = pixies.getFirst();
		helper.assertTrue(!pixie.hurt(player.damageSources().playerAttack(player), 5F), "The owner damaged its own pixie");
		helper.assertTrue(pixie.getTarget() != player, "The pet pixie selected its owner as the target");
		pixie.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void trueDamageCanKillAndIsHalvedForBosses(GameTestHelper helper) {
		Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
		var weak = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
		weak.setHealth(3F);
		DamageHandler.INSTANCE.dealTrueDamage(attacker, weak, 10F);
		helper.assertTrue(weak.isDeadOrDying(), "True damage above the remaining health must kill");

		var strong = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
		float removed = DamageHandler.INSTANCE.dealTrueDamage(attacker, strong, 6F);
		helper.assertTrue(removed >= 5.9F && removed <= 6.1F, "True damage must remove the full amount, removed " + removed);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaDrinkHealsAndGivesMana(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ExtraBotanyItems.manaDrink, 2);
		Player player = playerHolding(helper, stack);
		ManaItem mana = supplyMana(player, 0);
		player.setHealth(10F);

		ItemStack result = stack.getItem().finishUsingItem(stack, helper.getLevel(), player);
		helper.assertTrue(result.getCount() == 1, "The drink was not used up");
		helper.assertTrue(player.getHealth() == 15F, "The drink must give back 5 health");
		helper.assertTrue(mana.getMana() == 10000, "The drink must give 10000 mana, gave " + mana.getMana());
		helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST) && player.hasEffect(MobEffects.JUMP), "Missing effects");
		helper.assertTrue(player.getInventory().countItem(ExtraBotanyItems.manaGlassBottle) == 1, "The bottle was not given back");
		helper.succeed();
	}
}
