package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

import io.github.lounode.extrabotany.common.entity.vehicle.FlyingBoatEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 飞艇的渲染器：原版船的模型，加上 1.12 的三张飞艇贴图。
 * The renderer of the flying boat: the model of the vanilla boat with the three flying boat textures of 1.12.
 */
public class FlyingBoatRenderer extends BoatRenderer {
	private static final ResourceLocation[] TEXTURES = {
			prefix("textures/entity/boat/flying_boat_manasteel.png"),
			prefix("textures/entity/boat/flying_boat_elementium.png"),
			prefix("textures/entity/boat/flying_boat_terrasteel.png")
	};

	public FlyingBoatRenderer(EntityRendererProvider.Context context) {
		super(context, false);
	}

	@Override
	public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
		Pair<ResourceLocation, ListModel<Boat>> base = super.getModelWithLocation(boat);
		int kind = boat instanceof FlyingBoatEntity flying ? flying.getKind().ordinal() : 0;
		return Pair.of(TEXTURES[kind], base.getSecond());
	}
}
