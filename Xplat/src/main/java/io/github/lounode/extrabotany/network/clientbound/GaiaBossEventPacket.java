package io.github.lounode.extrabotany.network.clientbound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.BossEvent;

import io.github.lounode.extrabotany.common.bossevents.GaiaBossEvent;

import java.util.UUID;

public class GaiaBossEventPacket {
	public static ColorfulBossEventPacket createPlayersPacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdatePlayerCountOperation(((GaiaBossEvent) event).getPlayerCount()));
	}

	public static ColorfulBossEventPacket createGrainPacket(BossEvent event) {
		return new ColorfulBossEventPacket(event.getId(), new UpdateGrainTimeOperation(((GaiaBossEvent) event).getGrainTime()));
	}

	public record UpdatePlayerCountOperation(int playerCount) implements ColorfulBossEventPacket.Operation {
		public static final MapCodec<UpdatePlayerCountOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("playerCount").forGetter(UpdatePlayerCountOperation::playerCount))
				.apply(instance, UpdatePlayerCountOperation::new)
		);

		@Override
		public String getType() {
			return "update_player_count";
		}

		@Override
		public Codec<? extends ColorfulBossEventPacket.Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, ColorfulBossEventPacket.Handler handler) {
			((Handler) handler).updatePlayerCount(uuid, playerCount());
		}
	}

	public record UpdateGrainTimeOperation(int time) implements ColorfulBossEventPacket.Operation {
		public static final MapCodec<UpdateGrainTimeOperation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("time").forGetter(UpdateGrainTimeOperation::time))
				.apply(instance, UpdateGrainTimeOperation::new)

		);

		static {
			ColorfulBossEventPacket.Operation.register("update_grain_time", () -> CODEC);
		}

		@Override
		public Codec<? extends ColorfulBossEventPacket.Operation> getCodec() {
			return CODEC.codec();
		}

		@Override
		public void dispatch(UUID uuid, ColorfulBossEventPacket.Handler handler) {
			((Handler) handler).updateGrainTime(uuid, time());
		}

		@Override
		public String getType() {
			return "update_grain_time";
		}
	}

	public interface Handler extends ColorfulBossEventPacket.Handler {
		void updatePlayerCount(UUID uuid, int playerCount);
		void updateGrainTime(UUID uuid, int time);
	}
}
