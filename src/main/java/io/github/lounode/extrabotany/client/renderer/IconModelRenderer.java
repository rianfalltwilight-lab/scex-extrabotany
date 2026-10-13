package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 把一个独立注册的图标模型画成半透明的物品。
 * Draws an icon model that is registered as a standalone model as a translucent item.
 *
 * <p>图标模型放在 {@code assets/extrabotany/models/icon}，必须在模型注册事件里登记。
 * <p>The icon models are in {@code assets/extrabotany/models/icon}. The model registration event must register them.
 *
 * <p>来源：ExtraMeteorP 1.16 用植物魔法的 {@code RenderHelper#renderItemCustomColor} 画这些模型。
 * <p>Source: ExtraMeteorP 1.16 draws these models with {@code RenderHelper#renderItemCustomColor} of Botania.
 */
public final class IconModelRenderer {
	private static final long QUAD_SEED = 42L;
	private static final Direction[] DIRECTIONS = Direction.values();

	// 物品模型的图标是一片薄板，它的正面在 z = 8.5/16 处。
	// The icon of an item model is a thin plate, and its front face is at z = 8.5/16.
	private static final float ICON_FRONT_Z = 8.5F / 16F;

	private IconModelRenderer() {}

	/**
	 * 把图标画成一片正面在 z = 0 平面上的薄板。图标占据 x 和 y 从 0 到 1 的正方形。
	 * Draws the icon as a thin plate with its front face on the z = 0 plane. The icon is in the square from 0 to 1 on x and y.
	 *
	 * <p>这和 1.12 的 {@code IconHelper.renderIconIn3D} 使用的坐标相同，所以原版渲染器里的平移和旋转可以直接照搬。
	 * <p>These are the same coordinates as {@code IconHelper.renderIconIn3D} of 1.12 uses, so the translations and rotations of the original renderers can be used with no change.
	 */
	public static void renderFlat(ResourceLocation modelId, PoseStack pose, MultiBufferSource buffers, float alpha, int light) {
		pose.pushPose();
		pose.translate(0F, 0F, -ICON_FRONT_Z);
		render(modelId, pose, buffers, alpha, light);
		pose.popPose();
	}

	/**
	 * 画出模型的全部面。模型占据 0 到 1 的单位立方体，和物品模型相同。调用方负责平移和缩放。
	 * Draws all faces of the model. The model is in the unit cube from 0 to 1, as an item model is. The caller does the translation and the scale.
	 *
	 * @param alpha 不透明度，0 到 1。 The opacity, from 0 to 1.
	 * @param light 打包的光照值。 The packed light value.
	 */
	// 独立模型没有方块状态，所以使用不带模型数据的 getQuads。
	// A standalone model has no block state, so getQuads without model data is used.
	@SuppressWarnings("deprecation")
	public static void render(ResourceLocation modelId, PoseStack pose, MultiBufferSource buffers, float alpha, int light) {
		BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelId));
		VertexConsumer buffer = buffers.getBuffer(Sheets.translucentItemSheet());
		RandomSource random = RandomSource.create(QUAD_SEED);
		PoseStack.Pose last = pose.last();
		BlockState noState = null;
		for (int side = 0; side <= DIRECTIONS.length; side++) {
			random.setSeed(QUAD_SEED);
			Direction direction = side == DIRECTIONS.length ? null : DIRECTIONS[side];
			for (var quad : model.getQuads(noState, direction, random)) {
				buffer.putBulkData(last, quad, 1F, 1F, 1F, alpha, light, OverlayTexture.NO_OVERLAY);
			}
		}
	}
}
