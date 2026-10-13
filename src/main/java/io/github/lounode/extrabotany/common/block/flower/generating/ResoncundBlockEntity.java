package io.github.lounode.extrabotany.common.block.flower.generating;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 回音花。它听到范围里的声音时产生魔力。同一种声音听得越多，产生的魔力越少。一种声音 30 秒没有出现之后，它的计数被清除。
 * Resoncund. It makes mana when it hears a sound in the range. The more times it hears the same sound, the less mana that sound gives. When a sound does not occur for 30 seconds, its count is cleared.
 *
 * <p>所有服务端的回音花登记在一张静态的表里，声音事件从这张表里找到它们。花被移除或者区块卸载时，它从表里注销。
 * <p>All the Resoncund flowers of the server are recorded in a static list, and the sound events find them in this list. When a flower is removed or its chunk is unloaded, it is removed from the list.
 *
 * <p>来源：重制版（Lounode）。原版没有这朵花。
 * <p>Source: Reburn (Lounode). The original does not have this flower.
 */
public class ResoncundBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	public static final String TAG_SOUND_HEARD = "soundHeard";

	private static final int RANGE = 4;
	private static final int CACHE_SIZE = 32;
	private static final int MAX_PRODUCE = 500;
	private static final int MIN_PRODUCE = 0;

	public static final int MAX_MANA = 1200;
	public static final int MANA_LOSS_PER_HEARD = 50;
	private static final Set<ResoncundBlockEntity> LISTENERS = Collections.newSetFromMap(new WeakHashMap<>());
	/**
	 * Copy-on-write snapshot read by the sound event bridge. Sound events fire many times per
	 * tick; {@link #listeners()} used to allocate a fresh ArrayList under a lock for each one.
	 */
	private static volatile List<ResoncundBlockEntity> snapshot = List.of();

	private final LoadingCache<SoundEvent, Integer> SOUND_HEARD = CacheBuilder.newBuilder()
			.maximumSize(getCacheSize())
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build(CacheLoader.from(() -> 0));

	public ResoncundBlockEntity(BlockPos pos, BlockState blockState) {
		super(ExtrabotanyFlowerBlocks.RESONCUND, pos, blockState);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		// 只有服务端的花监听声音。客户端的花不产生魔力。
		// Only the flower on the server listens to sounds. The flower on the client makes no mana.
		if (!level.isClientSide()) {
			register(this);
		}
	}

	public static List<ResoncundBlockEntity> listeners() {
		return snapshot;
	}

	private static void register(ResoncundBlockEntity listener) {
		synchronized (LISTENERS) {
			if (LISTENERS.add(listener)) {
				snapshot = List.copyOf(LISTENERS);
			}
		}
	}

	private static void unregister(ResoncundBlockEntity listener) {
		synchronized (LISTENERS) {
			if (LISTENERS.remove(listener)) {
				snapshot = List.copyOf(LISTENERS);
			}
		}
	}


	@Override
	public void setRemoved() {
		// Broken or chunk-unloaded flowers stopped listening only when the next sound arrived.
		unregister(this);
		super.setRemoved();
	}

	@Override
	public void clearRemoved() {
		super.clearRemoved();
		if (level != null && !level.isClientSide()) {
			register(this);
		}
	}

	/** Same volume as {@code new AABB(getEffectivePos()).inflate(RANGE).contains(...)}, without allocating. */
	private boolean inRange(double x, double y, double z) {
		BlockPos p = getEffectivePos();
		return x >= p.getX() - RANGE && x < p.getX() + 1 + RANGE
				&& y >= p.getY() - RANGE && y < p.getY() + 1 + RANGE
				&& z >= p.getZ() - RANGE && z < p.getZ() + 1 + RANGE;
	}

	public void onSoundHeard(SoundEvent soundEvent) {
		int count = SOUND_HEARD.getUnchecked(soundEvent);
		long decayedProduce = (long) getMaxProduce() - (long) count * getLossPerHeard();
		int produce = (int) Math.max(getMinProduce(), decayedProduce);

		addMana(produce);

		// Repeated sounds stay exhausted until their normal cache expiry, even at
		// the persisted counter limit. Wrapping would restart their mana reward.
		SOUND_HEARD.put(soundEvent, count == Integer.MAX_VALUE ? count : count + 1);
		// 听过的声音要存档。客户端不需要这份数据，所以不用另外同步。以前这朵花每秒发一个完整的同步数据包。
		// The sounds that the flower heard must be saved. The client does not use this data, thus no additional synchronization is necessary. Before, this flower sent a full synchronization packet on each second.
		setChanged();
	}

	public int getLossPerHeard() {
		return ExtraBotanyConfig.common().resoncundLossPerHeard();
	}

	public int getMinProduce() {
		return MIN_PRODUCE;
	}

	public int getMaxProduce() {
		return MAX_PRODUCE;
	}

	public int getCacheSize() {
		return CACHE_SIZE;
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().resoncundMaxMana();
	}

	@Override
	public int getColor() {
		return 0xF54DAF;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	public Map<SoundEvent, Integer> getSoundHeard() {
		return SOUND_HEARD.asMap();
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		CompoundTag sounds = new CompoundTag();

		for (var entry : getSoundHeard().entrySet()) {
			SoundEvent sound = entry.getKey();
			int heards = entry.getValue();

			sounds.putInt(sound.getLocation().toString(), heards);
		}

		cmp.put(TAG_SOUND_HEARD, sounds);
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		CompoundTag sounds = cmp.getCompound(TAG_SOUND_HEARD);

		Map<SoundEvent, Integer> soundHeard = new ConcurrentHashMap<>();
		for (var key : sounds.getAllKeys()) {
			if (soundHeard.size() >= getCacheSize()) {
				break;
			}
			ResourceLocation res = ResourceLocation.tryParse(key);
			int heards = sounds.getInt(key);
			if (heards < 0) {
				continue;
			}
			BuiltInRegistries.SOUND_EVENT.getOptional(res).ifPresent(soundEvent -> {
				soundHeard.put(soundEvent, heards);
			});
		}

		SOUND_HEARD.invalidateAll();
		SOUND_HEARD.putAll(soundHeard);
	}

	public void onPlayLevelSound(PlayLevelSoundEvent.AtPosition event) {
		if (getLevel() == null) {
			return;
		}
		if (getLevel().isClientSide()) {
			unregister(this);
			return;
		}
		if (this.isRemoved()) {
			unregister(this);
			return;
		}
		if (event.getLevel() != getLevel()) {
			return;
		}
		if (event.getSound() == null) {
			return;
		}

		var position = event.getPosition();
		if (inRange(position.x, position.y, position.z)) {
			onSoundHeard(event.getSound().value());
		}
	}

	public void onPlayLevelSound(PlayLevelSoundEvent.AtEntity event) {
		if (getLevel() == null) {
			return;
		}
		if (getLevel().isClientSide()) {
			unregister(this);
			return;
		}
		if (this.isRemoved()) {
			unregister(this);
			return;
		}
		if (event.getLevel() != getLevel()) {
			return;
		}
		if (event.getSound() == null) {
			return;
		}
		var entity = event.getEntity();
		if (inRange(entity.getX(), entity.getY(), entity.getZ())) {
			onSoundHeard(event.getSound().value());
		}
	}
}
