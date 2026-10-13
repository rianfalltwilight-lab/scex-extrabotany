package io.github.lounode.extrabotany.client.renderer.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import vazkii.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;

import io.github.lounode.extrabotany.client.renderer.blockentity.CocoonOfDesireRenderer;
import io.github.lounode.extrabotany.client.renderer.blockentity.LivingrockBarrelRenderer;
import io.github.lounode.extrabotany.client.renderer.blockentity.ManaChargerRenderer;
import io.github.lounode.extrabotany.client.renderer.blockentity.PedestalRenderer;
import io.github.lounode.extrabotany.client.renderer.blockentity.PowerFrameRenderer;
import io.github.lounode.extrabotany.client.renderer.blockentity.QuantumManaBufferRenderer;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public final class EntityRenderers {
	public interface EntityRendererConsumer {
		<E extends Entity> void accept(EntityType<? extends E> entityType,
				EntityRendererProvider<E> entityRendererFactory);
	}

	public interface BERConsumer {
		<E extends BlockEntity> void register(BlockEntityType<E> type, BlockEntityRendererProvider<? super E> factory);
	}

	public static void registerEntityRenderers(EntityRendererConsumer consumer) {
		consumer.accept(ExtraBotanyEntityType.VOID_HERRSCHER, VoidHerrscherRenderer::new);
		consumer.accept(ExtraBotanyEntityType.EGO_MINION, EgoMinionRenderer::new);
		consumer.accept(ExtraBotanyEntityType.EGO_LANDMINE, EgoLandmineRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SUBSPACE_LANCE, SubspaceLanceRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SWORD_DOMAIN, SwordDomainRenderer::new);
		consumer.accept(ExtraBotanyEntityType.VOID_FIELD, VoidFieldRenderer::new);
		consumer.accept(ExtraBotanyEntityType.UFO, UfoRenderer::new);
		consumer.accept(ExtraBotanyEntityType.MOTOR, MotorRenderer::new);
		consumer.accept(ExtraBotanyEntityType.KEY_OF_TRUTH, KeyOfTruthRenderer::new);
		consumer.accept(ExtraBotanyEntityType.THUNDER_SLASH, ThunderSlashRenderer::new);
		consumer.accept(ExtraBotanyEntityType.FLAMESCION_SLASH, FlamescionSlashRenderer::new);
		consumer.accept(ExtraBotanyEntityType.FLAMESCION_VOID, GlyphEffectRenderer::flameVoid);
		consumer.accept(ExtraBotanyEntityType.FLAMESCION_ULT, GlyphEffectRenderer::ultimate);
		consumer.accept(ExtraBotanyEntityType.FLAMESCION_SWORD, GlyphEffectRenderer::sword);
		consumer.accept(ExtraBotanyEntityType.STRENGTHEN_SLASH, StrengthenSlashRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SUBSPACE, SubspaceRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SUBSPACE_SPEAR, SubspaceSpearRenderer::new);
		consumer.accept(ExtraBotanyEntityType.JUDAH_OATH, JudahOathRenderer::new);
		consumer.accept(ExtraBotanyEntityType.JUDAH_SPEAR, JudahSpearRenderer::new);
		// 樱花的誓约的剑只用粒子显示。
		// Only particles show the swords of the Oath of Sakura.
		consumer.accept(ExtraBotanyEntityType.JUDAH_SWORD, NoopRenderer::new);
		consumer.accept(ExtraBotanyEntityType.FLOWER_WEAPON, FlowerWeaponRenderer::new);
		consumer.accept(ExtraBotanyEntityType.TRUE_TERRABLADE_PROJECTILE,
				context -> new SwordProjectileRenderer<>(context, () -> ExtraBotanyItems.trueTerrablade));
		consumer.accept(ExtraBotanyEntityType.TRUE_SHADOW_KATANA_PROJECTILE,
				context -> new SwordProjectileRenderer<>(context, () -> ExtraBotanyItems.trueShadowKatana));
		consumer.accept(ExtraBotanyEntityType.INFLUX_WAVER_PROJECTILE,
				context -> new SwordProjectileRenderer<>(context, () -> ExtraBotanyItems.influxWaver));
		consumer.accept(ExtraBotanyEntityType.PHANTOM_SWORD, PhantomSwordRenderer::new);
		consumer.accept(ExtraBotanyEntityType.BUTTERFLY_PROJECTILE, ButterflyProjectileRenderer::new);
		consumer.accept(ExtraBotanyEntityType.BOTTLED_STAR, BottledStarRenderer::new);
		consumer.accept(ExtraBotanyEntityType.PHOTON_SHOTGUN_PROJECTILE, ThrownItemRenderer::new);
		consumer.accept(ExtraBotanyEntityType.PET_PIXIE, PetPixieRenderer::new);
		consumer.accept(ExtraBotanyEntityType.AURA_FIRE, NoopRenderer::new);
		consumer.accept(ExtraBotanyEntityType.MAGIC_LANDMINE, MagicLandMineRenderer::new);
		consumer.accept(ExtraBotanyEntityType.GAIA_LEGACY, GaiaRenderer::new);
		consumer.accept(ExtraBotanyEntityType.GAIA_III, GaiaRenderer::new);
		consumer.accept(ExtraBotanyEntityType.EGO, EgoRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SKULL_MISSILE, SkullMissileRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SKULL_LANDMINE_BLUE, SkullLandMineRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SKULL_LANDMINE_RED, SkullLandMineRenderer::new);
		consumer.accept(ExtraBotanyEntityType.SKULL_LANDMINE_GREEN, SkullLandMineRenderer::new);
		consumer.accept(ExtraBotanyEntityType.HOLY_WATER_GRENADE, ThrownItemRenderer::new);
	}

	public static void registerBlockEntityRenderers(BERConsumer consumer) {
		consumer.register(ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER, QuantumManaBufferRenderer::new);
		consumer.register(ExtraBotanyBlockEntities.COCOON_OF_DESIRE, CocoonOfDesireRenderer::new);
		consumer.register(ExtraBotanyBlockEntities.LIVINGROCK_BARREL, LivingrockBarrelRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.STARDUST_LOTUS, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtraBotanyBlockEntities.PEDESTAL, PedestalRenderer::new);
		consumer.register(ExtraBotanyBlockEntities.MANA_CHARGER, ManaChargerRenderer::new);
		consumer.register(ExtraBotanyBlockEntities.POWER_FRAME, PowerFrameRenderer::new);
		//Flowers
		consumer.register(ExtrabotanyFlowerBlocks.TRADE_ORCHID, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.WOODIENIA, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.REIKARLILY, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.BELLFLOWER, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.ANNOYINGFLOWER, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.STONESIA, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.EDELWEISS, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.RESONCUND, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.SUNSHINE_LILY, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.MOONLIGHT_LILY, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.SERENITIAN, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.TWINSTAR, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.OMNIVIOLET, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.TINKLE, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.BLOOD_ENCHANTRESS, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.MIRROWTUNIA, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.NECROFLEUR, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.NECROFLEUR_CHIBI, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.MANALINK, SpecialFlowerBlockEntityRenderer::new);
		consumer.register(ExtrabotanyFlowerBlocks.ENCHANTER, SpecialFlowerBlockEntityRenderer::new);

	}
}
