package io.github.lounode.extrabotany.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * 本模组创造模式物品栏的注册键。
 * The registry key of the creative tab of this mod.
 */
public class ExtraBotaniaRegistries {
	public static final ResourceKey<CreativeModeTab> EXTRA_BOTANIA_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			ResourceLocation.tryBuild(ExtraBotanyAPI.MODID, "extrabotany"));
}
