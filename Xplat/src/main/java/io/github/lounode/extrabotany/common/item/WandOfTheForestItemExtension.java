package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.Optional;

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
