package io.github.lounode.extrabotany.common.item.relic.voidcore.variants;

import com.mojang.blaze3d.vertex.PoseStack;

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

import vazkii.botania.common.helper.VecHelper;

import io.github.lounode.extrabotany.api.item.CoreOfTheVoidVariant;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class Steampunk implements CoreOfTheVoidVariant {

	private static final String ID = "steampunk";
	private static final ResourceLocation WING_MODEL = prefix("wing/steampunk");

	private BakedModel wing;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void onModelInit(Map<ResourceLocation, Consumer<BakedModel>> consumer) {
		consumer.put(WING_MODEL, bakedModel -> this.wing = bakedModel);
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (wing == null) {
			return;
		}

		boolean flying = living instanceof Player player && player.getAbilities().flying;
		float flap = 12F + (float) ((Math.sin((double) (living.tickCount + partialTicks) * (flying ? 0.2F : 0.12F)) + 0.4F) * (flying ? 30F : 5F));

		ms.pushPose();

		// attach to body
		bipedModel.body.translateAndRotate(ms);

		// position on body
		ms.translate(0, 0.5, 0.2);

		for (int i = 0; i < 2; i++) {
			ms.pushPose();
			ms.mulPose(VecHelper.rotateY(i == 0 ? flap : 180 - flap));

			// move so flapping about the edge instead of center of texture
			ms.translate(-1, 0, 0);

			// rotate since the textures are stored rotated
			//ms.mulPose(VecHelper.rotateZ(-60));
			ms.scale(1.5F, -1.5F, -1.5F);
			Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffers, light, OverlayTexture.NO_OVERLAY, wing);
			ms.popPose();
		}

		ms.popPose();
	}
}
