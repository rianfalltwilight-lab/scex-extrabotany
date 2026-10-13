package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.api.item.LeftClickItem;

import java.util.List;

/**
 * 遗物剑的基类。它处理灵魂绑定、魔力修复和左键技能的公共条件。
 * The base class for relic swords. It handles soulbind, repair with mana, and the common conditions of the left-click skill.
 *
 * <p>左键技能的条件：物品在主手、攻击冷却已满、玩家是遗物的主人。
 * <p>The conditions of the left-click skill: the item is in the main hand, the attack cooldown is full, and the player owns the relic.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemSwordRelic}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemSwordRelic}.
 */
public abstract class RelicSwordItem extends SwordItem implements LeftClickItem {
	private static final int MANA_PER_REPAIR = 240;

	protected RelicSwordItem(Tier tier, Properties props) {
		super(tier, props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	/**
	 * 检查左键技能的公共条件：在服务端、物品在主手、攻击冷却已满、玩家是遗物的主人。
	 * Checks the common conditions of a left-click skill: on the server, the item is in the main hand, the attack cooldown is full, and the player owns the relic.
	 *
	 * <p>不是剑的遗物武器也使用这个方法。
	 * <p>Relic weapons that are not swords also use this method.
	 */
	public static boolean canUseLeftClickSkill(Player player, Item item) {
		if (player.level().isClientSide() || player.isSpectator()) {
			return false;
		}
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(item) || player.getAttackStrengthScale(0F) < 1F) {
			return false;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	@Override
	public final void onLeftClick(Player player, @Nullable Entity target) {
		if (!canUseLeftClickSkill(player, this)) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!useSkill(player, stack, target)) {
			return;
		}
		if (target == null) {
			// 空挥不会重置服务端的攻击冷却。这里手动重置，否则玩家可以每 tick 触发一次技能。
			// A swing that hits nothing does not reset the attack cooldown on the server. Reset it here, or the player can start the skill on each tick.
			player.resetAttackStrengthTicker();
		}
	}

	/**
	 * 释放左键技能。子类在这里检查并扣除魔力。
	 * Uses the left-click skill. The subclass checks and takes the mana here.
	 *
	 * @return 技能成功释放时返回 {@code true}。 {@code true} when the skill was used.
	 */
	protected abstract boolean useSkill(Player player, ItemStack stack, @Nullable Entity target);

	/**
	 * 计算技能瞄准的坐标。有目标实体时瞄准实体上方 1 格；否则瞄准准星指向的方块上方。
	 * Calculates the aim position of the skill. With a target entity, the aim is 1 block above the entity. With no target, the aim is above the block at the crosshair.
	 *
	 * <p>原版取方块的整数坐标。这里取方块顶面的中心，所以落点更接近准星。
	 * <p>The original uses the integer position of the block. This port uses the center of the top face, so the impact point is nearer to the crosshair.
	 *
	 * @param range        射线的最大长度。 The maximum length of the ray.
	 * @param stopAtFluids 射线是否被流体挡住。 If a fluid stops the ray.
	 */
	protected static Vec3 aimPosition(LivingEntity user, @Nullable Entity target, double range, boolean stopAtFluids) {
		if (target != null) {
			return target.position().add(0, 1, 0);
		}
		BlockPos hit = ((BlockHitResult) user.pick(range, 1F, stopAtFluids)).getBlockPos();
		return Vec3.atBottomCenterOf(hit.above());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null) {
			relic.tickBinding(player);
		}
		if (stack.isDamaged() && ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_REPAIR, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	/**
	 * 掉在地上的遗物不会消失。
	 * A relic on the ground does not despawn.
	 */
	@Override
	public int getEntityLifespan(ItemStack stack, Level level) {
		return Integer.MAX_VALUE;
	}
}
