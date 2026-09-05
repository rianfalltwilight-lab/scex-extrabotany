package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.SeasonalItemHelper;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@GameTestHolder(LibMisc.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyCandyGameTests {
	private LegacyCandyGameTests() {}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void deployedCandyStacksSurviveDiskNbtAndOpen(GameTestHelper helper) throws IOException {
		ItemStack legacyBag = ItemStack.EMPTY;
		System.out.println("SCEX_ITEM_REGISTRY=" + BuiltInRegistries.ITEM.keySet().stream()
				.filter(id -> id.getNamespace().equals(LibMisc.MOD_ID)).map(Object::toString).sorted().collect(Collectors.joining(",")));
		for (String id : List.of("candy_bag", "candy_eins", "candy_zwei", "candy_drei")) {
			// Same id/count schema as the actual sophisticatedbackpacks stack (count=4).
			CompoundTag oldStack = new CompoundTag();
			oldStack.putString("id", "extrabotany:" + id);
			oldStack.putInt("count", 4);
			ItemStack loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), oldStack);
			helper.assertTrue(!loaded.isEmpty() && loaded.getCount() == 4, "Legacy stack lost: " + id);
			helper.assertTrue(BuiltInRegistries.ITEM.getKey(loaded.getItem()).toString().equals("extrabotany:" + id), "Legacy id changed");
			CompoundTag saved = (CompoundTag) loaded.save(helper.getLevel().registryAccess());
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			NbtIo.write(saved, new DataOutputStream(bytes));
			CompoundTag disk = NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())), NbtAccounter.unlimitedHeap());
			ItemStack restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), disk);
			helper.assertTrue(ItemStack.matches(loaded, restored), "Legacy NBT round trip changed stack: " + id);
			if (id.equals("candy_bag")) {
				legacyBag = restored;
			}
			CompoundTag custom = new CompoundTag();
			custom.putString("LootTable", "extrabotany:candy");
			custom.putString("legacy_marker", "preserve opaque fields");
			net.minecraft.world.item.component.CustomData.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, loaded, custom);
			ItemStack withData = ItemStack.parseOptional(helper.getLevel().registryAccess(),
					(CompoundTag) loaded.save(helper.getLevel().registryAccess()));
			helper.assertTrue(ItemStack.matches(loaded, withData), "Legacy custom components changed: " + id);
		}
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		var pos = helper.absolutePos(new BlockPos(2, 2, 2));
		player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, legacyBag);
		var result = ExtraBotanyItems.candyBag.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(result.getResult().consumesAction() && player.getMainHandItem().getCount() == 3,
				"Opening a candy bag did not consume exactly one");
		var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2));
		int count = drops.stream().mapToInt(e -> e.getItem().getCount()).sum();
		helper.assertTrue(count == 3, "Candy bag did not produce exactly three candies: " + count);
		helper.assertTrue(drops.stream().allMatch(e -> List.of(ExtraBotanyItems.candyEins, ExtraBotanyItems.candyZwei,
				ExtraBotanyItems.candyDrei).contains(e.getItem().getItem())), "Candy bag produced an invalid item");
		io.github.lounode.extrabotany.common.helper.ItemNBTHelper.setString(legacyBag, "LootTable", "invalid loot id!");
		helper.assertTrue(io.github.lounode.extrabotany.common.item.RewardBagItem.getLoot(legacyBag)
				.equals(ResourceLocation.parse("extrabotany:reward_bags/candy")), "Malformed legacy override must retain default candy loot");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void deployedCandyFoodAndSeasonalContract(GameTestHelper helper) {
		var candies = List.of(ExtraBotanyItems.candyEins, ExtraBotanyItems.candyZwei, ExtraBotanyItems.candyDrei);
		var effects = List.of(MobEffects.MOVEMENT_SPEED, MobEffects.JUMP, MobEffects.DIG_SPEED);
		for (int i = 0; i < candies.size(); i++) {
			var player = helper.makeMockPlayer(GameType.SURVIVAL);
			player.setHealth(10);
			player.getFoodData().setFoodLevel(10);
			ItemStack stack = new ItemStack(candies.get(i), 2);
			helper.assertTrue(stack.getUseDuration(player) == 14, "Candy use duration changed");
			ItemStack after = candies.get(i).finishUsingItem(stack, helper.getLevel(), player);
			var effect = player.getEffect(effects.get(i));
			helper.assertTrue(after.getCount() == 1 && player.getHealth() == 14 && player.getFoodData().getFoodLevel() == 12,
					"Candy consume/heal/food contract changed");
			helper.assertTrue(effect != null && effect.getDuration() == 200 && effect.getAmplifier() == 1, "Candy effect changed");
		}
		helper.assertTrue(SeasonalItemHelper.isChristmas(LocalDate.of(2026, 12, 16))
				&& SeasonalItemHelper.isChristmas(LocalDate.of(2027, 1, 2))
				&& !SeasonalItemHelper.isChristmas(LocalDate.of(2026, 12, 15))
				&& !SeasonalItemHelper.isChristmas(LocalDate.of(2027, 1, 3)), "Candy seasonal boundaries changed");
		helper.succeed();
	}
}
