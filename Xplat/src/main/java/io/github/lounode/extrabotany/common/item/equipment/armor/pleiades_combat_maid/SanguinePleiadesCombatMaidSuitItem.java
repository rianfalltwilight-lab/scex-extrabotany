package io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid;

import com.google.common.base.Suppliers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.brew.BotaniaMobEffects;
import vazkii.botania.common.brew.effect.BloodthirstMobEffect;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SanguinePleiadesCombatMaidSuitItem extends PleiadesCombatMaidSuitItem {

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidHeadgear),
			new ItemStack(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidSkirt),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidBoots)
	});
	public static int SANGUINE_KILL_REQUIRE = 50;
	public static final float HEAL_RATE = 0.3F;

	public SanguinePleiadesCombatMaidSuitItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		var modifierId = prefix("pleiades_combat_maid_" + getType().getName());
		return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MAX_HEALTH,
				new AttributeModifier(modifierId, 15, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.bySlot(getType().getSlot()));
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	public static boolean isSenketsu(ItemStack stack) {
		String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT).trim();
		return "senketsu".equals(name);
	}

	public static class EventHandler {
		public static void onAttackLiving(LivingDamageEvent.Post event) {
			Entity attacker = event.getSource().getEntity();
			if (!(attacker instanceof LivingEntity living)) {
				return;
			}
			if (!living.getItemBySlot(EquipmentSlot.CHEST).is(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit)) {
				return;
			}

			living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 10, 1));
			living.heal(HEAL_RATE * event.getNewDamage());
		}

		//Get Blood Suit
		private static final Map<UUID, Integer> bloodthirstKilled = new ConcurrentHashMap<>();

		public static void onEffectAdded(MobEffectEvent.Added event) {
			if (!(event.getEffectInstance().getEffect().value() instanceof BloodthirstMobEffect)) {
				return;
			}
			if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
				return;
			}
			bloodthirstKilled.put(serverPlayer.getUUID(), 0);
		}

		public static void onKilled(LivingDeathEvent event) {
			if (!(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
				return;
			}
			if (!serverPlayer.hasEffect(BotaniaMobEffects.BLOODTHIRST)) {
				return;
			}

			bloodthirstKilled.computeIfPresent(serverPlayer.getUUID(), (uuid, count) -> count + 1);
		}

		public static void onEffectRemove(MobEffectEvent.Remove event) {
			if (event.getEntity() instanceof ServerPlayer serverPlayer) {
				onEffectRemovedOrExpired(serverPlayer, event.getEffectInstance());
			}
		}

		public static void onEffectExpired(MobEffectEvent.Expired event) {
			if (event.getEntity() instanceof ServerPlayer serverPlayer) {
				onEffectRemovedOrExpired(serverPlayer, event.getEffectInstance());
			}
		}

		private static void onEffectRemovedOrExpired(ServerPlayer serverPlayer, @Nullable MobEffectInstance instance) {
			if (instance == null) {
				return;
			}
			if (!(instance.getEffect().value() instanceof BloodthirstMobEffect)) {
				return;
			}
			if (!bloodthirstKilled.containsKey(serverPlayer.getUUID())) {
				return;
			}

			replaceArmor(serverPlayer);
		}

		private static void replaceArmor(ServerPlayer serverPlayer) {
			int killed = bloodthirstKilled.get(serverPlayer.getUUID());
			bloodthirstKilled.remove(serverPlayer.getUUID());

			ItemStack origin = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);

			if (killed >= SANGUINE_KILL_REQUIRE && origin.is(ExtraBotanyItems.pleiadesCombatMaidSuit)) {
				ItemStack darkened = new ItemStack(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit);

				darkened.applyComponents(origin.getComponentsPatch());

				serverPlayer.setItemSlot(EquipmentSlot.CHEST, darkened);
			}
		}
	}
}
