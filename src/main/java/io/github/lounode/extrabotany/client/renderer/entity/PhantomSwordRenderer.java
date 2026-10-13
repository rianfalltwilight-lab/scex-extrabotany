package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.common.entity.projectile.PhantomSwordEntity;

/**
 * 幻影剑的渲染器。等待起飞的剑不显示。残影随时间变淡。
 * The renderer for the phantom sword. A sword that waits for its flight is not shown. An afterimage fades with time.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderPhantomSword}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderPhantomSword}.
 */
public class PhantomSwordRenderer extends EntityRenderer<PhantomSwordEntity> {
	private static final float SCALE = 1.5F;
	private static final float AFTERIMAGE_START_ALPHA = 0.6F;
	private static final float AFTERIMAGE_FADE_PER_TICK = 0.015F;

	public PhantomSwordRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(PhantomSwordEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		if (entity.getDelay() > 0) {
			return;
		}
		float alpha = entity.isFake()
				? Math.max(0F, AFTERIMAGE_START_ALPHA - (entity.tickCount + partialTick) * AFTERIMAGE_FADE_PER_TICK)
				: 1F;
		if (alpha <= 0F) {
			return;
		}

		pose.pushPose();
		pose.scale(SCALE, SCALE, SCALE);
		// 实体的偏航角是 atan2(dx, dz)。原版的旋转值是这个角度加 180 度。
		// The yaw of the entity is atan2(dx, dz). The rotation value of the original is this angle plus 180 degrees.
		pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() + 270));
		pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot() - 135));
		IconModelRenderer.renderFlat(SwordDomainRenderer.model(entity.getVariety()), pose, buffers, alpha, LightTexture.FULL_BRIGHT);
		pose.popPose();

		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(PhantomSwordEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
