package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 全能指环和精灵王指环。遗物饰品。佩戴时，背包里第一个饰品盒中的饰品像被佩戴着一样起作用。
 * ALL FOR ONE and Ring of Elven King. Relic accessories. While one is worn, the accessories in the first Bauble Box of the inventory work as if they are worn.
 *
 * <p>全能指环使用饰品盒的全部 24 格。精灵王指环只使用前 3 格。
 * <p>The ALL FOR ONE ring uses all 24 slots of the box. The Ring of Elven King only uses the first 3 slots.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemAFORing} 和 {@code ItemElvenKingRing}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemAFORing} and {@code ItemElvenKingRing}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版只调用盒中饰品的"佩戴 tick"。在 1.12 这已经覆盖了大多数饰品，因为它们的效果写在佩戴 tick 里。
 * 植物魔法 1.21.1 的饰品大多通过属性修饰符和装备查找起作用，所以这里还模拟这两样，否则戒指对大多数饰品没有效果。
 * The original only calls the "worn tick" of the accessories in the box. In 1.12 that covers most accessories, because their effects are in the worn tick.
 * Most accessories of Botania 1.21.1 work through attribute modifiers and equipment lookups, so this port also simulates these two. If it did not, the ring would have no effect for most accessories.</li>
 * <li>和已经佩戴的饰品同类的饰品不模拟，同类的饰品只模拟一个。原版没有这个限制，所以同一个效果可以叠加。
 * An accessory of the same type as a worn accessory is not simulated, and only one accessory of each type is simulated. The original has no such limit, so one effect can apply more than one time.</li>
 * </ul>
 *
 * <p>性能：模拟状态只在饰品盒的内容、佩戴的饰品或者戒指的格数改变时重建，另外每 100 tick 强制重建一次。
 * 没有佩戴戒指的玩家在装备查找时立刻返回。
 * <p>Performance: the simulation state is only built again when the contents of the box, the worn accessories or the slot count of the ring change, plus one forced rebuild on each 100th tick.
 * For a player that wears no ring, an equipment lookup returns immediately.
 */
public class AllForOneRingItem extends RelicBaubleItem {
	public static final int ALL_SLOTS = BaubleBoxItem.SIZE;
	public static final int ELVEN_KING_SLOTS = 3;

	private static final int FORCED_REBUILD_TICKS = 100;
	private static final ResourceLocation BOX_SLOT = prefix("bauble_box");
	private static final Map<Player, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

	private final int slots;

	/**
	 * @param slots 这枚戒指使用饰品盒的前几格。 The number of slots of the box that this ring uses, counted from the first slot.
	 */
	public AllForOneRingItem(int slots, Properties props) {
		super(props);
		this.slots = slots;
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, ResourceLocation.parse("botania:main/gaia_guardian_kill"));
	}

	/**
	 * 一名玩家的模拟状态。
	 * The simulation state of one player.
	 */
	private static final class State {
		long lastTick = Long.MIN_VALUE;
		long builtAt = Long.MIN_VALUE;
		int slots;
		ItemStack boxStack = ItemStack.EMPTY;
		@Nullable
		ItemContainerContents boxContents;
		int wornSignature;
		@Nullable
		SimpleContainer inventory;
		int size;
		int[] writtenHashes = new int[0];
		Map<Item, ItemStack> simulated = Map.of();
		Map<ResourceLocation, Holder<Attribute>> appliedModifiers = Map.of();
	}

	private static ItemStack findBoxStack(Player player) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack candidate = inventory.getItem(i);
			if (candidate.getItem() instanceof BaubleBoxItem) {
				return candidate;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * 判断盒中的这个物品能否被模拟：它必须是饰品，不能是这种戒指自己，并且遗物必须属于这名玩家。
	 * Finds if this item in the box can be simulated: it must be an accessory, it cannot be a ring of this kind, and a relic must belong to this player.
	 */
	private static boolean canSimulate(ItemStack stack, Player player) {
		if (!(stack.getItem() instanceof BaubleItem) || stack.getItem() instanceof AllForOneRingItem) {
			return false;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		return relic == null || relic.isRightPlayer(player);
	}

	private static boolean isOwner(ItemStack stack, Player player) {
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	private static int wornSignature(@Nullable Container worn) {
		int hash = 1;
		if (worn != null) {
			for (int i = 0; i < worn.getContainerSize(); i++) {
				hash = 31 * hash + System.identityHashCode(worn.getItem(i).getItem());
			}
		}
		return hash;
	}

	private static int contentHash(ItemStack stack) {
		return 31 * ItemStack.hashItemAndComponents(stack) + stack.getCount();
	}

	/**
	 * 在被模拟的饰品里查找。植物魔法的装备查找没有找到佩戴的饰品时，由混入类调用这个方法。
	 * Searches the simulated accessories. The mixin calls this when an equipment lookup of Botania finds no worn accessory.
	 */
	public static ItemStack find(Predicate<ItemStack> predicate, LivingEntity living) {
		if (!(living instanceof Player player)) {
			return ItemStack.EMPTY;
		}
		State state = STATES.get(player);
		if (state == null || state.inventory == null) {
			return ItemStack.EMPTY;
		}
		// 状态超过 1 tick 没有更新，说明戒指已经被取下。
		// A state with no update for more than 1 tick shows that the ring was taken off.
		if (player.level().getGameTime() - state.lastTick > 1) {
			return ItemStack.EMPTY;
		}
		for (int i = 0; i < state.size; i++) {
			ItemStack stack = state.inventory.getItem(i);
			if (canSimulate(stack, player) && predicate.test(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity living) {
		if (!super.canEquip(stack, living)) {
			return false;
		}
		// 一名玩家只能佩戴一枚这种戒指。
		// One player can only wear one ring of this kind.
		Container worn = EquipmentHandler.getAllWorn(living);
		if (worn != null) {
			for (int i = 0; i < worn.getContainerSize(); i++) {
				if (worn.getItem(i).getItem() instanceof AllForOneRingItem) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		super.onWornTick(stack, living);
		if (!(living instanceof Player player) || !isOwner(stack, player)) {
			return;
		}
		State state = STATES.computeIfAbsent(player, ignored -> new State());
		long now = player.level().getGameTime();
		if (state.lastTick == now) {
			return;
		}
		state.lastTick = now;

		boolean server = !player.level().isClientSide();
		Container worn = EquipmentHandler.getAllWorn(player);
		ItemStack boxStack = findBoxStack(player);
		ItemContainerContents boxContents = boxStack.isEmpty() ? null : boxStack.get(DataComponents.CONTAINER);
		int signature = wornSignature(worn);
		boolean stale = state.slots != slots
				|| state.boxStack != boxStack
				|| state.boxContents != boxContents
				|| state.wornSignature != signature
				|| state.builtAt == Long.MIN_VALUE
				|| now - state.builtAt >= FORCED_REBUILD_TICKS
				|| now < state.builtAt;
		if (stale) {
			rebuild(state, player, worn, boxStack, boxContents, signature, now, server);
		}

		state.simulated.forEach((item, simulatedStack) -> ((BaubleItem) item).onWornTick(simulatedStack, player));
		if (server && state.inventory != null) {
			writeBackIfChanged(state, boxStack);
		}
	}

	private void rebuild(State state, Player player, @Nullable Container worn, ItemStack boxStack,
			@Nullable ItemContainerContents boxContents, int signature, long now, boolean server) {
		SimpleContainer inventory = boxStack.isEmpty() ? null : BaubleBoxItem.getInventory(boxStack);
		int size = inventory == null ? 0 : Math.min(slots, inventory.getContainerSize());

		Set<Item> wornItems = new HashSet<>();
		if (worn != null) {
			for (int i = 0; i < worn.getContainerSize(); i++) {
				wornItems.add(worn.getItem(i).getItem());
			}
		}
		Map<Item, ItemStack> simulated = new LinkedHashMap<>();
		for (int i = 0; i < size; i++) {
			ItemStack candidate = inventory.getItem(i);
			if (canSimulate(candidate, player) && !wornItems.contains(candidate.getItem())) {
				simulated.putIfAbsent(candidate.getItem(), candidate);
			}
		}

		if (server) {
			Map<Item, ItemStack> previous = state.simulated;
			previous.forEach((item, oldStack) -> {
				if (!simulated.containsKey(item)) {
					((BaubleItem) item).onUnequipped(oldStack, player);
				}
			});
			simulated.forEach((item, newStack) -> {
				if (!previous.containsKey(item)) {
					((BaubleItem) item).onEquipped(newStack, player);
				}
			});
			removeModifiers(state, player);
			state.appliedModifiers = applyModifiers(simulated, player);
		}

		state.simulated = simulated;
		state.inventory = inventory;
		state.size = size;
		state.slots = slots;
		state.boxStack = boxStack;
		state.boxContents = boxContents;
		state.wornSignature = signature;
		state.builtAt = now;
		state.writtenHashes = new int[size];
		for (int i = 0; i < size; i++) {
			state.writtenHashes[i] = contentHash(inventory.getItem(i));
		}
	}

	private static void removeModifiers(State state, Player player) {
		state.appliedModifiers.forEach((id, attribute) -> {
			AttributeInstance instance = player.getAttribute(attribute);
			if (instance != null) {
				instance.removeModifier(id);
			}
		});
	}

	/**
	 * 把被模拟的饰品的属性修饰符加到玩家身上。每个修饰符用一个新的、带有来源物品的编号，这样它不会和真正佩戴的饰品冲突。
	 * Adds the attribute modifiers of the simulated accessories to the player. Each modifier gets a new ID that contains the source item, so that it does not conflict with an accessory that is really worn.
	 */
	private static Map<ResourceLocation, Holder<Attribute>> applyModifiers(Map<Item, ItemStack> simulated, Player player) {
		Map<ResourceLocation, Holder<Attribute>> applied = new HashMap<>();
		simulated.forEach((item, stack) -> ((BaubleItem) item).getEquippedAttributeModifiers(stack, BOX_SLOT).forEach((attribute, modifier) -> {
			ResourceLocation id = prefix(("box_sim/" + BuiltInRegistries.ITEM.getKey(item) + "/" + modifier.id()).replace(':', '.'));
			AttributeInstance instance = player.getAttribute(attribute);
			if (instance != null && !applied.containsKey(id)) {
				if (!instance.hasModifier(id)) {
					instance.addTransientModifier(new AttributeModifier(id, modifier.amount(), modifier.operation()));
				}
				applied.put(id, attribute);
			}
		}));
		return applied;
	}

	/**
	 * 把被模拟的饰品发生的改变（例如被取走的魔力）写回饰品盒。
	 * Writes the changes of the simulated accessories (for example mana that was taken) back to the box.
	 */
	private static void writeBackIfChanged(State state, ItemStack boxStack) {
		boolean dirty = false;
		for (int i = 0; i < state.size; i++) {
			int hash = contentHash(state.inventory.getItem(i));
			if (hash != state.writtenHashes[i]) {
				state.writtenHashes[i] = hash;
				dirty = true;
			}
		}
		if (!dirty) {
			return;
		}
		state.inventory.setChanged();
		// 这次写入换掉了饰品盒的容器组件。记住新的组件，这样下一 tick 不会把它当成外部的改动。
		// This write replaces the container component of the box. The new component is recorded, so that the next tick does not see it as a change from outside.
		state.boxContents = boxStack.get(DataComponents.CONTAINER);
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity living) {
		super.onUnequipped(stack, living);
		if (!(living instanceof Player player)) {
			return;
		}
		State state = STATES.remove(player);
		if (state == null || player.level().isClientSide()) {
			return;
		}
		state.simulated.forEach((item, simulatedStack) -> ((BaubleItem) item).onUnequipped(simulatedStack, player));
		removeModifiers(state, player);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, context, tooltip, flags);
	}
}
