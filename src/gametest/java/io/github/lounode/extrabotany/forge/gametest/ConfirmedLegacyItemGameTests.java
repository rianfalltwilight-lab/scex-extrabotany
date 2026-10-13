package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.brew.BaseBrewItem;

import java.util.List;

@GameTestHolder(LibMisc.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConfirmedLegacyItemGameTests {
	private ConfirmedLegacyItemGameTests() {}

	private static ItemStack loadOld(GameTestHelper helper, String id, int count) {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "extrabotany:" + id);
		tag.putInt("count", count);
		ItemStack stack = ItemStack.parseOptional(helper.getLevel().registryAccess(), tag);
		helper.assertTrue(!stack.isEmpty() && stack.getCount() == count, "Legacy stack lost/clamped: " + id);
		ItemStack restored = ItemStack.parseOptional(helper.getLevel().registryAccess(),
				(CompoundTag) stack.save(helper.getLevel().registryAccess()));
		helper.assertTrue(ItemStack.matches(stack, restored), "Legacy item save/reload changed: " + id);
		return restored;
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void infiniteWineAmplifiesInstantAndLastingEffectsOnce(GameTestHelper helper) {
		var wine = (InfiniteWineItem) ExtraBotanyItems.infiniteWine;
		ItemStack healingWine = new ItemStack(wine);
		BaseBrewItem.setBrew(healingWine, BotaniaBrews.MENDING);
		var healingEffects = wine.getBrew(healingWine).getPotionEffects(healingWine);
		helper.assertTrue(healingEffects.size() == 1 && healingEffects.getFirst().getEffect().equals(MobEffects.HEAL),
				"Mending fixture must contain one instant healing effect");
		var healing = healingEffects.getFirst();
		var actual = helper.makeMockPlayer(GameType.SURVIVAL);
		var expected = helper.makeMockPlayer(GameType.SURVIVAL);
		for (var player : List.of(actual, expected)) {
			player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
			player.setHealth(1);
		}
		// Compare the real drink path with one upstream amplifier increment. Extra headroom
		// prevents the player's health cap from hiding the former double increment.
		healing.getEffect().value().applyInstantenousEffect(expected, expected, expected, healing.getAmplifier() + 1, 1F);
		wine.finishUsingItem(healingWine, helper.getLevel(), actual);
		helper.assertTrue(actual.getHealth() == expected.getHealth() && actual.getHealth() > 1,
				"Infinite wine must amplify instant healing exactly once");
		helper.assertTrue(wine.getSwigsLeft(healingWine) == 11, "Instant brew must consume one swig");

		ItemStack lastingWine = new ItemStack(wine);
		BaseBrewItem.setBrew(lastingWine, ExtraBotanyBrews.revolution);
		var lastingPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
		wine.finishUsingItem(lastingWine, helper.getLevel(), lastingPlayer);
		var haste = lastingPlayer.getEffect(MobEffects.DIG_SPEED);
		helper.assertTrue(haste != null && haste.getAmplifier() == 3 && haste.getDuration() == 2700,
				"Infinite wine must preserve one lasting-effect level increment and 1.5x duration");
		helper.assertTrue(wine.getSwigsLeft(lastingWine) == 11, "Lasting brew must consume one swig");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void confirmedRunesPreserveCountsAndCraft(GameTestHelper helper) {
		for (int count : new int[] { 2, 4 }) loadOld(helper, "element_rune", count);
		for (int count : new int[] { 2, 6 }) loadOld(helper, "sin_rune", count);
		for (String kind : List.of("element", "sin")) {
			var names = kind.equals("element")
					? List.of("air", "earth", "water", "fire", "spring", "summer", "autumn", "winter")
					: List.of("mana", "pride", "gluttony", "wrath", "greed", "envy", "lust", "sloth");
			var inputs = new java.util.ArrayList<ItemStack>();
			inputs.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("botania:gaia_spirit"))));
			for (String name : names) inputs.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("botania:rune_of_" + name))));
			helper.assertTrue(inputs.stream().noneMatch(ItemStack::isEmpty), "Missing Botania recipe ingredient");
			var input = CraftingInput.of(3, 3, inputs);
			var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
			ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
			helper.assertTrue(output.is(kind.equals("element") ? ExtraBotanyItems.elementRune : ExtraBotanyItems.sinRune)
					&& output.getCount() == 8, "Legacy rune recipe output changed: " + kind);
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void confirmedDiscLoadsPlaysSavesAndEjects(GameTestHelper helper) {
		// Existing stored count=2 must survive even though the item max stack size is one.
		ItemStack old = loadOld(helper, "music_disc_herrscher_of_the_void", 2);
		var song = JukeboxSong.fromStack(helper.getLevel().registryAccess(), old).orElseThrow().value();
		// 这段音频长 47.5 秒。以前曲目写的是 201 秒，那是另一张唱片的长度。
		// The audio is 47.5 seconds long. Before, the song said 201 seconds, which is the length of the other record.
		helper.assertTrue(song.soundEvent().value().getLocation().equals(ResourceLocation.parse("extrabotany:music.herrscher"))
				&& song.lengthInSeconds() == 48.0F && song.comparatorOutput() == 1, "Legacy song binding changed");
		BlockPos pos = new BlockPos(2, 2, 2);
		helper.setBlock(pos, Blocks.JUKEBOX);
		JukeboxBlockEntity jukebox = helper.getBlockEntity(pos);
		jukebox.setTheItem(old.split(1));
		helper.assertTrue(old.getCount() == 1 && jukebox.getSongPlayer().isPlaying()
				&& jukebox.getComparatorOutput() == 1, "Restored disc did not start playing in jukebox");
		var saved = jukebox.saveWithoutMetadata(helper.getLevel().registryAccess());
		jukebox.loadWithComponents(saved, helper.getLevel().registryAccess());
		helper.assertTrue(jukebox.getTheItem().is(ExtraBotanyItems.recordHerrscherOfTheVoid), "Jukebox save/reload lost disc");
		ItemStack removed = jukebox.splitTheItem(1);
		helper.assertTrue(removed.is(ExtraBotanyItems.recordHerrscherOfTheVoid) && removed.getCount() == 1
				&& jukebox.getTheItem().isEmpty(), "Jukebox did not return the exact restored disc");
		helper.succeed();
	}
}
