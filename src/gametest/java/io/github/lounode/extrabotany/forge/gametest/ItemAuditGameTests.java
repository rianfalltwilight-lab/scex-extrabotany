package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.entity.AuraFireEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPack;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketExcalibur;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketVoidArchives;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 重制版物品的回归测试：材料增益的间隔、盾牌的格挡效果、伤害吸收的上限和熔炉燃料。
 * Regression tests for the items of Reburn: the interval of the material buffs, the block effects of the shields, the limit of the absorption, and the furnace fuels.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ItemAuditGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);

	private ItemAuditGameTests() {}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(CENTER).getCenter());
		player.setNoGravity(true);
		return player;
	}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(9, tablet);
		return mana;
	}

	/**
	 * 天空锤每 60 tick 给一次急迫效果，并且只消耗一次魔力。以前它在 60 tick 里消耗 59 次。
	 * The Aerialite Hammer gives Haste one time for each 60 ticks, and it only uses mana one time. Before, it used mana 59 times in 60 ticks.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void materialBuffUsesManaOncePerInterval(GameTestHelper helper) {
		Player player = player(helper);
		ItemStack hammer = new ItemStack(ExtraBotanyItems.aerialiteHammer);
		player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
		ManaItem mana = supplyMana(player, 10000);

		for (int tick = 1; tick <= 60; tick++) {
			player.tickCount = tick;
			hammer.inventoryTick(helper.getLevel(), player, 0, true);
		}
		helper.assertTrue(mana.getMana() == 10000 - 10, "Expected one buff payment of 10 mana in 60 ticks, used " + (10000 - mana.getMana()));
		MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
		helper.assertTrue(haste != null && haste.getAmplifier() == 1, "The hammer did not give Haste II");
		helper.succeed();
	}

	/**
	 * 泰拉钢盾牌只在真正格挡住伤害时给生命恢复。玩家举着盾牌、但是伤害没有被格挡时（例如从背后来的伤害），不触发效果。
	 * The Terrasteel Shield only gives Regeneration when the damage is really blocked. When the player has the shield up but the damage is not blocked (for example, damage from behind), the effect does not start.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shieldEffectsNeedARealBlock(GameTestHelper helper) {
		Player player = player(helper);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ExtraBotanyItems.terrasteelShield));
		player.startUsingItem(InteractionHand.OFF_HAND);
		helper.assertTrue(player.getUseItem().is(ExtraBotanyItems.terrasteelShield), "The player does not use the shield");
		DamageSource source = player.damageSources().generic();

		ManasteelShieldItem.EventHandler.onShieldBlockDamage(new LivingShieldBlockEvent(player, new DamageContainer(source, 4F), false));
		helper.assertFalse(player.hasEffect(MobEffects.REGENERATION), "Damage that was not blocked started the shield effect");

		ManasteelShieldItem.EventHandler.onShieldBlockDamage(new LivingShieldBlockEvent(player, new DamageContainer(source, 4F), true));
		helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "A real block did not start the shield effect");
		helper.succeed();
	}

	/**
	 * 勇士盾在手里时，玩家能攒下伤害吸收。从 1.20.5 起，没有"最大伤害吸收"属性就攒不下来。
	 * With the Achilles Shield in the hand, the player can collect absorption. From 1.20.5, this is not possible without the "max absorption" attribute.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void achillesShieldCollectsAbsorption(GameTestHelper helper) {
		Player player = player(helper);
		ItemStack shield = new ItemStack(ExtraBotanyItems.achillesShield);
		player.setItemInHand(InteractionHand.MAIN_HAND, shield);
		for (int i = 0; i < 3; i++) {
			player.tick();
		}
		helper.assertTrue(player.getMaxAbsorption() == 20F, "The shield in the hand does not give 20 max absorption, got " + player.getMaxAbsorption());

		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, CENTER);
		shield.hurtEnemy(husk, player);
		helper.assertTrue(player.getAbsorptionAmount() == 2F, "A hit with the shield did not give 2 absorption, got " + player.getAbsorptionAmount());

		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		for (int i = 0; i < 3; i++) {
			player.tick();
		}
		helper.assertTrue(player.getAbsorptionAmount() == 0F, "The absorption stayed after the shield left the hand");
		husk.discard();
		helper.succeed();
	}

	/**
	 * 精卫之羽提供的"最大伤害吸收"等于火焰能给的伤害吸收上限。
	 * The "max absorption" that the Feather of Jingwei gives is equal to the absorption limit of its fire.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void featherOfJingweiGivesMaxAbsorption(GameTestHelper helper) {
		ItemStack feather = new ItemStack(ExtraBotanyItems.featherOfJingwei);
		ResourceLocation slot = prefix("test_slot");
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = ((BaubleItem) feather.getItem()).getEquippedAttributeModifiers(feather, slot);
		double total = modifiers.get(Attributes.MAX_ABSORPTION).stream().mapToDouble(AttributeModifier::amount).sum();
		helper.assertTrue(total == AuraFireEntity.ABSORPTION_MAX, "Expected " + AuraFireEntity.ABSORPTION_MAX + " max absorption, got " + total);
		helper.succeed();
	}

	/**
	 * 梦魇燃料和精神燃料的燃烧时间来自 NeoForge 的数据映射。
	 * The burn times of the Nightmare Fuel and the Spirit Fuel come from the data map of NeoForge.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void fuelsComeFromTheDataMap(GameTestHelper helper) {
		int nightmare = new ItemStack(ExtraBotanyItems.nightmareFuel).getBurnTime(RecipeType.SMELTING);
		int spirit = new ItemStack(ExtraBotanyItems.spiritFuel).getBurnTime(RecipeType.SMELTING);
		helper.assertTrue(nightmare == 3200, "Nightmare Fuel burn time is " + nightmare);
		helper.assertTrue(spirit == 12800, "Spirit Fuel burn time is " + spirit);
		helper.succeed();
	}

	/**
	 * 三个"对着空气挥动"的数据包在处理之后重置服务端的攻击冷却。这样不发挥手数据包的客户端不能每 tick 触发一次效果。
	 * The three "swing at the air" packets reset the attack cooldown of the server after they are handled. Thus a client that sends no swing packet cannot start the effect on each tick.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void leftClickPacketsResetTheAttackCooldown(GameTestHelper helper) {
		for (LeftClickPack packet : List.of(LeftClickPacketExcalibur.INSTANCE, LeftClickPacketJingwei.INSTANCE, LeftClickPacketVoidArchives.INSTANCE)) {
			ChargedPlayer player = new ChargedPlayer(helper);
			helper.assertTrue(player.getAttackStrengthScale(0F) == 1F, "The test player must start with a full attack cooldown");
			packet.handle(player);
			helper.assertTrue(player.getAttackStrengthScale(0F) == 0F, packet.type().id() + " must reset the attack cooldown of the server");
		}
		helper.succeed();
	}

	/**
	 * 一名不在世界里的服务端玩家，它的攻击冷却已经走完。
	 * A server player that is not in the level, with a complete attack cooldown.
	 */
	private static final class ChargedPlayer extends ServerPlayer {
		ChargedPlayer(GameTestHelper helper) {
			super(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "eb-test-swing"), ClientInformation.createDefault());
			attackStrengthTicker = 100;
		}
	}
}
