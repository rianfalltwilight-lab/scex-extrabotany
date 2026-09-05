package io.github.lounode.extrabotany.data.patchouli.page;

import com.google.gson.JsonObject;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

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
