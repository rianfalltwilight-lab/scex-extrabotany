package io.github.lounode.extrabotany.client.renderer.wing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 彩虹翅膀：每边七片羽翼叠在一起，后一片比前一片小 15%、向下偏 8°、向后偏 1.6°。
 * The rainbow wings: seven feathers on each side, each one 15% smaller than the one before, turned 8° down and 1.6° back.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemCoreGod} 的 2 号外观（{@code elfwingIcons}）。
 * <p>Source: appearance 2 of {@code ItemCoreGod} of ExtraMeteorP 1.12 ({@code elfwingIcons}).
 */
public class RainbowWings implements WingRenderer {
	private static final int LAYERS = 7;
	private final List<ResourceLocation> models = new ArrayList<>();

	public RainbowWings() {
		for (int i = 1; i <= LAYERS; i++) {
			models.add(prefix("wing/rainbow_" + i));
		}
	}

	@Override
	public List<ResourceLocation> models() {
		return models;
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
			MultiBufferSource buffers, int light, float partialTicks) {
		float flap = WingRenderer.flapAngle(living, partialTicks) * 0.5F;
		ms.pushPose();
		bipedModel.body.translateAndRotate(ms);
		ms.translate(0, 0.2, 0.2);
		for (int side = 0; side < 2; side++) {
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(side == 0 ? flap : 180 - flap));
			for (int layer = 0; layer < LAYERS; layer++) {
				ms.pushPose();
				ms.mulPose(Axis.ZP.rotationDegrees(-8F * layer));
				ms.mulPose(Axis.XP.rotationDegrees(1.6F * layer));
				float scale = (float) Math.pow(0.85F, layer);
				ms.translate(-1 * scale, 0, 0);
				ms.scale(1.5F * scale, -1.5F * scale, -1.5F * scale);
				WingRenderer.drawModel(models.get(layer), stack, ms, buffers, light);
				ms.popPose();
			}
			ms.popPose();
		}
		ms.popPose();
	}
}
