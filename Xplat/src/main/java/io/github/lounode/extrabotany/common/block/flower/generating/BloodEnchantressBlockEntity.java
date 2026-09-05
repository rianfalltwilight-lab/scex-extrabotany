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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

		int inhibitionLevel = getInhibition();
		if (inhibitionLevel > DENY_PRODUCE_LEVEL) {
			return;
		}

		var entities = getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE)).stream()
				.filter(LivingEntity::isAlive)
				.toList();

		if (entities.isEmpty()) {
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
				sync();
			}
		}

	}

	public void generateTick() {
		if (getMana() >= getMaxMana()) {
			return;
		}
		addMana(getProduceMana());
		sync();
	}

	public int getInhibition() {
		var entities = getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE)).stream()
				.filter(LivingEntity::isAlive)
				.toList();

		int inhibitionLevel = 0;

		for (var entity : entities) {
			int entityDamagedTime = inhibitionEntityMap.getUnchecked(entity.getUUID());

			inhibitionLevel += entityDamagedTime;
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
