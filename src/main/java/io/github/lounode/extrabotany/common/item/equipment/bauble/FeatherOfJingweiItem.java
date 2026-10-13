package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.entity.AuraFireEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 精卫之羽。佩戴者空手、并且攻击冷却结束时按攻击键，会发射一团火焰，消耗 {@value #MANA_PER_USE} 点魔力。火焰命中时给佩戴者伤害吸收。
 * Feather of Jingwei. When the wearer has an empty hand and presses the attack key with the attack cooldown complete, it shoots a fire for {@value #MANA_PER_USE} mana. A hit of the fire gives absorption to the wearer.
 */
public class FeatherOfJingweiItem extends BaubleItem {
	public static final int MANA_PER_USE = 300;
	private static final Map<Player, Long> LAST_AURA_TICK = new WeakHashMap<>();

	public FeatherOfJingweiItem(Properties props) {
		super(props);
	}

	/**
	 * 火焰命中时给佩戴者伤害吸收，最多 {@link AuraFireEntity#ABSORPTION_MAX} 点。从 1.20.5 起，伤害吸收不能超过"最大伤害吸收"属性，所以这个饰品要提供这个属性。
	 * A hit of the fire gives absorption to the wearer, to a maximum of {@link AuraFireEntity#ABSORPTION_MAX}. From 1.20.5, absorption cannot be more than the "max absorption" attribute, thus this accessory must supply that attribute.
	 */
	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.MAX_ABSORPTION,
				new AttributeModifier(slotId, AuraFireEntity.ABSORPTION_MAX, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	public static void leftClick(PlayerInteractEvent.LeftClickEmpty event) {
		Player player = event.getEntity();
		if (!EquipmentHandler.findOrEmpty(ExtraBotanyItems.featherOfJingwei, player).isEmpty()) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(LeftClickPacketJingwei.INSTANCE);
		}
	}

	public static InteractionResult attackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			trySpawnAuraFire(player);
		}
		return InteractionResult.PASS;
	}

	public static void trySpawnAuraFire(Player player) {
		trySpawnAuraFire(player, player.getAttackStrengthScale(0F));
	}

	public static void trySpawnAuraFire(Player player, float attackStrength) {
		if (attackStrength != 1 ||
				player.isSpectator() ||
				!player.getMainHandItem().isEmpty()) {
			return;
		}

		ItemStack jingwei = EquipmentHandler.findOrEmpty(ExtraBotanyItems.featherOfJingwei, player);
		if (jingwei.isEmpty()) {
			return;
		}
		long tick = player.level().getGameTime();
		Long previous = LAST_AURA_TICK.put(player, tick);
		if (previous != null && previous == tick) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(jingwei, player, MANA_PER_USE, true)) {
			return;
		}

		AuraFireEntity fire = getFire(player);
		player.level().addFreshEntity(fire);
		player.playNotifySound(ExtraBotanySounds.FEATHER_OF_JINGWEI_SHOOT, SoundSource.PLAYERS, 1f, SoundEventUtil.randomPitch(player.level()));
	}

	public static AuraFireEntity getFire(Player player) {
		AuraFireEntity fire = new AuraFireEntity(player);

		float motionModifier = 9F;

		fire.setGravity(0);
		fire.setDeltaMovement(fire.getDeltaMovement().scale(motionModifier));

		return fire;
	}
}
