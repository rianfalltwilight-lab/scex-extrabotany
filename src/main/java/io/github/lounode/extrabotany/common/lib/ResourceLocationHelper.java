package io.github.lounode.extrabotany.common.lib;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationHelper {
	public static ResourceLocation prefix(String path) {
		return ResourceLocation.tryBuild(LibMisc.MOD_ID, path);
	}

	public static ResourceLocation prefixBotania(String path) {
		return ResourceLocation.tryBuild("botania", path);
	}

	public static ResourceLocation location(String namespace, String path) {
		return ResourceLocation.tryBuild(namespace, path);
	}
}
