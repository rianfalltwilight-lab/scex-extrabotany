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
import io.github.lounode.extrabotany.common.entity.judah.JudahOathEntity;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 犹大的誓约的渲染器。它在实体上方 3 格处画出誓约的图案，左右对称的两半各画一次。
 * The renderer for the Judah Oath. It draws the picture of the oath 3 blocks above the entity, one time for each of the two mirrored halves.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderJudahOath}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderJudahOath}.
 */
public class JudahOathRenderer extends EntityRenderer<JudahOathEntity> {
	private static final ResourceLocation[] MODELS = new ResourceLocation[Variant.values().length];
	// 原版的缩放是 1.65 × (32 / 20)。
	// The scale of the original is 1.65 × (32 / 20).
	private static final float SCALE = 2.64F;
	private static final float HEIGHT = 3F;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final double VISIBLE_RANGE = 4.0;

	static {
		for (Variant variant : Variant.values()) {
			MODELS[variant.ordinal()] = model(variant);
		}
	}

	public JudahOathRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	/**
	 * 誓约图案的图标模型。它必须在模型注册事件里登记。
	 * The icon model of the oath picture. The model registration event must register it.
	 */
	public static ResourceLocation model(Variant variant) {
		return prefix("icon/judahoath_" + variant.ordinal());
	}

	@Override
	public boolean shouldRender(JudahOathEntity oath, Frustum frustum, double x, double y, double z) {
		return oath.shouldRender(x, y, z) && frustum.isVisible(oath.getBoundingBox().inflate(VISIBLE_RANGE));
	}

	@Override
	public void render(JudahOathEntity oath, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		ResourceLocation model = MODELS[oath.getVariant().ordinal()];
		pose.pushPose();
		pose.translate(0F, HEIGHT, 0F);
		pose.mulPose(Axis.YP.rotationDegrees(oath.getRotation()));
		for (int side = 0; side < 2; side++) {
			pose.pushPose();
			if (side == 1) {
				pose.scale(-1F, 1F, 1F);
			}
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.scale(SCALE, SCALE, SCALE);
			IconModelRenderer.renderFlat(model, pose, buffers, 1F, FULL_BRIGHT);
			pose.popPose();
		}
		pose.popPose();
		super.render(oath, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(JudahOathEntity oath) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
