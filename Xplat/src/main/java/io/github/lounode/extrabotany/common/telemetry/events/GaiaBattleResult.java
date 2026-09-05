package io.github.lounode.extrabotany.common.telemetry.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.Difficulty;

public record GaiaBattleResult(String status, float healthRemain, int playerCount, Difficulty difficulty, int duration) {
	public static final Codec<GaiaBattleResult> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Codec.STRING.fieldOf("status").forGetter(GaiaBattleResult::status),
			Codec.FLOAT.fieldOf("healthRemain").forGetter(GaiaBattleResult::healthRemain),
			Codec.INT.fieldOf("playerCount").forGetter(GaiaBattleResult::playerCount),
			Difficulty.CODEC.fieldOf("difficulty").forGetter(GaiaBattleResult::difficulty),
			Codec.INT.fieldOf("duration").forGetter(GaiaBattleResult::duration))
			.apply(instance, GaiaBattleResult::new)
	);
}
