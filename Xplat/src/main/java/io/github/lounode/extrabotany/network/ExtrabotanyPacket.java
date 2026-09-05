package io.github.lounode.extrabotany.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Marker interface for ExtraBotany play payloads.
 *
 * <p>Serialization belongs to each payload's {@code STREAM_CODEC}; this keeps the
 * common packet types usable by NeoForge's typed payload registration.</p>
 */
public interface ExtrabotanyPacket extends CustomPacketPayload {}
