package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class BrewPage extends AbstractPage<BrewPage> {
	public BrewPage(String recipe) {
		object.addProperty("recipe", recipe);
	}

	public BrewPage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public BrewPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	public BrewPage withFlavor(@Translatable String flavor) {
		object.addProperty("flavor", flavor);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("botania:brew");
	}
}
