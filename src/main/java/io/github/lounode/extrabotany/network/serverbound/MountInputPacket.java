package io.github.lounode.extrabotany.network.serverbound;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 客户端报告骑手当前按下的按键。客户端只在按键状态改变时发送。
 * The client reports the keys that the rider holds at this time. The client only sends the packet when the key state changes.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code MountableUpdatePack}。原版只同步跳跃键和疾跑键。这里同步全部六个按键，这样其他玩家也能看到车身倾斜。
 * <p>Source: ExtraMeteorP 1.16 {@code MountableUpdatePack}. The original only synchronizes the jump key and the sprint key. This port synchronizes all six keys, so that other players can also see the body lean.
 *
 * @param input {@link MountEntity} 的 {@code INPUT_*} 常量的按位组合。 A bitwise combination of the {@code INPUT_*} constants of {@link MountEntity}.
 */
public record MountInputPacket(int input) implements ExtrabotanyPacket {
	public static final CustomPacketPayload.Type<MountInputPacket> ID =
			new CustomPacketPayload.Type<>(prefix("mount_input"));
	public static final StreamCodec<ByteBuf, MountInputPacket> STREAM_CODEC =
			ByteBufCodecs.BYTE.map(value -> new MountInputPacket(value & MountEntity.INPUT_MASK), packet -> (byte) packet.input());

	@Override
	public Type<MountInputPacket> type() {
		return ID;
	}

	public void handle(ServerPlayer player) {
		if (player.getVehicle() instanceof MountEntity mount && mount.getControllingPassenger() == player) {
			mount.setInput(input);
		}
	}
}
