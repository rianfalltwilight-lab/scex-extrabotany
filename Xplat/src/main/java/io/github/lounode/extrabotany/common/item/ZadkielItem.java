package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.item.equipment.bauble.FrostRingItem;

// TODO完善功能 冰刺
public class ZadkielItem extends Item {
	public ZadkielItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!world.isClientSide) {
			player.getCooldowns().addCooldown(this, 60);

			Vec3 look = player.getLookAngle();
			ServerLevel serverWorld = (ServerLevel) world;
			for (int i = 0; i < 10; i++) {
				double progress = i * 0.5;
				serverWorld.sendParticles(ParticleTypes.SNOWFLAKE,
						player.getX() + look.x * progress,
						player.getY() + player.getEyeHeight() + look.y * progress,
						player.getZ() + look.z * progress,
						3, 0, 0, 0, 0.1);
			}

			for (int i = 0; i < 5; i++) {
				Vec3 pos = player.position()
						.add(player.getLookAngle().scale(2 + i));

				BlockHitResult hit = world.clip(new ClipContext(
						pos, pos.add(0, -10, 0),
						ClipContext.Block.COLLIDER,
						ClipContext.Fluid.NONE,
						player
				));

				EvokerFangs fangs = new EvokerFangs(
						world,
						hit.getLocation().x(),
						hit.getBlockPos().getY() + 1,
						hit.getLocation().z(),
						(float) Math.toRadians(player.getYRot()),
						0,
						player
				);
				world.addFreshEntity(fangs);
			}
		}

		return InteractionResultHolder.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (level.isClientSide()) {
			return;
		}
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		FrostRingItem.freezeWater(living, entity.level(), entity.blockPosition(), 6);
	}
}
