package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

@GameTestHolder(LibMisc.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ExtraBotanyGameTests {
	private static final BlockPos BOOSTED_SOIL_POS = new BlockPos(1, 1, 1);
	private static final BlockPos BOOSTED_FLOWER_POS = BOOSTED_SOIL_POS.above();
	private static final BlockPos NORMAL_SOIL_POS = new BlockPos(3, 1, 1);
	private static final BlockPos NORMAL_FLOWER_POS = NORMAL_SOIL_POS.above();

	private ExtraBotanyGameTests() {}

	@GameTest(template = "empty", timeoutTicks = 10)
	public static void tradeOrchidDiscountAppliesWhenTrading(GameTestHelper helper) {
		var villager = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.VILLAGER, new BlockPos(1, 2, 1));
		var offer = new net.minecraft.world.item.trading.MerchantOffer(
				new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.EMERALD, 20),
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD), 10, 1, 0.0F);
		villager.getOffers().clear();
		villager.getOffers().add(offer);
		villager.addEffect(new net.minecraft.world.effect.MobEffectInstance(
				io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.DISCOUNT, 200, 49));
		villager.mobInteract(helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
				net.minecraft.world.InteractionHand.MAIN_HAND);
		helper.assertTrue(offer.getSpecialPriceDiff() == -10,
				"Trade Orchid 50% effect did not discount the 20-emerald offer");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 10)
	public static void legacyItemDataSurvivesSaveAndCopy(GameTestHelper helper) {
		var stack = new net.minecraft.world.item.ItemStack(
				io.github.lounode.extrabotany.common.item.ExtraBotanyItems.excalibur);
		io.github.lounode.extrabotany.common.helper.ItemNBTHelper.setInt(stack, "audit_value", 37);
		var restored = net.minecraft.world.item.ItemStack.parseOptional(helper.getLevel().registryAccess(),
				(net.minecraft.nbt.CompoundTag) stack.save(helper.getLevel().registryAccess()));
		helper.assertTrue(io.github.lounode.extrabotany.common.helper.ItemNBTHelper.getInt(restored, "audit_value", -1) == 37,
				"Legacy custom data was lost on registry-aware ItemStack save/load");
		var copy = restored.copy();
		io.github.lounode.extrabotany.common.helper.ItemNBTHelper.setInt(copy, "audit_value", 99);
		helper.assertTrue(io.github.lounode.extrabotany.common.helper.ItemNBTHelper.getInt(restored, "audit_value", -1) == 37,
				"Copied stack mutation changed the original custom data");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void enchantedSoilPreservesHydroangeasAtDecayBoundary(GameTestHelper helper) {
		helper.setBlock(BOOSTED_SOIL_POS, ExtraBotanyBlocks.enchantedSoil);
		helper.setBlock(NORMAL_SOIL_POS, Blocks.GRASS_BLOCK);
		var hydroangeas = vazkii.botania.common.block.BotaniaBlocks.HYDROANGEAS;
		helper.setBlock(BOOSTED_FLOWER_POS, hydroangeas);
		helper.setBlock(NORMAL_FLOWER_POS, hydroangeas);
		for (BlockPos pos : new BlockPos[] { BOOSTED_FLOWER_POS, NORMAL_FLOWER_POS }) {
			var flower = helper.getBlockEntity(pos);
			var tag = flower.saveWithoutMetadata(helper.getLevel().registryAccess());
			tag.putInt("passiveDecayTicks", 72000);
			flower.loadWithComponents(tag, helper.getLevel().registryAccess());
		}
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(helper.getBlockState(BOOSTED_FLOWER_POS).is(hydroangeas),
					"Enchanted soil failed to protect Hydroangeas at the decay boundary");
			helper.assertTrue(!helper.getBlockState(NORMAL_FLOWER_POS).is(hydroangeas),
					"Control Hydroangeas did not decay");
			helper.assertTrue(helper.getBlockEntity(BOOSTED_FLOWER_POS)
					.saveWithoutMetadata(helper.getLevel().registryAccess()).getInt("passiveDecayTicks") == 72000,
					"Enchanted soil changed the stored decay age");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 10)
	public static void coreRegistriesAreAvailable(GameTestHelper helper) {
		for (var key : java.util.List.of(ExtraBotanyDamageTypes.LINK_DAMAGE,
				ExtraBotanyDamageTypes.EXCALIBUR_BEAM_DAMAGE, ExtraBotanyDamageTypes.JINGWEI_PUNCH_DAMAGE,
				ExtraBotanyDamageTypes.REVERSE_HEAL_DAMAGE, ExtraBotanyDamageTypes.BACKFIRE_DAMAGE)) {
			helper.assertTrue(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(key).isPresent(),
					"Missing damage type: " + key.location());
		}
		helper.assertTrue(
				BuiltInRegistries.BLOCK.getKey(ExtraBotanyBlocks.enchantedSoil).equals(prefix("enchanted_soil")),
				"Enchanted soil is not registered under the stable ExtraBotany id");
		helper.assertTrue(
				BuiltInRegistries.SOUND_EVENT.getHolder(ExtraBotanySounds.ARMOR_EQUIP_IDOL.value().getLocation()).isPresent(),
				"ExtraBotany armor equip sound holder is not bound");
		helper.assertTrue(
				helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
						.get(ExtraBotanyDamageTypes.EXCALIBUR_BEAM_DAMAGE).isPresent(),
				"ExtraBotany damage types were not loaded from generated data");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void enchantedSoilDoublesGroundFlowerTicks(GameTestHelper helper) {
		helper.getLevel().setDayTime(1000);
		helper.setBlock(BOOSTED_SOIL_POS, ExtraBotanyBlocks.enchantedSoil);
		helper.setBlock(NORMAL_SOIL_POS, Blocks.GRASS_BLOCK);
		helper.setBlock(BOOSTED_FLOWER_POS, ExtrabotanyFlowerBlocks.sunshineLily);
		helper.setBlock(NORMAL_FLOWER_POS, ExtrabotanyFlowerBlocks.sunshineLily);

		helper.runAfterDelay(3, () -> {
			GeneratingFlowerBlockEntity boosted = helper.getBlockEntity(BOOSTED_FLOWER_POS);
			GeneratingFlowerBlockEntity normal = helper.getBlockEntity(NORMAL_FLOWER_POS);
			helper.assertTrue(boosted.getMana() > 0,
					"Ground flower over enchanted soil did not receive its accelerated tick");
			helper.assertTrue(normal.getMana() == 0,
					"Control flower advanced as quickly as the enchanted-soil flower");
			helper.succeed();
		});
	}
}
