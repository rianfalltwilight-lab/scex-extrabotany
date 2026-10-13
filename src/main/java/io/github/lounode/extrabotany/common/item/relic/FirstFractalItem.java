package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.entity.projectile.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 最初分型。左键召唤四把幻影剑，从四周刺向目标。
 * First Fractal. The attack key summons four phantom swords that fly to the target from all sides.
 *
 * <p>前三把剑各消耗 160 点魔力。魔力不足时剑仍然出现，这和原版一致。
 * <p>Each of the first three swords uses 160 mana. When there is not sufficient mana, the swords still appear. This agrees with the original.
 *
 * <p>玩家必须先击败空之律者，才能使用它的技能。
 * <p>The player must defeat the Herrscher of the Void before the skill can be used.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemFirstFractal}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemFirstFractal}.
 */
public class FirstFractalItem extends RelicSwordItem {
	public static final int MANA_PER_SWORD = 160;
	private static final double AIM_RANGE = 80;
	private static final int DELAYED_SWORDS = 3;
	private static final int FINAL_SWORD_VARIETY = 9;

	public FirstFractalItem(Properties props) {
		super(Tiers.NETHERITE, props
				// 原版的最初分型没有耐久。
				// The First Fractal of the original has no durability.
				.component(DataComponents.UNBREAKABLE, new Unbreakable(false))
				.attributes(createFractalAttributes()));
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.HERRSCHER_DEFEAT));
	}

	/**
	 * 原版数值：攻击力 12，攻击速度 1.6，移动速度 +30%，攻击速度 +15%，方块交互距离 +5。
	 * The values of the original: 12 attack damage, 1.6 attack speed, +30% movement speed, +15% attack speed, +5 block interaction range.
	 */
	private static ItemAttributeModifiers createFractalAttributes() {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 11, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.MOVEMENT_SPEED,
						new AttributeModifier(prefix("first_fractal_movement_speed"), 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(prefix("first_fractal_attack_speed"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.BLOCK_INTERACTION_RANGE,
						new AttributeModifier(prefix("first_fractal_interaction_range"), 5, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build();
	}

	@Override
	protected boolean useSkill(Player player, ItemStack stack, @Nullable Entity target) {
		if (!hasDefeatedHerrscher(player)) {
			player.displayClientMessage(
					Component.translatable("extrabotany.message.advancement_required").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (!player.hasInfiniteMaterials()) {
			for (int i = 0; i < DELAYED_SWORDS; i++) {
				ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_SWORD, true);
			}
		}
		attackEntity(player, target);
		return true;
	}

	/**
	 * 召唤四把幻影剑。不消耗魔力。Boss 也用这个方法攻击。
	 * Summons the four phantom swords. This method takes no mana. A boss also attacks with this method.
	 */
	public void attackEntity(LivingEntity user, @Nullable Entity target) {
		Vec3 aim = aimPosition(user, target, AIM_RANGE, false);
		for (int i = 0; i < DELAYED_SWORDS; i++) {
			user.level().addFreshEntity(PhantomSwordEntity.create(user, aim, 5 + 5 * i));
		}
		PhantomSwordEntity finalSword = PhantomSwordEntity.create(user, aim, 0);
		finalSword.setVariety(FINAL_SWORD_VARIETY);
		user.level().addFreshEntity(finalSword);

		user.level().playSound(null, user.getX(), user.getY(), user.getZ(),
				BotaniaSounds.TERRABLADE, user.getSoundSource(), 0.4F, 1.4F);
	}

	private static boolean hasDefeatedHerrscher(Player player) {
		return player.hasInfiniteMaterials()
				|| player instanceof ServerPlayer serverPlayer
						&& PlayerHelper.hasAdvancement(serverPlayer, prefix("main/" + LibAdvancementNames.HERRSCHER_DEFEAT));
	}
}
