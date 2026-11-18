package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import vazkii.botania.common.entity.PixieEntity;

/**
 * 暗影妖精。暗影之刃召唤它去攻击目标。行为和植物魔法的妖精相同，只是外观是黑色的。
 * The dark pixie. The Shadow Katana summons it to attack a target. It behaves as a pixie of Botania, but it looks dark.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityDarkPixie} 和 {@code RenderDarkPixie}（生命 2）。
 * <p>Source: {@code EntityDarkPixie} and {@code RenderDarkPixie} of ExtraMeteorP 1.12 (2 health).
 */
public class DarkPixieEntity extends PixieEntity {
	public DarkPixieEntity(EntityType<PixieEntity> type, Level level) {
		super(type, level);
	}

	public DarkPixieEntity(Level level) {
		this(ExtraBotanyEntityType.DARK_PIXIE, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0);
	}
}
