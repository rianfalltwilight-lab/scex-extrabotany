package io.github.lounode.extrabotany.api.gaia;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BlockPatternExtendBuilder {
	private static final Joiner COMMA_JOINED = Joiner.on(",");
	private final List<String[]> pattern = Lists.newArrayList();
	private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
	private int height;
	private int width;

	private BlockPatternExtendBuilder() {
		this.lookup.put(' ', (blockInWorld) -> true);
	}

	public BlockPatternExtendBuilder aisle(String... aisle) {
		if (!ArrayUtils.isEmpty((Object[]) aisle) && !StringUtils.isEmpty(aisle[0])) {
			if (this.pattern.isEmpty()) {
				this.height = aisle.length;
				this.width = aisle[0].length();
			}

			if (aisle.length != this.height) {
				throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + aisle.length + ")");
			} else {
				for (String s : aisle) {
					if (s.length() != this.width) {
						int var10002 = this.width;
						throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + var10002 + ", found one with " + s.length() + ")");
					}

					for (char c0 : s.toCharArray()) {
						if (!this.lookup.containsKey(c0)) {
							this.lookup.put(c0, null);
						}
					}
				}

				this.pattern.add(aisle);
				return this;
			}
		} else {
			throw new IllegalArgumentException("Empty pattern for aisle");
		}
	}

	public static BlockPatternExtendBuilder start() {
		return new BlockPatternExtendBuilder();
	}

	public BlockPatternExtendBuilder where(char symbol, Predicate<BlockInWorld> blockMatcher) {
		this.lookup.put(symbol, blockMatcher);
		return this;
	}

	public BlockPatternExtend build() {
		return new BlockPatternExtend(this.createPattern());
	}

	@SuppressWarnings("unchecked")
	public Predicate<BlockInWorld>[][][] createPattern() {
		this.ensureAllCharactersMatched();
		Predicate<BlockInWorld>[][][] predicate = (Predicate[][][]) Array.newInstance(Predicate.class, new int[] { this.pattern.size(), this.height, this.width });

		for (int i = 0; i < this.pattern.size(); ++i) {
			for (int j = 0; j < this.height; ++j) {
				for (int k = 0; k < this.width; ++k) {
					predicate[i][j][k] = this.lookup.get((this.pattern.get(i))[j].charAt(k));
				}
			}
		}

		return predicate;
	}

	private void ensureAllCharactersMatched() {
		List<Character> list = Lists.newArrayList();

		for (Map.Entry<Character, Predicate<BlockInWorld>> entry : this.lookup.entrySet()) {
			if (entry.getValue() == null) {
				list.add(entry.getKey());
			}
		}

		if (!list.isEmpty()) {
			throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(list) + " are missing");
		}
	}
}
