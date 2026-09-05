package io.github.lounode.extrabotany.forge.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import io.github.lounode.extrabotany.client.gui.HUD;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.LoootDataPacket;
import io.github.lounode.extrabotany.network.clientbound.ManaReaderPacket;
import io.github.lounode.extrabotany.network.clientbound.SpawnGaiaPacket;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketExcalibur;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketVoidArchives;

import java.util.function.Consumer;
import java.util.function.Supplier;

import java.util.function.BiConsumer;

public class ForgePacketHandler {
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(LegacyFlameSkillPacket.TYPE, LegacyFlameSkillPacket.CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()));
		registrar.playToServer(LegacySwordPacket.TYPE, LegacySwordPacket.CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()));
		registrar.playToServer(LegacyMountPacket.TYPE, LegacyMountPacket.CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()));

		//ServerBound
		registrar.playToServer(LeftClickPacketExcalibur.ID, LeftClickPacketExcalibur.STREAM_CODEC,
				makeServerBoundHandler(LeftClickPacketExcalibur::handle));
		registrar.playToServer(LeftClickPacketJingwei.ID, LeftClickPacketJingwei.STREAM_CODEC,
				makeServerBoundHandler(LeftClickPacketJingwei::handle));
		registrar.playToServer(LeftClickPacketVoidArchives.ID, LeftClickPacketVoidArchives.STREAM_CODEC,
				makeServerBoundHandler(LeftClickPacketVoidArchives::handle));

		//ClientBound
		registerOperation();
		registrar.playToClient(ManaReaderPacket.ID, ManaReaderPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> ManaReaderPacket.Handler::handle));
		registrar.playToClient(SpawnGaiaPacket.ID, SpawnGaiaPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> SpawnGaiaPacket.Handler::handle));
		registrar.playToClient(ColorfulBossEventPacket.ID, ColorfulBossEventPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> (packet, player) -> HUD.getInstance().getBossOverlay().update(packet)));
		registrar.playToClient(LoootDataPacket.ID, LoootDataPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> LoootDataPacket.Handler::handle));
	}

	private static void registerOperation() {
		ColorfulBossEventPacket.Operation.register("add", () -> ColorfulBossEventPacket.AddOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("remove", () -> ColorfulBossEventPacket.RemoveOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_progress", () -> ColorfulBossEventPacket.UpdateProgressOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_name", () -> ColorfulBossEventPacket.UpdateNameOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_style", () -> ColorfulBossEventPacket.UpdateStyleOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_properties", () -> ColorfulBossEventPacket.UpdatePropertiesOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_player_count", () -> GaiaBossEventPacket.UpdatePlayerCountOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_grain_time", () -> GaiaBossEventPacket.UpdateGrainTimeOperation.CODEC);
	}

	private static <T extends CustomPacketPayload> IPayloadHandler<T> makeServerBoundHandler(BiConsumer<T, ServerPlayer> handler) {
		return (packet, context) -> handler.accept(packet, (ServerPlayer) context.player());
	}

	private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler(Supplier<BiConsumer<T, Player>> handlerSupplier) {
		return (packet, context) -> handlerSupplier.get().accept(packet, context.player());
	}
}
