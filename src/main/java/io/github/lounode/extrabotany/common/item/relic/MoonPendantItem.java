package io.github.lounode.extrabotany.common.item.relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;

import vazkii.botania.api.item.BlockChangedListenerBauble;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.equipment.bauble.NimbusAmuletItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.util.FrostWalkerHelper;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 蚀月之心。遗物吊坠，它合并了多种吊坠的效果。
 * Heart of Corrupted Moon. A relic pendant that has the effects of several pendants.
 *
 * <p>效果：雨云护符的三段跳、绯红吊坠的防火、旁观者之眼的物品搜索、冰霜行者、击退免疫。
 * <p>The effects: the triple jump of the Nimbus Amulet, the fire protection of the Crimson Pendant, the item search of The Spectator, Frost Walker, and knockback immunity.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemMoonPendant}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemMoonPendant}.
 */
public class MoonPendantItem extends NimbusAmuletItem implements BlockChangedListenerBauble {
	private static final int FROST_WALKER_LEVEL = 8;

	public MoonPendantItem(Properties props) {
		super(props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.GAIA_TRIAL));
	}

	/**
	 * 让佩戴者免疫火焰伤害。
	 * Makes the wearer immune to fire damage.
	 *
	 * <p>先检查伤害类型，再查饰品栏。这个事件对所有实体的所有伤害都会触发，所以顺序很重要。
	 * <p>Check the damage type first, then search the bauble slots. This event occurs for all damage to all entities, so the sequence is important.
	 */
	public static void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
		if (event.isInvulnerable() || !event.getSource().is(DamageTypeTags.IS_FIRE)) {
			return;
		}
		if (event.getEntity() instanceof LivingEntity living
				&& !EquipmentHandler.findOrEmpty(ExtraBotanyItems.moonPendant, living).isEmpty()) {
			event.setInvulnerable(true);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && entity instanceof Player player) {
			Relic relic = Relic.LOOKUP.find(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic == null || !(entity instanceof Player player)) {
			return;
		}
		if (!player.level().isClientSide()) {
			relic.tickBinding(player);
		}
		if (!relic.isRightPlayer(player)) {
			return;
		}

		// 雨云护符的多段跳。
		// The multiple jumps of the Nimbus Amulet.
		super.onWornTick(stack, entity);
		((BaubleItem) BotaniaItems.CRIMSON_PENDANT).onWornTick(stack, player);
		((BaubleItem) BotaniaItems.THE_SPECTATOR).onWornTick(stack, player);

		if (player.level().isClientSide() && !player.isShiftKeyDown() && player.getRandom().nextFloat() >= 0.25F) {
			player.level().addParticle(
					new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SNOW_BLOCK.defaultBlockState()),
					player.getX() + player.getRandom().nextFloat() * 0.6 - 0.3,
					player.getY() + 1.1,
					player.getZ() + player.getRandom().nextFloat() * 0.6 - 0.3,
					0, -0.15, 0);
		}
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		super.onUnequipped(stack, entity);
		// 旁观者之眼把高亮数据存在物品上。取下时必须清除。
		// The Spectator keeps the highlight data on the item. The data must be removed on unequip.
		((BaubleItem) BotaniaItems.THE_SPECTATOR).onUnequipped(stack, entity);
	}

	@Override
	public void onChangedBlock(ItemStack stack, LivingEntity entity, ServerLevel level, BlockPos pos) {
		if (entity.isShiftKeyDown() || !(entity instanceof Player player)) {
			return;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null && relic.isRightPlayer(player)) {
			FrostWalkerHelper.freeze(stack, entity, level, FROST_WALKER_LEVEL);
		}
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(slotId, 1, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		Relic relic = Relic.LOOKUP.find(stack);
		return entity instanceof Player player
				&& relic != null
				&& relic.isRightPlayer(player)
				&& EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}
}
