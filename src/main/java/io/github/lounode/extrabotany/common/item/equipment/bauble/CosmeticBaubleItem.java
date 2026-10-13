package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.Set;

/**
 * 装饰饰品。它只改变外观，但"超级王冠"有一个额外效果。
 * Cosmetic bauble. The bauble only changes the appearance, but the Super Crown has one more effect.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemBaubleCosmetic}。护目镜和超级王冠的效果来自 1.12 的 {@code ItemCosmetic} 和 {@code ItemSuperCrown}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemBaubleCosmetic}. The goggles and the effect of the Super Crown come from {@code ItemCosmetic} and {@code ItemSuperCrown} of 1.12.
 */
public class CosmeticBaubleItem extends BaubleItem {

	/**
	 * 饰品的种类和渲染位置。位移和缩放的数值来自原版的渲染代码。
	 * The variants and their render positions. The translation and scale values come from the render code of the original.
	 */
	public enum Variant {
		FOX_EAR(true, 0F, -0.8F, -0.1F, 0.8F, -0.8F, -0.8F),
		FOX_MASK(true, 0.02F, -0.3F, -0.3F, 0.66F, -0.65F, -0.65F),
		PYLON(true, 0F, -0.8F, -0.1F, 0.5F, -0.5F, -0.5F),
		GOGGLE_JUNGLE(true, 0F, -0.2F, -0.3F, 0.55F, -0.55F, -0.55F),
		GOGGLE_OCEAN(true, 0F, -0.2F, -0.3F, 0.55F, -0.55F, -0.55F),
		GOGGLE_SNOWFIELD(true, 0F, -0.2F, -0.3F, 0.55F, -0.55F, -0.55F),
		GOGGLE_STANDARD(true, 0F, -0.2F, -0.3F, 0.55F, -0.55F, -0.55F),
		BLACK_GLASSES(true, 0F, -0.2F, -0.3F, 0.55F, 0.55F, -0.55F),
		THUG_LIFE(true, 0F, -0.2F, -0.3F, 0.7F, -0.7F, -0.7F),
		SUPER_CROWN(true, 0F, -0.7F, -0.1F, 0.65F, -0.65F, -0.65F),
		MASK(true, 0F, -0.3F, -0.3F, 0.65F, -0.65F, -0.65F),
		RED_SCARF(false, 0F, 0.16F, -0.15F, 0.55F, -0.55F, -0.55F);

		/**
		 * {@code true} 表示饰品跟随头部；{@code false} 表示跟随身体。
		 * {@code true} means that the bauble follows the head. {@code false} means that it follows the body.
		 */
		public final boolean onHead;
		public final float x, y, z;
		public final float scaleX, scaleY, scaleZ;

		Variant(boolean onHead, float x, float y, float z, float scaleX, float scaleY, float scaleZ) {
			this.onHead = onHead;
			this.x = x;
			this.y = y;
			this.z = z;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.scaleZ = scaleZ;
		}
	}

	private static final float SUPER_CROWN_DAMAGE_REDUCTION = 2F;
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private final Variant variant;

	public CosmeticBaubleItem(Variant variant, Properties props) {
		super(props);
		this.variant = variant;
	}

	public Variant getVariant() {
		return variant;
	}

	@Override
	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return super.hasRender(stack, living) && living instanceof Player;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		if (variant == Variant.FOX_MASK) {
			for (int line = 0; line < 3; line++) {
				tooltip.add(Component.translatable("extrabotany.foxmaskinfo" + line).withStyle(ChatFormatting.ITALIC));
			}
		}
	}

	/**
	 * 处理超级王冠的减伤。佩戴者同时穿着整套女仆装时，受到的伤害减少 2 点。
	 * Applies the damage reduction of the Super Crown. When the wearer also has a full maid armor set, the damage decreases by 2.
	 *
	 * <p>来源：ExtraMeteorP 1.12 {@code ItemSuperCrown#onEntityDamaged}。
	 * <p>Source: ExtraMeteorP 1.12 {@code ItemSuperCrown#onEntityDamaged}.
	 */
	public static void onSuperCrownWearerHurt(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (EquipmentHandler.findOrEmpty(ExtraBotanyItems.superCrown, player).isEmpty() || !hasMaidArmorSet(player)) {
			return;
		}
		event.setAmount(Math.max(0, event.getAmount() - SUPER_CROWN_DAMAGE_REDUCTION));
	}

	/**
	 * 判断玩家是否穿着整套女仆装。星空歌姬套装和昴宿星团战斗女仆套装可以混穿。
	 * Finds if the player has a full maid armor set. The pieces of the Starry Idol set and the Pleiades Combat Maid set can be used together.
	 */
	private static boolean hasMaidArmorSet(Player player) {
		List<Set<Item>> allowedBySlot = List.of(
				Set.of(ExtraBotanyItems.starryIdolHeadgear, ExtraBotanyItems.pleiadesCombatMaidHeadgear),
				Set.of(ExtraBotanyItems.starryIdolSuit, ExtraBotanyItems.pleiadesCombatMaidSuit,
						ExtraBotanyItems.sanguinePleiadesCombatMaidSuit),
				Set.of(ExtraBotanyItems.starryIdolSkirt, ExtraBotanyItems.pleiadesCombatMaidSkirt),
				Set.of(ExtraBotanyItems.starryIdolBoots, ExtraBotanyItems.pleiadesCombatMaidBoots));
		for (int i = 0; i < ARMOR_SLOTS.length; i++) {
			if (!allowedBySlot.get(i).contains(player.getItemBySlot(ARMOR_SLOTS[i]).getItem())) {
				return false;
			}
		}
		return true;
	}
}
