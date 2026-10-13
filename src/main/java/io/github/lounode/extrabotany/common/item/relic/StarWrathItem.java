package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.FallingStarEntity;

/**
 * 狂星之怒。左键消耗 500 点魔力，让 5 颗流星落向目标。
 * Star Wrath. The attack key uses 500 mana and makes 5 stars fall on the target.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemStarWrath}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemStarWrath}.
 */
public class StarWrathItem extends RelicSwordItem {
	public static final int MANA_PER_USE = 500;
	private static final double AIM_RANGE = 64;
	private static final int STAR_COUNT = 5;
	private static final double IMPACT_SPREAD = 6;
	private static final double START_SPREAD = 18;
	private static final double START_HEIGHT = 24;
	private static final double STAR_SPEED = 1.5;

	public StarWrathItem(Properties props) {
		super(Tiers.DIAMOND, props.attributes(createAttributes(Tiers.DIAMOND, 6, -1.6F)));
	}

	@Override
	protected boolean useSkill(Player player, ItemStack stack, @Nullable Entity target) {
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return false;
		}
		attackEntity(player, target);
		return true;
	}

	/**
	 * 召唤流星。不消耗魔力。Boss 也用这个方法攻击。
	 * Calls the falling stars. This method takes no mana. A boss also attacks with this method.
	 */
	public void attackEntity(LivingEntity user, @Nullable Entity target) {
		Vec3 aim = aimPosition(user, target, AIM_RANGE, true);
		RandomSource random = user.getRandom();
		for (int i = 0; i < STAR_COUNT; i++) {
			Vec3 impact = aim.add((0.5 - random.nextDouble()) * IMPACT_SPREAD, 0, (0.5 - random.nextDouble()) * IMPACT_SPREAD);
			Vec3 offset = new Vec3(
					(0.5 * random.nextDouble() - 0.25) * START_SPREAD,
					START_HEIGHT,
					(0.5 * random.nextDouble() - 0.25) * START_SPREAD);
			FallingStarEntity star = new FallingStarEntity(user, user.level());
			star.setPos(impact.add(offset));
			star.setDeltaMovement(offset.normalize().scale(-STAR_SPEED));
			user.level().addFreshEntity(star);
		}
	}
}
