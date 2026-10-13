package io.github.lounode.extrabotany.forge.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import io.github.lounode.extrabotany.client.network.ClientPacketHandlers;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.ItemActivationPacket;
import io.github.lounode.extrabotany.network.clientbound.ManaReaderPacket;
import io.github.lounode.extrabotany.network.clientbound.SpawnGaiaPacket;
import io.github.lounode.extrabotany.network.serverbound.FlamescionSkillPacket;
import io.github.lounode.extrabotany.network.serverbound.HerrscherSkillPacket;
import io.github.lounode.extrabotany.network.serverbound.LeftClickEmptyPacket;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketExcalibur;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketVoidArchives;
import io.github.lounode.extrabotany.network.serverbound.MountInputPacket;
import io.github.lounode.extrabotany.network.serverbound.MountSkillPacket;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 登记本模组的所有数据包和它们的处理方法。
 * Records all packets of this mod and their handlers.
 */
public class ForgePacketHandler {
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(FlamescionSkillPacket.ID, FlamescionSkillPacket.STREAM_CODEC,
				makeServerBoundHandler(FlamescionSkillPacket::handle));
		registrar.playToServer(LeftClickEmptyPacket.ID, LeftClickEmptyPacket.STREAM_CODEC,
				makeServerBoundHandler(LeftClickEmptyPacket::handle));
		registrar.playToServer(MountInputPacket.ID, MountInputPacket.STREAM_CODEC,
				makeServerBoundHandler(MountInputPacket::handle));
		registrar.playToServer(MountSkillPacket.ID, MountSkillPacket.STREAM_CODEC,
				makeServerBoundHandler(MountSkillPacket::handle));
		registrar.playToServer(HerrscherSkillPacket.ID, HerrscherSkillPacket.STREAM_CODEC,
				makeServerBoundHandler(HerrscherSkillPacket::handle));

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
				makeClientBoundHandler(() -> ClientPacketHandlers::handleManaReader));
		registrar.playToClient(SpawnGaiaPacket.ID, SpawnGaiaPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> ClientPacketHandlers::handleSpawnGaia));
		registrar.playToClient(ColorfulBossEventPacket.ID, ColorfulBossEventPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> ClientPacketHandlers::handleBossEvent));
		registrar.playToClient(ItemActivationPacket.ID, ItemActivationPacket.STREAM_CODEC,
				makeClientBoundHandler(() -> ClientPacketHandlers::handleItemActivation));
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
		return (packet, context) -> {
			if (context.player() instanceof ServerPlayer player) handler.accept(packet, player);
		};
	}

	/**
	 * 包装一个客户端的处理方法。
	 * Wraps a handler of the client.
	 *
	 * <p>处理方法在客户端的类里，专用服务器上没有那些类。所以这里接收的是一个 Supplier：只有在数据包到达客户端时才取出处理方法，那时才加载客户端的类。
	 * <p>The handlers are in a client class, and a dedicated server does not have those classes. Thus this method gets a Supplier: the handler is only taken from it when a packet arrives on the client, and only then the client class is loaded.
	 */
	private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler(Supplier<BiConsumer<T, Player>> handlerSupplier) {
		return (packet, context) -> handlerSupplier.get().accept(packet, context.player());
	}
}
