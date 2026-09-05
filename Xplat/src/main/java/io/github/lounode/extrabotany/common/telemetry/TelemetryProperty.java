package io.github.lounode.extrabotany.common.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringRepresentable;

import io.github.lounode.extrabotany.common.telemetry.events.GaiaBattleResult;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions.ModLoaderType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public record TelemetryProperty<T> (String id, String exportKey, Codec<T> codec, Exporter<T> exporter) {

	//Device
	public static final TelemetryProperty<String> GAME_VERSION = string("game_version", "minecraftVersion");
	public static final TelemetryProperty<ModLoaderType> MOD_LOADER = create("mod_loader", "modLoader", ModLoaderType.CODEC, (container, key, type) -> container.addProperty(key, type.getSerializedName()));
	public static final TelemetryProperty<String> OS_NAME = string("operating_system", "osName");
	public static final TelemetryProperty<String> OS_ARCH = string("os_arch", "osArch");
	public static final TelemetryProperty<String> OS_VERSION = string("os_version", "osVersion");
	public static final TelemetryProperty<Integer> CORE_COUNT = integer("core_count", "coreCount");
	public static final TelemetryProperty<String> JAVA_VERSION = string("java_version", "javaVersion");
	//Server
	///Use seed and config uuid to make it unique
	public static final TelemetryProperty<UUID> SERVER_UUID = uuid("server_uuid", "serverUUID");
	public static final TelemetryProperty<ServerType> SERVER_TYPE = create("server_type", "serverType", ServerType.CODEC, (container, key, type) -> container.addProperty(key, type.getSerializedName()));
	public static final TelemetryProperty<Boolean> ONLINE_MODE = bool("online_mode", "onlineMode");
	public static final TelemetryProperty<Integer> PLAYER_AMOUNT = integer("player_amount", "playerAmount");
	//
	public static final TelemetryProperty<Integer> MOD_SERVICE_ID = integer("mod_service_id", "modServiceId");
	public static final TelemetryProperty<String> MOD_VERSION = string("mod_version", "modVersion");

	public static final TelemetryProperty<List<GaiaBattleResult>> GAIA_BATTLE_RESULTS =
			create("gaia_battle_results", "gaiaBattleResults", GaiaBattleResult.CODEC.listOf().xmap(ArrayList::new, Function.identity()),
					(container, key, type) -> container.addProperty(
							key,
							type.stream()
									.map(GaiaBattleResult::toString)
									.collect(Collectors.joining(";"))
					));

	public static <T> TelemetryProperty<T> create(String id, String exportKey, Codec<T> codec, Exporter<T> exporter) {
		return new TelemetryProperty<T>(id, exportKey, codec, exporter);
	}

	public static TelemetryProperty<Boolean> bool(String id, String exportKey) {
		return create(id, exportKey, Codec.BOOL, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<String> string(String id, String exportKey) {
		return create(id, exportKey, Codec.STRING, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<Integer> integer(String id, String exportKey) {
		return create(id, exportKey, Codec.INT, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<Long> makeLong(String id, String exportKey) {
		return create(id, exportKey, Codec.LONG, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<UUID> uuid(String id, String exportKey) {
		return create(id, exportKey, UUIDUtil.STRING_CODEC, (container, key, uuid) -> container.addProperty(key, uuid.toString()));
	}

	public static TelemetryProperty<LongList> longSamples(String id, String exportKey) {
		return create(id, exportKey, Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()), (p_261674_, p_262049_, p_262118_) -> p_261674_.addProperty(p_262049_, p_262118_.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))));
	}

	public void export(TelemetryPropertyMap propertyMap, TelemetryPropertyContainer container) {
		T t = propertyMap.get(this);
		if (t != null) {
			this.exporter.apply(container, this.exportKey, t);
		} else {
			container.addNullProperty(this.exportKey);
		}

	}

	public enum GameMode implements StringRepresentable {
		SURVIVAL("survival", 0),
		CREATIVE("creative", 1),
		ADVENTURE("adventure", 2),
		SPECTATOR("spectator", 6),
		HARDCORE("hardcore", 99);

		public static final Codec<GameMode> CODEC = StringRepresentable.fromEnum(GameMode::values);
		private final String key;
		private final int id;

		GameMode(String key, int id) {
			this.key = key;
			this.id = id;
		}

		public int id() {
			return this.id;
		}

		@Override
		public String getSerializedName() {
			return this.key;
		}
	}

	public enum ServerType implements StringRepresentable {
		REALM("realm"),
		LOCAL("local"),
		OTHER("server");

		public static final Codec<ServerType> CODEC = StringRepresentable.fromEnum(TelemetryProperty.ServerType::values);
		private final String key;

		ServerType(String key) {
			this.key = key;
		}

		@Override
		public String getSerializedName() {
			return this.key;
		}
	}

	public interface Exporter<T> {
		void apply(TelemetryPropertyContainer container, String key, T value);
	}
}
