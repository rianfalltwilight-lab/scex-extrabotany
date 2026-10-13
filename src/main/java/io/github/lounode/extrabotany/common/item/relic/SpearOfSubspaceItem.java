package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceBursts;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 亚空之矛。遗物武器：攻击伤害 +8，攻击速度 -1.6，方块交互距离 +2。
 * Spear of Subspace. A relic weapon: +8 attack damage, -1.6 attack speed, +2 block interaction range.
 *
 * <ul>
 * <li>左键：消耗 600 魔力，在头顶打开一道裂隙，射出一支亚空之矛。
 * Left click: uses 600 mana, opens a rift above the head, and shoots one Subspace Spear.</li>
 * <li>按住右键再松开：消耗 10000 魔力，跳到空中，在身后打开 24 道裂隙，裂隙向前发射金色魔力脉冲。玩家得到 120 tick 的"永恒"效果。冷却 600 tick。
 * Hold and release the use key: uses 10000 mana, jumps into the air, and opens 24 rifts behind the player. The rifts shoot golden mana bursts ahead. The player gets the Eternity effect for 120 ticks. The cooldown is 600 ticks.</li>
 * <li>魔力不够时蓄力技能仍然可以释放，但是冷却加倍到 1200 tick。
 * When there is not sufficient mana the charged skill still works, but the cooldown is two times as long: 1200 ticks.</li>
 * </ul>
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemSpearSubspace}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemSpearSubspace}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版只在松开右键时释放技能，按满 200 tick 时什么也不发生。这里按满时也释放。
 * The original only uses the skill when the use key is released, and nothing occurs when the key is held for the full 200 ticks. This port also uses the skill at the end of the full time.</li>
 * <li>左键技能要求玩家是遗物的主人。原版只在蓄力技能里检查。
 * The left-click skill needs the player to own the relic. The original only checks this for the charged skill.</li>
 * </ul>
 */
public class SpearOfSubspaceItem extends RelicItem implements LeftClickItem, LensEffectItem {
	private static final double ATTACK_DAMAGE = 8.0;
	private static final double ATTACK_SPEED = -1.6;
	private static final double REACH_BONUS = 2.0;

	private static final int SPEAR_MANA_COST = 600;
	private static final int DOMAIN_MANA_COST = 10000;
	private static final int DOMAIN_COOLDOWN_TICKS = 600;
	private static final int DOMAIN_RIFT_COUNT = 24;
	private static final int RIFTS_PER_ROW = 8;
	private static final int ETERNITY_TICKS = 120;
	private static final double JUMP_SPEED = 1.5;
	private static final int USE_DURATION_TICKS = 200;

	public SpearOfSubspaceItem(Properties props) {
		super(props.attributes(ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				// 原版使用 Forge 的触及距离属性。它只影响方块，不影响实体。
				// The original uses the reach distance attribute of Forge. That attribute only applies to blocks, not to entities.
				.add(Attributes.BLOCK_INTERACTION_RANGE,
						new AttributeModifier(prefix("spear_of_subspace_block_reach"), REACH_BONUS, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build()));
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.HERRSCHER_DEFEAT));
	}

	// ---------------------------------------------------------------------
	// 左键：亚空之矛 / Left click: the Subspace Spear
	// ---------------------------------------------------------------------

	@Override
	public void onLeftClick(Player player, @Nullable Entity target) {
		if (!RelicSwordItem.canUseLeftClickSkill(player, this)) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!ManaItemHandler.instance().requestManaExact(stack, player, SPEAR_MANA_COST, true)) {
			return;
		}
		Level level = player.level();
		RandomSource random = level.getRandom();
		SubspaceEntity rift = new SubspaceEntity(level, player, SubspaceEntity.Mode.SPEAR, 24, 5, 10,
				0.4F + random.nextFloat() * 0.15F, Mth.wrapDegrees(-player.getYRot() + 180F));
		rift.setPos(player.getX(), player.getY() + 2.5 + random.nextFloat() * 0.2, player.getZ());
		rift.setYRot(player.getYRot());
		level.addFreshEntity(rift);
		if (target == null) {
			player.resetAttackStrengthTicker();
		}
	}

	// ---------------------------------------------------------------------
	// 蓄力：亚空间领域 / Charge: the Subspace domain
	// ---------------------------------------------------------------------

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.fail(stack);
		}
		player.startUsingItem(hand);
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION_TICKS;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		releaseUsing(stack, level, entity, 0);
		return stack;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		if (level.isClientSide() || !(entity instanceof Player player) || player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
			return;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic == null || !relic.isRightPlayer(player)) {
			return;
		}

		// 服务端抬起玩家，然后把新的速度发给客户端。
		// The server lifts the player and then sends the new velocity to the client.
		player.setSprinting(true);
		player.setDeltaMovement(player.getDeltaMovement().add(0.0, JUMP_SPEED, 0.0));
		player.hurtMarked = true;

		boolean paid = ManaItemHandler.instance().requestManaExactForTool(stack, player, DOMAIN_MANA_COST, true);
		player.getCooldowns().addCooldown(this, paid ? DOMAIN_COOLDOWN_TICKS : DOMAIN_COOLDOWN_TICKS * 2);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, ETERNITY_TICKS));
		level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, SoundSource.PLAYERS, 1.6F, 1F);
		openDomain(level, player);
	}

	private static void openDomain(Level level, Player player) {
		RandomSource random = level.getRandom();
		Vec3 behind = horizontalLook(player).scale(-2.0);
		float rotation = Mth.wrapDegrees(-player.getYRot() + 180F);
		for (int i = 0; i < DOMAIN_RIFT_COUNT; i++) {
			int row = i / RIFTS_PER_ROW;
			Vec3 origin = player.position().add(0.0, player.getBbHeight() / 2.0 + 1.6, row * 0.1).add(behind);
			Vec3 pos = origin.add(domainOffset(behind, i)).add(0.0, -0.5 + random.nextFloat(), 0.0);
			SubspaceEntity rift = new SubspaceEntity(level, player, SubspaceEntity.Mode.BURSTS, 120,
					15 + random.nextInt(12), 10 + random.nextInt(10), 1F + random.nextFloat(), rotation);
			rift.setPos(pos);
			rift.setYRot(player.getYRot());
			level.addFreshEntity(rift);
		}
	}

	/**
	 * 玩家视线的水平方向。玩家垂直向上或者向下看时，使用身体的朝向。
	 * The horizontal direction of the player view. When the player looks straight up or down, the body direction is used.
	 */
	private static Vec3 horizontalLook(Player player) {
		Vec3 look = player.getLookAngle().multiply(1.0, 0.0, 1.0);
		if (look.lengthSqr() < 1.0E-8) {
			double yaw = Math.toRadians(player.getYRot() + 90F);
			look = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
		}
		return look.normalize();
	}

	/**
	 * 计算第 {@code index} 道裂隙相对于扇形中心的位置。
	 * Calculates the position of rift number {@code index} relative to the center of the fan.
	 *
	 * <p>裂隙排成三排半圆形的扇面，每排 8 道，半径是 5、8.5 和 12 格。扇面垂直于玩家的水平视线，并且总是在上半边。
	 * <p>The rifts make three half-circle rows of 8, with a radius of 5, 8.5 and 12 blocks. The fan is at a right angle to the horizontal view of the player and is always in the upper half.
	 *
	 * @param look 玩家的水平视线方向。长度不重要。 The horizontal view direction of the player. The length is not important.
	 */
	public static Vec3 domainOffset(Vec3 look, int index) {
		Vec3 direction = look.multiply(1.0, 0.0, 1.0).normalize();
		if (direction.lengthSqr() < 1.0E-8) {
			direction = new Vec3(0.0, 0.0, 1.0);
		}
		Vec3 axis = direction.cross(new Vec3(-1.0, 0.0, -1.0)).normalize();
		if (axis.lengthSqr() < 1.0E-8) {
			// 视线和 (-1, 0, -1) 平行时叉积是零向量。这时扇面的起始轴取竖直方向。
			// The cross product is the zero vector when the view is parallel to (-1, 0, -1). In that case the start axis of the fan is vertical.
			axis = new Vec3(0.0, 1.0, 0.0);
		}
		double angle = index % RIFTS_PER_ROW * Math.PI / 7.0 - Math.PI / 2.0;
		double radius = index / RIFTS_PER_ROW * 3.5 + 5.0;
		Vec3 offset = axis.scale(Math.cos(angle)).add(direction.cross(axis).scale(Math.sin(angle))).scale(radius);
		return new Vec3(offset.x, Math.abs(offset.y), offset.z);
	}

	// ---------------------------------------------------------------------
	// 金色魔力脉冲的透镜回调 / Lens callbacks for the golden mana bursts
	// ---------------------------------------------------------------------

	@Override
	public void apply(ItemStack stack, BurstProperties props, Level level) {}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		SubspaceBursts.update(burst);
	}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult hit, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		return shouldKill;
	}

	@Override
	public boolean doParticles(ManaBurst burst, ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.spear_of_subspace").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, context, tooltip, flags);
	}
}
