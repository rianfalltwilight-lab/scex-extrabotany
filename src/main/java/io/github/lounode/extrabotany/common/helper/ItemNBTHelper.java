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
 * 读写物品自定义数据的工具方法。
 * Utility methods to read and write the custom data of an item.
 *
 * <p>在 1.20 里，这些数据直接放在物品的 NBT 里。在 1.21 里，它们放在 {@link DataComponents#CUSTOM_DATA} 组件里。键名没有变化，所以旧存档里的物品不需要转换。
 * <p>In 1.20 this data was directly in the NBT of the item. In 1.21 it is in the {@link DataComponents#CUSTOM_DATA} component. The key names did not change, thus the items of old saves need no conversion.
 *
 * <p>读取方法不复制整个标签，所以它们可以用在每 tick 运行的代码里。
 * <p>The read methods do not copy the full tag, thus they can be used in code that runs on each tick.
 */
public final class ItemNBTHelper {
	private ItemNBTHelper() {}

	private static CompoundTag copyTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/**
	 * Read-only view of the stack's CUSTOM_DATA tag. Every getter used to deep-copy the whole
	 * tag ({@code copyTag()}) just to read one primitive, and getters are called from tick paths.
	 * The view must never be mutated or leaked; values returned to callers are copied.
	 */
	@SuppressWarnings("deprecation")
	private static CompoundTag view(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
	}

	public static boolean verifyExistance(ItemStack stack, String key) {
		return view(stack).contains(key);
	}

	public static Tag get(ItemStack stack, String key) {
		Tag value = view(stack).get(key);
		return value == null ? new CompoundTag() : value.copy();
	}

	public static void set(ItemStack stack, String key, Tag value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(key, value.copy()));
	}

	public static boolean getBoolean(ItemStack stack, String key, boolean fallback) {
		CompoundTag tag = view(stack);
		return tag.contains(key) ? tag.getBoolean(key) : fallback;
	}

	public static void setBoolean(ItemStack stack, String key, boolean value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(key, value));
	}

	public static int getInt(ItemStack stack, String key, int fallback) {
		CompoundTag tag = view(stack);
		return tag.contains(key) ? tag.getInt(key) : fallback;
	}

	public static void setInt(ItemStack stack, String key, int value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(key, value));
	}

	public static long getLong(ItemStack stack, String key, long fallback) {
		CompoundTag tag = view(stack);
		return tag.contains(key) ? tag.getLong(key) : fallback;
	}

	public static void setLong(ItemStack stack, String key, long value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(key, value));
	}

	public static String getString(ItemStack stack, String key, String fallback) {
		CompoundTag tag = view(stack);
		return tag.contains(key) ? tag.getString(key) : fallback;
	}

	public static void setString(ItemStack stack, String key, String value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, value));
	}

	public static ListTag getList(ItemStack stack, String key, int elementType, boolean nullable) {
		CompoundTag tag = view(stack);
		if (!tag.contains(key, Tag.TAG_LIST)) {
			return nullable ? null : new ListTag();
		}
		ListTag list = tag.getList(key, elementType);
		if (list.isEmpty() && nullable) {
			return null;
		}
		return list.copy();
	}

	public static void setList(ItemStack stack, String key, ListTag value) {
		set(stack, key, value);
	}

	public static void removeEntry(ItemStack stack, String key) {
		if (!view(stack).contains(key)) {
			// Previously rewrote CUSTOM_DATA (new component instance -> slot resync) even when absent.
			return;
		}
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
