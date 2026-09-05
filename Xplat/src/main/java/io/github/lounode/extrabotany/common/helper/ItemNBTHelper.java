package io.github.lounode.extrabotany.common.helper;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Compatibility access for ExtraBotany's legacy per-stack data.
 *
 * <p>Minecraft 1.21 stores this payload in {@link DataComponents#CUSTOM_DATA};
 * callers intentionally retain their historical keys so existing stacks can be
 * upgraded without rewriting every feature at once.</p>
 */
public final class ItemNBTHelper {
	private ItemNBTHelper() {}

	private static CompoundTag copyTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	public static boolean verifyExistance(ItemStack stack, String key) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(key);
	}

	public static Tag get(ItemStack stack, String key) {
		Tag value = copyTag(stack).get(key);
		return value == null ? new CompoundTag() : value;
	}

	public static void set(ItemStack stack, String key, Tag value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(key, value.copy()));
	}

	public static boolean getBoolean(ItemStack stack, String key, boolean fallback) {
		return verifyExistance(stack, key) ? copyTag(stack).getBoolean(key) : fallback;
	}

	public static void setBoolean(ItemStack stack, String key, boolean value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(key, value));
	}

	public static int getInt(ItemStack stack, String key, int fallback) {
		return verifyExistance(stack, key) ? copyTag(stack).getInt(key) : fallback;
	}

	public static void setInt(ItemStack stack, String key, int value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(key, value));
	}

	public static long getLong(ItemStack stack, String key, long fallback) {
		return verifyExistance(stack, key) ? copyTag(stack).getLong(key) : fallback;
	}

	public static void setLong(ItemStack stack, String key, long value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(key, value));
	}

	public static String getString(ItemStack stack, String key, String fallback) {
		return verifyExistance(stack, key) ? copyTag(stack).getString(key) : fallback;
	}

	public static void setString(ItemStack stack, String key, String value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, value));
	}

	public static ListTag getList(ItemStack stack, String key, int elementType, boolean nullable) {
		CompoundTag tag = copyTag(stack);
		if (!tag.contains(key, Tag.TAG_LIST)) {
			return nullable ? null : new ListTag();
		}
		ListTag list = tag.getList(key, elementType);
		return list.isEmpty() && nullable ? null : list;
	}

	public static void setList(ItemStack stack, String key, ListTag value) {
		set(stack, key, value);
	}

	public static void removeEntry(ItemStack stack, String key) {
		CompoundTag tag = copyTag(stack);
		tag.remove(key);
		if (tag.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
		} else {
			CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
		}
	}

	public static JsonElement serializeStack(ItemStack stack) {
		return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).getOrThrow();
	}
}
