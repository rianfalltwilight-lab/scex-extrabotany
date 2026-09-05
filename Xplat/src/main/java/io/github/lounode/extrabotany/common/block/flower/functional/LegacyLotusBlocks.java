package io.github.lounode.extrabotany.common.block.flower.functional;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.legacy.LegacyBinderItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.flower.PoweredFloatingSpecialFlowerBlock;
import vazkii.botania.common.block.flower.PoweredSpecialFlowerBlock;

public final class LegacyLotusBlocks {
    public static final Block FLOWER = new PoweredSpecialFlowerBlock(MobEffects.LEVITATION, 0,
            BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.WHITE_MYSTICAL_FLOWER), () -> ExtrabotanyFlowerBlocks.STARDUST_LOTUS) {
        @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { return bind(stack, level, pos, player); }
    };
    public static final Block FLOATING = new PoweredFloatingSpecialFlowerBlock(
            BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.WHITE_FLOATING_FLOWER), () -> ExtrabotanyFlowerBlocks.STARDUST_LOTUS) {
        @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { return bind(stack, level, pos, player); }
    };
    public static final Block POTTED = io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.flowerPot(FLOWER, 0);

    private static ItemInteractionResult bind(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!(stack.getItem() instanceof LegacyBinderItem)
                || !(level.getBlockEntity(pos) instanceof StardustLotusBlockEntity lotus))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String dimension = tag.contains("dim") ? tag.getString("dim") : "minecraft:overworld";
        if (!level.isClientSide()) {
            if (!dimension.equals(level.dimension().location().toString()) || !tag.contains("posy") || tag.getInt("posy") == -1) {
                player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.invalid_bind"), true);
            } else {
                var target = new BlockPos(tag.getInt("posx"), tag.getInt("posy"), tag.getInt("posz"));
                lotus.setTarget(target);
                player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.bind_to_pos",
                        target.getX(), target.getY(), target.getZ()), true);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    private LegacyLotusBlocks() {}
}
