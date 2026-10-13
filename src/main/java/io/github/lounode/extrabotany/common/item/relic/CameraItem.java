package io.github.lounode.extrabotany.common.item.relic;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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


import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;

import io.github.lounode.extrabotany.api.item.IShadowium;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 文文的相机。遗物。按住使用键取景，取景框里的敌人会发光。松开时消耗 1500 点魔力"拍照"：框里的生物得到 10 秒的链接效果，框里别人的弹射物被清除。之后有 8 秒冷却。
 * Camera. A relic. Hold the use key to aim, and the enemies in the frame glow. Release it to "take a photo" for 1500 mana: the creatures in the frame get the Link effect for 10 seconds, and the projectiles of others in the frame are removed. After that there is a cooldown of 8 seconds.
 */
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
}
