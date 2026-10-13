package io.github.lounode.extrabotany.client.renderer.wing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 律者的翅膀。它左右不对称：右边是三片叠成扇形的羽翼，左边是一片大的羽翼。它总是以最大亮度显示。
 * The wings of the Herrscher. They have no symmetry: on the right there are three wings in the shape of a fan, and on the left there is one large wing. They always show at full brightness.
 */
public final class HerrscherWings implements WingRenderer {
	private static final ResourceLocation LEFT_WING_MODEL = prefix("wing/herrscher_left");
	private static final ResourceLocation RIGHT_WING_MODEL = prefix("wing/herrscher_right");

	private static final int RIGHT_WING_COUNT = 3;
	private static final float RIGHT_WING_SPREAD = 35F;

	@Override
	public List<ResourceLocation> models() {
		return List.of(LEFT_WING_MODEL, RIGHT_WING_MODEL);
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
			MultiBufferSource buffers, int light, float partialTicks) {
		float flap = WingRenderer.flapAngle(living, partialTicks) * 0.25F;

		ms.pushPose();
		bipedModel.body.translateAndRotate(ms);
		ms.translate(0, -0.2, 0.3);

		for (int i = 0; i < RIGHT_WING_COUNT; i++) {
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(flap));
			ms.mulPose(Axis.ZP.rotationDegrees(-RIGHT_WING_SPREAD * i));
			ms.translate(-1.2, -0.1F * i, 0);
			ms.scale(1.9F, -1.9F, -1.9F);
			WingRenderer.drawModel(RIGHT_WING_MODEL, stack, ms, buffers, LightTexture.FULL_BRIGHT);
			ms.popPose();
		}

		ms.pushPose();
		ms.mulPose(Axis.YP.rotationDegrees(180 - flap));
		ms.translate(-1.2, 0, 0);
		ms.scale(1.7F, -1.7F, -1.7F);
		WingRenderer.drawModel(LEFT_WING_MODEL, stack, ms, buffers, LightTexture.FULL_BRIGHT);
		ms.popPose();

		ms.popPose();
	}
}
