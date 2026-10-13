package io.github.lounode.extrabotany.common.item.relic;


import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;


import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.PixieHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.material.ItemTiers;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 勇士盾。遗物。它是盾牌，也是武器。用它攻击时，每次命中给持有者 2 点伤害吸收，最多 20 点。伤害吸收达到 10 点后，潜行并使用可以"解放"：攻击伤害和移动速度大幅提高，命中时有雷霆召唤者的连锁闪电，但是伤害吸收每半秒减少 1 点，用完后解放结束。格挡成功时，消耗 500 点魔力把挡下的伤害全部反弹给攻击者。
 * Achilles Shield. A relic. It is a shield and also a weapon. Each hit with it gives the holder 2 absorption, to a maximum of 20. With 10 absorption or more, a sneak-use starts the "release": the attack damage and the movement speed increase much, and a hit has the chain lightning of the Thundercaller, but the absorption decreases by 1 for each half second, and the release ends when the absorption is used up. A successful block uses 500 mana to return all the blocked damage to the attacker.
 */
public class AchillesShieldItem extends ManasteelShieldItem {

	public static final String TAG_RELEASED = "released";

	private static final float RELEASE_ABSORPTION_REQUIRE = 10.0F;
	private static final float MAX_ABSORPTION = 20.0F;

	public AchillesShieldItem(Properties properties) {
		super(properties.durability(ItemTiers.ACHILLES_SHIELD.getUses()), ItemTiers.ACHILLES_SHIELD);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!isReleased(stack)) {
			if (hand == InteractionHand.MAIN_HAND &&
					player.isSecondaryUseActive() &&
					player.getAbsorptionAmount() >= RELEASE_ABSORPTION_REQUIRE) {
				setReleased(stack, true);
				return InteractionResultHolder.success(stack);
			}
			return super.use(level, player, hand);
		} else {
			if (hand == InteractionHand.MAIN_HAND &&
					player.isSecondaryUseActive()) {
				setReleased(stack, false);
				return InteractionResultHolder.success(stack);
			}
			return InteractionResultHolder.pass(stack);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!world.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}

			if (isReleased(stack) && player.tickCount % 10 == 0) {
				if (player.getAbsorptionAmount() <= 0F) {
					setReleased(stack, false);
				}
				player.setAbsorptionAmount(player.getAbsorptionAmount() - 1);
			}
		}
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		super.onUseTick(level, entity, stack, remainingUseDuration);
		if (isReleased(stack)) {
			entity.stopUsingItem();
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (isReleased(stack)) {
			BotaniaItems.THUNDERCALLER.hurtEnemy(stack, target, attacker);
		} else {
			attacker.setAbsorptionAmount(Math.min(MAX_ABSORPTION, attacker.getAbsorptionAmount() + 2F));
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void onShieldBlock(ItemStack stack, LivingEntity blocker, DamageSource source, float damage) {
		super.onShieldBlock(stack, blocker, source, damage);
		Entity attacker = source.getEntity();
		if (attacker == null) {
			return;
		}
		/* 不好调，蒜鸟
		if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof Projectile projectile) {
			Entity target = projectile.getOwner();
			if (target != null) {
				Vec3 vec3 = target.getDeltaMovement();
				double d0 = target.getX() + vec3.x - blocker.getX();
				double d1 = target.getEyeY() - (double)1.1F - blocker.getY();
				double d2 = target.getZ() + vec3.z - blocker.getZ();
				double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		
				projectile.setOwner(blocker);
				projectile.setDeltaMovement(0, 0 ,0);
				projectile.setXRot(0);
				projectile.setYRot(0);
				projectile.setYRot(projectile.getXRot() - -20.0F);
				projectile.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
			}
		
			return;
		}
		*/

		if (blocker instanceof Player player && ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, 500, true)) {
			attacker.hurt(player.damageSources().thorns(player), damage);
		}
	}

	public static boolean isReleased(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_RELEASED, false);
	}

	public static void setReleased(ItemStack stack, boolean mode) {
		ItemNBTHelper.setBoolean(stack, TAG_RELEASED, mode);
	}

	public static void onModifyAttributes(ItemAttributeModifierEvent event) {
		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof AchillesShieldItem)) {
			return;
		}
		if (isReleased(stack)) {
			event.addModifier(Attributes.MOVEMENT_SPEED,
					new AttributeModifier(prefix("achilles_movement_speed"), 0.4F,
							AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
					EquipmentSlotGroup.MAINHAND);
		}
		// 从 1.20.5 起，伤害吸收不能超过"最大伤害吸收"属性，而这个属性默认是 0。没有这个修饰符时，盾牌攒不下任何伤害吸收。
		// From 1.20.5, absorption cannot be more than the "max absorption" attribute, and that attribute is 0 by default. Without this modifier, the shield cannot collect absorption.
		event.addModifier(Attributes.MAX_ABSORPTION,
				new AttributeModifier(prefix("achilles_max_absorption"), MAX_ABSORPTION,
						AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND);
		event.addModifier(Attributes.ATTACK_SPEED,
				new AttributeModifier(prefix("achilles_attack_speed"), -2.6,
						AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		event.addModifier(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(prefix("achilles_attack_damage"), isReleased(stack) ? 15 : 6,
						AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		event.addModifier(PixieHandler.PIXIE_SPAWN_CHANCE,
				PixieHandler.makeModifier(prefix("achilles_pixie_chance"), 0.5F),
				EquipmentSlotGroup.OFFHAND);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}
}
