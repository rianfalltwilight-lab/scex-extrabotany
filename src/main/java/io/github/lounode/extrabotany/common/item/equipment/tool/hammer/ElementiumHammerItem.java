package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 源质钢锤。它挖掘时不掉落"一次性"的方块（和源质钢镐相同），潜行时除外。拿在主手时，受伤后召唤小精灵的几率增加 5%。
 * Elementium Hammer. When it mines, the "disposable" blocks do not drop (the same as the Elementium Pickaxe), but not while the player sneaks. In the main hand it adds 5% to the chance to summon a pixie after a hit.
 */
public class ElementiumHammerItem extends ManasteelHammerItem {

	private static final int MANA_PER_DAMAGE = 70;

	public ElementiumHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	public static boolean shouldFilterOut(Entity e, ItemStack tool, ItemStack drop) {
		if (tool.isEmpty() || !tool.is(ExtraBotanyItems.elementiumHammer)) {
			return false;
		}
		if (e.isShiftKeyDown()) {
			return false;
		}

		return (isDisposable(drop) || isSemiDisposable(drop));
	}

	private static boolean isDisposable(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		return stack.is(BotaniaTags.Items.DISPOSABLE);
	}

	private static boolean isSemiDisposable(ItemStack stack) {
		return stack.is(BotaniaTags.Items.SEMI_DISPOSABLE);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	public static void onModifyAttributes(ItemAttributeModifierEvent event) {
		if (!event.getItemStack().is(ExtraBotanyItems.elementiumHammer)) {
			return;
		}
		event.addModifier(PixieHandler.PIXIE_SPAWN_CHANCE,
				PixieHandler.makeModifier(prefix("elementium_hammer_pixie_chance"), 0.05),
				EquipmentSlotGroup.MAINHAND);
	}
}
