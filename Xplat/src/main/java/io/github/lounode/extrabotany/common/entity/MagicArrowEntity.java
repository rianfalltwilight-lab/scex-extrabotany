package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import vazkii.botania.common.entity.ManaBurstEntity;

public class MagicArrowEntity extends ManaBurstEntity {

	public static final String TAG_DAMAGE = "Damage";
	private float damage;

	public MagicArrowEntity(EntityType<ManaBurstEntity> entityType, Level world) {
		super(entityType, world);
	}

	public MagicArrowEntity(Player player) {
		super(player);
	}

	public MagicArrowEntity(Level level, BlockPos pos, float rotX, float rotY, boolean fake) {
		super(level, pos, rotX, rotY, fake);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat(TAG_DAMAGE, getDamage());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		setDamage(compound.getFloat(TAG_DAMAGE));
	}

	public float getDamage() {
		return this.damage;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}
}
