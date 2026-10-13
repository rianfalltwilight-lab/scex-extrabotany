# 验收清单 / Acceptance checklist

本文件把 [`PORT_AUDIT.md`](PORT_AUDIT.md) 里需要在游戏里确认的项目和需要你决定的事项集中在一起。每一项后面的"第 N 批"指向 `PORT_AUDIT.md` 里对应的一节，那里有详细说明。
This file collects the items of [`PORT_AUDIT.md`](PORT_AUDIT.md) that must be checked in the game and the items that need your decision. "Batch N" after an item points to the related section of `PORT_AUDIT.md`, which has the details.

## 1. 准备 / Preparation

- 构建 / Build: `./gradlew build`。发布用的文件是 `build/libs/extrabotany-neoforge-1.21.1-2.0.0.jar`。
  The release file is `build/libs/extrabotany-neoforge-1.21.1-2.0.0.jar`.
- 必需的模组 / Necessary mods: NeoForge 21.1.248 或更高的 21.1 版本、植物魔法 456、Patchouli、Curios。JEI、EMI 和 KubeJS 是可选的。
  NeoForge 21.1.248 or a later 21.1 version, Botania 456, Patchouli, Curios. JEI, EMI and KubeJS are optional.
- 用旧存档测试之前，先备份存档。
  Make a backup of an old save before you test with it.
- 开发环境的客户端 / The development client: `./gradlew runClient`。

## 2. 已经自动验证的内容 / What the automatic checks cover

- 211 个游戏内测试全部通过（`./gradlew runGameTestServer`）。测试在专用服务器的环境里运行，这个环境里加载客户端的类会报错。
  All 211 game tests are successful (`./gradlew runGameTestServer`). The tests run in the environment of a dedicated server, in which the load of a client class is an error.
- 数据生成器可以重复运行：`runData` 之后工作区没有变化。
  The data generators can run again and again: the work tree has no change after `runData`.
- 发布 JAR：2604 个文件，没有重复的文件，没有测试和数据生成器的类；659 个类里，客户端包以外的类没有一个引用客户端的类。
  The release JAR: 2604 files, no file two times, no classes of the tests or of the data generators; of the 659 classes, none outside the client packages refers to a client class.
- 开发环境的客户端能启动到标题界面。日志里只有两条已知的警告（瓶中精灵和液态魔力的贴图有没有使用的帧，和原版 1.12 相同）。
  The development client starts and shows the title screen. The log only has two known warnings (the textures of the Bottled Pixie and of the Fluided Mana have unused frames, the same as the original 1.12).
- KubeJS 的配方脚本在运行时测试过（见 `kubejs.md`）。
  The recipe scripts of KubeJS were tested at run time (see `kubejs.md`).

## 3. 没有验证的内容 / What was not verified

这些只能由你来做，或者需要你先同意。
Only you can do these, or they need your agreement first.

- [ ] 用发布 JAR 启动真正的专用服务器。这需要你自己同意 Minecraft 的 EULA（`eula.txt`），并且需要一个 NeoForge 服务端。开发环境里可以用 `./gradlew runServer`，它也要求 `run/eula.txt`。
  A start of a real dedicated server with the release JAR. This needs your own agreement to the Minecraft EULA (`eula.txt`) and a NeoForge server. In the development environment, `./gradlew runServer` can be used, and it also needs `run/eula.txt`.
- [ ] 真实的多人游戏：两个客户端连接同一个服务器。数据包的同步、Boss 战里的多名玩家、PvP 规则只在这种环境里才能完整检查。
  A real multiplayer game: two clients on one server. The synchronization of the packets, more than one player in a boss fight, and the PvP rules can only be fully checked there.
- [ ] 用真实的旧存档进入游戏。注册 ID 和存档的键都有测试，但是没有用真实的旧世界试过。
  A real old save in the game. The registry IDs and the save keys have tests, but no real old world was used.
- [ ] 在整合包里和别的模组一起运行，包括 JEI 和 EMI 里的配方显示。
  A run in a modpack together with other mods, the recipe display in JEI and EMI included.
- [ ] 所有的画面效果和手感：模型的位置、动画、粒子、界面。自动测试看不到画面。
  All visual effects and the feel: model positions, animations, particles, HUDs. The automatic tests cannot see the screen.
- [ ] 客户端的渲染性能（帧率）。
  The render performance of the client (frame rate).

## 4. 游戏内检查 / In-game checks

### 4.1 旧存档 / Old saves

- [ ] 旧存档能打开，物品栏、箱子和饰品栏里的本模组物品都还在。
  The old save opens, and the items of this mod in the inventory, in chests and in the accessory slots are all still there.
- [ ] 已经放下的花、机器和祭坛都还在，并且继续工作。
  The flowers, machines and pedestals that were already placed are all still there and continue to work.
- [ ] 旧存档里含水的魔力机器现在不含水，没有别的变化（第 9 批）。
  A mana machine that was waterlogged in the old save is now not waterlogged, with no other change (batch 9).
- [ ] 玩家身上的薪炎能量和过载状态在登录后还在（第 6 批，存档迁移）。
  The Flamescion energy and the overload state of the player are still there after the login (batch 6, save migration).
- [ ] 已经完成的进度没有丢失。
  The advancements that were already done are not lost.
- [ ] 旧存档里的自然蕴息宝珠和挑战券都还在，储存的自然之息没有变。注意它们现在召唤不同的 Boss（见 4.12）。
  The Nature Orb and the Challenge Ticket of the old save are still there, and the stored Nature Aura did not change. Note that they now summon different bosses (see 4.12).

### 4.2 饰品和伤害 / Baubles and damage（第 1 批 / batch 1）

- [ ] 冰霜之星和死亡戒指隔着墙也生效。
  The Frost Star and the Death Ring also work through a wall.
- [ ] 薯片在致命伤害时触发，并且播放物品动画；对带 `c:bosses` 标签的 Boss 不触发。
  The Potato Chips activate on lethal damage and play the item animation; they do not activate against a boss that has the `c:bosses` tag.
- [ ] 瓶中火焰在亮度低于 8 的地方放火把。
  The Bottled Flame puts a torch where the light level is below 8.
- [ ] 饰品在玩家身上的渲染位置正常（面具、狐耳、围巾等装饰品）。
  The render position of the baubles on the player is correct (the mask, the fox ears, the scarf and the other cosmetic items).

### 4.3 工具、遗物剑和投射物 / Tools, relic swords and projectiles（第 2 批 / batch 2）

- [ ] 瓶中精灵召唤的宠物精灵跟随主人的攻击目标，不攻击主人。
  The pet pixie of the Bottled Pixie follows the attack target of its owner and does not attack the owner.
- [ ] 遗物剑的剑气能穿过方块。真影刃的三把飞刃在身前排成一道弧。
  The beams of the relic swords go through blocks. The three blades of the True Shadow Katana make an arc in front of the player.
- [ ] 最初分型没有耐久；魔力不足时技能仍然可以使用。
  The First Fractal has no durability; the skill still works with too little mana.
- [ ] 左键技能的连点速度受攻击冷却限制。
  The attack cooldown limits the click rate of the left-click skills.

### 4.4 盔甲和银翼射手 / Armor and the Silver Bullet（第 3 批 / batch 3）

- [ ] 三套盔甲的护甲值：星空歌姬 1/5/4/2，银翼护卫 4/8/7/3，静默贤者 5/9/8/4（头盔/胸甲/护腿/靴子）。
  The armor values of the three sets: Starry Idol 1/5/4/2, Shooting Guardian 4/8/7/3, Silent Sages 5/9/8/4 (helmet/chestplate/leggings/boots).
- [ ] 穿齐银翼护卫：吸血、拉弓加速和治疗减弱都生效；银翼射手不消耗魔力。
  With the full Shooting Guardian set: the life steal, the faster bow draw and the decreased healing all work; the Silver Bullet uses no mana.

### 4.5 坐骑和真理之钥 / Mounts and the Key of Truth（第 4 批 / batch 4）

- [ ] 骑手的座位高度（机车 0.475，飞碟 0.855）看起来正常。
  The seat heights of the rider (motor 0.475, car 0.855) look correct.
- [ ] 机车车轮的转动方向和转速。
  The turn direction and the turn speed of the motor wheels.
- [ ] 机车的加速、转向、冲刺和爬墙；飞碟的六向飞行、疾跑键下降、技能键（默认 R）抓取。
  The acceleration, the steering, the dash and the wall climb of the motor; the flight of the car in six directions, the sprint key to go down, and the skill key (R by default) to catch.
- [ ] 机车的能量条，充能后召唤真理之钥，骑上机车的声音。
  The energy bar of the motor, the Keys of Truth of a charged motor, and the ride-on sound.

### 4.6 雷之律者 / The Herrscher of Thunder（第 5 批 / batch 5）

- [ ] 手臂装甲和背后圣痕的位置。
  The position of the arm armor and of the stigma behind the back.
- [ ] 心斩的 35 帧动画；触发方式是按住疾跑键对空挥剑。
  The 35-frame animation of the Thunder Slash; the trigger is to hold the sprint key and swing a sword at the air.
- [ ] 能量条的显示，反击消耗 200 点能量。
  The energy bar, and the counterattack that uses 200 energy.

### 4.7 薪炎 / The Flamescion（第 6 批 / batch 6）

- [ ] 抗火的生物（例如烈焰人）也会受到薪炎的伤害。
  Mobs that resist fire (for example a blaze) also get damage from the Flamescion.
- [ ] 冲刺不会让玩家卡进方块；终结技和黑洞的汉字图案正常显示。
  The dash does not put the player into a block; the pictures of Chinese characters of the ultimate and of the void show correctly.
- [ ] 能量条和过载状态的显示。
  The energy bar and the display of the overload state.

### 4.8 亚空之矛和犹大的誓约 / The Spear of Subspace and the Judah Oath（第 7 批 / batch 7）

- [ ] 亚空之矛没有耐久；魔力不足时蓄力技能仍然可以使用，冷却变长。
  The Spear of Subspace has no durability; the charged skill still works with too little mana, with a longer cooldown.
- [ ] 犹大的誓约的光矛给敌人"神圣制裁"效果，敌人的最大生命值降低。
  The spears of the Judah Oath give the Divine Justice effect to enemies, and the maximum health of the enemies decreases.
- [ ] 亚空之矛的模型在碰撞箱的中心；五角星结界的形状正常。
  The model of the Subspace Spear is at the center of its collision box; the shape of the star field is correct.

### 4.9 王之花园、静籁永恒、全能指环和补给袋 / The Garden of King's Law, the Silent Eternity, the ALL FOR ONE ring and the supply bag（第 8 批 / batch 8）

- [ ] 王之花园接受星尘莲；花之武器撞上方块就消失，熵变花的武器会爆炸。
  The garden accepts the Stardust Lotus; a Flower Weapon goes away when it hits a block, and the weapon of the Entropinnyum makes an explosion.
- [ ] 全能指环和精灵王指环里的饰品效果生效（属性加成、植物魔法饰品的效果）。
  The effects of the accessories in the ALL FOR ONE ring and in the Ring of Elven King work (attribute bonuses, the effects of the Botania accessories).
- [ ] 限量补给袋：第一个袋子给空的律者核心；奖池抽完之后每次只给 2 个绿宝石。
  Supply Bag Limited Edition: the first bag gives an Empty Herrscher Core; after the pool is empty, each draw only gives 2 emeralds.

### 4.10 机器、活石桶、不随想之茧和星尘莲 / Machines, the Livingrock Barrel, the Cocoon of Desire and the Stardust Lotus（第 9 批 / batch 9）

- [ ] 不随想之茧的摇动动画，以及茧的亮度和普通方块相比是否自然。
  The shake animation of the Cocoon of Desire, and if the brightness of the cocoon looks natural next to usual blocks.
- [ ] 活石桶里液体表面的位置和颜色（水、液态魔力、岩浆）。
  The position and the color of the liquid surface in the Livingrock Barrel (water, Fluided Mana, lava).
- [ ] 森林法杖看四种机器时的界面。
  The HUD of the Wand of the Forest for the four machines.
- [ ] 星尘莲的进度光圈和法杖界面；两朵互相指向的星尘莲不会让生物不停地来回传送。
  The progress ring and the wand HUD of the Stardust Lotus; two lotuses that point at each other do not move a creature back and forth continuously.

### 4.11 空之律者 / The Herrscher of the Void（第 10 批 / batch 10）

- [ ] 整场战斗的节奏。原版没有出场动画，并且 Boss 在第一 tick 就传送。
  The rhythm of the full fight. The original has no entry animation, and the boss teleports on the first tick.
- [ ] 矛枪的位置（应该正好插在地上）、闪电链的闪电和电网的光点。
  The position of the lances (they must stand exactly in the ground), the lightning of the chain and the light points of the web.
- [ ] 审判之剑的光环、虚空力场的减速手感。
  The halo of the sword of judgment, and the feel of the slowdown in the void field.
- [ ] Boss 的翅膀和护盾，黄色的血条，蓝紫色的场地光圈。
  The wings and the shield of the boss, the yellow boss bar, and the blue-purple ring of the arena.

### 4.12 本我和盖亚守护者III / The Ego and Guardian of Gaia III（"本我成为单独的 Boss"一节 / section "The Ego becomes a separate boss"）

- [ ] 召唤：手持或佩戴有 200,000 点自然之息的自然蕴息宝珠，潜行右键盖亚仪式场地（和植物魔法的盖亚仪式相同，半径 12 格）中间的信标，出现的是本我，宝珠少了 200,000 点自然之息。创造模式物品栏里有一个充满的宝珠。
  The summon: hold or wear a Nature Orb that has 200,000 Nature Aura, and sneak-use the beacon in the center of a Gaia ritual arena (the same as for the Gaia ritual of Botania, a radius of 12 blocks); the Ego appears, and the orb has 200,000 Nature Aura less. The creative tab has a full orb.
- [ ] 挑战券召唤的是盖亚守护者III（重制版的 Boss，场地半径 15 格），整场战斗里没有本我的阶段：没有彩色的地雷阵，没有拿遗物剑的仆从，音乐一直是盖亚守护者III的音乐。
  The Challenge Ticket summons Guardian of Gaia III (the boss of Reburn, with an arena radius of 15 blocks), and the full fight has no Ego phase: no colored landmine formations, no minions with relic swords, and the music is always that of Guardian of Gaia III.
- [ ] 本我用召唤者的名字（血条上）和召唤者的皮肤，手里拿着它现在用的遗物剑，武器每隔一会儿换一把。
  The Ego has the name of its summoner (on the boss bar) and the skin of its summoner, holds the relic sword that it uses at this time, and changes its weapon at intervals.
- [ ] 本我没有出场动画，一出现就攻击。它被攻击时不会被击退，也不会跳向攻击者（和原版相同）。
  The Ego has no entry animation and attacks immediately. When it is attacked it gets no knockback and does not hop to the attacker (the same as in the original).
- [ ] 两只手都空着、只佩戴着自然蕴息宝珠时，潜行右键信标也能召唤本我，信标的界面不会打开。
  With two empty hands and only a worn Nature Orb, a sneak-use on the beacon also summons the Ego, and the beacon screen does not open.
- [ ] 左撇子的本我或者仆从（大约二十个里有一个）把剑拿在左手，挥剑的也是左手。
  A left-handed Ego or minion (approximately one in twenty) holds the sword in the left hand, and the left arm does the swing.
- [ ] 用细手臂（Alex 型）皮肤的玩家看本我仆从：手臂上的贴图没有错位。
  A player with a slim-arm (Alex type) skin looks at the Ego Minions: the texture on the arms is not out of position.
- [ ] 生命值低于 75%：本我回到场地中心上方，无敌，依次排出六种地雷阵型。八种阵型的样子，以及玩家能不能找到安全的位置。
  Below 75% health: the Ego goes back above the center of the arena, cannot be hurt, and puts down six landmine formations in sequence. The look of the eight formations, and if a player can find a safe position.
- [ ] 生命值低于 25%：本我召唤四个本我仆从，无敌并且回血；仆从全部被击败之后，无敌立刻结束。仆从手里的遗物剑的位置，以及仆从绕着玩家走位的样子。
  Below 25% health: the Ego summons four Ego Minions, cannot be hurt and regenerates; after all minions are defeated, it can be hurt again immediately. The position of the relic swords in the hands of the minions, and how the minions move around the player.
- [ ] 击败本我：每名参战的玩家得到一个潘多拉魔盒，有 20% 的几率得到本我的唱片；得到进度"KiLLER LADY"。战斗音乐是本我的音乐，Boss 死亡后停止。
  The Ego is defeated: each fighter gets one Pandora's Box and has a 20% chance for the music disc of the Ego; the fighters get the advancement "KiLLER LADY". The fight music is that of the Ego, and it stops after the boss dies.
- [ ] 多人：第二名玩家在战斗中途走进场地时，本我离开，场地里的玩家收到提示。
  Multiplayer: when a second player goes into the arena during the fight, the Ego goes away, and the players in the arena get a message.
- [ ] 整场战斗的难度。
  The difficulty of the full fight.
### 4.13 花、方块和药水效果 / Flowers, blocks and potion effects（第 12 批 / batch 12）

- [ ] 用雪绒花或死之花做迷之炖菜，吃下后效果立刻生效。
  A suspicious stew from an Edelweiss or a Necrofleur has its effect immediately.
- [ ] 手持森林法杖看风铃草：显示的风力和"主手法杖、副手风铃草、对着空气使用"在聊天栏显示的风力相同。
  The Wand of the Forest on a Bellflower: the wind level that it shows is the same as the wind level in the chat for "wand in the main hand, Bellflower in the off hand, use on the air".
- [ ] 永寂龙胆和叮当舞花收到红石信号时停止工作。
  The Serenitian and the Tinkle stop with a redstone signal.
- [ ] 祭坛的一级和二级结构仍然能给自然蕴息宝珠充能。
  The level 1 and level 2 structures of the pedestal can still charge the Nature Orb.
- [ ] 伐木花：能砍掉范围边上的原木（水平 8 格、向上 16 格）；一片伐木花不会让游戏周期性地卡顿（性能一节）。
  Woodienia: it breaks logs on the edge of its range (8 blocks horizontally, 16 blocks up); a field of Woodienias does not cause a periodic stutter (the performance section).

### 4.14 盖亚战斗、物品和事件 / Gaia fights, items and events（第 13 批 / batch 13）

- [ ] 勇士盾：用它攻击能攒下伤害吸收（黄色的心），攒到 10 点后潜行并使用可以解放。
  Achilles Shield: attacks with it collect absorption (yellow hearts), and at 10 points a sneak-use releases it.
- [ ] 精卫之羽：火焰命中后玩家得到伤害吸收。
  Feather of Jingwei: the player gets absorption after the fire hits.
- [ ] 盖亚守护者传送时，出发的位置有紫色的传送粒子；血条的噪点在出场结束时正常消失。
  When a Guardian of Gaia teleports, the start position has purple teleport particles; the noise of the boss bar goes away correctly at the end of the entry.
- [ ] 四种 Boss（盖亚守护者、盖亚守护者III、空之律者、本我）的场地里，生存模式的玩家不能用天翼族之冠或者空之律者核心飞行：起飞之后立刻落下。创造模式的玩家可以飞。
  In the arenas of the four bosses (Guardian of Gaia, Guardian of Gaia III, Herrscher of the Void, Ego), a survival player cannot fly with the Flügel Tiara or the Core of The Void: the player comes down immediately after the takeoff. A player in creative mode can fly.
- [ ] 战斗中途退出再进入世界（或者重启服务器）之后，血条旁边仍然显示参战的玩家数。
  After an exit and a new entry into the world during a fight (or a server restart), the boss bar still shows the number of players in the fight.
- [ ] 莱茵锤开启后魔力的消耗速度明显变慢，四种增益仍然一直存在。
  After the Rhein Hammer is on, the mana cost is clearly slower, and the four buffs are still always there.
- [ ] 梦魇燃料和精神燃料仍然能在熔炉里燃烧。
  The Nightmare Fuel and the Spirit Fuel still burn in a furnace.

### 4.15 界面和客户端 / HUDs and the client（第 14 批和"结构整理" / batch 14 and "Structure"）

- [ ] 模组列表里本模组的"鸣谢"一栏显示"似水流年"。
  The "credits" field of this mod in the mod list shows 似水流年.
- [ ] 百中弓拉弓时的贴图变化、锤子开启时的贴图、酿造瓶的剩余次数贴图。
  The texture change of the Failnaught during the draw, the texture of a hammer that is on, and the textures for the remaining uses of the brew bottles.
- [ ] 拿着使用魔力的物品时，物品栏上方的魔力条正常显示；戴着大师魔力之戒时魔力条不会变成空的或者溢出。
  With a mana item in the hand, the mana bar above the hotbar shows correctly; with the Master Band of Mana, the bar does not become empty and does not overflow.
- [ ] 空之律者核心的四种翅膀都正常显示并扇动，律者的翅膀在暗处也是亮的。
  The four wing types of the Core of The Void all show and flap, and the wings of the Herrscher are also bright in the dark.
- [ ] 森林法杖看神烦花、蕴魔瑾、魔链星、风铃草时的界面。
  The HUD of the Wand of the Forest for the Annoyingflower, the Enchanter, the Manalink and the Bellflower.
- [ ] 戴着成龙指环贴墙按跳跃键可以蹬墙跳。举起文文的相机时取景框正常显示。
  With the Jackie Chan Ring, the jump key at a wall makes a wall jump. The viewfinder shows correctly when Shameimaru's Camera is up.
- [ ] 按键设置里的技能键（默认 R）仍然存在。
  The skill key (R by default) is still in the key settings.

### 4.16 数据包、图鉴和进度 / Data pack, lexicon and advancements

- [ ] 三张唱片在唱片机里都能播放，律者唱片在音乐结束后不久停止。新的 EGO 唱片的贴图和名字正常。
  The three music discs all play in a jukebox, and the Herrscher disc stops a short time after the end of the music. The texture and the name of the new EGO disc are correct.
- [ ] 机车、真理之钥和心斩的音效正常（它们的文件改了位置）。
  The sounds of the motor, of the Key of Truth and of the Thunder Slash are correct (their files moved).
- [ ] 图鉴里"额外植物学"分类下的条目和以前一样多，另外多了 14 个原版 1.12 的条目；配方页显示正确；星尘莲的多方块结构预览和实际要求一致；空之律者核心的条目可以正常解锁。
  The "Extra Botany" category of the lexicon has as many entries as before, plus 14 entries of the original 1.12; the recipe pages are correct; the multiblock preview of the Stardust Lotus agrees with the real requirement; the entry of the Core of The Void can be unlocked.
- [ ] 图鉴里新的 13 个条目能打开，文字和实际功能一致：本我、焢煌之境：劫炎永燎、征服宝石、影刃、光子霰弹枪、西行寺幽幽子的折扇、银翼护卫套装、静默贤者套装、奖杯、量子魔力缓存器、万圣节糖果、通用符文、彩虹花瓣。按键的名字（潜行、使用、疾跑、技能键）显示成你设置的按键。
  The 13 new lexicon entries open, and their text agrees with the real function: the Ego, the Key of Flamescion, the Gem of Conquest, the Shadow Katana, the Photon Shotgun, Saigyouji Yuyuko's Fan, the Shooting Guardian set, the Silent Sages set, the Trophy, the Quantum Mana Buffer, the Halloween Candies, the Universal Runes, and the Universal Petal. The key names (sneak, use, sprint, the skill key) show the keys that you set.
- [ ] 进度树里新的六个进度和"KiLLER LADY"的位置和图标正常。
  The six new advancements and "KiLLER LADY" have a correct position and icon in the advancement tree.
- [ ] 被盖亚守护者III的红色骷髅地雷炸到之后得到隐藏进度"雷区激活"（这一条没有自动测试）。
  A hit from a red skull landmine of Guardian of Gaia III gives the hidden advancement "Minefield Active" (this item has no automatic test).
- [ ] 玩家被星尘莲传送之后得到"Promise"进度（这一条没有自动测试）。
  A player gets the advancement "Promise" after a Stardust Lotus teleports the player (this item has no automatic test).

### 4.17 通用符文和奖杯 / Universal runes and the Trophy（"通用符文、奖杯和进度"一节 / section "Universal runes, the Trophy and advancements"）

- [ ] 元灵符文可以放进合成台，代替配方里的水、火、地、风、春、夏、秋、冬符文；大罪符文可以代替魔力符文和七种原罪符文。它们不能互相代替。
  The Rune of Element can go into the crafting table in place of the runes of water, fire, earth, air, spring, summer, autumn and winter of a recipe; the Rune of Sin can replace the Rune of Mana and the seven sin runes. They cannot replace each other.
- [ ] 在符文祭坛上用元灵符文代替催化剂符文（例如做春之符文时的水之符文和火之符文），合成完成后元灵符文被退回。
  On the Runic Altar, a Rune of Element replaces a catalyst rune (for example the Rune of Water and the Rune of Fire for a Rune of Spring), and it is returned after the craft.
- [ ] 整合包里别的模组的配方也照常能用，日志里没有新的配方加载错误。
  The recipes of the other mods of a modpack also work as usual, and the log has no new recipe load errors.
- [ ] 完成全部主进度的玩家得到进度"Erase or Zero"和一个奖杯；在这次更新之前就完成了全部进度的玩家，登录后得到它。
  A player that completes all main advancements gets the advancement "Erase or Zero" and one Trophy; a player that completed all advancements before this update gets it after the login.

### 4.18 进度要求（可选的规则）/ The advancement requirement (optional rule)（"进度要求（可选）"一节 / section "The advancement requirement (optional)"）

这条规则默认关闭。要测试它，把配置文件 `config/extrabotany-common.toml` 里 `gaia` 一节的 `relicsNeedEgoDefeat` 改成 `true`。
The rule is off by default. To test it, set `relicsNeedEgoDefeat` in the section `gaia` of the configuration file `config/extrabotany-common.toml` to `true`.

- [ ] 规则关闭时一切和以前相同。
  With the rule off all is the same as before.
- [ ] 规则打开、没有击败本我的生存模式玩家：九件遗物拿在手里、穿上或者戴上之后立刻掉到地上，操作栏里有红色的提示，物品的提示文字里有一行红字。背包里的遗物不受影响。
  With the rule on, for a survival player that did not defeat the Ego: each of the nine relics drops immediately when it is held, worn or put into an accessory slot, the action bar shows a red message, and the tooltip of the item has a red line. A relic in the backpack is not affected.
- [ ] 击败本我之后，遗物可以正常使用，提示文字里的红字消失。创造模式的玩家始终不受影响。
  After the Ego is defeated the relics can be used as usual, and the red line of the tooltip goes away. A player in creative mode is never affected.

## 5. 需要你决定的事项 / Items for your decision

每一项都有一个现在的状态。你不回答，就保持现在的状态。上一轮的四项（哪个物品召唤哪个 Boss、仓库地址、《崩坏3》的注明、进度要求系统）你已经定了，不再列在这里。
Each item has a state now. With no answer from you, the state stays as it is. You already decided the four items of the last round (which item summons which boss, the repository address, the Honkai Impact 3rd note, the advancement requirement system), and they are no longer in this list.

| # | 事项 / Item | 现在的状态 / State now | 另一个选择 / The other option |
|---|---|---|---|
| 1 | 空之律者和本我："在场玩家比召唤时多就取消战斗"，旁观的创造模式玩家也算 / Herrscher of the Void and Ego: "more players than at the summon cancels the fight", spectators in creative mode included | 和原版相同。 / The same as the original. | 加一个配置开关。原版的四个配置开关也没有移植。 / Add a configuration switch. The four configuration switches of the original are also not ported. |
| 2 | 魔力缓存器的传输速度 / The transfer rate of the Mana Buffer | 1000 点每 tick（原版 1.16）。 / 1000 mana on each tick (original 1.16). | 400（原版 1.12 的默认值）。 / 400 (the default of the original 1.12). |
| 3 | 魔力机器不能含水 / Mana machines cannot be waterlogged | 不能含水；旧存档里的含水状态被忽略。 / They cannot; the waterlogged state of old saves is ignored. | 恢复含水。 / Make them waterloggable again. |
| 4 | 伐木花不再加载未加载的区块 / The Woodienia no longer loads chunks that are not loaded | 只砍已加载区块里的原木。 / It only breaks logs in loaded chunks. | 恢复旧行为（会让服务器同步加载区块）。 / The old behavior (which makes the server load chunks synchronously). |
| 5 | 本我的唱片 / The music disc of the Ego | 本我有 20% 的几率掉落。原版里这张唱片没有来源。 / The Ego drops it with a 20% chance. In the original this disc has no source. | 去掉掉落，只保留战斗音乐。 / Remove the drop and keep only the fight music. |
| 6 | 多人时本我的生命值 / The health of the Ego with more than one player | 600 x 玩家数，并且加满。原版没有加满：两名玩家时本我只有一半的生命值，一出现就进入地雷弹幕。我把它当作原版的缺陷，没有移植。 / 600 x the number of players, and full. The original does not fill it: with two players the Ego only has half of its health and starts the landmine barrage immediately. I think that this is a defect of the original, and did not port it. | 和原版完全相同（不加满）。 / Exactly as in the original (not full). |
| 7 | 通用符文在 JEI、EMI 和配方书里不显示成可选的原料 / JEI, EMI and the recipe book do not show the universal runes as possible ingredients | 不显示：这样发给客户端的配方数据和植物魔法的配方检查都不受影响。图鉴里有说明。 / Not shown: thus the recipe data that goes to the client and the recipe checks of Botania are not affected. The lexicon tells about it. | 显示出来（改动更大，对别的模组的配方有风险）。 / Show them (a larger change, with a risk for the recipes of other mods). |
| 8 | 重制版没有做完的内容：七个"欧证"翅膀模型、奖励袋内容的客户端显示、盖亚魂钢锤的"下落重击"范围击退 / Content that Reburn did not complete: the seven "rainbow" wing models, the client display of the reward bag content, the area knockback of the "fall smash" of the Gaia Hammer | 保持原样。 / As it is. | 做完，或者删掉没有用到的资源。 / Complete it, or remove the unused assets. |
| 9 | 犹大的誓约当作装饰品附加在饰品上 / The Judah Oath as a cosmetic item on an accessory | 没有移植。 / Not ported. | 移植。 / Port it. |
| 10 | 版本号 / The version | `2.0.0`。注意：按 NeoForge 比较版本的规则，`2.0.0` 排在 `2.0-scex.10-dev` 的前面。只有当别的模组声明了"要求 extrabotany 不低于某个 `2.0-scex` 版本"时，这才有影响。 / `2.0.0`. Note: by the version comparison rules of NeoForge, `2.0.0` is before `2.0-scex.10-dev`. This only has an effect when a different mod declares "extrabotany must be some `2.0-scex` version or later". | 用 `2.0.1` 或者 `2.1.0`，它们排在所有 `2.0-scex` 版本的后面。 / Use `2.0.1` or `2.1.0`, which are after all `2.0-scex` versions. |
| 11 | EMI 里没有石中姬、雪绒花和全知瑾的配方显示（JEI 里有石中姬的）/ EMI has no recipe display for the Stonesia, the Edelweiss and the Omniviolet (JEI has that of the Stonesia) | 保持原样。 / As it is. | 补上 EMI 和 JEI 的显示。 / Add the EMI and JEI displays. |

已经定下来的事项 / Items that are decided: 自然蕴息宝珠召唤本我、挑战券召唤盖亚守护者III；仓库是 `Rianfalltwilight-lab/scex-extrabotany`；图鉴里只有两个条目注明《崩坏3》；进度要求是默认关闭的配置项；遥测代码已经整个删除，这个模组不向任何服务器发送数据。
The Nature Orb summons the Ego and the Challenge Ticket summons Guardian of Gaia III; the repository is `Rianfalltwilight-lab/scex-extrabotany`; only two lexicon entries name Honkai Impact 3rd; the advancement requirement is a configuration option that is off by default; the telemetry code was fully removed, and this mod sends no data to a server.

没有改动、但是值得知道的设计（原版或重制版就是这样）在 `PORT_AUDIT.md` 第 12、13 批的"没有改动、但是值得知道的设计"里，例如：死之花和鲜血妖姬会伤害玩家和宠物；虚空万藏保持非默认形态时每秒消耗 10000 点魔力；水平相邻的两个魔力缓存器会互相吸取魔力。
The design that did not change but is good to know (the original or Reburn is like this) is in "Design that did not change, but is good to know" of batches 12 and 13 of `PORT_AUDIT.md`, for example: the Necrofleur and the Blood Enchantress hurt players and pets; the Void Archives use 10000 mana each second while they are in a form that is not the default; two Mana Buffers that are horizontally adjacent take mana from each other.