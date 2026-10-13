package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.handler.AllAdvancementsHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;
import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 原版 1.12 的进度的回归测试：数据包里有这些进度，代码在正确的时候授予它们。
 * Regression tests for the advancements of the original 1.12: the data pack has these advancements, and the code grants them at the correct time.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class AdvancementGameTests {
	private AdvancementGameTests() {}

	private static boolean hasAdvancement(GameTestHelper helper, ServerPlayer player, String name) {
		ResourceLocation id = prefix("main/" + name);
		AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(id);
		helper.assertTrue(advancement != null, "The data pack has no advancement " + id);
		return player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	/**
	 * 六个恢复的进度都在数据包里，它们的父进度也都存在。
	 * The six restored advancements are all in the data pack, and their parents all exist.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void originalAdvancementsExist(GameTestHelper helper) {
		var advancements = helper.getLevel().getServer().getAdvancements();
		for (String name : List.of(LibAdvancementNames.POSBINDER_CRAFT, LibAdvancementNames.STARDUSTLOTUS_TELEPORT,
				LibAdvancementNames.KINGGARDEN_CRAFT, LibAdvancementNames.SPEARSUBSPACE_CRAFT,
				LibAdvancementNames.BOTTLE_SET, LibAdvancementNames.LUCKY_DRAW)) {
			AdvancementHolder holder = advancements.get(prefix("main/" + name));
			helper.assertTrue(holder != null, "The data pack has no advancement main/" + name);
			var parent = holder.value().parent();
			helper.assertTrue(parent.isPresent() && advancements.get(parent.get()) != null, "The parent of main/" + name + " does not exist");
		}
		// "收集所有种类的瓶子"有四个条件，四个都要满足。
		// "Collect all kinds of bottles" has four conditions, and all four are necessary.
		var bottleSet = advancements.get(prefix("main/" + LibAdvancementNames.BOTTLE_SET)).value();
		helper.assertTrue(bottleSet.criteria().size() == 4 && bottleSet.requirements().size() == 4,
				"The bottle set advancement must have four separate requirements");
		helper.succeed();
	}

	/**
	 * 从限量补给袋里抽到静籁永恒的玩家得到"Starduster"进度。别的奖励不给这个进度。
	 * A player that draws the Silent Eternity from a Supply Bag Limited Edition gets the advancement "Starduster". The other rewards do not give it.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void supplyBagRareRewardGrantsTheAdvancement(GameTestHelper helper) {
		// 这名玩家不加入世界。加入世界会触发"玩家进入"事件，Curios 会在这个事件里给玩家发数据包，而测试用的连接不能接收它。
		// This player does not enter the level. An entry starts the "player joins" event, in which Curios sends a packet to the player, and the connection of a test cannot accept it.
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "eb-test-player"), ClientInformation.createDefault());
		BlockPos stand = helper.absolutePos(new BlockPos(2, 2, 2));
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		AABB area = new AABB(stand).inflate(4);

		boolean sawRare = false;
		String problem = null;
		// 奖池有 100 份奖励，另外第一个袋子一定给一个空的律者核心，所以 101 次之内一定会抽到静籁永恒。
		// The pool has 100 rewards, and the first bag always gives an Empty Herrscher Core, thus the Silent Eternity comes in 101 draws or less.
		for (int draw = 0; draw < 101 && problem == null; draw++) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.limitedEditionSupplyBag));
			player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
			List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
			boolean rareNow = drops.stream().anyMatch(drop -> drop.getItem().is(ExtraBotanyItems.silentEternity));
			drops.forEach(ItemEntity::discard);
			sawRare |= rareNow;
			boolean done = hasAdvancement(helper, player, LibAdvancementNames.LUCKY_DRAW);
			if (done != sawRare) {
				problem = "After draw " + (draw + 1) + ": Silent Eternity drawn = " + sawRare + ", advancement done = " + done;
			}
		}
		helper.assertTrue(problem == null, problem);
		helper.assertTrue(sawRare, "The Silent Eternity was not drawn in 101 draws");
		helper.succeed();
	}

	/**
	 * 玩家完成本模组全部主进度时，得到"Erase or Zero"进度和一个奖杯。少一个进度时什么也没有，奖杯也只给一次。
	 * When a player completes all main advancements of this mod, the player gets the advancement "Erase or Zero" and one Trophy. With one advancement missing there is nothing, and the Trophy is only given one time.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void allMainAdvancementsGiveTheTrophy(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "eb-test-trophy"), ClientInformation.createDefault());
		BlockPos stand = helper.absolutePos(new BlockPos(2, 2, 2));
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		AABB area = new AABB(stand).inflate(4);

		var manager = helper.getLevel().getServer().getAdvancements();
		ResourceLocation goalId = prefix("main/" + LibAdvancementNames.ALL_STATS);
		AdvancementHolder goal = manager.get(goalId);
		helper.assertTrue(goal != null, "The data pack has no advancement " + goalId);
		List<AdvancementHolder> main = manager.getAllAdvancements().stream()
				.filter(holder -> holder.id().getNamespace().equals("extrabotany") && holder.id().getPath().startsWith("main/")
						&& !holder.id().equals(goalId) && holder.value().display().isPresent())
				.toList();
		helper.assertTrue(main.size() >= 35, "Too few main advancements were found: " + main.size());

		for (AdvancementHolder holder : main.subList(0, main.size() - 1)) {
			complete(player, holder);
		}
		helper.assertTrue(!player.getAdvancements().getOrStartProgress(goal).isDone(), "With one advancement missing the player must not get the last advancement");
		helper.assertTrue(trophies(helper, area).isEmpty(), "With one advancement missing there must be no Trophy");

		complete(player, main.getLast());
		helper.assertTrue(player.getAdvancements().getOrStartProgress(goal).isDone(), "With all main advancements the player must get the last advancement");
		helper.assertTrue(trophies(helper, area).size() == 1, "With all main advancements the player must get one Trophy, but there are " + trophies(helper, area).size());

		AllAdvancementsHandler.check(player);
		helper.assertTrue(trophies(helper, area).size() == 1, "The Trophy must only be given one time");
		trophies(helper, area).forEach(ItemEntity::discard);

		// 和原版相同：直接授予这个进度（例如用命令）的玩家也得到奖杯。
		// The same as in the original: a player that gets this advancement directly (for example with a command) also gets the Trophy.
		ServerPlayer second = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "eb-test-trophy2"), ClientInformation.createDefault());
		second.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		complete(second, goal);
		helper.assertTrue(trophies(helper, area).size() == 1, "A direct grant of the advancement must give one Trophy");
		trophies(helper, area).forEach(ItemEntity::discard);
		helper.succeed();
	}

	private static void complete(ServerPlayer player, AdvancementHolder holder) {
		List<String> remaining = new java.util.ArrayList<>();
		player.getAdvancements().getOrStartProgress(holder).getRemainingCriteria().forEach(remaining::add);
		for (String criterion : remaining) {
			player.getAdvancements().award(holder, criterion);
		}
	}

	private static List<ItemEntity> trophies(GameTestHelper helper, AABB area) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, drop -> drop.getItem().is(ExtraBotanyBlocks.trophy.asItem()));
	}
}