# 整合包作者指南：配方、数据映射和 KubeJS / Guide for modpack makers: recipes, data maps and KubeJS

本模组的可调内容都是数据驱动的。你可以用数据包修改它们，也可以用 KubeJS 脚本。
All adjustable content of this mod is data-driven. You can change it with a data pack, or with a KubeJS script.

## 配方类型 / Recipe types

| 类型 / Type | 作用 / Function |
|---|---|
| `extrabotany:pedestal_smash` | 用锤子敲祭坛上的物品。 / Hit the item on a pedestal with a hammer. |
| `extrabotany:stonesia` | 石中姬吃掉一个方块，产生魔力。 / The Stonesia eats a block and makes mana. |
| `extrabotany:omniviolet` | 全知瑾烧掉一个物品，得到燃烧时间。 / The Omniviolet burns an item and gets burn time. |
| `extrabotany:edelweiss` | 雪绒花吃掉一个实体，产生魔力。 / The Edelweiss eats an entity and makes mana. |

### 数据包 JSON / Data pack JSON

```json
{
  "type": "extrabotany:pedestal_smash",
  "input": { "item": "extrabotany:gilded_potato" },
  "output": { "id": "extrabotany:gilded_potato_mashed", "count": 1 },
  "smash_tools": { "tag": "extrabotany:hammers" },
  "strike": 5,
  "exp": 5
}
```

- `strike`：需要敲击的次数。 / The number of hits that are necessary.
- `exp`：完成时掉落的经验。 / The experience that drops on completion.

```json
{
  "type": "extrabotany:stonesia",
  "input": { "type": "botania:tag", "tag": "#c:ores/iron" },
  "outputMana": 900
}
```

`input` 是植物魔法的方块状态原料。常用的三种写法：
`input` is a block state ingredient of Botania. The three usual forms:

- `{ "type": "botania:block", "block": "minecraft:obsidian" }`
- `{ "type": "botania:state", "state": { "Name": "minecraft:furnace", "Properties": { "lit": "true" } } }`
- `{ "type": "botania:tag", "tag": "#minecraft:dirt" }`（标签必须以 `#` 开头。 / The tag must start with `#`.）

```json
{
  "type": "extrabotany:omniviolet",
  "input": { "item": "minecraft:book" },
  "burnTime": 50
}
```

```json
{
  "type": "extrabotany:edelweiss",
  "input": "minecraft:snow_golem",
  "outputMana": 3200
}
```

`input` 可以是一个实体类型的 ID、一个以 `#` 开头的实体类型标签，或者 ID 的列表。
`input` can be the ID of one entity type, an entity type tag that starts with `#`, or a list of IDs.

## 数据映射 / Data maps

数据映射是 NeoForge 的功能。文件在 `data/<命名空间>/data_maps/item/` 下面。
Data maps are a function of NeoForge. The files are below `data/<namespace>/data_maps/item/`.

| 文件 / File | 作用 / Function |
|---|---|
| `data/extrabotany/data_maps/item/cocoon_hatch.json` | 把物品喂给不随想之茧之后，茧孵出哪种实体。 / The entity type that a Cocoon of Desire hatches after it gets the item. |
| `data/neoforge/data_maps/item/furnace_fuels.json` | 梦魇燃料和精神燃料的燃烧时间。这是 NeoForge 自带的数据映射。 / The burn time of the Nightmare Fuel and the Spirit Fuel. This is a data map of NeoForge itself. |

```json
{
  "values": {
    "minecraft:leather": { "entity": "minecraft:cow" },
    "#minecraft:wool": { "entity": "minecraft:sheep" }
  }
}
```

## KubeJS

本模组自带 KubeJS 插件。下面的脚本在 KubeJS 2101.7.2 上测试过：八条配方全部加入，没有失败。
This mod has a KubeJS plugin. The script below was tested with KubeJS 2101.7.2: all eight recipes were added, with no failure.

```js
ServerEvents.recipes(event => {
    const { extrabotany } = event.recipes;

    // 祭坛锤击 / Pedestal hammer
    // pedestal_smash(output, input, [smash_tools = '#extrabotany:hammers'], [exp = 5], [strike = 5])
    extrabotany.pedestal_smash('minecraft:glowstone', 'minecraft:diamond');
    extrabotany.pedestal_smash('minecraft:redstone', 'minecraft:diamond', 'minecraft:stick');
    extrabotany.pedestal_smash('minecraft:emerald', 'minecraft:diamond', '#minecraft:pickaxes', 100, 3);

    // 石中姬 / Stonesia
    // stonesia(outputMana, input)    input: 方块、方块状态或方块标签 / a block, a block state or a block tag
    extrabotany.stonesia(100, 'minecraft:obsidian');
    extrabotany.stonesia(10, '#minecraft:dirt');

    // 雪绒花 / Edelweiss
    // edelweiss(outputMana, input)   input: 实体类型或实体类型标签 / an entity type or an entity type tag
    extrabotany.edelweiss(100, 'minecraft:cow');
    extrabotany.edelweiss(200, '#minecraft:skeletons');

    // 全知瑾 / Omniviolet
    // omniviolet(input, burnTime)    input: 物品或物品标签 / an item or an item tag
    extrabotany.omniviolet('minecraft:apple', 100);
});
```

删除和修改配方使用 KubeJS 的通用方法，例如 `event.remove({ type: 'extrabotany:stonesia' })`。
To remove or to change recipes, use the general methods of KubeJS, for example `event.remove({ type: 'extrabotany:stonesia' })`.
