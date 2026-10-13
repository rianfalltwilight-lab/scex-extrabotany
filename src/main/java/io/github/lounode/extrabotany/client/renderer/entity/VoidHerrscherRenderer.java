package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.client.model.VoidHerrscherModel;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 空之律者的渲染器。翅膀从二阶段开始显示。护盾半透明，并且总是用最大亮度。
 * The renderer of the Herrscher of the Void. The wings show from rank II. The shields are translucent and always have the maximum brightness.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderVoidHerrscher}。和原版相同，身体、翅膀和护盾都使用 {@code wing.png} 这一张贴图。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderVoidHerrscher}. The same as in the original, the body, the wings and the shields all use the one texture {@code wing.png}.
 */
public class VoidHerrscherRenderer extends HumanoidMobRenderer<VoidHerrscher, VoidHerrscherModel> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/wing.png");
	private static final int SHIELD_COLOR = 0x99FFFFFF;

	public VoidHerrscherRenderer(EntityRendererProvider.Context context) {
		super(context, new VoidHerrscherModel(), 0F);
		addLayer(new WingsAndShieldsLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(VoidHerrscher boss) {
		return TEXTURE;
	}

	@Override
	protected RenderType getRenderType(VoidHerrscher boss, boolean bodyVisible, boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(TEXTURE);
	}

	private static class WingsAndShieldsLayer extends RenderLayer<VoidHerrscher, VoidHerrscherModel> {
		WingsAndShieldsLayer(RenderLayerParent<VoidHerrscher, VoidHerrscherModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, VoidHerrscher boss, float limbSwing, float limbSwingAmount,
				float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			if (boss.isRankII()) {
				getParentModel().renderWings(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
			}
			if (boss.getShields() > 0) {
				getParentModel().renderShields(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), LightTexture.FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY, SHIELD_COLOR);
			}
		}
	}
}
