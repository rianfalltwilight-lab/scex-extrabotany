package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static io.github.lounode.extrabotany.common.entity.EgoLandmineEntity.TYPE_BLUE;
import static io.github.lounode.extrabotany.common.entity.EgoLandmineEntity.TYPE_GREEN;
import static io.github.lounode.extrabotany.common.entity.EgoLandmineEntity.TYPE_RED;

/**
 * 本我的地雷的八种阵型。
 * The eight formations of the landmines of the Ego.
 *
 * <p>全部阵型以场地中心为中心。下面的数值逐项来自原版，所以阵型的形状和地雷的颜色都和原版相同。
 * <p>All formations have the center of the arena as their center. The values below come item by item from the original, thus the shapes of the formations and the colors of the landmines are the same as in the original.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityEGOLandmine#spawnLandmine}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityEGOLandmine#spawnLandmine}.
 *
 * <p>和原版的差异：阵型的中心是信标方块的中心。原版用方块的角，所以阵型偏了半格。
 * <p>Difference from the original: the center of a formation is the center of the beacon block. The original uses the corner of the block, thus the formations are half a block off center.
 */
public final class EgoLandmineFormations {
	/** 阵型的数量。 / The number of formations. */
	public static final int COUNT = 8;
	/** 定时弹幕按顺序使用的阵型的数量。别的阵型只会被随机选中。 / The number of formations that the timed barrage uses in sequence. The other formations are only selected at random. */
	public static final int BARRAGE_COUNT = 6;

	private static final Vec3 UNIT = new Vec3(2, 0, 0);

	private EgoLandmineFormations() {}

	/**
	 * 排出一个阵型。只在服务端调用。
	 * Puts down one formation. Only call this on the server.
	 *
	 * @param formation 阵型的编号，从 0 到 {@value #COUNT} - 1 / the number of the formation, from 0 to {@value #COUNT} - 1
	 * @param source    场地中心的信标 / the beacon in the center of the arena
	 */
	public static void spawn(int formation, Level level, BlockPos source, Entity owner) {
		Vec3 center = new Vec3(source.getX() + 0.5, source.getY(), source.getZ() + 0.5);
		switch (formation) {
			case 0 -> spawnSpokes(level, owner, center);
			case 1 -> spawnRings(level, owner, center);
			case 2 -> spawnSpiral(level, owner, center);
			case 3 -> spawnRose(level, owner, center);
			case 4 -> spawnOuterCircles(level, owner, center);
			case 5 -> spawnSixFlowers(level, owner, center);
			case 6 -> spawnGear(level, owner, center);
			default -> spawnSnowflake(level, owner, center);
		}
	}

	private static void add(Level level, Entity owner, Vec3 position, int type) {
		level.addFreshEntity(new EgoLandmineEntity(level, owner, type, position));
	}

	private static Vec3 rotate(Vec3 vector, double radians) {
		return vector.yRot((float) radians);
	}

	/** 阵型 0：八条从中心向外的辐条。 / Formation 0: eight spokes from the center. */
	private static void spawnSpokes(Level level, Entity owner, Vec3 center) {
		Vec3 direction = UNIT;
		for (int spoke = 0; spoke < 8; spoke++) {
			// 和原版相同：每条辐条在上一条的基础上再转一个角度，所以辐条不是均匀分布的。
			// The same as in the original: each spoke turns from the direction of the spoke before it, thus the spokes are not at equal angles.
			direction = rotate(direction, Math.PI / 4 * spoke);
			for (int step = 0; step < 8; step++) {
				add(level, owner, center.add(direction.scale(step + 1)), step % 4 == 0 ? TYPE_RED : TYPE_BLUE);
			}
		}
	}

	/** 阵型 1：五个同心圆环，颜色按环交替。 / Formation 1: five concentric rings, with a color for each ring in turn. */
	private static void spawnRings(Level level, Entity owner, Vec3 center) {
		for (int ring = 0; ring < 5; ring++) {
			for (int step = 0; step < 16; step++) {
				Vec3 offset = rotate(UNIT.add(3 * ring, 0, 0), Math.PI / 8 * step);
				add(level, owner, center.add(offset), ring % 3);
			}
		}
	}

	/** 阵型 2：一条螺旋线。 / Formation 2: a spiral. */
	private static void spawnSpiral(Level level, Entity owner, Vec3 center) {
		for (int i = 0; i < 72; i++) {
			double angle = i * Math.PI / 12;
			double radius = 1 + angle;
			add(level, owner, center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle)), i % 5 == 0 ? TYPE_RED : TYPE_BLUE);
		}
	}

	/** 阵型 3：一朵五瓣的玫瑰线。 / Formation 3: a rose curve with five petals. */
	private static void spawnRose(Level level, Entity owner, Vec3 center) {
		for (int i = 0; i < 80; i++) {
			double angle = i * Math.PI / 80;
			double radius = 24 * Math.sin(5 * angle);
			add(level, owner, center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle)), i % 4 == 0 ? TYPE_RED : TYPE_BLUE);
		}
	}

	/** 阵型 4：场地边缘的八个圆。 / Formation 4: eight circles at the edge of the arena. */
	private static void spawnOuterCircles(Level level, Entity owner, Vec3 center) {
		for (int circle = 0; circle < 8; circle++) {
			Vec3 circleCenter = center.add(rotate(UNIT.scale(6), Math.PI / 4 * circle));
			for (int step = 0; step < 16; step++) {
				add(level, owner, circleCenter.add(rotate(UNIT.scale(3), Math.PI / 8 * step)), circle % 3);
			}
		}
	}

	/** 阵型 5：六朵"花"，花心是蓝色，花瓣是红色。 / Formation 5: six "flowers", each with a blue center and red petals. */
	private static void spawnSixFlowers(Level level, Entity owner, Vec3 center) {
		for (int flower = 0; flower < 6; flower++) {
			Vec3 flowerCenter = center.add(rotate(UNIT.scale(5), Math.PI * 2 / 6 * flower));
			add(level, owner, flowerCenter, TYPE_BLUE);
			for (int step = 0; step < 16; step++) {
				add(level, owner, flowerCenter.add(rotate(UNIT.scale(2), Math.PI / 8 * step)), TYPE_RED);
			}
		}
	}

	/** 阵型 6：一个红色的大圆环，环上每隔一段有一个小圆。 / Formation 6: one large red ring, with a small circle at intervals on the ring. */
	private static void spawnGear(Level level, Entity owner, Vec3 center) {
		for (int i = 0; i < 72; i++) {
			Vec3 ringPoint = center.add(rotate(UNIT.scale(7), Math.PI * 2 / 72 * i));
			add(level, owner, ringPoint, TYPE_RED);
			if (i % 5 == 0) {
				for (int step = 0; step < 12; step++) {
					add(level, owner, ringPoint.add(rotate(UNIT.scale(4), Math.PI / 6 * step)), i % 2 == 0 ? TYPE_BLUE : TYPE_GREEN);
				}
			}
		}
	}

	/** 阵型 7：一片雪花。六条绿色的主干，每条主干的中间有一颗小星。 / Formation 7: a snowflake. Six green arms, each with a small star in its middle. */
	private static void spawnSnowflake(Level level, Entity owner, Vec3 center) {
		for (int arm = 0; arm < 6; arm++) {
			for (int step = 0; step < 11; step++) {
				Vec3 armPoint = center.add(rotate(UNIT.scale(step), Math.PI * 2 / 6 * arm));
				add(level, owner, armPoint, TYPE_GREEN);
				if (step == 5) {
					for (int ray = 0; ray < 6; ray++) {
						for (int rayStep = 0; rayStep < 7; rayStep++) {
							Vec3 rayPoint = armPoint.add(rotate(UNIT.scale(rayStep * 0.6), Math.PI * 2 / 6 * ray + Math.PI / 6));
							add(level, owner, rayPoint, ray % 3);
						}
					}
				}
			}
		}
	}
}
