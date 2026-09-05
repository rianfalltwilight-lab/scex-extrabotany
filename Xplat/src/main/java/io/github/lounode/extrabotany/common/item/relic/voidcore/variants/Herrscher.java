package io.github.lounode.extrabotany.common.item.relic.voidcore.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.CoreOfTheVoidVariant;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class Herrscher implements CoreOfTheVoidVariant {
	private static final String ID = "herrscher";
	private static final ResourceLocation LEFT_WING_MODEL = prefix("wing/herrscher_left");
	private static final ResourceLocation RIGHT_WING_MODEL = prefix("wing/herrscher_right");

	private BakedModel leftWing;
	private BakedModel rightWing;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void onModelInit(Map<ResourceLocation, Consumer<BakedModel>> consumer) {
		consumer.put(LEFT_WING_MODEL, bakedModel -> this.leftWing = bakedModel);
		consumer.put(RIGHT_WING_MODEL, bakedModel -> this.rightWing = bakedModel);
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (rightWing == null || leftWing == null) {
			return;
		}

		boolean flying = living instanceof Player player && player.getAbilities().flying;
		float flap = 12F + (float) ((Math.sin((double) (living.tickCount + partialTicks) * (flying ? 0.2F : 0.12F)) + 0.4F) * (flying ? 30F : 5F));

		ms.pushPose();

		// attach to body
		bipedModel.body.translateAndRotate(ms);

		// position on body
		ms.translate(0, -0.2, 0.3);

		//Right wing
		for (int i = 0; i < 3; i++) {
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(flap * 0.25F));
			ms.mulPose(Axis.ZP.rotationDegrees(-35F * i));

			ms.translate(-1.2, -0.1F * i, 0);

			ms.scale(1.9F, -1.9F, -1.9F);
			Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, rightWing);
			ms.popPose();
		}
		//Left wing

		ms.pushPose();
		ms.mulPose(Axis.YP.rotationDegrees(180 - flap * 0.25F));

		ms.translate(-1.2, 0, 0);

		ms.scale(1.7F, -1.7F, -1.7F);
		Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, leftWing);
		ms.popPose();

		ms.popPose();
	}
}
