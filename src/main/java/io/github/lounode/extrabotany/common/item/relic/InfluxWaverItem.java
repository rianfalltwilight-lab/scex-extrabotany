package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.projectile.InfluxWaverProjectileEntity;

/**
 * 波涌之刃。左键消耗 500 点魔力，发射一道会追加打击 3 次的剑气。
 * Influx Waver. The attack key uses 500 mana and shoots a beam that strikes 3 more times.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemInfluxWaver}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemInfluxWaver}.
 */
public class InfluxWaverItem extends RelicSwordItem {
	public static final int MANA_PER_USE = 500;
	private static final double AIM_RANGE = 64;
	private static final double BEAM_SPEED = 0.7;
	private static final int EXTRA_STRIKES = 3;

	public InfluxWaverItem(Properties props) {
		super(Tiers.DIAMOND, props.attributes(createAttributes(Tiers.DIAMOND, 5, -2F)));
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
	 * 发射剑气。不消耗魔力。Boss 也用这个方法攻击。
	 * Shoots the beam. This method takes no mana. A boss also attacks with this method.
	 */
	public void attackEntity(LivingEntity user, @Nullable Entity target) {
		Vec3 aim = aimPosition(user, target, AIM_RANGE, true);
		Vec3 start = user.position().add(0, 1.1, 0);
		user.level().addFreshEntity(
				new InfluxWaverProjectileEntity(user.level(), user, start, aim, BEAM_SPEED, EXTRA_STRIKES));
	}
}
