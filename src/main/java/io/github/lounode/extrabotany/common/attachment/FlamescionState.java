package io.github.lounode.extrabotany.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 薪炎的玩家状态：能量和过载标记。它是附加在玩家身上的数据，不属于某一把武器。
 * The player state of the Flamescion: the energy and the overload flag. It is data that is attached to a player and does not belong to one weapon.
 *
 * <p>这个对象是可变的。能量每 tick 变化，客户端自己做同样的计算，所以服务端不需要每 tick 同步。
 * <p>This object is mutable. The energy changes on each tick and the client does the same calculation itself, so the server does not have to synchronize on each tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code FlamescionCapability}。
 * <p>Source: ExtraMeteorP 1.16 {@code FlamescionCapability}.
 */
public final class FlamescionState {
	public static final int MAX_ENERGY = 600;

	public static final Codec<FlamescionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("energy").forGetter(FlamescionState::getEnergy),
			Codec.BOOL.fieldOf("overloaded").forGetter(FlamescionState::isOverloaded)
	).apply(instance, FlamescionState::new));
	public static final StreamCodec<ByteBuf, FlamescionState> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, FlamescionState::getEnergy,
			ByteBufCodecs.BOOL, FlamescionState::isOverloaded,
			FlamescionState::new);

	private int energy;
	private boolean overloaded;

	public FlamescionState() {
		this(0, false);
	}

	public FlamescionState(int energy, boolean overloaded) {
		setEnergy(energy);
		this.overloaded = overloaded;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = Mth.clamp(energy, 0, MAX_ENERGY);
	}

	/**
	 * 过载时玩家不能进入薪炎模式。过载持续到能量降回零。
	 * A player with an overload cannot go into the Flamescion mode. The overload continues until the energy is back at zero.
	 */
	public boolean isOverloaded() {
		return overloaded;
	}

	public void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}
}
