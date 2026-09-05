package io.github.lounode.extrabotany.common.item.relic.void_archives.variants;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.item.relic.CameraItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;

import java.util.List;

public class Camera implements VoidArchivesVariant {

	public static Camera INSTANCE = new Camera();

	private static final String ID = "camera";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		player.playNotifySound(ExtraBotanySounds.CAMERA_FOCUS, SoundSource.PLAYERS, .3F, SoundEventUtil.randomPitch(level));
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (!(entity instanceof Player player)) {
			return;
		}
		ItemStack usingItem = player.getUseItem();

		if (usingItem.isEmpty() || !(usingItem.getItem() instanceof VoidArchivesItem)) {
			return;
		}
		AABB bounds = CameraItem.getFreezeBounds(player);

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

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
		if (!(entity instanceof Player player)) {
			return;
		}

		CameraItem.executeCapture(world, player, player.getUsedItemHand());
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 1200;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPYGLASS;
	}
}
