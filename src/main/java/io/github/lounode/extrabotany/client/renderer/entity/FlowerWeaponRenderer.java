package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity;
import io.github.lounode.extrabotany.common.item.KingGardenItem;

import java.util.Random;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 花之武器的渲染器。它画出武器的图标，并在武器后方画出一道旋转的传送门。武器在蓄力的前 10 tick 淡入。
 * The renderer for the Flower Weapon. It draws the icon of the weapon and a turning portal behind the weapon. The weapon fades in during the first 10 ticks of the charge.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderFlowerWeapon}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderFlowerWeapon}.
 */
public class FlowerWeaponRenderer extends EntityRenderer<FlowerWeaponEntity> {
	private static final ResourceLocation PORTAL_TEXTURE = prefix("textures/misc/flowerweapon.png");
	private static final ResourceLocation[] MODELS = new ResourceLocation[KingGardenItem.WEAPON_TYPES];
	private static final float FADE_IN_TICKS = 10F;
	private static final float ICON_SCALE = 1.5F;
	private static final int FULL_BRIGHT = 0xF000F0;

	static {
		for (int i = 0; i < MODELS.length; i++) {
			MODELS[i] = model(i);
		}
	}

	public FlowerWeaponRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	/**
	 * 武器的图标模型。它必须在模型注册事件里登记。
	 * The icon model of a weapon. The model registration event must register it.
	 */
	public static ResourceLocation model(int variety) {
		return prefix("icon/flower_weapon_" + variety);
	}

	@Override
	public void render(FlowerWeaponEntity weapon, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		int liveTicks = weapon.getLiveTicks();
		int delay = weapon.getDelay();
		float charge = Math.min(FADE_IN_TICKS, Math.max(liveTicks, weapon.getChargeTicks()) + partialTick);
		float alpha = charge / FADE_IN_TICKS;
		if (alpha <= 0F) {
			return;
		}

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(weapon.getRotation()));

		pose.pushPose();
		pose.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);
		pose.mulPose(Axis.YP.rotationDegrees(-90F));
		pose.mulPose(Axis.ZP.rotationDegrees(90F));
		IconModelRenderer.renderFlat(MODELS[weapon.getVariety()], pose, buffers, alpha, FULL_BRIGHT);
		pose.popPose();

		// 传送门的位置和初始角度由实体的 UUID 决定，所以每把武器不同，但是同一把武器每一帧相同。
		// The UUID of the entity gives the position and the start angle of the portal, so they are different for each weapon but the same on each frame for one weapon.
		Random random = new Random(weapon.getUUID().getMostSignificantBits());
		pose.mulPose(Axis.XP.rotationDegrees(-90F));
		pose.translate(0F, -0.3F + random.nextFloat() * 0.1F, 1F);
		float portalScale = alpha;
		if (liveTicks > delay) {
			// 武器起飞之后，传送门在 5 tick 里缩小到消失。
			// After the weapon starts to fly, the portal gets smaller and is gone in 5 ticks.
			portalScale -= Math.min(1F, (liveTicks - delay + partialTick) * 0.2F);
		}
		if (portalScale > 0F) {
			portalScale *= 2F;
			pose.scale(portalScale, portalScale, portalScale);
			pose.mulPose(Axis.YP.rotationDegrees(charge * 9F + (weapon.tickCount + partialTick) * 0.5F + random.nextFloat() * 360F));
			VertexConsumer buffer = buffers.getBuffer(PortalRenderType.get(PORTAL_TEXTURE));
			Matrix4f matrix = pose.last().pose();
			buffer.addVertex(matrix, -1F, 0F, -1F).setUv(0F, 0F).setColor(1F, 1F, 1F, alpha);
			buffer.addVertex(matrix, -1F, 0F, 1F).setUv(0F, 1F).setColor(1F, 1F, 1F, alpha);
			buffer.addVertex(matrix, 1F, 0F, 1F).setUv(1F, 1F).setColor(1F, 1F, 1F, alpha);
			buffer.addVertex(matrix, 1F, 0F, -1F).setUv(1F, 0F).setColor(1F, 1F, 1F, alpha);
		}
		pose.popPose();
		super.render(weapon, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(FlowerWeaponEntity weapon) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
