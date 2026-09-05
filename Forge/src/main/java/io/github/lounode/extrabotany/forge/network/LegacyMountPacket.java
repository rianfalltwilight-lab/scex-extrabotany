package io.github.lounode.extrabotany.forge.network;

import io.github.lounode.extrabotany.common.entity.LegacyMount;
import io.github.lounode.extrabotany.common.item.legacy.LegacyMountItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import vazkii.botania.common.handler.EquipmentHandler;

public record LegacyMountPacket(int controls, boolean special, boolean summon) implements CustomPacketPayload {
    public static final Type<LegacyMountPacket> TYPE = new Type<>(ResourceLocation.parse("extrabotany:legacy_mount_input"));
    public static final StreamCodec<FriendlyByteBuf, LegacyMountPacket> CODEC = StreamCodec.of(
            (buffer, packet) -> { buffer.writeByte(packet.controls); buffer.writeBoolean(packet.special); buffer.writeBoolean(packet.summon); },
            buffer -> new LegacyMountPacket(buffer.readUnsignedByte(), buffer.readBoolean(), buffer.readBoolean()));
    @Override public Type<LegacyMountPacket> type() { return TYPE; }
    public void handle(ServerPlayer player) {
        if (summon) {
            if (player.getVehicle() != null) return;
            var stack = EquipmentHandler.findOrEmpty(candidate -> candidate.getItem() instanceof LegacyMountItems.Accessory, player);
            if (!(stack.getItem() instanceof LegacyMountItems.Accessory accessory)) return;
            var mount = accessory.create(player.level()); mount.setPos(player.getX(), player.getY() + .5, player.getZ()); mount.setYRot(player.getYRot());
            if (player.level().addFreshEntity(mount)) player.startRiding(mount);
        } else if (player.getVehicle() instanceof LegacyMount mount && mount.getControllingPassenger() == player) mount.input(controls, special);
    }
}
