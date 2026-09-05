package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class LinkPage extends AbstractPage<LinkPage> {

	public LinkPage(String url, String linkText, String text) {
		this.object.addProperty("url", url);
		this.object.addProperty("link_text", linkText);
		this.object.addProperty("text", text);
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:link");
	}
}
