package io.github.lounode.extrabotany.common.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

import io.github.lounode.extrabotany.common.integration.kubejs.schema.EdelweissSchema;
import io.github.lounode.extrabotany.common.integration.kubejs.schema.NaturePedestalSchema;
import io.github.lounode.extrabotany.common.integration.kubejs.schema.OmnivioletSchema;
import io.github.lounode.extrabotany.common.integration.kubejs.schema.StonesiaSchema;
import io.github.lounode.extrabotany.common.integration.kubejs.util.BlockStateComponent;
import io.github.lounode.extrabotany.common.lib.LibMisc;

/**
 * 本模组的 KubeJS 插件。有了它，脚本可以用 {@code event.recipes.extrabotany} 添加本模组的四种配方：祭坛锤击、石中姬、全知瑾和雪绒花。
 * The KubeJS plugin of this mod. With it, a script can add the four recipe types of this mod with {@code event.recipes.extrabotany}: pedestal hammer, Stonesia, Omniviolet and Edelweiss.
 *
 * <p>KubeJS 从 {@code kubejs.plugins.txt} 找到这个类。没有安装 KubeJS 时，没有代码加载它。
 * <p>KubeJS finds this class from {@code kubejs.plugins.txt}. When KubeJS is not installed, no code loads this class.
 *
 * <p>用法和例子在 {@code docs/kubejs.md}。
 * <p>The use and the examples are in {@code docs/kubejs.md}.
 */
public class KubeJSExtrabotanyPlugin implements KubeJSPlugin {

	@Override
	public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
		registry.register(BlockStateComponent.TYPE);
	}

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		registry.namespace(LibMisc.MOD_ID)
				.register("pedestal_smash", NaturePedestalSchema.SCHEMA)
				.register("stonesia", StonesiaSchema.SCHEMA)
				.register("omniviolet", OmnivioletSchema.SCHEMA)
				.register("edelweiss", EdelweissSchema.SCHEMA);

	}
}
