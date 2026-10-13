package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.common.entity.projectile.ButterflyProjectileEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 蝴蝶的渲染器。它把蝴蝶图标画成一个始终面向镜头的贴片。
 * The renderer for the butterfly. It draws the butterfly icon as a sprite that always faces the camera.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderButterflyProjectile}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderButterflyProjectile}.
 */
public class ButterflyProjectileRenderer extends EntityRenderer<ButterflyProjectileEntity> {
	/**
	 * 蝴蝶图标的模型。它必须在模型注册事件里登记。
	 * The model of the butterfly icon. The model registration event must register it.
	 */
	public static final ResourceLocation BUTTERFLY_MODEL = prefix("icon/butterflyprojectile");
	private static final float ALPHA = 0.9F;

	public ButterflyProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(ButterflyProjectileEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.mulPose(entityRenderDispatcher.cameraOrientation());
		pose.translate(-0.5, -0.5, 1F / 32);
		IconModelRenderer.renderFlat(BUTTERFLY_MODEL, pose, buffers, ALPHA, LightTexture.FULL_BRIGHT);
		pose.popPose();

		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(ButterflyProjectileEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
