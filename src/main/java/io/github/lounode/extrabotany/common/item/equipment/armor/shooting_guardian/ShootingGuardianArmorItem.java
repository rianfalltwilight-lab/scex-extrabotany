package io.github.lounode.extrabotany.common.item.equipment.armor.shooting_guardian;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 银翼护卫套装。每一件增加 10% 飞行速度、10% 移动速度、10% 攻击伤害和 3% 攻击速度。
 * Shooting Guardian armor set. Each piece adds 10% flight speed, 10% movement speed, 10% attack damage and 3% attack speed.
 *
 * <p>套装效果在 {@link ShootingGuardianHelmetItem} 中。
 * <p>The set effects are in {@link ShootingGuardianHelmetItem}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemShootingGuardianArmor}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemShootingGuardianArmor}.
 */
public class ShootingGuardianArmorItem extends MikuArmorItem {
	private static final double SPEED_BONUS = 0.10;
	private static final double DAMAGE_BONUS = 0.10;
	private static final double ATTACK_SPEED_BONUS = 0.03;

	private static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.shootingGuardianHelm),
			new ItemStack(ExtraBotanyItems.shootingGuardianChest),
			new ItemStack(ExtraBotanyItems.shootingGuardianLegs),
			new ItemStack(ExtraBotanyItems.shootingGuardianBoots)
	});

	public ShootingGuardianArmorItem(Type type, Properties props) {
		super(ArmorsMaterial.SHOOTING_GUARDIAN, type, props);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		ResourceLocation id = prefix("shooting_guardian_" + getType().getName());
		EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(getType().getSlot());
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.FLYING_SPEED,
						new AttributeModifier(id, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
				.withModifierAdded(Attributes.MOVEMENT_SPEED,
						new AttributeModifier(id, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
				.withModifierAdded(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(id, DAMAGE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
				.withModifierAdded(Attributes.ATTACK_SPEED,
						new AttributeModifier(id, ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot);
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.shootingguardian.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		ChatFormatting color = hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY;
		for (int line = 0; line < 4; line++) {
			list.add(Component.translatable("extrabotany.armorset.shootingguardian.desc" + line).withStyle(color));
		}
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return LibMisc.MOD_ID + (slot == EquipmentSlot.HEAD
				? ":textures/models/armor/shootingguardian_helmet.png"
				: ":textures/models/armor/shootingguardian.png");
	}
}
