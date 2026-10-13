package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;
import io.github.lounode.extrabotany.common.item.WandOfTheForestItemExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;

/** Contracts formerly guaranteed by the separate SCEX compat JAR (merged in scex.10). */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class WandMergeGameTests {
    private WandMergeGameTests() {}

    private static InteractionResult click(GameTestHelper h, net.minecraft.world.entity.player.Player player, BlockPos absolute) {
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(absolute.getCenter(), Direction.UP, absolute, false)));
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void extraBotanyFlowersExposeWandBindable(GameTestHelper h) {
        var pos = new BlockPos(1, 1, 1);
        h.setBlock(pos, ExtrabotanyFlowerBlocks.reikarlily);
        h.assertTrue(WandBindable.LOOKUP.find(h.getLevel(), h.absolutePos(pos), Direction.UP) != null,
                "ExtraBotany flower is not selectable by the Forest Wand");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void manalinkBindsOnLogicalServerAndPersists(GameTestHelper h) {
        var poolPos = new BlockPos(1, 1, 1);
        var linkPos = new BlockPos(3, 1, 1);
        h.setBlock(poolPos, BotaniaBlocks.MANA_POOL);
        h.setBlock(linkPos, ExtrabotanyFlowerBlocks.manalink);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var wand = new ItemStack(BotaniaItems.WAND_OF_THE_FOREST);
        wand.set(BotaniaDataComponents.WAND_BIND_MODE, Unit.INSTANCE);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        player.setShiftKeyDown(true);

        var absolutePool = h.absolutePos(poolPos);
        click(h, player, absolutePool);
        h.assertTrue(WandOfTheForestItemExtension.getBindingAttempt(wand)
                        .filter(GlobalPos.of(h.getLevel().dimension(), absolutePool)::equals).isPresent(),
                "Pool was not recorded as the extended selection");
        var result = click(h, player, h.absolutePos(linkPos));
        var manalink = (ManalinkBlockEntity) h.getBlockEntity(linkPos);
        h.assertTrue(result.consumesAction(), "Manalink click was not consumed");
        h.assertTrue(manalink.getLinkPos().filter(g -> g.pos().equals(absolutePool)).isPresent(),
                "Server-side Manalink did not receive the link");
        var reloaded = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(manalink.getBlockPos(), manalink.getBlockState(),
                manalink.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(reloaded instanceof ManalinkBlockEntity m && m.getLinkPos().isPresent(), "Link was not persisted");
        h.succeed();
    }
}
