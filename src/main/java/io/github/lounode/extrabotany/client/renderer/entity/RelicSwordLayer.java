package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * 在惯用手里绘制本我或者本我仆从的遗物剑。剑只是画出来的，生物并没有真的装备它，所以它不会掉落，也不会被别的模组拿走。
 * Draws the relic sword of the Ego or of an Ego Minion in the main hand. The sword is only drawn and the mob does not really have it as equipment, thus it cannot drop and a different mod cannot take it.
 *
 * <p>游戏让一小部分生物成为左撇子。挥剑的动画用惯用手，所以剑也要画在惯用手里。
 * <p>The game makes a small part of the mobs left-handed. The swing animation uses the main hand, thus the sword must also be drawn in the main hand.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HeldFakeItemLayer}。
 * <p>Source: ExtraMeteorP 1.16 {@code HeldFakeItemLayer}.
 */
public class RelicSwordLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private final ItemInHandRenderer itemInHandRenderer;
	private final Function<T, ItemStack> weapon;

	public RelicSwordLayer(RenderLayerParent<T, M> parent, ItemInHandRenderer itemInHandRenderer, Function<T, ItemStack> weapon) {
		super(parent);
		this.itemInHandRenderer = itemInHandRenderer;
		this.weapon = weapon;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		ItemStack stack = weapon.apply(entity);
		if (stack.isEmpty()) {
			return;
		}
		HumanoidArm arm = entity.getMainArm();
		boolean left = arm == HumanoidArm.LEFT;
		poseStack.pushPose();
		getParentModel().translateToHand(arm, poseStack);
		// 这些数值和原版的"手持物品"层相同。
		// These values are the same as in the vanilla "item in hand" layer.
		poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180F));
		poseStack.translate((left ? -1 : 1) / 16F, 0.125F, -0.625F);
		itemInHandRenderer.renderItem(entity, stack, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
				left, poseStack, buffers, packedLight);
		poseStack.popPose();
	}
}
