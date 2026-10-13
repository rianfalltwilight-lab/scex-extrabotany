package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;

/**
 * 不谐传送杖。右键消耗 2000 点魔力，把玩家传送到准星指向的方块上，最远 64 格。
 * Rod of Discord. A use takes 2000 mana and teleports the player onto the block at the crosshair, to a maximum of 64 blocks.
 *
 * <p>耐久条是冷却条：使用后耐久降到最低，然后每 tick 回复 1 点，约 4 秒回满。
 * <p>The durability bar is a cooldown bar. After a use the durability goes to the minimum, then it gets back 1 point each tick. It is full again after about 4 seconds.
 *
 * <p>在冷却结束前再次使用，玩家失去六分之一最大生命值。
 * <p>If the player uses the rod again before the cooldown ends, the player loses one sixth of the maximum health.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemRodOfDiscord}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemRodOfDiscord}.
 */
public class RodOfDiscordItem extends Item {
	private static final int MANA_PER_USE = 2000;
	private static final int MAX_DAMAGE = 81;
	private static final double RANGE = 64;
	private static final int NAUSEA_DURATION_TICKS = 100;

	public RodOfDiscordItem(Properties props) {
		super(props.durability(MAX_DAMAGE));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = player.pick(RANGE, 1F, false);
		if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult)) {
			return InteractionResultHolder.pass(stack);
		}
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		Vec3 destination = hit.getLocation();
		player.teleportTo(destination.x, destination.y + 1, destination.z);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 3F);
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_DURATION_TICKS));
		if (stack.getDamageValue() > 0) {
			player.setHealth(Math.max(1F, player.getHealth() - player.getMaxHealth() / 6F));
		}
		stack.setDamageValue(stack.getMaxDamage() - 1);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide() && stack.getDamageValue() > 0) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}
}
