package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.FrostWalkerHelper;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 静籁永恒。遗物饰品。
 * Silent Eternity. A relic accessory.
 *
 * <ul>
 * <li>佩戴者不能被任何方式治疗。
 * The wearer cannot be healed in any way.</li>
 * <li>佩戴者站着不动超过 15 tick 后，每 tick 直接恢复 0.4 点生命值，并且得到"永恒"效果，免疫全部伤害。
 * After the wearer stands still for more than 15 ticks, 0.4 health comes back directly on each tick, and the wearer gets the Eternity effect, which gives immunity to all damage.</li>
 * <li>它是一个永远有 666 魔力的魔力源，可以给其他物品供应魔力。
 * It is a mana source that always has 666 mana, and it can give mana to other items.</li>
 * <li>击退抗性 +1。脚下 4 格内的水和熔岩会结冰。
 * +1 knockback resistance. Water and lava within 4 blocks below the feet freeze.</li>
 * </ul>
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemSilentEternity}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemSilentEternity}.
 *
 * <p>和原版的差异：原版每 tick 把玩家的坐标和静止计数写进物品的 NBT。这里把它们放在内存里，因为每 tick 改写物品数据会让饰品栏每 tick 向客户端同步一次。
 * <p>Difference from the original: the original writes the position of the player and the stillness counter into the NBT of the item on each tick. This port keeps them in memory, because a change of the item data on each tick makes the accessory slot synchronize to the client on each tick.
 */
public class SilentEternityItem extends RelicBaubleItem {
	private static final int FREEZE_RADIUS = 4;
	private static final int STILL_TICKS_BEFORE_REST = 15;
	private static final float REST_HEAL = 0.4F;
	private static final int REST_EFFECT_TICKS = 10;
	private static final double STILL_DISTANCE_SQR = 1.0E-4;

	// 玩家对象被回收后，对应的记录自动消失。
	// A record goes away automatically after its player object is garbage-collected.
	private static final Map<Player, Stillness> STILLNESS = Collections.synchronizedMap(new WeakHashMap<>());

	public SilentEternityItem(Properties props) {
		super(props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	/**
	 * 阻止佩戴者受到治疗。
	 * Stops healing of the wearer.
	 */
	public static void onHeal(LivingHealEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		ItemStack stack = EquipmentHandler.findOrEmpty(ExtraBotanyItems.silentEternity, player);
		if (!stack.isEmpty() && isOwner(stack, player)) {
			event.setAmount(0F);
		}
	}

	private static boolean isOwner(ItemStack stack, Player player) {
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return super.canEquip(stack, entity) && EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(prefix("silent_eternity_knockback_resistance"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || !isOwner(stack, player)) {
			return;
		}
		new Reservoir(stack).addMana(Reservoir.MAX_MANA);
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		FrostWalkerHelper.freeze(stack, serverPlayer, serverPlayer.serverLevel(), FREEZE_RADIUS);
		// 结冰的逻辑只对站在地面上的生物生效。原版在调用前后临时把玩家标记为"在地面上"。
		// The freeze logic only works for a mob that is on the ground. The original marks the player as "on the ground" for the time of the call.
		boolean wasOnGround = player.onGround();
		player.setOnGround(true);
		try {
			FrostRingItem.freezeLava(player, player.level(), player.blockPosition(), FREEZE_RADIUS);
		} finally {
			player.setOnGround(wasOnGround);
		}

		Stillness stillness = STILLNESS.computeIfAbsent(player, ignored -> new Stillness());
		if (stillness.update(player) > STILL_TICKS_BEFORE_REST) {
			// 直接设置生命值，这样不会被自己的"禁止治疗"拦住。
			// Set the health directly, so that the "no healing" rule of this item does not stop it.
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + REST_HEAL));
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, REST_EFFECT_TICKS));
		}
	}

	/**
	 * 记录玩家上一 tick 的位置和已经静止的 tick 数。
	 * Keeps the position of the player on the last tick and the number of ticks with no movement.
	 */
	private static final class Stillness {
		private double x = Double.NaN;
		private double y;
		private double z;
		private int ticks;

		int update(Player player) {
			double dx = player.getX() - x;
			double dy = player.getY() - y;
			double dz = player.getZ() - z;
			ticks = dx * dx + dy * dy + dz * dz <= STILL_DISTANCE_SQR ? ticks + 1 : 0;
			x = player.getX();
			y = player.getY();
			z = player.getZ();
			return ticks;
		}
	}

	/**
	 * 静籁永恒的魔力接口：容量 666，可以向其他物品供应魔力，不能和魔力池交换魔力。
	 * The mana interface of the Silent Eternity: a capacity of 666, it can give mana to other items, and it cannot exchange mana with a mana pool.
	 */
	public record Reservoir(ItemStack stack) implements ManaItem {
		public static final int MAX_MANA = 666;
		private static final String TAG_MANA = "mana";

		// 这里只读取数据，所以不需要复制整个标签。
		// This only reads the data, so a copy of the full tag is not necessary.
		@SuppressWarnings("deprecation")
		@Override
		public int getMana() {
			int mana = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getInt(TAG_MANA);
			return Math.clamp(mana, 0, MAX_MANA);
		}

		@Override
		public int getMaxMana() {
			return MAX_MANA;
		}

		@Override
		public void addMana(int amount) {
			int mana = (int) Math.clamp((long) getMana() + amount, 0, MAX_MANA);
			// 佩戴时每 tick 都会补满魔力。数值没有变化时不改写物品数据。
			// The mana is filled on each tick while the item is worn. When the value does not change, the item data is not written.
			if (mana == getMana()) {
				return;
			}
			CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
				if (mana == 0) {
					data.remove(TAG_MANA);
				} else {
					data.putInt(TAG_MANA, mana);
				}
			});
		}

		@Override
		public boolean canReceiveManaFromPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean acceptDispatchedManaFromItem(ItemStack other) {
			return false;
		}

		@Override
		public boolean refuseRequestedManaFromItem(ItemStack other) {
			return true;
		}

		@Override
		public boolean canDrainManaToPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canSendRequestedManaToItem(ItemStack other) {
			return true;
		}

		@Override
		public boolean isNoExport() {
			return false;
		}
	}
}
