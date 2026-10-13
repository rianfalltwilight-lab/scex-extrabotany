package io.github.lounode.extrabotany.common.attachment;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 雷之律者的能量。它是附加在玩家身上的数据。
 * The energy of the Herrscher of Thunder. It is data that is attached to a player.
 *
 * <p>这个对象是可变的。充能时每 tick 都直接修改数值，但是不向客户端同步，因为客户端自己做同样的计算。
 * 只有在数值发生跳变时（反击、释放技能、充满），服务端才同步一次。
 * <p>This object is mutable. During a charge the value changes on each tick with no synchronization to the client, because the client does the same calculation itself.
 * The server only synchronizes when the value makes a jump (a counterattack, a skill use, a full charge).
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherEnergyCapability}。原版在充能的每个 tick 都发送一个同步数据包。
 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherEnergyCapability}. The original sends a synchronization packet on each tick of a charge.
 */
public final class HerrscherEnergy {
	public static final int MAX = 600;

	public static final Codec<HerrscherEnergy> CODEC = Codec.INT.xmap(HerrscherEnergy::new, HerrscherEnergy::get);
	public static final StreamCodec<ByteBuf, HerrscherEnergy> STREAM_CODEC =
			ByteBufCodecs.VAR_INT.map(HerrscherEnergy::new, HerrscherEnergy::get);

	private int energy;

	public HerrscherEnergy() {
		this(0);
	}

	public HerrscherEnergy(int energy) {
		set(energy);
	}

	public int get() {
		return energy;
	}

	public void set(int energy) {
		this.energy = Mth.clamp(energy, 0, MAX);
	}

	public boolean isFull() {
		return energy >= MAX;
	}
}
