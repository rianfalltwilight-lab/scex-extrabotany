package io.github.lounode.extrabotany.data.patchouli.page.botania;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

/**
 * 植物魔法的"精灵交易"配方页面。
 * The "elven trade" recipe page of Botania.
 */
public class ElvenTradePage extends AbstractPage<ElvenTradePage> {

	public ElvenTradePage(String recipe) {
		object.addProperty("recipes", recipe);
	}

	public ElvenTradePage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public ElvenTradePage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("botania:elven_trade");
	}
}
