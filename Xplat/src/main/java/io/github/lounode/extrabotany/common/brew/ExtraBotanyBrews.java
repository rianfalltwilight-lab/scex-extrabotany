package io.github.lounode.extrabotany.common.brew;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import vazkii.botania.api.brew.Brew;

import io.github.lounode.extrabotany.common.lib.LibBrewNames;

import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyBrews {

	public final static Brew manaCocktail = new Brew(0x59B7FF, 25000,
			new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60),
			new MobEffectInstance(MobEffects.JUMP, 20 * 60),
			new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60),
			new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60)
	).setNotBloodPendantInfusable().setNotIncenseInfusable();
	public final static Brew revolution = new Brew(0x483D8B, 10000,
			new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 90, 2)
	);
	public final static Brew shield = new Brew(0x006400, 10000,
			new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 60, 2),
			new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 2)
	);
	public final static Brew allInOne = new Brew(0xFFD700, 30000,
			new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 45, 0),
			new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 45, 0),
			new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 45, 0),
			new MobEffectInstance(MobEffects.JUMP, 20 * 45, 0),
			new MobEffectInstance(MobEffects.REGENERATION, 20 * 45, 0),
			new MobEffectInstance(MobEffects.ABSORPTION, 20 * 45, 0),
			new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 45, 0),
			new MobEffectInstance(MobEffects.LUCK, 20 * 45, 0)
	);
	public final static Brew deadpool = new Brew(0xFF4500, 20000,
			new MobEffectInstance(MobEffects.WITHER, 20 * 15, 1),
			new MobEffectInstance(MobEffects.POISON, 20 * 15, 1),
			new MobEffectInstance(MobEffects.GLOWING, 20 * 90, 2),
			new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 90, 2)
	);
	public final static Brew floating = new Brew(0x00CED1, 6000,
			new MobEffectInstance(MobEffects.LEVITATION, 20 * 8, 2)
	);

	public static void submitRegistrations(BiConsumer<Brew, ResourceLocation> r) {
		r.accept(manaCocktail, prefix(LibBrewNames.MANA_COCKTAIL));
		r.accept(revolution, prefix(LibBrewNames.REVOLUTION));
		r.accept(shield, prefix(LibBrewNames.SHIELD));
		r.accept(allInOne, prefix(LibBrewNames.ALL_IN_ONE));
		r.accept(deadpool, prefix(LibBrewNames.DEADPOOL));
		r.accept(floating, prefix(LibBrewNames.FLOATING));
	}

}
