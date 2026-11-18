package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.item.SequentialBreaker;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;
import java.util.function.Predicate;

/**
 * 镀金究极锤。泰拉钢锤的升级版，放在自然祭坛上可以升级：镀金土豆泥（5 个）升级修复，元素钢剑升级锋利，泰拉钢锤解锁范围挖掘。
 * Ultimate Hammer. A better version of the terrasteel hammer that is upgraded on a pedestal: gilded mashed potatoes (5) upgrade the repair, an elementium sword upgrades the sharpness, and a terrasteel hammer unlocks the area mining.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemHammerUltimate} 和 {@code BlockPedestal}（对照原版审计 E6）。
 * <p>Source: {@code ItemHammerUltimate} and {@code BlockPedestal} of ExtraMeteorP 1.12 (parity audit E6).
 *
 * <ul>
 * <li>修复：每 40 − 10 × 修复等级 tick 用 80 − 5 × 修复等级 魔力修复 1 点耐久；修复等级最多 3。
 * Repair: every 40 − 10 × repair level ticks, 80 − 5 × repair level mana repair 1 durability; at most repair level 3.</li>
 * <li>锋利：攻击时花 80 × 锋利 − 15 × 修复 魔力，额外造成 2 × 锋利 的魔法伤害；锋利 2、4、6、8 起分别加缓慢、失明、霉运、凋零，10 回复 4 生命；最多 10 级。
 * Sharpness: an attack costs 80 × sharpness − 15 × repair mana and deals 2 × sharpness extra magic damage; from sharpness 2, 4, 6 and 8 it adds Slowness, Blindness, Bad Luck and Wither, at 10 it heals 4; at most level 10.</li>
 * <li>范围：解锁后右键开关，打开时挖掉一个面上的 3 × 3 方块，每次损耗 8 点耐久。
 * Range: after the unlock a use sets it on or off; while it is on, it breaks 3 × 3 blocks of one face, with 8 durability each time.</li>
 * </ul>
 * 它可以存 50000 魔力（不对外输出），和原版相同。它也能当斧和铲用。
 * It can store 50000 mana (never exported), as in the original. It also works as an axe and a shovel.
 */
public class UltimateHammerItem extends ManasteelHammerItem implements SequentialBreaker {
	public static final String TAG_ENABLED = "enabled";
	public static final String TAG_REPAIR = "repair";
	public static final String TAG_DAMAGE = "damage";
	public static final String TAG_RANGE = "range";
	public static final int MAX_MANA = 50000;
	public static final int MANA_PER_DAMAGE = 80;
	public static final int MAX_REPAIR = 3;
	public static final int MAX_ATTACK = 10;
	public static final int POTATOES_PER_REPAIR = 5;
	private static final int RANGE = 1;
	private static final int AREA_DAMAGE = 8;

	public UltimateHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	public static int getRepair(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_REPAIR, 0);
	}

	public static void setRepair(ItemStack stack, int level) {
		ItemNBTHelper.setInt(stack, TAG_REPAIR, level);
	}

	public static int getAttack(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_DAMAGE, 0);
	}

	public static void setAttack(ItemStack stack, int level) {
		ItemNBTHelper.setInt(stack, TAG_DAMAGE, level);
	}

	public static boolean hasRange(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_RANGE, false);
	}

	public static void setRange(ItemStack stack, boolean range) {
		ItemNBTHelper.setBoolean(stack, TAG_RANGE, range);
	}

	public static boolean isEnabled(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ENABLED, false);
	}

	public static void setEnabled(ItemStack stack, boolean enabled) {
		ItemNBTHelper.setBoolean(stack, TAG_ENABLED, enabled);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (getAttack(stack) > 0) {
			tooltip.add(Component.translatable("extrabotany.damageupgrade" + getAttack(stack)).withStyle(ChatFormatting.GRAY));
		}
		if (getRepair(stack) > 0) {
			tooltip.add(Component.translatable("extrabotany.repairupgrade" + getRepair(stack)).withStyle(ChatFormatting.GRAY));
		}
		if (hasRange(stack)) {
			tooltip.add(Component.translatable("extrabotany.rangeupgrade").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hasRange(stack)) {
			setEnabled(stack, !isEnabled(stack));
			if (!level.isClientSide()) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.HAMMER_USE, SoundSource.PLAYERS, 0.5F, 0.4F);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		int repair = getRepair(stack);
		if (!level.isClientSide() && entity instanceof Player player && stack.getDamageValue() > 0
				&& entity.tickCount % (40 - repair * 10) == 0
				&& ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_DAMAGE - repair * 5, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		int attack = getAttack(stack);
		if (attack > 0 && attacker instanceof Player player
				&& ManaItemHandler.instance().requestManaExact(stack, player, 80 * attack - getRepair(stack) * 15, true)) {
			if (attack > 1) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
			}
			if (attack > 3) {
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1));
			}
			if (attack > 5) {
				target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 2));
			}
			if (attack > 7) {
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
			}
			if (attack > 9) {
				attacker.heal(4F);
			}
			target.invulnerableTime = 0;
			target.hurt(attacker.damageSources().magic(), 2F * attack);
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	// 1.12 getToolClasses 是镐、铲和斧。 / getToolClasses of 1.12 is pickaxe, shovel and axe.
	private static boolean isToolBlock(BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_AXE);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return isToolBlock(state) ? getTier().getSpeed() : super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return isToolBlock(state) && !state.is(getTier().getIncorrectBlocksForDrops());
	}

	@SoftImplement("IForgeItem")
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		BlockHitResult raycast = ToolCommons.raytraceFromEntity(player, 10, false);
		if (!player.level().isClientSide && raycast.getType() == HitResult.Type.BLOCK) {
			breakOtherBlock(player, stack, pos, pos, raycast.getDirection());
		}
		return false;
	}

	@Override
	public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		if (!isEnabled(stack)) {
			return;
		}
		Level level = player.level();
		// 1.12 只挖石头、金属、冰、玻璃、泥土、沙子、雪和黏土这类材料，相当于镐和铲能挖的方块。
		// 1.12 only breaks the rock, metal, ice, glass, dirt, sand, snow and clay materials, which are the blocks of the pickaxe and the shovel.
		Predicate<BlockState> canMine = state -> (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL))
				&& !state.is(getTier().getIncorrectBlocksForDrops());
		if (level.isEmptyBlock(pos) || !canMine.test(level.getBlockState(pos))) {
			return;
		}
		boolean doX = side.getStepX() == 0;
		boolean doY = side.getStepY() == 0;
		boolean doZ = side.getStepZ() == 0;
		Vec3i beginDiff = new Vec3i(doX ? -RANGE : 0, doY ? -1 : 0, doZ ? -RANGE : 0);
		Vec3i endDiff = new Vec3i(doX ? RANGE : 0, doY ? RANGE * 2 - 1 : 0, doZ ? RANGE : 0);
		ToolCommons.removeBlocksInIteration(player, stack, level, pos, beginDiff, endDiff, canMine);
		stack.hurtAndBreak(AREA_DAMAGE, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
	}

	@Override
	public int getEntityLifespan(ItemStack stack, Level level) {
		return Integer.MAX_VALUE;
	}

	/**
	 * 在自然祭坛上升级。返回 true 表示用掉了手里的物品。
	 * Upgrades on a pedestal. True means that the held item was used.
	 */
	public static boolean tryUpgrade(ItemStack hammer, ItemStack held) {
		if (held.is(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.gildedPotatoMashed)
				&& getRepair(hammer) < MAX_REPAIR && held.getCount() >= POTATOES_PER_REPAIR) {
			setRepair(hammer, getRepair(hammer) + 1);
			held.shrink(POTATOES_PER_REPAIR);
			return true;
		}
		if (held.is(vazkii.botania.common.item.BotaniaItems.ELEMENTIUM_SWORD) && getAttack(hammer) < MAX_ATTACK) {
			setAttack(hammer, getAttack(hammer) + 1);
			held.shrink(1);
			return true;
		}
		if (held.is(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.terrasteelHammer) && !hasRange(hammer)) {
			setRange(hammer, true);
			held.shrink(1);
			return true;
		}
		return false;
	}

	/**
	 * 魔力接口：容量 50000，可以从魔力池和别的物品充入，不输出。
	 * The mana interface: a capacity of 50000, filled from a mana pool and from other items, no export.
	 */
	public record ManaStore(ItemStack stack) implements ManaItem {
		private static final String TAG_MANA = "mana";

		@SuppressWarnings("deprecation")
		@Override
		public int getMana() {
			return Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getInt(TAG_MANA), 0, MAX_MANA);
		}

		@Override
		public int getMaxMana() {
			return MAX_MANA;
		}

		@Override
		public void addMana(int amount) {
			int mana = (int) Math.clamp((long) getMana() + amount, 0, MAX_MANA);
			ItemNBTHelper.setInt(stack, TAG_MANA, mana);
		}

		@Override
		public boolean canReceiveManaFromPool(BlockEntity pool) {
			return true;
		}

		@Override
		public boolean acceptDispatchedManaFromItem(ItemStack other) {
			return true;
		}

		@Override
		public boolean refuseRequestedManaFromItem(ItemStack other) {
			return true;
		}

		@Override
		public boolean canDrainManaToPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canSendRequestedManaToItem(ItemStack other) {
			return false;
		}

		@Override
		public boolean isNoExport() {
			return true;
		}
	}
}
