package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.xplat.BotaniaConfig;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.util.PlayerUtil;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * 伐木花。它消耗魔力，破坏范围里的一个原木。如果这朵花有放置者，它用放置者的假玩家来破坏方块，这样领地保护类的模组能识别是谁做的。红石信号让它停止工作。
 * Woodienia. It uses mana to break one log in the range. If the flower has an owner, it breaks the block with a fake player of the owner, thus protection mods can identify who did it. A redstone signal stops it.
 *
 * <p>来源：重制版（Lounode）。原版没有这朵花。
 * <p>Source: Reburn (Lounode). The original does not have this flower.
 */
public class WoodieniaBlockEntity extends ExtraFunctionalFlowerBlockEntity {
	private static final int IDLE_SCAN_BACKOFF = 20;
	private static final Predicate<BlockState> IS_LOG = state -> state.is(BlockTags.LOGS);

	private long nextIdleScan;

	public static final String TAG_COOLDOWN = "cooldown";
	public static final String TAG_OWNER_UUID = "ownerUUID";

	private int cooldown;
	@Nullable
	private UUID ownerUUID;

	public WoodieniaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.WOODIENIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		// 计时器每 tick 都变化，但是客户端不使用它。所以这里只标记区块需要保存，不发同步包。
		// The timers change on each tick, but the client does not use them. Thus this only marks the chunk for saving, and sends no packet.
		if (ticksExisted % 20 == 0) {
			markForPersisting();
		}
		if (isPowered()) {
			return;
		}
		if (getCooldown() > 0) {
			cooldown--;
			return;
		}
		if (getMana() < getManaPerUse()) {
			return;
		}

		if (getLevel().getGameTime() < nextIdleScan) {
			return;
		}
		BlockPos breakCoords = getValidBreakPos();

		if (breakCoords == null) {
			// 范围里没有原木。隔一会儿再扫描，不要每 tick 都扫描。
			// The range has no log. Scan again after some time, not on each tick.
			nextIdleScan = nextIdleScanTime(getLevel().getGameTime());
			return;
		}

		Player player = null;
		//TODO 需要更多测试来确定是否可行
		if (getOwnerUUID().isPresent()) {
			player = PlayerUtil.createFakePlayer((ServerLevel) getLevel(), getOwnerUUID().get());
		}

		breakBlock(getLevel(), breakCoords, player);
		setCooldown(getAfterEatCooldown());
		addMana(-getManaPerUse());
		syncThrottled();
	}

	/**
	 * 在范围里随机选一个原木。范围里没有原木时返回 {@code null}。
	 * Selects one log in the range at random. Returns {@code null} when the range has no log.
	 *
	 * <p>默认的范围是 17×17×17，有将近五千个方块，所以这次扫描是这朵花最贵的操作：
	 * <p>The default range is 17×17×17, which is almost five thousand blocks, thus this scan is the most expensive operation of the flower:
	 * <ul>
	 * <li>方块按区块段（16×16×16）读取。先看这一段的调色板：调色板里没有原木时，整段跳过。大多数时候范围里没有原木，这时一次扫描只检查几个调色板。
	 * The blocks are read by chunk section (16×16×16). The palette of the section is examined first: when the palette has no log, the full section is skipped. Most of the time the range has no log, and then one scan only examines some palettes.</li>
	 * <li>只读取已经加载的区块。向世界要一个未加载区块里的方块，会让服务器同步加载那个区块。
	 * Only loaded chunks are read. A request to the level for a block in a chunk that is not loaded makes the server load that chunk synchronously.</li>
	 * </ul>
	 *
	 * <p>调色板可能还留着已经不存在的方块，所以"调色板里有原木"不代表这一段里真的有原木。这时会逐个方块读取这一段，结果仍然正确。
	 * <p>A palette can still have blocks that are no longer there, thus "the palette has a log" does not mean that the section really has a log. In that case the section is read block by block, and the result is still correct.
	 *
	 * <p>每个原木被选中的概率相同。以前的代码给每个原木一个随机权重，留下权重最高的一部分，再从中随机选一个，结果也是每个原木概率相同，但是每次扫描都要建一张表。
	 * <p>Each log has the same chance. The old code gave each log a random weight, kept the part with the highest weights and then selected one of them at random; the result was also the same chance for each log, but each scan had to make a map.
	 */
	@Nullable
	public BlockPos getValidBreakPos() {
		Level level = getLevel();
		BlockPos center = getEffectivePos();
		Vec3i range = getRange();
		int minX = center.getX() - range.getX();
		int maxX = center.getX() + range.getX();
		int minZ = center.getZ() - range.getZ();
		int maxZ = center.getZ() + range.getZ();
		int minY = Math.max(center.getY(), level.getMinBuildHeight());
		int maxY = Math.min(center.getY() + range.getY(), level.getMaxBuildHeight() - 1);
		if (minY > maxY) {
			return null;
		}

		RandomSource random = level.getRandom();
		BlockPos chosen = null;
		int logs = 0;
		for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
			for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null) {
					continue;
				}
				int fromX = Math.max(minX, chunkX << 4);
				int toX = Math.min(maxX, (chunkX << 4) + 15);
				int fromZ = Math.max(minZ, chunkZ << 4);
				int toZ = Math.min(maxZ, (chunkZ << 4) + 15);
				for (int index = chunk.getSectionIndex(minY); index <= chunk.getSectionIndex(maxY); index++) {
					LevelChunkSection section = chunk.getSection(index);
					if (section.hasOnlyAir() || !section.maybeHas(IS_LOG)) {
						continue;
					}
					int sectionBottom = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(index));
					int fromY = Math.max(minY, sectionBottom);
					int toY = Math.min(maxY, sectionBottom + 15);
					for (int y = fromY; y <= toY; y++) {
						for (int z = fromZ; z <= toZ; z++) {
							for (int x = fromX; x <= toX; x++) {
								BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
								// 先判断空气：这个判断比标签判断快，而且范围里大部分方块是空气。
								// Air is examined first: this check is faster than the tag check, and most blocks in the range are air.
								if (state.isAir() || !state.is(BlockTags.LOGS)) {
									continue;
								}
								// 蓄水池抽样：第 n 个原木以 1/n 的概率替换已选的那个。
								// Reservoir sampling: log number n replaces the selected one with a chance of 1/n.
								logs++;
								if (random.nextInt(logs) == 0) {
									chosen = new BlockPos(x, y, z);
								}
							}
						}
					}
				}
			}
		}
		return chosen;
	}

	/**
	 * 范围里没有原木时，下一次扫描的时间。
	 * The time of the next scan when the range has no log.
	 *
	 * <p>每朵花按自己的位置有一个固定的相位，所以相邻的花在不同的 tick 扫描。区块加载之后所有的花会同时开始工作；没有相位时，它们以后每次都在同一个 tick 扫描，这个 tick 会特别慢。
	 * <p>Each flower has a fixed phase from its position, thus adjacent flowers scan on different ticks. All flowers start to work at the same time after a chunk is loaded; with no phase, they would then always scan on the same tick, and that tick would be very slow.
	 *
	 * <p>等待时间在 1 到 {@link #IDLE_SCAN_BACKOFF} tick 之间，所以花发现新原木的最长时间没有变。
	 * <p>The wait is between 1 tick and {@link #IDLE_SCAN_BACKOFF} ticks, thus the maximum time for the flower to find a new log did not change.
	 */
	private long nextIdleScanTime(long now) {
		int phase = Math.floorMod(getBlockPos().hashCode(), IDLE_SCAN_BACKOFF);
		return now + IDLE_SCAN_BACKOFF - Math.floorMod(now + phase, (long) IDLE_SCAN_BACKOFF);
	}

	protected static void breakBlock(Level level, BlockPos pos, @Nullable Player player) {
		BlockState state = level.getBlockState(pos);

		if (player != null) {
			ToolCommons.removeBlockWithDrops(player, ItemStack.EMPTY, level, pos, (b) -> true);
		} else {
			level.destroyBlock(pos, true);
		}

		if (BotaniaConfig.common().blockBreakParticles()) {
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		}
		level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer instanceof Player player) {
			setOwnerUUID(player.getGameProfile().getId());
		}
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int ticks) {
		this.cooldown = ticks;
	}

	public int getAfterEatCooldown() {
		return ExtraBotanyConfig.common().woodieniaCooldown();
	}

	public int getManaPerUse() {
		return ExtraBotanyConfig.common().woodieniaWorkManaCost();
	}

	public Vec3i getRange() {
		int[] range = ExtraBotanyConfig.common().woodieniaRange();
		return new Vec3i(range[0], range[1], range[2]);
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().woodieniaMaxMana();
	}

	@Override
	public int getColor() {
		return 0x664422;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Rectangle(getEffectivePos(), new AABB(
				Vec3.atLowerCornerOf(getEffectivePos().offset(-getRange().getX(), 0, -getRange().getZ())),
				Vec3.atLowerCornerOf(getEffectivePos().offset(getRange().getX() + 1, 0, getRange().getZ() + 1))));
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_COOLDOWN, getCooldown());
		getOwnerUUID().ifPresent(uuid -> cmp.putUUID(TAG_OWNER_UUID, uuid));
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		cooldown = cmp.getInt(TAG_COOLDOWN);
		if (cmp.contains(TAG_OWNER_UUID)) {
			setOwnerUUID(cmp.getUUID(TAG_OWNER_UUID));
		}
	}

	public Optional<UUID> getOwnerUUID() {
		return ownerUUID == null ? Optional.empty() : Optional.of(ownerUUID);
	}

	public void setOwnerUUID(UUID uuid) {
		this.ownerUUID = uuid;
	}
}
