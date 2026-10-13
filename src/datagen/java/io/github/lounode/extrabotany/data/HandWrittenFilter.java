package io.github.lounode.extrabotany.data;

import com.mojang.logging.LogUtils;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

/**
 * 包住一个数据生成器，不让它写出"已经有手写版本"的文件。
 * Wraps a data provider, and stops it from writing a file that already has a hand-written version.
 *
 * <p>资源有两个来源：{@code src/main/resources} 里的文件是手写的，{@code src/generated/resources} 里的文件是生成的。同一个路径只能有一个来源，否则打包时两个文件会冲突，而且人看不出哪个生效。
 * <p>The resources have two sources: the files in {@code src/main/resources} are written by hand, and the files in {@code src/generated/resources} are generated. One path must have one source only. If not, the two files collide in the JAR, and a person cannot see which one is used.
 *
 * <p>规则：手写的文件优先。生成器对很多内容使用通用的写法（例如"方块掉落它自己""物品模型是一张平面贴图"），手写的文件用来表达通用写法不能表达的内容。
 * <p>The rule: the hand-written file wins. The generators use a general form for most content (for example "a block drops itself", "an item model is one flat texture"), and a hand-written file is for content that the general form cannot express.
 *
 * <p>手写资源目录的位置来自系统属性 {@value #PROPERTY}，由 {@code build.gradle} 的数据生成运行配置设置。没有这个属性时不做任何过滤。
 * <p>The location of the hand-written resources comes from the system property {@value #PROPERTY}, which the data run of {@code build.gradle} sets. Without the property there is no filter.
 */
public final class HandWrittenFilter implements DataProvider {
	public static final String PROPERTY = "extrabotany.datagen.handwritten";

	private static final Logger LOGGER = LogUtils.getLogger();

	private final DataProvider delegate;
	private final Path outputRoot;
	@Nullable
	private final Path handWrittenRoot;

	public HandWrittenFilter(DataProvider delegate, Path outputRoot) {
		this.delegate = delegate;
		this.outputRoot = outputRoot.toAbsolutePath().normalize();
		String property = System.getProperty(PROPERTY);
		this.handWrittenRoot = property == null ? null : Path.of(property).toAbsolutePath().normalize();
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		if (handWrittenRoot == null) {
			return delegate.run(output);
		}
		Set<String> skipped = java.util.Collections.synchronizedSet(new TreeSet<>());
		CachedOutput filtered = (path, data, hash) -> {
			Path relative = outputRoot.relativize(path.toAbsolutePath().normalize());
			if (Files.exists(handWrittenRoot.resolve(relative))) {
				skipped.add(relative.toString().replace('\\', '/'));
			} else {
				output.writeIfNeeded(path, data, hash);
			}
		};
		return delegate.run(filtered).thenRun(() -> {
			if (!skipped.isEmpty()) {
				LOGGER.info("{}: did not write {} file(s) that have a hand-written version: {}",
						delegate.getName(), skipped.size(), skipped);
			}
		});
	}

	@Override
	public String getName() {
		return delegate.getName();
	}
}
