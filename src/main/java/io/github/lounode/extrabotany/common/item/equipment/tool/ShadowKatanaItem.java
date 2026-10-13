package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.PixieEntity;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 影刃。它在夜晚更强：攻击力 +10，攻击速度 +15%，命中时回复 2 点生命值。
 * Shadow Katana. The sword is stronger at night: +10 attack damage, +15% attack speed, and a hit gives back 2 health.
 *
 * <p>挥刀时消耗 100 点魔力，召唤一只精灵攻击目标。魔力还可以修复耐久。
 * <p>A swing uses 100 mana and summons a pixie that attacks the target. Mana also repairs the durability.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemShadowKatana} 和 {@code DarkPixieHandler}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemShadowKatana} and {@code DarkPixieHandler}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版每 tick 把"是否夜晚"写进物品数据。这里把夜晚加成直接加在玩家身上，物品数据不变。
 * The original writes "is it night" into the item data on each tick. This port puts the night bonus directly on the player, and the item data does not change.</li>
 * <li>原版空挥时攻击 8 格内的第一个生物，包括动物。这里空挥时只攻击怪物。
 * A swing at nothing in the original attacks the first living entity within 8 blocks, animals included. In this port such a swing only attacks monsters.</li>
 * <li>原版的精灵是暗色的。这里使用植物魔法的普通精灵。
 * The pixie of the original is dark. This port uses the usual pixie of Botania.</li>
 * </ul>
 */
public class ShadowKatanaItem extends SwordItem implements LeftClickItem {
	private static final int MANA_PER_REPAIR = 120;
	private static final int MANA_PER_PIXIE = 100;
	private static final double PIXIE_RANGE = 8;
	private static final float PIXIE_DAMAGE = 4F;
	private static final int PIXIE_EFFECT_DURATION_TICKS = 40;
	private static final float NIGHT_HEAL = 2F;
	private static final ResourceLocation NIGHT_DAMAGE_ID = prefix("shadow_katana_night_damage");
	private static final ResourceLocation NIGHT_SPEED_ID = prefix("shadow_katana_night_attack_speed");
	private static final AttributeModifier NIGHT_DAMAGE =
			new AttributeModifier(NIGHT_DAMAGE_ID, 10, AttributeModifier.Operation.ADD_VALUE);
	private static final AttributeModifier NIGHT_SPEED =
			new AttributeModifier(NIGHT_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	private static final List<Holder<MobEffect>> PIXIE_EFFECTS = List.of(
			MobEffects.BLINDNESS, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS, MobEffects.HUNGER);

	public ShadowKatanaItem(Properties props) {
		super(BotaniaAPI.instance().getElementiumItemTier(),
				props.attributes(createAttributes(BotaniaAPI.instance().getElementiumItemTier(), 3, -2.4F)));
	}

	/**
	 * 每 tick 更新玩家的夜晚加成。玩家主手拿着太刀并且是夜晚时，加成生效。
	 * Updates the night bonus of the player on each tick. The bonus applies when the player holds the katana in the main hand and it is night.
	 *
	 * <p>必须从服务端的玩家 tick 事件调用。物品离开背包时也要移除加成，所以不能放在物品自己的 tick 里。
	 * <p>The server player tick event must call this method. The bonus must also be removed when the item leaves the inventory, so the item tick is not sufficient.
	 */
	public static void updateNightBonus(Player player) {
		if (player.level().isClientSide()) {
			return;
		}
		boolean active = player.getMainHandItem().getItem() instanceof ShadowKatanaItem && !player.level().isDay();
		setModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), NIGHT_DAMAGE, active);
		setModifier(player.getAttribute(Attributes.ATTACK_SPEED), NIGHT_SPEED, active);
	}

	private static void setModifier(@Nullable AttributeInstance attribute, AttributeModifier modifier, boolean present) {
		if (attribute == null || attribute.hasModifier(modifier.id()) == present) {
			return;
		}
		if (present) {
			attribute.addTransientModifier(modifier);
		} else {
			attribute.removeModifier(modifier.id());
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		removeLegacyNightModifiers(stack);
		if (stack.isDamaged() && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_REPAIR, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	/**
	 * 清除旧版本写在物品上的夜晚加成。旧版本把加成存进物品的属性修饰符组件。
	 * Removes the night bonus that old versions wrote on the item. Old versions kept the bonus in the attribute modifier component of the item.
	 */
	private static void removeLegacyNightModifiers(ItemStack stack) {
		if (stack.getComponentsPatch().get(DataComponents.ATTRIBUTE_MODIFIERS) == null) {
			return;
		}
		ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		boolean hasLegacy = modifiers.modifiers().stream()
				.anyMatch(entry -> entry.modifier().is(NIGHT_DAMAGE_ID) || entry.modifier().is(NIGHT_SPEED_ID));
		if (hasLegacy) {
			stack.set(DataComponents.ATTRIBUTE_MODIFIERS, stack.getItem().components().get(DataComponents.ATTRIBUTE_MODIFIERS));
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!attacker.level().isClientSide() && !attacker.level().isDay()) {
			attacker.heal(NIGHT_HEAL);
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void onLeftClick(Player player, @Nullable Entity target) {
		if (!(player.level() instanceof ServerLevel level) || player.isSpectator()) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(this) || player.getAttackStrengthScale(0F) < 1F) {
			return;
		}
		LivingEntity pixieTarget = selectPixieTarget(player, target);
		if (pixieTarget == null) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_PIXIE, true)) {
			return;
		}
		spawnPixie(level, player, pixieTarget);
		if (target == null) {
			player.resetAttackStrengthTicker();
		}
	}

	@Nullable
	private static LivingEntity selectPixieTarget(Player player, @Nullable Entity attacked) {
		if (attacked instanceof LivingEntity living && living.isAlive()
				&& DamageHandler.INSTANCE.checkPassable(living, player)) {
			return living;
		}
		if (attacked != null) {
			return null;
		}
		AABB area = new AABB(player.blockPosition()).inflate(PIXIE_RANGE);
		LivingEntity nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity instanceof Enemy && DamageHandler.INSTANCE.checkPassable(entity, player))) {
			double distance = player.distanceToSqr(candidate);
			if (distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	/**
	 * 召唤一只攻击目标的精灵。精灵的伤害在夜晚翻倍。玩家穿着整套暗影武士盔甲时，精灵附带一个负面效果。
	 * Summons a pixie that attacks the target. The damage of the pixie is double at night. When the player has the full Shadow Warrior armor set, the pixie also gives a harmful effect.
	 */
	private static void spawnPixie(ServerLevel level, Player player, LivingEntity target) {
		PixieEntity pixie = new PixieEntity(level, false);
		pixie.setPos(player.getX(), player.getY() + 3, player.getZ());
		if (((ShadowWarriorArmorItem) ExtraBotanyItems.shadowWarriorHelmet).hasArmorSet(player)) {
			Holder<MobEffect> effect = PIXIE_EFFECTS.get(player.getRandom().nextInt(PIXIE_EFFECTS.size()));
			pixie.setApplyPotionEffect(new MobEffectInstance(effect, PIXIE_EFFECT_DURATION_TICKS, 0));
		}
		float damage = level.isDay() ? PIXIE_DAMAGE : PIXIE_DAMAGE * 2;
		pixie.setProps(target, player, damage);
		EventHooks.finalizeMobSpawn(pixie, level, level.getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.EVENT, null);
		level.addFreshEntity(pixie);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(Component.translatable("tooltip.extrabotany.shadow_katana").withStyle(ChatFormatting.GRAY));
	}
}
