package io.github.lounode.extrabotany.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.lib.LibEntityNames;

import java.util.function.BiConsumer;

public class ExtraBotanyEntityType {
	public static final EntityType<AuraFireEntity> AURA_FIRE = EntityType.Builder.<AuraFireEntity>of(
			AuraFireEntity::new, MobCategory.MISC)
			.sized(0, 0)
			.noSummon()
			.updateInterval(10)
			.clientTrackingRange(10)
			.build(LibEntityNames.AURA_FIRE.toString());

	public static final EntityType<Gaia> GAIA_LEGACY = EntityType.Builder.<Gaia>of(
			Gaia::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(LibEntityNames.GAIA_LEGACY.toString());

	public static final EntityType<MagicLandMineEntity> MAGIC_LANDMINE = EntityType.Builder.<MagicLandMineEntity>of(
			MagicLandMineEntity::new, MobCategory.MISC)
			.sized(5F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.MAGIC_LANDMINE.toString());

	public static final EntityType<GaiaIII> GAIA_III = EntityType.Builder.<GaiaIII>of(
			GaiaIII::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(LibEntityNames.GAIA_III.toString());

	public static final EntityType<SkullMissileEntity> SKULL_MISSILE = EntityType.Builder.<SkullMissileEntity>of(
			SkullMissileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.SKULL_MISSILE.toString());

	public static final EntityType<SkullLandMineEntity.Default> SKULL_LANDMINE_BLUE = EntityType.Builder.<SkullLandMineEntity.Default>of(
			SkullLandMineEntity.Default::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_BLUE.toString());

	public static final EntityType<SkullLandMineEntity.Danger> SKULL_LANDMINE_RED = EntityType.Builder.<SkullLandMineEntity.Danger>of(
			SkullLandMineEntity.Danger::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_RED.toString());

	public static final EntityType<SkullLandMineEntity.Disarm> SKULL_LANDMINE_GREEN = EntityType.Builder.<SkullLandMineEntity.Disarm>of(
			SkullLandMineEntity.Disarm::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_GREEN.toString());

	public static final EntityType<HolyWaterGrenadeEntity> HOLY_WATER_GRENADE = EntityType.Builder.<HolyWaterGrenadeEntity>of(
			HolyWaterGrenadeEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(LibEntityNames.HOLY_WATER_GRENADE.toString());

	public static void registerEntities(BiConsumer<EntityType<?>, ResourceLocation> r) {
		r.accept(io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher.TYPE, ResourceLocation.parse("extrabotany:void_herrscher"));
		r.accept(LegacyEgoMinion.TYPE, ResourceLocation.parse("extrabotany:ego_minion"));
		r.accept(LegacyLance.TYPE, ResourceLocation.parse("extrabotany:subspace_lance"));
		r.accept(LegacySwordDomain.TYPE, ResourceLocation.parse("extrabotany:sword_domain"));
		r.accept(LegacyVoidField.TYPE, ResourceLocation.parse("extrabotany:void_field"));
		LegacyFlameArea.TYPES.forEach((kind, type) -> r.accept(type, ResourceLocation.fromNamespaceAndPath("extrabotany", kind.id)));
		r.accept(LegacyFlameProjectile.STRENGTHEN, ResourceLocation.parse("extrabotany:strengthen_slash"));
		r.accept(LegacyFlameProjectile.SWORD, ResourceLocation.parse("extrabotany:flamescion_sword"));
		r.accept(LegacySubspace.TYPE, ResourceLocation.parse("extrabotany:subspace"));
		r.accept(LegacySubspaceSpear.TYPE, ResourceLocation.parse("extrabotany:subspace_spear"));
		r.accept(LegacyJudahEntity.OATH, ResourceLocation.parse("extrabotany:judah_oath"));
		r.accept(LegacyJudahEntity.SPEAR, ResourceLocation.parse("extrabotany:judah_spear"));
		r.accept(LegacyJudahSword.TYPE, ResourceLocation.parse("extrabotany:judah_sword"));
		r.accept(LegacyPhantomSword.TYPE, ResourceLocation.parse("extrabotany:phantom_sword"));
		LegacySwordProjectile.TYPES.forEach((kind, type) -> r.accept(type, ResourceLocation.fromNamespaceAndPath("extrabotany", kind.item + "_projectile")));
		r.accept(LegacyFlowerWeapon.TYPE, ResourceLocation.parse("extrabotany:flower_weapon"));
		r.accept(LegacyMount.UFO, ResourceLocation.parse("extrabotany:ufo"));
		r.accept(LegacyMount.MOTOR, ResourceLocation.parse("extrabotany:motor"));
		LegacyProjectile.TYPES.forEach((kind, type) -> r.accept(type, ResourceLocation.fromNamespaceAndPath("extrabotany", kind.id)));
		r.accept(AURA_FIRE, LibEntityNames.AURA_FIRE);
		r.accept(MAGIC_LANDMINE, LibEntityNames.MAGIC_LANDMINE);
		r.accept(GAIA_LEGACY, LibEntityNames.GAIA_LEGACY);
		r.accept(GAIA_III, LibEntityNames.GAIA_III);
		r.accept(SKULL_MISSILE, LibEntityNames.SKULL_MISSILE);
		r.accept(SKULL_LANDMINE_BLUE, LibEntityNames.SKULL_LANDMINE_BLUE);
		r.accept(SKULL_LANDMINE_RED, LibEntityNames.SKULL_LANDMINE_RED);
		r.accept(SKULL_LANDMINE_GREEN, LibEntityNames.SKULL_LANDMINE_GREEN);
		r.accept(HOLY_WATER_GRENADE, LibEntityNames.HOLY_WATER_GRENADE);
	}

	public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> consumer) {
		consumer.accept(io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher.TYPE, io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher.createAttributes());
		consumer.accept(LegacyEgoMinion.TYPE, LegacyEgoMinion.createAttributes());
		consumer.accept(GAIA_LEGACY, Gaia.createGaiaAttributes());
		consumer.accept(GAIA_III, GaiaIII.createGaiaAttributes());

	}
}
