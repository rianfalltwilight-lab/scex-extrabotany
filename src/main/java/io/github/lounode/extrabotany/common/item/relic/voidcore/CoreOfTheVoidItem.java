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
 * 空之律者核心。遗物饰品。佩戴者可以飞行（飞行时每 tick 消耗魔力），不受弹射物伤害，负面效果会被消耗魔力清除或者挡下。魔力不足时它会反噬：每半秒对佩戴者造成伤害。它有四种外观，只影响翅膀的样子。
 * Core of the Void. A relic accessory. The wearer can fly (flight uses mana on each tick), gets no damage from projectiles, and harmful effects are removed or prevented for mana. When there is not sufficient mana, it backfires: it gives damage to the wearer one time for each half second. It has four appearances, which only change the look of the wings.
 */
public class CoreOfTheVoidItem extends BaubleItem implements CustomCreativeTabContents {

	private static final String TAG_VARIANT = "variant";

	public static final String VARIANT_HERRSCHER = "herrscher";
	public static final String VARIANT_FLANDRE = "flandre";
	public static final String VARIANT_JIM = "jim";
	public static final String VARIANT_STEAMPUNK = "steampunk";

	private static final int FLY_COST = 100;
	private static final int CURE_COST = 200;
	private static final int BACKFIRE_THRESHOLD = 100;
	private static final float BACKFIRE_DAMAGE = 2.0F;

	private static final List<String> playersWithFlight = Collections.synchronizedList(new ArrayList<>());

	public CoreOfTheVoidItem(Properties properties) {
		super(properties);
		// 这里只登记外观的名字。翅膀的画法是客户端的代码，在 CoreOfTheVoidRenderer 里。
		// Only the names of the appearances are recorded here. The method to draw the wings is client code, in CoreOfTheVoidRenderer.
		for (String id : List.of(VARIANT_HERRSCHER, VARIANT_FLANDRE, VARIANT_JIM, VARIANT_STEAMPUNK)) {
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

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (entity.level().isClientSide()) {
			return;
		}

		if (shouldBackFire(stack, entity) && entity.tickCount % 10 == 0) {

			if (entity.hurt(damageSource(entity.level().registryAccess()), getBackfireDamage()) &&
					entity instanceof Player player) {
				player.playNotifySound(ExtraBotanySounds.PLAYER_BACKFIRE, SoundSource.PLAYERS, 1, SoundEventUtil.randomPitch(player.level()));
			}
			return;
		}

		tryRemoveHarmfulPotion(stack, entity);
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

	public static boolean shouldBackFire(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player player &&
				!(player.isCreative() || player.isSpectator()) &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getBackfireThreshold(), false)) {
			return true;
		}
		return false;
	}

	public static int getFlyCost() {
		return FLY_COST;
	}

	public static int getCureCost() {
		return CURE_COST;
	}

	public static int getBackfireThreshold() {
		return BACKFIRE_THRESHOLD;
	}

	public float getBackfireDamage() {
		return BACKFIRE_DAMAGE;
	}

	public static String getVariant(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_VARIANT, VARIANT_HERRSCHER);
	}

	public DamageSource damageSource(RegistryAccess access) {
		return ExtraBotanyDamageTypes.Sources.backfireDamage(access);
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

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		//TODO 反噬以及无魔力时去除Attribute
		attributes.put(Attributes.MOVEMENT_SPEED,
				new AttributeModifier(slotId, 0.1F, AttributeModifier.Operation.ADD_VALUE));
		attributes.put(Attributes.FLYING_SPEED,
				new AttributeModifier(slotId, 0.6F, AttributeModifier.Operation.ADD_VALUE));

		return attributes;
	}

	/**
	 * 佩戴者不受弹射物伤害。
	 * The wearer gets no damage from projectiles.
	 */
	public static void onLivingAttack(LivingIncomingDamageEvent event) {
		// 先做最便宜的判断：绝大多数受伤的生物不是玩家，受到的也不是弹射物伤害。
		// The cheapest checks come first: most creatures that get damage are not players, and most damage is not from projectiles.
		if (!event.getSource().is(DamageTypeTags.IS_PROJECTILE) || !(event.getEntity() instanceof Player player)) {
			return;
		}
		ItemStack armor = EquipmentHandler.findOrEmpty(ExtraBotanyItems.coreOfTheVoid, player);
		if (armor.isEmpty()) {
			return;
		}

		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(armor);
		if (relic == null || !relic.isRightPlayer(player) || shouldBackFire(armor, player)) {
			return;
		}
		event.setCanceled(true);
	}
	//Harmful potion remove
	protected void tryRemoveHarmfulPotion(ItemStack stack, LivingEntity entity) {
		if (!(entity instanceof Player player)) {
			return;
		}
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic == null || !relic.isRightPlayer(player)) {
			return;
		}

		List<MobEffectInstance> effects = entity.getActiveEffects().stream()
				.filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
				.toList();

		if (!effects.isEmpty() &&
				ManaItemHandler.instance().requestManaExactForTool(stack, player, getCureCost(), true)) {
			effects.forEach(e -> entity.removeEffect(e.getEffect()));
		}
	}

	//TODO 立即生效药水效果截断
	public static void onEffectAdd(MobEffectEvent.Applicable event) {
		if (!(event.getEntity() instanceof Player player)
				|| event.getEffectInstance().getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
			return;
		}
		ItemStack armor = EquipmentHandler.findOrEmpty(ExtraBotanyItems.coreOfTheVoid, player);

		if (!armor.isEmpty()) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(armor);
			if (relic != null &&
					relic.isRightPlayer(player) &&
					ManaItemHandler.instance().requestManaExact(armor, player, getCureCost(), true)) {

				event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
			}
		}
	}
}
