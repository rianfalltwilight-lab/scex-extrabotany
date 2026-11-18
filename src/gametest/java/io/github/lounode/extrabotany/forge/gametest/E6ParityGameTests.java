package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.entity.vehicle.FlyingBoatEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.CosmeticBaubleItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.UltimateHammerItem;
import io.github.lounode.extrabotany.common.item.handbag.MiniHandbagItem;
import io.github.lounode.extrabotany.common.item.handbag.MiniHandbagMenu;
import io.github.lounode.extrabotany.common.item.lens.CloudLens;
import io.github.lounode.extrabotany.common.item.lens.PotionLens;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 对照原版审计第 E6 批：补回只有 1.12 有的内容——飞艇、旅行手袋、云朵透镜、镀金究极锤和它的祭坛升级、三重血玉、问号和柠檬装饰品。
 * Batch E6 of the parity audit with the original: the content that only 1.12 has comes back — the flying boats, the Mini Handbag, the Cloud Lens, the Ultimate Hammer with its pedestal upgrades, the Tri-Bloodstone, and the question mark and lemon cosmetics.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E6ParityGameTests {

	private E6ParityGameTests() {}

	private static boolean hasRecipe(GameTestHelper h, String id) {
		return h.getLevel().getRecipeManager().byKey(prefix(id)).isPresent();
	}

	// 1.12 ModCraftingRecipe / ModRuneRecipe：新物品都有原版的配方；樱花誓约用三重血玉；Thug Life 没有配方（1.12 只能从"终局"得到）。
	// ModCraftingRecipe / ModRuneRecipe of 1.12: the new items have the original recipes; the Sakura oath uses the Tri-Bloodstone; Thug Life has no recipe (in 1.12 it only comes from "End Day").
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void recipesOf112(GameTestHelper h) {
		for (String id : List.of("tri_bloodstone", "question", "lens_cloud", "mini_handbag",
				"manasteel_flying_boat", "elementium_flying_boat", "terrasteel_flying_boat")) {
			h.assertTrue(hasRecipe(h, id), "Missing recipe " + id);
		}
		h.assertTrue(!hasRecipe(h, "thug_life"), "Thug Life still has the Reburn recipe");
		var sakura = h.getLevel().getRecipeManager().byKey(prefix("judah_oath_sakura")).orElseThrow().value();
		h.assertTrue(sakura.getIngredients().stream().anyMatch(i -> i.test(new ItemStack(ExtraBotanyItems.triBloodstone))),
				"The Sakura oath must use the Tri-Bloodstone");
		var hammer = (RunicAltarRecipe) h.getLevel().getRecipeManager().byKey(prefix("runic_altar/ultimate_hammer")).orElseThrow().value();
		h.assertTrue(hammer.getMana() == 100000 && hammer.getIngredients().size() == 5, "The Ultimate Hammer must cost 100000 with 5 inputs");
		h.succeed();
	}

	// 1.12 BlockPedestal：5 个镀金土豆泥升级修复（最多 3），元素钢剑升级锋利（最多 10），泰拉钢锤解锁范围。
	// BlockPedestal of 1.12: 5 gilded mashed potatoes upgrade the repair (at most 3), an elementium sword the sharpness (at most 10), a terrasteel hammer unlocks the range.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void ultimateHammerUpgrades(GameTestHelper h) {
		ItemStack hammer = new ItemStack(ExtraBotanyItems.ultimateHammer);
		ItemStack potatoes = new ItemStack(ExtraBotanyItems.gildedPotatoMashed, 4);
		h.assertTrue(!UltimateHammerItem.tryUpgrade(hammer, potatoes), "Four potatoes must not be enough");
		potatoes.setCount(16);
		for (int i = 0; i < 4; i++) {
			UltimateHammerItem.tryUpgrade(hammer, potatoes);
		}
		h.assertTrue(UltimateHammerItem.getRepair(hammer) == 3 && potatoes.getCount() == 1, "The repair must stop at 3 and use 5 potatoes each");
		ItemStack swords = new ItemStack(BotaniaItems.ELEMENTIUM_SWORD);
		h.assertTrue(UltimateHammerItem.tryUpgrade(hammer, swords) && UltimateHammerItem.getAttack(hammer) == 1 && swords.isEmpty(),
				"An elementium sword must give one sharpness level");
		ItemStack terra = new ItemStack(ExtraBotanyItems.terrasteelHammer);
		h.assertTrue(UltimateHammerItem.tryUpgrade(hammer, terra) && UltimateHammerItem.hasRange(hammer), "A terrasteel hammer must unlock the range");
		h.assertTrue(!UltimateHammerItem.tryUpgrade(hammer, new ItemStack(ExtraBotanyItems.terrasteelHammer)), "The range unlock happens only once");
		ManaItem mana = ManaItem.LOOKUP.find(hammer);
		h.assertTrue(mana != null && mana.getMaxMana() == 50000 && mana.isNoExport(), "The hammer must store 50000 mana and never export it");
		h.succeed();
	}

	// 祭坛上放究极锤，拿着镀金土豆泥右键祭坛，升级成功。 / An Ultimate Hammer on a pedestal, a use with gilded mashed potatoes upgrades it.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void ultimateHammerUpgradeOnPedestal(GameTestHelper h) {
		BlockPos rel = new BlockPos(1, 2, 1);
		h.setBlock(rel, ExtraBotanyBlocks.livingrockPedestal);
		PedestalBlockEntity pedestal = h.getBlockEntity(rel);
		pedestal.setItem(new ItemStack(ExtraBotanyItems.ultimateHammer));
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.gildedPotatoMashed, 5));
		BlockPos pos = h.absolutePos(rel);
		pedestal.use(h.getLevel().getBlockState(pos), h.getLevel(), pos, player, InteractionHand.MAIN_HAND,
				new BlockHitResult(pos.getCenter(), Direction.UP, pos, false));
		h.assertTrue(UltimateHammerItem.getRepair(pedestal.getItem()) == 1 && player.getMainHandItem().isEmpty(),
				"The pedestal must upgrade the hammer with 5 potatoes");
		h.succeed();
	}

	// 1.12 LensCloud：速度 × 0.75，魔力 × 5，损耗 × 4；它装酿造的方式和药水透镜相同；云里的效果持续时间是四分之一。
	// LensCloud of 1.12: speed × 0.75, mana × 5, loss × 4; it gets its brew like the potion lens; the effects in the cloud last one quarter.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cloudLens(GameTestHelper h) {
		ItemStack lens = new ItemStack(ExtraBotanyItems.lensCloud);
		h.assertTrue(LensItem.getLens(lens) instanceof CloudLens && LensItem.getLens(lens) instanceof PotionLens,
				"The Cloud Lens must take a brew like the potion lens");
		BurstProperties props = new BurstProperties(100, 60, 4F, 0F, 1F, 0);
		LensItem.getLens(lens).apply(lens, props);
		h.assertTrue(props.maxMana == 500 && props.manaLossPerTick == 16F && props.motionModifier == 0.75F, "The burst properties must follow 1.12");
		var cloud = CloudLens.spawnCloud(h.getLevel(), h.absoluteVec(new net.minecraft.world.phys.Vec3(2, 2, 2)).x, h.absolutePos(new BlockPos(2, 2, 2)).getY(),
				h.absoluteVec(new net.minecraft.world.phys.Vec3(2, 2, 2)).z, vazkii.botania.common.brew.BotaniaBrews.MENDING);
		h.assertTrue(cloud.getRadius() == 3F, "The cloud must have radius 3");
		cloud.discard();
		h.succeed();
	}

	// 1.12 ItemMasterHandbag：24 格，界面还有 4 个盔甲栏；物品保存在手袋上；手袋不能放进手袋。
	// ItemMasterHandbag of 1.12: 24 slots, and the screen also has 4 armor slots; the items stay on the handbag; a handbag cannot go into a handbag.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void miniHandbag(GameTestHelper h) {
		ItemStack bag = new ItemStack(ExtraBotanyItems.miniHandbag);
		var inv = MiniHandbagItem.getInventory(bag);
		inv.setItem(5, new ItemStack(Items.DIAMOND, 7));
		inv.setChanged();
		h.assertTrue(MiniHandbagItem.getInventory(bag).getItem(5).is(Items.DIAMOND) && MiniHandbagItem.getInventory(bag).getItem(5).getCount() == 7,
				"The handbag must keep its items");
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, bag);
		var menu = new MiniHandbagMenu(1, player.getInventory(), true);
		h.assertTrue(menu.slots.size() == 4 + 24 + 36, "The menu must have 64 slots, got " + menu.slots.size());
		h.assertTrue(!menu.slots.get(4).mayPlace(new ItemStack(ExtraBotanyItems.miniHandbag)), "A handbag must not go into a handbag");
		h.assertTrue(menu.stillValid(player), "The menu must stay open while the bag is held");
		h.succeed();
	}

	// 1.12 EntityFlyingBoat：没人驾驶、下方是空气时每 tick 下降 0.15；种类、最高高度和掉落物。
	// EntityFlyingBoat of 1.12: without a driver and with air below, it sinks 0.15 each tick; the kind, the height limit and the drop.
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void flyingBoat(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos start = h.absolutePos(new BlockPos(2, 20, 2));
		for (int y = 0; y < 30; y++) {
			level.setBlockAndUpdate(start.offset(0, -y, 0), Blocks.AIR.defaultBlockState());
		}
		var boat = new FlyingBoatEntity(level, start.getX() + 0.5, start.getY(), start.getZ() + 0.5, FlyingBoatEntity.Kind.TERRASTEEL);
		level.addFreshEntity(boat);
		double y = boat.getY();
		boat.tick();
		h.assertTrue(Math.abs(y - boat.getY() - FlyingBoatEntity.SINK) < 1.0E-4, "The boat must sink 0.15, moved " + (y - boat.getY()));
		h.assertTrue(boat.getDropItem() == ExtraBotanyItems.terrasteelFlyingBoat && FlyingBoatEntity.Kind.TERRASTEEL.maxHeight == 255
				&& FlyingBoatEntity.Kind.MANASTEEL.maxHeight == 32 && FlyingBoatEntity.Kind.ELEMENTIUM.maxHeight == 64, "The kinds must follow 1.12");
		CompoundTag tag = boat.saveWithoutId(new CompoundTag());
		var copy = new FlyingBoatEntity(io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType.FLYING_BOAT, level);
		copy.load(tag);
		h.assertTrue(copy.getKind() == FlyingBoatEntity.Kind.TERRASTEEL, "The kind must survive saving");
		boat.discard();
		h.succeed();
	}

	// 1.12 ItemCosmetic 的问号和柠檬；1.12 的究极锤进度。 / The question mark and the lemon of ItemCosmetic of 1.12; the Ultimate Hammer advancements of 1.12.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cosmeticsAndAdvancements(GameTestHelper h) {
		h.assertTrue(ExtraBotanyItems.question instanceof CosmeticBaubleItem q && q.getVariant() == CosmeticBaubleItem.Variant.QUESTION
				&& ExtraBotanyItems.lemon instanceof CosmeticBaubleItem l && l.getVariant() == CosmeticBaubleItem.Variant.LEMON,
				"The question mark and the lemon must be cosmetic baubles");
		var manager = h.getLevel().getServer().getAdvancements();
		for (String name : List.of(LibAdvancementNames.ULTIMATE_HAMMER_CRAFT, LibAdvancementNames.ULTIMATE_HAMMER_UPGRADE)) {
			h.assertTrue(manager.get(ResourceLocation.fromNamespaceAndPath("extrabotany", "main/" + name)) != null, "Missing advancement " + name);
		}
		h.succeed();
	}
}
