package io.github.lounode.extrabotany.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import vazkii.botania.client.render.entity.PixieRenderer;
import vazkii.botania.common.entity.PixieEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 暗影妖精：植物魔法的妖精模型，换成 1.12 的黑色贴图。
 * The dark pixie: the pixie model of Botania with the dark texture of 1.12.
 */
public class DarkPixieRenderer extends PixieRenderer {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/dark_pixie.png");
	// 1.12 RenderDarkPixie：十月里换成南瓜头贴图（对照原版审计 E7）。 / RenderDarkPixie of 1.12: the pumpkin texture in October (parity audit E7).
	private static final ResourceLocation PUMPKIN = prefix("textures/entity/dark_pixie_pumpkin.png");

	public DarkPixieRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public ResourceLocation getTextureLocation(PixieEntity entity) {
		return java.time.LocalDate.now().getMonth() == java.time.Month.OCTOBER ? PUMPKIN : TEXTURE;
	}
}
