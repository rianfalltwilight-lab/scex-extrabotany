package io.github.lounode.extrabotany.common.item.relic;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.IShadowium;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class CameraItem extends RelicItem implements IShadowium {
	private static final int MANA_PER_USE = 1500;
	private static final int RANGE = 20;
	private static final int ADVANCEMENT_REQUIRE = 10;

	public CameraItem(Properties props) {
		super(props);
	}
	//TODO 拍照UI 投掷物变成P点飞过来

	//拿着相机受伤时播放东方受伤音效
	//Z键 饰品栏也能拍照
	//车万女仆联动
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic == null ||
				!relic.isRightPlayer(player) ||
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, false)) {
			return InteractionResultHolder.pass(stack);
		}

		player.playNotifySound(ExtraBotanySounds.CAMERA_FOCUS, SoundSource.PLAYERS, .3F, SoundEventUtil.randomPitch(world));
		return ItemUtils.startUsingInstantly(world, player, hand);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
		if (!(entity instanceof Player player)) {
			return;
		}

		if (executeCapture(world, player, player.getUsedItemHand()).getResult() == InteractionResult.SUCCESS) {
			player.getCooldowns().addCooldown(this, 20 * 8);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 1200;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPYGLASS;
	}

	public static InteractionResultHolder<ItemStack> executeCapture(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null && (player.isCreative() || (relic.isRightPlayer(player) &&
				ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)))) {

			List<LivingEntity> livingEntities = world.getEntitiesOfClass(LivingEntity.class, getFreezeBounds(player)).stream()
					.filter(entity -> !entity.equals(player))
					.filter(entity -> entity.getTeam() == null || !entity.getTeam().isAlliedTo(player.getTeam()))
					.toList();
			for (var livingEntity : livingEntities) {
				//livingEntity.addEffect(new MobEffectInstance(ExtrabotanyMobEffects.IMMOBILIZE, 100));
				livingEntity.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.LINK, 20 * 10));
			}

			if (!world.isClientSide() && livingEntities.size() >= ADVANCEMENT_REQUIRE) {
				PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.I_SEE_EVERYTHING), "code_triggered");
			}

			List<Projectile> projectiles = world.getEntitiesOfClass(Projectile.class, getFreezeBounds(player)).stream()
					.filter(entity -> entity.getOwner() != player)
					.filter(entity -> entity.getTeam() == null || !entity.getTeam().isAlliedTo(player.getTeam()))
					.toList();
			for (var projectile : projectiles) {
				projectile.remove(Entity.RemovalReason.DISCARDED);
			}

			world.playSound(null, player.getOnPos(), ExtraBotanySounds.CAMERA_USE, SoundSource.PLAYERS);
			return InteractionResultHolder.success(stack);
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		shadowiumTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player)) {
			return;
		}
		super.inventoryTick(stack, world, entity, slot, selected);
		ItemStack usingItem = player.getUseItem();

		if (usingItem.isEmpty() || !(usingItem.getItem() instanceof CameraItem)) {
			return;
		}
		AABB bounds = getFreezeBounds(player);
		//renderAABBBorder(entity.level(), bounds);

		List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, bounds).stream()
				.filter(e -> !e.equals(player))
				.filter(e -> e.getTeam() == null || !e.getTeam().isAlliedTo(player.getTeam()))
				.toList();

		for (LivingEntity target : targets) {
			target.addEffect(new MobEffectInstance(
					MobEffects.GLOWING,
					2,
					0,
					false,
					true,
					true
			));
		}
	}

	public static void renderAABBBorder(Level level, AABB bounds) {
		int particleDensity = 10;
		float red = 1.0f, green = 0.0f, blue = 0.0f, scale = 1.0f;

		double xStep = (bounds.maxX - bounds.minX) / particleDensity;
		double yStep = (bounds.maxY - bounds.minY) / particleDensity;
		double zStep = (bounds.maxZ - bounds.minZ) / particleDensity;

		for (int i = 0; i <= particleDensity; i++) {
			spawnParticle(level, bounds.minX + xStep * i, bounds.minY, bounds.minZ, red, green, blue, scale);
			spawnParticle(level, bounds.minX + xStep * i, bounds.minY, bounds.maxZ, red, green, blue, scale);
			spawnParticle(level, bounds.minX, bounds.minY, bounds.minZ + zStep * i, red, green, blue, scale);
			spawnParticle(level, bounds.maxX, bounds.minY, bounds.minZ + zStep * i, red, green, blue, scale);

			spawnParticle(level, bounds.minX + xStep * i, bounds.maxY, bounds.minZ, red, green, blue, scale);
			spawnParticle(level, bounds.minX + xStep * i, bounds.maxY, bounds.maxZ, red, green, blue, scale);
			spawnParticle(level, bounds.minX, bounds.maxY, bounds.minZ + zStep * i, red, green, blue, scale);
			spawnParticle(level, bounds.maxX, bounds.maxY, bounds.minZ + zStep * i, red, green, blue, scale);

			spawnParticle(level, bounds.minX, bounds.minY + yStep * i, bounds.minZ, red, green, blue, scale);
			spawnParticle(level, bounds.maxX, bounds.minY + yStep * i, bounds.minZ, red, green, blue, scale);
			spawnParticle(level, bounds.minX, bounds.minY + yStep * i, bounds.maxZ, red, green, blue, scale);
			spawnParticle(level, bounds.maxX, bounds.minY + yStep * i, bounds.maxZ, red, green, blue, scale);
		}
	}

	private static void spawnParticle(Level level, double x, double y, double z, float r, float g, float b, float scale) {
		if (level.isClientSide) {
			level.addParticle(new DustParticleOptions(new Vector3f(r, g, b), scale),
					x, y, z, 0, 0, 0);
		}
	}

	public static void onItemCooldownFinish(Player player, Item item) {
		if (!(item instanceof CameraItem)) {
			return;
		}
		if (!player.level().isClientSide()) {
			return;
		}
		player.playNotifySound(ExtraBotanySounds.CAMERA_CHARGE, SoundSource.PLAYERS, .3F, SoundEventUtil.randomPitch(player.level()));
	}

	public int getManaPerUse() {
		return MANA_PER_USE;
	}

	public int getRange() {
		return RANGE;
	}

	public static AABB getFreezeBounds(Player player) {
		Vec3 lookVec = player.getLookAngle();
		Vec3 center = player.getEyePosition().add(lookVec.scale(RANGE * 0.75));

		double halfRange = RANGE / 2.0;
		return new AABB(
				center.x - halfRange, center.y - halfRange, center.z - halfRange,
				center.x + halfRange, center.y + halfRange, center.z + halfRange
		);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.camera").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal(""));
		super.appendHoverText(stack, context, tooltip, flags);
	}

	public static class Hud {
		public static final ResourceLocation CAMERA_UI_LOCATION = prefix("textures/gui/spyglass_scope.png");

		public static void renderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			float f = (float) Math.min(pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
			float f1 = Math.min((float) pGuiGraphics.guiWidth() / f, (float) pGuiGraphics.guiHeight() / f) * pScopeScale;
			int i = Mth.floor(f * f1);
			int j = Mth.floor(f * f1);
			int k = (pGuiGraphics.guiWidth() - i) / 2;
			int l = (pGuiGraphics.guiHeight() - j) / 2;
			int i1 = k + i;
			int j1 = l + j;
			pGuiGraphics.blit(CAMERA_UI_LOCATION, k, l, -90, 0.0F, 0.0F, i, j, i, j);

			RenderSystem.disableBlend();
		}
	}
}
