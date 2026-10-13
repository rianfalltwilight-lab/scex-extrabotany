package io.github.lounode.extrabotany.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import io.github.lounode.extrabotany.common.item.UniversalRunes;

/**
 * 让接受植物魔法符文的配方原料也接受元灵符文或者大罪符文。规则在 {@link UniversalRunes} 里。
 * Makes a recipe ingredient that accepts a rune of Botania also accept the Rune of Element or the Rune of Sin. The rule is in {@link UniversalRunes}.
 *
 * <p>这两个方法调用得非常频繁，所以这里的代码在平时只做一两次引用比较，并且不创建对象。
 * <p>These two methods are called very frequently, thus the code here usually only does one or two reference comparisons, and makes no objects.
 *
 * <p>线程：单人游戏里，服务端把配方对象直接交给客户端，所以两个线程使用同一个原料对象。游戏第一次生成编号列表时，先把空列表放进字段，再往里面填。另一个线程在这段时间里可能读到没有填完的列表。所以这里不改动游戏的字段，不复制游戏的列表，并且只在游戏的列表已经填满之后才保存检查结果。
 * <p>Threads: in a single-player game the server gives the recipe objects directly to the client, thus two threads use the same ingredient object. When the game makes the number list for the first time, it puts an empty list into the field and then fills it. A different thread can read the incomplete list in that time. Thus this code does not change the field of the game, does not copy the list of the game, and only stores the result of the check after the list of the game is full.
 */
@Mixin(Ingredient.class)
public abstract class UniversalRuneMixin {
	/**
	 * 上一次检查的结果。它是下面三种之一：{@code null}（还没有检查）；游戏的编号列表本身（这个原料不接受符文）；一个 {@link UniversalRunes.ExtendedIds}（这个原料接受符文）。
	 * The result of the last check. It is one of these three: {@code null} (no check was done); the number list of the game itself (this ingredient accepts no rune); a {@link UniversalRunes.ExtendedIds} (this ingredient accepts runes).
	 */
	@Unique
	@Nullable
	private Object extrabotany$runeState;

	@Shadow
	public abstract ItemStack[] getItems();

	/**
	 * 有序合成直接测试物品。
	 * Shaped crafting does a direct test of the item.
	 */
	@ModifyReturnValue(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
	private boolean extrabotany$testUniversalRunes(boolean matches, @Nullable ItemStack stack) {
		return matches || stack != null && UniversalRunes.isUniversalRune(stack) && UniversalRunes.replacesOneOf(stack, getItems());
	}

	/**
	 * 无序合成和植物魔法的符文祭坛比较物品的编号。
	 * Shapeless crafting and the Runic Altar of Botania compare item numbers.
	 */
	@ModifyReturnValue(method = "getStackingIds", at = @At("RETURN"))
	private IntList extrabotany$addUniversalRuneIds(IntList ids) {
		Object state = extrabotany$runeState;
		if (state == ids) {
			return ids;
		}
		if (state instanceof UniversalRunes.ExtendedIds extended && extended.source() == ids) {
			return extended.ids();
		}
		if (!UniversalRunes.isReady()) {
			return ids;
		}
		ItemStack[] accepted = getItems();
		// 游戏的列表填满时，它的长度等于物品的数量。长度不同说明另一个线程还在填它：这一次不处理，下一次调用再检查。
		// When the list of the game is full, its size is the number of items. A different size tells that a different thread still fills it: do nothing this time, and examine it again at the subsequent call.
		if (ids.size() != accepted.length) {
			return ids;
		}
		IntList withRunes = UniversalRunes.idsWithRunes(accepted);
		if (withRunes == null) {
			extrabotany$runeState = ids;
			return ids;
		}
		extrabotany$runeState = new UniversalRunes.ExtendedIds(ids, withRunes);
		return withRunes;
	}
}
