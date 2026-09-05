package io.github.lounode.extrabotany.api.gaia;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Vanilla extension. Matches if failed, return a {@link List<>}
 **/
public class BlockPatternExtend {

	private static final Direction[] FLAT_DIRECTIONS = new Direction[] {
			Direction.NORTH,
			Direction.SOUTH,
			Direction.WEST,
			Direction.EAST,
	};

	private final Predicate<BlockInWorld>[][][] pattern;
	private final int depth;
	private final int height;
	private final int width;

	public BlockPatternExtend(Predicate<BlockInWorld>[][][] pattern) {
		this.pattern = pattern;
		this.depth = pattern.length;
		if (this.depth > 0) {
			this.height = pattern[0].length;
			if (this.height > 0) {
				this.width = pattern[0][0].length;
			} else {
				this.width = 0;
			}
		} else {
			this.height = 0;
			this.width = 0;
		}

	}

	public int getDepth() {
		return this.depth;
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	@VisibleForTesting
	public Predicate<BlockInWorld>[][][] getPattern() {
		return this.pattern;
	}

	@Nullable
	@VisibleForTesting
	public BlockPatternMatchSuccess matches(LevelReader level, BlockPos pos, Direction finger, Direction thumb) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		return this.matches(pos, finger, thumb, loadingcache);
	}

	@Nullable
	private BlockPatternMatchSuccess matches(BlockPos pos, Direction finger, Direction thumb, LoadingCache<BlockPos, BlockInWorld> cache) {
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				for (int k = 0; k < this.depth; ++k) {
					if (!this.pattern[k][j][i].test(cache.getUnchecked(translateAndRotate(pos, finger, thumb, i, j, k)))) {
						return null;
					}
				}
			}
		}

		return new BlockPatternMatchSuccess(pos, finger, thumb, cache, this.width, this.height, this.depth);
	}

	@VisibleForTesting
	public MatchResult matchesWithFailResult(LevelReader level, BlockPos pos, Direction finger, Direction thumb) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failCache = new HashMap<>();
		return this.matchesWithFailResult(pos, finger, thumb, loadingcache, failCache);
	}

	private MatchResult matchesWithFailResult(BlockPos pos, Direction finger, Direction thumb, LoadingCache<BlockPos, BlockInWorld> cache, Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failCache) {
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				for (int k = 0; k < this.depth; ++k) {
					BlockPos checkPos = translateAndRotate(pos, finger, thumb, i, j, k);
					Predicate<BlockInWorld> predicate = pattern[k][j][i];
					BlockInWorld block = cache.getUnchecked(checkPos);

					if (!predicate.test(block)) {
						failCache.put(checkPos, Pair.of(predicate, block));
					}
				}
			}
		}

		if (!failCache.isEmpty()) {
			return new BlockPatternMatchFail(failCache, finger, thumb);
		}

		return new BlockPatternMatchSuccess(pos, finger, thumb, cache, this.width, this.height, this.depth);
	}

	@Nullable
	public BlockPatternMatchSuccess find(LevelReader level, BlockPos pos) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		int i = Math.max(Math.max(this.width, this.height), this.depth);

		for (BlockPos blockpos : BlockPos.betweenClosed(pos, pos.offset(i - 1, i - 1, i - 1))) {
			for (Direction direction : Direction.values()) {
				for (Direction direction1 : Direction.values()) {
					if (direction1 != direction && direction1 != direction.getOpposite()) {
						BlockPatternMatchSuccess matches = this.matches(blockpos, direction, direction1, loadingcache);
						if (matches != null) {
							return matches;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * <h1>Important: This method is NOT deprecated</h1>
	 * <h1>But it will cause performance issue</h1>
	 * <h1>So don't call it unless you know what are you doing!!!</h1>
	 */
	@Deprecated
	public MatchResult findWithFailResult(LevelReader level, BlockPos pos) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		int i = Math.max(Math.max(this.width, this.height), this.depth);
		List<BlockPatternMatchFail> fails = new ArrayList<>();

		for (BlockPos blockpos : BlockPos.betweenClosed(pos, pos.offset(i - 1, i - 1, i - 1))) {
			for (Direction direction : Direction.values()) {
				for (Direction direction1 : Direction.values()) {
					if (direction1 != direction && direction1 != direction.getOpposite()) {
						Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failCache = new HashMap<>();

						MatchResult matches = this.matchesWithFailResult(blockpos, direction, direction1, loadingcache, failCache);

						if (matches instanceof BlockPatternMatchSuccess success) {
							return success;
						}

						if (matches instanceof BlockPatternMatchFail fail) {
							fails.add(fail);
						}
					}
				}
			}
		}

		return fails.stream()
				.min(Comparator.comparingInt(BlockPatternMatchFail::size))
				.orElse(null);
	}

	/**
	 * <h1>Important: This method is NOT deprecated</h1>
	 * <h1>But it will cause performance issue</h1>
	 * <h1>So don't call it unless you know what are you doing!!!</h1>
	 */
	@Deprecated
	public MatchResult findFlatWithFailResult(LevelReader level, BlockPos pos) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		int i = Math.max(Math.max(this.width, this.height), this.depth);
		List<BlockPatternMatchFail> fails = new ArrayList<>();

		for (BlockPos blockpos : BlockPos.betweenClosed(pos, pos.offset(i - 1, i - 1, i - 1))) {
			for (Direction direction : FLAT_DIRECTIONS) {
				Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failCache = new HashMap<>();
				MatchResult matches = this.matchesWithFailResult(blockpos, Direction.DOWN, direction, loadingcache, failCache);

				if (matches instanceof BlockPatternMatchSuccess success) {
					return success;
				}

				if (matches instanceof BlockPatternMatchFail fail) {
					fails.add(fail);
				}
			}
		}

		return fails.stream()
				.min(Comparator.comparingInt(BlockPatternMatchFail::size))
				.orElse(null);
	}

	@Nullable
	public BlockPatternMatchSuccess findFlat(LevelReader level, BlockPos pos) {
		LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(level, false);
		int i = Math.max(Math.max(this.width, this.height), this.depth);

		for (BlockPos blockpos : BlockPos.betweenClosed(pos, pos.offset(i - 1, i - 1, i - 1))) {
			for (Direction direction : FLAT_DIRECTIONS) {
				BlockPatternMatchSuccess match = this.matches(blockpos, Direction.DOWN, direction, loadingcache);
				if (match != null) {
					return match;
				}
			}
		}

		return null;
	}

	public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader level, boolean forceLoad) {
		return CacheBuilder.newBuilder().build(new BlockPatternExtend.BlockCacheLoader(level, forceLoad));
	}

	protected static BlockPos translateAndRotate(BlockPos pos, Direction finger, Direction thumb, int palmOffset, int thumbOffset, int fingerOffset) {
		if (finger != thumb && finger != thumb.getOpposite()) {
			Vec3i vec3i = new Vec3i(finger.getStepX(), finger.getStepY(), finger.getStepZ());
			Vec3i vec3i1 = new Vec3i(thumb.getStepX(), thumb.getStepY(), thumb.getStepZ());
			Vec3i vec3i2 = vec3i.cross(vec3i1);
			return pos.offset(vec3i1.getX() * -thumbOffset + vec3i2.getX() * palmOffset + vec3i.getX() * fingerOffset, vec3i1.getY() * -thumbOffset + vec3i2.getY() * palmOffset + vec3i.getY() * fingerOffset, vec3i1.getZ() * -thumbOffset + vec3i2.getZ() * palmOffset + vec3i.getZ() * fingerOffset);
		} else {
			throw new IllegalArgumentException("Invalid forwards & up combination");
		}
	}

	static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
		private final LevelReader level;
		private final boolean loadChunks;

		public BlockCacheLoader(LevelReader level, boolean loadChunks) {
			this.level = level;
			this.loadChunks = loadChunks;
		}

		@Override
		public BlockInWorld load(BlockPos pos) {
			return new BlockInWorld(this.level, pos, this.loadChunks);
		}
	}

	public static class MatchResult {
		private final Direction forwards;
		private final Direction up;

		public MatchResult(Direction forwards, Direction up) {
			this.forwards = forwards;
			this.up = up;
		}

		public Direction getForwards() {
			return this.forwards;
		}

		public Direction getUp() {
			return this.up;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("up", this.getUp())
					.add("forwards", this.getForwards())
					.toString();
		}
	}

	public static class BlockPatternMatchFail extends MatchResult {
		private final Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failedBlocks;

		public BlockPatternMatchFail(Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> failedBlocks, Direction finger, Direction thumb) {
			super(finger, thumb);
			this.failedBlocks = failedBlocks;
		}

		public Map<BlockPos, Pair<Predicate<BlockInWorld>, BlockInWorld>> getFailedBlocks() {
			return failedBlocks;
		}

		public int size() {
			return this.failedBlocks.size();
		}
	}

	public static class BlockPatternMatchSuccess extends MatchResult {
		private final BlockPos frontTopLeft;

		private final LoadingCache<BlockPos, BlockInWorld> cache;
		private final int width;
		private final int height;
		private final int depth;

		public BlockPatternMatchSuccess(BlockPos frontTopLeft, Direction forwards, Direction up, LoadingCache<BlockPos, BlockInWorld> cache, int width, int height, int depth) {
			super(forwards, up);
			this.frontTopLeft = frontTopLeft;
			this.cache = cache;
			this.width = width;
			this.height = height;
			this.depth = depth;
		}

		public BlockPos getFrontTopLeft() {
			return this.frontTopLeft;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}

		public int getDepth() {
			return this.depth;
		}

		public BlockInWorld getBlock(int palmOffset, int thumbOffset, int fingerOffset) {
			return this.cache.getUnchecked(BlockPatternExtend.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), palmOffset, thumbOffset, fingerOffset));
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("up", this.getUp()).add("forwards", this.getForwards()).add("frontTopLeft", this.frontTopLeft).toString();
		}
	}
}
