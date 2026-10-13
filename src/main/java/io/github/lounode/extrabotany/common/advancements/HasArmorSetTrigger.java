package io.github.lounode.extrabotany.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.Arrays;
import java.util.Optional;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 进度触发器：玩家穿着一整套指定的盔甲。穿着血染昴宿星团战斗女仆服时，它当作普通的女仆服套装来判断。
 * An advancement trigger: the player wears a full set of the specified armor. With the Sanguine Pleiades Combat Maid Suit, the check is the same as for the usual maid set.
 */
public class HasArmorSetTrigger extends SimpleCriterionTrigger<HasArmorSetTrigger.TriggerInstance> {

	public static final ResourceLocation ID = prefix("has_armor_set");
	public static final HasArmorSetTrigger INSTANCE = new HasArmorSetTrigger();

	public void trigger(ServerPlayer player) {
		super.trigger(player, instance -> instance.matches(player));
	}

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, ArmorPredicates armor)
			implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ArmorPredicates.CODEC.fieldOf("armor").forGetter(TriggerInstance::armor)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> forArmorSet(ItemStack[] armorSet) {
			ItemPredicate[] predicates = Arrays.stream(armorSet)
					.map(item -> ItemPredicate.Builder.item().of(item.getItem()).build())
					.toArray(ItemPredicate[]::new);
			if (predicates.length != 4) {
				throw new IllegalArgumentException("Armor sets must contain exactly four items");
			}
			return INSTANCE.createCriterion(new TriggerInstance(Optional.empty(),
					new ArmorPredicates(predicates[0], predicates[1], predicates[2], predicates[3])));
		}

		public boolean matches(Player player) {
			if (player.getItemBySlot(EquipmentSlot.CHEST).is(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit)) {
				return matchesDarkened(player);
			}
			return armor.head().test(player.getItemBySlot(EquipmentSlot.HEAD))
					&& armor.chest().test(player.getItemBySlot(EquipmentSlot.CHEST))
					&& armor.legs().test(player.getItemBySlot(EquipmentSlot.LEGS))
					&& armor.feet().test(player.getItemBySlot(EquipmentSlot.FEET));
		}

		private boolean matchesDarkened(Player player) {
			return player.getItemBySlot(EquipmentSlot.HEAD).is(ExtraBotanyItems.pleiadesCombatMaidHeadgear)
					&& armor.head().test(player.getItemBySlot(EquipmentSlot.HEAD))
					&& player.getItemBySlot(EquipmentSlot.LEGS).is(ExtraBotanyItems.pleiadesCombatMaidSkirt)
					&& armor.legs().test(player.getItemBySlot(EquipmentSlot.LEGS))
					&& player.getItemBySlot(EquipmentSlot.FEET).is(ExtraBotanyItems.pleiadesCombatMaidBoots)
					&& armor.feet().test(player.getItemBySlot(EquipmentSlot.FEET));
		}
	}

	public record ArmorPredicates(ItemPredicate head, ItemPredicate chest, ItemPredicate legs,
			ItemPredicate feet) {
		public static final Codec<ArmorPredicates> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemPredicate.CODEC.fieldOf("head").forGetter(ArmorPredicates::head),
				ItemPredicate.CODEC.fieldOf("chest").forGetter(ArmorPredicates::chest),
				ItemPredicate.CODEC.fieldOf("legs").forGetter(ArmorPredicates::legs),
				ItemPredicate.CODEC.fieldOf("feet").forGetter(ArmorPredicates::feet)
		).apply(instance, ArmorPredicates::new));
	}
}
