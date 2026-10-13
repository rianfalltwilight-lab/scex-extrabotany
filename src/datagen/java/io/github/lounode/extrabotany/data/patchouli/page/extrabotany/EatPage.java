package io.github.lounode.extrabotany.data.patchouli.page.extrabotany;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

import static io.github.lounode.extrabotany.common.lib.RegistryHelper.getRegistryName;

/**
 * 本模组的"吃下获得"页面：吃下输入物品，得到输出物品。
 * The "eat to get" page of this mod: eat the input item and get the output item.
 */
public class EatPage extends AbstractPage<EatPage> {

	public EatPage(ItemLike input, ItemLike output) {
		object.addProperty("input", getRegistryName(input.asItem()).toString());
		object.addProperty("output", getRegistryName(output.asItem()).toString());
	}

	public EatPage withTitle(@Translatable String title) {
		object.addProperty("heading", title);
		return this;
	}

	public EatPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("extrabotany:eat");
	}
}
