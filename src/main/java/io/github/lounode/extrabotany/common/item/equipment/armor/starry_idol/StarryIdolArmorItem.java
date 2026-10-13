package io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;


import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.annotations.SoftImplement;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.client.IArmor;
import io.github.lounode.extrabotany.api.item.ArmorSet;
import io.github.lounode.extrabotany.api.item.ManaFixableItem;
import io.github.lounode.extrabotany.common.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 星空歌姬套装的盔甲，也是本模组其他重制版套装的基类。它用魔力代替耐久并自动修复，可以用幻影墨水隐藏外观，并且在提示文字里显示套装效果。
 * The armor of the Starry Idol set, and also the base class of the other Reburn armor sets of this mod. It uses mana in place of durability and repairs itself, its appearance can be hidden with Phantom Ink, and its tooltip shows the set effect.
 */
public class StarryIdolArmorItem extends ArmorItem implements CustomCreativeTabContents,
		ManaFixableItem, IArmor, ArmorSet, PhantomInkable {

	private static final String TAG_PHANTOM_INK = "phantomInk";
	private static final int MANA_PER_DAMAGE = 100;

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.starryIdolHeadgear),
			new ItemStack(ExtraBotanyItems.starryIdolSuit),
			new ItemStack(ExtraBotanyItems.starryIdolSkirt),
			new ItemStack(ExtraBotanyItems.starryIdolBoots)
	});

	public StarryIdolArmorItem(Type type, Properties properties) {
		this(ExtraBotanyAPI.instance().getStarryIdolArmorMaterial(), type, properties);
	}

	public StarryIdolArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	@SoftImplement("IForgeItem")
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return ToolCommons.damageItemIfPossible(stack, amount, entity, getManaPerDamage());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		// 套装进度只由戴在头上的那一件每秒检查一次。以前物品栏里的每一件盔甲每 tick 都检查，每次都要创建战利品上下文。
		// Only the piece that is worn on the head examines the set advancement, one time on each second. Before, each armor piece in the inventory examined it on each tick, and each check made a loot context.
		if (getType() == Type.HELMET && entity.tickCount % 20 == 0
				&& entity instanceof Player player && player.getItemBySlot(EquipmentSlot.HEAD) == stack) {
			triggerAdvancement(entity);
		}
		tickManaFix(stack, world, entity, slot, selected);
	}

	@Override
	public final net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
			ArmorMaterial.Layer layer, boolean innerModel) {
		return net.minecraft.resources.ResourceLocation.parse(hasPhantomInk(stack) ? ResourcesLib.MODEL_INVISIBLE_ARMOR : getArmorTextureAfterInk(stack, slot));
	}

	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_STARRY_IDOL;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
		TooltipHandler.addOnShift(list, flags, () -> addInformation(stack, context.level(), list, flags));
	}

	@Override
	public void addInformation(ItemStack stack, Level world, List<Component> list, TooltipFlag flags) {
		ArmorSet.super.addInformation(stack, world, list, flags);
		if (hasPhantomInk(stack)) {
			list.add(Component.translatable("botaniamisc.hasPhantomInk").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null || player.getInventory() == null || player.getInventory().armor == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(ExtraBotanyItems.starryIdolHeadgear);
			case CHEST -> stack.is(ExtraBotanyItems.starryIdolSuit);
			case LEGS -> stack.is(ExtraBotanyItems.starryIdolSkirt);
			case FEET -> stack.is(ExtraBotanyItems.starryIdolBoots);
			default -> false;
		};
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.starry_idol.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.starry_idol.desc")
				.withStyle(hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY));
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public boolean hasPhantomInk(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_PHANTOM_INK, false);
	}

	@Override
	public void setPhantomInk(ItemStack stack, boolean ink) {
		ItemNBTHelper.setBoolean(stack, TAG_PHANTOM_INK, ink);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(new ItemStack(me));
	}
}
