package io.github.lounode.extrabotany.data.patchouli.page;

import com.google.gson.JsonObject;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

/**
 * 图鉴页面的基类。子类把自己的字段写进一个 JSON 对象。
 * The base class of the lexicon pages. A subclass writes its fields into a JSON object.
 */
public abstract class AbstractPage<T extends AbstractPage<T>> implements IPatchouliPage {
	protected JsonObject object = new JsonObject();

	@SuppressWarnings("unchecked")
	public T withAdvancement(ResourceLocation advancement) {
		object.addProperty("advancement", advancement.toString());
		return (T) this;
	}

	public T withAdvancement(AdvancementHolder advancement) {
		return withAdvancement(advancement.id());
	}

	@SuppressWarnings("unchecked")
	public T withFlags(String flags) {
		object.addProperty("flag", flags);
		return (T) this;
	}

	@Override
	public JsonObject build() {
		object.addProperty("type", getType().toString());
		return object;
	}

}
