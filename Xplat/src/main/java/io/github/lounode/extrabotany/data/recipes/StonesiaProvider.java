package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.StateIngredients;

import io.github.lounode.extrabotany.common.crafting.StonesiasRecipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.location;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class StonesiaProvider extends ExtraBotanyRecipeProvider {
	public StonesiaProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		Consumer<Result> consumer = result -> recipeOutput.accept(result.id(),
				new StonesiasRecipe(result.input(), result.outputMana()), null);
		//Stone
		consumer.accept(new Result(id("stone"), forTag(BlockTags.BASE_STONE_OVERWORLD), 20));
		consumer.accept(new Result(id("cobblestone"), forTag(location("c", "cobblestones")), 10));
		//Minecraft Ores
		consumer.accept(new Result(id("coal_ore"), vanillaOre(BlockTags.COAL_ORES), 305));
		consumer.accept(new Result(id("iron_ore"), vanillaOre(BlockTags.IRON_ORES), 370));
		consumer.accept(new Result(id("diamond_ore"), vanillaOre(BlockTags.DIAMOND_ORES), 750));
		consumer.accept(new Result(id("redstone_ore"), vanillaOre(BlockTags.REDSTONE_ORES), 330));
		consumer.accept(new Result(id("lapis_ore"), vanillaOre(BlockTags.LAPIS_ORES), 290));
		consumer.accept(new Result(id("gold_ore"), vanillaOre(BlockTags.GOLD_ORES), 580));
		consumer.accept(new Result(id("emerald_ore"), vanillaOre(BlockTags.EMERALD_ORES), 690));
		consumer.accept(new Result(id("copper_ore"), vanillaOre(BlockTags.COPPER_ORES), 320));
		consumer.accept(new Result(id("quartz_ore"), ore("quartz"), 340));

		//Mod ores
		consumer.accept(new Result(id("tin_ore"), ore("tin"), 320));//锡
		consumer.accept(new Result(id("aluminum_ore"), ore("aluminum"), 390));//铝
		consumer.accept(new Result(id("lead_ore"), ore("lead"), 390));//铅
		consumer.accept(new Result(id("nickel_ore"), ore("nickel"), 480));//镍
		consumer.accept(new Result(id("silver_ore"), ore("silver"), 580));//银
		consumer.accept(new Result(id("zinc_ore"), ore("zinc"), 340));//锌
		consumer.accept(new Result(id("platinum_ore"), ore("platinum"), 800));//铂
		consumer.accept(new Result(id("uranium_ore"), ore("uranium"), 580));//铀
		consumer.accept(new Result(id("osmium_ore"), ore("osmium"), 450));//锇
		consumer.accept(new Result(id("cinnabar_ore"), ore("cinnabar"), 470));//朱砂
		consumer.accept(new Result(id("sulfur_ore"), ore("sulfur"), 290));//硫
		consumer.accept(new Result(id("apatite_ore"), ore("apatite"), 100));//磷灰石
		consumer.accept(new Result(id("niter_ore"), ore("niter"), 470));//硝石
		consumer.accept(new Result(id("tungsten_ore"), ore("tungsten"), 580));//钨
		consumer.accept(new Result(id("ruby_ore"), ore("ruby"), 800));//红宝石
		consumer.accept(new Result(id("sapphire_ore"), ore("sapphire"), 800));//蓝宝石
		consumer.accept(new Result(id("iridium_ore"), ore("iridium"), 800));//铱

		//consumer.accept(new Result(id("amber_ore"), ore("amber"), 470));//琥珀
		//consumer.accept(new Result(id("cobalt_ore"), ore("cobalt"), 540));//钴
		//consumer.accept(new Result(id("ardite_ore"), ore("ardite"), 540));//阿迪特
		//consumer.accept(new Result(id("mithril_ore"), ore("mithril"), 1300));//秘银
	}

	protected ResourceLocation id(String id) {
		return prefix("stonesia/" + id);
	}

	protected static StateIngredient forBlock(Block block) {
		return StateIngredients.of(block);
	}

	protected static StateIngredient forTag(ResourceLocation tag) {
		return StateIngredients.of(TagKey.create(Registries.BLOCK, tag));
	}

	protected static StateIngredient forTag(TagKey<Block> tag) {
		return StateIngredients.of(tag);
	}

	protected static StateIngredient ore(String id) {
		return forTag(location("c", "ores/" + id));
	}

	protected static StateIngredient vanillaOre(TagKey<Block> tag) {
		StateIngredient modTagIngredient = ore(tag.location().getPath().replace("_ores", ""));

		return StateIngredients.anyOf(
				forTag(tag),
				modTagIngredient
		);
	}

	private record Result(ResourceLocation id, StateIngredient input, int outputMana) {}

	@Override
	public String getName() {
		return "Extrabotany Stonesia recipes";
	}
}
