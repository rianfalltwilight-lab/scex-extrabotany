package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.block.dispenser.HammerDispenseBehavior;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe;
import io.github.lounode.extrabotany.common.entity.HolyWaterGrenadeEntity;
import io.github.lounode.extrabotany.common.entity.MagicArrowEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.KingGardenItem;
import io.github.lounode.extrabotany.common.item.relic.AchillesShieldItem;
import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;
import io.github.lounode.extrabotany.common.item.relic.FailnaughtItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 对照原版审计第 E3 批：武器。两个原版都有的取 1.16，只有 1.12 有的取 1.12。每个测试注明原版出处。
 * Batch E3 of the parity audit with the original: the weapons. What both originals have follows 1.16, what only 1.12 has follows 1.12. Each test names its source in the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E3ParityGameTests {

	private E3ParityGameTests() {}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static Player placedPlayer(GameTestHelper h, BlockPos rel) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		BlockPos abs = h.absolutePos(rel);
		player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
		h.getLevel().addFreshEntity(player);
		return player;
	}

	// Original 1.16 ItemExcaliber: 160 mana, 6 armor-piercing damage, anyone who holds it can swing a beam; ModItems.relic(): no repair, relic rarity.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void excaliburFollows116(GameTestHelper h) {
		Player player = placedPlayer(h, new BlockPos(2, 2, 2));
		try {
			ItemStack stack = new ItemStack(ExtraBotanyItems.excalibur);
			Relic.LOOKUP.find(stack).bindToUUID(UUID.randomUUID());
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			ManaBurstEntity burst = ExcaliburItem.getBurst(player, stack);
			h.assertTrue(burst.getMana() == 160, "The beam must carry 160 mana");
			h.assertTrue(ExcaliburItem.BURST_DAMAGE == 6F, "The beam must deal 6");
			ExcaliburItem.trySpawnBurst(player, 1F);
			h.assertTrue(!h.getLevel().getEntitiesOfClass(ManaBurstEntity.class, player.getBoundingBox().inflate(4)).isEmpty(),
					"A player who is not the owner must also swing a beam");
			h.assertFalse(stack.getItem().isRepairable(stack), "A relic must not be repairable");
			h.assertTrue(stack.getRarity() == Rarity.EPIC, "The Excalibur must have the relic rarity");
			var type = h.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ExtraBotanyDamageTypes.EXCALIBUR_BEAM_DAMAGE);
			h.assertTrue(type.is(DamageTypeTags.BYPASSES_ARMOR) && type.is(DamageTypeTags.BYPASSES_SHIELD)
					&& type.is(DamageTypeTags.BYPASSES_ENCHANTMENTS), "The beam must be absolute piercing damage");
		} finally {
			player.discard();
		}
		h.succeed();
	}

	// Original 1.16 ItemFailnaught and EntityMagicArrow: at least 8 ticks; rank (i - 8) / 5; mana min(800, 350 + 20 rank); damage min(50, 7 + 0.5 rank + attack) + Power; life min(150, 5 + 4i); only the owner shoots.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void failnaughtFollows116(GameTestHelper h) {
		h.assertTrue(FailnaughtItem.getManaCost(8) == 350 && FailnaughtItem.getManaCost(500) == 800, "Wrong mana cost");
		h.assertTrue(FailnaughtItem.getDamage(8, 1, 0) == 8 && FailnaughtItem.getDamage(2000, 1, 3) == 53, "Wrong damage");
		h.assertTrue(FailnaughtItem.getLife(8) == 37 && FailnaughtItem.getLife(100) == 150, "Wrong life");

		Player player = placedPlayer(h, new BlockPos(2, 2, 2));
		try {
			ManaItem mana = supplyMana(player, 5000);
			ItemStack bow = new ItemStack(ExtraBotanyItems.failnaught);
			player.setItemInHand(InteractionHand.MAIN_HAND, bow);
			Relic.LOOKUP.find(bow).bindToUUID(player.getUUID());
			int duration = bow.getUseDuration(player);
			bow.releaseUsing(h.getLevel(), player, duration - 7);
			h.assertTrue(mana.getMana() == 5000, "A draw of 7 ticks must not shoot");
			bow.releaseUsing(h.getLevel(), player, duration - 28);
			var arrows = h.getLevel().getEntitiesOfClass(MagicArrowEntity.class, player.getBoundingBox().inflate(4));
			double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
			h.assertTrue(arrows.size() == 1 && arrows.getFirst().getDamage() == FailnaughtItem.getDamage(28, attack, 0),
					"A draw of 28 ticks must shoot one arrow with the 1.16 damage");
			h.assertTrue(mana.getMana() == 5000 - 430, "A draw of 28 ticks must cost 350 + 4 x 20, left " + mana.getMana());
			arrows.forEach(MagicArrowEntity::discard);
			Relic.LOOKUP.find(bow).bindToUUID(UUID.randomUUID());
			bow.releaseUsing(h.getLevel(), player, duration - 28);
			h.assertTrue(h.getLevel().getEntitiesOfClass(MagicArrowEntity.class, player.getBoundingBox().inflate(4)).isEmpty(),
					"A player who is not the owner must not shoot");
			h.assertFalse(bow.getItem().isRepairable(bow), "A relic must not be repairable");
		} finally {
			player.discard();
		}
		h.succeed();
	}

	// Original 1.16 ModItems.relic(): setNoRepair.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void relicSwordsCannotBeRepaired(GameTestHelper h) {
		for (Item item : List.of(ExtraBotanyItems.trueTerrablade, ExtraBotanyItems.trueShadowKatana, ExtraBotanyItems.influxWaver,
				ExtraBotanyItems.starWrath, ExtraBotanyItems.firstFractal)) {
			h.assertFalse(item.isRepairable(new ItemStack(item)), item + " can be repaired");
		}
		h.succeed();
	}

	// Original 1.16 EntitySplashGrenade and ModItems: 5 blocks, 10 magic damage to creatures that are not players, harmful effects to them and beneficial ones to players, duration x 0.6; stacks to 32.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void holyWaterGrenadeFollows116(GameTestHelper h) {
		h.assertTrue(new ItemStack(ExtraBotanyItems.holyWaterGrenade).getMaxStackSize() == 32, "The grenade must stack to 32");
		BlockPos center = h.absolutePos(new BlockPos(4, 2, 4));
		var zombie = EntityType.ZOMBIE.create(h.getLevel());
		zombie.setNoAi(true);
		zombie.setPos(center.getX() + 4.5, center.getY(), center.getZ() + 0.5);
		h.getLevel().addFreshEntity(zombie);
		Player player = placedPlayer(h, new BlockPos(4, 2, 6));
		try {
			var grenade = new HolyWaterGrenadeEntity(h.getLevel(), center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
			float health = zombie.getHealth();
			grenade.applySplash(List.of(new MobEffectInstance(MobEffects.WEAKNESS, 100), new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100)), null);
			h.assertTrue(health - zombie.getHealth() == 10F, "A creature 4 blocks away must take 10 damage");
			var weakness = zombie.getEffect(MobEffects.WEAKNESS);
			h.assertTrue(weakness != null && weakness.getDuration() == 60, "The harmful effect must last 60% with no loss by distance");
			h.assertFalse(zombie.hasEffect(MobEffects.MOVEMENT_SPEED), "A creature got a beneficial effect");
			h.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && !player.hasEffect(MobEffects.WEAKNESS), "The player must get only the beneficial effect");
			h.assertTrue(player.getHealth() == player.getMaxHealth(), "The player must take no damage");
		} finally {
			zombie.discard();
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 ItemShieldCopy and the three shields: the durability is 2 times that of the material (600 / 1440 / 4600), 120 mana for each point.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void shieldsFollow112(GameTestHelper h) {
		h.assertTrue(new ItemStack(ExtraBotanyItems.manasteelShield).getMaxDamage() == 600, "Manasteel Shield must have 600");
		h.assertTrue(new ItemStack(ExtraBotanyItems.elementiumShield).getMaxDamage() == 1440, "Elementium Shield must have 1440");
		h.assertTrue(new ItemStack(ExtraBotanyItems.terrasteelShield).getMaxDamage() == 4600, "Terrasteel Shield must have 4600");
		h.assertTrue(new ItemStack(ExtraBotanyItems.achillesShield).getMaxDamage() == 4600, "Achilles Shield must have 4600");
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		ManaItem mana = supplyMana(player, 1000);
		ItemStack shield = new ItemStack(ExtraBotanyItems.elementiumShield);
		shield.setDamageValue(10);
		shield.getItem().inventoryTick(shield, h.getLevel(), player, 0, false);
		h.assertTrue(shield.getDamageValue() == 9 && mana.getMana() == 880, "One point of repair must cost 120, left " + mana.getMana());
		h.succeed();
	}

	// Original 1.12 ItemAchilleshield: the release needs 12 absorption; the ram deals 13 magic damage within 1 block.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void achillesShieldFollows112(GameTestHelper h) {
		Player player = placedPlayer(h, new BlockPos(2, 2, 2));
		var zombie = EntityType.ZOMBIE.create(h.getLevel());
		try {
			player.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);
			ItemStack shield = new ItemStack(ExtraBotanyItems.achillesShield);
			player.setItemInHand(InteractionHand.MAIN_HAND, shield);
			player.setShiftKeyDown(true);
			player.setAbsorptionAmount(11);
			shield.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
			h.assertFalse(AchillesShieldItem.isReleased(shield), "11 absorption must not release");
			player.setAbsorptionAmount(12);
			shield.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
			h.assertTrue(AchillesShieldItem.isReleased(shield), "12 absorption must release");

			zombie.setNoAi(true);
			zombie.setPos(player.getX() + 1, player.getY(), player.getZ());
			h.getLevel().addFreshEntity(zombie);
			float health = zombie.getHealth();
			player.setDeltaMovement(0.3, 0, 0);
			AchillesShieldItem.ram(new ItemStack(ExtraBotanyItems.achillesShield), player);
			h.assertTrue(health - zombie.getHealth() == AchillesShieldItem.RAM_DAMAGE, "The ram must deal 13");
		} finally {
			zombie.discard();
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 BehaviourHammer: a dispenser strikes the pedestal 5 times with a hammer.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void dispenserStrikesThePedestal(GameTestHelper h) {
		var manager = h.getLevel().getRecipeManager();
		var original = new ArrayList<>(manager.getRecipes());
		try {
			manager.replaceRecipes(List.of(new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(LibMisc.MOD_ID, "test/e3_dispenser"),
					new PedestalsRecipe(new ItemStack(Items.GRANITE), Ingredient.of(ExtraBotanyItems.manasteelHammer),
							Ingredient.of(Items.COBBLESTONE), 10, 0))));
			BlockPos pedestalRel = new BlockPos(1, 2, 1);
			BlockPos dispenserRel = pedestalRel.west();
			h.setBlock(pedestalRel, ExtraBotanyBlocks.livingrockPedestal);
			h.setBlock(dispenserRel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
			var pedestal = (PedestalBlockEntity) h.getBlockEntity(pedestalRel);
			pedestal.setItem(new ItemStack(Items.COBBLESTONE));
			var source = new BlockSource(h.getLevel(), h.absolutePos(dispenserRel), h.getBlockState(dispenserRel),
					(DispenserBlockEntity) h.getBlockEntity(dispenserRel));
			ItemStack hammer = new ItemStack(ExtraBotanyItems.manasteelHammer);
			ItemStack left = new HammerDispenseBehavior().dispense(source, hammer);
			h.assertTrue(pedestal.getStrikes() == 5, "One dispense must strike 5 times, got " + pedestal.getStrikes());
			h.assertTrue(!left.isEmpty() && left.getDamageValue() == 5, "The hammer must lose 5 durability");
			new HammerDispenseBehavior().dispense(source, left);
			h.assertTrue(pedestal.getItem().is(Items.GRANITE), "The second dispense must finish the recipe");
		} finally {
			manager.replaceRecipes(original);
		}
		h.succeed();
	}

	// Original 1.12 ItemHammer.onMining: any hammer in the main hand digs 1.25 times faster.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void hammersDigFaster(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.manasteelHammer));
		var event = new PlayerEvent.BreakSpeed(player, Blocks.STONE.defaultBlockState(), 4F, BlockPos.ZERO);
		NeoForge.EVENT_BUS.post(event);
		h.assertTrue(event.getNewSpeed() == 5F, "A hammer must dig 1.25 times faster, got " + event.getNewSpeed());
		h.succeed();
	}

	// Original 1.12 BlockPedestal: the King's Garden accepts any special flower item of the kind, a floating one too.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void kingsGardenAcceptsFloatingFlowers(GameTestHelper h) {
		int plain = KingGardenItem.getFlowerType(new ItemStack(ExtrabotanyFlowerBlocks.bellflower));
		int floating = KingGardenItem.getFlowerType(new ItemStack(ExtrabotanyFlowerBlocks.bellflowerFloating));
		h.assertTrue(plain >= 0 && plain == floating, "A floating Bellflower must count as a Bellflower");
		h.succeed();
	}
}
