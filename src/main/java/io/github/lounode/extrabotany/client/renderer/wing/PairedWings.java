package io.github.lounode.extrabotany.client.renderer.wing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 左右对称的一对翅膀：同一个模型画两次，第二次绕身体转到另一边。
 * A pair of wings with left-right symmetry: the same model is drawn two times, and the second time it is turned around the body to the other side.
 *
 * @param model     翅膀的模型。 The model of one wing.
 * @param bodyY     翅膀根部在身体上的高度。数值越大，位置越低。 The height of the wing root on the body. A larger value is a lower position.
 * @param flapScale 扇动幅度的倍数。 The multiplier of the flap angle.
 */
public record PairedWings(ResourceLocation model, double bodyY, float flapScale) implements WingRenderer {

	@Override
	public List<ResourceLocation> models() {
		return List.of(model);
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
			MultiBufferSource buffers, int light, float partialTicks) {
		float flap = WingRenderer.flapAngle(living, partialTicks) * flapScale;

		ms.pushPose();
		bipedModel.body.translateAndRotate(ms);
		ms.translate(0, bodyY, 0.2);

		for (int side = 0; side < 2; side++) {
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(side == 0 ? flap : 180 - flap));
			// 把模型移开一格，这样翅膀绕着自己的边缘扇动，而不是绕着贴图的中心。
			// Move the model by one block, so that the wing turns around its edge and not around the center of the texture.
			ms.translate(-1, 0, 0);
			ms.scale(1.5F, -1.5F, -1.5F);
			WingRenderer.drawModel(model, stack, ms, buffers, light);
			ms.popPose();
		}

		ms.popPose();
	}
}
