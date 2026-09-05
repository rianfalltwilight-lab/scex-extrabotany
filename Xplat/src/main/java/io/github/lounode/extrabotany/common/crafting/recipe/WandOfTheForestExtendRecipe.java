package io.github.lounode.extrabotany.common.crafting.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.item.WandOfTheForestItem;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

public class WandOfTheForestExtendRecipe extends CustomRecipe {
	public static final RecipeSerializer<WandOfTheForestExtendRecipe> SERIALIZER =
			new SimpleCraftingRecipeSerializer<>(WandOfTheForestExtendRecipe::new);

	public WandOfTheForestExtendRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		boolean findWand = false;
		boolean findReader = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof WandOfTheForestItem) {
				if (findWand) {
					return false;
				}
				findWand = true;
			} else if (stack.is(ExtraBotanyItems.manaReader)) {
				if (findReader) {
					return false;
				}
				findReader = true;
			}
		}

		return findWand && findReader;
	}

	@NotNull
	@Override
	public ItemStack assemble(CraftingInput inv, @NotNull HolderLookup.Provider registries) {
		ItemStack wand = ItemStack.EMPTY;
		ItemStack reader = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof WandOfTheForestItem) {
				wand = stack;
			} else {
				reader = stack;
			}
		}

		if (wand.isEmpty() || reader.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack wandOutput = wand.copy();

		CompoundTag extraBotanyTag = new CompoundTag();
		CompoundTag manaReaderTag = new CompoundTag();
		manaReaderTag.putBoolean("enable", true);
		extraBotanyTag.put("mana_reader", manaReaderTag);
		ItemNBTHelper.set(wandOutput, "extrabotany", extraBotanyTag);

		return wandOutput;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width > 1 || height > 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
