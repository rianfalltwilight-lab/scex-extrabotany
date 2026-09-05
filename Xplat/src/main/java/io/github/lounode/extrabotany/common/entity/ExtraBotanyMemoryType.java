package io.github.lounode.extrabotany.common.entity;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import io.github.lounode.extrabotany.common.lib.LibMemoryNames;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyMemoryType {
	public static final Map<ResourceLocation, MemoryModuleType<?>> ALL = new LinkedHashMap<>();

	public static final MemoryModuleType<Integer> TELEPORT_DELAY_BASE = register(prefix(LibMemoryNames.TELEPORT_DELAY_BASE), Codec.INT);
	public static final MemoryModuleType<Integer> TELEPORT_DELAY = register(prefix(LibMemoryNames.TELEPORT_DELAY), Codec.INT);
	public static final MemoryModuleType<Float> TELEPORT_RANGE = register(prefix(LibMemoryNames.TELEPORT_RANGE), Codec.FLOAT);
	public static final MemoryModuleType<Integer> LANDMINE_COUNT = register(prefix(LibMemoryNames.LANDMINE_COUNT), Codec.INT);
	public static final MemoryModuleType<Integer> PIXIES_MAX = register(prefix(LibMemoryNames.PIXIES_MAX), Codec.INT);
	public static final MemoryModuleType<Integer> MOB_SPAWN_TICKS = register(prefix(LibMemoryNames.MOB_SPAWN_TICKS), Codec.INT);

	private static <T> MemoryModuleType<T> register(ResourceLocation id, Codec<T> codec) {
		MemoryModuleType<T> type = new MemoryModuleType<>(Optional.of(codec));
		ALL.put(id, type);
		return type;
	}

	public static void registerMemories(BiConsumer<MemoryModuleType<?>, ResourceLocation> consumer) {
		for (var memory : ALL.entrySet()) {
			consumer.accept(memory.getValue(), memory.getKey());
		}
	}
}
