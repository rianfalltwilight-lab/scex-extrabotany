package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.equipment.tool.bow.LivingwoodBowItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.entity.MagicArrowEntity;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 百中弓。遗物。它不使用箭，拉弓后由主人消耗魔力射出一支魔法箭。魔法箭沿途对 2 格内的生物每 tick 造成穿甲伤害。
 * Failnaught. A relic. It does not use arrows: after a draw, its owner uses mana to shoot a magic arrow. On its way the magic arrow gives armor-piercing damage to the creatures within 2 blocks on each tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemFailnaught} 和 {@code EntityMagicArrow}。拉弓 i tick（至少 8），等级 = (i − 8) / 5；魔力 min(800, 350 + 20 × 等级)；伤害 min(50, 7 + 0.5 × 等级 + 攻击力) + 力量等级；存在 min(150, 5 + 4i) tick；速度 3。重燃的四档脉冲、命中扣魔力和冲击、火矢、多重射击、快速装填效果已经去掉。
 * <p>Source: {@code ItemFailnaught} and {@code EntityMagicArrow} of ExtraMeteorP 1.16. A draw of i ticks (at least 8) gives the rank (i − 8) / 5; the mana is min(800, 350 + 20 × rank); the damage is min(50, 7 + 0.5 × rank + attack damage) plus the Power level; it lives min(150, 5 + 4i) ticks; its speed is 3. The four burst tiers of Reburn, the mana cost on each hit and the effects of Punch, Flame, Multishot and Quick Charge are removed.
 */
public class FailnaughtItem extends LivingwoodBowItem implements LensEffectItem {
	private static final float ADVANCEMENT_REQUIRE = 100.0F;
	public static final float ATTACK_BOX_RADIUS = 2.0F;
	public static final int MIN_DRAW_TICKS = 8;
	public static final int MAX_DAMAGE = 50;
	public static final float SPEED = 3.0F;

	public FailnaughtItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}

	public static int getRank(int drawTicks) {
		return (drawTicks - MIN_DRAW_TICKS) / 5;
	}

	public static int getManaCost(int drawTicks) {
		return Math.min(800, 350 + getRank(drawTicks) * 20);
	}

	public static int getDamage(int drawTicks, double attackDamage, int powerLevel) {
		return (int) Math.min(MAX_DAMAGE, 7 + getRank(drawTicks) * 0.5F + attackDamage) + powerLevel;
	}

	public static int getLife(int drawTicks) {
		return Math.min(150, 5 + drawTicks * 4);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity livingEntity, int timeLeft) {
		if (world.isClientSide() || !(livingEntity instanceof Player player)) {
			return;
		}
		int drawTicks = getUseDuration(stack, player) - timeLeft;
		if (drawTicks < MIN_DRAW_TICKS) {
			return;
		}
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic == null || !relic.isRightPlayer(player)
				|| !ManaItemHandler.instance().requestManaExactForTool(stack, player, getManaCost(drawTicks), true)) {
			return;
		}
		int power = stack.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER));
		MagicArrowEntity arrow = getArrow(player, stack, drawTicks,
				getDamage(drawTicks, player.getAttributeValue(Attributes.ATTACK_DAMAGE), power));
		world.addFreshEntity(arrow);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.FAILNAUGHT_SHOOT, SoundSource.PLAYERS, 1F, 1F);
	}

	public static MagicArrowEntity getArrow(Player player, ItemStack stack, int drawTicks, float damage) {
		MagicArrowEntity arrow = new MagicArrowEntity(player);
		int life = getLife(drawTicks);
		arrow.setColor(0x20FF20);
		arrow.setMana(1000);
		arrow.setStartingMana(1000);
		// 魔力在 life tick 之后一次扣光，所以箭正好存在 life tick。 / The mana is all lost after life ticks, so the arrow lives exactly life ticks.
		arrow.setMinManaLoss(life);
		arrow.setManaLossPerTick(1000);
		arrow.setGravity(0F);
		arrow.setDeltaMovement(player.getLookAngle().scale(SPEED));
		arrow.setSourceLens(stack.copy());
		arrow.setDamage(damage);
		return arrow;
	}

	@Override
	public void apply(ItemStack stack, BurstProperties props, Level level) {}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		// 1.16 的魔法箭不和方块碰撞。 / The magic arrow of 1.16 does not collide with blocks.
		return false;
	}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Projectile entity = burst.entity();
		if (entity.level().isClientSide()) {
			return;
		}
		Entity thrower = entity.getOwner();
		if (!(thrower instanceof Player player) || !thrower.isAlive()) {
			entity.discard();
			return;
		}
		AABB axis = new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.xOld, entity.yOld, entity.zOld).inflate(ATTACK_BOX_RADIUS);
		List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, axis).stream()
				.filter(e -> e != thrower)
				.filter(e -> !(e instanceof Player other && !player.canHarmPlayer(other)))
				.toList();
		float damage = entity instanceof MagicArrowEntity arrow ? arrow.getDamage() : 0F;
		for (LivingEntity living : entities) {
			DamageSource source = ExtraBotanyDamageTypes.Sources.piercingDamage(entity.level().registryAccess(), player);
			living.hurt(source, damage);
			if (living instanceof Skeleton skeleton && !skeleton.isAlive()) {
				float fx = (float) (player.getX() - living.getX());
				float fz = (float) (player.getZ() - living.getZ());
				if (Math.sqrt(fx * fx + fz * fz) > ADVANCEMENT_REQUIRE) {
					PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.HUNDRED_BLOCK_PIERCE), "code_triggered");
				}
			}
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	/** 拉弓动画的速度：和原版的弓一样，20 tick 拉满。 / The speed of the draw animation: as a vanilla bow, full after 20 ticks. */
	public float chargeVelocityMultiplier(ItemStack itemStack, LivingEntity livingEntity) {
		return 1F;
	}

	@Override
	public boolean doParticles(ManaBurst burst, ItemStack stack) {
		return true;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return 0;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
	}

	@Override
	public boolean isValidRepairItem(ItemStack bow, ItemStack material) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.failnaught").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal(""));
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}
}
