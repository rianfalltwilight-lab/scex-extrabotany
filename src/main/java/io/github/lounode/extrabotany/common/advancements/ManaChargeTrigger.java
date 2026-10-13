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

/**
 * 进度触发器：某个物品里的魔力达到要求。魔力是 long 类型，因为大师魔力之戒的容量超过 int 的范围。
 * An advancement trigger: the mana in an item is in the specified range. The mana is a long value, because the capacity of the Master Band of Mana is more than the range of an int.
 */
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
