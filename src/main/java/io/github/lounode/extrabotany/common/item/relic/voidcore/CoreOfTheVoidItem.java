package io.github.lounode.extrabotany.common.item.relic.voidcore;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空之律者核心。遗物饰品。佩戴者可以飞行，飞行时每 tick 消耗 {@value #FLY_COST} 魔力。它有四种外观，只影响翅膀的样子。
 * Core of the Void. A relic accessory. The wearer can fly, and flight uses {@value #FLY_COST} mana on each tick. It has four appearances, which only change the look of the wings.
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code ItemCoreGod}。重燃加的移动和飞行速度、弹射物免疫、负面效果清除和魔力不足时的反噬已经去掉。
 * <p>Source: {@code ItemCoreGod} of ExtraMeteorP 1.16. The movement and flight speed, the projectile immunity, the removal of harmful effects and the backfire without mana that Reburn added are removed.
 */
public class CoreOfTheVoidItem extends BaubleItem implements CustomCreativeTabContents {

	private static final String TAG_VARIANT = "variant";

	public static final String VARIANT_HERRSCHER = "herrscher";
	public static final String VARIANT_FLANDRE = "flandre";
	public static final String VARIANT_JIM = "jim";
	public static final String VARIANT_STEAMPUNK = "steampunk";
	// 1.12 ItemCoreGod 的 2 号外观：空之律者 2% 掉落。 / Appearance 2 of 1.12 ItemCoreGod: a 2% drop of the Herrscher of the Void.
	public static final String VARIANT_RAINBOW = "rainbow";

	public static final int FLY_COST = 35;

	private static final List<String> playersWithFlight = Collections.synchronizedList(new ArrayList<>());

	public CoreOfTheVoidItem(Properties properties) {
		super(properties);
		// 这里只登记外观的名字。翅膀的画法是客户端的代码，在 CoreOfTheVoidRenderer 里。
		// Only the names of the appearances are recorded here. The method to draw the wings is client code, in CoreOfTheVoidRenderer.
		for (String id : List.of(VARIANT_HERRSCHER, VARIANT_FLANDRE, VARIANT_RAINBOW, VARIANT_STEAMPUNK, VARIANT_JIM)) {
			ExtraBotanyAPI.instance().registerCOVVariant(() -> id);
		}
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		var variants = ExtraBotanyAPI.instance().getCOVVariants();
		for (var variant : variants.values()) {
			ItemStack stack = new ItemStack(this);
			ItemNBTHelper.setString(stack, TAG_VARIANT, variant.getId());
			output.accept(stack);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
		tooltip.add(Component.translatable("extrabotany.wings." + getVariant(stack)));
	}

	@SuppressWarnings("deprecation") // Matches Botania 456's flight state until its shared flight API migrates.
	public static void updatePlayerFlyStatus(Player player) {
		ItemStack tiara = EquipmentHandler.findOrEmpty(ExtraBotanyItems.coreOfTheVoid, player);

		if (playersWithFlight.contains(playerStr(player))) {
			if (shouldPlayerHaveFlight(player)) {
				player.getAbilities().mayfly = true;
				if (player.getAbilities().flying) {
					if (!player.level().isClientSide) {
						if (!player.isCreative() && !player.isSpectator()) {
							ManaItemHandler.instance().requestManaExact(tiara, player, getFlyCost(), true);
						}
					}
				}
			} else {
				if (!player.isSpectator() && !player.getAbilities().instabuild) {
					player.getAbilities().mayfly = false;
					player.getAbilities().flying = false;
					player.getAbilities().invulnerable = false;
				}
				playersWithFlight.remove(playerStr(player));
			}
		} else if (shouldPlayerHaveFlight(player)) {
			playersWithFlight.add(playerStr(player));
			player.getAbilities().mayfly = true;
		}
	}

	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		String username = event.getEntity().getGameProfile().getName();
		playersWithFlight.remove(username + ":false");
		playersWithFlight.remove(username + ":true");
	}

	private static boolean shouldPlayerHaveFlight(Player player) {
		ItemStack armor = EquipmentHandler.findOrEmpty(ExtraBotanyItems.coreOfTheVoid, player);
		if (!armor.isEmpty()) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(armor);
			if (relic == null ||
					!relic.isRightPlayer(player) ||
					!ManaItemHandler.instance().requestManaExact(armor, player, getFlyCost(), false)

			) {
				return false;
			}
			return true;
		}
		return false;
	}

	private static String playerStr(Player player) {
		return player.getGameProfile().getName() + ":" + player.level().isClientSide;
	}

	public static int getFlyCost() {
		return FLY_COST;
	}

	public static String getVariant(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_VARIANT, VARIANT_HERRSCHER);
	}

	@Override
	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return super.hasRender(stack, living) && living instanceof Player;
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
		super.inventoryTick(stack, world, entity, slot, selected);
	}
}
