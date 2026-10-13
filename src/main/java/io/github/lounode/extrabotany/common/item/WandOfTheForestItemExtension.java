package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.Optional;

/**
 * 森林法杖上"为魔链星选中的魔力池"的读写方法。这份数据放在法杖的自定义数据里，和植物魔法自己的绑定数据分开。
 * The read and write methods for "the mana pool that was selected for a Manalink" on the Wand of the Forest. This data is in the custom data of the wand, separate from the binding data of Botania.
 */
public interface WandOfTheForestItemExtension {
	String TAG_EXTEND_BOUND = "boundExtension";

	static void setBindingAttemptExtend(ItemStack stack, GlobalPos pos) {
		GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
				.result()
				.ifPresent((tag) -> ItemNBTHelper.set(stack, TAG_EXTEND_BOUND, tag));
	}

	static Optional<GlobalPos> getBindingAttempt(ItemStack stack) {
		GlobalPos pos = GlobalPos.CODEC.parse(NbtOps.INSTANCE, ItemNBTHelper.get(stack, TAG_EXTEND_BOUND))
				.result()
				.orElse(null);
		if (pos == null || pos.pos().getY() == Integer.MIN_VALUE) {
			return Optional.empty();
		}

		return Optional.of(pos);
	}
}
