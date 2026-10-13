package io.github.lounode.extrabotany.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import vazkii.botania.common.advancements.RelicBindTrigger;
import vazkii.botania.common.advancements.UseItemSuccessTrigger;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.advancements.HasArmorSetTrigger;
import io.github.lounode.extrabotany.common.advancements.ManaChargeTrigger;
import io.github.lounode.extrabotany.common.advancements.MinMaxBoundsExtension;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.relic.MasterBandOfManaItem;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 生成本模组的进度树（{@code extrabotany:main/*}）。
 * Makes the advancement tree of this mod ({@code extrabotany:main/*}).
 *
 * <p>很多进度的条件是"不可能"（{@code code_triggered}）。这些进度由代码在事情发生时直接授予。
 * <p>Many advancements have the condition "impossible" ({@code code_triggered}). The code grants these advancements directly when the event occurs.
 *
 * <p>从原版恢复的几个进度是 {@code src/main/resources} 里的手写文件。
 * <p>Some advancements that were restored from the original are hand-written files in {@code src/main/resources}.
 */
public class AdvancementProvider extends vazkii.botania.data.AdvancementProvider {

	public static String CODE_TRIGGE = "code_triggered";

	public static net.neoforged.neoforge.common.data.AdvancementProvider create(
			PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
			ExistingFileHelper existingFileHelper) {
		return new net.neoforged.neoforge.common.data.AdvancementProvider(
				packOutput, lookupProvider, existingFileHelper, List.of(new ExtrabotanyAdvancements()));
	}

	public static class ExtrabotanyAdvancements implements net.neoforged.neoforge.common.data.AdvancementProvider.AdvancementGenerator {
		@Override
		public void generate(HolderLookup.Provider lookup, Consumer<AdvancementHolder> consumer,
				ExistingFileHelper existingFileHelper) {
			AdvancementHolder root = Advancement.Builder.advancement()
					.display(rootDisplay(ExtraBotanyItems.zadkiel, "itemGroup.extrabotany", "extrabotany.desc", prefix("textures/block/photonium_block.png")))
					.addCriterion("use_lexicon", UseItemSuccessTrigger.Instance.used(BotaniaItems.LEXICA_BOTANIA))
					.save(consumer, mainId(LibAdvancementNames.ROOT));
			AdvancementHolder senbonZakura = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.manaReader, LibAdvancementNames.SENBON_ZAKURA, AdvancementType.CHALLENGE))
					.parent(root)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.SENBON_ZAKURA));
			//Pedestal
			AdvancementHolder pedestal = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyBlocks.livingrockPedestal, LibAdvancementNames.CRAFT_PEDESTAL, AdvancementType.TASK))
					.parent(root)
					.addCriterion("craft_pedestal", onPickup(ExtraBotanyTags.Items.PEDESTALS))
					.save(consumer, mainId(LibAdvancementNames.CRAFT_PEDESTAL));
			AdvancementHolder kurukuru = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyBlocks.livingrockPedestal, LibAdvancementNames.KURUKURU, AdvancementType.GOAL))
					.parent(pedestal)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.KURUKURU));
			AdvancementHolder goodtek = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.manasteelHammer, LibAdvancementNames.GOODTEK, AdvancementType.TASK))
					.parent(pedestal)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.GOODTEK));

			AdvancementHolder deepDarkFantasy = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.nightmareFuel, LibAdvancementNames.DEEP_DARK_FANTASY, AdvancementType.TASK))
					.parent(root)
					.addCriterion("eat_nightmare_fuel", UseItemSuccessTrigger.Instance.used(ExtraBotanyItems.nightmareFuel))
					.save(consumer, mainId(LibAdvancementNames.DEEP_DARK_FANTASY));
			AdvancementHolder stygianTwins = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.shadowium, LibAdvancementNames.STYGIAN_TWINS, AdvancementType.TASK))
					.parent(goodtek)
					.addCriterion("craft_shadowium", onPickup(ExtraBotanyTags.Items.INGOTS_SHADOWIUM))
					.addCriterion("craft_photonium", onPickup(ExtraBotanyTags.Items.INGOTS_PHOTONIUM))
					.save(consumer, mainId(LibAdvancementNames.STYGIAN_TWINS));
			AdvancementHolder gaiaTrial = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.challengeTicket, LibAdvancementNames.GAIA_TRIAL, AdvancementType.CHALLENGE))
					.parent(stygianTwins)
					.addCriterion("kill_gaia_3rd",
							KilledTrigger.TriggerInstance.playerKilledEntity(
									EntityPredicate.Builder.entity()
										.of(ExtraBotanyEntityType.GAIA_III)
							)
					)
					.save(consumer, mainId(LibAdvancementNames.GAIA_TRIAL));
			// 击败本我。来源：ExtraMeteorP 1.16 的 egodefeat。原版的父进度是"得到自然蕴息宝珠"，这里没有那个进度，所以用给宝珠充能的祭坛的进度。
			// Defeat the Ego. Source: egodefeat of ExtraMeteorP 1.16. The parent in the original is "get the Nature Orb"; that advancement is not here, thus the advancement of the pedestal that charges the orb is used.
			Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.heroMedal, LibAdvancementNames.EGO_DEFEAT, AdvancementType.CHALLENGE))
					.parent(pedestal)
					.addCriterion("kill_ego",
							KilledTrigger.TriggerInstance.playerKilledEntity(
									EntityPredicate.Builder.entity()
										.of(ExtraBotanyEntityType.EGO)
							)
					)
					.save(consumer, mainId(LibAdvancementNames.EGO_DEFEAT));
			AdvancementHolder theSourceOfHonkai = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.pandorasBox, LibAdvancementNames.THE_SOURCE_OF_HONKAI, AdvancementType.TASK))
					.parent(gaiaTrial)
					.addCriterion("open_pandoras_box", UseItemSuccessTrigger.Instance.used(ExtraBotanyItems.pandorasBox))
					.save(consumer, mainId(LibAdvancementNames.THE_SOURCE_OF_HONKAI));
			AdvancementHolder theOriginalDivineKey = relicBindAdvancement(
					ExtraBotanyItems.voidArchives, LibAdvancementNames.THE_ORIGINAL_DIVINE_KEY)
							.parent(theSourceOfHonkai)
							.save(consumer, mainId(LibAdvancementNames.THE_ORIGINAL_DIVINE_KEY));
			AdvancementHolder iSeeEveryThing = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.camera, LibAdvancementNames.I_SEE_EVERYTHING, AdvancementType.GOAL))
					.parent(stygianTwins)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.I_SEE_EVERYTHING));
			AdvancementHolder onePunch = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.featherOfJingwei, LibAdvancementNames.ONE_PUNCH, AdvancementType.CHALLENGE))
					.parent(theSourceOfHonkai)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.ONE_PUNCH));
			AdvancementHolder soulSteel = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.orichalcos, LibAdvancementNames.SOULSTEEL, AdvancementType.CHALLENGE))
					.parent(theSourceOfHonkai)
					.addCriterion("craft_orichalcos", onPickup(ExtraBotanyTags.Items.INGOTS_ORICHALCOS))
					.save(consumer, mainId(LibAdvancementNames.SOULSTEEL));
			//ManaRing
			AdvancementHolder craftRing = relicBindAdvancement(
					ExtraBotanyItems.manaRingMaster, LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS)
							.parent(soulSteel)
							.save(consumer, mainId(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));

			AdvancementHolder overlord = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.manaRingMaster, LibAdvancementNames.OVERLOAD, AdvancementType.CHALLENGE))
					.parent(craftRing)
					.addCriterion("mana_charge", ManaChargeTrigger.TriggerInstance.manaCharged(
							ItemPredicate.Builder.item().of(ExtraBotanyItems.manaRingMaster).build(),
							MinMaxBoundsExtension.Longs.atLeast(MasterBandOfManaItem.ADVANCEMENT_PHASE1_REQUIRE)
					))
					.save(consumer, mainId(LibAdvancementNames.OVERLOAD));
			AdvancementHolder lordOfKing = Advancement.Builder.advancement()
					.display(hidden(ExtraBotanyItems.manaRingMaster, LibAdvancementNames.LOAD_OF_RING, AdvancementType.CHALLENGE))
					.parent(overlord)
					.addCriterion("mana_charge", ManaChargeTrigger.TriggerInstance.manaCharged(
							ItemPredicate.Builder.item().of(ExtraBotanyItems.manaRingMaster).build(),
							MinMaxBoundsExtension.Longs.atLeast(MasterBandOfManaItem.ADVANCEMENT_PHASE2_REQUIRE)
					))
					.save(consumer, mainId(LibAdvancementNames.LOAD_OF_RING));
			AdvancementHolder hundredBlockPierce = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.failnaught, LibAdvancementNames.HUNDRED_BLOCK_PIERCE, AdvancementType.CHALLENGE))
					.parent(soulSteel)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.HUNDRED_BLOCK_PIERCE));
			AdvancementHolder skyIsNotTheLimit = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.aerialite, LibAdvancementNames.SKY_IS_NOT_THE_LIMIT, AdvancementType.TASK))
					.parent(stygianTwins)
					.addCriterion("craft_aerialite", onPickup(ExtraBotanyTags.Items.INGOTS_AERIALITE))
					.save(consumer, mainId(LibAdvancementNames.SKY_IS_NOT_THE_LIMIT));

			AdvancementHolder potatoServer = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.gildedPotatoMashed, LibAdvancementNames.POTATO_SERVER, AdvancementType.TASK))
					.parent(root)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.POTATO_SERVER));

			AdvancementHolder rheinKraft = relicBindAdvancement(ExtraBotanyItems.rheinHammer, LibAdvancementNames.RHEIN_KRAFT)
					.parent(theSourceOfHonkai)
					.save(consumer, mainId(LibAdvancementNames.RHEIN_KRAFT));
			AdvancementHolder spongeHammer = Advancement.Builder.advancement()
					.display(simple(Items.SPONGE, LibAdvancementNames.SPONGE_HAMMER, AdvancementType.TASK))
					.parent(rheinKraft)
					.addCriterion(CODE_TRIGGE, impossible())
					.save(consumer, mainId(LibAdvancementNames.SPONGE_HAMMER));
			AdvancementHolder omgItsMiku = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.starryIdolHeadgear, LibAdvancementNames.OMG_ITS_MIKU, AdvancementType.TASK))
					.parent(root)
					.addCriterion("has_armor_set", armorSet(StarryIdolArmorItem.ARMOR_SET))
					.save(consumer, mainId(LibAdvancementNames.OMG_ITS_MIKU));
			AdvancementHolder sevenSamurai = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.shadowWarriorHelmet, LibAdvancementNames.SEVEN_SAMURAI, AdvancementType.CHALLENGE))
					.parent(stygianTwins)
					.addCriterion("has_armor_set", armorSet(ShadowWarriorArmorItem.ARMOR_SET))
					.save(consumer, mainId(LibAdvancementNames.SEVEN_SAMURAI));
			AdvancementHolder goblinKiller = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.goblinSlayerHelmet, LibAdvancementNames.GOBLIN_KILLER, AdvancementType.CHALLENGE))
					.parent(stygianTwins)
					.addCriterion("has_armor_set", armorSet(GoblinSlayerArmorItem.ARMOR_SET))
					.save(consumer, mainId(LibAdvancementNames.GOBLIN_KILLER));
			AdvancementHolder cuteDress = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.pleiadesCombatMaidSuit, LibAdvancementNames.CUTE_DRESS, AdvancementType.CHALLENGE))
					.parent(stygianTwins)
					.addCriterion("has_armor_set", armorSet(PleiadesCombatMaidArmorItem.ARMOR_SET))
					.save(consumer, mainId(LibAdvancementNames.CUTE_DRESS));
			AdvancementHolder corruption = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit, LibAdvancementNames.CORRUPTION, AdvancementType.CHALLENGE))
					.parent(cuteDress)
					.addCriterion("has_item", onPickup(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit))
					.save(consumer, mainId(LibAdvancementNames.CORRUPTION));
			AdvancementHolder aBaldrupt = Advancement.Builder.advancement()
					.display(hidden(BotaniaBlocks.DAFFOMILL, LibAdvancementNames.A_BALDRUPT, AdvancementType.TASK))
					.parent(goodtek)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.A_BALDRUPT));
			AdvancementHolder muromiSan = Advancement.Builder.advancement()
					.display(simple(ExtrabotanyFlowerBlocks.annoyingflower, LibAdvancementNames.MUROMI_SAN, AdvancementType.GOAL))
					.parent(goodtek)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.MUROMI_SAN));
			AdvancementHolder itsOnlyTheFairyTale = Advancement.Builder.advancement()
					.display(simple(ExtrabotanyFlowerBlocks.edelweiss, LibAdvancementNames.ITS_ONLY_THE_FAIRY_TALE, AdvancementType.TASK))
					.parent(goodtek)
					.addCriterion(CODE_TRIGGE, impossible())
					.save(consumer, mainId(LibAdvancementNames.ITS_ONLY_THE_FAIRY_TALE));

			// 原版 1.12 的进度，对应从原版恢复的内容。标题和说明来自原版的语言文件。
			// Advancements of the original 1.12, for the content that was restored from the original. The titles and the descriptions come from the language files of the original.
			AdvancementHolder posBinder = Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.binder, LibAdvancementNames.POSBINDER_CRAFT, AdvancementType.TASK))
					.parent(root)
					.addCriterion("has_item", onPickup(ExtraBotanyItems.binder))
					.save(consumer, mainId(LibAdvancementNames.POSBINDER_CRAFT));
			Advancement.Builder.advancement()
					.display(simple(ExtrabotanyFlowerBlocks.stardustLotus, LibAdvancementNames.STARDUSTLOTUS_TELEPORT, AdvancementType.TASK))
					.parent(posBinder)
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.STARDUSTLOTUS_TELEPORT));
			Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.kingGarden, LibAdvancementNames.KINGGARDEN_CRAFT, AdvancementType.TASK))
					.parent(pedestal)
					.addCriterion("has_item", onPickup(ExtraBotanyItems.kingGarden))
					.save(consumer, mainId(LibAdvancementNames.KINGGARDEN_CRAFT));
			Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.spearOfSubspace, LibAdvancementNames.SPEARSUBSPACE_CRAFT, AdvancementType.CHALLENGE))
					.parent(gaiaTrial)
					.addCriterion("has_item", onPickup(ExtraBotanyItems.spearOfSubspace))
					.save(consumer, mainId(LibAdvancementNames.SPEARSUBSPACE_CRAFT));
			// 四个条件都要满足。
			// All four conditions are necessary.
			Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.bottledFlame, LibAdvancementNames.BOTTLE_SET, AdvancementType.TASK))
					.parent(root)
					.addCriterion("bottled_flame", onPickup(ExtraBotanyItems.bottledFlame))
					.addCriterion("bottled_pixie", onPickup(ExtraBotanyItems.bottledPixie))
					.addCriterion("bottled_star", onPickup(ExtraBotanyItems.bottledStar))
					.addCriterion("king_garden", onPickup(ExtraBotanyItems.kingGarden))
					.save(consumer, mainId(LibAdvancementNames.BOTTLE_SET));
			// 父进度"击败空之律者"是手写的文件，所以这里只引用它的 ID。
			// The parent "defeat the Herrscher of the Void" is a hand-written file, thus only its ID is used here.
			Advancement.Builder.advancement()
					.display(simple(ExtraBotanyItems.limitedEditionSupplyBag, LibAdvancementNames.LUCKY_DRAW, AdvancementType.CHALLENGE))
					.parent(AdvancementSubProvider.createPlaceholder(mainId(LibAdvancementNames.HERRSCHER_DEFEAT)))
					.addCriterion("code_triggered", impossible())
					.save(consumer, mainId(LibAdvancementNames.LUCKY_DRAW));
		}
	}

	protected static Criterion<HasArmorSetTrigger.TriggerInstance> armorSet(Supplier<ItemStack[]> armorSet) {
		return HasArmorSetTrigger.TriggerInstance.forArmorSet(armorSet.get());
	}

	private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
		return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
	}

	private static Advancement.Builder relicBindAdvancement(Item relicItem, String titleKey) {
		return Advancement.Builder.advancement()
				.display(simple(relicItem, titleKey, AdvancementType.CHALLENGE))
				.rewards(AdvancementRewards.Builder.experience(50))
				.addCriterion("has_relic", RelicBindTrigger.Instance.bound(relicItem));
	}

	protected static DisplayInfo simple(ItemLike icon, String name, AdvancementType frameType) {
		String expandedName = "advancement.extrabotany:" + name;
		return new DisplayInfo(new ItemStack(icon.asItem()),
				Component.translatable(expandedName),
				Component.translatable(expandedName + ".desc"),
				Optional.empty(), frameType, true, true, false);
	}

	protected static DisplayInfo hidden(ItemLike icon, String name, AdvancementType frameType) {
		String expandedName = "advancement.extrabotany:" + name;
		return new DisplayInfo(new ItemStack(icon.asItem()),
				Component.translatable(expandedName),
				Component.translatable(expandedName + ".desc"),
				Optional.empty(), frameType, true, true, true);
	}

	private static String mainId(String name) {
		return prefix("main/" + name).toString();
	}

	private static String challengeId(String name) {
		return prefix("challenge/" + name).toString();
	}
}
