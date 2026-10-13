package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 四种符石和四元归一石的互斥规则。
 * The exclusion rules for the four elemental stones and The Community.
 *
 * <p>四元归一石包含四种符石的全部效果。所以玩家不能同时佩戴四元归一石和任意一种符石。
 * <p>The Community has all the effects of the four stones. Thus a player cannot wear The Community together with a stone.
 *
 * <p>来源：ExtraMeteorP 1.16 各符石的 {@code canEquip}。
 * <p>Source: {@code canEquip} of each stone in ExtraMeteorP 1.16.
 */
public final class ElementalStones {

	private ElementalStones() {}

	/**
	 * 判断实体是否佩戴了指定的饰品。
	 * Finds if the entity wears the given bauble.
	 */
	public static boolean isWearing(Item bauble, LivingEntity entity) {
		return !EquipmentHandler.findOrEmpty(bauble, entity).isEmpty();
	}

	/**
	 * 判断实体能否佩戴一种符石。
	 * Finds if the entity can equip an elemental stone.
	 */
	public static boolean canEquipStone(Item stone, LivingEntity entity) {
		return !isWearing(stone, entity) && !isWearing(ExtraBotanyItems.theCommunity, entity);
	}

	/**
	 * 判断实体能否佩戴四元归一石。
	 * Finds if the entity can equip The Community.
	 */
	public static boolean canEquipCommunity(LivingEntity entity) {
		return !isWearing(ExtraBotanyItems.theCommunity, entity)
				&& !isWearing(ExtraBotanyItems.aeroStone, entity)
				&& !isWearing(ExtraBotanyItems.earthStone, entity)
				&& !isWearing(ExtraBotanyItems.aquaStone, entity)
				&& !isWearing(ExtraBotanyItems.ignisStone, entity);
	}

	/**
	 * 判断玩家是否拥有水之符石的魔力折扣。水之符石和四元归一石都提供这个折扣。
	 * Finds if the entity has the mana discount of the Aqua Stone. The Aqua Stone and The Community give this discount.
	 */
	public static boolean hasAquaDiscount(LivingEntity entity) {
		return isWearing(ExtraBotanyItems.aquaStone, entity) || isWearing(ExtraBotanyItems.theCommunity, entity);
	}
}
