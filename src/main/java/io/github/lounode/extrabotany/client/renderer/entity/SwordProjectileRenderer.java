package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;

import io.github.lounode.extrabotany.common.entity.projectile.TargetedProjectileEntity;

import java.util.function.Supplier;

/**
 * 把剑气画成一把半透明的、发光的剑。模型就是对应物品的模型。
 * Draws a sword beam as a translucent, glowing sword. The model is the model of the related item.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderProjectileBase}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderProjectileBase}.
 */
public class SwordProjectileRenderer<T extends TargetedProjectileEntity> extends EntityRenderer<T> {
	private static final float SCALE = 1.2F;
	private static final float ALPHA = 229F / 255F;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final long QUAD_SEED = 42L;

	private final Supplier<Item> item;

	public SwordProjectileRenderer(EntityRendererProvider.Context context, Supplier<Item> item) {
		super(context);
		this.item = item;
	}

	@Override
	@SuppressWarnings("deprecation") // 物品模型是静态的，不需要模型数据。 The item model is static and needs no model data.
	public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item.get());

		pose.pushPose();
		pose.scale(SCALE, SCALE, SCALE);
		pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90));
		pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));
		// 物品贴图里的剑是斜着画的。再转 45 度，让剑尖指向飞行方向。
		// The sword in the item texture is drawn at an angle. Turn 45 degrees more so that the tip points in the flight direction.
		pose.mulPose(Axis.ZP.rotationDegrees(-45));
		pose.translate(-0.5F, -0.5F, -0.5F);

		RandomSource random = RandomSource.create(QUAD_SEED);
		VertexConsumer vertices = buffers.getBuffer(Sheets.translucentItemSheet());
		for (Direction side : Direction.values()) {
			random.setSeed(QUAD_SEED);
			for (var quad : model.getQuads(null, side, random)) {
				vertices.putBulkData(pose.last(), quad, 1F, 1F, 1F, ALPHA, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			}
		}
		random.setSeed(QUAD_SEED);
		for (var quad : model.getQuads(null, null, random)) {
			vertices.putBulkData(pose.last(), quad, 1F, 1F, 1F, ALPHA, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		}
		pose.popPose();

		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
