package io.github.lounode.extrabotany.forge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record LegacyFlameSkillPacket(boolean ultimate) implements CustomPacketPayload {
    public static final Type<LegacyFlameSkillPacket> TYPE = new Type<>(ResourceLocation.parse("extrabotany:legacy_flame_skill"));
    public static final StreamCodec<FriendlyByteBuf, LegacyFlameSkillPacket> CODEC = StreamCodec.of((buffer, packet) -> buffer.writeBoolean(packet.ultimate), buffer -> new LegacyFlameSkillPacket(buffer.readBoolean()));
    @Override public Type<LegacyFlameSkillPacket> type() { return TYPE; }
    public void handle(ServerPlayer player) {
        if (ultimate) io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.ultimate(player);
        else io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.dash(player);
    }
}
