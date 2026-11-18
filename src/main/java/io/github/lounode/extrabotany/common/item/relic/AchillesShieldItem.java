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
 * 勇士盾。遗物。它是盾牌，也是武器。用它攻击时，每次命中给持有者 2 点伤害吸收，最多 20 点。伤害吸收至少 12 点时，潜行并使用可以切换"解放"：攻击伤害从 6 变成 15，移动速度 +40%，用它攻击会召唤雷击，不能再举盾；解放时每 10 tick 消耗 1 点伤害吸收，用完自动结束。
 * Achilles Shield. A relic. It is a shield and also a weapon. Each hit with it gives the holder 2 absorption, to a maximum of 20. With at least 12 absorption, a sneak-use switches the "release": the attack damage becomes 15 instead of 6, the movement speed +40%, its hits call lightning, and it can no longer block; while released it uses 1 absorption each 10 ticks, and the release ends when none is left.
 *
 * <p>举盾移动时，撞到 1 格内的生物造成 13 + 1.5 × 锋利等级的魔法伤害并击退。格挡时用 500 魔力把伤害还给直接攻击者，并把弹射物反射回去。
 * <p>When the holder moves with the shield raised, it rams the creatures within 1 block for 13 + 1.5 × Sharpness magic damage and knocks them back. A block returns the damage to the direct attacker for 500 mana, and sends projectiles back.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemAchilleshield}（只有 1.12 有）。
 * <p>Source: {@code ItemAchilleshield} of ExtraMeteorP 1.12 (only 1.12 has it).
 */
public class AchillesShieldItem extends ManasteelShieldItem {

	public static final String TAG_RELEASED = "released";

	public static final float RELEASE_ABSORPTION_REQUIRE = 12.0F;
	public static final float RAM_DAMAGE = 13.0F;
	public static final int REFLECT_COST = 500;
	private static final float MAX_ABSORPTION = 20.0F;

	public AchillesShieldItem(Properties properties) {
		super(properties, ItemTiers.ACHILLES_SHIELD);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		// 1.12：潜行并且伤害吸收至少 12 时切换解放；没有解放时举盾。 / 1.12: with sneak and at least 12 absorption, switch the release; when not released, raise the shield.
		if (player.isSecondaryUseActive() && player.getAbsorptionAmount() >= RELEASE_ABSORPTION_REQUIRE) {
			setReleased(stack, !isReleased(stack));
			return InteractionResultHolder.success(stack);
		}
		if (!isReleased(stack)) {
			return super.use(level, player, hand);
		}
		return InteractionResultHolder.pass(stack);
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
				if (player.getAbsorptionAmount() > 0F) {
					player.setAbsorptionAmount(player.getAbsorptionAmount() - 1);
				} else {
					setReleased(stack, false);
				}
			}
		}
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		super.onUseTick(level, entity, stack, remainingUseDuration);
		if (isReleased(stack)) {
			entity.stopUsingItem();
			return;
		}
		if (!level.isClientSide()) {
			ram(stack, entity);
		}
	}

	/**
	 * 举盾冲撞：持盾者在水平方向移动时，撞 1 格内的生物。1.12 只在 motionX 或 motionZ 为正时生效，这里改成任何水平移动。
	 * The shield ram: while the holder moves horizontally, it hits the creatures within 1 block. 1.12 only worked when motionX or motionZ was positive; here any horizontal movement counts.
	 */
	public static void ram(ItemStack stack, LivingEntity user) {
		var motion = user.getDeltaMovement();
		if (motion.horizontalDistanceSqr() < 1.0E-6) {
			return;
		}
		var dir = motion.normalize();
		var registries = user.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
		int knockback = stack.getEnchantmentLevel(registries.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK));
		int sharpness = stack.getEnchantmentLevel(registries.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS));
		for (LivingEntity target : user.level().getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(1), e -> e != user)) {
			target.knockback(1.5F * (1 + knockback), -dir.x, -dir.z);
			if (user instanceof Player) {
				target.hurt(user.damageSources().magic(), RAM_DAMAGE + 1.5F * sharpness);
			}
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
		Entity direct = source.getDirectEntity();
		if (direct == null || source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
			return;
		}
		// 1.12 用魔法伤害；这里用荆棘伤害，两面勇士盾不会互相无限反弹。 / 1.12 used magic damage; here thorns damage, so two Achilles Shields do not bounce it forever.
		if (blocker instanceof Player player && ManaItemHandler.instance().requestManaExactForTool(stack, player, REFLECT_COST, true)) {
			direct.hurt(player.damageSources().thorns(player), damage);
		}
		// 1.12 把挡下的弹射物算作持盾者射出的。箭由原版的盾牌弹回；其他弹射物这里掉头。
		// 1.12 makes the blocked projectile count as shot by the holder. Arrows are bounced back by the vanilla shield; other projectiles turn around here.
		if (direct instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
			projectile.setOwner(blocker);
			if (!(projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow)) {
				projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-1));
			}
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
				PixieHandler.makeModifier(prefix("achilles_pixie_chance"), 1.0F),
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
