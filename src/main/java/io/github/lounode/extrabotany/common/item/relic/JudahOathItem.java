package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;

import io.github.lounode.extrabotany.common.entity.judah.JudahOathEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

/**
 * 犹大的誓约。遗物武器：攻击伤害 +12，攻击速度 -3.6。它有三种外观。
 * Judah Oath. A relic weapon: +12 attack damage, -3.6 attack speed. It has three appearances.
 *
 * <p>使用：消耗 2500 魔力，把誓约投向前方。誓约落地后展开一个结界，禁锢范围内的生物并且召唤光矛。樱花的誓约召唤的是沿五角星飞行的剑。
 * <p>Use: uses 2500 mana and throws the oath ahead. After the oath lands it opens a field that holds the mobs in range and summons spears. The Oath of Sakura summons swords that fly along a five-pointed star.
 *
 * <p>三种外观共用 80 tick 的冷却。原版里它们是同一个物品的三个子类型。
 * <p>The three appearances share a cooldown of 80 ticks. In the original they are three subtypes of one item.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemJudahOath}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemJudahOath}.
 *
 * <p>和原版的差异：原版还可以把它当作装饰品附加在饰品上，在背后显示图案。这个功能没有移植。
 * <p>Difference from the original: the original can also attach it to an accessory as a cosmetic item, which shows a picture behind the back. This function is not ported.
 */
public class JudahOathItem extends RelicItem {
	/**
	 * 誓约的三种外观。序号和原版的物品子类型数值相同，并且写进实体的存档。
	 * The three appearances of the oath. The ordinal agrees with the item subtype value of the original, and it is saved with the entity.
	 */
	public enum Variant {
		JUDAH,
		KIRA,
		SAKURA;

		public static Variant byId(int id) {
			return id >= 0 && id < values().length ? values()[id] : JUDAH;
		}
	}

	private static final double ATTACK_DAMAGE = 12.0;
	private static final double ATTACK_SPEED = -3.6;
	private static final int MANA_COST = 2500;
	private static final int COOLDOWN_TICKS = 80;
	private static final float THROW_SPEED = 0.5F;
	private static final int OATH_DAMAGE = 7;

	private final Variant variant;

	public JudahOathItem(Variant variant, Properties props) {
		super(props.attributes(ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build()));
		this.variant = variant;
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	public Variant getVariant() {
		return variant;
	}

	public static Item itemFor(Variant variant) {
		return switch (variant) {
			case JUDAH -> ExtraBotanyItems.judahOath;
			case KIRA -> ExtraBotanyItems.judahOathKira;
			case SAKURA -> ExtraBotanyItems.judahOathSakura;
		};
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.fail(stack);
		}
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic == null) {
			return InteractionResultHolder.fail(stack);
		}
		// 先绑定，这样刚拿到的誓约可以立刻使用。
		// Bind first, so that a new oath can be used immediately.
		relic.tickBinding(player);
		if (!relic.isRightPlayer(player) || !ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
			return InteractionResultHolder.fail(stack);
		}
		for (Variant other : Variant.values()) {
			player.getCooldowns().addCooldown(itemFor(other), COOLDOWN_TICKS);
		}

		JudahOathEntity oath = new JudahOathEntity(level, player, variant, OATH_DAMAGE, Mth.wrapDegrees(-player.getYRot() + 180F));
		oath.setPos(player.getX(), player.getY() + 1.0, player.getZ());
		oath.shootFromRotation(player, player.getXRot(), player.getYRot(), 0F, THROW_SPEED, 0F);
		level.addFreshEntity(oath);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.judah_oath").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		super.appendHoverText(stack, context, tooltip, flags);
	}
}
