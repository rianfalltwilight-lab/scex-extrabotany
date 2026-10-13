package io.github.lounode.extrabotany.common.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

/**
 * 本模组的进度触发器。
 * The advancement triggers of this mod.
 */
public final class ExtrabotanyCriteriaTriggers {
	private ExtrabotanyCriteriaTriggers() {}

	public static void init(BiConsumer<CriterionTrigger<?>, ResourceLocation> register) {
		register.accept(ItemUsedTrigger.INSTANCE, ItemUsedTrigger.ID);
		register.accept(ManaChargeTrigger.INSTANCE, ManaChargeTrigger.ID);
		register.accept(HasArmorSetTrigger.INSTANCE, HasArmorSetTrigger.ID);
	}
}
