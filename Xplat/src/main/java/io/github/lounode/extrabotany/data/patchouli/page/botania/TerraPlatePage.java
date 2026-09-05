package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;

public class TerraPlatePage extends AbstractPage<TerraPlatePage> {

	public TerraPlatePage(String recipe) {
		object.addProperty("recipe", recipe);
	}

	public TerraPlatePage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public TerraPlatePage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return prefixBotania("terrasteel");
	}
}
