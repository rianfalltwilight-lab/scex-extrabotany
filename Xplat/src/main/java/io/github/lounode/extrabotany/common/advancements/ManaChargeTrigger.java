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
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ManaChargeTrigger extends SimpleCriterionTrigger<ManaChargeTrigger.TriggerInstance> {
	public static final ResourceLocation ID = prefix("mana_charge");
	public static final ManaChargeTrigger INSTANCE = new ManaChargeTrigger();

	public void trigger(ServerPlayer player, ItemStack itemStack, long mana) {
		this.trigger(player, instance -> instance.matches(itemStack, mana));
	}

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, ItemPredicate item,
			MinMaxBoundsExtension.Longs mana) implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ItemPredicate.CODEC.fieldOf("item").forGetter(TriggerInstance::item),
				MinMaxBoundsExtension.Longs.CODEC.fieldOf("mana").forGetter(TriggerInstance::mana)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> manaCharged(ItemPredicate item,
				MinMaxBoundsExtension.Longs mana) {
			return INSTANCE.createCriterion(new TriggerInstance(Optional.empty(), item, mana));
		}

		public boolean matches(ItemStack stack, long mana) {
			return this.item.test(stack) && this.mana.matches(mana);
		}
	}
}
