package io.github.lounode.extrabotany.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 进度触发器：玩家使用某个物品的次数达到要求。
 * An advancement trigger: the number of times that the player used an item is in the specified range.
 */
public class ItemUsedTrigger extends SimpleCriterionTrigger<ItemUsedTrigger.TriggerInstance> {
	public static final ResourceLocation ID = prefix("item_used");
	public static final ItemUsedTrigger INSTANCE = new ItemUsedTrigger();

	public void trigger(ServerPlayer player, ItemStack stack, int count) {
		this.trigger(player, instance -> instance.matches(stack, count));
	}

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, ItemPredicate item,
			MinMaxBounds.Ints count) implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ItemPredicate.CODEC.fieldOf("item").forGetter(TriggerInstance::item),
				MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(TriggerInstance::count)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> itemUsed(ItemPredicate item, MinMaxBounds.Ints count) {
			return INSTANCE.createCriterion(new TriggerInstance(Optional.empty(), item, count));
		}

		public boolean matches(ItemStack stack, int count) {
			return this.item.test(stack) && this.count.matches(count);
		}
	}
}
