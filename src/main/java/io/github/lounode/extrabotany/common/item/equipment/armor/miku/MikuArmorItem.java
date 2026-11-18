package io.github.lounode.extrabotany.common.item.equipment.armor.miku;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaDiscountArmor;
import vazkii.botania.api.mana.ManaItemHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.List;
import java.util.function.Supplier;

/**
 * 星空歌姬（初音）套装。穿齐四件时，每一件让工具的魔力消耗降低 15%，合计 60%。
 * Starry Idol (Miku) armor set. With all four pieces, each piece decreases the mana cost of tools by 15%, for a total of 60%.
 *
 * <p>植物魔法把每个护甲栏的折扣相加，所以四件的折扣会叠加。原版也是这样。
 * <p>Botania adds the discounts of all armor slots, so the discounts of the four pieces add together. The original does the same.
 *
 * <p>受到的耐久损耗优先用魔力支付，每点 70 魔力。修复 1 点耐久需要 140 魔力。
 * <p>Durability loss is paid with mana first, at 70 mana for each point. The repair of 1 durability point takes 140 mana.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemMikuArmor}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemMikuArmor}.
 */
public class MikuArmorItem extends StarryIdolArmorItem implements ManaDiscountArmor {
	private static final int MANA_PER_DAMAGE = 70;
	private static final float SET_DISCOUNT = 0.15F;

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.mikuHelm),
			new ItemStack(ExtraBotanyItems.mikuChest),
			new ItemStack(ExtraBotanyItems.mikuLegs),
			new ItemStack(ExtraBotanyItems.mikuBoots)
	});

	public MikuArmorItem(Type type, Properties props) {
		this(ArmorsMaterial.MIKU, type, props);
	}

	protected MikuArmorItem(Holder<ArmorMaterial> material, Type type, Properties props) {
		super(material, type, props);
	}

	@Override
	public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
		return hasArmorSet(player) ? SET_DISCOUNT : 0F;
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		triggerAdvancement(entity);
		if (level.isClientSide() || !(entity instanceof Player player) || !stack.isDamaged()) {
			return;
		}
		// 修复的价格是损耗价格的两倍，和原版一致。
		// The repair price is two times the loss price, as in the original.
		if (ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	/**
	 * 1.16 的套装用盔甲上的一个标记决定昼夜加成。标记一变，物品就变了，游戏会重新计算这件盔甲的属性。只有变化时才写，返回是否变了。
	 * The sets of 1.16 decide the day or night bonus with a flag on the armor. When the flag changes, the item changes, and the game calculates the attributes of the piece again. It only writes on a change, and returns if there was one.
	 */
	protected static boolean setFlag(ItemStack stack, String tag, boolean value) {
		if (ItemNBTHelper.getBoolean(stack, tag, false) == value) {
			return false;
		}
		if (value) {
			ItemNBTHelper.setBoolean(stack, tag, true);
		} else {
			ItemNBTHelper.removeEntry(stack, tag);
		}
		return true;
	}

	protected static boolean getFlag(ItemStack stack, String tag) {
		return ItemNBTHelper.getBoolean(stack, tag, false);
	}

	/** 这件盔甲是不是正穿在这个玩家身上的那一件。 / Tells if this piece is the one that the player wears. */
	protected boolean isWorn(Player player, ItemStack stack) {
		return player.getItemBySlot(getEquipmentSlot()) == stack;
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}
		ItemStack worn = player.getItemBySlot(slot);
		for (ItemStack piece : getArmorSetStacks()) {
			if (worn.is(piece.getItem())) {
				return ((StarryIdolArmorItem) piece.getItem()).getEquipmentSlot() == slot;
			}
		}
		return false;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.miku.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.miku.desc")
				.withStyle(hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY));
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return LibMisc.MOD_ID + ":textures/models/armor/miku.png";
	}
}
