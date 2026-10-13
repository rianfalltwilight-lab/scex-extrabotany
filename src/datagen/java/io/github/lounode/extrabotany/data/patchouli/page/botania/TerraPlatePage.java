package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;

/**
 * 植物魔法的"泰拉凝聚板"配方页面。
 * The "terrestrial agglomeration" recipe page of Botania.
 */
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
