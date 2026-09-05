package io.github.lounode.extrabotany.common.telemetry;

import com.google.gson.JsonObject;
import com.mojang.serialization.*;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TelemetryPropertyMap {
	final Map<TelemetryProperty<?>, Object> entries;

	TelemetryPropertyMap(Map<TelemetryProperty<?>, Object> entries) {
		this.entries = entries;
	}

	public static TelemetryPropertyMap.Builder builder() {
		return new TelemetryPropertyMap.Builder();
	}

	public static Codec<TelemetryPropertyMap> createCodec(final List<TelemetryProperty<?>> properties) {
		return (new MapCodec<TelemetryPropertyMap>() {
			@Override
			public <T> RecordBuilder<T> encode(TelemetryPropertyMap map, DynamicOps<T> ops, RecordBuilder<T> builder) {
				RecordBuilder<T> recordbuilder = builder;

				for (TelemetryProperty<?> telemetryproperty : properties) {
					recordbuilder = this.encodeProperty(map, recordbuilder, telemetryproperty);
				}

				return recordbuilder;
			}

			private <T, V> RecordBuilder<T> encodeProperty(TelemetryPropertyMap map, RecordBuilder<T> builder, TelemetryProperty<V> key) {
				V v = map.get(key);
				return v != null ? builder.add(key.id(), v, key.codec()) : builder;
			}

			@Override
			public <T> DataResult<TelemetryPropertyMap> decode(DynamicOps<T> ops, MapLike<T> p_262176_) {
				DataResult<TelemetryPropertyMap.Builder> dataresult = DataResult.success(new TelemetryPropertyMap.Builder());

				for (TelemetryProperty<?> telemetryproperty : properties) {
					dataresult = this.decodeProperty(dataresult, ops, p_262176_, telemetryproperty);
				}

				return dataresult.map(TelemetryPropertyMap.Builder::build);
			}

			private <T, V> DataResult<TelemetryPropertyMap.Builder> decodeProperty(DataResult<TelemetryPropertyMap.Builder> p_261892_, DynamicOps<T> p_261859_, MapLike<T> p_261668_, TelemetryProperty<V> p_261627_) {
				T t = p_261668_.get(p_261627_.id());
				if (t != null) {
					DataResult<V> dataresult = p_261627_.codec().parse(p_261859_, t);
					return p_261892_.apply2stable((p_262028_, p_261796_) -> {
						return p_262028_.put(p_261627_, p_261796_);
					}, dataresult);
				} else {
					return p_261892_;
				}
			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> p_261746_) {
				return properties.stream().map(TelemetryProperty::id).map(p_261746_::createString);
			}
		}).codec();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T get(TelemetryProperty<T> key) {
		return (T) this.entries.get(key);
	}

	@Override
	public String toString() {
		return this.entries.toString();
	}

	public Set<TelemetryProperty<?>> propertySet() {
		return this.entries.keySet();
	}

	public JsonObject toJson() {
		Codec<TelemetryPropertyMap> codec = createCodec(propertySet().stream().toList());
		return codec.encodeStart(JsonOps.INSTANCE, this)
				.result()
				.orElseThrow()
				.getAsJsonObject();
	}

	public static class Builder {
		private final Map<TelemetryProperty<?>, Object> entries = new Reference2ObjectOpenHashMap<>();

		Builder() {}

		public <T> TelemetryPropertyMap.Builder put(TelemetryProperty<T> key, T value) {
			this.entries.put(key, value);
			return this;
		}

		public <T> TelemetryPropertyMap.Builder putIfNotNull(TelemetryProperty<T> key, @Nullable T value) {
			if (value != null) {
				this.entries.put(key, value);
			}

			return this;
		}

		public TelemetryPropertyMap.Builder putAll(TelemetryPropertyMap propertyMap) {
			this.entries.putAll(propertyMap.entries);
			return this;
		}

		public TelemetryPropertyMap build() {
			return new TelemetryPropertyMap(this.entries);
		}
	}
}
