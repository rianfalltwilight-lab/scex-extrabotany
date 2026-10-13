package io.github.lounode.extrabotany.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSlashEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSwordEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionVoidEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.StrengthenSlashEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.entity.judah.JudahOathEntity;
import io.github.lounode.extrabotany.common.entity.judah.JudahSpearEntity;
import io.github.lounode.extrabotany.common.entity.judah.JudahSwordEntity;
import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;
import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;
import io.github.lounode.extrabotany.common.entity.projectile.BottledStarEntity;
import io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity;
import io.github.lounode.extrabotany.common.entity.projectile.ButterflyProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.InfluxWaverProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.entity.projectile.PhotonProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.TrueShadowKatanaProjectileEntity;
import io.github.lounode.extrabotany.common.entity.projectile.TrueTerrabladeProjectileEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity;
import io.github.lounode.extrabotany.common.lib.LibEntityNames;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyEntityType {
	// 来自原版（ExtraMeteorP）的投射物和召唤物。注册 ID 和旧存档保持一致。
	// Projectiles and summoned entities from the original (ExtraMeteorP). The registry IDs agree with old saves.
	public static final EntityType<TrueTerrabladeProjectileEntity> TRUE_TERRABLADE_PROJECTILE = EntityType.Builder
			.<TrueTerrabladeProjectileEntity>of(TrueTerrabladeProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":true_terrablade_projectile");
	public static final EntityType<TrueShadowKatanaProjectileEntity> TRUE_SHADOW_KATANA_PROJECTILE = EntityType.Builder
			.<TrueShadowKatanaProjectileEntity>of(TrueShadowKatanaProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":true_shadow_katana_projectile");
	public static final EntityType<InfluxWaverProjectileEntity> INFLUX_WAVER_PROJECTILE = EntityType.Builder
			.<InfluxWaverProjectileEntity>of(InfluxWaverProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":influx_waver_projectile");
	public static final EntityType<PhantomSwordEntity> PHANTOM_SWORD = EntityType.Builder
			.<PhantomSwordEntity>of(PhantomSwordEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":phantom_sword");
	public static final EntityType<ButterflyProjectileEntity> BUTTERFLY_PROJECTILE = EntityType.Builder
			.<ButterflyProjectileEntity>of(ButterflyProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":butterfly_projectile");
	public static final EntityType<BottledStarEntity> BOTTLED_STAR = EntityType.Builder
			.<BottledStarEntity>of(BottledStarEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":bottled_star");
	public static final EntityType<PhotonProjectileEntity> PHOTON_SHOTGUN_PROJECTILE = EntityType.Builder
			.<PhotonProjectileEntity>of(PhotonProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":photon_shotgun_projectile");
	public static final EntityType<PetPixieEntity> PET_PIXIE = EntityType.Builder
			.<PetPixieEntity>of(PetPixieEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(3)
			.build(LibMisc.MOD_ID + ":pet_pixie");

	// 坐骑的碰撞箱尺寸来自原版。
	// The collision box sizes of the mounts come from the original.
	public static final EntityType<MotorEntity> MOTOR = EntityType.Builder
			.<MotorEntity>of(MotorEntity::new, MobCategory.MISC)
			.sized(1.675F, 1.875F)
			.clientTrackingRange(10)
			.updateInterval(3)
			.build(LibMisc.MOD_ID + ":motor");
	public static final EntityType<UfoEntity> UFO = EntityType.Builder
			.<UfoEntity>of(UfoEntity::new, MobCategory.MISC)
			.sized(1.5F, 1.5F)
			.clientTrackingRange(10)
			.updateInterval(3)
			.build(LibMisc.MOD_ID + ":ufo");
	public static final EntityType<KeyOfTruthEntity> KEY_OF_TRUTH = EntityType.Builder
			.<KeyOfTruthEntity>of(KeyOfTruthEntity::new, MobCategory.MISC)
			.sized(0.1F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(10)
			.build(LibMisc.MOD_ID + ":key_of_truth");
	public static final EntityType<ThunderSlashEntity> THUNDER_SLASH = EntityType.Builder
			.<ThunderSlashEntity>of(ThunderSlashEntity::new, MobCategory.MISC)
			.sized(0.1F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(10)
			.build(LibMisc.MOD_ID + ":thunder_slash");

	// 薪炎的效果实体。
	// The effect entities of the Flamescion.
	public static final EntityType<FlamescionSlashEntity> FLAMESCION_SLASH = EntityType.Builder
			.<FlamescionSlashEntity>of(FlamescionSlashEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":flamescion_slash");
	public static final EntityType<FlamescionVoidEntity> FLAMESCION_VOID = EntityType.Builder
			.<FlamescionVoidEntity>of(FlamescionVoidEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":flamescion_void");
	public static final EntityType<FlamescionUltEntity> FLAMESCION_ULT = EntityType.Builder
			.<FlamescionUltEntity>of(FlamescionUltEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":flamescion_ult");
	public static final EntityType<StrengthenSlashEntity> STRENGTHEN_SLASH = EntityType.Builder
			.<StrengthenSlashEntity>of(StrengthenSlashEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":strengthen_slash");
	public static final EntityType<FlamescionSwordEntity> FLAMESCION_SWORD = EntityType.Builder
			.<FlamescionSwordEntity>of(FlamescionSwordEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":flamescion_sword");

	// 亚空之矛和犹大的誓约的效果实体。
	// The effect entities of the Spear of Subspace and of the Judah Oath.
	public static final EntityType<SubspaceEntity> SUBSPACE = EntityType.Builder
			.<SubspaceEntity>of(SubspaceEntity::new, MobCategory.MISC)
			.sized(0.1F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":subspace");
	// 画出来的矛很长，但是它的截面大约是半格。飞行本身扫过矛的长度方向。
	// The spear on the screen is long, but its cross-section is approximately half a block. The flight covers the length of the spear.
	public static final EntityType<SubspaceSpearEntity> SUBSPACE_SPEAR = EntityType.Builder
			.<SubspaceSpearEntity>of(SubspaceSpearEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":subspace_spear");
	public static final EntityType<JudahOathEntity> JUDAH_OATH = EntityType.Builder
			.<JudahOathEntity>of(JudahOathEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":judah_oath");
	public static final EntityType<JudahSpearEntity> JUDAH_SPEAR = EntityType.Builder
			.<JudahSpearEntity>of(JudahSpearEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":judah_spear");
	public static final EntityType<JudahSwordEntity> JUDAH_SWORD = EntityType.Builder
			.<JudahSwordEntity>of(JudahSwordEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":judah_sword");

	public static final EntityType<VoidHerrscher> VOID_HERRSCHER = EntityType.Builder
			.<VoidHerrscher>of(VoidHerrscher::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(LibMisc.MOD_ID + ":void_herrscher");
	// 这三个实体不和别的东西碰撞。它们的尺寸只用来决定是否绘制。
	// These three entities do not collide with other things. Their size is only used to find if they are drawn.
	public static final EntityType<SubspaceLanceEntity> SUBSPACE_LANCE = EntityType.Builder
			.<SubspaceLanceEntity>of(SubspaceLanceEntity::new, MobCategory.MISC)
			.sized(0.5F, 2F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":subspace_lance");
	public static final EntityType<SwordDomainEntity> SWORD_DOMAIN = EntityType.Builder
			.<SwordDomainEntity>of(SwordDomainEntity::new, MobCategory.MISC)
			.sized(0.5F, 2F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":sword_domain");
	public static final EntityType<VoidFieldEntity> VOID_FIELD = EntityType.Builder
			.<VoidFieldEntity>of(VoidFieldEntity::new, MobCategory.MISC)
			.sized(3F, 2F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":void_field");

	public static final EntityType<EgoMinionEntity> EGO_MINION = EntityType.Builder
			.<EgoMinionEntity>of(EgoMinionEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(3)
			.build(LibMisc.MOD_ID + ":ego_minion");
	public static final EntityType<EgoLandmineEntity> EGO_LANDMINE = EntityType.Builder
			.<EgoLandmineEntity>of(EgoLandmineEntity::new, MobCategory.MISC)
			.sized(3F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibMisc.MOD_ID + ":ego_landmine");

	public static final EntityType<FlowerWeaponEntity> FLOWER_WEAPON = EntityType.Builder
			.<FlowerWeaponEntity>of(FlowerWeaponEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibMisc.MOD_ID + ":flower_weapon");
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

	// 本我。来源：ExtraMeteorP 1.16 的实体 ego。
	// The Ego. Source: the entity ego of ExtraMeteorP 1.16.
	public static final EntityType<Ego> EGO = EntityType.Builder.<Ego>of(
			Ego::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.clientTrackingRange(10)
			.updateInterval(2)
			.build(LibEntityNames.EGO.toString());

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
		r.accept(VOID_HERRSCHER, prefix("void_herrscher"));
		r.accept(EGO, LibEntityNames.EGO);
		r.accept(EGO_MINION, prefix("ego_minion"));
		r.accept(EGO_LANDMINE, prefix("ego_landmine"));
		r.accept(SUBSPACE_LANCE, prefix("subspace_lance"));
		r.accept(SWORD_DOMAIN, prefix("sword_domain"));
		r.accept(VOID_FIELD, prefix("void_field"));
		r.accept(FLAMESCION_SLASH, prefix("flamescion_slash"));
		r.accept(FLAMESCION_VOID, prefix("flamescion_void"));
		r.accept(FLAMESCION_ULT, prefix("flamescion_ult"));
		r.accept(STRENGTHEN_SLASH, prefix("strengthen_slash"));
		r.accept(FLAMESCION_SWORD, prefix("flamescion_sword"));
		r.accept(SUBSPACE, prefix("subspace"));
		r.accept(SUBSPACE_SPEAR, prefix("subspace_spear"));
		r.accept(JUDAH_OATH, prefix("judah_oath"));
		r.accept(JUDAH_SPEAR, prefix("judah_spear"));
		r.accept(JUDAH_SWORD, prefix("judah_sword"));
		r.accept(FLOWER_WEAPON, prefix("flower_weapon"));
		r.accept(UFO, prefix("ufo"));
		r.accept(MOTOR, prefix("motor"));
		r.accept(KEY_OF_TRUTH, prefix("key_of_truth"));
		r.accept(THUNDER_SLASH, prefix("thunder_slash"));
		r.accept(TRUE_TERRABLADE_PROJECTILE, prefix("true_terrablade_projectile"));
		r.accept(TRUE_SHADOW_KATANA_PROJECTILE, prefix("true_shadow_katana_projectile"));
		r.accept(INFLUX_WAVER_PROJECTILE, prefix("influx_waver_projectile"));
		r.accept(PHANTOM_SWORD, prefix("phantom_sword"));
		r.accept(BUTTERFLY_PROJECTILE, prefix("butterfly_projectile"));
		r.accept(BOTTLED_STAR, prefix("bottled_star"));
		r.accept(PHOTON_SHOTGUN_PROJECTILE, prefix("photon_shotgun_projectile"));
		r.accept(PET_PIXIE, prefix("pet_pixie"));
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
		consumer.accept(VOID_HERRSCHER, VoidHerrscher.createAttributes());
		consumer.accept(EGO, Ego.createAttributes());
		consumer.accept(EGO_MINION, EgoMinionEntity.createAttributes());
		consumer.accept(PET_PIXIE, PetPixieEntity.createAttributes());
		consumer.accept(GAIA_LEGACY, Gaia.createGaiaAttributes());
		consumer.accept(GAIA_III, GaiaIII.createGaiaAttributes());

	}
}
