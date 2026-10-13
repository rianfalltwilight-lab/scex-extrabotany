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
import io.github.lounode.extrabotany.common.entity.judah.JudahSpearEntity;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 犹大的誓约的光矛的渲染器。光矛的图标有四种：犹大和 KIRA 各有"下落"和"上升"两种。
 * The renderer for a spear of the Judah Oath. There are four spear icons: Judah and KIRA each have one for "down" and one for "up".
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderJudahSpear}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderJudahSpear}.
 */
public class JudahSpearRenderer extends EntityRenderer<JudahSpearEntity> {
	public static final int MODEL_COUNT = 4;

	private static final ResourceLocation[] MODELS = new ResourceLocation[MODEL_COUNT];
	private static final float SCALE = 1.8F;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final double VISIBLE_RANGE = 4.0;

	static {
		for (int i = 0; i < MODEL_COUNT; i++) {
			MODELS[i] = model(i);
		}
	}

	public JudahSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	/**
	 * 光矛的图标模型。序号是"外观 × 2 + 是否上升"。它必须在模型注册事件里登记。
	 * The icon model of a spear. The index is "appearance × 2 + going up". The model registration event must register it.
	 */
	public static ResourceLocation model(int index) {
		return prefix("icon/spear_" + index);
	}

	@Override
	public boolean shouldRender(JudahSpearEntity spear, Frustum frustum, double x, double y, double z) {
		return spear.shouldRender(x, y, z) && frustum.isVisible(spear.getBoundingBox().inflate(VISIBLE_RANGE));
	}

	@Override
	public void render(JudahSpearEntity spear, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		// 樱花的誓约不召唤光矛，所以它没有自己的图标。
		// The Oath of Sakura summons no spears, so it has no icon of its own.
		int variant = spear.getVariant() == Variant.KIRA ? 1 : 0;
		ResourceLocation model = MODELS[variant * 2 + (spear.isFake() ? 1 : 0)];
		pose.pushPose();
		pose.translate(-0.05F, 0F, 1.15F);
		pose.mulPose(Axis.YP.rotationDegrees(-90F));
		pose.mulPose(Axis.ZP.rotationDegrees(135F));
		pose.scale(SCALE, SCALE, SCALE);
		IconModelRenderer.renderFlat(model, pose, buffers, 1F, FULL_BRIGHT);
		pose.popPose();
		super.render(spear, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(JudahSpearEntity spear) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
