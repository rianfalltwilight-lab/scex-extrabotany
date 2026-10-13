package io.github.lounode.extrabotany.client.renderer.wing;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 画出空之律者核心的一种翅膀。
 * Draws one type of wings of the Core of the Void.
 *
 * <p>翅膀的模型是独立模型，放在 {@code assets/extrabotany/models/wing}。{@link #models()} 给出的模型会在模型注册事件里登记。
 * <p>The models of the wings are standalone models in {@code assets/extrabotany/models/wing}. The model registration event records the models that {@link #models()} gives.
 */
public interface WingRenderer {

	/**
	 * 这种翅膀使用的全部模型。
	 * All models that these wings use.
	 */
	List<ResourceLocation> models();

	void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
			MultiBufferSource buffers, int light, float partialTicks);

	/**
	 * 翅膀当前张开的角度（度）。翅膀按正弦曲线扇动，佩戴者飞行时扇得更快、幅度更大。
	 * The current opening angle of the wings, in degrees. The wings move on a sine curve, and they move faster and more when the wearer flies.
	 */
	static float flapAngle(LivingEntity living, float partialTicks) {
		boolean flying = living instanceof Player player && player.getAbilities().flying;
		double phase = (living.tickCount + partialTicks) * (flying ? 0.2F : 0.12F);
		return 12F + (float) ((Math.sin(phase) + 0.4F) * (flying ? 30F : 5F));
	}

	/**
	 * 用物品渲染器画出一个翅膀模型。资源包里没有这个模型时什么也不画。
	 * Draws one wing model with the item renderer. When the resource packs do not have this model, nothing is drawn.
	 */
	static void drawModel(ResourceLocation modelId, ItemStack stack, PoseStack ms, MultiBufferSource buffers, int light) {
		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		BakedModel model = modelManager.getModel(ModelResourceLocation.standalone(modelId));
		if (model == modelManager.getMissingModel()) {
			return;
		}
		minecraft.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffers, light, OverlayTexture.NO_OVERLAY, model);
	}
}
