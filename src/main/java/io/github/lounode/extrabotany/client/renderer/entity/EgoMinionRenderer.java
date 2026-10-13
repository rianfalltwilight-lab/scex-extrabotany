package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;

/**
 * 本我仆从的渲染器。随从的外形是一名玩家，它用正在观看的玩家自己的皮肤，手里拿着它的遗物剑。
 * The renderer of the Ego Minion. The minion has the shape of a player, uses the skin of the player that looks at it, and holds its relic sword.
 *
 * <p>皮肤有粗手臂和细手臂两种。模型必须和皮肤的种类一致，不然手臂上的贴图会错位。
 * <p>A skin has wide arms or slim arms. The model must agree with the type of the skin, or the texture on the arms is out of position.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderEGO}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderEGO}.
 *
 * <p>和原版的差异：原版按随从的名字从网上下载那名贡献者的皮肤。这里不联网，使用观看者自己的皮肤，和这个模组里的盖亚守护者相同。
 * <p>Difference from the original: the original downloads the skin of the contributor that has the name of the minion from the internet. This port does not use the network, and uses the skin of the viewer, the same as the Guardians of Gaia of this mod.
 */
public class EgoMinionRenderer extends HumanoidMobRenderer<EgoMinionEntity, HumanoidModel<EgoMinionEntity>> {
	private final HumanoidModel<EgoMinionEntity> wideModel;
	private final HumanoidModel<EgoMinionEntity> slimModel;

	public EgoMinionRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		wideModel = getModel();
		slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
		addLayer(new RelicSwordLayer<>(this, context.getItemInHandRenderer(), EgoMinionEntity::getWeapon));
	}

	@Override
	public void render(EgoMinionEntity minion, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		model = getSkin(minion).model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
		super.render(minion, yaw, partialTick, poseStack, buffers, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(EgoMinionEntity minion) {
		return getSkin(minion).texture();
	}

	private static PlayerSkin getSkin(EgoMinionEntity minion) {
		return Minecraft.getInstance().getCameraEntity() instanceof AbstractClientPlayer player
				? player.getSkin()
				: DefaultPlayerSkin.get(minion.getUUID());
	}
}
