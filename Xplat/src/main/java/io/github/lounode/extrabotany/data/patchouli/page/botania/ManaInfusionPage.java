package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class ManaInfusionPage extends AbstractPage<ManaInfusionPage> {

	public ManaInfusionPage(String recipe) {
		object.addProperty("recipes", recipe);
	}

	public ManaInfusionPage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public ManaInfusionPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("botania:mana_infusion");
	}
}
