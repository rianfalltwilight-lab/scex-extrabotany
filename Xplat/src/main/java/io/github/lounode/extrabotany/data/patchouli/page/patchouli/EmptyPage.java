package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

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
