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

import vazkii.botania.common.component.BotaniaDataComponents;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.UUID;

/**
 * 用莱茵河的黄金改变一件遗物的灵魂绑定。黄金绑定了谁，遗物就改成绑定谁。黄金没有绑定时，遗物的绑定被清除。
 * Changes the soulbind of a relic with Das Rheingold. The relic gets the binding of the gold. When the gold has no binding, the binding of the relic is removed.
 */
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
			} else {
				return false;
			}
		}

		return findRheinGold && findRelic;
	}

	@Override
	public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
		if (!matches(container, null)) {
			return ItemStack.EMPTY;
		}

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

		ItemStack relicOutput = relic.copyWithCount(1);
		UUID rheingoldUUID = getSoulbindUUID(rheingold);

		if (rheingoldUUID == null) {
			relicOutput.remove(BotaniaDataComponents.SOULBOUND);
			ItemNBTHelper.removeEntry(relicOutput, TAG_SOULBIND_UUID);
		} else {
			bindToUUID(relicOutput, rheingoldUUID);
		}

		return relicOutput;
	}

	public static void bindToUUID(ItemStack stack, UUID uuid) {
		var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null) {
			relic.bindToUUID(uuid);
			ItemNBTHelper.removeEntry(stack, TAG_SOULBIND_UUID);
			return;
		}
		// The transferable binding on Rheingold itself retains its legacy storage.
		ItemNBTHelper.setString(stack, TAG_SOULBIND_UUID, uuid.toString());
	}

	@Nullable
	public static UUID getSoulbindUUID(ItemStack stack) {
		var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null && relic.getSoulbindUUID() != null) {
			return relic.getSoulbindUUID();
		}
		if (ItemNBTHelper.verifyExistance(stack, TAG_SOULBIND_UUID)) {
			try {
				return UUID.fromString(ItemNBTHelper.getString(stack, TAG_SOULBIND_UUID, ""));
			} catch (IllegalArgumentException ex) { // Invalid legacy bindings behave as unbound without changing recipe inputs.
				return null;
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
