package io.github.lounode.extrabotany.client;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.network.TriConsumer;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.OrichalcosHammer;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.TerrasteelHammerItem;
import io.github.lounode.extrabotany.common.item.relic.AchillesShieldItem;
import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;
import io.github.lounode.extrabotany.common.item.relic.FailnaughtItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Failnaught;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyItemProperties {
	public static void init(TriConsumer<ItemLike, ResourceLocation, ClampedItemPropertyFunction> consumer) {
		// [BotaniaCopy] ItemProperties.BOW's minecraft:pulling property
		ClampedItemPropertyFunction pulling = (stack, worldIn, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
		ClampedItemPropertyFunction pull = (stack, worldIn, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			} else {
				FailnaughtItem item = ((FailnaughtItem) stack.getItem());
				return entity.getUseItem() != stack
						? 0.0F
						: (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) * item.chargeVelocityMultiplier(stack, entity) / 20.0F;
			}
		};
		consumer.accept(ExtraBotanyItems.failnaught, ResourceLocation.tryParse("pulling"), pulling);
		for (var candy : java.util.List.of(ExtraBotanyItems.candyBag, ExtraBotanyItems.candyEins,
				ExtraBotanyItems.candyZwei, ExtraBotanyItems.candyDrei)) {
			consumer.accept(candy, prefix("seasonal_christmas"), (stack, level, entity, seed) ->
					io.github.lounode.extrabotany.common.item.SeasonalItemHelper.isChristmas() ? 1.0F : 0.0F);
		}
		consumer.accept(ExtraBotanyItems.failnaught, ResourceLocation.tryParse("pull"), pull);

		ClampedItemPropertyFunction pulling2 = (stack, worldIn, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
		ClampedItemPropertyFunction pull2 = (stack, worldIn, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			} else {

				var variant = VoidArchivesItem.getVariant(stack);
				if (!(variant instanceof Failnaught failnaught)) {
					return 0.0F;
				}

				return entity.getUseItem() != stack
						? 0.0F
						: (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) * failnaught.chargeVelocityMultiplier(stack, entity) / 20.0F;
			}
		};

		consumer.accept(ExtraBotanyItems.voidArchives, ResourceLocation.tryParse("pulling"), pulling2);
		consumer.accept(ExtraBotanyItems.voidArchives, ResourceLocation.tryParse("pull"), pull2);

		ClampedItemPropertyFunction brewGetter = (stack, world, entity, seed) -> {
			BaseBrewItem item = ((BaseBrewItem) stack.getItem());
			if (item.getBrew(stack) == ExtraBotanyBrews.manaCocktail && item.getSwigsLeft(stack) == 1) {
				return 0;
			}

			return (float) (0.01D * (item.getSwigs(stack) - item.getSwigsLeft(stack)));
		};

		ClampedItemPropertyFunction brewGetter2 = (stack, world, entity, seed) -> {
			BaseBrewItem item = ((BaseBrewItem) stack.getItem());

			return (float) (0.01D * (item.getSwigs(stack) - item.getSwigsLeft(stack)));
		};

		consumer.accept(ExtraBotanyItems.manaCocktail, prefix("swigs_taken"), brewGetter);
		consumer.accept(ExtraBotanyItems.infiniteWine, prefix("swigs_taken"), brewGetter2);

		ClampedItemPropertyFunction voidArchivesVariantGetter = (stack, world, entity, seed) -> {
			int variantIndex = VoidArchivesItem.getVariantIndex(stack);
			return (float) (0.01D * variantIndex);
		};

		consumer.accept(ExtraBotanyItems.voidArchives, prefix("variant"), voidArchivesVariantGetter);

		consumer.accept(ExtraBotanyItems.terrasteelHammer, prefix("active"),
				(stack, world, entity, seed) -> TerrasteelHammerItem.isEnabled(stack) ? 1 : 0);
		consumer.accept(ExtraBotanyItems.orichalcosHammer, prefix("active"),
				(stack, world, entity, seed) -> OrichalcosHammer.isEnabled(stack) ? 1 : 0);
		consumer.accept(ExtraBotanyItems.rheinHammer, prefix("active"),
				(stack, world, entity, seed) -> RheinHammerItem.isEnabled(stack) ? 1 : 0);
		consumer.accept(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit, prefix("senketsu"),
				(stack, world, entity, seed) -> SanguinePleiadesCombatMaidSuitItem.isSenketsu(stack) ? 1 : 0);
		consumer.accept(ExtraBotanyItems.excalibur, prefix("saber"),
				(stack, world, entity, seed) -> ExcaliburItem.isSaber(stack) ? 1 : 0);
		consumer.accept(ExtraBotanyItems.manasteelShield, prefix("blocking"),
				(stack, world, entity, seed) -> (entity == null || !entity.isUsingItem()) ? 0 : 1);
		consumer.accept(ExtraBotanyItems.elementiumShield, prefix("blocking"),
				(stack, world, entity, seed) -> (entity == null || !entity.isUsingItem()) ? 0 : 1);
		consumer.accept(ExtraBotanyItems.terrasteelShield, prefix("blocking"),
				(stack, world, entity, seed) -> (entity == null || !entity.isUsingItem()) ? 0 : 1);
		consumer.accept(ExtraBotanyItems.achillesShield, prefix("blocking"),
				(stack, world, entity, seed) -> {
					if (entity == null) {
						return 0;
					}
					if (AchillesShieldItem.isReleased(stack)) {
						return 0;
					}
					return entity.isUsingItem() ? 1 : 0;
				});
		consumer.accept(ExtraBotanyItems.achillesShield, prefix("released"),
				(stack, world, entity, seed) -> {
					if (entity == null) {
						return 0;
					}
					return AchillesShieldItem.isReleased(stack) ? 1 : 0;
				});
	}
}
