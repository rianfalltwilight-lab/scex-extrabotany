package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import io.github.lounode.extrabotany.common.lib.RegistryHelper;
import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

public class EntityPage extends AbstractPage<EntityPage> {

	public EntityPage(String entity) {
		object.addProperty("entity", entity);
	}

	public EntityPage(EntityType<?> type) {
		this(RegistryHelper.getRegistryName(type).toString());
	}

	public EntityPage withScale(float scale) {
		object.addProperty("scale", scale);
		return this;
	}

	public EntityPage withOffset(float offset) {
		object.addProperty("offset", offset);
		return this;
	}

	public EntityPage withRotate(boolean rotate) {
		object.addProperty("rotate", rotate);
		return this;
	}

	public EntityPage withDefaultRotation(float rotation) {
		object.addProperty("default_rotation", rotation);
		return this;
	}

	public EntityPage withText(String text) {
		object.addProperty("text", text);
		return this;
	}

	public EntityPage withName(String name) {
		object.addProperty("name", name);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:entity");
	}
}
