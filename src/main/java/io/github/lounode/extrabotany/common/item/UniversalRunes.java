package io.github.lounode.extrabotany.common.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.item.BotaniaItems;

import java.util.Set;

/**
 * 元灵符文和大罪符文的规则：它们可以代替植物魔法的符文。
 * The rule of the Rune of Element and the Rune of Sin: they can replace the runes of Botania.
 *
 * <ul>
 * <li>元灵符文代替四种元素符文（水、火、地、风）和四种季节符文（春、夏、秋、冬）。
 * The Rune of Element replaces the four element runes (water, fire, earth, air) and the four season runes (spring, summer, autumn, winter).</li>
 * <li>大罪符文代替魔力符文和七种原罪符文。
 * The Rune of Sin replaces the Rune of Mana and the seven sin runes.</li>
 * </ul>
 *
 * <p>做法：游戏用两种办法判断一个物品是否符合配方原料，一种是直接测试物品（有序合成），另一种是比较物品的编号（无序合成、符文祭坛）。{@code UniversalRuneMixin} 在这两处都加上通用符文。原料的"可接受物品"列表没有改动，所以发给客户端的配方数据和植物魔法对配方的检查都不受影响；JEI 和 EMI 也因此不会把通用符文显示成可选的原料。
 * <p>How it works: the game has two methods to find if an item agrees with a recipe ingredient: a direct test of the item (shaped crafting), and a comparison of item numbers (shapeless crafting, the Runic Altar). {@code UniversalRuneMixin} adds the universal runes in these two places. The list of accepted items of the ingredient is not changed, thus the recipe data that goes to the client and the recipe checks of Botania are not affected; for the same reason JEI and EMI do not show the universal runes as possible ingredients.
 *
 * <p>来源：ExtraMeteorP 1.16。原版把两种符文放进植物魔法的符文标签（{@code botania:runes/air} 等）里。植物魔法 1.21.1 的配方直接写物品，不用这些标签，所以那个办法现在没有效果，重制版里这两种符文也就没有代替的作用了。
 * <p>Source: ExtraMeteorP 1.16. The original puts the two runes into the rune tags of Botania ({@code botania:runes/air} and others). The recipes of Botania 1.21.1 name the items directly and do not use these tags, thus that method has no effect now, and in Reburn the two runes did not replace anything.
 */
public final class UniversalRunes {
	@Nullable
	private static Item elementRune;
	@Nullable
	private static Item sinRune;
	private static Set<Item> elementRunes = Set.of();
	private static Set<Item> sinRunes = Set.of();

	private UniversalRunes() {}

	/**
	 * 在物品注册完成之后调用一次。在这之前，这个类什么也不做。
	 * Call this one time after the items are registered. Before that, this class does nothing.
	 */
	public static void init() {
		elementRunes = Set.of(BotaniaItems.RUNE_OF_WATER, BotaniaItems.RUNE_OF_FIRE, BotaniaItems.RUNE_OF_EARTH, BotaniaItems.RUNE_OF_AIR,
				BotaniaItems.RUNE_OF_SPRING, BotaniaItems.RUNE_OF_SUMMER, BotaniaItems.RUNE_OF_AUTUMN, BotaniaItems.RUNE_OF_WINTER);
		sinRunes = Set.of(BotaniaItems.RUNE_OF_MANA, BotaniaItems.RUNE_OF_LUST, BotaniaItems.RUNE_OF_GLUTTONY, BotaniaItems.RUNE_OF_GREED,
				BotaniaItems.RUNE_OF_SLOTH, BotaniaItems.RUNE_OF_WRATH, BotaniaItems.RUNE_OF_ENVY, BotaniaItems.RUNE_OF_PRIDE);
		sinRune = ExtraBotanyItems.sinRune;
		elementRune = ExtraBotanyItems.elementRune;
	}

	public static boolean isReady() {
		return elementRune != null;
	}

	/**
	 * 这个物品是不是元灵符文或者大罪符文。
	 * Tells if this item is the Rune of Element or the Rune of Sin.
	 */
	public static boolean isUniversalRune(ItemStack stack) {
		Item item = stack.getItem();
		return elementRune != null && (item == elementRune || item == sinRune);
	}

	/**
	 * 这个通用符文能不能代替"可接受物品"列表里的某一个符文。
	 * Tells if this universal rune can replace one of the runes in the list of accepted items.
	 *
	 * @param rune 元灵符文或者大罪符文 / the Rune of Element or the Rune of Sin
	 */
	public static boolean replacesOneOf(ItemStack rune, ItemStack[] accepted) {
		Set<Item> replaced = rune.getItem() == elementRune ? elementRunes : sinRunes;
		for (ItemStack candidate : accepted) {
			if (replaced.contains(candidate.getItem())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 为接受符文的原料生成一个新的物品编号列表：原料自己的物品，加上能代替它们的通用符文。列表从小到大排列，以后不再改动。原料不接受符文时返回 {@code null}。
	 * Makes a new list of item numbers for an ingredient that accepts runes: the items of the ingredient, plus the universal runes that can replace them. The list is in increasing order and does not change afterward. Returns {@code null} when the ingredient accepts no rune.
	 *
	 * <p>列表是从"可接受物品"列表重新生成的，不是从游戏自己的编号列表复制的。原因见 {@code UniversalRuneMixin}：复制时那个列表可能还没有填完。
	 * <p>The list is made again from the list of accepted items, and is not a copy of the number list of the game. {@code UniversalRuneMixin} gives the cause: that list can be incomplete at the time of a copy.
	 *
	 * @param accepted 原料的"可接受物品"列表 / the list of accepted items of the ingredient
	 */
	@Nullable
	public static IntList idsWithRunes(ItemStack[] accepted) {
		boolean needsElement = false;
		boolean needsSin = false;
		for (ItemStack stack : accepted) {
			Item item = stack.getItem();
			needsElement |= elementRunes.contains(item);
			needsSin |= sinRunes.contains(item);
		}
		if (!needsElement && !needsSin) {
			return null;
		}
		IntList ids = new IntArrayList(accepted.length + 2);
		for (ItemStack stack : accepted) {
			ids.add(StackedContents.getStackingIndex(stack));
		}
		if (needsElement) {
			addId(ids, elementRune);
		}
		if (needsSin) {
			addId(ids, sinRune);
		}
		ids.sort(IntComparators.NATURAL_COMPARATOR);
		return ids;
	}

	private static void addId(IntList ids, Item item) {
		int id = StackedContents.getStackingIndex(new ItemStack(item));
		if (!ids.contains(id)) {
			ids.add(id);
		}
	}

	/**
	 * 一个原料的检查结果：游戏自己的编号列表，以及加上了通用符文的列表。两个字段都不可变，所以别的线程读到这个对象时，它一定是完整的。
	 * The result of the check of one ingredient: the number list of the game, and the list with the universal runes added. The two fields cannot change, thus a different thread that reads this object always gets a complete object.
	 *
	 * @param source 游戏自己的编号列表。它只用来按对象比较。 / the number list of the game; it is only used for a comparison by identity
	 * @param ids    加上了通用符文的列表 / the list with the universal runes added
	 */
	public record ExtendedIds(IntList source, IntList ids) {}
}
