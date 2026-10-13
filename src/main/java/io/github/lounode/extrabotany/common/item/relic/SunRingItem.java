package io.github.lounode.extrabotany.common.item.relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.item.BlockChangedListenerBauble;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 圣阳尊戒。遗物戒指，它合并了七种戒指的效果。
 * Ring of Sacred Sun. A relic ring that has the effects of seven rings.
 *
 * <p>合并的戒指：地幔之戒、高级光环之戒、矫正之戒、脊索之戒、死亡戒指、冰霜之星、弥散指环（原版的"驭魔之戒"）。
 * <p>The rings: Ring of the Mantle, Greater Band of Aura, Ring of Correction, Ring of Chordata, Death Ring, Frost Star, Dispersive Ring (the "Ring of Mana Drive" of the original).
 *
 * <p>它还增加 3.5 格方块交互距离和 25% 精灵生成概率。
 * <p>The ring also adds 3.5 blocks of block interaction range and 25% pixie spawn chance.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemSunRing}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemSunRing}.
 */
public class SunRingItem extends RelicBaubleItem implements BlockChangedListenerBauble {
	private static final double REACH_BONUS = 3.5;
	private static final double PIXIE_CHANCE_BONUS = 0.25;

	public SunRingItem(Properties props) {
		super(props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.GAIA_TRIAL));
	}

	/**
	 * 需要每 tick 运行的戒指。这个列表不能放在静态字段里，因为物品的注册顺序不确定。
	 * The rings that must run on each tick. This list cannot be a static field, because the registration order of the items is not fixed.
	 */
	private static List<BaubleItem> tickingRings() {
		return List.of(
				(BaubleItem) BotaniaItems.RING_OF_THE_MANTLE,
				(BaubleItem) BotaniaItems.GREATER_BAND_OF_AURA,
				(BaubleItem) BotaniaItems.RING_OF_CORRECTION,
				(BaubleItem) BotaniaItems.RING_OF_CHORDATA,
				(BaubleItem) ExtraBotanyItems.deathRing,
				(BaubleItem) ExtraBotanyItems.frostStar,
				(BaubleItem) ExtraBotanyItems.dispersiveRing);
	}

	/**
	 * 在佩戴和取下时有状态的戒指。
	 * The rings that keep a state between equip and unequip.
	 */
	private static List<BaubleItem> statefulRings() {
		return List.of(
				(BaubleItem) BotaniaItems.RING_OF_THE_MANTLE,
				(BaubleItem) BotaniaItems.RING_OF_CHORDATA);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic == null || !(entity instanceof Player player)) {
			return;
		}
		relic.tickBinding(player);
		if (!relic.isRightPlayer(player)) {
			return;
		}
		for (BaubleItem ring : tickingRings()) {
			ring.onWornTick(stack, player);
		}
	}

	@Override
	public void onEquipped(ItemStack stack, LivingEntity entity) {
		super.onEquipped(stack, entity);
		for (BaubleItem ring : statefulRings()) {
			ring.onEquipped(stack, entity);
		}
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		super.onUnequipped(stack, entity);
		for (BaubleItem ring : statefulRings()) {
			ring.onUnequipped(stack, entity);
		}
	}

	@Override
	public void onChangedBlock(ItemStack stack, LivingEntity entity, ServerLevel level, BlockPos pos) {
		// 冰霜之星的结冰效果由换格事件触发，所以这里也要转发。
		// The block change event starts the freeze effect of the Frost Star, so this ring must also pass the event on.
		((BlockChangedListenerBauble) ExtraBotanyItems.frostStar).onChangedBlock(stack, entity, level, pos);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return super.canEquip(stack, entity) && EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		// 原版使用 Forge 的触及距离属性。它只影响方块，不影响实体。
		// The original uses the reach distance attribute of Forge. That attribute only applies to blocks, not to entities.
		attributes.put(Attributes.BLOCK_INTERACTION_RANGE,
				new AttributeModifier(slotId, REACH_BONUS, AttributeModifier.Operation.ADD_VALUE));
		attributes.put(PixieHandler.PIXIE_SPAWN_CHANCE, PixieHandler.makeModifier(slotId, PIXIE_CHANCE_BONUS));
		return attributes;
	}
}
