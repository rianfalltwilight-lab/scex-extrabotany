package io.github.lounode.extrabotany.mixin.client;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.patchouli.client.book.BookContentsBuilder;
import vazkii.patchouli.client.book.template.BookTemplate;

import io.github.lounode.extrabotany.common.util.PatchouliUtil;

import java.util.function.Supplier;

@Mixin(BookContentsBuilder.class)
public class PatchouliTemplateXModMixinBuilder {
	@Inject(method = "getTemplate", at = @At("RETURN"), remap = false, cancellable = true)
	private void getTemplate(ResourceLocation id, CallbackInfoReturnable<Supplier<BookTemplate>> cir) {
		if (cir.getReturnValue() == null) {
			cir.setReturnValue(PatchouliUtil.INSTANCE.getTemplate(id));
		}
	}
}
