# 注释规范 / Comment style

本项目的注释使用中英对照。两种语言都遵循 ASD-STE100（简化技术英语）的规则。
This project uses Chinese and English comments together. The two languages obey the rules of ASD-STE100 (Simplified Technical English).

## 1. 格式 / Layout

- 中文行在前，英文行在后。两行说同一件事。
  The Chinese line comes first. The English line comes second. The two lines give the same information.
- 类和公开方法使用 Javadoc。方法内部使用 `//`。
  Use Javadoc for classes and public methods. Use `//` in method bodies.
- 一条注释只说一件事。
  One comment gives one topic.

```java
/**
 * 风之符石。玩家佩戴后，移动速度和飞行速度提高 15%。
 * Aero Stone. The item increases movement speed and flight speed by 15% when a player wears it.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemAeroStone}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemAeroStone}.
 */
public class AeroStoneItem extends BaubleItem {

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		// 先检查间隔，再请求魔力。这样不会每 tick 都扣除魔力。
		// Check the interval first, then request mana. This prevents a mana cost on each tick.
	}
}
```

## 2. 写作规则 / Writing rules

| 规则 / Rule | 中文 | English |
|---|---|---|
| 句长 / Sentence length | 一句不超过 25 个字。 | A sentence has a maximum of 20 words (25 for a description). |
| 语态 / Voice | 用主动句。写明谁做什么。 | Use the active voice. Tell who does what. |
| 时态 / Tense | 说明现在的行为。 | Use the simple present tense. |
| 一句一意 / One idea | 一句话只写一个意思。 | Write one idea in each sentence. |
| 术语 / Terms | 同一个东西只用一个名字。 | Use one name for one thing. |
| 用词 / Words | 不用"可能、大概、应该"这类含糊的词。 | Do not use vague words such as "should", "maybe" or "probably". Use "must", "can" or a fact. |
| 冠词 / Articles | —— | Use articles ("the", "a"). Do not use contractions. |
| 原因 / Reasons | 说明"为什么"，不重复代码已经说明的"做什么"。 | Give the reason. Do not repeat what the code already shows. |

## 3. 固定术语 / Fixed terms

| 中文 | English |
|---|---|
| 魔力 | mana |
| 遗物 | relic |
| 饰品 | bauble |
| 灵魂绑定 | soulbind |
| 服务端 / 客户端 | server / client |
| 存档兼容 | save compatibility |
| 注册 ID | registry ID |
| 原版（指 ExtraMeteorP 的实现） | the original (the ExtraMeteorP implementation) |
| 重制版（指 Lounode 的实现） | Reburn (the Lounode implementation) |

物品、方块、实体和药水效果的名字使用游戏里的名字：中文来自 `zh_cn.json`，英文来自 `en_us.json`。不要自己翻译。植物魔法的内容使用植物魔法语言文件里的名字。
The names of items, blocks, entities and effects are the names in the game: the Chinese name from `zh_cn.json`, the English name from `en_us.json`. Do not make a translation of your own. For Botania content, use the names in the language files of Botania.

游戏里的名字太长或者有歧义时，可以用简称，但是类注释的第一句必须先给出游戏里的名字，再说明简称。
When the name in the game is too long or is ambiguous, a short name is permitted, but the first sentence of the class comment must give the name in the game first and then give the short name.

## 4. 来源标注 / Source notes

从原版移植的类必须在类注释里写明来源分支和原类名。
A class that comes from the original must give the source branch and the original class name in the class comment.

行为和原版不同的时候，必须写明差异和原因。
When the behaviour is different from the original, the comment must give the difference and the reason.
