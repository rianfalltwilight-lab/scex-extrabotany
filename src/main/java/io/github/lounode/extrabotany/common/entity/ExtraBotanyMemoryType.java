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

/**
 * 盖亚守护者的大脑使用的记忆类型：传送的范围和延迟、地雷的数量、小精灵的数量，以及召唤怪物阶段剩余的时间。
 * The memory types that the brain of the Guardian of Gaia uses: the range and the delay of the teleport, the number of landmines, the number of pixies, and the time that is left in the monster stage.
 */
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
