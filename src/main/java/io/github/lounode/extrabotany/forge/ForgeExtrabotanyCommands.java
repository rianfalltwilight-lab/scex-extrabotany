package io.github.lounode.extrabotany.forge;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 本模组的命令。{@code /exbot itemcheck [true|false]} 查看或者修改"盖亚战斗是否检查玩家的物品"。需要 2 级权限。
 * The commands of this mod. {@code /exbot itemcheck [true|false]} shows or changes "if a Gaia fight examines the items of the players". Permission level 2 is necessary.
 */
public final class ForgeExtrabotanyCommands {
    private ForgeExtrabotanyCommands() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("exbot").requires(source -> source.hasPermission(2))
                .then(Commands.literal("itemcheck").executes(context -> {
                    boolean current = ExtraBotanyConfig.common().guardianItemCheck();
                    context.getSource().sendSuccess(() -> Component.translatable("commands.extrabotany.itemcheck.query",
                            Component.translatable("commands.extrabotany.state." + (current ? "on" : "off")).withStyle(current ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
                    return 1;
                }).then(Commands.argument("value", BoolArgumentType.bool()).executes(context -> {
                    boolean enabled = BoolArgumentType.getBool(context, "value"); ForgeExtrabotanyConfig.setGuardianItemCheck(enabled);
                    context.getSource().sendSuccess(() -> Component.translatable("commands.extrabotany.itemcheck." + (enabled ? "on" : "off"))
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), true);
                    return 1;
                }))));
    }
}
