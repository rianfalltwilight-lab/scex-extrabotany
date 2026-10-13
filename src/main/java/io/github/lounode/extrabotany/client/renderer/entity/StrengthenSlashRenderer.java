package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.common.entity.flamescion.StrengthenSlashEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 强化斩击的渲染器。它把斩击图标放大 3.5 倍，并让图标朝向飞行的方向。
 * The renderer for the Strengthened Slash. It makes the slash icon 3.5 times larger and points the icon in the flight direction.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderStrengthenSlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderStrengthenSlash}.
 */
public class StrengthenSlashRenderer extends EntityRenderer<StrengthenSlashEntity> {
	/**
	 * 斩击的图标模型。它必须在模型注册事件里登记。
	 * The icon model of the slash. The model registration event must register it.
	 */
	public static final ResourceLocation SLASH_MODEL = prefix("icon/strengthenslash");

	private static final float SCALE = 3.5F;
	private static final float ALPHA = 0.8F;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final double VISIBLE_RANGE = 6.0;

	public StrengthenSlashRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(StrengthenSlashEntity slash, Frustum frustum, double x, double y, double z) {
		return slash.shouldRender(x, y, z) && frustum.isVisible(slash.getBoundingBox().inflate(VISIBLE_RANGE));
	}

	@Override
	public void render(StrengthenSlashEntity slash, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.scale(SCALE, SCALE, SCALE);
		// 投射物的偏航角和原版里保存的角度相差 270 度。
		// The yaw of a projectile is different from the angle that the original saves by 270 degrees.
		pose.mulPose(Axis.YP.rotationDegrees(slash.getYRot() + 270F));
		pose.mulPose(Axis.ZP.rotationDegrees(slash.getXRot() + 30F));
		pose.translate(-0.5F, -0.5F, -0.5F);
		IconModelRenderer.render(SLASH_MODEL, pose, buffers, ALPHA, FULL_BRIGHT);
		pose.popPose();
		super.render(slash, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(StrengthenSlashEntity slash) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
