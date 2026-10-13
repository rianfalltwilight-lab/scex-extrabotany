package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.FirstFractalItem;
import io.github.lounode.extrabotany.common.item.relic.InfluxWaverItem;
import io.github.lounode.extrabotany.common.item.relic.StarWrathItem;
import io.github.lounode.extrabotany.common.item.relic.TrueShadowKatanaItem;
import io.github.lounode.extrabotany.common.item.relic.TrueTerrabladeItem;

/**
 * 本我和本我仆从使用的武器攻击。每种武器的攻击就是对应遗物剑的技能，不消耗魔力。
 * The weapon attacks of the Ego and of the Ego Minions. The attack of each weapon is the skill of the related relic sword, with no mana cost.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityEGO#tryAttack}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityEGO#tryAttack}.
 */
public final class EgoWeaponFire {
	// 这些编号和原版相同。随从的种类也用它们。
	// These numbers are the same as in the original. The minion types also use them.
	public static final int WEAPON_TRUE_SHADOW_KATANA = 0;
	public static final int WEAPON_TRUE_TERRABLADE = 1;
	public static final int WEAPON_INFLUX_WAVER = 2;
	public static final int WEAPON_STAR_WRATH = 3;
	public static final int WEAPON_FIRST_FRACTAL = 4;

	private EgoWeaponFire() {}

	/**
	 * 返回这个编号对应的遗物剑。它只用来显示。
	 * Returns the relic sword that has this number. It is only for display.
	 */
	public static ItemStack getWeapon(int weapon) {
		return new ItemStack(switch (weapon) {
			case WEAPON_TRUE_SHADOW_KATANA -> ExtraBotanyItems.trueShadowKatana;
			case WEAPON_TRUE_TERRABLADE -> ExtraBotanyItems.trueTerrablade;
			case WEAPON_INFLUX_WAVER -> ExtraBotanyItems.influxWaver;
			case WEAPON_STAR_WRATH -> ExtraBotanyItems.starWrath;
			default -> ExtraBotanyItems.firstFractal;
		});
	}

	/**
	 * 让射手用指定的武器攻击目标。只在服务端生效。
	 * Makes the shooter attack the target with the given weapon. The method only has an effect on the server.
	 */
	public static void fire(LivingEntity shooter, LivingEntity target, int weapon) {
		if (shooter.level().isClientSide()) {
			return;
		}
		switch (weapon) {
			case WEAPON_TRUE_TERRABLADE -> ((TrueTerrabladeItem) ExtraBotanyItems.trueTerrablade).attackEntity(shooter, target);
			case WEAPON_TRUE_SHADOW_KATANA -> ((TrueShadowKatanaItem) ExtraBotanyItems.trueShadowKatana).attackEntity(shooter, target);
			case WEAPON_INFLUX_WAVER -> ((InfluxWaverItem) ExtraBotanyItems.influxWaver).attackEntity(shooter, target);
			case WEAPON_STAR_WRATH -> ((StarWrathItem) ExtraBotanyItems.starWrath).attackEntity(shooter, target);
			default -> ((FirstFractalItem) ExtraBotanyItems.firstFractal).attackEntity(shooter, target);
		}
	}
}