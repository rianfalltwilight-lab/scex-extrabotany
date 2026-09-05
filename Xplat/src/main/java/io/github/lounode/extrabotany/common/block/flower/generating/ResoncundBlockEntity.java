package io.github.lounode.extrabotany.common.block.flower.generating;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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

public class ResoncundBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	public static final String TAG_SOUND_HEARD = "soundHeard";

	private static final int RANGE = 4;
	private static final int CACHE_SIZE = 32;
	private static final int MAX_PRODUCE = 500;
	private static final int MIN_PRODUCE = 0;

	public static final int MAX_MANA = 1200;
	public static final int MANA_LOSS_PER_HEARD = 50;
	private static final Set<ResoncundBlockEntity> LISTENERS = Collections.newSetFromMap(new WeakHashMap<>());

	private final LoadingCache<SoundEvent, Integer> SOUND_HEARD = CacheBuilder.newBuilder()
			.maximumSize(getCacheSize())
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build(CacheLoader.from(() -> 0));

	public ResoncundBlockEntity(BlockPos pos, BlockState blockState) {
		super(ExtrabotanyFlowerBlocks.RESONCUND, pos, blockState);
		register(this);
	}

	public static List<ResoncundBlockEntity> listeners() {
		synchronized (LISTENERS) {
			return new ArrayList<>(LISTENERS);
		}
	}

	private static void register(ResoncundBlockEntity listener) {
		synchronized (LISTENERS) {
			LISTENERS.add(listener);
		}
	}

	private static void unregister(ResoncundBlockEntity listener) {
		synchronized (LISTENERS) {
			LISTENERS.remove(listener);
		}
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (ticksExisted % 20 == 0) {
			sync();
		}
	}

	public void onSoundHeard(SoundEvent soundEvent) {
		int count = SOUND_HEARD.getUnchecked(soundEvent);
		if (count > Integer.MAX_VALUE - 1) {
			count = 0;
		}

		int produce = getMaxProduce();
		produce = produce - (getSoundHeard().get(soundEvent) * getLossPerHeard());
		produce = Math.max(getMinProduce(), produce);

		addMana(produce);

		SOUND_HEARD.put(soundEvent, count + 1);
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

		var aabb = new AABB(getEffectivePos()).inflate(RANGE);
		if (aabb.contains(event.getPosition())) {
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
		var aabb = new AABB(getEffectivePos()).inflate(RANGE);
		if (aabb.contains(event.getEntity().position())) {
			onSoundHeard(event.getSound().value());
		}
	}
}
