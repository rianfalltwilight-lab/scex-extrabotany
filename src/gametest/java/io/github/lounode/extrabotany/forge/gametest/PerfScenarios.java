package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 性能场景。它们不是测试：每个场景在一块空地上放 64 个同一种方块，然后测量服务端每 tick 的耗时。
 * Performance scenarios. They are not tests: each scenario puts 64 blocks of one type on an empty area, and then measures the time of each server tick.
 *
 * <p>平时的测试运行不包含它们。运行方法：
 * <p>The usual test run does not include them. To run them:
 *
 * <pre>gradlew runGameTestServer -Pgametest_namespaces=extrabotany_perf</pre>
 *
 * <p>只想运行其中几个场景时，把环境变量 {@code EB_PERF_ONLY} 设成方块的注册名，用逗号分开，例如 {@code floating_woodienia,manabuffer}。
 * <p>To run only some of the scenarios, set the environment variable {@code EB_PERF_ONLY} to the registry names of the blocks, with commas between them, for example {@code floating_woodienia,manabuffer}.
 *
 * <p>结果写在日志里，以 {@code EB_PERF} 开头，每种方块一行。测试服务器的两个 tick 之间不休眠，所以相邻两次回调的间隔就是一个 tick 的耗时。
 * <p>The results are in the log, in lines that start with {@code EB_PERF}, one line for each block type. The test server does not sleep between two ticks, thus the time between two callbacks is the time of one tick.
 *
 * <p>花用的是浮空花（不需要泥土），旁边有一个创造魔力池。功能花会绑定魔力池并且寻找可以做的事，这是花在基地里最常见的状态。机器全部空载。
 * <p>The flowers are floating flowers (which need no soil), with a Creative Mana Pool next to them. The functional flowers bind to the pool and look for work, which is the most usual state of a flower in a base. The machines all have no load.
 *
 * <p>要看是哪些方法花了时间，可以在运行时打开 Java Flight Recorder（环境变量 {@code JAVA_TOOL_OPTIONS=-XX:StartFlightRecording=...}）。
 * <p>To see which methods use the time, start the Java Flight Recorder for the run (the environment variable {@code JAVA_TOOL_OPTIONS=-XX:StartFlightRecording=...}).
 */
@GameTestHolder(PerfScenarios.NAMESPACE)
@PrefixGameTestTemplate(false)
public final class PerfScenarios {
	static final String NAMESPACE = "extrabotany_perf";
	private static final String ONLY_VARIABLE = "EB_PERF_ONLY";

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final int PATCH = 8;

	private static final int IDLE_TICKS = 20;
	private static final int WARMUP_TICKS = 200;
	private static final int MEASURE_TICKS = 600;
	private static final int SETTLE_TICKS = 100;
	private static final int BASELINE_TICKS = 300;
	private static final int TOTAL_TICKS = IDLE_TICKS + WARMUP_TICKS + MEASURE_TICKS + SETTLE_TICKS + BASELINE_TICKS;

	private PerfScenarios() {}

	/**
	 * 每种花和每种机器一个场景。场景一个接一个运行，所以它们互不影响。
	 * One scenario for each flower type and each machine type. The scenarios run one after the other, thus they have no effect on each other.
	 */
	@GameTestGenerator
	public static Collection<TestFunction> scenarios() {
		// 生成器方法不受命名空间过滤，所以在这里自己判断。
		// The namespace filter does not apply to generator methods, thus the check is made here.
		String enabled = System.getProperty("neoforge.enabledGameTestNamespaces", "");
		if (!List.of(enabled.split(",")).contains(NAMESPACE)) {
			return List.of();
		}

		List<Block> flowers = BuiltInRegistries.BLOCK.stream()
				.filter(block -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
				.filter(block -> block instanceof FloatingSpecialFlowerBlock)
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toList();
		List<Block> machines = List.of(ExtraBotanyBlocks.manaBuffer, ExtraBotanyBlocks.quantumManaBuffer,
				ExtraBotanyBlocks.manaGenerator, ExtraBotanyBlocks.manaLiquefaction, ExtraBotanyBlocks.livingrockBarrel,
				ExtraBotanyBlocks.cocoonOfDesire, ExtraBotanyBlocks.livingrockPedestal, ExtraBotanyBlocks.manaCharger,
				ExtraBotanyBlocks.powerFrame);

		List<TestFunction> scenarios = new ArrayList<>();
		int index = 0;
		for (Block flower : flowers) {
			scenarios.add(scenario(index++, flower, true, null));
		}
		for (Block machine : machines) {
			// 魔力充能基座只能放在魔力池的上面。没有魔力池时它会掉落成物品。
			// A Mana Charger can only be on top of a mana pool. With no mana pool it drops as an item.
			Block support = machine == ExtraBotanyBlocks.manaCharger ? BotaniaBlocks.MANA_POOL : null;
			scenarios.add(scenario(index++, machine, false, support));
		}
		String only = System.getenv(ONLY_VARIABLE);
		if (only != null && !only.isBlank()) {
			List<String> names = List.of(only.split(","));
			scenarios.removeIf(scenario -> !names.contains(scenario.testName().substring("perf.".length())));
		}
		return scenarios;
	}

	private static TestFunction scenario(int index, Block block, boolean withPool, @Nullable Block support) {
		String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
		// 每个场景有自己的批次。批次一个接一个运行，但是顺序不固定。
		// Each scenario has its own batch. The batches run one after the other, but their sequence is not fixed.
		String batch = String.format(Locale.ROOT, "perf_%02d_%s", index, name);
		return new TestFunction(batch, "perf." + name, NAMESPACE + ":arena", TOTAL_TICKS + 100, 0L, true,
				helper -> run(helper, name, block, withPool, support));
	}

	/**
	 * 铺一块 8×8 的田。{@code support} 不是 {@code null} 时，每个方块的下面先放一个 {@code support}。
	 * Puts down one 8×8 patch. When {@code support} is not {@code null}, one {@code support} is put below each block first.
	 */
	private static void placePatch(GameTestHelper helper, Block block, boolean withPool, @Nullable Block support) {
		if (withPool) {
			helper.setBlock(new BlockPos(2, 2, 2), BotaniaBlocks.CREATIVE_MANA_POOL);
		}
		int y = support == null ? 2 : 3;
		for (int x = 1; x <= PATCH; x++) {
			for (int z = 1; z <= PATCH; z++) {
				if (support != null) {
					helper.setBlock(new BlockPos(2 + x, 2, 2 + z), support);
				}
				helper.setBlock(new BlockPos(2 + x, y, 2 + z), block);
			}
		}
	}

	/**
	 * 数一数田里还有多少个这种方块的方块实体。数量不是 64 时，说明这种方块在这里放不住，这个场景的数字没有意义。
	 * Counts the block entities of this block type that are still in the patch. When the number is not 64, the block type cannot stay here, and the numbers of this scenario have no meaning.
	 */
	private static int countPatch(GameTestHelper helper, Block block) {
		int count = 0;
		for (int x = 1; x <= PATCH; x++) {
			for (int z = 1; z <= PATCH; z++) {
				for (int y = 2; y <= 3; y++) {
					BlockPos pos = helper.absolutePos(new BlockPos(2 + x, y, 2 + z));
					if (helper.getLevel().getBlockState(pos).is(block) && helper.getLevel().getBlockEntity(pos) != null) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private static void clearPatch(GameTestHelper helper) {
		// 先拿走上面一层，这样上面的方块不会因为失去支撑而掉落成物品。
		// The top layer is removed first, so that the blocks on top do not lose their support and drop as items.
		for (int y = 3; y >= 2; y--) {
			for (int x = 0; x <= PATCH; x++) {
				for (int z = 0; z <= PATCH; z++) {
					helper.setBlock(new BlockPos(2 + x, y, 2 + z), Blocks.AIR);
				}
			}
		}
	}

	/**
	 * 放下方块，预热之后测量 tick 耗时；然后拿走方块，再测量空场地的 tick 耗时作为基线。两次的差就是这些方块的开销。
	 * Puts the blocks down and measures the tick time after a warm-up; then removes the blocks and measures the tick time of the empty area as the baseline. The difference between the two is the cost of these blocks.
	 *
	 * <p>基线放在后面测量：这时 JVM 已经把服务器的代码编译好了。放在前面测量时，基线会偏慢，算出来的开销会是负数。
	 * <p>The baseline is measured last: at that time the JVM has compiled the server code. When it is measured first, the baseline is too slow, and the calculated cost becomes negative.
	 */
	private static void run(GameTestHelper helper, String name, Block block, boolean withPool, @Nullable Block support) {
		long[] lastTick = { 0 };
		int[] tick = { 0 };
		int[] blockEntities = { 0 };
		List<Long> baseline = new ArrayList<>();
		List<Long> loaded = new ArrayList<>();
		int placeAt = IDLE_TICKS;
		int measureFrom = placeAt + WARMUP_TICKS;
		int clearAt = measureFrom + MEASURE_TICKS;
		int baselineFrom = clearAt + SETTLE_TICKS;
		int end = baselineFrom + BASELINE_TICKS;

		helper.onEachTick(() -> {
			long now = System.nanoTime();
			long duration = now - lastTick[0];
			lastTick[0] = now;
			int current = tick[0]++;
			if (current > measureFrom && current <= clearAt) {
				loaded.add(duration);
			} else if (current > baselineFrom && current <= end) {
				baseline.add(duration);
			}
			if (current == placeAt) {
				placePatch(helper, block, withPool, support);
			}
			if (current == clearAt) {
				blockEntities[0] = countPatch(helper, block);
				clearPatch(helper);
			}
			if (current == end) {
				double costMicros = (mean(loaded) - mean(baseline)) / 1000.0 / Math.max(1, blockEntities[0]);
				LOGGER.info("EB_PERF {} | {} block entities | cost {} us for each one on each tick | slowest 5% of ticks: {} ms more than empty | empty: {} | loaded: {}",
						String.format(Locale.ROOT, "%-28s", name), blockEntities[0], String.format(Locale.ROOT, "%6.2f", costMicros),
						String.format(Locale.ROOT, "%6.3f", (percentile(loaded, 95) - percentile(baseline, 95)) / 1_000_000.0),
						describe(baseline), describe(loaded));
				helper.succeed();
			}
		});
	}

	/**
	 * 去掉最慢的 1% 之后的平均值。垃圾回收偶尔会让一个 tick 特别慢；每隔 20 tick 才做一次的工作必须算进去，所以不能用中位数。
	 * The mean without the slowest 1%. Garbage collection sometimes makes one tick very slow; work that is done on each 20th tick must be included, thus the median cannot be used.
	 */
	private static double mean(List<Long> values) {
		List<Long> sorted = new ArrayList<>(values);
		sorted.sort(null);
		int count = Math.max(1, sorted.size() - Math.max(1, sorted.size() / 100));
		return sorted.stream().limit(count).mapToLong(Long::longValue).average().orElse(0);
	}

	private static long percentile(List<Long> values, int percent) {
		List<Long> sorted = new ArrayList<>(values);
		sorted.sort(null);
		return sorted.isEmpty() ? 0 : sorted.get(Math.min(sorted.size() - 1, sorted.size() * percent / 100));
	}

	private static String describe(List<Long> values) {
		return String.format(Locale.ROOT, "mean %.3f ms, median %.3f ms, 95%% %.3f ms", mean(values) / 1_000_000.0,
				percentile(values, 50) / 1_000_000.0, percentile(values, 95) / 1_000_000.0);
	}
}
