package io.github.lounode.extrabotany.common.block.flower.generating;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 鲜血妖姬。它伤害紧挨着它的生物，并用它们的生命产生魔力。同一个生物被伤害的次数越多，产生的魔力越少。
 * Blood Enchantress. It hurts the creatures adjacent to it and makes mana from their health. The more times the same creature is hurt, the less mana it gives.
 *
 * <p>每个生物被伤害的次数只保存在内存里，30 秒没有被访问就会清除。
 * <p>The number of hits for each creature is only kept in memory, and it is cleared when it is not read for 30 seconds.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileBloodyEnchantress}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileBloodyEnchantress} of ExtraMeteorP 1.12.
 */
public class BloodEnchantressBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_BURN_TIME = "burnTime";

	private static final int RANGE = 1;

	public static final int MAX_MANA = 800;
	public static final int DENY_PRODUCE_LEVEL = 35;
	public static final int PRODUCE_MANA = 22;

	private int burnTime;
	private final LoadingCache<UUID, Integer> inhibitionEntityMap = CacheBuilder.newBuilder()
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build(CacheLoader.from(() -> 0));

	public BloodEnchantressBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.BLOOD_ENCHANTRESS, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (getBurnTime() > 0) {
			generateTick();
			setBurnTime(getBurnTime() - 1);
			return;
		}

		if (getMana() >= getMaxMana()) {
			return;
		}

		// One AABB query per tick (was two: getInhibition() scanned the same box again).
		List<LivingEntity> entities = new ArrayList<>();
		for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE))) {
			if (entity.isAlive()) {
				entities.add(entity);
			}
		}
		if (entities.isEmpty()) {
			return;
		}

		int inhibitionLevel = getInhibition(entities);
		if (inhibitionLevel > DENY_PRODUCE_LEVEL) {
			return;
		}

		for (var entity : entities) {
			int entityDamagedTime = inhibitionEntityMap.getUnchecked(entity.getUUID());
			if (entityDamagedTime > 4 && entity.getRandom().nextBoolean()) {
				// if > 4 then 1/2 chance ignore this entity
				continue;
			}
			if (entityDamagedTime > 10) {
				break;
			}

			if (entity.hurt(getLevel().damageSources().generic(), 3F)) {
				addMana((int) (22F * 12F * (1F - 0.04F * entityDamagedTime - 0.02F * inhibitionLevel)));
				inhibitionEntityMap.put(entity.getUUID(), entityDamagedTime + 1);

				setBurnTime(getBurnTime() + 20);
				syncThrottled();
			}
		}

	}

	public void generateTick() {
		if (getMana() >= getMaxMana()) {
			return;
		}
		addMana(getProduceMana());
		syncThrottled();
	}

	public int getInhibition() {
		List<LivingEntity> entities = new ArrayList<>();
		for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE))) {
			if (entity.isAlive()) {
				entities.add(entity);
			}
		}
		return getInhibition(entities);
	}

	private int getInhibition(List<LivingEntity> entities) {
		int inhibitionLevel = 0;
		for (var entity : entities) {
			inhibitionLevel += inhibitionEntityMap.getUnchecked(entity.getUUID());
		}
		return inhibitionLevel;
	}

	public int getProduceMana() {
		return ExtraBotanyConfig.common().bloodEnchantressProduceMana();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().bloodEnchantressMaxMana();
	}

	@Override
	public int getColor() {
		return 0x8B0000;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_BURN_TIME, getBurnTime());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setBurnTime(cmp.getInt(TAG_BURN_TIME));
	}

	public int getBurnTime() {
		return burnTime;
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}
}
