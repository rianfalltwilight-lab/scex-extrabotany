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
	public static final int PLEIADS_MAID_COMBAT_DURABILITY_FACTOR = 40;
	public static final int GOBLIN_SLAYER_DURABILITY_FACTOR = 27;
	public static final int SHADOW_WARRIOR_DURABILITY_FACTOR = 24;
	public static final int MIKU_DURABILITY_FACTOR = 5;
	public static final int SHOOTING_GUARDIAN_DURABILITY_FACTOR = 34;
	public static final int SILENT_SAGES_DURABILITY_FACTOR = 50;

	private static final List<RegistryHelper.HolderProxy<ArmorMaterial>> ALL = new ArrayList<>();
	// 初音、射手、沉默贤者以及下面的女仆、哥布林杀手、暗影战士的耐久、护甲、附魔值和韧性来自原版 1.16 的 ExtraBotanyAPI.ArmorMaterial。原版的数组顺序是靴子、护腿、胸甲、头盔。装备音效留到 E7。
	// The durability, armor, enchantability and toughness of Miku, Shooting Guardian, Silent Sages and of Maid, Goblin Slayer and Shadow Warrior below come from ExtraBotanyAPI.ArmorMaterial of the original 1.16. The array sequence of the original is boots, leggings, chestplate, helmet. The equip sounds also come from it (parity audit E7: maid diamond, goblin slayer and shadow warrior iron).
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
					ArmorItem.Type.CHESTPLATE, 9,
					ArmorItem.Type.HELMET, 4),
			32, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_DIAMOND, () -> Ingredient.of(ExtraBotanyItems.dasRheingold), 3, 0);

	public static final Holder<ArmorMaterial> GOBLIN_SLAYER = create("goblin_slayer",
			Map.of(
					ArmorItem.Type.BOOTS, 3,
					ArmorItem.Type.LEGGINGS, 5,
					ArmorItem.Type.CHESTPLATE, 7,
					ArmorItem.Type.HELMET, 2),
			30, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ExtraBotanyItems.photonium), 1, 0);

	public static final Holder<ArmorMaterial> SHADOW_WARRIOR = create("shadow_warrior",
			Map.of(
					ArmorItem.Type.BOOTS, 2,
					ArmorItem.Type.LEGGINGS, 5,
					ArmorItem.Type.CHESTPLATE, 6,
					ArmorItem.Type.HELMET, 2),
			26, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ExtraBotanyItems.shadowium), 1, 0);

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
