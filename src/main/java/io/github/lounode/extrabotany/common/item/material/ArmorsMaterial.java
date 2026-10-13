package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import vazkii.botania.common.helper.RegistryHelper;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/** ExtraBotany's registered armor materials for Minecraft 1.21's holder-based API. */
public final class ArmorsMaterial {
	public static final int STARRY_IDOL_DURABILITY_FACTOR = 16;
	public static final int PLEIADS_MAID_COMBAT_DURABILITY_FACTOR = 50;
	public static final int GOBLIN_SLAYER_DURABILITY_FACTOR = 21;
	public static final int SHADOW_WARRIOR_DURABILITY_FACTOR = 23;
	public static final int MIKU_DURABILITY_FACTOR = 5;
	public static final int SHOOTING_GUARDIAN_DURABILITY_FACTOR = 34;
	public static final int SILENT_SAGES_DURABILITY_FACTOR = 50;

	private static final List<RegistryHelper.HolderProxy<ArmorMaterial>> ALL = new ArrayList<>();
	// 这三种材料的护甲值来自原版 1.16。原版的数组顺序是靴子、护腿、胸甲、头盔。
	// The armor values of these three materials come from the original 1.16. The array sequence of the original is boots, leggings, chestplate, helmet.
	public static final Holder<ArmorMaterial> MIKU = create("miku",
			Map.of(ArmorItem.Type.BOOTS, 2, ArmorItem.Type.LEGGINGS, 4, ArmorItem.Type.CHESTPLATE, 5, ArmorItem.Type.HELMET, 1),
			22, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ExtraBotanyItems.manaDrink), 0, 0);
	public static final Holder<ArmorMaterial> SHOOTING_GUARDIAN = create("shootingguardian",
			Map.of(ArmorItem.Type.BOOTS, 3, ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.CHESTPLATE, 8, ArmorItem.Type.HELMET, 4),
			34, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ExtraBotanyItems.orichalcos), 2, 0);
	public static final Holder<ArmorMaterial> SILENT_SAGES = create("silentsages",
			Map.of(ArmorItem.Type.BOOTS, 4, ArmorItem.Type.LEGGINGS, 8, ArmorItem.Type.CHESTPLATE, 9, ArmorItem.Type.HELMET, 5),
			40, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ExtraBotanyItems.orichalcos), 3, 0);

	public static final Holder<ArmorMaterial> STARRY_IDOL = create("starry_idol",
			Map.of(
					ArmorItem.Type.BOOTS, 2,
					ArmorItem.Type.LEGGINGS, 5,
					ArmorItem.Type.CHESTPLATE, 6,
					ArmorItem.Type.HELMET, 2),
			18, ExtraBotanySounds.ARMOR_EQUIP_IDOL, () -> Ingredient.of(BotaniaItems.MANAWEAVE_CLOTH), 0, 0);

	public static final Holder<ArmorMaterial> PLEIADS_MAID_COMBAT = create("pleiads_maid_combat",
			Map.of(
					ArmorItem.Type.BOOTS, 4,
					ArmorItem.Type.LEGGINGS, 7,
					ArmorItem.Type.CHESTPLATE, 8,
					ArmorItem.Type.HELMET, 3),
			50, ExtraBotanySounds.ARMOR_EQUIP_MAID, () -> Ingredient.of(ExtraBotanyItems.dasRheingold), 4, 0);

	public static final Holder<ArmorMaterial> GOBLIN_SLAYER = create("goblin_slayer",
			Map.of(
					ArmorItem.Type.BOOTS, 3,
					ArmorItem.Type.LEGGINGS, 6,
					ArmorItem.Type.CHESTPLATE, 6,
					ArmorItem.Type.HELMET, 3),
			40, ExtraBotanySounds.ARMOR_EQUIP_GOBLIN, () -> Ingredient.of(ExtraBotanyItems.photonium), 2.5F, 0);

	public static final Holder<ArmorMaterial> SHADOW_WARRIOR = create("shadow_warrior",
			Map.of(
					ArmorItem.Type.BOOTS, 3,
					ArmorItem.Type.LEGGINGS, 7,
					ArmorItem.Type.CHESTPLATE, 6,
					ArmorItem.Type.HELMET, 3),
			28, ExtraBotanySounds.ARMOR_EQUIP_WARRIOR, () -> Ingredient.of(ExtraBotanyItems.shadowium), 1.5F, 0);

	private static Holder<ArmorMaterial> create(String name, Map<ArmorItem.Type, Integer> defense,
			int enchantmentValue, Holder<SoundEvent> equipSound, Supplier<Ingredient> repairIngredient,
			float toughness, float knockbackResistance) {
		ResourceLocation id = prefix(name);
		RegistryHelper.HolderProxy<ArmorMaterial> proxy = RegistryHelper.lazyHolderProxy(
				Registries.ARMOR_MATERIAL, id,
				() -> new ArmorMaterial(defense, enchantmentValue, equipSound,
						repairIngredient, List.of(new ArmorMaterial.Layer(id)), toughness, knockbackResistance));
		ALL.add(proxy);
		return proxy;
	}

	public static void registerArmorMaterials(Registry<ArmorMaterial> registry) {
		ALL.forEach(proxy -> proxy.register(registry));
	}

	private ArmorsMaterial() {}
}
