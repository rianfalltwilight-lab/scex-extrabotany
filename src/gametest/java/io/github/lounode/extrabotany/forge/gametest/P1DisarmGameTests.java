package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.RegisterEvent;

import top.theillusivec4.curios.api.CuriosApi;

import io.github.lounode.extrabotany.common.entity.EgoLandmineEntity;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.handler.EgoRequirementHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.forge.ForgeExtrabotanyConfig;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Regressions for preserving equipment when a forced drop is cancelled. */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P1DisarmGameTests {
	private static final Map<UUID, Integer> FACTORY_CALLS = new HashMap<>();
	private static Item customDropItem;
	private static Item ordinaryDropItem;
	private static Item nullDropItem;

	private P1DisarmGameTests() {}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void egoMineDropsTheWholeStack(GameTestHelper helper) {
		exerciseMine(helper, true, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void egoMineCancelledJoinKeepsOriginal(GameTestHelper helper) {
		exerciseMine(helper, true, true);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void skullMineDropsTheWholeStack(GameTestHelper helper) {
		exerciseMine(helper, false, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void skullMineCancelledJoinKeepsOriginal(GameTestHelper helper) {
		exerciseMine(helper, false, true);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void requirementDropsEquipmentAndCurioIntact(GameTestHelper helper) {
		exerciseRequirement(helper, false, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void requirementCancelledJoinKeepsEquipmentAndCurio(GameTestHelper helper) {
		exerciseRequirement(helper, true, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void requirementCancelledTossKeepsEquipmentAndCurio(GameTestHelper helper) {
		exerciseRequirement(helper, false, true);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void customMineDropCommitsOnceAcrossTicks(GameTestHelper helper) {
		exerciseCustomDrop(helper, customDropItem, false, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cancelledCustomMineDropKeepsOriginalAcrossTicks(GameTestHelper helper) {
		exerciseCustomDrop(helper, customDropItem, true, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void tossReplacementUsesActualCustomEntity(GameTestHelper helper) {
		exerciseCustomDrop(helper, customDropItem, false, true);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void nullCustomFactoryFallsBackOnlyOnce(GameTestHelper helper) {
		exerciseCustomDrop(helper, nullDropItem, false, false);
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void ordinaryCustomFactoryIsNotAppliedAgain(GameTestHelper helper) {
		exerciseCustomDrop(helper, ordinaryDropItem, false, false);
	}

	// This source set is not part of the published mod. Current Botania 1.21.1
	// uses ItemEntity mixins and provides no hasCustomEntity/createEntity item.
	@EventBusSubscriber(modid = "extrabotany", bus = EventBusSubscriber.Bus.MOD)
	public static final class TestItems {
		@SubscribeEvent
		public static void registerItems(RegisterEvent event) {
			event.register(Registries.ITEM, registry -> {
				customDropItem = new FactoryItem(FactoryResult.CUSTOM);
				ordinaryDropItem = new FactoryItem(FactoryResult.ORDINARY);
				nullDropItem = new FactoryItem(FactoryResult.NULL);
				registry.register(ResourceLocation.fromNamespaceAndPath("extrabotany", "p1_test_custom_drop"), customDropItem);
				registry.register(ResourceLocation.fromNamespaceAndPath("extrabotany", "p1_test_ordinary_drop"), ordinaryDropItem);
				registry.register(ResourceLocation.fromNamespaceAndPath("extrabotany", "p1_test_null_drop"), nullDropItem);
			});
		}
	}

	private enum FactoryResult { CUSTOM, ORDINARY, NULL }

	private static final class FactoryItem extends Item {
		private final FactoryResult result;

		private FactoryItem(FactoryResult result) {
			super(new Item.Properties());
			this.result = result;
		}

		@Override
		public boolean hasCustomEntity(ItemStack stack) {
			return true;
		}

		@Override
		public Entity createEntity(Level level, Entity location, ItemStack stack) {
			UUID marker = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getUUID("p1_disarm");
			FACTORY_CALLS.merge(marker, 1, Integer::sum);
			if (result == FactoryResult.NULL) {
				return null;
			}
			ItemEntity resultEntity = result == FactoryResult.CUSTOM
					? new CustomDropEntity(level, stack) : new ItemEntity(level, location.getX(), location.getY(), location.getZ(), stack);
			resultEntity.restoreFrom(location);
			return resultEntity;
		}
	}

	private static final class CustomDropEntity extends ItemEntity {
		private CustomDropEntity(Level level, ItemStack stack) {
			super(level, 0, 0, 0, stack);
		}
	}

	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(new BlockPos(2, 3, 2)).getCenter());
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
		player.setHealth(200);
		helper.assertTrue(helper.getLevel().addFreshEntity(player), "Could not add the victim to the level");
		return player;
	}

	private static ItemStack marked(Player player, ItemStack stack, String slot) {
		CompoundTag data = new CompoundTag();
		data.putUUID("p1_disarm", player.getUUID());
		data.putString("slot", slot);
		data.putIntArray("payload", new int[] { 3, 7, 19 });
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("preserve-" + slot));
		var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null) {
			relic.bindToUUID(player.getUUID());
		}
		if (stack.isDamageableItem()) {
			stack.setDamageValue(13);
			stack.enchant(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
					.getOrThrow(Enchantments.SHARPNESS), 3);
		}
		return stack;
	}

	private static void assertBoundWeapon(GameTestHelper helper, Player player, ItemStack stack) {
		var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		helper.assertTrue(relic != null && player.getUUID().equals(relic.getSoulbindUUID()), "Weapon fixture lacks actual relic binding");
		helper.assertTrue(stack.isDamageableItem() && stack.getDamageValue() == 13, "Weapon fixture lacks durability damage");
		helper.assertTrue(stack.getEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.SHARPNESS)) == 3, "Weapon fixture lacks actual Sharpness III");
	}

	private static boolean belongsTo(ItemEntity item, Player player) {
		CompoundTag data = item.getItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return data.hasUUID("p1_disarm") && data.getUUID("p1_disarm").equals(player.getUUID());
	}

	private static List<ItemEntity> drops(GameTestHelper helper, Player player) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3),
				item -> belongsTo(item, player));
	}

	private static void changeCopy(ItemEntity item) {
		item.getItem().setCount(1);
		item.getItem().set(DataComponents.CUSTOM_NAME, Component.literal("cancelled-copy"));
		item.getItem().remove(DataComponents.ENCHANTMENTS);
		var relic = EXplatAbstractions.INSTANCE.findRelic(item.getItem());
		if (relic != null) {
			relic.bindToUUID(UUID.randomUUID());
		}
	}

	private static void assertDrops(GameTestHelper helper, Player player, List<ItemStack> expected) {
		List<ItemEntity> drops = drops(helper, player);
		helper.assertTrue(drops.size() == expected.size(), "Wrong number of dropped entities: " + drops.size());
		for (ItemStack stack : expected) {
			helper.assertTrue(drops.stream().filter(item -> ItemStack.matches(item.getItem(), stack)).count() == 1,
					"Dropped stack count or components changed: " + stack);
		}
		helper.assertTrue(drops.stream().allMatch(ItemEntity::hasPickUpDelay), "Drop has no pickup delay");
	}

	private static void exerciseMine(GameTestHelper helper, boolean ego, boolean cancelJoin) {
		helper.assertTrue(!ExtraBotanyConfig.common().disableGaiaDisArm(), "Mine fixture requires disarm enabled");
		Player player = player(helper);
		ItemStack original = marked(player, ego ? new ItemStack(Items.DIAMOND, 7) : new ItemStack(ExtraBotanyItems.excalibur), "main");
		if (!ego) {
			assertBoundWeapon(helper, player, original);
		}
		ItemStack expected = original.copy();
		player.setItemSlot(EquipmentSlot.MAINHAND, original);
		AtomicInteger cancelled = new AtomicInteger();
		Consumer<EntityJoinLevelEvent> cancel = event -> {
			if (cancelJoin && event.getEntity() instanceof ItemEntity item && belongsTo(item, player)) {
				changeCopy(item);
				event.setCanceled(true);
				cancelled.incrementAndGet();
			}
		};
		NeoForge.EVENT_BUS.addListener(cancel);
		try {
			if (ego) {
				EgoLandmineEntity mine = new EgoLandmineEntity(helper.getLevel(), null, EgoLandmineEntity.TYPE_GREEN, player.position());
				helper.getLevel().addFreshEntity(mine);
				mine.tickCount = 50;
				mine.tick();
				helper.assertTrue(mine.isRemoved(), "Ego mine did not explode");
			} else {
				SkullLandMineEntity mine = new SkullLandMineEntity.Disarm(helper.getLevel(), null);
				mine.setPos(player.position());
				helper.getLevel().addFreshEntity(mine);
				mine.explode();
				helper.assertTrue(mine.isRemoved(), "Skull mine did not explode");
			}
			if (cancelJoin) {
				helper.assertTrue(cancelled.get() == 1, "Did not cancel exactly one attempted drop");
				helper.assertTrue(player.getMainHandItem() == original && ItemStack.matches(original, expected),
						"Cancelled mine drop removed or mutated the original");
				assertDrops(helper, player, List.of());
			} else {
				helper.assertTrue(player.getMainHandItem().isEmpty(), "Successful mine drop left the original equipped");
				assertDrops(helper, player, List.of(expected));
			}
		} finally {
			NeoForge.EVENT_BUS.unregister(cancel);
			drops(helper, player).forEach(Entity::discard);
			player.discard();
		}
		helper.succeed();
	}

	private static void exerciseCustomDrop(GameTestHelper helper, Item targetItem, boolean cancelJoin, boolean replaceOnToss) {
		helper.assertTrue(targetItem != null, "Custom drop fixture was not registered");
		helper.assertTrue(!ExtraBotanyConfig.common().disableGaiaDisArm(), "Mine fixture requires disarm enabled");
		Player player = player(helper);
		ItemStack original = marked(player, new ItemStack(replaceOnToss ? ExtraBotanyItems.excalibur : targetItem), "custom");
		ItemStack before = original.copy();
		ItemStack expectedDrop = replaceOnToss ? original.transmuteCopy(targetItem) : original.copy();
		player.setItemSlot(EquipmentSlot.MAINHAND, original);
		AtomicInteger joins = new AtomicInteger();
		AtomicInteger tosses = new AtomicInteger();
		Consumer<EntityJoinLevelEvent> join = event -> {
			if (event.getEntity() instanceof ItemEntity item && belongsTo(item, player)) {
				joins.incrementAndGet();
				if (cancelJoin && item instanceof CustomDropEntity) {
					changeCopy(item);
					event.setCanceled(true);
				}
			}
		};
		Consumer<ItemTossEvent> toss = event -> {
			if (replaceOnToss && event.getPlayer() == player) {
				tosses.incrementAndGet();
				event.getEntity().setItem(event.getEntity().getItem().transmuteCopy(targetItem));
			}
		};
		boolean beforeConfig = ExtraBotanyConfig.common().relicsNeedEgoDefeat();
		NeoForge.EVENT_BUS.addListener(join);
		NeoForge.EVENT_BUS.addListener(toss);
		try {
			if (replaceOnToss) {
				ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(true);
				EgoRequirementHandler.tickPlayer(player);
			} else {
				SkullLandMineEntity mine = new SkullLandMineEntity.Disarm(helper.getLevel(), null);
				mine.setPos(player.position());
				mine.explode();
			}
			helper.assertTrue(joins.get() == 1 && tosses.get() == (replaceOnToss ? 1 : 0), "Did not join the final entity exactly once");
			assertCustomDropResult(helper, player, original, before, expectedDrop, targetItem, cancelJoin);
		} catch (RuntimeException | Error failure) {
			cleanupCustomDrop(helper, player);
			throw failure;
		} finally {
			NeoForge.EVENT_BUS.unregister(join);
			NeoForge.EVENT_BUS.unregister(toss);
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(beforeConfig);
		}
		long started = helper.getLevel().getGameTime();
		// Listeners are already removed: any deferred framework replacement can
		// now enter the world, so an immediate-only assertion cannot hide a copy.
		helper.runAfterDelay(2, () -> {
			try {
				helper.assertTrue(helper.getLevel().getGameTime() > started, "No server tick elapsed");
				assertCustomDropResult(helper, player, original, before, expectedDrop, targetItem, cancelJoin);
			} finally {
				cleanupCustomDrop(helper, player);
			}
			helper.succeed();
		});
	}

	private static void assertCustomDropResult(GameTestHelper helper, Player player, ItemStack original,
			ItemStack before, ItemStack expected, Item targetItem, boolean cancelled) {
		helper.assertTrue(FACTORY_CALLS.getOrDefault(player.getUUID(), 0) == 1, "Custom factory was skipped or invoked more than once");
		if (cancelled) {
			helper.assertTrue(player.getMainHandItem() == original && ItemStack.matches(original, before), "Cancelled final entity changed original slot");
			assertDrops(helper, player, List.of());
		} else {
			helper.assertTrue(player.getMainHandItem().isEmpty(), "Successful final entity left the original equipped");
			assertDrops(helper, player, List.of(expected));
			if (targetItem == customDropItem) {
				helper.assertTrue(drops(helper, player).getFirst() instanceof CustomDropEntity, "Custom entity type was not preserved");
			}
		}
	}

	private static void cleanupCustomDrop(GameTestHelper helper, Player player) {
		drops(helper, player).forEach(Entity::discard);
		player.discard();
		FACTORY_CALLS.remove(player.getUUID());
	}

	private static void exerciseRequirement(GameTestHelper helper, boolean cancelJoin, boolean cancelToss) {
		boolean before = ExtraBotanyConfig.common().relicsNeedEgoDefeat();
		Player player = player(helper);
		ItemStack hand = marked(player, new ItemStack(ExtraBotanyItems.excalibur), "hand");
		ItemStack head = marked(player, new ItemStack(ExtraBotanyItems.camera), "head");
		ItemStack curio = marked(player, new ItemStack(ExtraBotanyItems.sunRing), "curio");
		assertBoundWeapon(helper, player, hand);
		List<ItemStack> expected = List.of(hand.copy(), head.copy(), curio.copy());
		for (ItemStack stack : expected) {
			var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
			helper.assertTrue(relic != null && player.getUUID().equals(relic.getSoulbindUUID()), "Restricted fixture lacks actual relic binding");
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, hand);
		player.setItemSlot(EquipmentSlot.HEAD, head);
		var curios = CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values().stream()
				.filter(handler -> handler.getStacks().getSlots() > 0).findFirst().orElseThrow().getStacks();
		curios.setStackInSlot(0, curio);
		AtomicInteger joined = new AtomicInteger();
		AtomicInteger tossed = new AtomicInteger();
		Consumer<EntityJoinLevelEvent> join = event -> {
			if (event.getEntity() instanceof ItemEntity item && belongsTo(item, player)) {
				joined.incrementAndGet();
				if (cancelJoin) {
					changeCopy(item);
					event.setCanceled(true);
				}
			}
		};
		Consumer<ItemTossEvent> toss = event -> {
			if (event.getPlayer() == player && belongsTo(event.getEntity(), player)) {
				tossed.incrementAndGet();
				if (cancelToss) {
					changeCopy(event.getEntity());
					event.setCanceled(true);
				}
			}
		};
		NeoForge.EVENT_BUS.addListener(join);
		NeoForge.EVENT_BUS.addListener(toss);
		try {
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(true);
			EgoRequirementHandler.tickPlayer(player);
			helper.assertTrue(tossed.get() == 3, "Each restricted slot must retain ItemTossEvent semantics");
			helper.assertTrue(joined.get() == (cancelToss ? 0 : 3), "Unexpected number of entity join attempts");
			if (cancelJoin || cancelToss) {
				helper.assertTrue(player.getMainHandItem() == hand && ItemStack.matches(hand, expected.get(0)), "Cancelled drop lost or mutated main hand");
				helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD) == head && ItemStack.matches(head, expected.get(1)), "Cancelled drop lost or mutated head slot");
				helper.assertTrue(curios.getStackInSlot(0) == curio && ItemStack.matches(curio, expected.get(2)), "Cancelled drop lost or mutated Curios slot");
				assertDrops(helper, player, List.of());
			} else {
				helper.assertTrue(player.getMainHandItem().isEmpty() && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
						&& curios.getStackInSlot(0).isEmpty(), "Successfully dropped equipment remains equipped");
				assertDrops(helper, player, expected);
				EgoRequirementHandler.tickPlayer(player);
				assertDrops(helper, player, expected);
				helper.assertTrue(tossed.get() == 3 && joined.get() == 3, "Repeated tick attempted duplicate drops");
			}
		} finally {
			NeoForge.EVENT_BUS.unregister(join);
			NeoForge.EVENT_BUS.unregister(toss);
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(before);
			drops(helper, player).forEach(Entity::discard);
			player.discard();
		}
		helper.succeed();
	}
}
