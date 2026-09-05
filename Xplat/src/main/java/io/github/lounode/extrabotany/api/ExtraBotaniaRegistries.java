package io.github.lounode.extrabotany.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class ExtraBotaniaRegistries {
	public static final ResourceKey<CreativeModeTab> EXTRA_BOTANIA_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			ResourceLocation.tryBuild(ExtraBotanyAPI.MODID, "extrabotany"));
}
