package io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.item.IPhotonium;
import io.github.lounode.extrabotany.client.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

import java.util.List;
import java.util.function.Supplier;

public class GoblinSlayerArmorItem extends StarryIdolArmorItem implements IPhotonium {

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.goblinSlayerHelmet),
			new ItemStack(ExtraBotanyItems.goblinSlayerChestplate),
			new ItemStack(ExtraBotanyItems.goblinSlayerLeggings),
			new ItemStack(ExtraBotanyItems.goblinSlayerBoots)
	});

	public GoblinSlayerArmorItem(Type type, Properties properties) {
		this(ExtraBotanyAPI.instance().getGoblinSlayerArmorMaterial(), type, properties);
	}

	public GoblinSlayerArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		photoniumTick(stack, world, entity, slot, selected);
	}

	@Override
	public void photoniumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		if ((living.getItemBySlot(type.getSlot()) != stack)) {
			return;
		}
		if (living.tickCount % (3 * 20) == 0) {
			return;
		}

		if (living instanceof Player player &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getPhotoniumBuffMana(), true)) {
			return;
		}

		if (isDay(level) && isOutCave(level, entity.blockPosition())) {
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5 * 20, 1));
		}
	}

	@Override
	public boolean isOutCave(Level level, BlockPos pos) {
		return true;
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_GOBLIN_SLAYER;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.goblin_slayer.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.goblin_slayer.desc0")
				.withStyle(hasArmorSet ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
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
			case HEAD -> stack.is(ExtraBotanyItems.goblinSlayerHelmet);
			case CHEST -> stack.is(ExtraBotanyItems.goblinSlayerChestplate);
			case LEGS -> stack.is(ExtraBotanyItems.goblinSlayerLeggings);
			case FEET -> stack.is(ExtraBotanyItems.goblinSlayerBoots);
			default -> false;
		};
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	public static boolean isGoblin(Entity entity) {
		return entity.getType().is(ExtraBotanyTags.Entities.GOBLINS);
	}
}
