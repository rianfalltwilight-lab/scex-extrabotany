package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class TextPage extends AbstractPage<TextPage> {

	public TextPage(@Translatable String text) {
		object.addProperty("text", text);
	}

	public TextPage withTitle(String title) {
		object.addProperty("title", title);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:text");
	}
}
