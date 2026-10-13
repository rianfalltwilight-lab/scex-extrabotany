# 移植审计记录 / Port audit log

本文件记录每一批重写的内容、对照的原版来源，以及发现并修复的问题。
This file records the content of each rewrite batch, the original source that it was compared with, and the defects that were found and corrected.

## 对照基准 / Reference sources

| 名称 / Name | 仓库与提交 / Repository and commit | 用途 / Use |
|---|---|---|
| 原版 1.16 | `ExtraMeteorP/Extra-Botany` 分支 `1.16`，`f96f6d3` | 1.16 才有的内容，以及两个分支都有的内容 / Content only in 1.16, and content in the two branches |
| 原版 1.12 | `ExtraMeteorP/Extra-Botany` 分支 `master`，`4ad2cc9` | 1.12 才有的内容 / Content only in 1.12 |
| 重制版 | `Lounode/ExtraBotany` 标签 `release-1.20.1-1.9.2`，`a4d4f2a` | 本工程的底座 / The base of this project |

规则 / Rules:

1. 两个原版分支都有的物品：移植内容更完整的那一版，并在类注释里写明来源。
   An item that is in the two original branches: port the more complete version, and give the source in the class comment.
2. 注册 ID 和存档数据的键名不变。
   The registry IDs and the keys of the saved data do not change.
3. 原版的明显缺陷不移植。差异写在类注释的"和原版的差异"里。
   The port does not copy clear defects of the original. The class comment gives each difference under "Difference from the original".

## 基线 / Baseline

- 编译通过，7 条弃用警告。 / The build is successful with 7 deprecation warnings.
- 88 个 GameTest 全部通过。 / All 88 GameTests are successful.
- `runData` 失败：方块战利品表生成器对没有战利品表的方块报错。生成资源里有手工修改，和生成器不一致。
  `runData` fails: the block loot generator rejects a block that has no loot table. The generated resources contain manual changes that do not agree with the generators.

## 第一批：伤害系统与饰品 / Batch 1: damage system and baubles

新类 / New classes: `DamageHandler`（重写 / rewritten）、`ElementalStones`、`AeroStoneItem`、`AquaStoneItem`、`EarthStoneItem`、`IgnisStoneItem`、`TheCommunityItem`、`PeaceAmuletItem`、`FrostStarItem`、`DeathRingItem`、`PowerGloveItem`、`PotatoChipsItem`、`GemOfConquestItem`、`BottledFlameItem`、`CosmeticBaubleItem`、`SunRingItem`、`MoonPendantItem`、`FrostWalkerHelper`、`ItemActivationPacket`、`AccessoryRenderers`。

删除的类 / Removed classes: `LegacyAccessories`、`LegacyCosmetics`、`LegacyCelestialAccessories`、`LegacyBottledFlameItem`、`LegacyAccessoryEvents`、`LegacyAccessoryRenderers`。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | `DamageHandler.dmg` 是空实现，永远返回 `false`。原版的穿透伤害和直接扣血都不存在。 / `DamageHandler.dmg` had no body and always returned `false`. The piercing damage and the direct health decrease of the original did not exist. | 按原版恢复五种伤害。新增 `piercing` 和 `magic_piercing` 两个伤害类型。 / Restored the five damage kinds of the original. Added the damage types `piercing` and `magic_piercing`. |
| 2 | "怪物只能伤害玩家"的规则被写成了"所有生物只能伤害玩家"。铁傀儡和狼也受影响。 / The rule "a monster can only damage players" was written as "each mob can only damage players". Iron golems and wolves were also affected. | 改回只对怪物生效。 / The rule applies only to monsters again. |
| 3 | 冰霜之星和死亡戒指要求视线无遮挡。原版没有这个条件。 / The Frost Star and the Death Ring required a clear line of sight. The original has no such condition. | 去掉视线检查。 / Removed the line-of-sight check. |
| 4 | 冰霜之星每 tick 运行一次冰霜行者。 / The Frost Star ran Frost Walker on each tick. | 改为实体换格时触发。 / Now it runs when the entity changes block. |
| 5 | 征服宝石有三条原版没有的属性加成。原版的宝石没有属性，它开启"雷之律者"模式。 / The Gem of Conquest had three attribute bonuses that are not in the original. The gem of the original has no attributes. It starts the Herrscher of Thunder mode. | 去掉自创的属性。律者模式在后续批次移植。 / Removed the invented attributes. A later batch ports the Herrscher mode. |
| 6 | 蚀月之心委托给了"第三只眼"和"雪花吊坠"。原版委托给"旁观者之眼"，并且不会放置雪。 / The Heart of Corrupted Moon delegated to the Third Eye and the Snowflake Pendant. The original delegates to The Spectator and does not put snow. | 按原版改正。 / Corrected to agree with the original. |
| 7 | 蚀月之心的防火检查对每个实体的每次伤害都查一次饰品栏。 / The fire check of the Heart of Corrupted Moon searched the bauble slots for each damage event of each entity. | 改用 `EntityInvulnerabilityCheckEvent`，并且先检查伤害类型。 / Now it uses `EntityInvulnerabilityCheckEvent` and checks the damage type first. |
| 8 | 圣阳尊戒同时增加了攻击距离。原版只增加方块交互距离。 / The Ring of Sacred Sun also increased the attack range. The original only increases the block interaction range. | 去掉攻击距离加成。 / Removed the attack range bonus. |
| 9 | 薯片用类型判断 Boss，只认识三种。触发时没有物品动画。 / The Potato Chips found bosses by class and only knew three. No item animation played on activation. | 改用 `c:bosses` 标签。新增激活动画数据包和头部渲染。 / Now it uses the `c:bosses` tag. Added the activation packet and the head renderer. |
| 10 | 和平友好之证在每次玩家伤害事件里线性扫描全部 243 个物品。 / The Peace Amulet did a linear scan of all 243 items on each player damage event. | 改为检查物品的命名空间。 / Now it checks the namespace of the item. |
| 11 | 瓶中火焰只在亮度 3 以下放火把。原版是亮度 8 以下。 / The Bottled Flame only put a torch below light level 3. The original uses light level 8. | 按原版改正。 / Corrected to agree with the original. |

和原版的差异 / Differences from the original:

- 死亡戒指：原版先扣魔力再检查间隔，结果每 tick 扣魔力但没有效果。本移植先检查间隔。
  Death Ring: the original takes mana before it checks the interval, so it takes mana on each tick with no effect. This port checks the interval first.
- 瓶中火焰：原版每 tick 检查。本移植每 10 tick 检查一次。
  Bottled Flame: the original checks on each tick. This port checks one time each 10 ticks.

测试 / Tests: 新增 `BaubleGameTests`，15 项。全部 103 项通过。
Added `BaubleGameTests` with 15 tests. All 103 tests are successful.

## 第二批：工具、遗物剑和投射物 / Batch 2: tools, relic swords and projectiles

新类 / New classes: `LeftClickItem`、`LeftClickEmptyPacket`、`RelicSwordItem`、`TrueTerrabladeItem`、`TrueShadowKatanaItem`、`InfluxWaverItem`、`StarWrathItem`、`FirstFractalItem`、`ShadowKatanaItem`、`RodOfDiscordItem`、`UuzFanItem`、`BottledPixieItem`、`BottledStarItem`、`PhotonShotgunItem`、`BinderItem`、`ManaDrinkItem`、`TargetedProjectileEntity` 及其五个子类、`ButterflyProjectileEntity`、`BottledStarEntity`、`PhotonProjectileEntity`、`PetPixieEntity`，以及对应的渲染器。

删除的类 / Removed classes: `LegacyTools`、`LegacyRelicSword`、`LegacyFirstFractal`、`LegacySwordProjectile`、`LegacyPhantomSword`、`LegacyProjectile`、`LegacyManaDrinkItem`、`LegacyBinderItem`、`LegacySwordPacket`。

新增注册项 / New registry entry: 实体 `extrabotany:pet_pixie`。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 瓶中精灵召唤的是植物魔法的普通精灵，目标设成了玩家自己。开启 PvP 时精灵会攻击主人然后消失。 / The Bottled Pixie summoned a usual Botania pixie with the player as the target. With PvP on, the pixie attacked its owner and then disappeared. | 按原版新增宠物精灵实体：存活 1000 tick，跟随主人的攻击目标，不受主人伤害。 / Added the pet pixie entity of the original: it lives 1000 ticks, follows the attack target of its owner, and takes no damage from its owner. |
| 2 | 影刃每 tick 对背包里的每把刀先删后加属性组件，物品数据不停同步。 / The Shadow Katana removed and added the attribute component of each katana in the inventory on each tick, so the item data synchronized continuously. | 夜晚加成改为直接加在玩家身上的临时属性。旧物品上残留的加成会被清除。 / The night bonus is now a transient attribute on the player. The bonus that stays on old items is removed. |
| 3 | 影刃修复 1 点耐久只要 60 魔力。原版是 120。精灵伤害固定 3.5。原版是 4，夜晚 8。 / The Shadow Katana repaired 1 durability point for 60 mana. The original uses 120. The pixie damage was always 3.5. The original uses 4, and 8 at night. | 按原版改正。 / Corrected to agree with the original. |
| 4 | 遗物剑的剑气伤害加上了"攻击力 − 9"的自创加成，并且碰到方块就消失。 / The beams of the relic swords had an invented bonus of "attack damage − 9" and disappeared when they hit a block. | 去掉自创加成。剑气穿过方块，和原版一致。 / Removed the invented bonus. The beams go through blocks, as in the original. |
| 5 | 最初分型：攻击速度基础值用了 1.16 的 −1.6，又叠加了 1.12 的 +15%；+5 距离加在了攻击距离上；每次固定扣 640 魔力且魔力不足不能用；有耐久。 / First Fractal: the base attack speed used −1.6 from 1.16 together with +15% from 1.12. The +5 range was on the attack range. Each use took 640 mana and failed with no mana. The item had durability. | 按 1.12 改正：基础攻击速度 −2.4，+5 方块交互距离，前三把剑各 160 魔力（不足时仍可使用），无耐久。 / Corrected to 1.12: base attack speed −2.4, +5 block interaction range, 160 mana for each of the first three swords (the skill still works with no mana), no durability. |
| 6 | 幻影剑每 tick 只打一个目标就停止。原版打范围内的全部目标。 / The phantom sword stopped after one target each tick. The original hits all targets in range. | 按原版改正。 / Corrected to agree with the original. |
| 7 | 真影刃的三把飞刃排成一条横线。原版排成身前的一道弧。 / The three blades of the True Shadow Katana were in a horizontal line. In the original they make an arc in front of the player. | 按原版改正。 / Corrected to agree with the original. |
| 8 | 魔力透镜：超导没有伤害。 / The Mana Lens: SUPERCONDUCTOR did no damage. | 恢复原版的穿透魔法伤害（玩家 25，其他 8）。 / Restored the piercing magic damage of the original (25 to a player, 8 to others). |
| 9 | 左键技能靠一张静态表记录每个玩家上次使用的时间来限速。 / The left-click skills used a static map with the last use time of each player to limit the rate. | 改为重置原版的攻击冷却。不再需要静态表。 / Now the skill resets the vanilla attack cooldown. The static map is not necessary. |
| 10 | 左键数据包、攻击事件和点方块事件里硬编码了具体的物品类。 / The left-click packet, the attack event and the block click event had hard-coded item classes. | 新增 `LeftClickItem` 接口，三条路径统一分发。 / Added the `LeftClickItem` interface. The three paths now use one dispatch. |
| 11 | EGO 的武器攻击另写了一套投射物生成代码，和玩家武器的行为不一致。 / The weapon attacks of the EGO had separate projectile code that did not agree with the player weapons. | 按原版改为直接调用武器物品的攻击方法。 / As in the original, the EGO now calls the attack method of the weapon item. |

和原版的差异 / Differences from the original:

- 影刃空挥时只对怪物召唤精灵。原版会攻击 8 格内的任意生物。
  A swing of the Shadow Katana at nothing only summons a pixie against a monster. The original attacks each living entity within 8 blocks.
- 瓶中星的流星飞向落点。原版的流星向外散开，追踪流星的速度方向相反。
  The stars of the Bottled Star fly to the impact point. In the original the stars spread out, and the tracking star has a reversed velocity.
- 真影刃只从可攻击的实体里选目标。
  The True Shadow Katana only selects a target from the entities that can be attacked.
- 西行寺幽幽子的折扇的蝴蝶没有命中效果。这和原版一致：原版没有写完。
  The butterflies of the Saigyouji Yuyuko's Fan have no hit effect. This agrees with the original, which is not complete.
- 光子霰弹枪在原版里没有注册。这里保留现有的实体弹丸实现。
  The original does not register the Photon Shotgun. This port keeps the current pellet entity implementation.

测试 / Tests: 新增 `RelicSwordGameTests`，10 项。全部 112 项通过。
Added `RelicSwordGameTests` with 10 tests. All 112 tests are successful.

## 第三批：盔甲和银翼射手 / Batch 3: armor and the Silver Bullet

新类 / New classes: `MikuArmorItem`、`ShootingGuardianArmorItem`、`ShootingGuardianHelmetItem`、`SilentSagesArmorItem`、`SilverBulletItem`。

删除的类 / Removed classes: `LegacyArmorItem`、`LegacySilverBulletItem`。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 三套盔甲的护甲值填反了：头盔用了靴子的值，胸甲和护腿互换。例如星空歌姬胸甲是 4，原版是 5。 / The armor values of the three sets were reversed: the helmet used the value of the boots, and the chestplate and the leggings were exchanged. For example, the Starry Idol chestplate had 4 and the original has 5. | 按原版改正：星空歌姬 1/5/4/2，银翼护卫 4/8/7/3，静默贤者 5/9/8/4（头盔/胸甲/护腿/靴子）。 / Corrected to agree with the original: Starry Idol 1/5/4/2, Shooting Guardian 4/8/7/3, Silent Sages 5/9/8/4 (helmet/chestplate/leggings/boots). |
| 2 | 银翼护卫的套装效果完全没有实现：无视护甲、20% 吸血、拉弓加速、治疗减弱都不存在。 / The set effects of the Shooting Guardian were not implemented at all: armor piercing, 20% life steal, faster bow draw and decreased healing did not exist. | 按原版实现四个效果，使用 1.21.1 的伤害减免修饰符和伤害后事件。 / Implemented the four effects of the original with the damage reduction modifier and the post-damage event of 1.21.1. |
| 3 | 银翼护卫每件盔甲的属性加成没有实现。 / The attribute bonus of each Shooting Guardian piece was not implemented. | 每件 +10% 飞行速度、+10% 移动速度、+10% 攻击伤害、+3% 攻击速度。 / Each piece gives +10% flight speed, +10% movement speed, +10% attack damage and +3% attack speed. |
| 4 | 三套盔甲共用一个类，用字符串区分套装。 / The three sets used one class and a string to tell the sets apart. | 每套一个类，套装判定引用各自的四件物品。 / Each set has its own class. The set check refers to the four items of that set. |
| 5 | 银翼射手的冷却记在玩家的物品冷却上。魔力脉冲枪把冷却记在物品组件上，所以两条路径的冷却互不相通。 / The cooldown of the Silver Bullet was on the item cooldown of the player. The Mana Blaster keeps the cooldown in an item component, so the two paths did not share one cooldown. | 免费射击也使用物品的冷却组件。 / The free shot also uses the cooldown component of the item. |
| 6 | 中文的套装说明第三行写的是"伤害抵抗"，但效果是速度提升。 / Line 3 of the Chinese set description said "damage resistance", but the effect is more speed. | 改为"速度提升"。 / Changed to "more speed". |

和原版的差异 / Differences from the original:

- 银翼射手：原版只在说明书里写了"穿齐银翼护卫时不消耗魔力"，代码没有实现。这里按说明书实现。
  Silver Bullet: the original only gives "no mana cost with the full Shooting Guardian set" in the lexicon, and the code does not implement it. This port does what the lexicon says.
- 静默贤者：原版的套装判定引用了暗影武士的四件物品，所以套装永远凑不齐。这里引用静默贤者自己的四件。原版引用的贴图不存在，这里沿用暗影武士的贴图和模型。
  Silent Sages: the set check of the original refers to the four Shadow Warrior items, so the set can never be complete. This port refers to the four Silent Sages items. The texture that the original refers to does not exist, so this port uses the texture and the model of the Shadow Warrior.
- 银翼护卫的吸血按实际造成的伤害计算。原版按护甲减免之前的伤害计算。
  The life steal of the Shooting Guardian uses the damage that was actually applied. The original uses the damage before armor.
- 魔力折扣是每件 15%，四件合计 60%。这和原版一致，因为植物魔法把四个护甲栏的折扣相加。
  The mana discount is 15% for each piece, 60% for the four pieces. This agrees with the original, because Botania adds the discounts of the four armor slots.

## 第四批：坐骑和真理之钥 / Batch 4: mounts and the Key of Truth

新类 / New classes: `MountableAccessory`、`MountEntity`、`MotorEntity`、`UfoEntity`、`KeyOfTruthEntity`、`MountItems`、`MotorItem`、`CosmicCarKeyItem`、`MotorAccessoryItem`、`CosmicCarKeyAccessoryItem`、`MountInputPacket`、`MountSkillPacket`、`MotorRenderer`、`UfoRenderer`、`KeyOfTruthRenderer`、`MotorEnergyHud`、`MountInputHandler`、`ExtraBotanyKeyMappings`。

删除的类 / Removed classes: `LegacyMount`、`LegacyMountItems`、`LegacyMountRenderer`、`LegacyMountInput`、`LegacyMountPacket`、`LegacyKeyMappings`。`LegacyMountModels` 改名为 `MountModels`。薪炎的按键输入暂时拆到 `LegacyFlameInput`。
`LegacyMountModels` is now `MountModels`. The key input of the Flamescion is in `LegacyFlameInput` for the time being.

新增注册项 / New registry entries: 实体 `extrabotany:key_of_truth`；伤害类型 `extrabotany:herrscher_ice`；声音 `entity.motor.ride_on`、`entity.key_of_truth.shoot`。

从原版仓库新取用的资源 / Assets newly taken from the original repository: `textures/entity/keyoftruth.png`、`textures/entity/energybeam.png`。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 机车的运动是自创的：固定速度 0.28，没有转向惯性。原版用船的运动规则：每 tick 加速 0.0625，阻力 0.9，转向有惯性。 / The motor movement was invented: a fixed speed of 0.28 and no turn inertia. The original uses boat movement rules: an acceleration of 0.0625 on each tick, a friction of 0.9, and turn inertia. | 按原版重写运动逻辑。 / Rewrote the movement logic to agree with the original. |
| 2 | 机车不会召唤真理之钥。真理之钥实体不存在。 / The motor did not summon the Key of Truth. The Key of Truth entity did not exist. | 新增真理之钥实体和渲染器。机车充能后每 15 tick 召唤一把。 / Added the Key of Truth entity and its renderer. A charged motor summons one key on each 15th tick. |
| 3 | 机车和机车饰品不是遗物，没有灵魂绑定。 / The motor and the motor accessory were not relics and had no soulbind. | 两者都改为遗物。机车被拆下时，掉落的物品绑定回原来的主人。 / The two items are now relics. When the motor comes apart, the dropped item is bound to its owner again. |
| 4 | 机车的伤害是间接魔法伤害，并且附带缓慢效果。原版是无视护甲、药水和附魔的冰伤害，没有缓慢。 / The motor did indirect magic damage with a Slowness effect. The original does ice damage that ignores armor, effects and enchantments, with no Slowness. | 新增 `herrscher_ice` 伤害类型，去掉缓慢效果。 / Added the `herrscher_ice` damage type and removed the Slowness effect. |
| 5 | 能量不足 200 时按住跳跃键没有惩罚。原版会清空能量。 / A held jump key with less than 200 energy had no penalty. The original sets the energy to zero. | 按原版改正。 / Corrected to agree with the original. |
| 6 | 飞碟朝视线方向飞行（包括上下），疾跑键切换抓取，没有下降键。原版只在水平方向飞行，疾跑键下降，技能键（R）切换抓取。 / The Cosmic Car flew along the view direction (up and down included), the sprint key toggled the catch, and there was no key to go down. The original only flies horizontally, the sprint key goes down, and the skill key (R) toggles the catch. | 按原版改正。说明书和提示文字也改正了。 / Corrected to agree with the original. The lexicon and the tooltip are also corrected. |
| 7 | 碰撞箱尺寸和原版不同：机车 1.4×1（原版 1.675×1.875），飞碟 2.5×1.2（原版 1.5×1.5）。 / The collision boxes did not agree with the original: motor 1.4×1 (original 1.675×1.875), car 2.5×1.2 (original 1.5×1.5). | 按原版改正。 / Corrected to agree with the original. |
| 8 | 没有机车的能量条。贴图 `motorhud.png` 存在但是没有被使用。 / There was no energy bar for the motor. The texture `motorhud.png` was there but nothing used it. | 新增能量条 HUD。 / Added the energy bar HUD. |
| 9 | 骑上机车的声音和真理之钥的射击声没有注册。 / The ride-on sound of the motor and the shoot sound of the Key of Truth were not registered. | 注册两个声音事件并加上字幕。 / Registered the two sound events and added subtitles. |
| 10 | 机车和飞碟共用一个实体类，用布尔值区分。 / The motor and the car used one entity class with a boolean flag. | 拆成一个基类和两个子类。 / Split into one base class and two subclasses. |

和原版的差异 / Differences from the original:

- 1.21.1 的 `Boat` 把运动方法设为私有，所以坐骑继承 `VehicleEntity`，并且自带一份船的运动逻辑。
  The `Boat` of 1.21.1 has private movement methods, so the mounts extend `VehicleEntity` and have their own copy of the boat movement logic.
- 冰伤害把骑手记为攻击者。原版的冰伤害没有攻击者，所以它无视 PvP 设置和和平友好之证。
  The ice damage records the rider as the attacker. The ice damage of the original has no attacker, so it ignores the PvP setting and the Peace Amulet.
- 真理之钥：原版把主人保存在静态字段里，全部钥匙共用最后一把钥匙的主人。这里每把钥匙保存自己的主人。
  Key of Truth: the original keeps the owner in a static field, so all keys share the owner of the newest key. In this port each key keeps its own owner.
- 机车冲刺时只在前方有碰撞箱时抬高。原版在前方不是空气时就抬高，所以穿过草丛时会不断升高。
  A dashing motor only goes up when the block ahead has a collision shape. The original goes up when the block ahead is not air, so it goes up continuously in tall grass.
- 机车物品保留它原来绑定的玩家。原版把放置机车的玩家记为主人，别人放下再拆掉就能改绑。
  The motor item keeps the player that it is bound to. The original records the player that places the motor as the owner, so a different player can change the binding with a place and a break.
- 召唤坐骑和抓取都由服务端自己查找饰品和目标。原版信任客户端发来的物品和实体编号。
  For the summon and the catch, the server finds the accessory and the target itself. The original trusts the item and the entity ID from the client.
- 飞碟不能抓 Boss。只有在可以伤害对方玩家时才能抓玩家。被抓的实体用 UUID 保存。
  The Cosmic Car cannot catch a boss. It can only catch a player when the rider can damage that player. The caught entity is saved by its UUID.
- 飞碟的车头朝实际的运动方向。被抓的实体靠近悬挂点时减速，不再来回抖动。
  The Cosmic Car points in the actual movement direction. The caught entity slows down near the hang point and does not shake.
- 机车的车轮按实际移动的距离转动。原版把角度值当作弧度使用，车轮每 tick 跳到一个不连续的角度。
  The motor wheels turn by the distance that the motor actually moves. The original uses a degree value as radians, so the wheels jump to a different angle on each tick.
- 骑上机车的声音现在会播放。原版在服务端调用了只在客户端有效的方法，所以声音从不播放。
  The ride-on sound of the motor now plays. The original calls a client-only method on the server, so the sound never plays.

需要进游戏确认的项目 / Items to check in the game:

- 骑手的座位高度（机车 0.475，飞碟 0.855）是从 1.16 的数值换算来的。
  The seat heights of the rider (motor 0.475, car 0.855) are converted from the 1.16 values.
- 机车车轮的转动方向和转速。
  The turn direction and the turn speed of the motor wheels.
- 骑手的客户端控制坐骑的运动：加速、转向、冲刺、爬墙、飞碟的六向飞行。这些逻辑只在真实的客户端上运行，自动测试不能覆盖。
  The client of the rider controls the movement of the mount: acceleration, steering, the dash, the wall climb, and the flight of the car in six directions. This logic only runs on a real client, and the automatic tests cannot cover it.

## 第五批：雷之律者 / Batch 5: the Herrscher of Thunder

新类 / New classes: `ExtraBotanyAttachments`、`HerrscherEnergy`、`HerrscherHandler`、`ThunderSlashEntity`、`HerrscherSkillPacket`、`ThunderSlashRenderer`、`HerrscherLayer`、`EnergyBarsHud`、`HerrscherInputHandler`。

新增注册项 / New registry entries: 实体 `extrabotany:thunder_slash`；伤害类型 `extrabotany:herrscher_thunder`；数据附加 `extrabotany:herrscher_energy`；声音 `entity.thunder_slash.slash`。

从原版仓库新取用的资源 / Assets newly taken from the original repository: `textures/entity/herrscher.png`、`textures/gui/hud.png`（改名为 `herrscherhud.png` / renamed to `herrscherhud.png`）、`textures/entity/slash_0.png` 到 `slash_34.png`（放在 `textures/entity/thunder_slash/` / put in `textures/entity/thunder_slash/`）。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 征服宝石没有任何效果。原版里佩戴它的玩家成为雷之律者。 / The Gem of Conquest had no effect. In the original the player that wears it becomes the Herrscher of Thunder. | 按原版实现：能量（每 tick 2 点，上限 600）、反击（200 点能量）、心斩（全部能量）、能量条和玩家渲染层。 / Implemented as in the original: energy (2 on each tick, maximum 600), the counterattack (200 energy), the Thunder Slash (all the energy), the energy bar and the player render layer. |
| 2 | 征服宝石的提示文字描述的是不存在的属性加成。 / The tooltip of the Gem of Conquest gave attribute bonuses that do not exist. | 改为描述雷之律者的能力。 / Changed to give the abilities of the Herrscher of Thunder. |

和原版的差异 / Differences from the original:

- 心斩：原版只在客户端扣除能量，服务端不检查能量，并且信任客户端发来的位置。这里由服务端检查能量并且计算位置。
  Thunder Slash: the original only removes the energy on the client. Its server does not check the energy and trusts the position from the client. In this port the server checks the energy and calculates the position.
- 能量同步：原版在充能的每个 tick 都给玩家发一个数据包。这里客户端自己计算充能，服务端只在反击、释放心斩、充满和戴上宝石时同步。
  Energy synchronization: the original sends a packet to the player on each tick of a charge. In this port the client calculates the charge itself, and the server only synchronizes on a counterattack, a Thunder Slash, a full charge, and when the gem is put on.
- 充能由征服宝石的佩戴 tick 触发。原版每 tick 为每个玩家搜索一次饰品栏。
  The worn tick of the Gem of Conquest does the charge. The original searches the accessory slots of each player on each tick.
- 能量用 NeoForge 的数据附加保存。玩家死亡后能量清零，和原版一致。
  The energy is kept in a NeoForge data attachment. The energy goes back to zero after the player dies, as in the original.
- 雷伤害把玩家记为攻击者，所以 PvP 规则和和平友好之证都生效。
  The thunder damage records the player as the attacker, so the PvP rules and the Peace Amulet apply.
- 心斩保存主人的 UUID。原版不保存主人，读档之后心斩不再造成伤害。
  The Thunder Slash saves the UUID of its owner. The original does not save the owner, so the slash does no damage after a load.

需要进游戏确认的项目 / Items to check in the game:

- 雷之律者的手臂装甲和背后圣痕的位置。
  The position of the arm armor and of the stigma behind the back of the Herrscher of Thunder.
- 心斩的 35 帧动画，以及按住疾跑键对空挥剑的触发方式。
  The 35-frame animation of the Thunder Slash, and the trigger: hold the sprint key and swing a sword at the air.

## 第六批：薪炎（焢煌之境：劫炎永燎） / Batch 6: the Flamescion (Key of Flamescion)

新类 / New classes: `FlamescionState`、`FlamescionHandler`、`FlamescionWeaponItem`、`PlayerOwnedEntity`、`FlamescionAreaEntity`、`FlamescionSlashEntity`、`FlamescionVoidEntity`、`FlamescionUltEntity`、`FlameProjectileEntity`、`FlamescionSwordEntity`、`StrengthenSlashEntity`、`FlamescionSkillPacket`、`IconModelRenderer`、`FlamescionLayer`、`FlamescionSlashRenderer`、`GlyphEffectRenderer`、`StrengthenSlashRenderer`、`FlamescionInputHandler`。

删除的类 / Removed classes: `LegacyFlamescionItem`、`LegacyFlameArea`、`LegacyFlameProjectile`、`LegacyFlameRenderer`、`LegacyFlamescionLayer`、`LegacyFlameSkillPacket`、`LegacyFlameInput`。

新增注册项 / New registry entries: 数据附加 `extrabotany:flamescion_state`。实体和物品的注册 ID 没有变化。
The registry IDs of the entities and of the item did not change.

存档迁移 / Save migration: 旧版本保存在玩家持久数据里的能量和过载状态，在玩家登录时迁移到数据附加。旧版本写在物品自定义数据里的两个键会被清除。
The energy and the overload state that older versions kept in the persistent data of the player are moved to the data attachment when the player logs in. The two keys that older versions wrote into the custom data of the item are removed.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | `flamescion_flame` 伤害类型带有 `is_fire` 标签，所以抗火的生物（下界生物、有抗火效果的玩家）完全免疫薪炎。原版的火焰伤害不是火焰类型。 / The damage type `flamescion_flame` had the `is_fire` tag, so mobs that resist fire (nether mobs, players with Fire Resistance) were fully immune to the Flamescion. The flame damage of the original is not of the fire type. | 去掉 `is_fire` 标签，加上魔法伤害和无击退的标签。 / Removed the `is_fire` tag and added the tags for magic damage and no knockback. |
| 2 | 薪炎的斩击和黑洞给怪物加的是"时空锁定"。原版加的是缓慢 IV。 / The slash and the void of the Flamescion gave Time Lock to monsters. The original gives Slowness IV. | 按原版改正。 / Corrected to agree with the original. |
| 3 | 能量和过载状态同时存在玩家数据和每一把武器的物品数据里，每 tick 把状态写回背包里的全部薪炎之钥。 / The energy and the overload state were kept in the player data and also in the item data of each weapon. On each tick the state was written back to all Keys of Flamescion in the inventory. | 状态只保存在玩家的数据附加里。能量条从数据附加读取。 / The state is only kept in the data attachment of the player. The energy bar reads the data attachment. |
| 4 | 左键技能靠一张静态表记录每个玩家上次攻击的时间。 / The left-click skill used a static map with the last attack time of each player. | 改为重置原版的攻击冷却。 / Now the skill resets the vanilla attack cooldown. |
| 5 | `flamescion_flame` 没有死亡消息。 / `flamescion_flame` had no death message. | 加上语言键。 / Added the language keys. |
| 6 | 一个实体类用枚举表示三种范围效果，另一个实体类用布尔值表示两种投射物，一个渲染器处理五种实体。 / One entity class used an enum for three area effects, a different entity class used a boolean for two projectiles, and one renderer handled five entities. | 每种效果一个实体类，共用两个基类。渲染器拆成三个。 / Each effect has its own entity class, with two base classes. The renderer is split into three. |

和原版的差异 / Differences from the original:

- 强化斩击：原版的构造方法忘了保存主人，所以强化斩击从不造成伤害。这里保存主人，伤害按原版代码里写的数值生效（每 2 tick 5 点，范围 2.5 格）。
  Strengthened Slash: the constructor of the original does not keep the owner, so the slash never does damage. This port keeps the owner, and the damage applies with the values that the original code gives (5 on each 2nd tick, a range of 2.5 blocks).
- 冲刺和终结技：原版的服务端不检查条件，只有客户端检查。这里服务端也检查薪炎模式和冷却。
  The dash and the ultimate: the server of the original does not check the conditions. Only the client checks. In this port the server also checks the Flamescion mode and the cooldown.
- 冲刺在终点有方块时缩短距离，这样玩家不会卡进方块里。玩家仍然可以穿过薄墙。
  The dash becomes shorter when a block is at the end point, so that the player does not get stuck in a block. The player can still go through a thin wall.
- 点燃时立刻把玩家标记为离地。原版等客户端的移动数据包来更新，这段时间里服务端会移除"灼热模式"效果。
  The ignition marks the player as off the ground immediately. The original waits for the movement packet of the client, and in that time the server removes the Incandescence Mode effect.
- 对着空气挥剑放出剑气之后重置攻击冷却。原版的服务端不重置，连点可以无限放出剑气。
  After a swing at the air releases a sword, the attack cooldown is reset. The server of the original does not reset it, so fast clicks release swords with no limit.
- 火焰伤害把玩家记为攻击者，所以 PvP 规则和和平友好之证都生效。点燃和黑洞只影响玩家可以伤害的生物。
  The flame damage records the player as the attacker, so the PvP rules and the Peace Amulet apply. The ignition and the void only affect the mobs that the player can damage.
- 近战命中放出斩击：只有玩家亲手造成的伤害才触发。
  The slash from a melee hit: only damage that the player does directly starts it.
- 能量同步：原版在能量变化的每个 tick 都发送数据包。这里客户端自己计算，服务端只在过载状态改变时和每 10 tick 同步一次。
  Energy synchronization: the original sends a packet on each tick that the energy changes. In this port the client calculates the energy itself, and the server only synchronizes when the overload state changes and on each 10th tick.
- 冲刺键和终结技键使用玩家设置的潜行键和技能键。原版写死了左 Shift 键和 R 键。
  The dash key and the ultimate key use the sneak key and the skill key that the player sets. The original has the left Shift key and the R key hard-coded.
- 剑气、黑洞和终结技仍然用汉字图案渲染（语言键 `mri.*`）。这是原版的画法，所以这些键在英文语言文件里也是汉字。
  The sword, the void and the ultimate are still drawn as pictures made from Chinese characters (language keys `mri.*`). This is how the original draws them, so these keys are also Chinese characters in the English language file.

## 第七批：亚空之矛和犹大的誓约 / Batch 7: the Spear of Subspace and the Judah Oath

新类 / New classes: `OwnedEntity`、`SubspaceEntity`、`SubspaceSpearEntity`、`SubspaceBursts`、`SpearOfSubspaceItem`、`JudahOathItem`、`JudahOathEntity`、`JudahSpearEntity`、`JudahSwordEntity`、`DivineJusticeMobEffect`、`PortalRenderType`、`SubspaceRenderer`、`SubspaceSpearRenderer`、`JudahOathRenderer`、`JudahSpearRenderer`。

删除的类 / Removed classes: `LegacySubspaceSpearItem`、`LegacySubspace`、`LegacySubspaceSpear`、`LegacySubspaceBurst`、`LegacySubspaceRenderer`、`LegacySubspaceSpearRenderer`、`LegacyPortalRenderType`、`LegacyJudahItem`、`LegacyJudahEntity`、`LegacyJudahSword`、`LegacyJudahRenderer`。

新增注册项 / New registry entries: 药水效果 `extrabotany:divine_justice`；伤害类型 `extrabotany:piercing_fire`。实体和物品的注册 ID 没有变化。
The registry IDs of the entities and of the items did not change.

从原版仓库新取用的资源 / Assets newly taken from the original repository: `textures/mob_effect/divine_justice.png`，从原版 1.12 的 `textures/gui/potions.png` 里裁出。
`textures/mob_effect/divine_justice.png`, cut from `textures/gui/potions.png` of the original 1.12.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 犹大的誓约的光矛不会削减敌人的生命上限。"神圣制裁"效果不存在。原版的说明书明确写了这个效果。 / The spears of the Judah Oath did not lower the maximum health of enemies. The Divine Justice effect did not exist. The lexicon of the original gives this effect clearly. | 新增"神圣制裁"效果：每级降低 5% 最大生命值，最高五级，结束前造成真实伤害。 / Added the Divine Justice effect: each level removes 5% of the maximum health, to a maximum of five levels, with true damage before the end. |
| 2 | 亚空之矛的蓄力技能在魔力不足时不能释放。原版仍然可以释放，只是冷却从 600 tick 变成 1200 tick。 / The charged skill of the Spear of Subspace did not work with too little mana. In the original it still works, and only the cooldown changes from 600 to 1200 ticks. | 按原版改正。 / Corrected to agree with the original. |
| 3 | 亚空之矛增加了 2 格的实体交互距离。原版只增加方块交互距离。 / The Spear of Subspace added 2 blocks of entity interaction range. The original only adds block interaction range. | 去掉实体交互距离。 / Removed the entity interaction range. |
| 4 | 亚空之矛是钻石等级的剑，有耐久。原版里它是没有耐久的遗物。 / The Spear of Subspace was a sword of the diamond tier with durability. In the original it is a relic with no durability. | 改为没有耐久的遗物物品。 / Changed to a relic item with no durability. |
| 5 | 亚空之矛的左键技能用"工具折扣"扣魔力。原版的左键技能不享受折扣。 / The left-click skill of the Spear of Subspace took mana with the tool discount. The left-click skill of the original gets no discount. | 按原版改正。 / Corrected to agree with the original. |
| 6 | 樱花的誓约的剑只造成一次普通的攻击伤害，护甲可以减免。原版是无视护甲的魔法伤害加火焰伤害。 / The swords of the Oath of Sakura only did one usual attack hit that armor decreases. The original does magic damage that ignores armor, plus fire damage. | 改为穿透魔法伤害加新的穿透火焰伤害。 / Changed to piercing magic damage plus the new piercing fire damage. |
| 7 | 投出誓约时播放了三叉戟的声音。原版没有声音。 / A trident sound played when the oath was thrown. The original has no sound. | 去掉这个声音。 / Removed the sound. |
| 8 | 光矛的伤害是玩家近战类型，会触发荆棘等近战效果。 / The spear damage was of the player melee type, which starts melee effects such as thorns. | 改为雷电类型的伤害，并且把主人记为攻击者。 / Changed to damage of the lightning type, with the owner recorded as the attacker. |

和原版的差异 / Differences from the original:

- 樱花的誓约：原版直接修改全局的魔法伤害和熔岩伤害对象，让它们永久无视护甲。这会改变整个游戏里的毒、熔岩等伤害。这里使用模组自己的伤害类型。
  Oath of Sakura: the original changes the global magic and lava damage objects so that they ignore armor permanently. This changes poison, lava and other damage in the full game. This port uses the damage types of the mod.
- 光矛：原版把投出者保存在静态字段里，全部光矛共用一个投出者。这里每支光矛保存自己的主人。
  Spears: the original keeps the thrower in a static field, so all spears share one thrower. In this port each spear keeps its own owner.
- 五角星：原版用整数坐标反复旋转来计算顶点，每次旋转都有取整误差。这里用精确的角度计算。
  The star: the original calculates the points with repeated rotations of integer coordinates, and each rotation has a rounding error. This port uses exact angles.
- 亚空之矛的蓄力技能在按满 200 tick 时也会释放。原版只在松开右键时释放。
  The charged skill of the Spear of Subspace also works at the end of the full 200 ticks. The original only uses it when the use key is released.
- 亚空之矛的左键技能要求玩家是遗物的主人。原版只在蓄力技能里检查。
  The left-click skill of the Spear of Subspace needs the player to own the relic. The original only checks this for the charged skill.
- 亚空之矛的渲染位置：原版的渲染器按方块实体的写法平移了模型，在 1.21.1 上会偏离碰撞箱。这里把模型中心对准碰撞箱中心。
  The render position of the Subspace Spear: the renderer of the original moves the model as a block entity renderer does, which is away from the collision box on 1.21.1. This port puts the center of the model at the center of the collision box.
- 犹大的誓约在原版里还可以当作装饰品附加在饰品上。这个功能没有移植。
  In the original the Judah Oath can also be attached to an accessory as a cosmetic item. This function is not ported.
- 结界的粒子圈每 6 度一个粒子。原版每 2 度一个，每 tick 生成 180 个粒子。
  The particle ring of the field has one particle for each 6 degrees. The original has one for each 2 degrees, which is 180 particles on each tick.

## 第八批：王之花园、静籁永恒、全能指环和补给袋 / Batch 8: the Garden of King's Law, the Silent Eternity, the ALL FOR ONE ring and the supply bag

新类 / New classes: `KingGardenItem`、`FlowerWeaponEntity`、`FlowerWeaponRenderer`、`SilentEternityItem`、`AllForOneRingItem`、`BoxEquipmentMixin`、`SupplyBagItem`。

删除的类 / Removed classes: `LegacyKingGardenItem`、`LegacyFlowerWeapon`、`LegacyFlowerWeaponRenderer`、`LegacyEternityItem`、`LegacyBoxSimulator`、`LegacyBoxEquipmentMixin`、`LegacySupplyBagItem`。

新增数据 / New data: 配方 `extrabotany:king_garden_clear`（把王之花园单独放进合成格，清空里面的花）。原版有这个配方。
The recipe `extrabotany:king_garden_clear` (put the garden alone into the crafting grid to remove its flowers). The original has this recipe.

注册 ID、物品数据的键和补给袋的存档格式都没有变化。
The registry IDs, the keys of the item data and the save format of the supply bag did not change.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 王之花园不接受星尘莲，所以 3 号花之武器无法获得。 / The garden did not accept the Stardust Lotus, so Flower Weapon 3 could not be made. | 加上星尘莲。 / Added the Stardust Lotus. |
| 2 | 花之武器穿过方块。原版里它撞上方块就消失，熵变花的武器还会爆炸。 / The Flower Weapon went through blocks. In the original it goes away when it hits a block, and the weapon of the Entropinnyum also makes an explosion. | 按原版改正。 / Corrected to agree with the original. |
| 3 | 花之武器用"受击动画计时"判断目标能否被命中。原版用无敌帧。 / The Flower Weapon used the hurt animation timer to find if a target can be hit. The original uses the invulnerability time. | 按原版改正。 / Corrected to agree with the original. |
| 4 | 花之武器的召唤位置比原版低 0.9 格，因为没有加上玩家身高的一半。 / The summon position of the Flower Weapon was 0.9 blocks lower than in the original, because half of the player height was not added. | 按原版改正。 / Corrected to agree with the original. |
| 5 | 王之花园和花之武器播放三叉戟的声音，飞行时是村民的绿色粒子。原版用植物魔法的宝具声音和绿色的光点粒子。 / The garden and the Flower Weapon played trident sounds, and the flight had green villager particles. The original uses the treasure weapon sounds of Botania and green wisp particles. | 按原版改正。 / Corrected to agree with the original. |
| 6 | 王之花园拿在副手蓄力时，蓄力状态每 tick 被重置。 / When the garden was charged in the off hand, the charge state was reset on each tick. | 副手里的花园不再被重置。 / A garden in the off hand is not reset. |
| 7 | 花之武器每 tick 同步两个计数器，每把武器每 tick 发一个数据包。 / The Flower Weapon synchronized two counters on each tick, which is one packet for each weapon on each tick. | 计数器不再同步，客户端自己计数。 / The counters are not synchronized, and the client counts them itself. |
| 8 | 补给袋的奖池抽完之后会重新装满，所以玩家可以再次得到静籁永恒。原版抽完之后每次只给 2 个绿宝石。 / The pool of the supply bag was filled again after it was empty, so a player could get the Silent Eternity again. In the original each draw from an empty pool only gives 2 emeralds. | 按原版改正。 / Corrected to agree with the original. |

和原版的差异 / Differences from the original:

- 全能指环和精灵王指环：原版只调用饰品盒中饰品的"佩戴 tick"。这里还模拟属性修饰符和植物魔法的装备查找，并且同类的饰品只模拟一个。原因见 `AllForOneRingItem` 的类注释：植物魔法 1.21.1 的饰品大多不再通过佩戴 tick 起作用。
  ALL FOR ONE and Ring of Elven King: the original only calls the "worn tick" of the accessories in the box. This port also simulates attribute modifiers and the equipment lookup of Botania, and only one accessory of each type is simulated. The class comment of `AllForOneRingItem` gives the reason: most accessories of Botania 1.21.1 do not work through the worn tick.
- 静籁永恒：静止计数放在内存里。原版每 tick 把它写进物品数据。
  Silent Eternity: the stillness counter is kept in memory. The original writes it into the item data on each tick.
- 限量补给袋：玩家第一次打开时一定得到一个空的律者核心。这是这个移植版加的规则，因为制作空之律者核心需要它。奖池保存在主世界的存档数据里，原版保存在玩家的持久数据里。
  Supply Bag Limited Edition: the first bag that a player opens always gives an Empty Herrscher Core. This port adds that rule, because the Core of the Void needs it. The pool is kept in the saved data of the overworld, and the original keeps it in the persistent data of the player.
- 王之花园：物品没有被选中时，蓄力状态会重置。原版只在全部武器召唤完再松开时重置。
  Garden of King's Law: the charge state is reset when the item is not selected. The original only resets it on a release after all weapons are summoned.
- 花之武器的魔法伤害把玩家记为攻击者。
  The magic damage of the Flower Weapon records the player as the attacker.

## 第九批：魔力机器、活石桶、不随想之茧、奖杯、星尘莲和液态魔力 / Batch 9: mana machines, the Livingrock Barrel, the Cocoon of Desire, the trophy, the Stardust Lotus and Fluided Mana

新类 / New classes: `ManaMachineBlockEntity`、`ManaBufferBlockEntity`、`ManaGeneratorBlockEntity`、`ManaLiquefactionBlockEntity`、`ManaMachineBlock`、`ManaBufferBlock`、`LivingrockBarrelBlock`、`LivingrockBarrelBlockEntity`、`CocoonOfDesireBlock`、`CocoonOfDesireBlockEntity`、`TrophyBlock`、`ExtraBotanyFluids`、`ExtraBotanyDataMaps`、`ManaMachineWandHud`、`StardustLotusWandHud`、`QuantumManaBufferModel`、`QuantumManaBufferRenderer`、`CocoonOfDesireRenderer`、`LivingrockBarrelRenderer`。`StardustLotusBlockEntity` 重写了。
`StardustLotusBlockEntity` was written again.

删除的类 / Removed classes: `LegacyMachineBlock`、`LegacyMachineEntity`、`LegacyBarrelBlock`、`LegacyBarrelEntity`、`LegacyCocoonBlock`、`LegacyCocoonEntity`、`LegacyTrophyBlock`、`LegacyLotusBlocks`、`LegacyLotusHud`、`LegacyQuantumModel`、`LegacyQuantumRenderer`、`LegacyManaFluid`。

新增数据 / New data: 数据映射 `data/extrabotany/data_maps/item/cocoon_hatch.json`（不随想之茧的孵化表）。
The data map `data/extrabotany/data_maps/item/cocoon_hatch.json` (the hatch table of the Cocoon of Desire).

注册 ID、方块实体的存档键和配置文件的键都没有变化。活石桶现在把液体存在 `Fluid` 键里，旧的 `fluid` 和 `amount` 键仍然可以读取。
The registry IDs, the save keys of the block entities and the keys of the configuration file did not change. The Livingrock Barrel now saves the fluid in the key `Fluid`, and the old keys `fluid` and `amount` can still be read.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 这个模组全部花朵的自定义数据都没有同步到客户端。两个花朵基类没有实现 `getUpdateTag`，所以客户端每次同步之后数据都变回默认值。受影响的有星尘莲、风铃草、烦人花等花的法杖界面和动画状态。 / The custom data of all flowers of this mod was not synchronized to the clients. The two flower base classes did not implement `getUpdateTag`, thus the data on the client went back to the defaults after each synchronization. This had an effect on the wand displays and the animation states of the Stardust Lotus, the Bellflower, the Annoyingflower and other flowers. | 两个基类现在把子类的数据写进同步标签。 / The two base classes now write the data of the subclass into the update tag. |
| 2 | 星尘莲每传送一批生物就关闭传送门，并且需要新的纸和魔力。原版里传送门打开之后一直保持打开。 / The Stardust Lotus closed the portal after each group of creatures, and needed new paper and mana. In the original the portal stays open after it opens. | 按原版改正。 / Corrected to agree with the original. |
| 3 | 星尘莲只要绑定了魔力池，花费就减少 20%。原版里只有附近有充能的自然蕴息宝珠时才减少。 / The cost of the Stardust Lotus decreased by 20% whenever the flower was bound to a mana pool. In the original the cost only decreases when a charged Nature Orb is near. | 取消这个折扣。见下面的差异。 / This discount was removed. See the differences below. |
| 4 | 重新绑定星尘莲的目标会清空已经消耗的魔力和纸。 / A new target for the Stardust Lotus removed the mana and the paper that the flower had used. | 按原版改正：保留。 / Corrected to agree with the original: they stay. |
| 5 | 星尘莲的绑定逻辑写在两个匿名的方块子类里。 / The binding logic of the Stardust Lotus was in two anonymous block subclasses. | 绑定逻辑移到坐标读取器（`BinderItem`）里。星尘莲现在使用普通的花方块。 / The binding logic is now in the Position Reader (`BinderItem`). The Stardust Lotus now uses the usual flower blocks. |
| 6 | 不随想之茧只接受白色羊毛。原版接受任意颜色的羊毛。 / The Cocoon of Desire only accepted white wool. The original accepts wool of any color. | 孵化表使用标签 `minecraft:wool`。 / The hatch table uses the tag `minecraft:wool`. |
| 7 | 用绿宝石块孵化出的村民是成年的。原版是幼年的。 / A villager from a block of emerald was an adult. In the original it is a baby. | 所有能长大的生物孵化时都是幼年。 / All creatures that can grow hatch as babies. |
| 8 | 茧不摇动。原版的茧有摇动动画。 / The cocoon did not wobble. The cocoon of the original has a wobble animation. | 加上渲染器。摇动使用孵化进度。 / A renderer was added. The wobble uses the hatch progress. |
| 9 | 茧在孵化期间每 tick 把区块标记为需要保存，并且每 tick 通知旁边的比较器。茧每 tick 查找掉落物。 / During the incubation the cocoon marked the chunk for saving on each tick and told the adjacent comparators on each tick. The cocoon searched for dropped items on each tick. | 每 20 tick 标记一次，每 10 tick 查找一次，不再通知比较器。 / The mark is set one time for each 20 ticks, the search runs one time for each 10 ticks, and the comparators are not told. |
| 10 | 茧吸收掉落物的范围比原版大（3x3x3 格，原版是 2x2x2 格）。 / The range in which the cocoon took dropped items was larger than in the original (3x3x3 blocks, and the original has 2x2x2 blocks). | 按原版改正。 / Corrected to agree with the original. |
| 11 | 活石桶看不出里面有什么。原版会绘制桶里的液体。 / The contents of the Livingrock Barrel could not be seen. The original draws the fluid in the barrel. | 加上渲染器。 / A renderer was added. |
| 12 | 活石桶不接受手里的液体容器时，容器会执行自己的动作。例如水桶把水倒在桶旁边。原版阻止这个动作。 / When the Livingrock Barrel did not accept the fluid container in the hand, the container did its own action. For example, a water bucket put its water adjacent to the barrel. The original stops this action. | 按原版改正。 / Corrected to agree with the original. |
| 13 | 活石桶每次液体变化都发一个同步数据包。管道每 tick 抽取时就是每 tick 一个数据包。 / The Livingrock Barrel sent one synchronization packet for each change of the fluid. With a pipe that drains on each tick, this is one packet on each tick. | 最多每 10 tick 发一次。 / At most one packet is sent for each 10 ticks. |
| 14 | 活石桶不保留液体的数据组件。 / The Livingrock Barrel did not keep the data components of the fluid. | 现在保存完整的液体堆。 / The full fluid stack is now saved. |
| 15 | 魔力缓存器没有比较器输出。原版 1.12 有。 / The Mana Buffer had no comparator output. The original 1.12 has one. | 加上，算法和魔力池相同。 / Added, with the same formula as the mana pool. |
| 16 | 魔力缓存器的硬度是 5.5。原版是 2。 / The hardness of the Mana Buffer was 5.5. The original has 2. | 按原版改正。 / Corrected to agree with the original. |
| 17 | 魔力缓存器和量子魔力缓存器互相主动交换魔力，所以两者之间的速度是 6000 点每 tick。原版里只有量子魔力缓存器主动，速度是 5000 点。 / The Mana Buffer and the Quantum Mana Buffer each started transfers with the other, thus the speed between them was 6000 mana for each tick. In the original only the Quantum Mana Buffer starts the transfer, and the speed is 5000 mana. | 按原版改正。 / Corrected to agree with the original. |
| 18 | 四种机器都可以含水。它们是完整方块，所以水桶会把水倒进方块里面。 / The four machines could be waterlogged. They are full blocks, thus a water bucket put its water into the block. | 机器不再可以含水。旧存档里的含水状态在读取时被忽略。 / The machines cannot be waterlogged now. The load ignores the waterlogged state in old saves. |
| 19 | 机器每 tick 用字符串在一张表里查找配置值，每台机器每 tick 好几次。液态魔力转化器每 tick 每个面都按 ID 在注册表里查找一次液态魔力。FE转换器和液态魔力转化器每 tick 对六个面各做一到两次能力查询。 / On each tick the machines looked up configuration values by string in a map, several times for each machine. On each tick and for each side, the Mana Liquefaction Device looked up Fluided Mana by ID in the registry. On each tick the Flux Manafield and the Mana Liquefaction Device did one or two capability queries for each of the six sides. | 配置有了带类型的读取方法。液态魔力是一个常量。邻居的能力用 `BlockCapabilityCache` 缓存。 / The configuration now has typed accessors. Fluided Mana is a constant. `BlockCapabilityCache` keeps the capabilities of the neighbours. |
| 20 | 魔力已满的液态魔力转化器继续从容器抽取液体，直到内部储量满为止。原版在魔力已满时停止抽取。 / A Mana Liquefaction Device with full mana continued to drain fluid until its internal store was full. The original stops the drain when the mana is full. | 按原版改正。 / Corrected to agree with the original. |

和原版的差异 / Differences from the original:

- 魔力缓存器的传输速度是 1000 点每 tick。这是原版 1.16 的值。原版 1.12 的默认值是 400。
  The transfer speed of the Mana Buffer is 1000 mana for each tick. This is the value of the original 1.16. The default of the original 1.12 is 400.
- FE转换器只抽取它装得下的 FE，并且魔力已满时不转换。原版在这两种情况下会丢失 FE。FE转换器不再用"无方向"的查询作为后备，因为这种查询会绕过邻居的面设置。
  The Flux Manafield only takes the FE that it has space for, and it does not convert when the mana is full. The original loses FE in these two conditions. The Flux Manafield does not use the query without a side as a fallback, because such a query ignores the side configuration of the neighbour.
- 液态魔力转化器按最小的整数份转换，所以没有资源凭空出现或消失。内部储量的上限是 16000。原版里储量可以变成负数，也没有上限，容器装不下的液体会消失。每 tick 的最大速度和原版相同。
  The Mana Liquefaction Device converts in the smallest whole lots, thus no resource comes from nothing and no resource is lost. The limit of the internal store is 16000. In the original the store can become negative and has no limit, and the fluid that does not fit into a tank is lost. The maximum speed for each tick is the same as in the original.
- 星尘莲没有移植"自然蕴息宝珠让花费减少 20%"的规则。原版 1.12 有 6 种花使用自然蕴息宝珠，这个移植版的花都不使用它。
  The rule "a Nature Orb decreases the cost of the Stardust Lotus by 20%" is not ported. Six flowers of the original 1.12 use the Nature Orb, and no flower of this port uses it.
- 星尘莲把生物传送到目标方块中心的上方（原版是方块的角）。刚被传送的生物在 40 tick 内不会再被星尘莲传送，这样两朵互相指向的星尘莲不会让生物不停地来回传送。星尘莲不传送旁观者。星尘莲只消耗刚好的魔力。
  The Stardust Lotus teleports a creature to the position above the center of the target block (the original uses the corner of the block). A creature that was teleported is not teleported again by a Stardust Lotus for 40 ticks, thus two lotuses that point at each other do not teleport a creature back and forth without end. The Stardust Lotus does not teleport spectators. The Stardust Lotus only uses the exact quantity of mana.
- 不随想之茧保存孵化进度，并且在放入或取出物品时把计时归零。茧每次只吸收一个掉落物。孵化表可以用数据包修改。玩家的物品栏已满时，取出的物品掉在玩家脚下。
  The Cocoon of Desire saves the hatch progress, and sets the timer to zero when an item is put in or taken out. The cocoon takes only one dropped item at a time. A data pack can change the hatch table. When the inventory of the player is full, the item drops at the feet of the player.
- 茧的摇动使用孵化进度。原版使用"方块实体存在的时间"，所以茧只在区块加载后的头两分钟摇动。
  The wobble of the cocoon uses the hatch progress. The original uses "the time that the block entity has existed", thus the cocoon only wobbles in the first two minutes after the chunk loads.
- 活石桶每 10 tick 给花药台加一次水（原版每 tick）。桶里的液体使用所在位置的光照和液体自己的颜色。
  The Livingrock Barrel fills apothecaries one time for each 10 ticks (the original does it on each tick). The fluid in the barrel uses the light at its position and the tint of the fluid itself.
- 原版用 WAILA 和 The One Probe 显示机器的数值。这里用森林法杖的界面显示魔力，以及 FE转换器和液态魔力转化器的内部储量。
  The original shows the values of the machines with WAILA and The One Probe. This port uses the display of the Wand of the Forest to show the mana, and the internal store of the Flux Manafield and of the Mana Liquefaction Device.
- 和原版相同、没有改动的一点：水平相邻的两个魔力缓存器每 tick 互相吸取魔力，所以魔力会在它们之间来回移动。原版的设计是把缓存器竖着叠放。
  One point that is the same as in the original and was not changed: two Mana Buffers that are horizontally adjacent take mana from each other on each tick, thus the mana moves back and forth between them. The design of the original is to put the buffers in a vertical stack.

需要在游戏里确认的项目 / Items to check in the game:

- 不随想之茧的摇动动画，以及用渲染器绘制的茧和普通方块相比亮度是否自然。
  The wobble animation of the Cocoon of Desire, and if the brightness of the cocoon from the renderer looks natural in comparison with usual blocks.
- 活石桶里液体表面的位置和颜色（水、液态魔力、岩浆）。
  The position and the color of the fluid surface in the Livingrock Barrel (water, Fluided Mana, lava).
- 森林法杖看四种机器时的界面。
  The display when the Wand of the Forest looks at the four machines.
- 星尘莲的进度光圈和法杖界面（现在客户端能收到数据了）。
  The progress ring and the wand display of the Stardust Lotus (the client now gets the data).

## 第十批：空之律者 / Batch 10: the Herrscher of the Void

新类 / New classes: `VoidHerrscher`、`VoidHerrscherAI`、`SubspaceLanceEntity`、`SwordDomainEntity`、`VoidFieldEntity`、`VoidCallerItem`、`VoidHerrscherModel`、`VoidHerrscherRenderer`、`SubspaceLanceRenderer`、`SwordDomainRenderer`、`VoidFieldRenderer`。

删除的类 / Removed classes: `LegacyVoidHerrscher`、`LegacyLance`、`LegacySwordDomain`、`LegacyVoidField`、`LegacyOwnedEntity`、`LegacyVoidCallerItem`、`LegacyVoidHerrscherRenderer`、`LegacyBossSupportRenderer`、`LegacyIconRenderer`。

注册 ID 和存档的键都没有变化。实体类型现在登记在 `ExtraBotanyEntityType` 里（`VOID_HERRSCHER`、`SUBSPACE_LANCE`、`SWORD_DOMAIN`、`VOID_FIELD`）。
The registry IDs and the save keys did not change. The entity types are now in `ExtraBotanyEntityType` (`VOID_HERRSCHER`, `SUBSPACE_LANCE`, `SWORD_DOMAIN`, `VOID_FIELD`).

数据 / Data: 空之律者的掉落表 `entities/void_herrscher` 加上了潘多拉魔盒。以前掉落物写在代码里，掉落表没有被使用。
The loot table `entities/void_herrscher` of the Herrscher of the Void now has the Pandora's Box. Before, the drops were in the code and the loot table was not used.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 空之律者使用盖亚守护者III的行为树。它同时执行两套传送、导弹和地雷，还召唤精灵。原版的空之律者没有精灵。 / The Herrscher of the Void used the behavior tree of Guardian of Gaia III. It did two sets of teleports, missiles and landmines at the same time, and it also summoned pixies. The Herrscher of the original has no pixies. | 空之律者有了自己的行为树（`VoidHerrscherAI`），里面只有和盖亚守护者共用的规则。 / The Herrscher now has its own behavior tree (`VoidHerrscherAI`) that only has the rules that it shares with the Guardians of Gaia. |
| 2 | 召唤之后有 200 tick 的出场动画，这段时间 Boss 无敌并且从 1 点生命值开始回血。原版没有出场动画，战斗立刻开始。 / After the summon there was a start animation of 200 ticks, during which the boss could not be hurt and its health went up from 1. The original has no start animation, and the fight starts immediately. | 按原版改正。 / Corrected to agree with the original. |
| 3 | 开头 5 次无效攻击的判定在别的规则之前，所以这 5 次攻击不会累计伤害，也不会缩短特殊攻击的冷却。 / The check of the first 5 attacks that have no effect came before the other rules, thus these 5 attacks did not add to the total damage and did not decrease the cooldown of the special attacks. | 按原版的顺序改正：别的规则照常执行，只有伤害变成 0。 / Corrected to the sequence of the original: the other rules run as usual, and only the damage becomes 0. |
| 4 | 困难难度下 Boss 有 15 点护甲，导弹和地雷更强。原版的"困难模式"参数永远是关闭的。 / On hard difficulty the boss had 15 armor, and the missiles and the landmines were stronger. The "hard mode" parameter of the original is always off. | 取消难度加成。 / The difficulty bonus was removed. |
| 5 | 场地检查要求信标下面有 3x3 的底座，并且检查 7 格高的空间。原版只要求信标、四个盖亚水晶塔和 5 格高的空间。 / The arena check required a 3x3 base below the beacon, and examined a space of 7 blocks in height. The original only requires the beacon, four Gaia Pylons and a space of 5 blocks in height. | 按原版改正。 / Corrected to agree with the original. |
| 6 | 在场玩家比召唤时多的时候，原版取消战斗。这个规则没有被移植。 / The original cancels the fight when there are more players than at the time of the summon. This rule was not ported. | 加上。 / Added. |
| 7 | Boss 不会远离玩家。原版里玩家靠近到 6 格以内时，Boss 会走开。 / The boss did not go away from players. In the original the boss walks away when a player comes within 6 blocks. | 加上。 / Added. |
| 8 | 生命值可以一次减少超过 25 点（例如别的模组直接修改生命值时）。原版限制每次最多减少 25 点。 / The health could decrease by more than 25 at one time (for example when a different mod changed the health directly). The original limits each decrease to 25. | 加上这个限制。`/kill` 命令不受限制。 / The limit was added. The `/kill` command is not limited. |
| 9 | 护盾的伤害上限用小数计算（21.25、18.06、15.35）。原版去掉小数（21、17、14）。 / The damage limit with shields was calculated with fractions (21.25, 18.06, 15.35). The original removes the fraction (21, 17, 14). | 按原版改正。 / Corrected to agree with the original. |
| 10 | 受伤之后 Boss 向远离攻击者的方向跳。原版和植物魔法的盖亚守护者一样，向攻击者的方向跳。 / After a hit the boss hopped away from the attacker. The original hops to the attacker, the same as the Guardian of Gaia of Botania. | 按原版改正。跳跃的速度也改成原版的值。 / Corrected to agree with the original. The speed of the hop also has the values of the original. |
| 11 | 一阶段 Boss 传送到地面上方 1 格。原版传送到地面。 / In rank I the boss teleported to 1 block above the floor. The original teleports to the floor. | 按原版改正。 / Corrected to agree with the original. |
| 12 | 巫毒诅咒的等级是 2、4 或 6 级。原版因为运算符的优先级，实际是前 90 秒 1 级，之后 3 级。 / The level of the Witch Curse was 2, 4 or 6. Because of operator precedence, the original really has level 1 for the first 90 seconds and level 3 after that. | 按原版的实际行为改正。 / Corrected to the real behavior of the original. |
| 13 | Boss 每 tick 给每名玩家重新加一次巫毒诅咒，所以每 tick 给每名玩家发一个效果数据包。 / The boss applied the Witch Curse to each player again on each tick, thus each player got an effect packet on each tick. | 效果快结束时才续上。 / The effect is only applied again when it is near its end. |
| 14 | 亚空之矛枪每 35 tick 直接伤害 8 格内最近的玩家。原版是闪电链：矛枪旁边的生物引发它，它在附近的生物之间跳跃。 / The Subspace Lance directly hurt the nearest player within 8 blocks for each 35 ticks. The original has a lightning chain: a creature adjacent to the lance starts it, and it jumps between the creatures near it. | 按原版重写，并显示闪电。 / Written again from the original, with a lightning effect. |
| 15 | 矛枪落在地面上。原版的矛枪停在地面上方约 1.8 格，这样画出来的矛才正好插在地上。第一把矛枪在第 300 tick 从空中落下，原版在第 100 tick 直接出现在信标的位置。 / The lance fell onto the floor. The lance of the original stops approximately 1.8 blocks above the floor, and then the drawn spear is correctly in the ground. The first lance fell from the air at tick 300, and in the original it appears at the position of the beacon at tick 100. | 按原版改正。 / Corrected to agree with the original. |
| 16 | 审判之剑和矛枪只显示末地烛粒子。原版有金色光点、电网的光点和剑旁边的光环。 / The sword of judgment and the lance only showed End Rod particles. The original has golden wisps, sparkles for the web and a halo adjacent to the sword. | 按原版加上。 / Added as in the original. |
| 17 | 虚空力场在服务端每 tick 给范围内的每个生物发一个速度数据包。 / On each tick the void field sent one velocity packet from the server for each creature in its range. | 和原版一样，两端各自减速，不发数据包。 / The same as in the original, each side slows the creatures itself, and no packet is sent. |
| 18 | 支援者是 7 个虚构的名字。原版使用模组贡献者的名单。 / The supporters were 7 fictional names. The original uses the list of contributors of the mod. | 使用原版仓库 `contributors.md` 里的 45 个名字。 / The 45 names from `contributors.md` of the original repository are used. |
| 19 | "糖果"支援只给玩家 4 点治疗和一个效果。原版让玩家吃掉两颗糖果。 / The "candy" support only gave the player 4 healing and one effect. The original makes the player eat two candies. | 按原版改正。 / Corrected to agree with the original. |
| 20 | 进入二阶段和三阶段时有聊天消息。原版没有。 / There were chat messages at the start of rank II and rank III. The original has none. | 删除。 / Removed. |
| 21 | Boss 血条是紫色。原版是黄色。场地边缘的粒子是暗红色，原版是蓝紫色。 / The boss bar was purple. The original has yellow. The particles at the edge of the arena were dark red, and the original has blue-purple. | 按原版改正。 / Corrected to agree with the original. |
| 22 | 用 `/summon` 召唤的 Boss 读不到存档数据，所以它的冷却时间和"无效攻击"次数都是 0。 / A boss from `/summon` had no save data, thus its cooldowns and its number of "attacks with no effect" were 0. | 没有存档数据时保留默认值。 / The defaults stay when there is no save data. |

和原版的差异 / Differences from the original:

- 原版的代码里有特殊攻击的预警消息，但是因为一个判断写错了，它们从来没有被发出去。这里会发出"点名"和"虚空裁决"的预警，以及被点名时的提示。
  The code of the original has warning messages for the special attacks, but because of an incorrect condition they are never sent. This port sends the warnings for the "Smite" and the "Void Judge", and the message for the player that is smitten.
- 原版在二阶段抽到"虚空裁决"之后会卡住，直到三阶段都不再使用特殊攻击。这里改为 200 tick 之后回到"点名"。
  The original stops after rank II selects the "Void Judge", and uses no special attack until rank III. This port goes back to the "Smite" after 200 ticks.
- 物品检查使用这个模组里盖亚守护者的公共规则：检查在场每名玩家的物品栏和饰品栏，带着最初分型的玩家不受限制。战斗中只收缴手持、穿戴和佩戴的违规物品。原版只检查召唤者的物品栏，战斗中会丢出物品栏里的全部违规物品。
  The item check uses the common rules of the Guardians of Gaia of this mod: it examines the inventory and the accessory slots of each player in the arena, and a player with the First Fractal has no limit. During the fight only illegal items in the hands, in the armor slots and in the accessory slots are removed. The original only examines the inventory of the summoner, and during the fight it drops all illegal items of the inventory.
- Boss 每 tick 打碎身边的方块（盖亚守护者的公共规则）。原版只在传送路径上打碎方块，并且每 tick 在 21x21x21 的范围里查找两种 1.12 模组的"作弊方块"。
  The boss breaks adjacent blocks on each tick (a common rule of the Guardians of Gaia). The original only breaks blocks on the teleport path, and on each tick it searches a 21x21x21 space for "cheat blocks" of two 1.12 mods.
- 这个模组里 Boss 的攻击只伤害玩家。所以矛枪的闪电链只伤害玩家，别的生物只得到虚弱效果。原版的闪电链伤害空之律者以外的全部生物。
  In this mod the attacks of a boss only hurt players. Thus the lightning chain of the lance only hurts players, and other creatures only get the Weakness effect. The lightning chain of the original hurts all creatures except the Herrscher.
- 矛枪的电网每一对只计算一次（0.8 点真实伤害）。原版两把矛枪各算一次（各 0.4 点）。电网的普通伤害是带主人的魔法伤害。
  The web of the lances is calculated one time for each pair (0.8 true damage). In the original each of the two lances calculates it (0.4 each). The usual damage of the web is magic damage with the owner as the attacker.
- 审判之剑旁边的光环由剑的渲染器绘制。原版的光环是另一个实体。
  The renderer of the sword of judgment draws the halo adjacent to the sword. The halo of the original is a different entity.
- 支援者帮助一名随机的玩家。原版总是帮助玩家列表里的第一名玩家。
  A supporter helps a random player. The original always helps the first player in the list of players.
- 原版每 tick 把玩家向上的速度反转。这在服务端没有效果，所以没有移植。
  On each tick the original reverses the upward speed of the players. This has no effect on the server, thus it is not ported.
- 没有移植原版的这些配置开关：是否启用空之律者、是否使用天堂裁决、是否打碎非法方块、是否允许中途加入。
  These configuration switches of the original are not ported: the switch that enables the Herrscher, the switch for the Divine Judge, the switch that breaks illegal blocks, and the switch that permits players to join during the fight.
- 死亡时除了原版的爆炸声，还会播放盖亚守护者的死亡音效（公共代码）。
  At death, the death sound of the Guardian of Gaia (common code) plays together with the explosion sound of the original.

需要在游戏里确认的项目 / Items to check in the game:

- 整场战斗的节奏。原版没有出场动画，并且 Boss 在第一 tick 就传送。
  The pace of the full fight. The original has no start animation, and the boss teleports on the first tick.
- "在场玩家比召唤时多就取消战斗"的规则。旁观的创造模式玩家也算在内。如果这条规则在多人服务器上不合适，可以再加一个配置开关。
  The rule "the fight is cancelled when there are more players than at the time of the summon". A creative mode player that only watches also counts. If this rule is not applicable on a multiplayer server, a configuration switch can be added.
- 矛枪的位置（应该正好插在地上）、闪电链的闪电和电网的光点。
  The position of the lance (it must be correctly in the ground), the lightning of the chain and the sparkles of the web.
- 审判之剑的光环、虚空力场的减速手感。
  The halo of the sword of judgment, and how the slow effect of the void field feels.
- Boss 的翅膀和护盾，黄色的血条，蓝紫色的场地光圈。
  The wings and the shields of the boss, the yellow boss bar, and the blue-purple ring of the arena.

## 第十一批：盖亚守护者III的 EGO（本我）阶段 / Batch 11: the EGO phase of Guardian of Gaia III

> 这一批把本我做成了盖亚守护者III的一个阶段。后来按维护者的决定，本我改成了单独的 Boss，见后面的"本我成为单独的 Boss"一节。这一批里关于地雷、阵型和本我仆从的内容仍然有效；关于阶段门槛、攻击间隔、回血上限和名字闪烁的内容不再适用。
> This batch made the Ego a phase of Guardian of Gaia III. Later, by the decision of the maintainer, the Ego became a separate boss; see the section "The Ego becomes a separate boss" below. The content of this batch about the landmines, the formations and the Ego Minions is still applicable; the content about the phase thresholds, the attack intervals, the regeneration limit and the name flicker is no longer applicable.
新类 / New classes: `EgoMinionEntity`、`EgoLandmineEntity`、`EgoLandmineFormations`、`EgoMinionRenderer`、`EgoLandmineRenderer`、`OriginalContributors`。`GaiaIII` 里 EGO 阶段的代码和 `GaiaEgoSwordAttack` 重写了。`LegacyArmorMeshes` 改名为 `OriginalArmorMeshes`。
The code of the EGO phase in `GaiaIII`, and `GaiaEgoSwordAttack`, were written again. `LegacyArmorMeshes` has the new name `OriginalArmorMeshes`.

删除的类 / Removed classes: `LegacyEgoMinion`、`LegacyEgoMinionRenderer`。主代码里已经没有 `Legacy` 开头的类了。
There are no classes with a name that starts with `Legacy` in the main code now.

新的注册项 / New registry entry: 实体类型 `extrabotany:ego_landmine`。别的注册 ID 和存档的键都没有变化。
The entity type `extrabotany:ego_landmine`. The other registry IDs and the save keys did not change.

背景 / Background: 在原版 1.16 里，EGO 是一个单独的 Boss，它代替了 1.12 的盖亚守护者III（召唤方式相同）。这个移植版以前就把 EGO 做成了重制版盖亚守护者III的第二个大阶段。这一批保留这个结构，只把 EGO 阶段的各个部件改成和原版 1.16 一致。
In the original 1.16 the EGO is a separate boss that replaces Guardian of Gaia III of 1.12 (the summon method is the same). Before this batch, this port already had the EGO as the second large phase of the Guardian of Gaia III of Reburn. This batch keeps that structure, and only makes the parts of the EGO phase agree with the original 1.16.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | EGO 的地雷弹幕使用盖亚阶段的骷髅地雷。那种地雷宽 5 格，红色的会造成"最大生命值 30% + 8"的真实伤害。阵型里的地雷互相重叠，所以站错位置的玩家会被几颗红色地雷同时炸死。原版 EGO 的地雷宽 3 格，它的额外伤害最少留下 1 点生命值。 / The landmine barrage of the EGO used the skull landmines of the Gaia phase. Such a landmine is 5 blocks wide, and the red one gives true damage of "30% of maximum health + 8". The landmines in a formation overlap, thus some red landmines killed a player in an incorrect position at the same time. The landmine of the original EGO is 3 blocks wide, and its additional damage leaves a minimum of 1 health. | 新的 `EgoLandmineEntity` 按原版 1.16 实现。 / The new `EgoLandmineEntity` is made from the original 1.16. |
| 2 | 八种地雷阵型只有六种。弹幕结束之后不再有地雷。原版在弹幕之后每传送七次随机排出一种阵型。 / Only six of the eight landmine formations were there. After the barrage there were no more landmines. The original puts down one random formation for each seven teleports after the barrage. | 补上第 6、7 种阵型和随机阵型的规则。 / Formations 6 and 7 and the rule for random formations were added. |
| 3 | 弹幕一开始就排出第一种阵型，无敌时间是 430 tick。原版在 40 tick 之后才排出第一种阵型，无敌时间是 460 tick。 / The barrage put down the first formation immediately, and the invulnerability time was 430 ticks. The original puts down the first formation after 40 ticks, and the invulnerability time is 460 ticks. | 按原版改正。 / Corrected to agree with the original. |
| 4 | 任何伤害都能伤到随从（怪物、岩浆、摔落）。原版只有真正的玩家能伤害随从。 / All damage could hurt a minion (monsters, lava, a fall). In the original only true players can hurt a minion. | 按原版改正。 / Corrected to agree with the original. |
| 5 | 0 号和 1 号随从的武器反了。原版 0 号用真影刃，1 号用真泰拉之刃。 / The weapons of minion 0 and minion 1 were exchanged. In the original minion 0 uses the True Shadow Katana and minion 1 uses the True Terra Blade. | 按原版改正。 / Corrected to agree with the original. |
| 6 | 随从的名字固定是四个。那是原版在贡献者名单为空时的备用名字。 / The names of the minions were always the same four. Those are the fallback names of the original for an empty list of contributors. | 从原版的贡献者名单里随机选四个。 / Four names are selected at random from the list of contributors of the original. |
| 7 | 随从手里没有武器。原版会画出它的遗物剑。 / The minions had no weapon in the hand. The original draws the relic sword. | 渲染器加上只用来显示的武器层。 / The renderer got a weapon layer that is only for display. |
| 8 | 盖亚守护者III的召唤代码直接调用了已弃用的 `finalizeSpawn`。 / The summon code of Guardian of Gaia III called the deprecated `finalizeSpawn` directly. | 改用 `EventHooks.finalizeMobSpawn`。 / It now uses `EventHooks.finalizeMobSpawn`. |

和原版的差异 / Differences from the original:

- EGO 是盖亚守护者III的一个阶段，不是单独的 Boss。所以它的门槛按 Boss 的总生命值重新划分：2/3 以下进入 EGO 阶段，1/2 以下开始地雷弹幕（原版是 EGO 自己生命值的 75%），1/6 以下召唤随从（原版是 25%）。Boss 的生命值和护甲是盖亚守护者III的数值。
  The EGO is a phase of Guardian of Gaia III and not a separate boss. Thus its thresholds are divided again along the full health of the boss: the EGO phase starts at 2/3 or less, the landmine barrage starts at 1/2 or less (the original has 75% of the health of the EGO itself), and the minions come at 1/6 or less (the original has 25%). The health and the armor of the boss are the values of Guardian of Gaia III.
- 遗物剑攻击的间隔是固定的 70、55、45 tick，每次随机选一把剑。原版的间隔是随机的，Boss 每 100 tick 换一次武器，并且在传送时和累计受到 50 点伤害时追加攻击。
  The interval of the relic sword attacks is a fixed 70, 55 or 45 ticks, and a sword is selected at random each time. The original has a random interval, the boss changes its weapon for each 100 ticks, and it adds attacks at a teleport and at a total of 50 damage.
- 随机阵型按时间触发（相当于原版"七次传送"的时间）。这个 Boss 的传送间隔和原版 EGO 不同，所以不能直接数传送次数。
  The random formation is started by time (the equivalent of the "seven teleports" of the original). The teleport interval of this boss is different from the original EGO, thus the teleports cannot be counted directly.
- 随从阶段里 Boss 最多回血到最大生命值的 2/3，这样它不会退回盖亚阶段。原版没有这个上限。
  In the minion stage the boss regenerates to a maximum of 2/3 of its maximum health, thus it does not go back to the Gaia phase. The original has no such limit.
- 原版的 EGO 用召唤者的名字和皮肤，随从用贡献者的皮肤（从网上下载）。这里 Boss 和随从都用观看者自己的皮肤，Boss 的名字每隔一段时间闪成"EGO"。
  The EGO of the original has the name and the skin of the summoner, and the minions have the skins of the contributors (from the internet). In this port the boss and the minions use the skin of the viewer, and the name of the boss flickers to "EGO" at intervals.
- 地雷阵型的中心是信标方块的中心。原版用方块的角。配置关闭缴械时，绿色地雷不让玩家丢出物品。
  The center of the landmine formations is the center of the beacon block. The original uses the corner of the block. When the configuration disables the disarm, the green landmine does not make the player drop the item.
- 原版的 EGO 也有"在场玩家比召唤时多就取消战斗"的规则。盖亚守护者III（重制版的设计）没有这条规则，这里没有加。
  The EGO of the original also has the rule "the fight is cancelled when there are more players than at the time of the summon". Guardian of Gaia III (the design of Reburn) does not have this rule, and it was not added here.

需要在游戏里确认的项目 / Items to check in the game:

- 八种地雷阵型的样子，以及玩家能不能找到安全的位置。
  The appearance of the eight landmine formations, and if a player can find a safe position.
- 随从手里的遗物剑的位置，以及随从绕着玩家走位的样子。
  The position of the relic sword in the hand of a minion, and how the minions move around the player.
- 合并之后整场战斗的难度。如果更希望和原版 1.16 一样把 EGO 做成单独的 Boss，这是一个可以再做的改动。
  The difficulty of the full fight after the two bosses are put together. If a separate EGO boss as in the original 1.16 is preferred, that change can be made later.

## 第十二批：盲审重制版遗留代码（一）——遥测、花、方块、药水效果和 mixin / Batch 12: blind audit of the Reburn code (1) – telemetry, flowers, blocks, potion effects and mixins

从这一批开始，审计的对象是从重制版（Lounode）继承来的代码，以及 SCEX 对它的修改。这些内容保留重制版的设计，只修正缺陷和性能问题。
From this batch on, the audit examines the code that came from Reburn (Lounode), and the SCEX changes to it. This content keeps the design of Reburn. Only defects and performance problems are corrected.

新类 / New classes: `HydroangeasBlockEntityAccessor`、`FlowerGameTests`、`StructureGameTests`。
删除的类 / Removed classes: `common.telemetry` 整个包（9 个类）、`api.client.PlayerNameHelper`。
The full package `common.telemetry` (9 classes), and `api.client.PlayerNameHelper`.

注册 ID 和存档的键都没有变化。风铃草多了一个键 `windLevel`。
The registry IDs and the save keys did not change. The Bellflower has one new key, `windLevel`.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 模组默认开启遥测。它把服务器的信息发到 bStats 和前任维护者的服务器 `lounode.top`，包括每一场盖亚战斗的结果。 / The mod had telemetry that was on by default. It sent data about the server to bStats and to the server `lounode.top` of the maintainer before, with the result of each Gaia fight. | 删除整个遥测包和它的两个配置项。 / The full telemetry package and its two configuration entries were removed. |
| 2 | 霜冻指环每 tick 读一千多个方块，并且会冻结流动的水。 / The Frost Ring read more than one thousand blocks on each tick, and it froze flowing water. | 只在佩戴者换了一格之后结冰，另外每 20 tick 一次。只冻结水源和岩浆源。 / The freeze only runs after the wearer moves to a different block, and one time for each 20 ticks. Only water sources and lava sources freeze. |
| 3 | 大师魔力之戒和虚空万藏的遗物进度 ID 少了 `main/` 前缀，所以这两个进度永远不会触发。 / The relic advancement IDs of the Master Band of Mana and the Void Archives did not have the `main/` prefix, thus these two advancements never occurred. | 补上前缀。 / The prefix was added. |
| 4 | 空之律者不在 `c:bosses` 标签里。 / The Herrscher of the Void was not in the tag `c:bosses`. | 加入标签。 / It was added to the tag. |
| 5 | 神烦花、伐木花、雷卡兰、回音花和商友兰每秒向所有能看到它的客户端发一个完整的方块实体数据包，即使没有任何变化。 / The Annoyingflower, the Woodienia, the Reikarlily, the Resoncund and the Trade Orchid sent a full block entity packet to all clients that could see them on each second, also when nothing changed. | 计时器只标记存档。魔力由植物魔法的基类同步。客户端需要的数据在变化时才同步。 / The timers only mark the save. The base class of Botania synchronizes the mana. The data that the client needs is only synchronized when it changes. |
| 6 | 花方块在药水效果注册之前创建，所以五种花（雪绒花、永寂龙胆、镜姬、死之花、小死之花）的迷之炖菜效果用的是代理 Holder。吃下这种炖菜的生物身上有效果的图标，但是效果的逻辑不生效，直到重新进入世界。 / The flower blocks are made before the potion effects are registered, thus the suspicious stew effect of five flowers (Edelweiss, Serenitian, Mirrowtunia, Necrofleur, Necrofleur Petite) used a proxy holder. A creature that ate such a stew had the icon of the effect, but the logic of the effect did not work until the world was entered again. | 这五种花在取炖菜效果时换成注册表里的 Holder。 / These five flowers replace the holder with that of the registry when the stew effect is read. |
| 7 | 雷卡兰、风铃草、石中姬、雪绒花、全知瑾和鲜血妖姬使用带"充能"属性的方块，但是它们不对红石做出反应。收到红石信号时它们显示"已停用"的粒子，却继续工作。 / The Reikarlily, the Bellflower, the Stonesia, the Edelweiss, the Omniviolet and the Blood Enchantress used blocks with the "powered" property, but they did not react to redstone. With a redstone signal they showed the "disabled" particles and continued to work. | 这些花改用没有"充能"属性的方块，和植物魔法的产能花一致。它们的行为没有变化。 / These flowers now use blocks without the "powered" property, the same as the generating flowers of Botania. Their behavior did not change. |
| 8 | 永寂龙胆和叮当舞花不对红石做出反应。原版（1.16 和 1.12）里红石信号能让它们停止工作。 / The Serenitian and the Tinkle did not react to redstone. In the original (1.16 and 1.12) a redstone signal stops them. | 按原版改正。 / Corrected to agree with the original. |
| 9 | 永寂龙胆每秒把范围里每一朵特殊花的全部数据写成 NBT 再读回去，只为了把一个计数器清零。 / On each second the Serenitian wrote the full data of each special flower in the range to NBT and read it back, only to set one counter to zero. | 直接清零本模组的被动花和植物魔法的水绣球。别的模组的花仍然用 NBT 的方法。 / The counter is set directly for the passive flowers of this mod and for the Hydroangeas of Botania. The flowers of other mods continue to use the NBT method. |
| 10 | 风铃草的法杖界面显示的风力是客户端自己算的。每一端的风是分别随机生成的，所以这个数值和服务端真正使用的风力不同。 / The wand HUD of the Bellflower showed a wind level that the client calculated itself. Each side makes its wind at random independently, thus this value was different from the wind level that the server used. | 风铃草保存服务端的风力并同步到客户端。 / The Bellflower keeps the wind level of the server and synchronizes it to the client. |
| 11 | 风的数据表不是线程安全的。单人游戏里客户端线程和服务端线程同时访问它。 / The data map of the wind was not thread-safe. In a single-player game the client thread and the server thread use it at the same time. | 改用 `ConcurrentHashMap`。 / It now uses `ConcurrentHashMap`. |
| 12 | 客户端的回音花也登记为声音监听者，直到下一个声音出现才注销。 / The Resoncund of the client was also recorded as a sound listener, until the subsequent sound removed it. | 只有服务端的回音花登记。 / Only the Resoncund of the server is recorded. |
| 13 | 全知瑾燃烧时每 tick 读一百个左右的方块来计算书架的加成。 / While it burned, the Omniviolet read approximately one hundred blocks on each tick to calculate the bookshelf bonus. | 每 20 tick 计算一次。 / The bonus is calculated one time for each 20 ticks. |
| 14 | 叮当舞花在运行时引用了数据生成器的类。 / The Tinkle referred to a class of the data generators at run time. | 改用字符串常量。 / It now uses a string constant. |
| 15 | 每一个祭坛每 5 秒检查两次多方块结构，每次尝试几百个起点。结构旁边的祭坛也会被算作有效。魔链星也用同样的方法检查结构。 / Each pedestal examined the multiblock structure two times for each 5 seconds, with some hundreds of origins each time. A pedestal adjacent to a structure was also accepted. The Manalink examined its structure with the same method. | 祭坛只在给自然能量物品充能时检查结构。祭坛和魔链星都只在固定的起点比对一次，所以祭坛必须在结构的中心。 / The pedestal only examines the structure while it charges a nature energy item. The pedestal and the Manalink only do one comparison at the fixed origin, thus the pedestal must be at the center of the structure. |
| 16 | 魔力充能基座和力量框架里没有魔力物品时，比较器的信号是负数。 / When the Mana Charger or the Power Frame had no mana item, the comparator signal was negative. | 限制在 0 到 15。 / The signal is limited to the range 0 to 15. |
| 17 | 攀爬饰品的 mixin 每 tick 为世界里的每一个生物查找几次饰品栏。 / The mixin for the climbing accessories searched the accessory inventory some times on each tick for each creature in the world. | 只为玩家查找。 / It now only searches for players. |
| 18 | 蕴魔土的 mixin 每 tick 为世界里的每一朵特殊花查找一次方块实体。 / The mixin for the Enchanted Soil did one block entity lookup on each tick for each special flower in the world. | 改为检查花是否已被移除。 / It now examines if the flower is removed. |
| 19 | 法杖界面的 mixin 每一帧复制手里物品的全部自定义数据。 / The mixin for the wand HUD copied all the custom data of the held items on each frame. | 物品没有本模组的数据时不复制。 / It does not copy when the item has no data of this mod. |
| 20 | 一个测试文件里留着已删除的包的通配符 import。增量编译没有发现它，但是全新的构建会失败。 / One test file kept a wildcard import of a removed package. The incremental compilation did not find it, but a clean build failed. | 删除这一行。全量编译现在没有错误，也没有警告。 / The line was removed. A full compilation now has no errors and no warnings. |
| 21 | `ForgeCommonInitializer` 里有一段检查模组 ID "tconstruc" 的代码，这个 ID 不存在，所以这段代码从不运行。花方块类里有一个没有使用的内部类。 / `ForgeCommonInitializer` had code that examined the mod ID "tconstruc". This ID does not exist, thus the code never ran. The flower block class had an inner class that was not used. | 删除。 / Removed. |

没有改动、但是值得知道的设计 / Design that did not change, but is good to know:

- 死之花会伤害生命值不超过一半的任何生物，包括玩家和宠物。原版也是这样。
  The Necrofleur hurts all creatures that have half of their health or less, with players and pets included. The original does the same.
- 鲜血妖姬伤害紧挨着它的任何生物，包括玩家。
  The Blood Enchantress hurts all creatures adjacent to it, with players included.
- 雷卡兰在下雨时大约每 3 分钟自己召来一道闪电。它不对红石做出反应，所以不能用红石关掉。
  When it rains, the Reikarlily calls a lightning bolt itself approximately one time for each 3 minutes. It does not react to redstone, thus redstone cannot stop it.
- 圣水手雷的有害效果会作用于投掷者以外的所有生物，包括别的玩家。
  The harmful effects of the Holy Water Grenade apply to all creatures other than the thrower, with other players included.
- 森林法杖在绑定模式下潜行右键魔力池，是为魔链星选择这个魔力池，不再切换魔力池的输出模式。功能模式下潜行右键仍然切换输出模式。
  In the bind mode of the Wand of the Forest, a sneak-use on a mana pool selects that pool for a Manalink, and does not change the output mode of the pool. In the function mode a sneak-use continues to change the output mode.

需要在游戏里确认的项目 / Items to check in the game:

- 用雪绒花或死之花做迷之炖菜，吃下后效果立刻生效（温暖：不受冰冻伤害；治疗反转：治疗变成伤害）。
  A suspicious stew from an Edelweiss or a Necrofleur: the effect works immediately after the stew is eaten (Warm: no freeze damage; Heal Reverse: a heal becomes damage).
- 手持森林法杖看风铃草：显示的风力和"主手法杖、副手风铃草、对着空气使用"在聊天栏显示的风力相同。
  Look at a Bellflower with a Wand of the Forest: the wind level that is shown is the same as the wind level in the chat from "wand in the main hand, Bellflower in the other hand, use at the air".
- 祭坛的一级和二级结构仍然能给自然蕴息宝珠充能。
  The tier 1 and tier 2 structures of the pedestal continue to charge the Nature Orb.
- 按键设置里的技能键（默认 R）仍然存在。它的注册方式改成了新写法。
  The skill key (R by default) is still in the key settings. Its registration now uses the new pattern.

## 第十三批：盲审重制版遗留代码（二）——盖亚战斗、物品和事件 / Batch 13: blind audit of the Reburn code (2) – Gaia fights, items and events

新类 / New classes: `BossBarGameTests`、`ItemAuditGameTests`。
新的数据文件 / New data file: `data/neoforge/data_maps/item/furnace_fuels.json`。

删除的类（没有任何代码使用它们） / Removed classes (no code used them): `BossBarHandler`、`ColorfulBossBar`、`LoootDataPacket`、`EntityNbtHelper`、`ICustomEnchantable`、`ExtrabotanyEnchantments`、`Tracking`、`EntityTypeIngredient`、`EntityIngredient`、`ManaInputRecipe`。

注册 ID 和存档的键都没有变化。数据包 `extrabotany:ldp` 被删除，它从来没有被发送过。
The registry IDs and the save keys did not change. The packet `extrabotany:ldp` was removed. It was never sent.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 光子、暗影、天空、奥利哈钢四种材料的增益本来应该每 3 秒（暗影是 5 秒）刷新一次，但是判断写反了，所以在 60 tick 里有 59 tick 都刷新效果并且消耗一次魔力。莱茵锤开启时因此每 tick 消耗 800 点魔力，天空锤每 tick 消耗 10 点。每次刷新还会给玩家发一个药水效果数据包。哥布林猎人和暗影武士套装有同样的问题。 / The buffs of the four materials (photonium, shadowium, aerialite, orichalcos) must be applied one time for each 3 seconds (5 seconds for shadowium), but the condition was inverted, thus the effect was applied and mana was used on 59 of each 60 ticks. Because of this, the Rhein Hammer used 800 mana on each tick while it was on, and the Aerialite Hammer used 10 mana on each tick. Each application also sent a potion effect packet to the player. The Goblin Slayer and Shadow Warrior armor sets had the same defect. | 改正判断。增益只在服务端处理。 / The condition was corrected. The buffs are only processed on the server. |
| 2 | 从 1.20.5 起，伤害吸收不能超过"最大伤害吸收"属性，而这个属性默认是 0。所以勇士盾攒不下伤害吸收，它的"解放"永远无法使用；精卫之羽的火焰命中后也不给伤害吸收。 / From 1.20.5, absorption cannot be more than the "max absorption" attribute, and that attribute is 0 by default. Thus the Achilles Shield could not collect absorption and its "release" could never be used; and a hit of the fire of the Feather of Jingwei gave no absorption. | 勇士盾拿在手里时提供 20 点最大伤害吸收。精卫之羽佩戴时提供 10 点。 / The Achilles Shield in a hand gives 20 max absorption. The Feather of Jingwei gives 10 when it is worn. |
| 3 | 盾牌的格挡效果（泰拉钢盾牌的反伤和生命恢复、源质钢盾牌的点燃、勇士盾的反弹）在玩家举着盾牌受到任何伤害时都会触发，包括没有被挡住的伤害（例如背后的攻击和摔落伤害）。 / The block effects of the shields (the thorns and Regeneration of the Terrasteel Shield, the ignition of the Elementium Shield, the return damage of the Achilles Shield) started with all damage that the player got while the shield was up, also damage that was not blocked (for example, an attack from behind, or fall damage). | 只在伤害真正被格挡时触发。 / They only start when the damage is really blocked. |
| 4 | Boss 传送时的粒子从来没有出现过：那段代码在服务端运行，而服务端的 `addParticle` 什么也不做。 / The particles of a boss teleport never appeared: that code ran on the server, and `addParticle` of the server does nothing. | 改为发送原版的传送实体事件（末影人用的那种）。 / It now sends the vanilla teleport entity event (the same as the Enderman). |
| 5 | Boss 出场和召唤怪物期间，血条每 tick 发一个 JSON 编码的数据包来同步无敌时间。客户端只用它来算血条的噪点强度，而强度只在最后 11 tick 变化。 / During the emerge stage and the monster stage of a boss, the boss bar sent one JSON-encoded packet on each tick to synchronize the invulnerability time. The client only uses it to calculate the grain intensity of the bar, and the intensity only changes in the last 11 ticks. | 只在强度变化时发包。新看到 Boss 的玩家立刻收到一次。 / A packet is only sent when the intensity changes. A player that starts to see the boss gets one immediately. |
| 6 | 客户端收到一条它没有的血条的更新时会抛出空指针异常。 / The client threw a NullPointerException when it got an update for a boss bar that it did not have. | 忽略这样的更新。 / Such an update is ignored. |
| 7 | 召唤盖亚守护者III时，结构检查尝试 2916 个起点和方向，并且每一个都完整比对 243 个方块。 / At the summon of Guardian of Gaia III, the structure check tried 2916 origins and directions, with a full comparison of 243 blocks for each. | 在固定的起点比对一次。提示的错误方块也因此更准确。 / One comparison at the fixed origin. The reported incorrect blocks are also more accurate because of this. |
| 8 | 地雷每 tick 在服务端创建粒子数据，而服务端并不显示粒子。同一段粒子代码复制了四份。 / On each tick the landmines made particle data on the server, and the server shows no particles. The same particle code was copied four times. | 粒子只在客户端处理，代码合并成一份。 / The particles are only processed on the client, and the code is in one place. |
| 9 | 空之律者核心有三个完全相同的伤害处理方法，同一次伤害三个都会运行，每一个都为受伤的生物（包括怪物）查找一次饰品栏。药水效果的处理方法也为每一个生物查找饰品栏。 / The Core of the Void had three identical damage handlers, all three ran for the same damage, and each one searched the accessory inventory of the hurt creature (monsters included). The potion effect handler also searched the accessory inventory of each creature. | 合并成一个方法，并且先做便宜的判断。 / They are one method now, and the cheap checks come first. |
| 10 | 穿在身上或者放在物品栏里的每一件重制版盔甲每 tick 都检查一次套装进度，每次检查都要创建一个战利品上下文。 / Each Reburn armor piece that was worn or in the inventory examined the set advancement on each tick, and each check made a loot context. | 只由戴在头上的那一件每秒检查一次。 / Only the piece on the head examines it, one time on each second. |
| 11 | 白雏菊项链右键一次会转化两次（游戏为两只手各触发一次事件），例如冰直接变成蓝冰，并且消耗两份魔力。 / One use with the Pure Daisy Pendant converted two times (the game starts the event one time for each hand); for example, ice became blue ice directly, and two units of mana were used. | 只处理主手的事件，并且在转化后取消这次点击。 / Only the event of the main hand is processed, and the click is cancelled after the conversion. |
| 12 | 百中弓的脉冲每命中一个生物，就把整个脉冲实体存成 NBT，只为了读出伤害值。 / For each creature that a burst of the Failnaught hit, the full burst entity was saved to NBT, only to read the damage value. | 直接读取字段。 / The field is read directly. |
| 13 | 自然蕴息宝珠、昴宿星团战斗女仆服和泰拉碾压者在客户端也执行魔力、治疗和药水效果的逻辑，使两边的数据暂时不一致。 / The Nature Orb, the Pleiades Combat Maid Suit and the Terra Smasher also ran the mana, healing and potion effect logic on the client, which made the data of the two sides different for a time. | 这些逻辑只在服务端运行。 / This logic only runs on the server. |
| 14 | 战斗女仆服用 `== ItemStack.EMPTY` 判断空手。别的模组放进手里的空物品堆不是同一个对象。 / The maid suit used `== ItemStack.EMPTY` to find an empty hand. An empty stack that a different mod puts in the hand is not the same object. | 改用 `isEmpty()`。 / It now uses `isEmpty()`. |
| 15 | 梦魇燃料和精神燃料的燃烧时间由事件处理器提供。 / An event handler supplied the burn times of the Nightmare Fuel and the Spirit Fuel. | 改用 NeoForge 的数据映射 `neoforge:furnace_fuels`，整合包可以用数据包修改它。 / They now use the NeoForge data map `neoforge:furnace_fuels`, and a modpack can change it with a data pack. |
| 16 | 魔力透镜：魔力的产物生成位置偏了半格。魔力透镜：追踪和魔力透镜：冲击在假脉冲（只用来计算路径）上也查找生物。 / The product of the Mana Lens: Mana appeared half a block away. The Trace Lens and the Push Lens also searched for creatures with a fake burst (which only calculates the path). | 改正位置。假脉冲不查找生物。 / The position was corrected. A fake burst does not search for creatures. |
| 17 | 魔术手指在没有玩家的情况下被使用时会崩溃。破坏方块的行为每 tick 为每个空气方块查一次注册表。盖亚的伤害上限有一处用了常量而不是子类的数值。 / The Magic Finger caused a crash when it was used without a player. The block smash behavior did a registry lookup for each air block on each tick. One place of the Gaia damage cap used the constant and not the value of the subclass. | 改正。 / Corrected. |

没有改动、但是值得知道的设计 / Design that did not change, but is good to know:

- 虚空万藏保持非默认形态时每 tick 消耗 500 点魔力（每秒 10000 点）。重制版就是这样设计的。
  The Void Archives uses 500 mana on each tick (10000 on each second) while it has a form that is not the default form. This is the design of Reburn.
- 泰拉碾压者、奥利哈钢锤和莱茵锤在开启范围挖掘时每 tick 消耗魔力，不管有没有在挖。莱茵锤没有开启时不能挖掘。
  The Terra Smasher, the Orichalcos Hammer and the Rhein Hammer use mana on each tick while the area mining is on, also when they do not mine. The Rhein Hammer cannot mine when it is not on.
- 盖亚魂钢锤的"下落重击"只把玩家弹起，没有范围击退。重制版留下了没有完成的代码。
  The "fall smash" of the Gaia Hammer only makes the player bounce, and it has no area knockback. Reburn left code that was not completed.
- 奖励袋内容的客户端显示（`RewardBagManager`）在重制版里没有完成，这张表总是空的。
  The client display of the reward bag contents (`RewardBagManager`) was not completed in Reburn, and the map is always empty.
- 魔法飞弹的初始位置有一个运算符优先级的小毛病（`Math.random() - 0.5 * 0.1`）。植物魔法的盖亚守护者有完全相同的代码，所以这里保持一致。
  The start position of the magic missiles has a small operator precedence defect (`Math.random() - 0.5 * 0.1`). The Guardian of Gaia of Botania has exactly the same code, thus it stays the same here.

需要在游戏里确认的项目 / Items to check in the game:

- 勇士盾：用它攻击能攒下伤害吸收（黄色的心），攒到 10 点后潜行并使用可以解放。
  Achilles Shield: attacks with it collect absorption (yellow hearts), and with 10 or more a sneak-use starts the release.
- 精卫之羽：火焰命中后玩家得到伤害吸收。
  Feather of Jingwei: the player gets absorption after a hit of the fire.
- 盖亚守护者传送时，出发的位置有紫色的传送粒子。
  When a Guardian of Gaia teleports, there are purple teleport particles at the start position.
- 盖亚血条在 Boss 出场结束时噪点正常消失。
  The grain of the Gaia boss bar goes away correctly at the end of the emerge stage.
- 莱茵锤开启后魔力的消耗速度明显变慢，四种增益仍然一直存在。
  After the Rhein Hammer is set on, the mana use is clearly slower, and the four buffs are still there continuously.
- 梦魇燃料和精神燃料仍然能在熔炉里燃烧。
  The Nightmare Fuel and the Spirit Fuel can still burn in a furnace.

## 第十四批：盲审重制版遗留代码（三）——平台层和客户端 / Batch 14: blind audit of the Reburn code (3) – the platform layer and the client

删除的类（没有任何代码使用它们） / Removed classes (no code used them): `LootDataPoolClient`、`CopyBrewFormFlaskRecipeWrapper`。

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 模组信息里对似水流年的致谢显示成乱码。`gradle.properties` 里直接写了中文，而 Gradle 按 ISO-8859-1 读取这个文件。 / The tribute to 似水流年 in the mod information was shown as garbled text. `gradle.properties` had the Chinese characters directly, and Gradle reads this file as ISO-8859-1. | 改成 `\uXXXX` 转义。生成的 `neoforge.mods.toml` 已经确认是正确的文字。 / The characters are now `\uXXXX` escapes. The generated `neoforge.mods.toml` was examined and has the correct text. |
| 2 | 物品属性在每次模型重载时、在工作线程上登记。原版保存物品属性的表不是线程安全的。 / The item properties were recorded at each model reload, on a worker thread. The vanilla map of the item properties is not thread-safe. | 在客户端初始化时、在主线程上登记一次。 / They are recorded one time on the main thread at the client setup. |
| 3 | 魔力条每一帧都查找玩家身上所有的魔力物品，即使玩家身上没有使用魔力的物品、魔力条不会显示。累加魔力时每一帧为每个物品创建 `BigDecimal`。 / On each frame the mana bar searched all mana items of the player, also when the player had no item that uses mana and the bar was not shown. The sum of the mana made `BigDecimal` objects for each item on each frame. | 魔力条不显示时不查找。累加改用 `long`。 / There is no search when the bar is not shown. The sum uses a `long`. |
| 4 | 盾牌的"格挡中"模型在玩家使用任何物品时都会显示，例如副手拿着盾牌吃东西。 / The "blocking" model of a shield was shown when the player used any item, for example when the player ate with the shield in the other hand. | 只在玩家正在使用这面盾牌时显示。 / It is only shown when the player uses that shield. |
| 5 | 糖果类物品每一帧渲染时都读取一次系统日期，用来判断是不是圣诞节。 / Each render of a candy item on each frame read the system date, to find if it is Christmas. | 结果缓存一分钟。 / The result is kept for one minute. |
| 6 | EMI 里祭坛配方的工作台用的是方块标签。 / The workstation of the pedestal recipes in EMI used a block tag. | 改用物品标签。 / It now uses the item tag. |
| 7 | 两个 mixin 配置里有 `refmap` 项。NeoForge 1.21 不生成也不需要 refmap，这一项只会在日志里产生警告。（已在第十三批的提交里删除。） / The two mixin configurations had a `refmap` entry. NeoForge 1.21 makes no refmap and does not need one, and the entry only caused a warning in the log. (It was removed in the commit of batch 13.) | 删除。 / Removed. |

没有改动、但是值得知道的事项 / Items that did not change, but are good to know:

- 花和花盆的镂空渲染仍然用 `ItemBlockRenderTypes.setRenderLayer` 登记。NeoForge 推荐在模型 JSON 里写 `render_type`。这一项留到修复数据生成器时处理。
  The cutout render type of the flowers and the flower pots is still recorded with `ItemBlockRenderTypes.setRenderLayer`. NeoForge recommends `render_type` in the model JSON. This item will be done together with the repair of the data generators.
- EMI 里没有石中姬、雪绒花和全知瑾的配方显示。JEI 里有石中姬的。
  EMI does not show the recipes of the Stonesia, the Edelweiss and the Omniviolet. JEI shows those of the Stonesia.

需要在游戏里确认的项目 / Items to check in the game:

- 模组列表里本模组的"鸣谢"一栏显示"似水流年"。
  In the mod list, the "credits" field of this mod shows "似水流年".
- 百中弓拉弓时的贴图变化、锤子开启时的贴图、酿造瓶的剩余次数贴图都正常。
  The texture change of the Failnaught during the draw, the texture of a hammer that is on, and the textures for the uses that are left in a brew bottle are all correct.
- 拿着使用魔力的物品时，物品栏上方的魔力条正常显示；戴着大师魔力之戒时魔力条不会变成空的或者溢出。
  With an item that uses mana in the inventory, the mana bar above the hotbar is shown correctly; with the Master Band of Mana, the bar does not become empty or overflow.

## 结构整理：单模块布局和客户端代码分离 / Structure: one-module layout and separation of the client code

这一节不改游戏内容。它只改变文件的位置和类之间的引用关系。
This section does not change game content. It only changes the location of files and the references between classes.

### 布局 / Layout

SCEX 的移植保留了重制版的三个目录（`Xplat`、`Forge`、`Fabric`），但是只构建 NeoForge。现在项目是一个标准的 Gradle 模块。
The SCEX port kept the three directories of Reburn (`Xplat`, `Forge`, `Fabric`), but it only built NeoForge. The project is now one standard Gradle module.

| 目录 / Directory | 内容 / Content |
|---|---|
| `src/main/java`、`src/main/resources` | 模组本身。发布的 JAR 只有这里的内容。 / The mod. The release JAR only has this content. |
| `src/main/templates` | `neoforge.mods.toml` 和 `pack.mcmeta` 的模板。构建时填入 `gradle.properties` 里的值。 / The templates of `neoforge.mods.toml` and `pack.mcmeta`. The build puts in the values from `gradle.properties`. |
| `src/generated/resources` | 数据生成器的输出（`gradlew runData`）。 / The output of the data generators (`gradlew runData`). |
| `src/datagen/java` | 数据生成器的代码。这是单独的源码集，不进入发布的 JAR。（在"数据生成和数据包"一节里加入。） / The code of the data generators. This is a separate source set, and it does not go into the release JAR. (Added in the section "Data generation and the data pack".) |
| `src/gametest/java`、`src/gametest/resources` | 游戏内测试和它们的结构文件。这是单独的源码集，不进入发布的 JAR。 / The game tests and their structure files. This is a separate source set, and it does not go into the release JAR. |
| `docs` | 本记录和注释规范。`docs/scex` 保存 SCEX 移植时期的记录、脚本和来源文件。 / This record and the comment style. `docs/scex` keeps the records, the scripts and the provenance files from the time of the SCEX port. |

- 删除 / Removed：`Fabric` 目录（没有构建脚本使用它）、`Xplat` 和 `Forge` 下 1.20 格式的旧生成资源、过期的 IDE 注解目录、`assets/botania/patchouli_books/lexicon` 下手工放的图鉴副本（植物魔法的图鉴 ID 是 `lexica_botania`，这些文件从来不会被读取）。
  The `Fabric` directory (no build script used it), the old generated resources in the 1.20 format below `Xplat` and `Forge`, the stale IDE annotation directory, and the hand-made lexicon copies below `assets/botania/patchouli_books/lexicon` (the lexicon ID of Botania is `lexica_botania`, and these files were never read).
- 开发运行分成两组：客户端、服务端和数据生成只加载模组；测试服务端和测试客户端加载模组和测试。
  The dev runs are in two groups: the client, the server and the data run only load the mod; the test server and the test client load the mod and the tests.
- mixin 配置只在 `neoforge.mods.toml` 里声明。JAR 清单里重复的 `MixinConfigs` 已删除。
  The mixin configurations are declared in `neoforge.mods.toml` only. The duplicate `MixinConfigs` entry of the JAR manifest is removed.

### 客户端代码分离 / Separation of the client code

规则 / Rule：`client`、`forge.client` 和 `mixin.client` 以外的包不引用客户端的类（`net.minecraft.client`、`com.mojang.blaze3d`、本模组的 `client` 包）。专用服务器上没有这些类。以前这些引用藏在公共类的嵌套类和方法体里，只是因为 JVM 延迟加载才没有出错。
Packages other than `client`, `forge.client` and `mixin.client` do not refer to client classes (`net.minecraft.client`, `com.mojang.blaze3d`, the `client` package of this mod). A dedicated server does not have these classes. Before, these references were in nested classes and method bodies of common classes, and there was no error only because the JVM loads classes late.

有两个例外，它们是公共代码进入客户端代码的入口：`Proxy`（只在客户端创建 `ClientProxy`）和 `ForgePacketHandler`（只在 `Supplier` 里引用客户端的处理方法，数据包到达客户端时才取出）。植物魔法的粒子数据类和 `TooltipHandler` 不算客户端的类，植物魔法自己的公共代码也使用它们。
There are two exceptions, and they are the entry points from common code into client code: `Proxy` (it makes the `ClientProxy` on the client only) and `ForgePacketHandler` (it refers to the client handlers only in a `Supplier`, which is read when a packet arrives on the client). The particle data classes and `TooltipHandler` of Botania are not client classes for this rule, because the common code of Botania also uses them.

| 之前 / Before | 之后 / After |
|---|---|
| 神烦花、蕴魔瑾、魔链星、风铃草的方块实体里嵌套的法杖界面类；`ExtrabotanyFlowerBlocks` 和 `ExtraBotanyBlockEntities` 里的 `registerWandHudCaps` / The wand display classes that were nested in the block entities of the Annoyingflower, the Enchanter, the Manalink and the Bellflower; `registerWandHudCaps` in `ExtrabotanyFlowerBlocks` and `ExtraBotanyBlockEntities` | `client.gui` 里的 `AnnoyingFlowerWandHud`、`EnchanterWandHud`、`ManalinkWandHud`、`BellflowerWandHud`，登记表是 `WandHuds` / The four display classes in `client.gui`, and the table `WandHuds` |
| `Gaia` 里嵌套的音乐类 / The music class that was nested in `Gaia` | `client.sound.GaiaMusic`。客户端处理生成数据包时开始播放。 / `client.sound.GaiaMusic`. The client starts it when it handles the spawn packet. |
| `CameraItem.Hud` | `client.gui.CameraOverlay` |
| `JackieChanRingItem` 里的 `instanceof LocalPlayer` / `instanceof LocalPlayer` in `JackieChanRingItem` | `Proxy#tryWallJump`，实现在 `ClientProxy` / `Proxy#tryWallJump`, with the implementation in `ClientProxy` |
| `CoreOfTheVoidVariant` 的渲染方法、四个外观类、`CoreOfTheVoidItem.Renderer`、`ExtraBotanyModels` / The render methods of `CoreOfTheVoidVariant`, the four appearance classes, `CoreOfTheVoidItem.Renderer`, `ExtraBotanyModels` | `client.renderer.wing`：`CoreOfTheVoidRenderer`、`WingRenderer`、`HerrscherWings`、`PairedWings` |
| 客户端数据包里嵌套的 `Handler` 类；`ForgePacketHandler` 里直接使用 `HUD` 的 lambda / The `Handler` classes that were nested in the client-bound packets; the lambda in `ForgePacketHandler` that used `HUD` directly | `client.network.ClientPacketHandlers` |
| `client.lib.ResourcesLib`（盔甲物品在公共代码里使用它） / `client.lib.ResourcesLib` (the armor items use it in common code) | `common.lib.ResourcesLib` |

随之发生的小改动 / Small changes that came with it:

| # | 改动 / Change | 原因 / Reason |
|---|---|---|
| 1 | 翅膀模型在渲染时从模型管理器里取，和本模组别的独立模型相同。`ExtraBotanyModels` 删除。 / The wing models are read from the model manager at render time, as the other standalone models of this mod are. `ExtraBotanyModels` is removed. | 旧的做法把烘焙后的模型存进字段。这些字段在工作线程上写入、在渲染线程上读取，没有同步。 / The old method stored the baked models in fields. A worker thread wrote these fields and the render thread read them, with no synchronization. |
| 2 | 吉米、蒸汽朋克和芙兰朵露的翅膀合成一个类 `PairedWings`，用参数区分。 / The wings of Jim, Steampunk and Flandre are one class, `PairedWings`, with parameters. | 三个类的代码只有模型、高度和扇动幅度不同。 / The code of the three classes was different only in the model, the height and the flap angle. |
| 3 | API：`CoreOfTheVoidVariant` 只剩 `getId()`。附属模组用 `CoreOfTheVoidRenderer#registerWings` 登记翅膀的画法。 / API: `CoreOfTheVoidVariant` only has `getId()`. An add-on records the method to draw the wings with `CoreOfTheVoidRenderer#registerWings`. | API 接口不能有客户端类型的参数。 / An API interface must not have parameters of client types. |
| 4 | `Gaia#syncDataFormServer` 改名为 `syncDataFromServer`，并返回"数据包里有没有场地"。 / `Gaia#syncDataFormServer` has the new name `syncDataFromServer`, and it returns if the packet has an arena. | 改正拼写。客户端的处理方法用返回值决定是否播放音乐。 / The spelling is corrected. The client handler uses the result to decide if the music starts. |
| 5 | 删除没有使用的代码：`CameraItem` 里画调试粒子的方法、`CoreOfTheVoidItem` 里被注释掉的 `FlyManager`、`ResourceLocationHelper#modelResourceLocation`、三个只有 `package-info` 的空包。 / Unused code is removed: the debug particle methods in `CameraItem`, the `FlyManager` in `CoreOfTheVoidItem` that was a comment, `ResourceLocationHelper#modelResourceLocation`, and three packages that only had a `package-info`. | — |

没有改动、但是值得知道的事项 / Items that did not change, but are good to know:

- 资源里有七个"欧证"翅膀的模型（`models/wing/rainbow_1` 到 `rainbow_7`）和它的翻译键，但是重制版里没有使用它们的代码。这是没有完成的内容，保持原样。
  The resources have seven models of the "Rainbow" wings (`models/wing/rainbow_1` to `rainbow_7`) and the translation key for them, but Reburn has no code that uses them. This is content that was not completed, and it stays as it is.
- 平台层的包名仍然是 `forge`。改包名要同时改 mixin 配置和入口声明，对玩家没有任何好处，所以没有改。
  The package of the platform layer is still named `forge`. A rename needs changes in the mixin configuration and in the entry point declarations, and it gives nothing to players, thus it was not done.

需要在游戏里确认的项目 / Items to check in the game:

- 空之律者核心的四种翅膀都正常显示并扇动，律者的翅膀在暗处也是亮的。
  The four types of wings of the Core of the Void show and move correctly, and the wings of the Herrscher are also bright in the dark.
- 拿着森林法杖看神烦花、蕴魔瑾、魔链星、风铃草、星尘莲和四种魔力机器时，界面正常显示。
  With the Wand of the Forest in the hand, the displays of the Annoyingflower, the Enchanter, the Manalink, the Bellflower, the Stardust Lotus and the four mana machines show correctly.
- 盖亚守护者出现时播放战斗音乐，守护者死亡后音乐停止。
  The fight music plays when a Guardian of Gaia appears, and the music stops after the guardian dies.
- 戴着成龙指环贴墙按跳跃键可以蹬墙跳。举起文文的相机时取景框正常显示。
  With the Jackie Chan Ring, a press of the jump key against a wall gives a wall jump. The frame shows correctly when the Camera is held up.

## KubeJS、配方分类和译名 / KubeJS, recipe categories and names

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 五个合成配方的 `category` 写的是 `combat`：亚空之矛、银翼射手、犹大的誓约和它的两个变体。合成配方只有 `building`、`redstone`、`equipment`、`misc` 四种分类。游戏不报错，直接当作 `misc`；KubeJS 每次加载都给出警告。 / Five crafting recipes had `combat` as `category`: the Spear of Subspace, the Silver Bullet, the Judah Oath and its two variants. A crafting recipe only has the four categories `building`, `redstone`, `equipment` and `misc`. The game gives no error and uses `misc`; KubeJS gave a warning at each load. | 改成 `equipment`。 / Changed to `equipment`. |
| 2 | 祭坛的方块实体里有一个空的 `HUD` 类，旁边只有一条"待办"。 / The block entity of the pedestal had an empty `HUD` class, with only a "to do" line. | 删除。 / Removed. |
| 3 | 第一批到第十一批的注释和本记录里，一部分中文名是我自己翻译的，和游戏里的译名不同（例如"寂静永恒""万物归一之戒""魔力液化器""影武者太刀""雷斩"）。一部分英文名也和游戏里的不同（例如 "Gaia Guardian III"）。 / In the comments and in this record from batch 1 to batch 11, some Chinese names were my own translations and not the names in the game. Some English names were also different from the names in the game (for example "Gaia Guardian III"). | 全部改成 `zh_cn.json` 和 `en_us.json` 里的名字（静籁永恒、全能指环、液态魔力转化器、影刃、心斩；Guardian of Gaia III）。这只改注释和文档，不改代码。 / All are changed to the names in `zh_cn.json` and `en_us.json`. This only changes comments and documents, not code. |

检查过、没有问题的项目 / Items that were examined and are correct:

- KubeJS 插件的四个配方模式和配方的编解码器一致。在 KubeJS 2101.7.2 上用脚本实际添加了八条配方（每种类型都有），全部成功。脚本和用法在 `docs/kubejs.md`。KubeJS 只是编译期依赖，开发环境平时不加载它；这次测试临时把它加进运行环境，测试之后已经撤销。
  The four recipe schemas of the KubeJS plugin agree with the recipe codecs. On KubeJS 2101.7.2 a script added eight recipes (each type was included), and all were added. The script and the use are in `docs/kubejs.md`. KubeJS is only a compile-time dependency, and the development environment does not usually load it; for this test it was put into the runtime temporarily, and that change was reverted after the test.

注释里保留的几个简称 / Short names that the comments keep:

- "薪炎之钥"：焢煌之境：劫炎永燎（Key of Flamescion）。
- "矛枪"：空之律者插在场地上的亚空之矛实体（Subspace Lance），用来和同名的物品区分。
- "审判之剑"：天堂裁决攻击召唤的剑（实体名也是"天堂裁决"，Sword Domain）。
- "EGO"：盖亚守护者III的第二个大阶段。游戏里实体的名字是"本我"（Ego）。

## 数据生成和数据包 / Data generation and the data pack

开始时的状态 / The state at the start：`gradlew runData` 在第三个生成器就崩溃，所以生成的资源已经很久不能重新生成。从原版恢复的内容是直接手写进生成目录的，或者手写在 `src/main/resources` 里。同一个文件有时在两个目录里各有一份。
`gradlew runData` stopped with an error at the third generator, so the generated resources could not be made again for a long time. The content that was restored from the original was typed directly into the generated directory, or written by hand in `src/main/resources`. Sometimes one file had a copy in each of the two directories.

现在的规则 / The rules now:

- `src/generated/resources` 完全属于生成器。`gradlew runData` 重新生成它的全部内容，连续运行两次的结果完全相同。不要在这个目录里手写文件：下一次生成会删除它。
  `src/generated/resources` belongs to the generators completely. `gradlew runData` makes all of its content again, and two runs one after the other give exactly the same result. Do not write a file by hand in this directory: the next run deletes it.
- 手写的文件放在 `src/main/resources`。同一个路径如果有手写的文件，生成器就不写（`HandWrittenFilter`），日志里会列出被跳过的文件。所以两个目录里没有重复的路径。
  Hand-written files go into `src/main/resources`. When a path has a hand-written file, the generator does not write that path (`HandWrittenFilter`), and the log lists the files that were skipped. Thus no path is in the two directories.
- 生成器的代码在单独的源码集 `src/datagen` 里，不进入发布的 JAR（少了 77 个类）。
  The code of the generators is in a separate source set, `src/datagen`, and does not go into the release JAR (77 classes less).

对比方法 / How the result was compared：修复前把生成目录复制了一份作为基线，修复后把新的输出和基线逐个文件按 JSON 的含义比较（键的顺序不算差异，标签里条目的顺序不算差异）。基线有 1033 个文件：1017 个含义相同；14 个有差异，全部列在下面；2 个是测试用的结构文件，已经移走。新的输出另外多出 96 个文件，它们以前是 `src/main/resources` 里的手写文件，现在由生成器生成（其中一个是新的 EGO 唱片的模型）。
Before the repair a copy of the generated directory was kept as the baseline. After the repair each file of the new output was compared with the baseline by the meaning of the JSON (the order of keys is not a difference, and the order of the entries in a tag is not a difference). The baseline has 1033 files: 1017 have the same meaning; 14 are different, and all are in the list below; 2 are the structure files of the tests, which were moved. The new output has 96 more files. Before, they were hand-written files in `src/main/resources`, and the generators make them now (one of them is the model of the new EGO record).

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 方块战利品表的生成器把"没有战利品表"的方块（液态魔力）也算进去，基类拒绝并崩溃。 / The generator for block loot tables also included the block with no loot table (Fluided Mana); the base class refused it and stopped with an error. | 跳过没有战利品表的方块。 / Blocks with no loot table are skipped. |
| 2 | 空之律者和本我仆从没有生成的战利品表，生成器因此崩溃。它们的表是手写的文件。 / The Herrscher of the Void and the Ego Minion had no generated loot table, and the generator stopped with an error. Their tables were hand-written files. | 写进生成器，内容和手写的文件相同。 / They are now in the generator, with the same content as the hand-written files. |
| 3 | `music.ego` 这个声音事件没有任何声音，生成器因此崩溃。SCEX 留下了音频文件 `swordland.ogg`（1.7 MB），但是没有任何东西播放它。原版 1.16 的 EGO 战斗播放这首音乐。 / The sound event `music.ego` had no sound, and the generator stopped with an error. SCEX kept the audio file `swordland.ogg` (1.7 MB), but nothing played it. In the original 1.16 the EGO fight plays this music. | 盖亚守护者III进入 EGO 阶段时换成这首音乐。客户端的音乐类在守护者的音乐变化时停止旧的、开始新的。 / When Guardian of Gaia III goes into the EGO phase, the music changes to this track. The music class of the client stops the old track and starts the new one when the music of the guardian changes. |
| 4 | 原版有 EGO 音乐的唱片（`recordego`），移植版没有。 / The original has a record for the EGO music (`recordego`); the port did not have it. | 新物品 `music_disc_ego`，贴图是原版的唱片贴图。它和另外两张唱片在同一个掉落池里（盖亚守护者III，20% 的几率掉落三张之一）。 / A new item, `music_disc_ego`, with the record texture of the original. It is in the same drop pool as the other two records (Guardian of Gaia III, a 20% chance for one of the three). |
| 5 | 律者唱片的曲目长度写的是 201 秒，那是盖亚唱片的长度。实际的音频是 47.5 秒，所以唱片机放完之后还要"播放"两分半钟的静音。一条 SCEX 的测试把 201 这个错误的数值固定了下来。 / The song length of the Herrscher record was 201 seconds, which is the length of the Gaia record. The audio is 47.5 seconds, so the jukebox "played" silence for two and a half minutes after the end. A SCEX test had fixed the incorrect value 201. | 改成 48 秒，并改正那条测试。 / Changed to 48 seconds, and the test is corrected. |
| 6 | 图鉴的生成器把条目写到 `botania/patchouli_books/lexicon`。植物魔法的图鉴是 `lexica_botania`，所以这 63 个生成的文件从来没有被读取；真正生效的是 `src/main/resources` 里手工复制的一份。 / The lexicon generator wrote the entries to `botania/patchouli_books/lexicon`. The lexicon of Botania is `lexica_botania`, so these 63 generated files were never read; the files that the game used were a hand-made copy in `src/main/resources`. | 生成器改为写到 `lexica_botania`，删除手工复制的 63 个文件。28 个从原版恢复的条目仍然是手写的文件。 / The generator now writes to `lexica_botania`, and the 63 hand-made copies are removed. The 28 entries that were restored from the original stay as hand-written files. |
| 7 | 空之律者核心的图鉴条目在生成器里被一个"不可能完成"的进度锁住（重制版的 Boss 当时没有做完）。手工复制的那一份已经改回正常的进度。 / In the generator, the lexicon entry of the Core of The Void was locked behind an advancement that cannot be completed (the boss of Reburn was not complete then). The hand-made copy already had the usual advancement. | 生成器改成和手工的那一份相同。 / The generator now agrees with the hand-made copy. |
| 8 | 进度"彼岸双生"的说明是"合成出光子锭与暗影锭"，但是两个条件都在检查暗影锭。 / The description of the advancement "Stygian Twins" is "Craft a Photonium Ingot and a Shadowium Ingot", but the two conditions both looked for the Shadowium Ingot. | 第二个条件改为检查光子锭。条件的名字没有变，所以已经完成的玩家不受影响。 / The second condition now looks for the Photonium Ingot. The names of the conditions did not change, thus players who completed it are not affected. |
| 9 | 标签：从原版恢复的物品和方块（约 90 个条目）是手写进生成文件的，生成器不知道它们。 / Tags: the items and blocks that were restored from the original (about 90 entries) were typed into the generated files, and the generators did not know them. | 加进 `ExtraBotanyItems` 的物品分组和标签生成器。 / They are added to the item groups in `ExtraBotanyItems` and to the tag generators. |
| 10 | 有两个"Forge 专用"的标签生成器。标签生成器只能引用它自己定义的标签，所以通用标签（`c:ingots/aerialite`）不能引用本模组的标签（`extrabotany:aerialite_ingots`），这个引用是手写进去的。 / There were two "Forge-specific" tag generators. A tag generator can only refer to tags that it defines itself, so a common tag (`c:ingots/aerialite`) could not refer to the tag of this mod (`extrabotany:aerialite_ingots`), and that reference was typed in by hand. | 两个生成器并入普通的方块标签和物品标签生成器。 / The two generators are merged into the usual block tag and item tag generators. |
| 11 | 声音生成器使用的是 NeoForge 的 `SoundDefinitionsProvider` 的两个复制品（670 行）。复制品不检查声音文件是否存在。 / The sound generator used two copies (670 lines) of `SoundDefinitionsProvider` of NeoForge. The copies did not examine if the sound files exist. | 改用 NeoForge 的类，删除复制品。现在缺少声音文件时生成失败。 / It now uses the class of NeoForge, and the copies are removed. The generation now fails when a sound file is missing. |
| 12 | 四个声音文件放在 `sounds/` 的根目录，名字和声音事件无关（`cyclone.ogg` 等）；`flamescionult.ogg` 和 `item/flamescion_weapon/ult.ogg` 是同一段音频，前者没有被使用。 / Four sound files were in the root of `sounds/`, with names that had no relation to the sound event (`cyclone.ogg` and others); `flamescionult.ogg` and `item/flamescion_weapon/ult.ogg` were the same audio, and the first was not used. | 文件移到和声音事件同名的路径，删除重复的那个。三首音乐的文件名改成 `gaia3`、`ego`、`herrscher`。 / The files are moved to the path with the name of the sound event, and the duplicate is removed. The files of the three music tracks have the names `gaia3`, `ego` and `herrscher`. |
| 13 | 两个测试用的结构文件在生成目录里，所以它们也在发布的 JAR 里。 / Two structure files of the tests were in the generated directory, so they were also in the release JAR. | 移到 `src/gametest/resources`。 / Moved to `src/gametest/resources`. |
| 14 | 没有被任何代码或数据引用的标签文件：`botania:mystical_flowers`、`botania:double_mystical_flowers`、`botania:shiny_flowers`（旧版植物魔法的名字，这里是空文件）、`extrabotany:mana_pools`、`extrabotany:mana_spreaders`、`extrabotany:any`、`minecraft:music_discs`（1.21 已经没有这个标签）。液态魔力方块的战利品表文件（流体方块没有战利品表）。 / Tag files that no code and no data referred to: `botania:mystical_flowers`, `botania:double_mystical_flowers`, `botania:shiny_flowers` (names of old Botania versions; empty files here), `extrabotany:mana_pools`, `extrabotany:mana_spreaders`, `extrabotany:any`, `minecraft:music_discs` (this tag does not exist in 1.21). The loot table file of the Fluided Mana block (a fluid block has no loot table). | 删除。 / Removed. |

新输出和基线的 14 处差异 / The 14 differences between the new output and the baseline:

- 上表第 4 项（EGO 唱片）：`c:music_discs` 标签、盖亚守护者III的战利品表和图鉴的"盖亚专辑"条目多了这张唱片。
  Item 4 of the table (the EGO record): the tag `c:music_discs`, the loot table of Guardian of Gaia III and the lexicon entry "Gaia Album" have this record now.
- 上表第 7 项：空之律者核心的图鉴条目。第 8 项：进度"彼岸双生"。
  Item 7 of the table: the lexicon entry of the Core of The Void. Item 8: the advancement "Stygian Twins".
- `sounds.json`：EGO 音乐有了声音，文件路径按上面第 12 项改了名。
  `sounds.json`: the EGO music has a sound, and the file paths have the new names of item 12 above.
- 活石桶的配方：配方书分类从 `building` 变成 `misc`（生成器对"装饰"类物品给出的分类），进度文件里两个条件的顺序不同。
  The recipe of the Livingrock Barrel: the recipe book category changes from `building` to `misc` (the category that the generator gives for "decorations"), and the two conditions in the advancement file have a different order.
- 三个伤害类型标签：同一个条目以前出现两次，一次是普通条目，一次是"可选"条目。现在只有普通条目。
  Three damage type tags: before, the same entry occurred two times, one time as a usual entry and one time as an "optional" entry. Now there is only the usual entry.
- `minecraft:small_flowers` 和 `minecraft:tall_flowers`：去掉了对上面第 14 项里两个空标签的引用。
  `minecraft:small_flowers` and `minecraft:tall_flowers`: the references to the two empty tags of item 14 above are removed.
- `extrabotany:bellflower_ignore`：去掉了对 `extrabotany:mana_pools` 和 `extrabotany:mana_spreaders` 的引用。同样的方块仍然通过 `botania:mana_pools` 和 `botania:mana_spreaders` 在这个标签里。
  `extrabotany:bellflower_ignore`: the references to `extrabotany:mana_pools` and `extrabotany:mana_spreaders` are removed. The same blocks are still in this tag through `botania:mana_pools` and `botania:mana_spreaders`.

没有改动、但是值得知道的事项 / Items that did not change, but are good to know:

- 花和花盆的镂空渲染仍然用 `ItemBlockRenderTypes.setRenderLayer` 登记。植物魔法 456 自己在 1.21.1 上也是这样做的，而且这里的模型生成器继承自植物魔法的生成器，不能在模型里写 `render_type`。这一项不再列为待办。
  The cutout render type of the flowers and the flower pots is still recorded with `ItemBlockRenderTypes.setRenderLayer`. Botania 456 itself does the same on 1.21.1, and the model generators here extend the generators of Botania, which cannot write `render_type` into a model. This item is not an open item any more.
- 启动时有两条"贴图有没有使用的帧"的警告（瓶中精灵的第 12 帧，液态魔力的第 20 到 31 帧）。贴图和动画文件都和原版 1.12 的相同，所以保持不变。
  At startup there are two warnings about "unused frames in a sprite" (frame 12 of the Bottled Pixie, frames 20 to 31 of Fluided Mana). The textures and the animation files are the same as in the original 1.12, thus they stay as they are.
- 图鉴条目放在 `en_us` 目录里，但是条目里的文字都是翻译键，所以所有语言共用这些文件。
  The lexicon entries are in the directory `en_us`, but all text in the entries is translation keys, thus all languages use these files.

需要在游戏里确认的项目 / Items to check in the game:

- 盖亚守护者III的生命值降到 2/3 时，战斗音乐从盖亚的音乐换成 EGO 的音乐。
  When the health of Guardian of Gaia III goes down to 2/3, the fight music changes from the Gaia track to the EGO track.
- 三张唱片在唱片机里都能播放，律者唱片在音乐结束后不久停止。新的 EGO 唱片的贴图和名字正常。
  The three records all play in a jukebox, and the Herrscher record stops soon after the end of the music. The texture and the name of the new EGO record are correct.
- 图鉴里"额外植物学"分类下的条目和以前一样多，空之律者核心的条目可以正常解锁。
  The category "Extra Botany" of the lexicon has as many entries as before, and the entry of the Core of The Void unlocks in the usual way.
- 机车、真理之钥、心斩的音效正常（它们的文件改了位置）。
  The sounds of the Motor, the Key of Truth and the Thunder Slash are correct (their files have a new location).

## 图鉴 / The lexicon

对照方法 / How it was compared：把原版 1.16 的 54 个图鉴条目和原版 1.12 的图鉴条目逐个和移植版的条目对照，再列出移植版里"任何条目都没有提到"的物品和方块。
The 54 lexicon entries of the original 1.16 and the lexicon entries of the original 1.12 were compared one by one with the entries of this port, and then the items and blocks that no entry of this port refers to were listed.

结果 / Result:

- 原版 1.16 的 112 个物品在移植版里都有。重制版给一部分物品换了注册名：`goldcloth` 现在是 `das_rheingold`（莱茵河的黄金），`manadrivering`（驭魔之戒）现在是 `dispersive_ring`（弥散指环），`buddhistrelics` 现在是 `void_archives`（虚空万藏），`treasurebox` 现在是 `pandoras_box`（潘多拉魔盒）。
  All 112 items of the original 1.16 are in this port. Reburn gave some items a new registry name: `goldcloth` is now `das_rheingold` (Das Rheingold), `manadrivering` (Ring of Mana Drive) is now `dispersive_ring` (Dispersive Ring), `buddhistrelics` is now `void_archives` (Void Archives), and `treasurebox` is now `pandoras_box` (Pandora's Box).
- 新增 14 个条目，文字来自原版 1.12 的语言文件（英文和中文），名字换成了现在的名字，和现在的行为不同的句子做了改动：星尘莲、坐标读取器、王之花园、不随想之茧、FE转换器、液态魔力转化器、活石桶、亚空之矛、犹大的誓约、瓶中火焰、瓶中星、瓶中精灵、限量补给袋（包含静籁永恒和魔力透镜：超导）、装饰饰品（包含超级王冠）。
  14 entries are added. The text comes from the language files of the original 1.12 (English and Chinese), with the names of today, and with changes where the behavior is different now: Stardust Lotus, Position Reader, Garden of King's Law, Cocoon of Desire, Flux Manafield, Mana Liquefaction Device, Livingrock Barrel, Spear of Subspace, Judah Oath, Bottled Flame, Bottled Star, Bottled Pixie, Supply Bag Limited Edition (with Silent Eternity and Mana Lens: SUPERCONDUCTOR), Cosmetic Baubles (with the Super Crown).
- 新的测试 `LexiconGameTests` 检查全部 105 个条目：图标、展示的物品、配方、多方块结构里的方块都存在，翻译键在英文和中文的语言文件里都有。
  The new test `LexiconGameTests` examines all 105 entries: the icon, the shown items, the recipes and the blocks of the multiblock structures exist, and the translation keys are in the English and in the Chinese language file.

仍然没有图鉴条目的内容 / Content that still has no lexicon entry：原版也没有给它们写条目，所以没有可以恢复的文字。要不要补写，由你决定。
The original has no entries for them either, so there is no text to restore. It is your decision if new text is written for them.

- 焢煌之境：劫炎永燎、征服宝石（雷之律者）、影刃、光子霰弹枪、西行寺幽幽子的折扇、银翼护卫套装、静默贤者套装、奖杯、量子魔力缓存器、万圣节糖果和万圣糖惊魂夜、元灵符文和大罪符文、彩虹花瓣。
  Key of Flamescion, Gem of Conquest (Herrscher of Thunder), Shadow Katana, Photon Shotgun, Saigyouji Yuyuko's Fan, the Shooting Guardian set, the Silent Sages set, the Trophy, the Quantum Mana Buffer, the Halloween candies and Halloween Candy Night, the Rune of Element and the Rune of Sin, the Universal Petal.

需要在游戏里确认的项目 / Items to check in the game:

- 图鉴里新的 14 个条目能打开，配方页显示正确，星尘莲的多方块结构预览和实际要求一致。
  The 14 new entries of the lexicon open, the recipe pages are correct, and the multiblock preview of the Stardust Lotus agrees with the real requirement.

## 进度 / Advancements

恢复的进度 / Restored advancements：原版 1.12 里对应"已经恢复的内容"的六个进度。标题和说明来自原版的语言文件，名字从驼峰式改成了下划线式。
Six advancements of the original 1.12 for the content that is already restored. The titles and the descriptions come from the language files of the original, and the names were changed from camel case to snake case.

| 进度 / Advancement | 条件 / Condition | 父进度 / Parent |
|---|---|---|
| `posbinder_craft`（Letter Song） | 得到坐标读取器。 / Get a Position Reader. | `root` |
| `stardustlotus_teleport`（Promise） | 被星尘莲传送一次（代码授予）。 / Be teleported by a Stardust Lotus one time (granted by code). | `posbinder_craft` |
| `kinggarden_craft`（Last Night, Good Night） | 得到王之花园。 / Get a Garden of King's Law. | `craft_pedestal` |
| `spearsubspace_craft`（Nightglow） | 得到亚空之矛。 / Get the Spear of Subspace. | `gaia_trial` |
| `bottle_set`（Inside Out） | 得到瓶中火焰、瓶中精灵、瓶中星和王之花园（原版就是这四样）。 / Get the Bottled Flame, the Bottled Pixie, the Bottled Star and the Garden of King's Law (the original has these four). | `root` |
| `lucky_draw`（Starduster） | 从限量补给袋里抽到静籁永恒（代码授予）。原版在抽到"只有一份"的奖励时授予。 / Draw the Silent Eternity from a Supply Bag Limited Edition (granted by code). The original grants it for a reward that has only one lot. | `herrscher_defeat` |

父进度是我按现在的进度树选的：原版的父进度（例如 `newknowledgeUnlock`）在重制版的进度树里不存在。
I selected the parents for the advancement tree of today: the parents of the original (for example `newknowledgeUnlock`) are not in the advancement tree of Reburn.

没有恢复：原版 1.16 的"进度要求"系统 / Not restored: the "advancement requirement" system of the original 1.16

- 原版的规则 / The rule of the original：九件遗物（空之律者核心、蚀月之心、圣阳尊戒、虚空万藏、文文的相机、王者圣剑、百中弓、最初分型、大师魔力之戒）要求玩家完成"击败 EGO"的进度。没有这个进度的非创造模式玩家不能用它们交互，而且每 tick 都会把手上、盔甲栏和饰品栏里的这些物品丢到地上。
  Nine relics (Core of The Void, Heart of Corrupted Moon, Ring of Sacred Sun, Void Archives, Shameimaru's Camera, Excalibur, Failnaught, First Fractal, Master Band of Mana) need the advancement "defeat the EGO". A player who is not in creative mode and does not have it cannot interact with these items, and on each tick the items in the hands, the armor slots and the bauble slots are dropped.
- 为什么不恢复 / Why it is not restored：在原版里这些遗物只能在击败 EGO 之后得到，所以这条规则只是防止"别人代打"。重制版改了获得方式：文文的相机用暗影锭、盖亚魂锭和望远镜合成，百中弓用盖亚魂锭、奥利哈钢和蕴魔丝线合成，它们在击败盖亚守护者III之前就能做出来。恢复这条规则会让正常合成出相机的玩家不能使用它。
  In the original these relics can only be obtained after the EGO is defeated, so the rule only prevents a "carry" by another player. Reburn changed how they are obtained: Shameimaru's Camera is crafted from Shadowium Ingots, Gaia Spirit Ingots and a spyglass, and the Failnaught from Gaia Spirit Ingots, Orichalcos and Mana String; a player can make them before Guardian of Gaia III is defeated. With the rule restored, a player who made the camera in the usual way could not use it.
- 现在的状态 / The state now：遗物仍然有植物魔法的灵魂绑定（别人拿着会受伤）。最初分型的技能要求"击败空之律者"，这来自原版 1.12，早先已经恢复。
  The relics still have the soulbind of Botania (a different player who holds one gets damage). The skill of the First Fractal needs "defeat the Herrscher of the Void", which comes from the original 1.12 and was restored earlier.
- 如果你想要这条规则，可以把它做成一个默认关闭的配置项。请告诉我。
  If you want this rule, it can be a configuration option that is off by default. Tell me.

原版 1.12 的其余进度（例如 `ringSet`、`musicAll`、`manalinkiumUse`）没有恢复：重制版有自己设计的进度树，这些进度对应的内容在重制版里已经有别的进度。
The other advancements of the original 1.12 (for example `ringSet`, `musicAll`, `manalinkiumUse`) are not restored: Reburn has an advancement tree of its own design, and the content of these advancements already has other advancements in Reburn.

需要在游戏里确认的项目 / Items to check in the game:

- 进度树里新的六个进度的位置和图标正常。
  The six new advancements have a correct position and icon in the advancement tree.
- 玩家被星尘莲传送之后得到"Promise"进度。这一条没有自动测试：测试里的玩家不能真正进入世界（Curios 会在玩家进入时发送测试连接不能接收的数据包）。
  A player gets the advancement "Promise" after a Stardust Lotus teleports the player. This item has no automatic test: a player in a test cannot really enter the level (Curios sends a packet at the entry that the test connection cannot accept).

## 性能 / Performance

怎么测的 / How it was measured:

1. 用 Java Flight Recorder 记录一次完整的测试运行。服务端线程的采样里，本模组的代码大约占 3%，没有哪个方法特别突出。
   A full test run was recorded with the Java Flight Recorder. In the samples of the server thread, the code of this mod is approximately 3%, and no method stands out.
2. 写了一组性能场景（`src/gametest/java/.../PerfScenarios.java`）：每个场景在空地上放 64 个同一种方块，测量服务端每 tick 的耗时，再拿走方块测量空场地，两者相减。花用浮空花，旁边有一个创造魔力池；机器全部空载。
   A set of performance scenarios was written (`src/gametest/java/.../PerfScenarios.java`): each scenario puts 64 blocks of one type on an empty area and measures the time of each server tick, then removes the blocks and measures the empty area; the result is the difference. The flowers are floating flowers with a Creative Mana Pool next to them; the machines all have no load.

运行方法 / How to run them:

```
gradlew runGameTestServer -Pgametest_namespaces=extrabotany_perf
```

结果在日志里，以 `EB_PERF` 开头。平时的测试运行不包含这些场景。环境变量 `EB_PERF_ONLY=floating_woodienia,manabuffer` 可以只运行其中几个。
The results are in the log, in lines that start with `EB_PERF`. The usual test run does not include these scenarios. The environment variable `EB_PERF_ONLY=floating_woodienia,manabuffer` runs only some of them.

结果 / Results（改动之后；每个方块每 tick 的开销，单位微秒 / after the changes; cost of each block on each tick, in microseconds）:

| 方块 / Block | 开销 / Cost (µs) |
|---|---|
| 鲜血妖姬 / Blood Enchantress | 0.97 |
| 石中姬 / Stonesia | 0.71 |
| 液态魔力转化器 / Mana Liquefaction Device | 0.57 |
| 风铃草 / Bellflower | 0.55 |
| 量子魔力缓存器 / Quantum Mana Buffer | 0.46 |
| 其余 25 种（包括伐木花，0.26）/ The other 25 types (the Woodienia, 0.26, included) | 0.05 – 0.37 |

这张表是一次完整运行的结果。三次完整运行里，同一种方块的数字相差最多 0.5 微秒（鲜血妖姬在 1.0 到 1.4 之间），所以 0.5 微秒以下的数字只能说明"很便宜"，不能互相比较。
This table is the result of one full run. In three full runs, the numbers of one block type differ by a maximum of 0.5 microseconds (the Blood Enchantress is between 1.0 and 1.4), thus the numbers below 0.5 microseconds only tell that the block is cheap, and they cannot be compared with each other.

一个 tick 有 50 毫秒。按最贵的鲜血妖姬的最高值算，1000 朵花每 tick 用 1.4 毫秒，不到一个 tick 的 3%。
One tick has 50 milliseconds. With the highest value of the most expensive type, the Blood Enchantress, 1000 flowers use 1.4 milliseconds on each tick, which is less than 3% of a tick.

鲜血妖姬每 tick 查询一次周围的生物，这和原版 1.12 相同（原版查询两次），没有改。
The Blood Enchantress queries the creatures around it one time on each tick; this agrees with the original 1.12 (which queries two times), and it was not changed.

改动 / Changes:

- 伐木花（`WoodieniaBlockEntity`）。这是唯一一个明显的热点。它每 20 tick 扫描一次 17×17×17 的范围（将近五千个方块）来找原木。
  The Woodienia (`WoodieniaBlockEntity`). This was the only clear hot spot. On each 20th tick it scans its 17×17×17 range (almost five thousand blocks) for logs.
  - 扫描按区块段进行，先看区块段的调色板里有没有原木，没有就整段跳过。范围里没有原木时（最常见的情况），一次扫描只检查几个调色板。
    The scan goes by chunk section and examines the palette of the section first; a section whose palette has no log is skipped. When the range has no log (the most usual case), one scan only examines some palettes.
  - 只扫描已经加载的区块。以前的代码会让服务器同步加载范围里未加载的区块。
    Only loaded chunks are scanned. The old code made the server load the chunks of the range synchronously when they were not loaded.
  - 每朵花按自己的位置错开扫描的 tick。以前同一个区块里的花在区块加载之后永远在同一个 tick 扫描。等待时间仍然不超过 20 tick。
    Each flower scans on a tick that depends on its position. Before, the flowers of one chunk always scanned on the same tick after the chunk was loaded. The wait is still 20 ticks or less.
  - 选原木的方法从"随机权重表"改成蓄水池抽样，每个原木被选中的概率仍然相同，不再每次扫描都建一张表。
    The selection of the log changed from a "random weight map" to reservoir sampling; each log still has the same chance, and no map is made on each scan.

  | 64 朵伐木花 / 64 Woodienias | 以前 / Before | 现在 / Now |
  |---|---|---|
  | 每朵花每 tick / Each flower on each tick | 5.3 µs | 0.7 µs |
  | 最慢的 5% 的 tick 多用的时间 / Extra time of the slowest 5% of ticks | 7.2 ms | 0.15 ms |

  （这两列都是单独运行这一个场景得到的，各两次。/ Both columns come from runs of this scenario alone, two runs each.）
- 石中姬（`StonesiaBlockEntity`）。删掉了一个从来不会命中的缓存：它用"配方列表是不是同一个对象"来判断配方有没有变化，但是原版的配方管理器每次都返回一个新的列表（`List.copyOf`），所以缓存每 tick 都被清空。行为没有变化，开销也没有可测量的变化；配方只有 28 个，逐个判断已经足够快。
  The Stonesia (`StonesiaBlockEntity`). A cache that never had a hit was removed: it used "is the recipe list the same object" to tell if the recipes changed, but the recipe manager of vanilla returns a new list each time (`List.copyOf`), thus the cache was cleared on each tick. The behavior did not change, and the cost has no measurable change; there are only 28 recipes, and it is fast enough to examine them one by one.
- 注册类（`ExtraBotanyItems`）。删掉了没有代码使用的 `PICKAXES`、`REPLICATOR_BLACKLIST` 和内部类 `HiddenLensItem`。
  The registry class (`ExtraBotanyItems`). The unused `PICKAXES`, `REPLICATOR_BLACKLIST` and the inner class `HiddenLensItem` were removed.

新的测试 / New tests（`FlowerGameTests`）:

- `woodieniaSelectsOnlyLogsInRange`：范围外的原木不选；范围边上、别的区块和别的区块段里的原木都能选到；三个原木被选中的次数大致相同。
  Logs out of range are not selected; logs on the edge of the range, in other chunks and in other chunk sections can be selected; three logs are selected approximately the same number of times.
- `woodieniaBreaksLogsAndUsesMana`：空闲的花在 20 tick 之内发现新原木；每个原木消耗一次魔力；充能的花不工作。
  An idle flower finds a new log in 20 ticks or less; each log uses mana one time; a powered flower does no work.
- `stonesiaEatsOnlyRecipeBlocksAroundIt`：石中姬只吃符合配方的方块，只在周围 17 个位置里选。
  The Stonesia only eats blocks that agree with a recipe, and only selects from the 17 positions around it.

没有检查的部分 / Parts that were not examined:

- 客户端的渲染性能（翅膀、Boss 的特效、法杖界面）。这需要在真实的显卡上看帧率，自动测试做不到。
  The render performance of the client (wings, boss effects, the wand HUD). This needs the frame rate on a real graphics card, which an automatic test cannot give.
- Boss 战和大量投射物同时存在时的开销。普通的测试运行里有这些内容，JFR 没有显示热点，但是没有单独的场景。
  The cost of boss fights and of many projectiles at the same time. The usual test run has this content and the JFR showed no hot spot, but there is no separate scenario.
- 伐木花在"范围里一直有原木"时的开销（树场）。这时它每 11 tick 扫描一次有原木的区块段。扫描比以前快，但是没有单独测量，因为破坏方块和掉落物的开销会盖过扫描的开销。
  The cost of the Woodienia when its range always has logs (a tree farm). In that case it scans the sections that have logs on each 11th tick. The scan is faster than before, but it was not measured separately, because the cost of the block breaks and of the item drops would hide the cost of the scan.

行为变化 / Behavior change:

- 伐木花不再让服务器加载未加载的区块。如果花的范围伸进了未加载的区块，那里的原木要等区块加载之后才会被破坏。
  The Woodienia no longer makes the server load chunks that are not loaded. If the range of the flower goes into a chunk that is not loaded, the logs there are only broken after the chunk is loaded.
## 本我成为单独的 Boss / The Ego becomes a separate boss

决定 / Decision：维护者要求"按着原版来"。原版 1.16 里本我是一个单独的 Boss；这个移植版以前把它做成了盖亚守护者III的第二个大阶段。
The maintainer asked to "follow the original". In the original 1.16 the Ego is a separate boss; before, this port made it the second large phase of Guardian of Gaia III.

新类 / New classes: `Ego`、`EgoRenderer`、`RelicSwordLayer`。`VoidHerrscherAI` 改名为 `ScriptedBossAI`，空之律者和本我共用它。
`VoidHerrscherAI` is now `ScriptedBossAI`, and the Herrscher of the Void and the Ego use it together.

删除的类 / Removed classes: `GaiaEgoSwordAttack`。

新增注册项 / New registry entries: 实体 `extrabotany:ego`（原版 1.16 的 ID 也是 `ego`）；战利品表 `extrabotany:entities/ego`；进度 `extrabotany:main/ego_defeat`；图鉴条目 `extrabotanies/ego`。本我加入了 `c:bosses` 标签。
The entity `extrabotany:ego` (the ID of the original 1.16 is also `ego`); the loot table `extrabotany:entities/ego`; the advancement `extrabotany:main/ego_defeat`; the lexicon entry `extrabotanies/ego`. The Ego is in the tag `c:bosses`.

本我的规则逐项对照原版 1.16 的 `EntityEGO` / The rules of the Ego, item by item against `EntityEGO` of the original 1.16:

| 规则 / Rule | 原版 1.16 / Original 1.16 | 合并时的做法 / In the merged fight | 现在 / Now |
|---|---|---|---|
| 场地 / Arena | 半径 12 格，高 5 格 / radius 12, height 5 | 盖亚守护者III的场地（15、7）/ the arena of Gaia III (15, 7) | 和原版相同 / same as the original |
| 生命值和护甲 / Health and armor | 600 x 玩家数，护甲 20 / 600 x players, armor 20 | 盖亚守护者III的数值 / the values of Gaia III | 和原版相同 / same as the original |
| 出场 / Entry | 没有无敌时间，立刻攻击 / no invulnerable time, attacks immediately | 200 tick 的出场动画 / an entry of 200 ticks | 和原版相同 / same as the original |
| 伤害上限 / Damage limit | 25 | 30 | 25 |
| 反击 / Counter | 累计 50 点伤害后攻击并传送 / attack and teleport after 50 damage | 没有 / none | 和原版相同 / same as the original |
| 阶段门槛 / Stage thresholds | 低于 75% 和 25% / below 75% and 25% | 总生命值的 1/2 和 1/6 / 1/2 and 1/6 of the full health | 和原版相同 / same as the original |
| 攻击间隔 / Attack interval | 80 - 15 x 阶段 + 0 到 15 的随机数 / 80 - 15 x stage + a random 0 to 15 | 固定的 70、55、45 / fixed 70, 55, 45 | 和原版相同 / same as the original |
| 武器 / Weapon | 每 100 tick 换一次，阶段决定可选的武器 / changes for each 100 ticks, the stage gives the possible weapons | 每次攻击随机选 / random for each attack | 和原版相同 / same as the original |
| 传送 / Teleport | 每 100 - 10 x 阶段 tick，先攻击再传送；路线上的玩家受到 6 点伤害，方块被打碎 / for each 100 - 10 x stage ticks, attack and then teleport; players on the path get 6 damage and blocks are broken | 盖亚守护者的传送行为 / the teleport behavior of the Guardian of Gaia | 和原版相同 / same as the original |
| 随机阵型 / Random formation | 每传送六次之后排一种 / one after each six teleports | 按时间 / by time | 和原版相同 / same as the original |
| 仆从阶段的回血 / Regeneration with minions | 每 tick 0.25，没有上限 / 0.25 on each tick, no limit | 最多回到 2/3 / to a maximum of 2/3 | 和原版相同 / same as the original |
| 名字和皮肤 / Name and skin | 召唤者的名字和皮肤 / those of the summoner | 观看者的皮肤，名字闪成"EGO" / the skin of the viewer, the name flickers to "EGO" | 召唤者的名字和皮肤 / those of the summoner |
| 受伤时的反应 / Reaction to a hit | 不被击退，也不跳向攻击者 / no knockback and no hop to the attacker | 跳向攻击者（盖亚守护者的公共行为）/ hops to the attacker (common behavior of the Guardians of Gaia) | 和原版相同 / same as the original |
| 属性 / Attributes | 移动速度 0.55，击退抗性 1，跟随距离 35，攻击力 8 / speed 0.55, knockback resistance 1, follow range 35, attack damage 8 | 盖亚守护者III的属性 / the attributes of Gaia III | 和原版相同 / same as the original |
| 战利品 / Loot | 只在第 2 阶段被击败时掉落 / only when it is defeated in stage 2 | 盖亚守护者III的战利品 / the loot of Gaia III | 和原版相同 / same as the original |

召唤方式 / The summon:

- 现在的规则 / The rule now：自然蕴息宝珠（200,000 点自然之息，潜行右键信标）召唤本我；挑战券召唤盖亚守护者III。
  The Nature Orb (200,000 Nature Aura, sneak-use on the beacon) summons the Ego; the Challenge Ticket summons Guardian of Gaia III.
- 原因 / The cause：原版 1.12 里挑战券和宝珠都召唤盖亚守护者III（图鉴原文："Challenge Ticket can be used as a one-shot thing to summon Gaia Guardian III"）。原版 1.16 里这两个物品都召唤本我，图鉴先写的是宝珠（"200,000 Natural breath from a Nature Orb or an Invitation Letter"）。这个移植版同时有两个 Boss：盖亚守护者III是重制版对 1.12 那个 Boss 的移植，本我来自 1.16。所以两个物品各召唤一个，每个 Boss 都保留一种原版的召唤方式。
  In the original 1.12 the Challenge Ticket and the orb summon Guardian of Gaia III (the lexicon says: "Challenge Ticket can be used as a one-shot thing to summon Gaia Guardian III"). In the original 1.16 the two items summon the Ego, and the lexicon names the orb first ("200,000 Natural breath from a Nature Orb or an Invitation Letter"). This port has the two bosses at the same time: Guardian of Gaia III is the Reburn port of the boss of 1.12, and the Ego comes from 1.16. Thus each item summons one boss, and each boss keeps one summon method of the original.
- 这是移植时做的选择，不是原版的规则。对调只需要改两处调用。
  This is a selection of the port, not a rule of the original. An exchange only needs a change of two calls.

和原版的差异 / Differences from the original:

- 召唤时检查场地里每名玩家的物品栏和饰品栏（这个模组里 Boss 的公共规则）。原版只检查召唤者。战斗中只收缴手持、穿戴和佩戴的违规物品；原版丢出物品栏里的全部违规物品。
  The summon examines the inventory and the accessory slots of each player in the arena (the common rule of the bosses of this mod). The original only examines the summoner. During the fight only the illegal items that are held or worn are removed; the original drops all illegal items of the inventory.
- 攻击的目标是离本我最近的玩家。原版总是攻击玩家列表里的第一名玩家。
  The target of an attack is the player nearest to the Ego. The original always attacks the first player in the player list.
- 皮肤来自服务器的玩家列表。原版按名字从网上下载。召唤者不在线时用默认皮肤。
  The skin comes from the player list of the server. The original downloads it by name from the internet. When the summoner is not online, the default skin is used.
- 弹幕和仆从阶段里本我正好停在场地中心上方 3 格。原版里它会先下落大约 0.08 格。
  During the barrage and the minion phase the Ego stays exactly 3 blocks above the center of the arena. In the original it first falls approximately 0.08 blocks.
- 弹幕进行到第几波会保存。原版读档之后从第一波重新开始。原版在弹幕开始时"打乱"阵型的顺序，但是那段代码没有效果，所以原版和这里都按固定的顺序排出前六种阵型。
  The wave number of the barrage is saved. The original starts again from the first wave after a load. The original "shuffles" the sequence of the formations at the start of the barrage, but that code has no effect, thus the original and this port put down the first six formations in a fixed sequence.
- 潘多拉魔盒绑定在得到它的玩家身上（和盖亚守护者III的做法相同）。另外有 20% 的几率掉落本我的唱片；原版里这张唱片没有来源，这个移植版以前把它放在盖亚守护者III的唱片池里，现在移到了本我的战利品里。
  The Pandora's Box is bound to the player that gets it (the same as for Guardian of Gaia III). There is also a 20% chance for the music disc of the Ego; in the original this disc has no source, and before, this port had it in the disc pool of Guardian of Gaia III; now it is in the loot of the Ego.
- 死亡时除了原版的爆炸声，还会播放盖亚守护者的死亡音效（公共代码）。
  At death, the death sound of the Guardian of Gaia also plays (common code), together with the explosion sound of the original.
- 多人时的生命值：原版把最大生命值设成 600 x 玩家数，但是没有把当前生命值加满，所以两名玩家召唤出来的本我只有一半的生命值，第一个 tick 就进入地雷弹幕。本我仆从也有同样的问题。这里把生命值加满。单人时没有区别。
  The health with more than one player: the original sets the maximum health to 600 x the number of players, but does not fill the current health; thus an Ego that two players summon only has half of its health and starts the landmine barrage on the first tick. The Ego Minions have the same problem. This port fills the health. With one player there is no difference.
- 战斗音乐循环播放。原版只播放一遍（原版的注释说循环被暂时关掉了）。
  The fight music loops. The original only plays it one time (a comment in the original says that the loop is off for the time being).
- 进度"击败本我"的父进度是"制作祭坛"。原版的父进度是"得到自然蕴息宝珠"，这里没有那个进度。
  The parent of the advancement "defeat the Ego" is "craft a pedestal". The parent in the original is "get the Nature Orb", which is not here.
- 图鉴条目的图标是玩家的头。原版的图标是一个这里没有的物品。条目的文字按现在的召唤方式和阶段数值改写了。
  The icon of the lexicon entry is a player head. The icon of the original is an item that is not here. The text of the entry was written again for the summon and the stage values of today.

盖亚守护者III / Guardian of Gaia III:

- 它恢复成重制版的样子：整场战斗只有骷髅导弹、骷髅地雷和小精灵，音乐不变，唱片池里是盖亚守护者III和空之律者的唱片。
  It is the boss of Reburn again: the full fight only has skull missiles, skull landmines and pixies, the music does not change, and the disc pool has the discs of Guardian of Gaia III and of the Herrscher of the Void.
- 存档 / Saves：旧存档里正在战斗的盖亚守护者III的阶段数据（`BigPhase`、`EgoStage`、`InFogBarrage` 等键）不再被读取，它会以普通的盖亚守护者III继续战斗。它召唤的本我仆从在读档后消失。注册 ID 都没有变。
  The phase data of a Guardian of Gaia III that is in a fight in an old save (the keys `BigPhase`, `EgoStage`, `InFogBarrage` and others) is no longer read; the boss continues the fight as a usual Guardian of Gaia III. The Ego Minions that it summoned go away after the load. No registry ID changed.

行为变化 / Behavior changes:

- 自然蕴息宝珠以前召唤盖亚守护者III（包含本我的阶段），现在召唤本我。挑战券以前召唤的战斗包含本我的阶段，现在不包含。
  Before, the Nature Orb summoned Guardian of Gaia III (with the Ego phase); now it summons the Ego. Before, the fight of the Challenge Ticket had the Ego phase; now it does not.
- 潘多拉魔盒的来源多了一个（本我）。
  The Pandora's Box has one more source (the Ego).

测试 / Tests: `EgoGameTests` 重写，11 项：用宝珠召唤（自然之息不够、带着别的模组的物品、场地里已经有 Boss 时失败）、存档、八种阵型的地雷数量、三种地雷、仆从的规则、伤害上限和反击、第一个 tick 的攻击和传送、75% 的弹幕、25% 的仆从阶段和战利品、玩家变多时离开、盖亚守护者III不再有本我的阶段。
`EgoGameTests` was written again, 11 tests: the summon with the orb (it fails with too little Nature Aura, with an item of a different mod, and with a boss already in the arena), the save data, the landmine counts of the eight formations, the three landmine types, the minion rules, the damage limit and the counter, the attack and the teleport on the first tick, the barrage at 75%, the minion phase and the loot at 25%, the exit when more players come, and Guardian of Gaia III with no Ego phase.

需要在游戏里确认的项目列在 `ACCEPTANCE.md` 的 4.12 里。
The items to check in the game are in 4.12 of `ACCEPTANCE.md`.

## 通用符文、奖杯和进度 / Universal runes, the Trophy and advancements

写图鉴之前逐个核对了物品的功能，发现下面三处在重制版和这个移植版里都没有接通。它们都按原版恢复了。
Before the lexicon text was written, the function of each item was examined; the three items below were not connected in Reburn and in this port. They were restored from the original.

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 元灵符文和大罪符文没有任何代替作用，元灵符文也不是任何配方的材料。原版 1.16 把它们放进植物魔法的符文标签（`botania:runes/air` 等），所以它们能代替对应的符文。植物魔法 1.21.1 的配方直接写物品，不再使用这些标签。 / The Rune of Element and the Rune of Sin replaced nothing, and the Rune of Element was not a material of a recipe. The original 1.16 puts them into the rune tags of Botania (`botania:runes/air` and others), thus they can replace the related runes. The recipes of Botania 1.21.1 name the items directly and no longer use these tags. | 新增 `UniversalRunes` 和 `UniversalRuneMixin`：接受植物魔法符文的配方原料也接受对应的通用符文。有序合成、无序合成和符文祭坛（催化剂会退回）都有效。 / Added `UniversalRunes` and `UniversalRuneMixin`: a recipe ingredient that accepts a rune of Botania also accepts the related universal rune. Shaped crafting, shapeless crafting and the Runic Altar (catalysts are returned) all work. |
| 2 | 奖杯没有获得的途径。进度"Erase or Zero"（完成全部主进度）的数据在数据包里，但是没有代码授予它。原版 1.12 在玩家完成全部进度时授予它并给一个奖杯。 / There was no way to get the Trophy. The data of the advancement "Erase or Zero" (complete all main advancements) was in the data pack, but no code granted it. The original 1.12 grants it when a player completes all advancements, and gives one Trophy. | 新增 `AllAdvancementsHandler`。玩家完成最后一个主进度时，或者登录时已经完成了全部主进度，得到这个进度和一个奖杯。 / Added `AllAdvancementsHandler`. When a player completes the last main advancement, or has all main advancements at login, the player gets this advancement and one Trophy. |
| 3 | 隐藏进度"雷区激活"的数据在数据包里，但是没有代码授予它。 / The data of the hidden advancement "Minefield Active" was in the data pack, but no code granted it. | 和原版 1.12 相同：被红色骷髅地雷炸到的玩家得到它。 / The same as in the original 1.12: a player that a red skull landmine hits gets it. |

通用符文的做法 / How the universal runes work:

- 游戏用两种办法判断物品是否符合配方原料：直接测试物品（`Ingredient.test`，有序合成用它），以及比较物品的编号（`Ingredient.getStackingIds`，无序合成和符文祭坛用它）。mixin 只改这两处的返回值。
  The game has two methods to find if an item agrees with a recipe ingredient: a direct test of the item (`Ingredient.test`, used by shaped crafting), and a comparison of item numbers (`Ingredient.getStackingIds`, used by shapeless crafting and the Runic Altar). The mixin only changes the return values of these two.
- 原料的"可接受物品"列表（`getItems`）没有改。服务端把这个列表发给客户端，植物魔法用它检查"催化剂和原料不能是同一种物品"；改了它，别的模组的配方有可能加载失败。代价是 JEI、EMI 和配方书不会把通用符文显示成可选的原料。
  The list of accepted items of an ingredient (`getItems`) is not changed. The server sends this list to the client, and Botania uses it for the check "a catalyst and an ingredient must not be the same item"; a change there could make the recipes of other mods fail to load. The cost is that JEI, EMI and the recipe book do not show the universal runes as possible ingredients.
- 这两个方法调用得非常频繁。平时多出来的开销是一两次引用比较，不创建对象（使用 MixinExtras 的 `@ModifyReturnValue`）。
  These two methods are called very frequently. The usual added cost is one or two reference comparisons, with no new objects (the mixin uses `@ModifyReturnValue` of MixinExtras).

和原版的差异 / Differences from the original:

- 原版的进度列表是写死的 42 个。这里"主进度"是数据包里命名空间为 `extrabotany`、路径以 `main/` 开头并且有显示信息的全部进度，所以数据包增减进度之后规则仍然成立。隐藏的进度也算。
  The advancement list of the original is a fixed list of 42. Here a "main advancement" is each advancement of the data pack with the namespace `extrabotany`, a path that starts with `main/` and display data; thus the rule stays correct when a data pack adds or removes advancements. Hidden advancements also count.
- 原版在完成 6、12、18 个进度时还分别送出三件物品（弥散指环、魔术手指、精卫之羽）。这个移植版里这三件物品有自己的配方，所以没有恢复这些赠送。
  The original also gives three items at 6, 12 and 18 advancements (the Dispersive Ring, the Magic Finger, the Feather of Jingwei). In this port these three items have their own recipes, thus these gifts were not restored.

测试 / Tests: `UniversalRuneGameTests` 3 项（16 种符文的原料、有序和无序合成、符文祭坛上一次完整的合成和催化剂的退回），`AdvancementGameTests` 新增 1 项（全部主进度给出奖杯，少一个不给，只给一次）。"雷区激活"没有自动测试，原因和星尘莲的进度相同。
`UniversalRuneGameTests` has 3 tests (the ingredients of the 16 runes, shaped and shapeless crafting, one full craft on the Runic Altar with the return of the catalysts), and `AdvancementGameTests` has 1 new test (all main advancements give the Trophy, one missing gives nothing, and it is only given one time). "Minefield Active" has no automatic test, for the same cause as the advancement of the Stardust Lotus.

## 图鉴：以前没有条目的内容 / Lexicon: content that had no entry

维护者要求按功能补写图鉴。新增 12 个条目，文字是按现在的代码行为新写的，不是从原版翻译的（原版也没有这些条目）。
The maintainer asked for lexicon entries that tell the function. 12 entries were added; the text was written new from the behavior of the code of today, and is not a translation of the original (the original also has no such entries).

| 条目 / Entry | 内容 / Content |
|---|---|
| 焢煌之境：劫炎永燎 / Key of Flamescion | 点燃、薪炎模式的五个技能、能量和过载。注明原型来自《崩坏3》。 / The ignition, the five skills of Flamescion mode, the energy and the overload. It names Honkai Impact 3rd as the model. |
| 征服宝石 / Gem of Conquest | 雷之律者的能量、反击和心斩。注明原型来自《崩坏3》。 / The energy, the counterattack and the Thunder Slash of the Herrscher of Thunder. It names Honkai Impact 3rd as the model. |
| 影刃 / Shadow Katana | 夜晚的加成、召唤小精灵、用魔力修复。 / The night bonus, the pixie, the repair with Mana. |
| 光子霰弹枪 / Photon Shotgun | 12 颗弹丸和它们的伤害。 / The 12 pellets and their damage. |
| 西行寺幽幽子的折扇 / Saigyouji Yuyuko's Fan | 三只蝴蝶。条目写明蝴蝶只是装饰（原版没有写完命中效果）。 / The three butterflies. The entry tells that the butterflies are only decoration (the original did not complete the hit effect). |
| 银翼护卫套装 / Shooting Guardian | 每件的属性和四件的套装效果。 / The attributes of each piece and the effects of the full set. |
| 静默贤者套装 / Silent Sages | 魔力折扣。 / The Mana discount. |
| 奖杯 / Trophy | 获得方式。 / How to get it. |
| 量子魔力缓存器 / Quantum Mana Buffer | 容量、速度和连接规则。 / The capacity, the rate and the connection rules. |
| 万圣节糖果 / Halloween Candies | 三种糖果的效果和糖果袋。 / The effects of the three candies, and the candy bag. |
| 通用符文 / Universal Runes | 元灵符文和大罪符文能代替哪些符文。 / The runes that the Rune of Element and the Rune of Sin can replace. |
| 彩虹花瓣 / Universal Petal | 它可以当作任意颜色的花瓣。 / It can be a petal of each color. |

说明 / Notes:

- 只有两个条目注明了《崩坏3》：我能确定来源的只有这两件物品。
  Only two entries name Honkai Impact 3rd: I am only sure about the source of these two items.
- 配方页下面的短句是我写的，不是任何作品里的原句。
  The short lines below the recipe pages are mine, and are not quotes from a work.
- 文字里的数值都来自代码。以后改了数值，要一起改这些条目的文字（`zh_cn.json` 和 `en_us.json` 里以 `extrabotany.page.` 开头的键）。
  All values in the text come from the code. After a change of a value, the text of these entries must change too (the keys that start with `extrabotany.page.` in `zh_cn.json` and `en_us.json`).

## 发布信息 / Release metadata

- 版本号 / Version：`2.0-scex.10-dev` → `2.0.0`，正式版的第一个版本。发布文件是 `extrabotany-neoforge-1.21.1-2.0.0.jar`。
  The first formal release. The release file is `extrabotany-neoforge-1.21.1-2.0.0.jar`.
- 注意 / Note：NeoForge 用 Maven 的规则比较版本，`2.0.0` 排在 `2.0-scex.10-dev` 的前面（`scex` 是一个不认识的限定词，排在正式版之后）。如果别的模组声明了"要求 extrabotany 不低于某个 `2.0-scex` 版本"，它不会接受 `2.0.0`；`2.0.1` 和 `2.1.0` 没有这个问题。SCEX 的森林法杖兼容层有这样的声明，但是它的功能已经并入本模组，并且本模组把它标成了不兼容，所以没有影响。
  NeoForge compares versions with the rules of Maven, and `2.0.0` is before `2.0-scex.10-dev` (`scex` is an unknown qualifier, which is after a release). If a different mod declares "extrabotany must be some `2.0-scex` version or later", it does not accept `2.0.0`; `2.0.1` and `2.1.0` do not have this problem. The SCEX Forest Wand compat bridge has such a declaration, but its function is merged into this mod and this mod marks it as incompatible, thus there is no effect.
- 主页和问题反馈 / Home page and issue tracker：`https://github.com/Rianfalltwilight-lab/scex-extrabotany`。维护者给的是账号名；这个账号下有两个相关的仓库，`scex-extrabotany` 有问题反馈页和发布页，另一个（`Extrabotany`）的简介也指向它，所以选了它。
  The maintainer gave the account name; the account has two related repositories. `scex-extrabotany` has the issue tracker and the releases, and the description of the other one (`Extrabotany`) also points to it, thus it was selected.
## 复查：和原版逐项对照之后的改正 / Review: corrections after an item-by-item comparison with the original

维护者要求再确认一次"是否符合原版的行为"和"有没有留下潜在的问题"。这一节是那次复查的结果：把本我、本我仆从、本我的地雷、五种武器的投射物，以及这一轮新写的代码，逐行和原版 1.16 的源码对照了一遍。
The maintainer asked for one more check of "does it agree with the behavior of the original" and "are there potential problems left". This section is the result of that check: the Ego, the Ego Minion, the landmine of the Ego, the projectiles of the five weapons, and the code that was written new in this round were compared line by line with the source of the original 1.16.

对照之后和原版一致、没有改动的部分 / Parts that agree with the original and did not change: 八种地雷阵型的每一个数值、地雷的伤害和效果、五种武器的投射物在 Boss 手里的伤害、本我的阶段门槛、攻击和传送的间隔、武器的更换、反击、战利品的规则。
Each value of the eight landmine formations, the damage and the effects of the landmines, the damage of the projectiles of the five weapons in the hands of a boss, the stage thresholds of the Ego, the intervals of the attacks and the teleports, the weapon changes, the counter, and the loot rule.

发现并改正的问题 / Defects that were found and corrected:

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 我把本我的属性写错了：没有击退抗性，移动速度 0.4。原版的属性登记在另一个文件里（`ExtraBotany.java`），上一轮我只看了实体类，漏掉了它。 / I had the attributes of the Ego wrong: no knockback resistance, and a speed of 0.4. The original registers the attributes in a different file (`ExtraBotany.java`); in the last round I only read the entity class and did not see it. | 改成原版的数值：移动速度 0.55，击退抗性 1，护甲 20，跟随距离 35，攻击力 8。本我现在不会被击退。 / Changed to the values of the original: speed 0.55, knockback resistance 1, armor 20, follow range 35, attack damage 8. The Ego now gets no knockback. |
| 2 | 本我仆从的属性也和原版不同（第十一批留下的）：移动速度 0.4（原版 0.5），跟随距离 32（原版 35），攻击力没有设置（原版 7）。仆从走位比原版慢 20%。 / The attributes of the Ego Minion were also different from the original (left from batch 11): speed 0.4 (original 0.5), follow range 32 (original 35), no attack damage (original 7). The minions moved 20% slower than in the original. | 改成原版的数值。护甲的默认值是 15，被召唤出来的仆从是 10，和原版相同。 / Changed to the values of the original. The default armor is 15 and a summoned minion has 10, the same as in the original. |
| 3 | 本我仆从的攻击比原版晚 1 tick：原版先倒数，数到 0 的那个 tick 就攻击。 / The attack of an Ego Minion was 1 tick later than in the original: the original does the countdown first and attacks on the tick on which it gets to 0. | 按原版改正。 / Corrected to agree with the original. |
| 4 | 本我传送时，紧贴着它出发位置的玩家也会受到 6 点伤害。原版 1.16 只在传送的路线穿进玩家的碰撞箱时才算命中。 / When the Ego teleported, a player directly at its start position also got 6 damage. The original 1.16 only counts a hit when the path of the teleport goes into the collision box of the player. | 按原版改正。空之律者没有改：它来自 1.12，那一版的判断方法不同，贴身的玩家会被命中。 / Corrected to agree with the original. The Herrscher of the Void did not change: it comes from 1.12, where the check is different and a player at the start position is hit. |
| 5 | 遗物剑总是画在右手里。游戏让大约 5% 的生物成为左撇子，这时挥剑的是左手，剑却在右手。原版把剑画在惯用手里。 / The relic sword was always drawn in the right hand. The game makes approximately 5% of the mobs left-handed; then the left arm did the swing, but the sword was in the right hand. The original draws the sword in the main hand. | 按原版改正（本我和本我仆从）。 / Corrected to agree with the original (the Ego and the Ego Minions). |
| 6 | 读档之后，本我仆从所在的区块可能比本我所在的区块先加载。这时仆从找不到本我，会立刻消失，仆从阶段提前结束。 / After a load, the chunk of an Ego Minion can load before the chunk of the Ego. Then the minion could not find the Ego and went away immediately, and the minion phase stopped early. | 仆从找不到本我时先等 100 tick。本我死亡或者离开时由本我带走仆从，所以正常情况下仆从仍然立刻消失。 / A minion now waits 100 ticks when it cannot find the Ego. When the Ego dies or goes away, the Ego removes its minions, thus in the usual case the minions still go away immediately. |
| 7 | 奖杯只在"自动检查"这条路上给出。用命令直接授予"Erase or Zero"进度的玩家得不到奖杯；原版里只要得到这个进度就给。 / The Trophy was only given on the "automatic check" path. A player that got the advancement "Erase or Zero" directly from a command did not get the Trophy; the original gives it whenever the advancement is obtained. | 改成在得到这个进度时给出奖杯。另外，数据包把主进度全部去掉时不再授予这个进度。 / The Trophy is now given when the advancement is obtained. Also, the advancement is no longer granted when a data pack removes all main advancements. |
| 8 | 佩戴着自然蕴息宝珠、两只手都空着时，潜行右键信标只会打开信标的界面，不能召唤（重制版留下的问题：只有副手的事件才查找佩戴的宝珠，而主手的事件已经把界面打开了）。 / With a worn Nature Orb and two empty hands, a sneak-use on the beacon only opened the beacon screen and did not summon (a problem from Reburn: only the off-hand event searched for the worn orb, but the main-hand event already opened the screen). | 任何一只空手的事件都会查找佩戴的宝珠。这个处理器现在先判断"是否潜行"和"是不是信标"，最后才查找饰品：它在每次右键方块时都运行。 / The event of each empty hand now searches for the worn orb. The handler now examines "sneak" and "is it a beacon" first and searches the accessories last: it runs for each use on a block. |

没有改动、但是值得知道的地方 / Items that did not change, but are good to know:

- 本我每次被攻击都把伤害（最多 25）累加进"反击"的计数，即使这次攻击因为无敌帧没有真正造成伤害。所以连续快速命中的武器会让它很频繁地反击和传送。原版就是这样。
  For each attack, the Ego adds the damage (a maximum of 25) to the "counter" total, also when the attack did no real damage because of the invulnerability frames. Thus a weapon with fast multiple hits makes it do the counter and teleport very frequently. The original is like this.
- 真影刃在 Boss 手里选择目标点的规则来自第二批，没有改：8 格内有玩家时瞄准其中一名，没有时瞄准攻击的目标。
  The rule for the aim of the True Shadow Katana in the hands of a boss comes from batch 2 and did not change: with a player within 8 blocks it aims at one of them, and with no such player it aims at the target of the attack.
- 通用符文有一种极少见的情况：如果别的模组的符文祭坛配方把一种符文当作"消耗的原料"，同时把同一类的另一种符文当作"催化剂"，那么用通用符文同时代替两者时，两个通用符文都会被退回。植物魔法和本模组没有这样的配方。
  The universal runes have one very rare case: if a Runic Altar recipe of a different mod uses one rune as a "consumed ingredient" and a different rune of the same group as a "catalyst", then two universal runes that replace the two are both returned. Botania and this mod have no such recipe.

测试 / Tests: `EgoGameTests` 新增 `egoAndMinionsFireEveryWeapon`（本我和仆从用五种武器各攻击一次都会放出投射物；被攻击的本我不移动），并且检查本我和仆从的属性。`AdvancementGameTests` 检查直接授予进度时也给奖杯。全部 210 项通过。
`EgoGameTests` has the new test `egoAndMinionsFireEveryWeapon` (the Ego and a minion each attack one time with each of the five weapons and always release projectiles; an Ego that is attacked does not move), and examines the attributes of the Ego and of the minions. `AdvancementGameTests` examines that a direct grant of the advancement also gives the Trophy. All 210 tests are successful.

## 进度要求（可选）/ The advancement requirement (optional)

决定 / Decision：维护者同意把原版 1.16 的"进度要求"规则做成默认关闭的配置项。
The maintainer agreed to make the "advancement requirement" rule of the original 1.16 a configuration option that is off by default.

新类 / New class: `EgoRequirementHandler`。新标签 / New tag: `extrabotany:needs_ego_defeat`。新配置项 / New configuration option: `gaia.relicsNeedEgoDefeat`（默认 `false` / default `false`）。

规则打开时，没有完成"击败本我"进度的非创造模式玩家 / With the rule on, a player that is not in creative mode and did not complete the advancement "defeat the Ego":

- 手里拿着受限遗物时，交互被取消（右键方块、右键物品、右键实体、左键方块）。
  cannot interact with a restricted relic in the hand (use on a block, use of the item, use on an entity, left-click on a block);
- 拿在手里、穿在身上或者戴在饰品栏里的受限遗物，每 tick 都被丢到地上，并且在操作栏里看到一条提示。背包里的不受影响。
  has each restricted relic that is held, worn or in an accessory slot dropped on each tick, and sees a message in the action bar. The backpack is not affected;
- 在这些遗物的提示文字里看到一行红字。
  sees a red line in the tooltip of these relics.

受限的遗物是标签里的九件，和原版相同：空之律者核心、蚀月之心、圣阳尊戒、虚空万藏、文文的相机、王者圣剑、百中弓、最初分型、大师魔力之戒。数据包可以修改这个标签。
The restricted relics are the nine items of the tag, the same as in the original: Core of The Void, Heart of Corrupted Moon, Ring of Sacred Sun, Void Archives, Shameimaru's Camera, Excalibur, Failnaught, First Fractal, Master Band of Mana. A data pack can change the tag.

和原版的差异 / Differences from the original:

- 原版里这条规则总是生效。这里默认关闭：重制版里文文的相机和百中弓在击败 Boss 之前就能合成，规则打开之后这样合成出来的遗物不能使用。打开之前请考虑这一点。
  In the original the rule is always on. Here it is off by default: in Reburn, Shameimaru's Camera and the Failnaught can be crafted before the boss is defeated, and with the rule on, relics that are crafted in this way cannot be used. Think about this before you turn it on.
- 原版用物品类上的接口标记受限的物品。这里用标签，所以整合包不用改代码就能调整。
  The original marks the restricted items with an interface on the item class. This port uses a tag, thus a modpack can adjust the list with no code change.
- 原版的客户端总是取消交互，由服务端决定结果。这里客户端按自己知道的进度判断，所以完成了进度的玩家在客户端没有多余的卡顿。
  The client of the original always cancels the interaction, and the server decides the result. Here the client decides with the advancements that it knows, thus a player that completed the advancement has no unnecessary stutter on the client.
- 物品被丢出时，玩家在操作栏里看到原因。原版只有提示文字。
  When an item is dropped, the player sees the cause in the action bar. The original only has the tooltip.
- 这个配置项不会从服务端同步到客户端。服务端决定物品能不能用；客户端的配置只影响提示文字和客户端的预判，所以两边应该设成一样。
  The option is not synchronized from the server to the client. The server decides if an item can be used; the option of the client only has an effect on the tooltip and on the prediction of the client, thus the two sides should have the same value.

性能 / Performance: 规则关闭时，每名玩家每 tick 多读一次配置。规则打开时，每名玩家每 tick 检查 6 个装备栏和饰品栏；进度只在找到受限遗物之后才查询。
With the rule off, one more read of the configuration for each player on each tick. With the rule on, the 6 equipment slots and the accessory slots of each player are examined on each tick; the advancement is only read after a restricted relic is found.

测试 / Tests: `EgoRequirementGameTests` 2 项：默认关闭时什么也不发生；打开之后，没有进度的玩家的遗物被丢出、交互被取消，别的物品、背包里的遗物和创造模式的玩家不受影响，完成进度之后恢复正常。
`EgoRequirementGameTests` has 2 tests: with the default (off) nothing occurs; with the rule on, the relics of a player with no advancement are dropped and the interaction is cancelled, other items, relics in the backpack and players in creative mode are not affected, and all is as usual after the advancement.
## 自检：发布前的第二次复查 / Self-check: the second review before the release

维护者要求再做一次自检。这一节是结果。
The maintainer asked for one more self-check. This section is the result.

怎么查的 / How it was done:

1. 把这一轮新写的类重新读了一遍，并且和原版 1.16 的 `EntityEGO`、`EntityEGOMinion` 逐段对照：本我、本我仆从、地雷和阵型、武器攻击、两个渲染器、通用符文、两个进度处理器、自然蕴息宝珠的召唤、伐木花的扫描。
   The classes that were written new in this round were read again, and compared part by part with `EntityEGO` and `EntityEGOMinion` of the original 1.16: the Ego, the Ego Minion, the landmines and formations, the weapon attacks, the two renderers, the universal runes, the two advancement handlers, the summon with the Nature Orb, and the scan of the Woodienia.
2. 按已知的缺陷类型搜索了全部主代码：同步数据登记在错误的类上（0 处）；用 `==` 比较空物品堆（1 处，是和局部哨兵值比较，没有问题）；没有检查就转换成服务端玩家（22 处，全部在只有服务端才会走到的路径上）；NBT 列表的元素类型（4 处，都正确）；以玩家或世界为键的静态表（都用弱引用，或者在卸载时移除）；没有渲染器的实体类型（39 种都有）；数据包的登记和方向。
   All main sources were searched for known defect types: synchronized data that is defined on an incorrect class (0 places); a comparison of an empty stack with `==` (1 place, a comparison with a local sentinel, which is correct); a cast to a server player with no check (22 places, all on paths that only the server uses); the element type of NBT lists (4 places, all correct); static maps with a player or a level as the key (all use weak references, or the entry is removed at the unload); entity types with no renderer (all 39 have one); the registration and the direction of the packets.
3. 看了测试服务端和开发客户端的日志：没有数据包加载错误。客户端的警告只有原来那两条贴图帧的警告，以及植物魔法和原版自己的几条。
   The logs of the test server and of the development client were examined: there are no data pack load errors. The warnings of the client are only the two known sprite frame warnings, and some from Botania and from vanilla.

发现并改正的问题 / Defects that were found and corrected:

| # | 问题 / Defect | 处理 / Correction |
|---|---|---|
| 1 | 场地里"不能飞行"的规则没有效果。服务端每 tick 清除玩家的飞行标志，但是不通知客户端，而玩家的移动由客户端决定。所以戴着天翼族之冠或者空之律者核心的玩家在四种 Boss 的战斗里都能照常飞行。这是重制版留下的问题。植物魔法的盖亚守护者、原版 1.12 的盖亚守护者III和空之律者、原版 1.16 的本我都在客户端和服务端各清除一次。 / The rule "no flight in the arena" had no effect. The server cleared the flight flag of the players on each tick, but it did not tell the client, and the client controls the movement of its player. Thus a player with the Flügel Tiara or the Core of The Void could fly as usual in the fights with all four bosses. This problem came from Reburn. The Guardian of Gaia of Botania, Guardian of Gaia III and the Herrscher of the Void of the original 1.12, and the Ego of the original 1.16 all clear the flag on the client and on the server. | `GaiaArena#tick` 在客户端清除本地玩家的飞行标志（创造模式除外），做法和植物魔法相同。 / `GaiaArena#tick` clears the flight flag of the local player on the client (not in creative mode), the same as Botania does. |
| 2 | 通用符文的 mixin 在单人游戏里有一个很难遇到、但是后果持久的问题。单人游戏里服务端把配方对象直接交给客户端，两个线程用同一个原料对象。游戏第一次生成原料的物品编号列表时，先把空列表放进字段再往里填；另一个线程这时读到的是没有填完的列表。原来的 mixin 会把这个不完整的列表复制一份、加上通用符文，再放回游戏的字段里。这样这个原料的列表就一直缺少物品，用到它的无序配方和符文祭坛配方在重启之前不能合成。只有接受符文的原料会受影响。 / The mixin for the universal runes had a problem in single-player games that is very hard to get, but has a lasting effect. In a single-player game the server gives the recipe objects directly to the client, and two threads use the same ingredient object. When the game makes the item number list of an ingredient for the first time, it puts an empty list into the field and then fills it; a different thread reads an incomplete list at that time. The mixin before copied this incomplete list, added the universal runes, and put the copy back into the field of the game. Then the list of this ingredient had missing items permanently, and the shapeless recipes and the Runic Altar recipes that use it could not be crafted until a restart. Only ingredients that accept runes were affected. | mixin 不再改动游戏的字段，也不再复制游戏的列表。它从原料的"可接受物品"列表重新生成自己的列表，保存在自己的字段里；游戏的列表没有填满时不保存结果。平时的开销仍然是一次引用比较。 / The mixin does not change the field of the game and does not copy the list of the game. It makes its own list from the list of accepted items of the ingredient, and keeps it in its own field; it stores no result while the list of the game is not full. The usual cost is still one reference comparison. |
| 3 | Boss 读档之后（例如战斗中途重启服务器），血条上的参战玩家数变成 0，客户端不再显示它。玩家数本身保存了，只是没有交给血条。重制版的构造方法里有一条相关的"待办"注释。 / After a boss was loaded (for example after a server restart during a fight), the player count of the boss bar became 0, and the client did not show it. The player count itself was saved, but it was not given to the boss bar. The constructor of Reburn had a "to do" comment about this. | 读档时把玩家数交给血条。四种 Boss 都受益。 / The load gives the player count to the boss bar. All four bosses have the benefit. |
| 4 | 本我仆从总是用粗手臂的模型。它用的是观看者自己的皮肤，所以皮肤是细手臂的玩家会看到仆从手臂上的贴图错位。本我和盖亚守护者已经按皮肤的种类选模型。 / The Ego Minion always used the model with wide arms. It uses the skin of the viewer, thus a player with a slim-arm skin saw the texture on the arms of the minions out of position. The Ego and the Guardians of Gaia already select the model by the type of the skin. | 仆从的渲染器也按皮肤的种类选模型。 / The renderer of the minion also selects the model by the type of the skin. |
| 5 | 王者圣剑、虚空万藏（王者圣剑形态）和精卫之羽的"对着空气挥动"数据包只靠服务端的攻击冷却来限速，而冷却是由客户端紧接着发来的挥手数据包重置的。不发挥手数据包的改版客户端可以每 tick 发射一次。植物魔法的泰拉之刃有同样的特点。 / The "swing at the air" packets of the Excalibur, of the Void Archives (in its Excalibur form) and of the Feather of Jingwei were only limited by the attack cooldown of the server, and the swing packet that the client sends immediately afterward resets that cooldown. A modified client that sends no swing packet could shoot on each tick. The Terra Blade of Botania has the same property. | 三个数据包在处理之后自己重置攻击冷却。正常的客户端没有任何变化。 / The three packets reset the attack cooldown themselves after they are handled. There is no change for a usual client. |

没有改动、但是值得知道的地方 / Items that did not change, but are good to know:

- 对 Boss 使用 `/kill`：Boss 的生命值变成 0 并且消失，但是不掉落战利品，也没有死亡的音效（重制版的设计）。原版的 Boss 完全不受 `/kill` 影响。
  `/kill` on a boss: the health of the boss becomes 0 and the boss goes away, but there is no loot and no death sound (the design of Reburn). `/kill` has no effect at all on the bosses of the original.
- 本我因为死亡以外的原因消失时（和平难度、场地里没有玩家、在场玩家变多），已经排出的地雷仍然会在 50 tick 之内爆炸。原版相同。
  When the Ego goes away for a cause other than death (peaceful difficulty, no player in the arena, more players in the arena), the landmines that are already there still explode in 50 ticks or less. The original is the same.
- 本我仆从选定一名玩家之后一直跟着他，直到他死亡。玩家用别的办法离开场地时，本我因为场地里没有玩家而消失，仆从随后也消失（最多 100 tick）。
  After an Ego Minion selects a player it follows that player until the player dies. When the player leaves the arena by a different method, the Ego goes away because the arena has no player, and the minions go away afterward (in 100 ticks or less).
- 本我死亡时，客户端会看到两次爆炸粒子（公共代码一次，本我自己一次）。
  At the death of the Ego the client sees the explosion particles two times (one time from the common code, one time from the Ego itself).

测试 / Tests: `ItemAuditGameTests` 新增 `leftClickPacketsResetTheAttackCooldown`（三个数据包处理之后攻击冷却归零）。`UniversalRuneGameTests` 增加了检查：只有一种符文的原料有两个物品编号，并且每次调用返回同一个列表。全部 211 项通过。
`ItemAuditGameTests` has the new test `leftClickPacketsResetTheAttackCooldown` (the attack cooldown is zero after each of the three packets is handled). `UniversalRuneGameTests` has more checks: an ingredient with one rune has two item numbers, and each call returns the same list. All 211 tests are successful.

第 1、3、4 项只在客户端或者重启之后才看得出来，没有自动测试；它们列在 `ACCEPTANCE.md` 的 4.12 和 4.14 里。第 2 项的线程问题不能用测试稳定地重现；测试只确认新的做法结果正确。
Items 1, 3 and 4 can only be seen on the client or after a restart, and have no automatic test; they are in 4.12 and 4.14 of `ACCEPTANCE.md`. The thread problem of item 2 cannot be reproduced reliably in a test; the tests only confirm that the result of the new method is correct.

## 待办 / Open items

没有未完成的移植项目。需要在游戏里确认的项目和需要你决定的事项集中在 [`ACCEPTANCE.md`](ACCEPTANCE.md) 里。
There are no open port items. The items to check in the game and the items for your decision are collected in [`ACCEPTANCE.md`](ACCEPTANCE.md).