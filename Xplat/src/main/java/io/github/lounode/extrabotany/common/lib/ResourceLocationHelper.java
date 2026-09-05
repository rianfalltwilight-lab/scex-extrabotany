package io.github.lounode.extrabotany.common.lib;

import net.minecraft.client.resources.model.ModelResourceLocation;
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

	public static ModelResourceLocation modelResourceLocation(String path, String variant) {
		return new ModelResourceLocation(prefix(path), variant);
	}
}
