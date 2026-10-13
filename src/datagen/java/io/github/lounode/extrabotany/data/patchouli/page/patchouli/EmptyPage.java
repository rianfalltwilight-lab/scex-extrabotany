package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

/**
 * Patchouli 自带的"空白"页面。
 * The "empty" page of Patchouli.
 */
public class EmptyPage extends AbstractPage<EmptyPage> {

	public EmptyPage drawFiller(boolean drawFiller) {
		object.addProperty("draw_filler", drawFiller);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:crafting");
	}
}
