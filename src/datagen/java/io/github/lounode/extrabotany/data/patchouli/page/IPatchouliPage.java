package io.github.lounode.extrabotany.data.patchouli.page;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

/**
 * 图鉴的一页：页面类型和页面的 JSON。
 * One page of the lexicon: the page type and the JSON of the page.
 */
public interface IPatchouliPage {
	ResourceLocation getType();

	JsonObject build();

}
