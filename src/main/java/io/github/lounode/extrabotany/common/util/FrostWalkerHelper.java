package io.github.lounode.extrabotany.common.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * 让物品产生"冰霜行者"附魔的效果。
 * Makes an item give the effect of the Frost Walker enchantment.
 *
 * <p>1.21 的冰霜行者由数据包定义。旧版本的静态方法已经不存在。
 * <p>In 1.21 a data pack defines Frost Walker. The static method of the old versions does not exist.
 *
 * <p>这里直接运行附魔的效果组件。这样数据包对冰霜行者的修改也会生效。
 * <p>This class runs the effect components of the enchantment. Thus data pack changes to Frost Walker also apply.
 */
public final class FrostWalkerHelper {

	private FrostWalkerHelper() {}

	/**
	 * 在实体脚下结冰。调用者必须在实体换到新的方块位置时调用。
	 * Freezes the water below the entity. The caller must call this method when the entity moves to a new block position.
	 *
	 * @param stack            提供效果的物品。 The item that gives the effect.
	 * @param entity           获得效果的实体。 The entity that gets the effect.
	 * @param level            实体所在的世界。 The level of the entity.
	 * @param enchantmentLevel 等效的附魔等级。等级越高，结冰半径越大。 The equivalent enchantment level. A higher level gives a larger radius.
	 */
	public static void freeze(ItemStack stack, LivingEntity entity, ServerLevel level, int enchantmentLevel) {
		// 原版要求实体站在地面上。原模组强制满足这个条件，所以跳跃时也能结冰。
		// Vanilla requires that the entity is on the ground. The original mod forces this condition, so the effect also applies during a jump.
		boolean wasOnGround = entity.onGround();
		entity.setOnGround(true);
		try {
			Enchantment frostWalker = level.holderLookup(Registries.ENCHANTMENT)
					.getOrThrow(Enchantments.FROST_WALKER).value();
			EnchantedItemInUse itemInUse = new EnchantedItemInUse(stack, EquipmentSlot.FEET, entity);
			for (var effect : frostWalker.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
				effect.effect().onChangedBlock(level, enchantmentLevel, itemInUse, entity, entity.position(), false);
			}
		} finally {
			entity.setOnGround(wasOnGround);
		}
	}
}
