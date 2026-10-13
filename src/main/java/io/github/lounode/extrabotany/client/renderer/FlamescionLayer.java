package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 薪炎之钥的玩家渲染层。玩家主手拿着薪炎之钥时，背后有一个缓慢旋转的火环。
 * The player render layer of the Key of Flamescion. When the player has the Key of Flamescion in the main hand, a ring of fire turns slowly behind the back.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code LayerFlamescion}。
 * <p>Source: ExtraMeteorP 1.16 {@code LayerFlamescion}.
 */
public class FlamescionLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	/**
	 * 火环的图标模型。它必须在模型注册事件里登记。
	 * The icon model of the fire ring. The model registration event must register it.
	 */
	public static final ResourceLocation RING_MODEL = prefix("icon/flamescionring");

	private static final float SCALE = 1.2F;
	private static final float PULSE_PERIOD_TICKS = 20F;
	private static final float SPIN_DEGREES_PER_TICK = 0.2F;

	public FlamescionLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (player.isInvisible() || !player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)) {
			return;
		}
		// 亮度和不透明度一起缓慢起伏。
		// The brightness and the opacity go up and down slowly together.
		float pulse = Mth.cos(ageInTicks / PULSE_PERIOD_TICKS);
		float alpha = 0.75F + 0.15F * pulse;
		int ringLight = 0xF000B0 + (int) (0x30 * pulse);

		pose.pushPose();
		getParentModel().body.translateAndRotate(pose);
		pose.translate(-0.6F, -0.6F, 0F);
		pose.scale(SCALE, SCALE, SCALE);
		pose.mulPose(Axis.YP.rotationDegrees(-20F));
		pose.mulPose(Axis.ZP.rotationDegrees(-40F));
		pose.mulPose(Axis.XP.rotationDegrees(100F));
		pose.mulPose(Axis.ZN.rotationDegrees(ageInTicks * SPIN_DEGREES_PER_TICK));
		// 物品模型以 (0.5, 0.5, 0.5) 为中心，所以先把中心移到原点。
		// An item model has its center at (0.5, 0.5, 0.5), so the center is moved to the origin first.
		pose.translate(-0.5F, -0.5F, -0.5F);
		IconModelRenderer.render(RING_MODEL, pose, buffers, alpha, ringLight);
		pose.popPose();
	}
}
