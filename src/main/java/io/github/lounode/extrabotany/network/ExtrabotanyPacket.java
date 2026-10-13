package io.github.lounode.extrabotany.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 本模组所有数据包的标记接口。每个数据包用自己的 {@code STREAM_CODEC} 序列化。
 * The marker interface for all packets of this mod. Each packet is serialized by its own {@code STREAM_CODEC}.
 */
public interface ExtrabotanyPacket extends CustomPacketPayload {}
