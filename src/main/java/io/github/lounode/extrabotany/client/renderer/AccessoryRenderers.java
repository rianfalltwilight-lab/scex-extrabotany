package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.AccessoryRenderer;

import io.github.lounode.extrabotany.client.renderer.wing.CoreOfTheVoidRenderer;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.CosmeticBaubleItem;

/**
 * 注册饰品在玩家身上的渲染器。
 * Registers the renderers that draw baubles on the player.
 *
 * <p>装饰饰品和薯片直接把物品模型画在头部或身体上。空之律者核心画出翅膀。
 * <p>The cosmetic baubles and the Potato Chips draw the item model directly on the head or on the body. The Core of the Void draws wings.
 */
public final class AccessoryRenderers {

	private AccessoryRenderers() {}

	/**
	 * 必须在客户端初始化的主线程任务里调用。
	 * This method must be called from the main thread work of the client setup.
	 */
	public static void register() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof CosmeticBaubleItem cosmetic) {
				CosmeticBaubleItem.Variant variant = cosmetic.getVariant();
				AccessoryRenderRegistry.register(item, new ItemModelRenderer(variant.onHead,
						variant.x, variant.y, variant.z, variant.scaleX, variant.scaleY, variant.scaleZ));
			}
		}
		// 薯片戴在头上。数值来自原版 ItemPotatoChips#doRender。
		// The Potato Chips go on the head. The values come from ItemPotatoChips#doRender of the original.
		AccessoryRenderRegistry.register(ExtraBotanyItems.potatoChips,
				new ItemModelRenderer(true, 0F, -0.3F, -0.3F, 0.6F, -0.6F, -0.6F));
		AccessoryRenderRegistry.register(ExtraBotanyItems.coreOfTheVoid, new CoreOfTheVoidRenderer());
	}

	/**
	 * 把物品模型画在头部或身体上的渲染器。
	 * A renderer that draws the item model on the head or on the body.
	 */
	private record ItemModelRenderer(boolean onHead, float x, float y, float z,
			float scaleX, float scaleY, float scaleZ) implements AccessoryRenderer {

		@Override
		public void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
				MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks,
				float ageInTicks, float netHeadYaw, float headPitch) {
			ms.pushPose();
			(onHead ? bipedModel.head : bipedModel.body).translateAndRotate(ms);
			ms.translate(x, y, z);
			ms.scale(scaleX, scaleY, scaleZ);
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
					light, OverlayTexture.NO_OVERLAY, ms, buffers, living.level(), 0);
			ms.popPose();
		}
	}
}
