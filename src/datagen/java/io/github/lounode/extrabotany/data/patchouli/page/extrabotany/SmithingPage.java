package io.github.lounode.extrabotany.data.patchouli.page.extrabotany;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

/**
 * 锻造台配方页面。Patchouli 自带的锻造页面不显示模板物品，所以本模组有自己的模板。
 * The smithing table recipe page. The smithing page of Patchouli does not show the template item, thus this mod has its own template.
 */
public class SmithingPage extends AbstractPage<SmithingPage> {
	public SmithingPage(String recipe) {
		object.addProperty("recipe", recipe);
	}

	public SmithingPage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public SmithingPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("extrabotany:smithing");
	}
}
