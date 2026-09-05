package io.github.lounode.extrabotany.data.patchouli.page;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

public interface IPatchouliPage {
	ResourceLocation getType();

	JsonObject build();

}
