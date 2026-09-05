package io.github.lounode.extrabotany.common.advancements;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.MinMaxBounds;

import java.util.Optional;
import java.util.function.Function;

public final class MinMaxBoundsExtension {
	private MinMaxBoundsExtension() {}

	public record Longs(Optional<Long> min, Optional<Long> max) implements MinMaxBounds<Long> {
		public static final Longs ANY = new Longs(Optional.empty(), Optional.empty());
		public static final Codec<Longs> CODEC = MinMaxBounds.createCodec(Codec.LONG, Longs::new);

		public static Longs exactly(long value) {
			return new Longs(Optional.of(value), Optional.of(value));
		}

		public static Longs between(long min, long max) {
			return new Longs(Optional.of(min), Optional.of(max));
		}

		public static Longs atLeast(long min) {
			return new Longs(Optional.of(min), Optional.empty());
		}

		public static Longs atMost(long max) {
			return new Longs(Optional.empty(), Optional.of(max));
		}

		public boolean matches(long value) {
			return min.map(bound -> bound <= value).orElse(true)
					&& max.map(bound -> bound >= value).orElse(true);
		}

		public static Longs fromReader(StringReader reader) throws CommandSyntaxException {
			return fromReader(reader, Function.identity());
		}

		public static Longs fromReader(StringReader reader, Function<Long, Long> formatter)
				throws CommandSyntaxException {
			return MinMaxBounds.<Long, Longs>fromReader(reader, Longs::create, Long::parseLong,
					CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidLong, formatter);
		}

		private static Longs create(StringReader reader, Optional<Long> min, Optional<Long> max)
				throws CommandSyntaxException {
			if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
				throw MinMaxBounds.ERROR_SWAPPED.createWithContext(reader);
			}
			return new Longs(min, max);
		}
	}
}
