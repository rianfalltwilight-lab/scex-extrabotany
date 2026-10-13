package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

/**
 * 植物魔法的"魔力灌注"配方页面。
 * The "mana infusion" recipe page of Botania.
 */
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
