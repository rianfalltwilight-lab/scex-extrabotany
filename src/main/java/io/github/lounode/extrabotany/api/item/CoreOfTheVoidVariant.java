package io.github.lounode.extrabotany.api.item;

/**
 * 空之律者核心的一种外观。外观只影响翅膀的样子，所以在逻辑这一边它只有一个名字。
 * One appearance of the Core of the Void. An appearance only changes the look of the wings, thus on the logic side it only has a name.
 *
 * <p>名字保存在物品上，创造模式物品栏给每个名字列出一个物品，提示文字的翻译键是 {@code extrabotany.wings.<名字>}。
 * <p>The name is stored on the item, the creative tab shows one item for each name, and the translation key of the tooltip is {@code extrabotany.wings.<name>}.
 *
 * <p>翅膀的模型和画法是客户端的代码。它们用 {@code CoreOfTheVoidRenderer#registerWings} 登记，名字必须相同。
 * <p>The models of the wings and the method to draw them are client code. {@code CoreOfTheVoidRenderer#registerWings} records them, and the name must be the same.
 */
@FunctionalInterface
public interface CoreOfTheVoidVariant {

	String getId();
}
