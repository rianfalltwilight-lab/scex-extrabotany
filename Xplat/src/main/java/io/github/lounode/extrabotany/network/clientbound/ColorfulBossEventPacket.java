package io.github.lounode.extrabotany.network.clientbound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.BossEvent;

import io.github.lounode.extrabotany.common.bossevents.ComponentCodec;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record ColorfulBossEventPacket(UUID id, Operation operation) implements ExtrabotanyPacket {

	public static final Codec<BossEvent.BossBarColor> BOSSBAR_COLOR_CODEC = Codec.STRING.xmap(
			BossEvent.BossBarColor::valueOf,
			BossEvent.BossBarColor::name
	);

	public static final Codec<BossEvent.BossBarOverlay> BOSSBAR_OVERLAY_CODEC = Codec.STRING.xmap(
			BossEvent.BossBarOverlay::valueOf,
			BossEvent.BossBarOverlay::name
	);

	public static final CustomPacketPayload.Type<ColorfulBossEventPacket> ID = new CustomPacketPayload.Type<>(prefix("cbp"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ColorfulBossEventPacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> packet.encode(buf),
			ColorfulBossEventPacket::decode
	);

	@Override
	public Type<ColorfulBossEventPacket> type() {
		return ID;
	}

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeUUID(this.id);
		buf.writeJsonWithCodec(Operation.CODEC, operation());
	}

	private static ColorfulBossEventPacket decode(RegistryFriendlyByteBuf buf) {
		return new ColorfulBossEventPacket(buf.readUUID(), buf.readJsonWithCodec(Operation.CODEC));
	}

	public static ColorfulBossEventPacket createAddPacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), ColorfulBossEventPacket.AddOperation.form(event));
	}

	public static ColorfulBossEventPacket createRemovePacket(UUID id) {
		return new ColorfulBossEventPacket(id, new RemoveOperation(id));
	}

	public static ColorfulBossEventPacket createUpdateProgressPacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdateProgressOperation(event.getProgress()));
	}

	public static ColorfulBossEventPacket createUpdateNamePacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdateNameOperation(event.getName()));
	}

	public static ColorfulBossEventPacket createUpdateStylePacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdateStyleOperation(event.getColor(), event.getOverlay()));
	}

	public static ColorfulBossEventPacket createUpdatePropertiesPacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdatePropertiesOperation(event.shouldDarkenScreen(), event.shouldPlayBossMusic(), event.shouldCreateWorldFog()));
	}

	public interface Handler {
		default void add(UUID uuid, Component name, float progress, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {}

		default void remove(UUID uuid) {}

		default void updateProgress(UUID uuid, float progress) {}

		default void updateName(UUID uuid, Component name) {}

		default void updateStyle(UUID uuid, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {}

		default void updateProperties(UUID uuid, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {}
	}

	public void dispatch(Handler handler) {
		this.operation.dispatch(this.id, handler);
	}

	public record AddOperation(Component name, float progress, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) implements Operation {

		public static final MapCodec<AddOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ComponentCodec.CODEC.fieldOf("name").forGetter(AddOperation::name),
				Codec.FLOAT.fieldOf("progress").forGetter(AddOperation::progress),
				BOSSBAR_COLOR_CODEC.fieldOf("color").forGetter(AddOperation::color),
				BOSSBAR_OVERLAY_CODEC.fieldOf("overlay").forGetter(AddOperation::overlay),
				Codec.BOOL.fieldOf("darkenScreen").forGetter(AddOperation::darkenScreen),
				Codec.BOOL.fieldOf("playMusic").forGetter(AddOperation::playMusic),
				Codec.BOOL.fieldOf("createWorldFog").forGetter(AddOperation::createWorldFog)
		).apply(instance, AddOperation::new)
		);

		public static AddOperation form(BossEvent event) {
			return new AddOperation(
					event.getName(),
					event.getProgress(),
					event.getColor(),
					event.getOverlay(),
					event.shouldDarkenScreen(),
					event.shouldPlayBossMusic(),
					event.shouldCreateWorldFog()
			);
		}

		@Override
		public String getType() {
			return "add";
		}

		@Override
		public Codec<? extends Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, Handler handler) {
			handler.add(uuid, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
		}

	}

	public record RemoveOperation(UUID uuid) implements Operation {
		public static final MapCodec<RemoveOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.xmap(UUID::fromString, UUID::toString)
						.fieldOf("uuid")
						.forGetter(RemoveOperation::uuid)
		).apply(instance, RemoveOperation::new)
		);

		@Override
		public String getType() {
			return "remove";
		}

		@Override
		public Codec<? extends Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, ColorfulBossEventPacket.Handler handler) {
			handler.remove(uuid);
		}
	}

	public record UpdateProgressOperation(float progress) implements ColorfulBossEventPacket.Operation {
		public static final MapCodec<UpdateProgressOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.FLOAT.fieldOf("progress").forGetter(UpdateProgressOperation::progress))
				.apply(instance, UpdateProgressOperation::new)
		);

		@Override
		public String getType() {
			return "update_progress";
		}

		@Override
		public Codec<? extends ColorfulBossEventPacket.Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, ColorfulBossEventPacket.Handler handler) {
			handler.updateProgress(uuid, progress());
		}
	}

	public record UpdateNameOperation(Component name) implements ColorfulBossEventPacket.Operation {
		public static final MapCodec<UpdateNameOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ComponentCodec.CODEC.fieldOf("name").forGetter(UpdateNameOperation::name)
		).apply(instance, UpdateNameOperation::new)
		);

		@Override
		public Codec<? extends Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, Handler handler) {
			handler.updateName(uuid, name());
		}

		@Override
		public String getType() {
			return "update_name";
		}
	}

	public record UpdateStyleOperation(BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) implements ColorfulBossEventPacket.Operation {
		public static final MapCodec<UpdateStyleOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BOSSBAR_COLOR_CODEC.fieldOf("color").forGetter(UpdateStyleOperation::color),
				BOSSBAR_OVERLAY_CODEC.fieldOf("overlay").forGetter(UpdateStyleOperation::overlay)
		).apply(instance, UpdateStyleOperation::new)
		);

		@Override
		public Codec<? extends Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, Handler handler) {
			handler.updateStyle(uuid, color(), overlay());
		}

		@Override
		public String getType() {
			return "update_style";
		}
	}

	public record UpdatePropertiesOperation(boolean darkenScreen, boolean playMusic, boolean createWorldFog) implements ColorfulBossEventPacket.Operation {

		public static final MapCodec<UpdatePropertiesOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.fieldOf("darkenScreen").forGetter(UpdatePropertiesOperation::darkenScreen),
				Codec.BOOL.fieldOf("playMusic").forGetter(UpdatePropertiesOperation::playMusic),
				Codec.BOOL.fieldOf("createWorldFog").forGetter(UpdatePropertiesOperation::createWorldFog)
		).apply(instance, UpdatePropertiesOperation::new)
		);

		@Override
		public Codec<? extends Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, Handler handler) {
			handler.updateProperties(uuid, darkenScreen(), playMusic(), createWorldFog());
		}

		@Override
		public String getType() {
			return "update_properties";
		}
	}

	public interface Operation {
		Map<String, Supplier<MapCodec<? extends Operation>>> REGISTRY = new HashMap<>();

		Codec<Operation> CODEC = Codec.STRING.dispatch(
				Operation::getType,
				type -> REGISTRY.getOrDefault(type, () -> {
					throw new IllegalArgumentException("Unknown operation type: " + type);
				}).get()
		);

		Codec<? extends Operation> getCodec();

		void dispatch(UUID uuid, Handler handler);
		String getType();

		static void register(String type, Supplier<MapCodec<? extends Operation>> codecSupplier) {
			REGISTRY.put(type, codecSupplier);
		}
	}
}
