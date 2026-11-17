package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.entity.MagicArrowEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Failnaught;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.mixin.LivingEntityAccessor;

import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2EquipmentBowGameTests {
	private record Shot(Player player, ManaItem mana, List<MagicArrowEntity> arrows) {}

	private static Shot shoot(GameTestHelper helper, boolean archives, int multishot, int drawTicks, int manaAmount) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(new BlockPos(32, 5, 32)).getCenter());
		ItemStack stack = new ItemStack(archives ? ExtraBotanyItems.voidArchives : ExtraBotanyItems.failnaught);
		if (archives) VoidArchivesItem.setVariant(stack, Failnaught.INSTANCE);
		Relic.LOOKUP.find(stack).bindToUUID(player.getUUID());
		if (multishot > 0) {
			stack.enchant(helper.getLevel().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), multishot);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(Math.max(5000, manaAmount));
		player.getInventory().setItem(2, tablet);
		helper.assertTrue(stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(), "Draw must start");
		mana.addMana(manaAmount - mana.getMana());
		((LivingEntityAccessor) player).botania_setUseItemRemaining(72000 - drawTicks);
		stack.releaseUsing(helper.getLevel(), player, 72000 - drawTicks);
		List<MagicArrowEntity> arrows = helper.getLevel().getEntitiesOfClass(MagicArrowEntity.class,
				player.getBoundingBox().inflate(4), arrow -> arrow.getOwner() == player);
		return new Shot(player, mana, arrows);
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void multishotOneAndTwoChargeThreeSingleArrowBudgets(GameTestHelper helper) {
		for (boolean archives : new boolean[] {false, true}) {
			for (int multishot : new int[] {0, 1, 2}) {
				Shot shot = shoot(helper, archives, multishot, 80, 5000);
				try {
					int count = multishot == 0 ? 1 : 3;
					helper.assertTrue(shot.arrows.size() == count, "Wrong projectile count for multishot " + multishot);
					helper.assertTrue(shot.mana.getMana() == 5000 - 800 * count, "Mana payment must match arrow count");
					for (MagicArrowEntity arrow : shot.arrows) {
						helper.assertTrue(arrow.getMana() == 800, "Each arrow must carry one arrow budget");
						helper.assertTrue(Math.abs(arrow.getDamage() - 40) < 0.001F, "Full draw retains 40 damage");
					}
				} finally {
					shot.arrows.forEach(MagicArrowEntity::discard);
				}
			}
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void multishotRefusesPartialManaPayment(GameTestHelper helper) {
		for (boolean archives : new boolean[] {false, true}) {
			Shot shot = shoot(helper, archives, 1, 80, 2399);
			try {
				helper.assertTrue(shot.arrows.isEmpty(), "Insufficient total mana must not emit arrows");
				helper.assertTrue(shot.mana.getMana() == 2399, "Rejected shot must not spend partial mana");
			} finally {
				shot.arrows.forEach(MagicArrowEntity::discard);
			}
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void damageNeverDropsAtChargeTierBoundaries(GameTestHelper helper) {
		for (boolean archives : new boolean[] {false, true}) {
			float previous = -1;
			for (int drawTicks : new int[] {27, 28, 29, 55, 56, 57, 71, 72, 73, 80}) {
				Shot shot = shoot(helper, archives, 0, drawTicks, 5000);
				try {
					helper.assertTrue(shot.arrows.size() == 1, "Boundary shot must emit one arrow");
					float damage = shot.arrows.getFirst().getDamage();
					helper.assertTrue(damage > previous, "Damage dropped at draw tick " + drawTicks + ": " + previous + " -> " + damage);
					previous = damage;
				} finally {
					shot.arrows.forEach(MagicArrowEntity::discard);
				}
			}
		}
		helper.succeed();
	}
}
