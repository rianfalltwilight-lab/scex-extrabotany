package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.generating.BloodEnchantressBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.HolyWaterGrenadeEntity;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.JackieChanRingItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.ParkourRingItem;
import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 对照原版审计第 E0 批：能玩性问题和明确的错误。每个测试注明原版出处。
 * Batch E0 of the parity audit with the original: playability problems and clear bugs. Each test names its source in the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E0ParityGameTests {

	// Original 1.16 ItemEmptyBottle: a use on a mana pool takes 25000 mana and gives a Mana Drink.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void managlassBottleOnPoolGivesManaDrink(GameTestHelper h) {
		BlockPos poolPos = new BlockPos(1, 1, 1);
		h.setBlock(poolPos, BotaniaBlocks.MANA_POOL);
		var pool = (ManaPoolBlockEntity) h.getBlockEntity(poolPos);
		pool.receiveMana(30000);
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.manaGlassBottle));
		BlockPos abs = h.absolutePos(poolPos);
		var hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
		ExtraBotanyItems.manaGlassBottle.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		h.assertTrue(player.getInventory().countItem(ExtraBotanyItems.manaDrink) == 1, "The bottle did not become a Mana Drink");
		h.assertTrue(player.getInventory().countItem(ExtraBotanyItems.manaCocktail) == 0, "The bottle still gave a cocktail");
		h.assertTrue(pool.getCurrentMana() == 5000, "The bottle did not take exactly 25000 mana");
		h.succeed();
	}

	// Original 1.16 recipes/powerframe.json.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void powerFrameHasACraftingRecipe(GameTestHelper h) {
		var recipe = h.getLevel().getRecipeManager().byKey(prefix("power_frame"));
		h.assertTrue(recipe.isPresent(), "The Power Frame has no recipe");
		ItemStack result = recipe.get().value().getResultItem(h.getLevel().registryAccess());
		h.assertTrue(result.is(ExtraBotanyBlocks.powerFrame.asItem()), "The recipe makes the wrong item");
		h.succeed();
	}

	// Original 1.16 ModItems.GILDEDMASHEDPOTATO_PROP.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void gildedMashedPotatoIsTheOriginalFood(GameTestHelper h) {
		var food = new ItemStack(ExtraBotanyItems.gildedPotatoMashed).get(DataComponents.FOOD);
		h.assertTrue(food != null, "Gilded Mashed Potato cannot be eaten");
		h.assertTrue(food.nutrition() == 5 && Math.abs(food.saturation() - 5 * 0.2F * 2) < 1e-4 && food.canAlwaysEat(),
				"Gilded Mashed Potato does not have the food values of the original");
		boolean resistance = false, slowness = false, absorption = false;
		for (var possible : food.effects()) {
			MobEffectInstance effect = possible.effect();
			resistance |= effect.getEffect().is(MobEffects.DAMAGE_RESISTANCE) && effect.getAmplifier() == 3 && effect.getDuration() == 600;
			slowness |= effect.getEffect().is(MobEffects.MOVEMENT_SLOWDOWN) && effect.getAmplifier() == 3 && effect.getDuration() == 600;
			absorption |= effect.getEffect().is(MobEffects.ABSORPTION) && effect.getAmplifier() == 1 && effect.getDuration() == 600;
		}
		h.assertTrue(resistance && slowness && absorption, "Gilded Mashed Potato lost an effect of the original");
		h.succeed();
	}

	// Original 1.16 SubTileBloodyEnchantress and ExtraBotanyAPI.addPotionEffect: the inhibition is the Blood Temptation effect, and it runs out.
	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void bloodEnchantressInhibitionIsAnEffectThatRunsOut(GameTestHelper h) {
		var pig = h.spawn(EntityType.PIG, new BlockPos(2, 2, 2));
		pig.setInvulnerable(false);
		for (int i = 0; i < 12; i++) {
			BloodEnchantressBlockEntity.addTemptation(pig);
		}
		var effect = pig.getEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION);
		h.assertTrue(effect != null && effect.getAmplifier() == 10, "Blood Temptation did not stop at amplifier 10");
		h.assertTrue(effect.getDuration() == 100 + 200 * 10, "Blood Temptation did not last 100 + 200 x amplifier ticks");

		BlockPos flowerPos = new BlockPos(2, 2, 3);
		h.setBlock(flowerPos, ExtrabotanyFlowerBlocks.bloodEnchantress);
		var flower = (BloodEnchantressBlockEntity) h.getBlockEntity(flowerPos);
		E1ParityGameTests.bindToSpreader(h, flower, new BlockPos(4, 2, 3));
		flower.tickFlower();
		h.assertTrue(flower.getMana() == 0, "The flower used a creature at the highest Blood Temptation");

		pig.removeEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION);
		flower.tickFlower();
		h.assertTrue(flower.getMana() > 0, "The flower did not use the creature again after the effect ended");
		h.assertTrue(BloodEnchantressBlockEntity.getTemptation(pig) == 0 && pig.hasEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION),
				"The hit did not give a new Blood Temptation of amplifier 0");
		h.succeed();
	}

	// Original 1.12 EntityGaiaIII: experienceValue = 1225, also for a ticket summon (the constructor with a home position).
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void gaiaIIIGivesTheSameExperienceForEverySummon(GameTestHelper h) {
		var summoned = new GaiaIII(h.getLevel(), h.absolutePos(BlockPos.ZERO));
		var loaded = new GaiaIII(io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType.GAIA_III, h.getLevel());
		h.assertTrue(summoned.getExperienceReward(h.getLevel(), null) == GaiaIII.XP_REWARD,
				"A summoned Guardian of Gaia III does not give 1225 experience");
		h.assertTrue(loaded.getExperienceReward(h.getLevel(), null) == GaiaIII.XP_REWARD,
				"A loaded Guardian of Gaia III does not give 1225 experience");
		h.succeed();
	}

	// Original 1.16 ItemExcaliber: the beam turns only to hostile creatures.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void excaliburBeamHomesOnlyOnHostileCreatures(GameTestHelper h) {
		h.assertTrue(ExcaliburItem.canTargetEntity(h.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1))), "The beam does not turn to a zombie");
		h.assertFalse(ExcaliburItem.canTargetEntity(h.makeMockPlayer(GameType.SURVIVAL)), "The beam turns to a player");
		h.assertFalse(ExcaliburItem.canTargetEntity(h.spawn(EntityType.VILLAGER, new BlockPos(2, 2, 1))), "The beam turns to a villager");
		h.assertFalse(ExcaliburItem.canTargetEntity(h.spawn(EntityType.WOLF, new BlockPos(3, 2, 1))), "The beam turns to a wolf");
		h.succeed();
	}

	// Original 1.16 EntitySplashGrenade: beneficial effects to players, harmful effects to the other creatures.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void holyWaterGrenadeNeverHarmsPlayers(GameTestHelper h) {
		// The players must be in the level, so that the splash finds them.
		Vec3 center = h.absoluteVec(new Vec3(3.5, 2, 3.5));
		var thrower = h.makeMockPlayer(GameType.SURVIVAL);
		var other = h.makeMockPlayer(GameType.SURVIVAL);
		thrower.setPos(center.add(1, 0, 0));
		other.setPos(center.add(-1, 0, 0));
		h.assertTrue(h.getLevel().addFreshEntity(thrower) && h.getLevel().addFreshEntity(other), "Fixture: could not add the players");
		var zombie = h.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 4));
		var grenade = new HolyWaterGrenadeEntity(h.getLevel(), thrower);
		grenade.setPos(center);
		List<MobEffectInstance> effects = new ArrayList<>(List.of(
				new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400), new MobEffectInstance(MobEffects.WEAKNESS, 400)));
		grenade.applySplash(effects, null);
		for (LivingEntity player : List.of(thrower, other)) {
			h.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "A player did not get the beneficial effect");
			h.assertFalse(player.hasEffect(MobEffects.WEAKNESS), "A player got the harmful effect");
		}
		h.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS) && !zombie.hasEffect(MobEffects.MOVEMENT_SPEED),
				"A hostile creature did not get only the harmful effect");
		thrower.discard();
		other.discard();
		h.succeed();
	}

	// Reburn-only flower: a full sound table must not push out old sounds and reset their counts.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void resoncundCannotBeFarmedByRotatingSounds(GameTestHelper h) {
		BlockPos pos = new BlockPos(1, 1, 1);
		h.setBlock(pos, ExtrabotanyFlowerBlocks.resoncund);
		var flower = (ResoncundBlockEntity) h.getBlockEntity(pos);
		List<SoundEvent> sounds = BuiltInRegistries.SOUND_EVENT.stream().limit(ResoncundBlockEntity.CACHE_SIZE + 1).toList();
		try {
			for (int i = 0; i < ResoncundBlockEntity.CACHE_SIZE; i++) {
				flower.onSoundHeard(sounds.get(i));
				flower.addMana(-flower.getMana());
			}
			flower.onSoundHeard(sounds.get(ResoncundBlockEntity.CACHE_SIZE));
			h.assertTrue(flower.getMana() == 0, "A new sound gave mana while the sound table was full");
			h.assertTrue(flower.getSoundHeard().size() == ResoncundBlockEntity.CACHE_SIZE
					&& flower.getSoundHeard().get(sounds.get(0)) == 1, "A full sound table forgot an old count");
			flower.onSoundHeard(sounds.get(0));
			h.assertTrue(flower.getMana() == 500 - flower.getLossPerHeard(), "A known sound did not give its decayed mana");
		} finally {
			h.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
		}
		h.succeed();
	}

	// Original 1.12 WallJumpingShim (5) and ItemParkourRing (8).
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void wallJumpRingsHaveTheOriginalLimits(GameTestHelper h) {
		h.assertTrue(((JackieChanRingItem) ExtraBotanyItems.jackieChanRing).getMaxAllowedJumps() == 5, "The Jackie Chan Ring does not allow 5 wall jumps");
		h.assertTrue(((JackieChanRingItem) ExtraBotanyItems.parkourRing).getMaxAllowedJumps() == 8, "The Parkour Ring does not allow 8 wall jumps");
		h.assertTrue(ExtraBotanyItems.parkourRing instanceof ParkourRingItem, "Fixture: wrong Parkour Ring class");
		h.succeed();
	}
}
