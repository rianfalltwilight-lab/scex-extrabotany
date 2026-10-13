package io.github.lounode.extrabotany.common.item.equipment.armor.silent_sages;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.List;
import java.util.function.Supplier;

/**
 * 静默贤者套装。穿齐四件时，每一件让工具的魔力消耗降低 15%，合计 60%。
 * Silent Sages armor set. With all four pieces, each piece decreases the mana cost of tools by 15%, for a total of 60%.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemSilentSagesArmor}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemSilentSagesArmor}.
 *
 * <p>和原版的差异：原版的套装判定引用了暗影武士盔甲的四件物品，所以套装永远凑不齐。这里改为引用静默贤者自己的四件。
 * 原版引用的盔甲贴图文件不存在，所以这里沿用暗影武士的贴图和模型。
 * <p>Differences from the original: the set check of the original refers to the four Shadow Warrior items, so the set can never be complete.
 * This port refers to the four Silent Sages items.
 * The armor texture file that the original refers to does not exist, so this port uses the texture and the model of the Shadow Warrior.
 */
public class SilentSagesArmorItem extends MikuArmorItem {
	private static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.silentSagesHelm),
			new ItemStack(ExtraBotanyItems.silentSagesChest),
			new ItemStack(ExtraBotanyItems.silentSagesLegs),
			new ItemStack(ExtraBotanyItems.silentSagesBoots)
	});

	public SilentSagesArmorItem(Type type, Properties props) {
		super(ArmorsMaterial.SILENT_SAGES, type, props);
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.silentsages.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.silentsages.desc")
				.withStyle(hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY));
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return LibMisc.MOD_ID + ":textures/models/armor/shadow_warrior.png";
	}
}
