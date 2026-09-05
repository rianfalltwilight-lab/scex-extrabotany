package io.github.lounode.extrabotany.forge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** The server resolves held item, ownership, mana and attack timing itself. */
public record LegacySwordPacket() implements CustomPacketPayload {
    public static final LegacySwordPacket INSTANCE = new LegacySwordPacket();
    public static final Type<LegacySwordPacket> TYPE = new Type<>(ResourceLocation.parse("extrabotany:legacy_sword_attack"));
    public static final StreamCodec<FriendlyByteBuf, LegacySwordPacket> CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<LegacySwordPacket> type() { return TYPE; }
    public void handle(ServerPlayer player) {
        io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.attack(player);
        if (player.getMainHandItem().getItem() instanceof io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword sword) sword.tryUse(player, null);
    }
}
