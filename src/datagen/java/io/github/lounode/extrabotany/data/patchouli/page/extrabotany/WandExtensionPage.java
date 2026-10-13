package io.github.lounode.extrabotany.data.patchouli.page.extrabotany;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import io.github.lounode.extrabotany.common.lib.RegistryHelper;
import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

/**
 * "把魔力读取器装到森林法杖上"的页面。这个升级是一条动态配方，所以页面直接写出输入和输出，而不是引用一条配方。
 * The page for "put the Mana Reader on the Wand of the Forest". This upgrade is a dynamic recipe, thus the page gives the inputs and the output directly and does not refer to a recipe.
 */
public final class WandExtensionPage extends AbstractPage<WandExtensionPage> {
    public WandExtensionPage(ItemLike wandItem) {
        String wand = RegistryHelper.getRegistryName(wandItem.asItem()).toString();
        object.addProperty("heading", "extrabotany.page.mana_reader3");
        object.addProperty("input1", wand);
        object.addProperty("input2", "extrabotany:mana_reader");
        object.addProperty("output", wand + "[minecraft:custom_data={extrabotany:{mana_reader:{enable:1b}}}]");
        object.addProperty("shapeless", true);
    }

    @Override
    public ResourceLocation getType() {
        return ResourceLocation.parse("extrabotany:craft_manual");
    }
}
