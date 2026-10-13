package io.github.lounode.extrabotany.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 王之花园。它保存最多 20 朵花。按住使用键时，它在身后依次召唤每朵花对应的"花之武器"，每把消耗 100 魔力。全部召唤完之后松开，武器飞向玩家瞄准的位置。
 * Garden of King's Law. It keeps a maximum of 20 flowers. While the use key is held, it summons the Flower Weapon of each flower behind the player in sequence, for 100 mana each. A release after all are summoned makes the weapons fly to the position that the player aims at.
 *
 * <p>把王之花园放在自然基座上，再用花右键基座，就可以把花加进去。
 * <p>To add a flower: put the garden on a Nature Pedestal, then use the flower on the pedestal.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemKingGarden} 和 {@code BlockPedestal}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemKingGarden} and {@code BlockPedestal}.
 *
 * <p>和原版的差异：物品不在主手或者副手被选中时，蓄力状态会重置。原版只在全部武器召唤完再松开时重置，所以中途松开会让状态卡住。
 * <p>Difference from the original: the charge state is reset when the item is not selected. The original only resets it on a release after all weapons are summoned, so an early release makes the state stay.
 */
public class KingGardenItem extends Item {
	public static final int MAX_FLOWERS = 20;
	public static final int WEAPON_TYPES = 17;

	// 这三个键写在物品的自定义数据里。名称和旧版本相同，所以旧存档里的配置仍然有效。
	// These three keys are in the custom data of the item. The names are the same as in older versions, so the configuration in old saves is still valid.
	private static final String TAG_TYPES = "type";
	private static final String TAG_CHARGING = "charging";
	private static final String TAG_SPAWNED = "weapons_spawned";

	private static final int MANA_PER_WEAPON = 100;
	private static final int WEAPONS_PER_ROW = 5;
	private static final int USE_DURATION_TICKS = 72000;
	private static final int TOOLTIP_FLOWER_LINES = 5;

	// 每种武器对应的花。下标是武器的种类编号，顺序和原版相同。
	// The flower for each weapon. The index is the weapon type number, in the same sequence as the original.
	private static final String[] FLOWERS = {
			"extrabotany:blood_enchantress", "extrabotany:sunshine_lily", "extrabotany:moonlight_lily", "extrabotany:stardustlotus",
			"extrabotany:stonesia", "botania:entropinnyum", "botania:dreadthorne", "botania:medumone", "botania:thermalily",
			"botania:tigerseye", "botania:bellethorne", "botania:heisei_dream", "extrabotany:annoyingflower", "extrabotany:manalink",
			"extrabotany:omniviolet", "extrabotany:bellflower", "extrabotany:tinkle"
	};

	public KingGardenItem(Properties props) {
		super(props);
	}

	private static CompoundTag data(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/**
	 * 返回物品里保存的武器种类编号，按加入的顺序排列。
	 * Returns the weapon type numbers in the item, in the sequence that they were added.
	 */
	public static int[] getTypes(ItemStack stack) {
		return data(stack).getIntArray(TAG_TYPES);
	}

	public static boolean isCharging(ItemStack stack) {
		return data(stack).getBoolean(TAG_CHARGING);
	}

	/**
	 * 返回这朵花对应的武器种类编号。它不是王之花园接受的花时返回 -1。
	 * Returns the weapon type number of this flower. The value is -1 when the garden does not accept it.
	 */
	public static int getFlowerType(ItemStack flower) {
		if (flower.isEmpty()) {
			return -1;
		}
		String id = BuiltInRegistries.ITEM.getKey(flower.getItem()).toString();
		return Arrays.asList(FLOWERS).indexOf(id);
	}

	/**
	 * 把一朵花加进王之花园。
	 * Adds one flower to the garden.
	 *
	 * @return 加入成功时返回 {@code true}。花不被接受或者花园已满时返回 {@code false}。 {@code true} when the flower was added. {@code false} when the flower is not accepted or the garden is full.
	 */
	public static boolean addFlower(ItemStack garden, ItemStack flower) {
		int type = getFlowerType(flower);
		int[] current = getTypes(garden);
		if (!(garden.getItem() instanceof KingGardenItem) || type < 0 || current.length >= MAX_FLOWERS) {
			return false;
		}
		int[] next = Arrays.copyOf(current, current.length + 1);
		next[current.length] = type;
		CustomData.update(DataComponents.CUSTOM_DATA, garden, tag -> tag.putIntArray(TAG_TYPES, next));
		return true;
	}

	private static void resetCharge(ItemStack stack) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putBoolean(TAG_CHARGING, false);
			tag.putInt(TAG_SPAWNED, 0);
		});
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(TAG_CHARGING, true));
		player.startUsingItem(hand);
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingTicks) {
		int[] types = getTypes(stack);
		int spawned = Math.max(0, data(stack).getInt(TAG_SPAWNED));
		if (level.isClientSide() || remainingTicks == getUseDuration(stack, living) || spawned >= types.length) {
			return;
		}
		if (living instanceof Player player && !ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_WEAPON, true)) {
			return;
		}

		FlowerWeaponEntity weapon = new FlowerWeaponEntity(level, living, types[spawned], spawned, Mth.wrapDegrees(-living.getYRot() + 180F));
		weapon.setPos(weaponPosition(living, spawned));
		weapon.setYRot(living.getYRot());
		level.addFreshEntity(weapon);
		level.playSound(null, weapon.getX(), weapon.getY(), weapon.getZ(), BotaniaSounds.TREASURE_WEAPON_SPAWN, SoundSource.PLAYERS,
				1F, 1F + level.getRandom().nextFloat() * 3F);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG_SPAWNED, spawned + 1));
	}

	/**
	 * 计算第 {@code index} 把武器的位置。武器在玩家身后排成半圆形的扇面，每排 5 把。
	 * Calculates the position of weapon number {@code index}. The weapons make half-circle rows of 5 behind the player.
	 */
	private static Vec3 weaponPosition(LivingEntity living, int index) {
		Vec3 look = living.getLookAngle().multiply(1.0, 0.0, 1.0);
		if (look.lengthSqr() < 1.0E-8) {
			double yaw = Math.toRadians(living.getYRot() + 90F);
			look = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
		}
		Vec3 direction = look.normalize();
		int row = index / WEAPONS_PER_ROW;
		Vec3 origin = living.position().add(0.0, living.getBbHeight() / 2.0 + 1.6, row * 0.1).add(direction.scale(-2.0));

		Vec3 axis = direction.cross(new Vec3(-1.0, 0.0, -1.0)).normalize();
		if (axis.lengthSqr() < 1.0E-8) {
			axis = new Vec3(0.0, 1.0, 0.0);
		}
		double angle = index % WEAPONS_PER_ROW * Math.PI / 4.0 - Math.PI / 2.0;
		double radius = row * 3.5 + 5.0;
		Vec3 offset = axis.scale(Math.cos(angle)).add(direction.cross(axis).scale(Math.sin(angle))).scale(radius);
		return origin.add(offset.x, Math.abs(offset.y), offset.z);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remainingTicks) {
		// 全部武器召唤完之后松开，蓄力结束，武器开始飞行。
		// A release after all weapons are summoned ends the charge, and the weapons start to fly.
		if (data(stack).getInt(TAG_SPAWNED) >= getTypes(stack).length) {
			resetCharge(stack);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && !selected && isCharging(stack)
				&& !(entity instanceof LivingEntity living && living.getOffhandItem() == stack)) {
			resetCharge(stack);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity living) {
		return USE_DURATION_TICKS;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		int[] types = getTypes(stack);
		tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.configuration", types.length, MAX_FLOWERS)
				.withStyle(ChatFormatting.GRAY));
		if (types.length == 0) {
			return;
		}
		tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.flowers").withStyle(ChatFormatting.DARK_GRAY));
		for (int i = 0; i < Math.min(TOOLTIP_FLOWER_LINES, types.length); i++) {
			int type = types[i];
			Component name = type >= 0 && type < FLOWERS.length
					? Component.translatable(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(FLOWERS[type])).getDescriptionId())
					: Component.literal("?");
			tooltip.add(Component.literal(" - ").append(name).withStyle(ChatFormatting.DARK_GRAY));
		}
		if (types.length > TOOLTIP_FLOWER_LINES) {
			tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.more", types.length - TOOLTIP_FLOWER_LINES)
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
