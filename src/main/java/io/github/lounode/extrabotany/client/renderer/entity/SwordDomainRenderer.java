package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 审判之剑的渲染器。它绘制一把很大的剑和剑旁边的一个光环。
 * The renderer of the sword of judgment. It draws a very large sword and a halo adjacent to the sword.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderSwordDomain} 和 {@code RenderDomain}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderSwordDomain} and {@code RenderDomain}.
 *
 * <p>和原版的差异：原版里光环是另一个实体（{@code EntityDomain}）。这里由剑的渲染器一起绘制，位置相同，所以少了一种实体。
 * <p>Difference from the original: in the original the halo is a different entity ({@code EntityDomain}). In this port the renderer of the sword draws it at the same position, thus one entity type is not necessary.
 */
public class SwordDomainRenderer extends EntityRenderer<SwordDomainEntity> {
	private static final ResourceLocation HALO_TEXTURE = prefix("textures/misc/flowerweapon.png");
	private static final ResourceLocation[] MODELS = new ResourceLocation[SwordDomainEntity.TYPES];
	private static final float SWORD_SCALE = 3F;
	private static final float HALO_SCALE = 2F;
	/** 剑每 tick 下沉的距离。光环留在剑出现时的高度。 / The distance that the sword goes down on each tick. The halo stays at the height where the sword appeared. */
	private static final float SINK_SPEED = 0.01F;
	private static final float MAX_SINK = 2F;

	static {
		for (int i = 0; i < MODELS.length; i++) {
			MODELS[i] = model(i);
		}
	}

	public SwordDomainRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	/**
	 * 剑的图标模型。它必须在模型注册事件里登记。幻影剑也使用这些模型。
	 * The icon model of a sword. The model registration event must register it. The Phantom Sword also uses these models.
	 */
	public static ResourceLocation model(int type) {
		return prefix("icon/sworddomain_" + Math.floorMod(type, SwordDomainEntity.TYPES));
	}

	@Override
	public ResourceLocation getTextureLocation(SwordDomainEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}

	@Override
	public boolean shouldRender(SwordDomainEntity entity, Frustum frustum, double x, double y, double z) {
		// 剑和光环比实体的碰撞箱大得多。
		// The sword and the halo are much larger than the bounding box of the entity.
		return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(4));
	}

	@Override
	public void render(SwordDomainEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		poseStack.pushPose();
		poseStack.scale(SWORD_SCALE, SWORD_SCALE, SWORD_SCALE);
		poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(135F));
		IconModelRenderer.renderFlat(MODELS[entity.getSwordType()], poseStack, buffers, 1F, LightTexture.FULL_BRIGHT);
		poseStack.popPose();

		poseStack.pushPose();
		float sunk = Math.min((entity.tickCount + partialTick) * SINK_SPEED, MAX_SINK);
		poseStack.translate(0F, sunk - 0.5F, -2F);
		poseStack.scale(HALO_SCALE, HALO_SCALE, HALO_SCALE);
		VertexConsumer buffer = buffers.getBuffer(PortalRenderType.get(HALO_TEXTURE));
		Matrix4f matrix = poseStack.last().pose();
		buffer.addVertex(matrix, -1F, 0F, -1F).setUv(0F, 0F).setColor(1F, 1F, 1F, 1F);
		buffer.addVertex(matrix, -1F, 0F, 1F).setUv(0F, 1F).setColor(1F, 1F, 1F, 1F);
		buffer.addVertex(matrix, 1F, 0F, 1F).setUv(1F, 1F).setColor(1F, 1F, 1F, 1F);
		buffer.addVertex(matrix, 1F, 0F, -1F).setUv(1F, 0F).setColor(1F, 1F, 1F, 1F);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
	}
}
