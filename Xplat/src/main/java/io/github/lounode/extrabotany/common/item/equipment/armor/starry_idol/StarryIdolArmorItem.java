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

import org.jetbrains.annotations.Nullable;

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
import io.github.lounode.extrabotany.client.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
		triggerAdvancement(entity);
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
