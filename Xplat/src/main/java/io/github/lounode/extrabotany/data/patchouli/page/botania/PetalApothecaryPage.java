package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class PetalApothecaryPage extends AbstractPage<PetalApothecaryPage> {

	public PetalApothecaryPage(String recipe) {
		object.addProperty("recipe", recipe);
	}

	public PetalApothecaryPage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public PetalApothecaryPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("botania:petal_apothecary");
	}
}
