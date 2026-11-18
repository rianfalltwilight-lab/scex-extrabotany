package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.crafting.RunicAltarRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.ConventionalBotaniaTags;
import net.neoforged.neoforge.common.Tags;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 生成符文祭坛配方。
 * Makes these recipes. The runic altar recipes.
 */
public class RunicAltarProvider extends ExtraBotanyRecipeProvider {

	public RunicAltarProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public String getName() {
		return "ExtraBotany runic altar recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		Consumer<FinishedRecipe> consumer = recipe -> recipeOutput.accept(recipe.id,
				new RunicAltarRecipe(recipe.output, Ingredient.of(BotaniaBlocks.LIVINGROCK), recipe.mana,
						recipe.inputs, new Ingredient[0]), null);
		final int costTier1 = 5200;
		final int costTier2 = 8000;
		final int costTier3 = 12000;

		//Ingredient manaSteel = Ingredient.of(BotaniaTags.Items.INGOTS_MANASTEEL);

		//Zadkiel
		consumer.accept(new FinishedRecipe(idFor("zadkiel"), new ItemStack(ExtraBotanyItems.zadkiel), 500000,
				Ingredient.of(Items.ICE),
				Ingredient.of(Items.BLUE_ICE),
				Ingredient.of(Items.PACKED_ICE),
				Ingredient.of(Items.SNOW_BLOCK),
				Ingredient.of(Items.POWDER_SNOW_BUCKET),
				Ingredient.of(Items.TOTEM_OF_UNDYING)
		));
		// 对照原版审计 E5。奥利哈钢只在泰拉凝聚板上做（1.16 terra_plate/orichalcos.json），这里不再有 1.12 的符文祭坛配方。
		// Parity audit E5. Orichalcos is only made on the terrestrial agglomeration plate (1.16 terra_plate/orichalcos.json); the runic altar recipe of 1.12 is gone.
		// 暗影锭和光子锭：1.16 runic_altar/shadowium(_).json 和 photonium(_).json，各有一份 1 个和一份 3 个的配方。
		// Shadowium and photonium: 1.16 runic_altar/shadowium(_).json and photonium(_).json, each with a recipe for 1 and a recipe for 3.
		Ingredient elementium = Ingredient.of(ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS);
		Ingredient mashed = Ingredient.of(ExtraBotanyItems.gildedPotatoMashed);
		Ingredient nightmare = Ingredient.of(ExtraBotanyItems.nightmareFuel);
		Ingredient spiritFuel = Ingredient.of(ExtraBotanyItems.spiritFuel);
		consumer.accept(new FinishedRecipe(idFor("shadowium_ingot"), new ItemStack(ExtraBotanyItems.shadowium), 6000,
				elementium, mashed, nightmare, nightmare));
		consumer.accept(new FinishedRecipe(idFor("shadowium_ingot_triple"), new ItemStack(ExtraBotanyItems.shadowium, 3), 15000,
				elementium, elementium, elementium, mashed, nightmare, nightmare, nightmare, nightmare, nightmare));
		consumer.accept(new FinishedRecipe(idFor("photonium_ingot"), new ItemStack(ExtraBotanyItems.photonium), 6000,
				elementium, mashed, spiritFuel, spiritFuel));
		consumer.accept(new FinishedRecipe(idFor("photonium_ingot_triple"), new ItemStack(ExtraBotanyItems.photonium, 3), 15000,
				elementium, elementium, elementium, mashed, spiritFuel, spiritFuel, spiritFuel, spiritFuel, spiritFuel));
		// 1.16 RuneRecipeProvider：4 个马铃薯和 1 个金锭，出 4 个镀金马铃薯，2000 魔力。
		// RuneRecipeProvider of 1.16: 4 potatoes and 1 gold ingot give 4 gilded potatoes, 2000 mana.
		Ingredient potato = Ingredient.of(Items.POTATO);
		consumer.accept(new FinishedRecipe(idFor("gilded_potato"), new ItemStack(ExtraBotanyItems.gildedPotato, 4), 2000,
				potato, potato, potato, potato, Ingredient.of(Tags.Items.INGOTS_GOLD)));
		// 对照原版审计 E6：1.12 ModRuneRecipe.recipeUltimatehammer，100000 魔力。 / Parity audit E6: recipeUltimatehammer of ModRuneRecipe of 1.12, 100000 mana.
		consumer.accept(new FinishedRecipe(idFor(ExtraBotanyItems.ultimateHammer), new ItemStack(ExtraBotanyItems.ultimateHammer), 100000,
				Ingredient.of(ExtraBotanyItems.terrasteelHammer), mashed, mashed, mashed, Ingredient.of(Items.GOLD_BLOCK)));
		// 1.12 ModRuneRecipe：成龙指环和蜘蛛指环，2000 魔力（重燃用工作台）。
		// ModRuneRecipe of 1.12: the Jackie Chan ring and the spider ring, 2000 mana (Reburn used the crafting table).
		Ingredient manasteel = Ingredient.of(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS);
		consumer.accept(new FinishedRecipe(idFor(ExtraBotanyItems.jackieChanRing), new ItemStack(ExtraBotanyItems.jackieChanRing), 2000,
				manasteel, manasteel, Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(BotaniaItems.RUNE_OF_EARTH), Ingredient.of(Items.STICKY_PISTON)));
		consumer.accept(new FinishedRecipe(idFor(ExtraBotanyItems.spiderRing), new ItemStack(ExtraBotanyItems.spiderRing), 2000,
				manasteel, manasteel, Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(BotaniaItems.RUNE_OF_EARTH), Ingredient.of(Items.STONE)));
		// 1.12 ModRuneRecipe.recipeDarkened：女仆服和四件暗影战士盔甲，50000 魔力（重燃是嗜血击杀计数）。
		// ModRuneRecipe.recipeDarkened of 1.12: the maid suit and the four Shadow Warrior pieces, 50000 mana (Reburn counted kills under Bloodthirst).
		consumer.accept(new FinishedRecipe(idFor(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit), new ItemStack(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit), 50000,
				Ingredient.of(ExtraBotanyItems.pleiadesCombatMaidSuit),
				Ingredient.of(ExtraBotanyItems.shadowWarriorHelmet),
				Ingredient.of(ExtraBotanyItems.shadowWarriorChestplate),
				Ingredient.of(ExtraBotanyItems.shadowWarriorLeggings),
				Ingredient.of(ExtraBotanyItems.shadowWarriorBoots)));
		consumer.accept(new FinishedRecipe(idFor(ExtraBotanyItems.orichalcosHammer), new ItemStack(ExtraBotanyItems.orichalcosHammer), ManaPoolBlock.MAX_MANA,
				Ingredient.of(ExtraBotanyItems.orichalcos),
				Ingredient.of(ExtraBotanyItems.gildedPotatoMashed),
				Ingredient.of(ExtraBotanyItems.theChaos),
				Ingredient.of(ExtraBotanyItems.theOrigin),
				Ingredient.of(ExtraBotanyItems.theEnd)
		));

	}

	private static ResourceLocation idFor(String s) {
		return prefix("runic_altar/" + s);
	}

	private static ResourceLocation idFor(ItemLike itemLike) {
		return prefix("runic_altar/" + BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath());
	}

	protected static class FinishedRecipe {
		private final ResourceLocation id;
		private final ItemStack output;
		private final int mana;
		private final Ingredient[] inputs;

		protected FinishedRecipe(ResourceLocation id, ItemStack output, int mana, Ingredient... inputs) {
			this.id = id;
			this.output = output;
			this.mana = mana;
			this.inputs = inputs;
		}

	}
}
