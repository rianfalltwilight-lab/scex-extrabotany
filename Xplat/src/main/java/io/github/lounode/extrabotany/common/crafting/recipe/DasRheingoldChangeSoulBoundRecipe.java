package io.github.lounode.extrabotany.common.crafting.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.UUID;

public final class DasRheingoldChangeSoulBoundRecipe extends CustomRecipe {

	private static final String TAG_SOULBIND_UUID = "soulbindUUID";
	public static final RecipeSerializer<DasRheingoldChangeSoulBoundRecipe> SERIALIZER =
			new SimpleCraftingRecipeSerializer<>(DasRheingoldChangeSoulBoundRecipe::new);

	public DasRheingoldChangeSoulBoundRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		boolean findRheinGold = false;
		boolean findRelic = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(ExtraBotanyItems.dasRheingold)) {
				if (findRheinGold) {
					return false;
				}
				findRheinGold = true;
			} else if (io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack) != null) {
				if (findRelic) {
					return false;
				}
				findRelic = true;
			}
		}

		return findRheinGold && findRelic;
	}

	@Override
	public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
		ItemStack rheingold = ItemStack.EMPTY;
		ItemStack relic = ItemStack.EMPTY;

		for (int i = 0; i < container.size(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() == ExtraBotanyItems.dasRheingold) {
				rheingold = stack;
			} else {
				relic = stack;
			}
		}

		if (rheingold.isEmpty() || relic.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(relic) == null) {
			return ItemStack.EMPTY;
		}

		ItemStack relicOutput = relic.copy();
		UUID rheingoldUUID = getSoulbindUUID(rheingold);

		if (rheingoldUUID == null) {
			ItemNBTHelper.removeEntry(relicOutput, TAG_SOULBIND_UUID);
		} else {
			bindToUUID(relicOutput, rheingoldUUID);
		}

		return relicOutput;
	}

	public static void bindToUUID(ItemStack stack, UUID uuid) {
		ItemNBTHelper.setString(stack, TAG_SOULBIND_UUID, uuid.toString());
	}

	@Nullable
	public static UUID getSoulbindUUID(ItemStack stack) {
		if (ItemNBTHelper.verifyExistance(stack, TAG_SOULBIND_UUID)) {
			try {
				return UUID.fromString(ItemNBTHelper.getString(stack, TAG_SOULBIND_UUID, ""));
			} catch (IllegalArgumentException ex) { // Bad UUID in tag
				ItemNBTHelper.removeEntry(stack, TAG_SOULBIND_UUID);
			}
		}

		return null;
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
