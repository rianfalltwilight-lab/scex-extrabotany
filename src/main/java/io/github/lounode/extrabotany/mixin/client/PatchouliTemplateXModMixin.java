package io.github.lounode.extrabotany.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.patchouli.common.book.BookRegistry;

import io.github.lounode.extrabotany.common.util.PatchouliUtil;

@Mixin(BookRegistry.class)
public class PatchouliTemplateXModMixin {
	@Inject(method = "reloadContents", at = @At("HEAD"), remap = false)
	private void onReload(CallbackInfo ci) {
		PatchouliUtil.onPatchouliReload(BookRegistry.INSTANCE.books);
	}
}
