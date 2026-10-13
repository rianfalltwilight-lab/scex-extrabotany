package io.github.lounode.extrabotany.api.gaia;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * 判断方块状态是否带有某个方块标签的谓词。它用在多方块图案里。
 * A predicate that tells if a block state has a given block tag. It is used in multiblock patterns.
 */
public class BlockTagPredicate implements Predicate<BlockState> {
	public static final Predicate<BlockState> ANY = (tag) -> true;
	private final TagKey<Block> tag;

	private BlockTagPredicate(final TagKey<Block> tag) {
		this.tag = tag;
	}

	public static BlockTagPredicate forTag(TagKey<Block> tag) {
		return new BlockTagPredicate(tag);
	}

	@Override
	public boolean test(@Nullable BlockState state) {
		if (state == null) {
			return false;
		}
		return state.is(tag);
	}
}
