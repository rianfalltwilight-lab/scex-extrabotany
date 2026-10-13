package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.client.fx.SparkleParticleData;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import vazkii.botania.common.helper.PlayerHelper;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 星尘莲。它用魔力打开一个传送门，把旁边的生物传送到绑定的坐标。
 * Stardust Lotus. It uses mana to open a portal that teleports the adjacent creatures to the bound position.
 *
 * <p>使用方法：
 * <p>How to use it:
 * <ol>
 * <li>搭好多方块结构：花的下一层是 5x5 的石英块外圈，四个角各有两格高的青金石块柱子，柱子顶上是海晶灯。
 * Build the multiblock structure: one level below the flower there is the outer ring of a 5x5 square of quartz blocks, each of the four corners has a pillar of two lapis blocks, and there is a sea lantern on top of each pillar.</li>
 * <li>用坐标读取器右键这朵花，设定目标。目标必须在同一个维度。
 * Use the Position Reader on the flower to set the target. The target must be in the same dimension.</li>
 * <li>把一张纸丢在花旁边。花开始消耗魔力。
 * Drop one paper adjacent to the flower. The flower starts to use mana.</li>
 * <li>魔力够了之后传送门打开。花周围 1 格内的生物被传送到目标上方。
 * When there is sufficient mana, the portal opens. The creatures within 1 block of the flower are teleported to the position above the target.</li>
 * </ol>
 *
 * <p>花费是"基础值 + 距离 x 每格的值"，默认是 20000 + 距离 x 150。传送门打开之后一直保持打开，这和原版相同。目标改得更远时，花要补足差额。红石信号让花停止工作。
 * <p>The cost is "base value + distance x value for each block", by default 20000 + distance x 150. After the portal opens it stays open, which is the same as in the original. When the target is changed to a farther position, the flower must get the difference. A redstone signal stops the flower.
 *
 * <p>数据的键名是 {@code consumedMana}、{@code hasPaper}、{@code hasTarget}、{@code targetX}、{@code targetY} 和 {@code targetZ}。旧存档使用相同的键名。
 * <p>The keys of the data are {@code consumedMana}, {@code hasPaper}, {@code hasTarget}, {@code targetX}, {@code targetY} and {@code targetZ}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code SubTileStardustLotus}。
 * <p>Source: ExtraMeteorP 1.12 {@code SubTileStardustLotus}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>没有移植"自然蕴息宝珠让花费减少 20%"的规则。原版 1.12 的 6 种花有这个规则，这个移植版的花都没有它。
 * The rule "a Nature Orb decreases the cost by 20%" is not ported. Six flowers of the original 1.12 have this rule, and no flower of this port has it.</li>
 * <li>生物被传送到目标方块的中心上方。原版传送到方块的角上。
 * A creature is teleported to the position above the center of the target block. The original teleports to the corner of the block.</li>
 * <li>刚被传送的生物在 {@value #TELEPORT_COOLDOWN} tick 内不会再被星尘莲传送。原版里两朵互相指向的星尘莲会让生物不停地来回传送。
 * A creature that was teleported is not teleported again by a Stardust Lotus for {@value #TELEPORT_COOLDOWN} ticks. In the original, two lotuses that point at each other teleport a creature back and forth without end.</li>
 * <li>花只消耗刚好的魔力。原版每次消耗固定的一份，所以总量会超过花费。
 * The flower only uses the exact quantity of mana. The original uses a fixed lot each time, thus the total is more than the cost.</li>
 * <li>旁观者不会被传送。
 * A spectator is not teleported.</li>
 * </ul>
 *
 * <p>性能：多方块结构每 {@value #STRUCTURE_CHECK_INTERVAL} tick 检查一次。查找纸和生物没有结果时，花休眠几 tick。原版每 tick 检查 28 个方块，并且每 tick 查找两次实体。
 * <p>Performance: the multiblock structure is examined one time for each {@value #STRUCTURE_CHECK_INTERVAL} ticks. When a search for paper or creatures finds nothing, the flower sleeps for some ticks. The original examines 28 blocks on each tick and searches for entities two times on each tick.
 */
public class StardustLotusBlockEntity extends ExtraFunctionalFlowerBlockEntity {
	private static final String TAG_CONSUMED_MANA = "consumedMana";
	private static final String TAG_HAS_PAPER = "hasPaper";
	private static final String TAG_HAS_TARGET = "hasTarget";
	private static final String TAG_TARGET_X = "targetX";
	private static final String TAG_TARGET_Y = "targetY";
	private static final String TAG_TARGET_Z = "targetZ";

	private static final int RANGE = 1;
	private static final int COLOR = 0x800080;
	private static final int STRUCTURE_CHECK_INTERVAL = 20;
	private static final int TELEPORT_COOLDOWN = 40;
	private static final int[][] PILLARS = { { -2, -2 }, { -2, 2 }, { 2, -2 }, { 2, 2 } };

	@Nullable
	private BlockPos target;
	private int consumedMana;
	private boolean hasPaper;

	private boolean structureValid;
	private long nextStructureCheck;

	public StardustLotusBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.STARDUST_LOTUS, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		Level level = getLevel();
		if (target == null || isPowered() || !isStructureValidCached(level)) {
			return;
		}
		int cost = getTeleportCost();
		if (level.isClientSide()) {
			spawnParticles(level, cost);
			return;
		}

		if (!hasPaper) {
			if (isIdleBackoff()) {
				return;
			}
			if (!takePaper(level)) {
				backoffIdle(IDLE_BACKOFF_TICKS);
				return;
			}
		}
		if (consumedMana < cost) {
			int amount = Math.min(Math.min(ExtraBotanyConfig.common().stardustLotusConsumeSpeed(), getMana()), cost - consumedMana);
			if (amount > 0) {
				addMana(-amount);
				consumedMana += amount;
				syncThrottled();
			}
			return;
		}
		if (!isIdleBackoff() && !teleportCreatures(level)) {
			backoffIdle(IDLE_BACKOFF_TICKS);
		}
	}

	/**
	 * 拿走旁边的一张纸。拿到时返回 {@code true}。
	 * Takes one paper that is adjacent to the flower. Returns {@code true} when it got one.
	 */
	private boolean takePaper(Level level) {
		List<ItemEntity> papers = level.getEntitiesOfClass(ItemEntity.class, getWorkArea(),
				entity -> entity.isAlive() && entity.getItem().is(Items.PAPER));
		if (papers.isEmpty()) {
			return false;
		}
		ItemEntity paper = papers.getFirst();
		ItemStack remaining = paper.getItem().copy();
		remaining.shrink(1);
		if (remaining.isEmpty()) {
			paper.discard();
		} else {
			paper.setItem(remaining);
		}
		hasPaper = true;
		sync();
		return true;
	}

	/**
	 * 传送范围内的生物。至少传送了一个时返回 {@code true}。
	 * Teleports the creatures in the range. Returns {@code true} when it teleported one or more.
	 */
	private boolean teleportCreatures(Level level) {
		List<LivingEntity> creatures = level.getEntitiesOfClass(LivingEntity.class, getWorkArea(),
				entity -> entity.isAlive() && !entity.isSpectator() && !entity.isOnPortalCooldown());
		for (LivingEntity creature : creatures) {
			creature.teleportTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);
			creature.setPortalCooldown(TELEPORT_COOLDOWN);
			if (creature instanceof ServerPlayer player) {
				PlayerHelper.grantCriterion(player, prefix("main/" + LibAdvancementNames.STARDUSTLOTUS_TELEPORT), "code_triggered");
			}
		}
		return !creatures.isEmpty();
	}

	private void spawnParticles(Level level, int cost) {
		if (consumedMana <= 0 || cost <= 0) {
			return;
		}
		BlockPos pos = getEffectivePos();
		// 一圈光点表示进度。整圈亮起时传送门已经打开。
		// A ring of sparkles shows the progress. When the full ring is on, the portal is open.
		int degrees = (int) Math.min(360L, 360L * consumedMana / cost);
		for (int i = 0; i < degrees; i += 15) {
			double angle = Math.toRadians(i);
			level.addParticle(SparkleParticleData.sparkle(1F, 1F, 0.1F, 1F, 10),
					pos.getX() + 0.5 - Math.cos(angle) * 2, pos.getY() + 0.2, pos.getZ() + 0.5 - Math.sin(angle) * 2, 0, 0, 0);
		}
		if (consumedMana >= cost) {
			level.addParticle(SparkleParticleData.sparkle(5F, 1F, 0.1F, 1F, 10), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0);
		}
	}

	private AABB getWorkArea() {
		return new AABB(getEffectivePos()).inflate(RANGE);
	}

	private boolean isStructureValidCached(Level level) {
		long now = level.getGameTime();
		if (now >= nextStructureCheck) {
			structureValid = isStructureValid();
			nextStructureCheck = now + STRUCTURE_CHECK_INTERVAL;
		}
		return structureValid;
	}

	/**
	 * 检查多方块结构是否完整。
	 * Examines if the multiblock structure is complete.
	 */
	public boolean isStructureValid() {
		Level level = getLevel();
		BlockPos center = getEffectivePos();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				boolean outerRing = Math.abs(x) == 2 || Math.abs(z) == 2;
				if (outerRing && !isBlock(level, pos.setWithOffset(center, x, -1, z), Blocks.QUARTZ_BLOCK)) {
					return false;
				}
			}
		}
		for (int[] pillar : PILLARS) {
			if (!isBlock(level, pos.setWithOffset(center, pillar[0], 0, pillar[1]), Blocks.LAPIS_BLOCK)
					|| !isBlock(level, pos.setWithOffset(center, pillar[0], 1, pillar[1]), Blocks.LAPIS_BLOCK)
					|| !isBlock(level, pos.setWithOffset(center, pillar[0], 2, pillar[1]), Blocks.SEA_LANTERN)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isBlock(Level level, BlockPos pos, Block block) {
		return level.getBlockState(pos).is(block);
	}

	/**
	 * 设定传送目标。已经消耗的魔力和纸保留不变，这和原版相同。
	 * Sets the teleport target. The mana and the paper that the flower already used stay, which is the same as in the original.
	 */
	public void setTarget(BlockPos newTarget) {
		target = newTarget.immutable();
		sync();
	}

	@Nullable
	public BlockPos getTarget() {
		return target;
	}

	public int getConsumedMana() {
		return consumedMana;
	}

	public boolean hasPaper() {
		return hasPaper;
	}

	/**
	 * 打开通往当前目标的传送门所需的魔力。没有目标时是 0。
	 * The mana that is necessary to open the portal to the current target. It is 0 when there is no target.
	 */
	public int getTeleportCost() {
		if (target == null) {
			return 0;
		}
		ExtraBotanyConfig.ConfigAccess config = ExtraBotanyConfig.common();
		double distance = Math.sqrt(target.distSqr(getEffectivePos()));
		return (int) Math.min(Integer.MAX_VALUE, config.stardustLotusBaseCost() + distance * config.stardustLotusCostPerBlock());
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().stardustLotusMaxMana();
	}

	@Override
	public int getColor() {
		return COLOR;
	}

	@Override
	public RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void writeToPacketNBT(CompoundTag tag) {
		tag.putInt(TAG_CONSUMED_MANA, consumedMana);
		tag.putBoolean(TAG_HAS_PAPER, hasPaper);
		tag.putBoolean(TAG_HAS_TARGET, target != null);
		if (target != null) {
			tag.putInt(TAG_TARGET_X, target.getX());
			tag.putInt(TAG_TARGET_Y, target.getY());
			tag.putInt(TAG_TARGET_Z, target.getZ());
		}
	}

	@Override
	public void readFromPacketNBT(CompoundTag tag) {
		consumedMana = tag.getInt(TAG_CONSUMED_MANA);
		hasPaper = tag.getBoolean(TAG_HAS_PAPER);
		target = tag.getBoolean(TAG_HAS_TARGET)
				? new BlockPos(tag.getInt(TAG_TARGET_X), tag.getInt(TAG_TARGET_Y), tag.getInt(TAG_TARGET_Z))
				: null;
	}
}
