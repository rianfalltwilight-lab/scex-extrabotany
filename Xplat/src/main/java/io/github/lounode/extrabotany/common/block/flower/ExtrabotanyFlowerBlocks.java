package io.github.lounode.extrabotany.common.block.flower;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;
import vazkii.botania.common.block.flower.PoweredFloatingSpecialFlowerBlock;
import vazkii.botania.common.block.flower.PoweredSpecialFlowerBlock;
import vazkii.botania.common.block.flower.SpecialFlowerBlock;
import vazkii.botania.common.brew.BotaniaMobEffects;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.*;
import io.github.lounode.extrabotany.common.block.flower.generating.*;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibBlockNames;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExtrabotanyFlowerBlocks {
	public static final BlockEntityType<StardustLotusBlockEntity> STARDUST_LOTUS = EXplatAbstractions.INSTANCE.createBlockEntityType(StardustLotusBlockEntity::new, LegacyLotusBlocks.FLOWER, LegacyLotusBlocks.FLOATING);

	private static final BlockBehaviour.Properties FLOWER_PROPS = BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.WHITE_MYSTICAL_FLOWER);
	private static final BlockBehaviour.Properties FLOATING_PROPS = BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.WHITE_FLOATING_FLOWER);

	public static final Block tradeOrchid = new PoweredSpecialFlowerBlock(MobEffects.HERO_OF_THE_VILLAGE, 5 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.TRADE_ORCHID);
	public static final Block tradeOrchidFloating = new PoweredFloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.TRADE_ORCHID);
	public static final Block tradeOrchidPotted = ExtraBotanyBlocks.flowerPot(tradeOrchid, 0);

	public static final Block woodienia = new PoweredSpecialFlowerBlock(MobEffects.HUNGER, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.WOODIENIA);
	public static final Block woodieniaFloating = new PoweredFloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.WOODIENIA);
	public static final Block woodieniaPotted = ExtraBotanyBlocks.flowerPot(woodienia, 0);

	public static final Block reikarlily = createSpecialFlowerBlock(MobEffects.FIRE_RESISTANCE, 30 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.REIKARLILY);
	public static final Block reikarlilyFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.REIKARLILY);
	public static final Block reikarlilyPotted = ExtraBotanyBlocks.flowerPot(reikarlily, 0);

	public static final Block bellflower = createSpecialFlowerBlock(MobEffects.MOVEMENT_SPEED, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.BELLFLOWER);
	public static final Block bellflowerFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.BELLFLOWER);
	public static final Block bellflowerPotted = ExtraBotanyBlocks.flowerPot(bellflower, 0);

	public static final Block annoyingflower = createSpecialFlowerBlock(MobEffects.LUCK, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.ANNOYINGFLOWER);
	public static final Block annoyingflowerFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.ANNOYINGFLOWER);
	public static final Block annoyingflowerPotted = ExtraBotanyBlocks.flowerPot(annoyingflower, 0);

	public static final Block stonesia = createSpecialFlowerBlock(MobEffects.DIG_SPEED, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.STONESIA);
	public static final Block stonesiaFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.STONESIA);
	public static final Block stonesiaPotted = ExtraBotanyBlocks.flowerPot(stonesia, 0);

	public static final Block edelweiss = createSpecialFlowerBlock(ExtraBotanyMobEffects.WARM, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.EDELWEISS);
	public static final Block edelweissFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.EDELWEISS);
	public static final Block edelweissPotted = ExtraBotanyBlocks.flowerPot(edelweiss, 0);

	public static final Block resoncund = createSpecialFlowerBlock(MobEffects.MOVEMENT_SPEED, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.RESONCUND);
	public static final Block resoncundFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.RESONCUND);
	public static final Block resoncundPotted = ExtraBotanyBlocks.flowerPot(resoncund, 0);

	public static final Block sunshineLily = createSpecialFlowerBlock(MobEffects.GLOWING, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.SUNSHINE_LILY);
	public static final Block sunshineLilyFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.SUNSHINE_LILY);
	public static final Block sunshineLilyPotted = ExtraBotanyBlocks.flowerPot(sunshineLily, 15);

	public static final Block moonlightLily = createSpecialFlowerBlock(MobEffects.BAD_OMEN, 10 * 60, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.MOONLIGHT_LILY);
	public static final Block moonlightLilyFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.MOONLIGHT_LILY);
	public static final Block moonlightLilyPotted = ExtraBotanyBlocks.flowerPot(moonlightLily, 8);

	public static final Block serenitian = createSpecialFlowerBlock(ExtraBotanyMobEffects.IMMOBILIZE, 10 * 10, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.SERENITIAN);
	public static final Block serenitianFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.SERENITIAN);
	public static final Block serenitianPotted = ExtraBotanyBlocks.flowerPot(serenitian, 0);

	public static final Block twinstar = createSpecialFlowerBlock(MobEffects.LUCK, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.TWINSTAR);
	public static final Block twinstarFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.TWINSTAR);
	public static final Block twinstarPotted = ExtraBotanyBlocks.flowerPot(twinstar, 0);

	public static final Block omniviolet = createSpecialFlowerBlock(MobEffects.NIGHT_VISION, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.OMNIVIOLET);
	public static final Block omnivioletFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.OMNIVIOLET);
	public static final Block omnivioletPotted = ExtraBotanyBlocks.flowerPot(omniviolet, 0);

	public static final Block tinkle = createSpecialFlowerBlock(MobEffects.JUMP, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.TINKLE);
	public static final Block tinkleFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.TINKLE);
	public static final Block tinklePotted = ExtraBotanyBlocks.flowerPot(tinkle, 0);

	public static final Block bloodEnchantress = createSpecialFlowerBlock(BotaniaMobEffects.BLOODTHIRST, 10 * 60, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.BLOOD_ENCHANTRESS);
	public static final Block bloodEnchantressFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.BLOOD_ENCHANTRESS);
	public static final Block bloodEnchantressPotted = ExtraBotanyBlocks.flowerPot(bloodEnchantress, 0);

	public static final Block mirrowtunia = createSpecialFlowerBlock(ExtraBotanyMobEffects.THIRROR, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.MIRROWTUNIA);
	public static final Block mirrowtuniaFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.MIRROWTUNIA);
	public static final Block mirrowtuniaPotted = ExtraBotanyBlocks.flowerPot(mirrowtunia, 0);

	public static final Block necrofleur = createSpecialFlowerBlock(ExtraBotanyMobEffects.HEAL_REVERSE, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.NECROFLEUR);
	public static final Block necrofleurChibi = createSpecialFlowerBlock(ExtraBotanyMobEffects.HEAL_REVERSE, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.NECROFLEUR_CHIBI);
	public static final Block necrofleurFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.NECROFLEUR);
	public static final Block necrofleurChibiFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.NECROFLEUR_CHIBI);
	public static final Block necrofleurPotted = ExtraBotanyBlocks.flowerPot(necrofleur, 0);
	public static final Block necrofleurChibiPotted = ExtraBotanyBlocks.flowerPot(necrofleurChibi, 0);

	public static final Block manalink = createSpecialFlowerBlock(MobEffects.INVISIBILITY, 10 * 20, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.MANALINK);
	public static final Block manalinkFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.MANALINK);
	public static final Block manalinkPotted = ExtraBotanyBlocks.flowerPot(manalink, 0);

	public static final Block enchanter = createSpecialFlowerBlock(MobEffects.HEAL, 0, FLOWER_PROPS, () -> ExtrabotanyFlowerBlocks.ENCHANTER);
	public static final Block enchanterFloating = new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ExtrabotanyFlowerBlocks.ENCHANTER);
	public static final Block enchanterPotted = ExtraBotanyBlocks.flowerPot(enchanter, 0);

	public static final BlockEntityType<TradeOrchidBlockEntity> TRADE_ORCHID = EXplatAbstractions.INSTANCE.createBlockEntityType(TradeOrchidBlockEntity::new, tradeOrchid, tradeOrchidFloating);
	public static final BlockEntityType<WoodieniaBlockEntity> WOODIENIA = EXplatAbstractions.INSTANCE.createBlockEntityType(WoodieniaBlockEntity::new, woodienia, woodieniaFloating);
	public static final BlockEntityType<ReikarlilyBlockEntity> REIKARLILY = EXplatAbstractions.INSTANCE.createBlockEntityType(ReikarlilyBlockEntity::new, reikarlily, reikarlilyFloating);
	public static final BlockEntityType<BellflowerBlockEntity> BELLFLOWER = EXplatAbstractions.INSTANCE.createBlockEntityType(BellflowerBlockEntity::new, bellflower, bellflowerFloating);
	public static final BlockEntityType<AnnoyingFlowerBlockEntity> ANNOYINGFLOWER = EXplatAbstractions.INSTANCE.createBlockEntityType(AnnoyingFlowerBlockEntity::new, annoyingflower, annoyingflowerFloating);
	public static final BlockEntityType<StonesiaBlockEntity> STONESIA = EXplatAbstractions.INSTANCE.createBlockEntityType(StonesiaBlockEntity::new, stonesia, stonesiaFloating);
	public static final BlockEntityType<EdelweissBlockEntity> EDELWEISS = EXplatAbstractions.INSTANCE.createBlockEntityType(EdelweissBlockEntity::new, edelweiss, edelweissFloating);
	public static final BlockEntityType<ResoncundBlockEntity> RESONCUND = EXplatAbstractions.INSTANCE.createBlockEntityType(ResoncundBlockEntity::new, resoncund, resoncundFloating);
	public static final BlockEntityType<SunshineLilyBlockEntity> SUNSHINE_LILY = EXplatAbstractions.INSTANCE.createBlockEntityType(SunshineLilyBlockEntity::new, sunshineLily, sunshineLilyFloating);
	public static final BlockEntityType<MoonlightLilyBlockEntity> MOONLIGHT_LILY = EXplatAbstractions.INSTANCE.createBlockEntityType(MoonlightLilyBlockEntity::new, moonlightLily, moonlightLilyFloating);
	public static final BlockEntityType<SerenitianBlockEntity> SERENITIAN = EXplatAbstractions.INSTANCE.createBlockEntityType(SerenitianBlockEntity::new, serenitian, serenitianFloating);
	public static final BlockEntityType<TwinstarBlockEntity> TWINSTAR = EXplatAbstractions.INSTANCE.createBlockEntityType(TwinstarBlockEntity::new, twinstar, twinstarFloating);
	public static final BlockEntityType<OmnivioletBlockEntity> OMNIVIOLET = EXplatAbstractions.INSTANCE.createBlockEntityType(OmnivioletBlockEntity::new, omniviolet, omnivioletFloating);
	public static final BlockEntityType<TinkleBlockEntity> TINKLE = EXplatAbstractions.INSTANCE.createBlockEntityType(TinkleBlockEntity::new, tinkle, tinkleFloating);
	public static final BlockEntityType<BloodEnchantressBlockEntity> BLOOD_ENCHANTRESS = EXplatAbstractions.INSTANCE.createBlockEntityType(BloodEnchantressBlockEntity::new, bloodEnchantress, bloodEnchantressFloating);
	public static final BlockEntityType<MirrowtuniaBlockEntity> MIRROWTUNIA = EXplatAbstractions.INSTANCE.createBlockEntityType(MirrowtuniaBlockEntity::new, mirrowtunia, mirrowtuniaFloating);
	public static final BlockEntityType<NecrofleurBlockEntity> NECROFLEUR = EXplatAbstractions.INSTANCE.createBlockEntityType(NecrofleurBlockEntity::new, necrofleur, necrofleurFloating);
	public static final BlockEntityType<NecrofleurBlockEntity.Mini> NECROFLEUR_CHIBI = EXplatAbstractions.INSTANCE.createBlockEntityType(NecrofleurBlockEntity.Mini::new, necrofleurChibi, necrofleurChibiFloating);
	public static final BlockEntityType<ManalinkBlockEntity> MANALINK = EXplatAbstractions.INSTANCE.createBlockEntityType(ManalinkBlockEntity::new, manalink, manalinkFloating);
	public static final BlockEntityType<EnchanterBlockEntity> ENCHANTER = EXplatAbstractions.INSTANCE.createBlockEntityType(EnchanterBlockEntity::new, enchanter, enchanterFloating);

	private static ResourceLocation floating(ResourceLocation orig) {
		return ResourceLocation.fromNamespaceAndPath(orig.getNamespace(), "floating_" + orig.getPath());
	}

	private static ResourceLocation potted(ResourceLocation orig) {
		return ResourceLocation.fromNamespaceAndPath(orig.getNamespace(), "potted_" + orig.getPath());
	}

	private static ResourceLocation chibi(ResourceLocation orig) {
		return ResourceLocation.fromNamespaceAndPath(orig.getNamespace(), orig.getPath() + "_chibi");
	}

	private static ResourceLocation getId(Block b) {
		return BuiltInRegistries.BLOCK.getKey(b);
	}

	private static FlowerBlock createSpecialFlowerBlock(
			Holder<MobEffect> effect, int effectDuration,
			BlockBehaviour.Properties props,
			Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType) {
		return new SpecialFlowerBlock(effect, effectDuration, props, beType);
	}

	public static void registerBlocks(BiConsumer<Block, ResourceLocation> r) {
		r.accept(LegacyLotusBlocks.FLOWER, ResourceLocation.parse("extrabotany:stardustlotus"));
		r.accept(LegacyLotusBlocks.FLOATING, ResourceLocation.parse("extrabotany:floating_stardustlotus"));
		r.accept(LegacyLotusBlocks.POTTED, ResourceLocation.parse("extrabotany:potted_stardustlotus"));
		r.accept(tradeOrchid, LibBlockNames.TRADE_ORCHID);
		r.accept(tradeOrchidFloating, floating(LibBlockNames.TRADE_ORCHID));
		r.accept(tradeOrchidPotted, potted(LibBlockNames.TRADE_ORCHID));

		r.accept(woodienia, LibBlockNames.WOODIENIA);
		r.accept(woodieniaFloating, floating(LibBlockNames.WOODIENIA));
		r.accept(woodieniaPotted, potted(LibBlockNames.WOODIENIA));

		r.accept(reikarlily, LibBlockNames.REIKARLILY);
		r.accept(reikarlilyFloating, floating(LibBlockNames.REIKARLILY));
		r.accept(reikarlilyPotted, potted(LibBlockNames.REIKARLILY));

		r.accept(bellflower, LibBlockNames.BELLFLOWER);
		r.accept(bellflowerFloating, floating(LibBlockNames.BELLFLOWER));
		r.accept(bellflowerPotted, potted(LibBlockNames.BELLFLOWER));

		r.accept(annoyingflower, LibBlockNames.ANNOYINGFLOWER);
		r.accept(annoyingflowerFloating, floating(LibBlockNames.ANNOYINGFLOWER));
		r.accept(annoyingflowerPotted, potted(LibBlockNames.ANNOYINGFLOWER));

		r.accept(stonesia, LibBlockNames.STONESIA);
		r.accept(stonesiaFloating, floating(LibBlockNames.STONESIA));
		r.accept(stonesiaPotted, potted(LibBlockNames.STONESIA));

		r.accept(edelweiss, LibBlockNames.EDELWEISS);
		r.accept(edelweissFloating, floating(LibBlockNames.EDELWEISS));
		r.accept(edelweissPotted, potted(LibBlockNames.EDELWEISS));

		r.accept(resoncund, LibBlockNames.RESONCUND);
		r.accept(resoncundFloating, floating(LibBlockNames.RESONCUND));
		r.accept(resoncundPotted, potted(LibBlockNames.RESONCUND));

		r.accept(sunshineLily, LibBlockNames.SUNSHINE_LILY);
		r.accept(sunshineLilyFloating, floating(LibBlockNames.SUNSHINE_LILY));
		r.accept(sunshineLilyPotted, potted(LibBlockNames.SUNSHINE_LILY));

		r.accept(moonlightLily, LibBlockNames.MOONLIGHT_LILY);
		r.accept(moonlightLilyFloating, floating(LibBlockNames.MOONLIGHT_LILY));
		r.accept(moonlightLilyPotted, potted(LibBlockNames.MOONLIGHT_LILY));

		r.accept(serenitian, LibBlockNames.SERENITIAN);
		r.accept(serenitianFloating, floating(LibBlockNames.SERENITIAN));
		r.accept(serenitianPotted, potted(LibBlockNames.SERENITIAN));

		r.accept(twinstar, LibBlockNames.TWINSTAR);
		r.accept(twinstarFloating, floating(LibBlockNames.TWINSTAR));
		r.accept(twinstarPotted, potted(LibBlockNames.TWINSTAR));

		r.accept(omniviolet, LibBlockNames.OMNIVIOLET);
		r.accept(omnivioletFloating, floating(LibBlockNames.OMNIVIOLET));
		r.accept(omnivioletPotted, potted(LibBlockNames.OMNIVIOLET));

		r.accept(tinkle, LibBlockNames.TINKLE);
		r.accept(tinkleFloating, floating(LibBlockNames.TINKLE));
		r.accept(tinklePotted, potted(LibBlockNames.TINKLE));

		r.accept(bloodEnchantress, LibBlockNames.BLOOD_ENCHANTRESS);
		r.accept(bloodEnchantressFloating, floating(LibBlockNames.BLOOD_ENCHANTRESS));
		r.accept(bloodEnchantressPotted, potted(LibBlockNames.BLOOD_ENCHANTRESS));

		r.accept(mirrowtunia, LibBlockNames.MIRROWTUNIA);
		r.accept(mirrowtuniaFloating, floating(LibBlockNames.MIRROWTUNIA));
		r.accept(mirrowtuniaPotted, potted(LibBlockNames.MIRROWTUNIA));

		r.accept(necrofleur, LibBlockNames.NECROFLEUR);
		r.accept(necrofleurChibi, chibi(LibBlockNames.NECROFLEUR));
		r.accept(necrofleurFloating, floating(LibBlockNames.NECROFLEUR));
		r.accept(necrofleurChibiFloating, chibi(floating(LibBlockNames.NECROFLEUR)));
		r.accept(necrofleurPotted, potted(LibBlockNames.NECROFLEUR));
		r.accept(necrofleurChibiPotted, potted(chibi(LibBlockNames.NECROFLEUR)));

		r.accept(manalink, LibBlockNames.MANALINK);
		r.accept(manalinkFloating, floating(LibBlockNames.MANALINK));
		r.accept(manalinkPotted, potted(LibBlockNames.MANALINK));

		r.accept(enchanter, LibBlockNames.ENCHANTER);
		r.accept(enchanterFloating, floating(LibBlockNames.ENCHANTER));
		r.accept(enchanterPotted, potted(LibBlockNames.ENCHANTER));

	}

	public static void registerItemBlocks(BiConsumer<Item, ResourceLocation> r) {
		r.accept(new SpecialFlowerBlockItem(LegacyLotusBlocks.FLOWER, props()), getId(LegacyLotusBlocks.FLOWER));
		r.accept(new SpecialFlowerBlockItem(LegacyLotusBlocks.FLOATING, props()), getId(LegacyLotusBlocks.FLOATING));
		r.accept(new SpecialFlowerBlockItem(tradeOrchid, props()), getId(tradeOrchid));
		r.accept(new SpecialFlowerBlockItem(tradeOrchidFloating, props()), getId(tradeOrchidFloating));

		r.accept(new SpecialFlowerBlockItem(woodienia, props()), getId(woodienia));
		r.accept(new SpecialFlowerBlockItem(woodieniaFloating, props()), getId(woodieniaFloating));

		r.accept(new SpecialFlowerBlockItem(reikarlily, props()), getId(reikarlily));
		r.accept(new SpecialFlowerBlockItem(reikarlilyFloating, props()), getId(reikarlilyFloating));

		r.accept(new SpecialFlowerBlockItem(bellflower, props()), getId(bellflower));
		r.accept(new SpecialFlowerBlockItem(bellflowerFloating, props()), getId(bellflowerFloating));

		r.accept(new SpecialFlowerBlockItem(annoyingflower, props()), getId(annoyingflower));
		r.accept(new SpecialFlowerBlockItem(annoyingflowerFloating, props()), getId(annoyingflowerFloating));

		r.accept(new SpecialFlowerBlockItem(stonesia, props()), getId(stonesia));
		r.accept(new SpecialFlowerBlockItem(stonesiaFloating, props()), getId(stonesiaFloating));

		r.accept(new SpecialFlowerBlockItem(edelweiss, props()), getId(edelweiss));
		r.accept(new SpecialFlowerBlockItem(edelweissFloating, props()), getId(edelweissFloating));

		r.accept(new SpecialFlowerBlockItem(resoncund, props()), getId(resoncund));
		r.accept(new SpecialFlowerBlockItem(resoncundFloating, props()), getId(resoncundFloating));

		r.accept(new SpecialFlowerBlockItem(sunshineLily, props()), getId(sunshineLily));
		r.accept(new SpecialFlowerBlockItem(sunshineLilyFloating, props()), getId(sunshineLilyFloating));

		r.accept(new SpecialFlowerBlockItem(moonlightLily, props()), getId(moonlightLily));
		r.accept(new SpecialFlowerBlockItem(moonlightLilyFloating, props()), getId(moonlightLilyFloating));

		r.accept(new SpecialFlowerBlockItem(serenitian, props().rarity(Rarity.EPIC)), getId(serenitian));
		r.accept(new SpecialFlowerBlockItem(serenitianFloating, props().rarity(Rarity.EPIC)), getId(serenitianFloating));

		r.accept(new SpecialFlowerBlockItem(twinstar, props()), getId(twinstar));
		r.accept(new SpecialFlowerBlockItem(twinstarFloating, props()), getId(twinstarFloating));

		r.accept(new SpecialFlowerBlockItem(omniviolet, props()), getId(omniviolet));
		r.accept(new SpecialFlowerBlockItem(omnivioletFloating, props()), getId(omnivioletFloating));

		r.accept(new SpecialFlowerBlockItem(tinkle, props()), getId(tinkle));
		r.accept(new SpecialFlowerBlockItem(tinkleFloating, props()), getId(tinkleFloating));

		r.accept(new SpecialFlowerBlockItem(bloodEnchantress, props()), getId(bloodEnchantress));
		r.accept(new SpecialFlowerBlockItem(bloodEnchantressFloating, props()), getId(bloodEnchantressFloating));

		r.accept(new SpecialFlowerBlockItem(mirrowtunia, props()), getId(mirrowtunia));
		r.accept(new SpecialFlowerBlockItem(mirrowtuniaFloating, props()), getId(mirrowtuniaFloating));

		r.accept(new SpecialFlowerBlockItem(necrofleur, props()), getId(necrofleur));
		r.accept(new SpecialFlowerBlockItem(necrofleurChibi, props()), getId(necrofleurChibi));
		r.accept(new SpecialFlowerBlockItem(necrofleurFloating, props()), getId(necrofleurFloating));
		r.accept(new SpecialFlowerBlockItem(necrofleurChibiFloating, props()), getId(necrofleurChibiFloating));

		r.accept(new SpecialFlowerBlockItem(manalink, props()), getId(manalink));
		r.accept(new SpecialFlowerBlockItem(manalinkFloating, props()), getId(manalinkFloating));

		r.accept(new SpecialFlowerBlockItem(enchanter, props()), getId(enchanter));
		r.accept(new SpecialFlowerBlockItem(enchanterFloating, props()), getId(enchanterFloating));

	}

	private static Item.Properties props() {
		return ExtraBotanyItems.defaultBuilder();
	}

	public static void registerTEs(BiConsumer<BlockEntityType<?>, ResourceLocation> r) {
		r.accept(STARDUST_LOTUS, getId(LegacyLotusBlocks.FLOWER));
		r.accept(TRADE_ORCHID, getId(tradeOrchid));
		r.accept(WOODIENIA, getId(woodienia));
		r.accept(REIKARLILY, getId(reikarlily));
		r.accept(BELLFLOWER, getId(bellflower));
		r.accept(ANNOYINGFLOWER, getId(annoyingflower));
		r.accept(STONESIA, getId(stonesia));
		r.accept(EDELWEISS, getId(edelweiss));
		r.accept(RESONCUND, getId(resoncund));
		r.accept(SUNSHINE_LILY, getId(sunshineLily));
		r.accept(MOONLIGHT_LILY, getId(moonlightLily));
		r.accept(SERENITIAN, getId(serenitian));
		r.accept(TWINSTAR, getId(twinstar));
		r.accept(OMNIVIOLET, getId(omniviolet));
		r.accept(TINKLE, getId(tinkle));
		r.accept(BLOOD_ENCHANTRESS, getId(bloodEnchantress));
		r.accept(MIRROWTUNIA, getId(mirrowtunia));
		r.accept(NECROFLEUR, getId(necrofleur));
		r.accept(NECROFLEUR_CHIBI, getId(necrofleurChibi));
		r.accept(MANALINK, getId(manalink));
		r.accept(ENCHANTER, getId(enchanter));
	}

	public static void registerWandHudCaps(WandHudCapabilityConsumer consumer) {
		consumer.accept(be -> new io.github.lounode.extrabotany.client.renderer.LegacyLotusHud((StardustLotusBlockEntity) be), STARDUST_LOTUS);
		consumer.accept(be -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((FunctionalFlowerBlockEntity) be),
				TRADE_ORCHID, WOODIENIA, SERENITIAN, MIRROWTUNIA, NECROFLEUR, NECROFLEUR_CHIBI
		);
		consumer.accept(be -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((GeneratingFlowerBlockEntity) be),
				REIKARLILY, STONESIA, EDELWEISS, RESONCUND, SUNSHINE_LILY, MOONLIGHT_LILY,
				TWINSTAR, OMNIVIOLET, TINKLE, BLOOD_ENCHANTRESS
		);
		consumer.accept(be -> new BellflowerBlockEntity.WandHud((BellflowerBlockEntity) be), BELLFLOWER);
		consumer.accept(be -> new AnnoyingFlowerBlockEntity.WandHud((AnnoyingFlowerBlockEntity) be), ANNOYINGFLOWER);
		consumer.accept(be -> new ManalinkBlockEntity.WandHUD((ManalinkBlockEntity) be), MANALINK);
		consumer.accept(be -> new EnchanterBlockEntity.WandHUD((EnchanterBlockEntity) be), ENCHANTER);
	}

	@FunctionalInterface
	public interface WandHudCapabilityConsumer {
		void accept(Function<BlockEntity, WandHUD> factory, BlockEntityType<?>... types);
	}

	public static void registerFlowerPotPlants(BiConsumer<ResourceLocation, Supplier<? extends Block>> consumer) {
		registerBlocks((block, resourceLocation) -> {
			if (block instanceof FlowerPotBlock) {
				var id = getId(block);
				consumer.accept(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring(vazkii.botania.common.lib.LibBlockNames.POTTED_PREFIX.length())), () -> block);
			}
		});
	}

	private static class Hidden extends SpecialFlowerBlockItem implements CustomCreativeTabContents {

		public Hidden(Block block1, Properties props) {
			super(block1, props);
		}

		@Override
		public void addToCreativeTab(Item me, CreativeModeTab.Output output) {

		}
	}
}
