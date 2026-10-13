package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import vazkii.botania.common.entity.ManaBurstEntity;

/**
 * 百中弓射出的魔力脉冲。它带有一个伤害值，百中弓在脉冲碰到生物时读取它。
 * The mana burst that the Failnaught shoots. It holds a damage value, and the Failnaught reads it when the burst touches a creature.
 */
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
