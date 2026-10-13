package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSwordEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionVoidEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.StrengthenSlashEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.FlamescionHandler;

import java.util.List;

/**
 * 焢煌之境：劫炎永燎。注释里用它英文名的直译"薪炎之钥"来称呼它。下界合金等级的剑，带有一套空中战斗的技能。
 * Key of Flamescion. The Chinese comments call it "薪炎之钥", the direct translation of the English name; the Chinese name in the game is different. A sword of the netherite tier with a set of skills for a fight in the air.
 *
 * <ul>
 * <li>潜行并使用：点燃。站在地面上时，把自己和 3 格内的生物抛到空中。玩家得到"灼热模式"效果，在空中进入薪炎模式。
 * Sneak and use: ignite. On the ground, this throws the player and the mobs within 3 blocks into the air. The player gets the Incandescence Mode effect and goes into the Flamescion mode in the air.</li>
 * <li>薪炎模式下挥剑：放出一道剑气。
 * Swing in the Flamescion mode: releases a Flamescion Sword.</li>
 * <li>薪炎模式下使用：在前方 5 格放出一个黑洞。冷却 40 tick。
 * Use in the Flamescion mode: releases a Flamescion Void 5 blocks ahead. The cooldown is 40 ticks.</li>
 * <li>有"薪炎强化"效果时挥剑：放出三道强化斩击，并且消耗这个效果。
 * Swing with the Flamescion Strength effect: releases three Strengthened Slashes and uses up the effect.</li>
 * </ul>
 *
 * <p>冲刺和终结技由按键触发，逻辑在 {@link FlamescionHandler} 中。
 * <p>Keys start the dash and the ultimate. Their logic is in {@link FlamescionHandler}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemFlamescionWeapon}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemFlamescionWeapon}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>点燃时立刻把玩家标记为离地。原版等客户端的移动数据包来更新，这段时间里服务端会因为"玩家还在地面上"而移除"灼热模式"效果。
 * The ignition marks the player as off the ground immediately. The original waits for the movement packet of the client, and in that time the server removes the Incandescence Mode effect because "the player is still on the ground".</li>
 * <li>对着空气挥剑放出剑气之后，重置攻击冷却。原版的服务端不重置，所以连点可以无限放出剑气。
 * After a swing at the air releases a sword, the attack cooldown is reset. The server of the original does not reset it, so fast clicks release swords with no limit.</li>
 * <li>点燃只抛起玩家可以伤害的生物。
 * The ignition only throws the mobs that the player can damage.</li>
 * </ul>
 */
public class FlamescionWeaponItem extends SwordItem implements LeftClickItem {
	private static final int ATTACK_DAMAGE_BONUS = 5;
	private static final float ATTACK_SPEED = -1.6F;
	private static final float IGNITE_RANGE = 3F;
	private static final double IGNITE_LIFT = 1.0;
	private static final int IGNITE_EFFECT_TICKS = 60;
	private static final int IGNITE_TIMELOCK_TICKS = 60;
	private static final double SKILL_DISTANCE = 5.0;
	private static final int VOID_EFFECT_TICKS = 80;
	private static final int VOID_COOLDOWN_TICKS = 40;
	private static final float STRENGTHEN_SPREAD_DEGREES = 15F;

	// 旧版本把能量写在物品的自定义数据里。现在能量属于玩家，所以清除这两个键。
	// Older versions wrote the energy into the custom data of the item. The energy now belongs to the player, so these two keys are removed.
	private static final String LEGACY_TAG_ENERGY = "flamescion_energy";
	private static final String LEGACY_TAG_OVERLOADED = "flamescion_overloaded";

	public FlamescionWeaponItem(Properties props) {
		super(Tiers.NETHERITE, props.attributes(SwordItem.createAttributes(Tiers.NETHERITE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED)));
	}

	@Override
	public void onLeftClick(Player player, @Nullable Entity target) {
		if (player.level().isClientSide() || player.isSpectator()
				|| !player.getMainHandItem().is(this) || player.getAttackStrengthScale(0F) < 1F) {
			return;
		}
		Level level = player.level();
		Vec3 look = player.getLookAngle();
		if (player.hasEffect(ExtraBotanyMobEffects.FLAMESCION)) {
			for (int i = -1; i <= 1; i++) {
				Vec3 direction = look.yRot((float) Math.toRadians(STRENGTHEN_SPREAD_DEGREES * i));
				level.addFreshEntity(new StrengthenSlashEntity(level, player, direction));
			}
			player.removeEffect(ExtraBotanyMobEffects.FLAMESCION);
		} else if (FlamescionHandler.isFlamescionMode(player)) {
			level.addFreshEntity(new FlamescionSwordEntity(level, player, look));
		} else {
			return;
		}
		// 打中实体时，原版的攻击流程会自己重置冷却。对着空气或者方块挥剑时必须在这里重置。
		// When the swing hits an entity, the vanilla attack resets the cooldown itself. For a swing at the air or at a block, the reset must be done here.
		if (target == null) {
			player.resetAttackStrengthTicker();
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isSpectator() || FlamescionHandler.isOverloaded(player) || player.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.pass(stack);
		}
		boolean inMode = FlamescionHandler.isFlamescionMode(player);
		if (player.isShiftKeyDown() && !inMode) {
			ignite(level, player);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		if (inMode) {
			if (!level.isClientSide()) {
				Vec3 target = player.position().add(player.getLookAngle().scale(SKILL_DISTANCE));
				FlamescionVoidEntity flameVoid = new FlamescionVoidEntity(level, player);
				flameVoid.setPos(target);
				level.addFreshEntity(flameVoid);
				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, VOID_EFFECT_TICKS));
			}
			player.getCooldowns().addCooldown(this, VOID_COOLDOWN_TICKS);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		return InteractionResultHolder.pass(stack);
	}

	private void ignite(Level level, Player player) {
		boolean grounded = player.onGround();
		if (grounded) {
			// 客户端和服务端都抛起玩家自己，这样两边的运动一致。
			// The client and the server both throw the player, so that the movement agrees on the two sides.
			player.setDeltaMovement(player.getDeltaMovement().add(0.0, IGNITE_LIFT, 0.0));
			player.setOnGround(false);
			player.hurtMarked = true;
		}
		if (level.isClientSide()) {
			spawnIgniteParticles(level, player, grounded);
			return;
		}
		if (grounded) {
			AABB area = new AABB(player.blockPosition()).inflate(IGNITE_RANGE - 0.5);
			List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area);
			for (LivingEntity living : nearby) {
				if (living == player || !DamageHandler.INSTANCE.checkPassable(living, player)) {
					continue;
				}
				living.setDeltaMovement(living.getDeltaMovement().add(0.0, IGNITE_LIFT, 0.0));
				living.hurtMarked = true;
				living.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, IGNITE_TIMELOCK_TICKS));
			}
		}
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, IGNITE_EFFECT_TICKS));
	}

	/**
	 * 点燃的粒子效果。在地面上是一圈上升的火柱，在空中是一圈向外扩散的火焰。
	 * The particles of the ignition. On the ground they are a ring of rising flame columns, and in the air they are a ring of flames that go outward.
	 */
	private static void spawnIgniteParticles(Level level, Player player, boolean grounded) {
		double y = player.getY() + 0.5;
		if (grounded) {
			for (int angle = 0; angle < 360; angle += 30) {
				double x = player.getX() + IGNITE_RANGE * Math.cos(Math.toRadians(angle));
				double z = player.getZ() + IGNITE_RANGE * Math.sin(Math.toRadians(angle));
				for (int j = 0; j < 6; j++) {
					level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.12 * j, 0.0);
				}
			}
			return;
		}
		for (int angle = 0; angle < 360; angle += 15) {
			double dx = Math.cos(Math.toRadians(angle));
			double dz = Math.sin(Math.toRadians(angle));
			for (int j = 0; j < 3; j++) {
				double speed = 0.25 + 0.01 * j;
				level.addParticle(ParticleTypes.FLAME, player.getX() + 0.5 * dx, y, player.getZ() + 0.5 * dz, dx * speed, 0.0, dz * speed);
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide()) {
			return;
		}
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data != null && (data.contains(LEGACY_TAG_ENERGY) || data.contains(LEGACY_TAG_OVERLOADED))) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, (CompoundTag tag) -> {
				tag.remove(LEGACY_TAG_ENERGY);
				tag.remove(LEGACY_TAG_OVERLOADED);
			});
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.controls").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.ultimate_key",
				Component.keybind("key.extrabotany.legacy_skill")).withStyle(ChatFormatting.GRAY));
	}
}
