# ExtraBotany 全模组静态深度审计报告

审计日期：2026-10-25。结论对应本地源码，不代表公开发行版的最新版本。

**修复进展：原审计的 3 项 P1 与 23 项 P2 已按后续授权修复。P1 定向回归 46/46 通过，P2 扩展定向回归 208/208 通过，见第 13—14 节。用户追加指出的酒杯液体与透镜透明问题也已修复，并通过客户端复现对照和画面复核，见第 14.4 节。第 1—12 节保留修复前审计基线与行号；P3 和独立玩法待定项未据此宣称完成。未打包或发布。**

## 1. 基线、方法与结论

| 项目 | 基线 |
| --- | --- |
| 本地目录 | 仓库根目录 |
| 模组版本 | `2.0.0` |
| 目标环境 | Minecraft `1.21.1`；本地核对的 NeoForge 依赖为 `21.1.248`，不是据此收紧运行版本要求 |
| Git 提交 | `45801bfb7070b784f90c64ed6c962e4fc760623e` |
| 审查范围 | main 552 个 Java 文件 / 49,983 行；datagen 65 个 / 6,324 行；gametest 43 个 / 8,454 行；40 个 GameTest 类中的 211 个声明测试，另有性能场景生成器和两个客户端探针 |
| 执行约束 | 只读业务源码、测试、资源及依赖源码；未构建、未打包、未运行测试或启动游戏；唯一最终新增物为本报告 |

本轮完成包级清点、风险路径人工静态审查、全部现有测试方法审阅、资源引用交叉核对，以及选定经典上游机制的固定提交比对。下文的“确认”指代码和依赖实现能闭合触发链；不表示已经运行复现，也不把静态审查表述为形式化证明或所有环境下的百分之百保证。每个包的统计和全部测试方法索引附在文末；详细业务证据见第 8—10 节。

**未确认 P0。优先处理三类 P1：强制掉落失败吞装备、基座旧锤回调删除新物品、雷卡兰满魔力时生成有伤害闪电。** 前两项涉及永久物品丢失，第三项可能伤害玩家、宠物、掉落物并引火。发生条件分别列明；没有把所有缴械、所有锤击或所有闪电都归入该结论。

### 1.1 高风险清单

| 编号 | 严重性 | 已确认的触发条件与后果 | 详细证据 |
| --- | --- | --- | --- |
| BOSS-01 | P1（有条件） | 地雷/进度限制先清空装备槽；掉落实体加入或 Toss 事件被取消时装备不返还。普通 GaiaDisarm 已有成功后移除的保护 | 第 8 节 BOSS-01 |
| M-01 | P1（短时间窗口） | 自动基座仍缓存旧锤；玩家取下将损坏的锤并换入另一物品；旧锤的断裂回调清空当前展示框，删除新物品 | 第 10 节基座缓存条目 |
| M-02 | P1（天气/容量条件） | 雷卡兰雨天且冷却到期；满魔力导致转视觉闪电的路径提前返回，留下真实伤害/引火闪电 | 第 10 节雷卡兰条目 |

所有修复均为建议，未改动业务实现。取消生成的兼容场景是否在当前生产整包实际出现，本轮没有运行确认；这一限制不消除物品保全路径本身的缺陷。

### 1.2 用户重点假设的核对结果

| 重点 | 当前代码结论 |
| --- | --- |
| 背包满导致普通盖亚缴械吞物品 | 普通缴械直接落地，背包容量不是前提；复制 ItemStack 保留组件，成功加入世界才清槽。地雷/进度限制另有 P1。掉落物没有专门防虚空、自然消失或第三方清理保护 |
| C2S 每 tick 无限刷圣剑/遗物剑气 | 服务端核验持有物、归属与攻击冷却并重置冷却；未建立可重复无限生成链。First Fractal 的低魔力机制须按原版规则理解。没有无界实体生成的证据不等于完成恶意流量压测 |
| 魔力池每 tick 强制存盘/全服完整 NBT 风暴 | 未确认。机器已有脏标记和同步合并；setChanged 本身不是强制落盘定时器，区块更新也不能等同全服广播。大额加法已采用 long 和边界钳制 |
| 基座漏斗与玩家同 tick 一物两拿 | 常规服务器主线程路径未建立并发双取链；但发现独立的展示框缓存删除物品缺陷 |
| 自修让所有武器/魔弓无法使用 | 自修工具未覆盖默认重新装备动画，存在 P2 视觉/同步问题；Failnaught 不自动修复，且有继续使用判断，不能把它写成确定拉弓中断 |
| 神装无限叠血、永久飞行、无限反伤递归崩服 | 未建立无限叠加/永久飞行链。发现独立的 AllForOne 属性去重错误、永恒效果拦 /kill/虚空，以及盾反的有限深递归；后者不能描述为无限递归或已证实栈溢出 |
| 酒杯/鸡尾酒一律变无效果水瓶 | 正常 Botania brew 路径、保存和饮用已有实现及有效测试；另有无限酒换口味丢组件/洗绑定漏洞，见机器与酿造详审。Botania brew 不等于任意原版自定义 PotionContents 都受支持 |
| 所有渲染器都会 Not building! | 本轮未发现可闭合的交错写失效缓冲区链；关键实体均在切换 RenderType 前完成前一批顶点提交。未运行着色器/真实客户端兼容验收 |
| 测试全绿即证明所有上述安全 | 不成立。存在名实不符、空集合通过、未断言掉落保全、监听器/世界时间污染，以及截图恒真标记；本轮也没有运行测试 |

## 2. 中低风险与设计边界总览

详细行号、触发链、最小修复和测试缺口分别见业务详审；本表用于安排修复顺序，不把同一根因重复算作多个高危缺陷。

| 领域 | 已确认问题/明确边界 |
| --- | --- |
| Boss | P2：空之律者低血读档回血、单物品白名单在战斗缴械遗漏、受击传送延迟被覆盖、取消实体生成仍扣召唤成本、取消战斗留下能伤人的地雷。P3：legacy Gaia RAID 记忆过早过期、空之律者速杀计时不持久化 |
| 装备与饰品 | 自修重新装备动画；Failnaught 多重射击计费与蓄力伤害断点；AllForOne 按 ID 而非属性+ID 去重；冰霜饰品的区块/方块改写边界；血嗜计数离线清理；诅咒戒缺 PvP 过滤；取消坐骑生成仍消耗物品；永恒与防死物品对 /kill/虚空的拦截；盾反有限深递归。具体严重性和前提见第 9 节 |
| 机器、花、酿造 | 邻接方块查询和 64/80 格定位射线可触发区块加载；双子花冷端初始化为 0；无限酒换口味重建结果导致组件/绑定丢失；相邻商友兰互相清除折扣。其他细项、原版差异和未证实假设见第 10 节 |
| 兼容保护 | Boss 直接破坏方块、某些花/饰品直接改写世界，没有自动获得玩家破坏事件的授权语义；真伤直接 setHealth 也不自动尊重普通受伤取消。原版已有类似机制，需明确整包契约 |
| 客户端与资料 | P2：EMI 自定义配方注册不完整。P3：BGM 缺少本地参战状态停止条件；日文缺 681 个键；测试及探针存在以下独立问题 |

## 3. 客户端、手册、数据与本地化

### CLIENT-01 · P2：EMI 只注册踏板实例，其他专属展示缺失

位置：[src/main/java/io/github/lounode/extrabotany/client/integration/emi/EmiExtrabotanyPlugin.java:72–90](../src/main/java/io/github/lounode/extrabotany/client/integration/emi/EmiExtrabotanyPlugin.java:72)。注册了 STONESIA 分类和普通/浮空工作台，但唯一添加配方的循环是 PEDESTAL_SMASH；三个酒类仅设组件比较器，没有像 JEI 一样建立 brew 展示配方。因此启用本模组直接 EMI 集成时，Stonesia（石中姬）专属分类没有实际条目，酒类特殊配方也缺少本模组的专用展示。其他桥接插件可能额外补条目，不能据此说任何整包都绝对看不到普通合成配方。

对照 [src/main/java/io/github/lounode/extrabotany/client/integration/jei/JEIExtraBotanyPlugin.java:68–78,154–184](../src/main/java/io/github/lounode/extrabotany/client/integration/jei/JEIExtraBotanyPlugin.java:68)：JEI 确实注册踏板和 Stonesia，并为 Botania brew 创建鸡尾酒/酒杯/圣水手雷展示。最小修复：为 EMI 补现有配方类型和现有 brew 结果的等价适配；不要另造平行业务配方。验收核对分类实际条目和组件不同的结果，不仅检查分类存在。

### CLIENT-02 · P3：BGM 只跟随 Boss 存活，不跟随本地玩家参战状态

位置：[src/main/java/io/github/lounode/extrabotany/client/sound/GaiaMusic.java:21–39](../src/main/java/io/github/lounode/extrabotany/client/sound/GaiaMusic.java:21)；[src/main/java/io/github/lounode/extrabotany/client/network/ClientPacketHandlers.java:41–49](../src/main/java/io/github/lounode/extrabotany/client/network/ClientPacketHandlers.java:41)。声音循环播放，tick 仅以 guardian.isAlive 为停止条件。多人战斗仍继续时，本地玩家死亡停留死亡画面，或离开战斗半径但仍追踪该实体，音乐可以继续播放；声音衰减不等于结束循环实例。

Boss 死亡/移除后会停止，不能描述为 Boss 删除后永久循环。客户端 Boss 条已有登出 reset，也不能误报完全不清理。最小修复：明确参与者、死亡/离场和换维度规则，在声音 tick 或明确离场通知中停止；避免一律按距离终止旁观者音乐，规则见 D4。

### CLIENT-03 · P3：日文本地化不完整

[src/main/resources/assets/extrabotany/lang/en_us.json](../src/main/resources/assets/extrabotany/lang/en_us.json) 和 [zh_cn.json](../src/main/resources/assets/extrabotany/lang/zh_cn.json) 均为 1,104 键，键集合相同；[ja_jp.json](../src/main/resources/assets/extrabotany/lang/ja_jp.json) 为 423 键，缺 681 键，且部分已有值仍为英文。未发现日文专有的多余键。当前状态会回退英文，通常不是运行崩溃，但不能宣称日文完整。最小修复：先补新 Boss/装备和关键操作说明，再补其余键；以语言键集合差异作为资料验收。

本工程普通花盆映射和浮空变体注册已有实现；在本轮检索的源码、资源与构建声明中未发现 BotanyPots 专用集成或配方。因此不能把普通花盆/浮空正常注册写成 BotanyPots 功能已经实现。整包外部数据包可能补联动，本轮不覆盖未提供的外部包。

### 3.1 渲染审查结果

审查了方块实体、Boss、投射物、武器、门/领域、翅膀与光环的 RenderType/VertexConsumer 生命周期、PoseStack 分支，以及客户端注册和模型入口。FlowerWeapon 与 SwordDomain 均先完整画物品再取门材质缓冲；VoidHerrscher 的翅膀提交完成后才切盾；KeyOfTruth 的模型与光束也是顺序提交。关键提前 return 位于 pushPose 之前，检查范围内栈配对成立。没有为迎合问题清单制造 Not building! 或 PoseStack 泄漏结论。

本地 Minecraft MultiBufferSource 的非固定缓冲切换确实会结束旧批次，所以仍应保留真实缓冲器回归；伪造一个永不关闭的 VertexConsumer 只能测几何，不能验证该异常。静态大型模型坐标、第三方 shader 和每个粒子时相没有进行视觉运行验收。

### 3.2 资源和手册交叉核对

- main/generated 合计 1,941 个资源文件；1,506 个 JSON 全部解析成功，没有重复 JSON 键，没有这两个源集间的同路径资源覆盖。
- 核对 270 个方块模型引用、448 个本模组纹理引用、51 个本模组父模型引用，没有发现对应本地资源缺失。
- en_us 手册目录共 118 个条目；172 个 Patchouli 配方引用中的本模组引用均能找到资源；本模组内部章节链接未发现悬空目标。
- 中文/日文没有复制整套条目目录，不应直接当作缺失手册；现有条目使用翻译键。中英键集合一致不等于每个翻译语义正确；外部模组引用、多方块状态属性、运行时模板/页面加载仍需具体环境验收。
- 容器菜单、配方序列化、DataComponents、注册、datagen、标签、战利品与 advancement 入口已纳入静态扫描；没有将“JSON 能解析”当成“游戏内全部语义有效”。

### 3.3 点名花朵的补充核对

**AnnoyingFlower 的 LootContext 不缺必需参数。** [src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/AnnoyingFlowerBlockEntity.java:178–201](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/AnnoyingFlowerBlockEntity.java:178) 提供 ORIGIN 和 TOOL，正是原版 FISHING 参数集的必需项；THIS_ENTITY 等为可选。不能报告“没有 FishingHook 就必崩”。但普通 FISHING 表的宝藏分支要求 THIS_ENTITY 是满足 openWater 条件的浮标，当前没有该实体，普通模式无法获得该分支；吃鸡加速直接使用 TREASURE 子表。若设计就是吃鸡才出宝藏，应在说明中写清；若承诺完整原版钓鱼概率，则需补齐该语义。只取 loot[0] 在原版单 roll 下正常，外部数据包/全局战利品修改器生成多栈时其余结果未被处理，属于兼容边界。

**Tinkle 每 20 个 flower tick 采样一次。** [src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/TinkleBlockEntity.java:49–83](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/TinkleBlockEntity.java:49) 在服务端、无红石、未满魔力时查询真实玩家；读取的是采样瞬间位移/速度，并非累计这 20 tick 的路程。瞬间停步、采样相位及长度/长度平方混用会影响收益。它不是每 tick 范围扫描，是否应改成路程累计需按 D5 确认玩法。

**Bellflower 的空间检查仍有可省成本。** [src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/BellflowerBlockEntity.java:90–99,141–179](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/BellflowerBlockEntity.java:90) 每 5 个 flower tick 进行空间检查，默认半径参数下是 5×3×5−1＝74 个方块位置，另有蒲公英方块实体读取，满魔力没有提前停止。此为确定调用量，不是已实测 TPS 损失。可先在满槽无其他必要副作用时跳过产能扫描，再根据实际阵列规模评估错峰/缓存；蕴魔土会加速 flower tick，不能与世界 tick 恒等。

### CHUNK-01 · P2：长射线定位可同步读取未加载区块

位置：[src/main/java/io/github/lounode/extrabotany/common/item/relic/FirstFractalItem.java:109](../src/main/java/io/github/lounode/extrabotany/common/item/relic/FirstFractalItem.java:109) 经 [RelicSwordItem.java:105](../src/main/java/io/github/lounode/extrabotany/common/item/relic/RelicSwordItem.java:105) 使用 user.pick(80)；其他剑的 64 格定位与 [FlowerWeaponEntity.java:203–206](../src/main/java/io/github/lounode/extrabotany/common/entity/projectile/FlowerWeaponEntity.java:203) 的 64 格 clip 有同类调用。当前没有先裁剪到已加载区块范围。

本地依赖链：Entity.pick → Level.clip/BlockGetter.getBlockState → Level.getChunk(FULL) → LevelReader 的 create=true → ServerChunkCache 的票据/任务及 managedBlock/join。玩家瞄准射线穿出当前已加载 FULL 区块、途中没有先撞到方块时，可同步请求并等待邻近区块。施放时调用不受“远端投射物尚未 tick”保护。范围固定 64/80 格，不是无限扩展或必然卡死；是否出现尖峰取决于加载视距与地形。

First Fractal 当前定位并移动的是幻影剑实体，没有玩家瞬移功能，不能按“玩家瞬移斩”假设报告穿墙漏洞。最小修复是为定位射线限制已加载范围/使用不会加载的访问器，保留已加载地形的既有命中语义。相关测试应把射线末端放到真正未加载的区块；目前普通技能测试只在已加载夹具内验证。


Failnaught 的 MagicArrow 没有独立 isLoaded 检查，其继承的弹射物 tick 会 clip；但原版只 tick 处于实体 ticking 范围的实体，普通魔箭约 1.4 格/tick，离开范围后停止。当前不足以闭合“普通魔箭持续加载无人区”触发链，不能把 CHUNK-01 扩大为无限远箭强载。幻影剑在目标附近生成，setPos/addFreshEntity 本身也不证明请求区块加载。

## 4. 测试集审阅

本轮逐一审阅 211 个声明 GameTest 的目标、断言、夹具与清理；`src/test` 没有单元测试源码。依赖中声明 JUnit 不能视为已有单元测试覆盖。PerfScenarios 和两个客户端探针独立于这 211 项。下面优先列会造成误判或污染的具体问题；全部测试方法索引见附录。

### 4.1 假阳性、名实不符与过期探针

| 测试/位置 | 问题 | 最小纠正 |
| --- | --- | --- |
| [FullLegacyGameTests.java:48–56](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FullLegacyGameTests.java:48) 的 relicSwordOwnershipManaAndDuplicateAttack | 实际只向 King's Garden 装 20 朵花并保存/重读；完全不测剑、归属、魔力或重复攻击 | 改成准确名称；相关安全断言放到实际武器/网络场景 |
| RelicSwordGameTests.trueDamageCanKillAndIsHalvedForBosses | 实际对象都是 ZOMBIE，没有 Boss 和减半断言 | 加真实 Boss 对照及精确伤害差 |
| RelicItemGameTests.kingGardenSummonsWeaponsThatWaitForTheRelease | 后半段遍历查询结果断言状态，若武器提前消失，空集合也可通过 | 在各观察点断言非空/数量稳定，再断言状态 |
| EgoGameTests.egoLandmineTypes；EgoRequirementGameTests.theRuleDropsRelicsUntilTheEgoIsDefeated | 只验证原槽变空，物品直接丢失也通过 | 同时核对地上数量、组件、归属及取消时原槽保留 |
| VoidHerrscherGameTests.herrscherSaveDataRoundTrip | 复用对象且不核对低血 Health，新对象读档回血仍通过 | 非默认战斗状态保存后载入新对象，逐字段比较 |
| FullLegacyGameTests.allLegacyEntitiesConstructAndReload | 验证默认实例类型和 UUID，不验证阶段、生命、计时和主人恢复顺序 | 保留注册冒烟定位；另测确有行为风险的状态 |
| [UpstreamRenderProbe.java:160–161](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/UpstreamRenderProbe.java:160) | 异步截图刚提交就 check(true, captured)，截图失败也记成功 | 依据保存完成结果及文件存在/可读状态记录成功 |
| [UpstreamRenderProbe.java:74](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/UpstreamRenderProbe.java:74) | type→counter 的假缓冲器无法发现 RenderType 切换关闭旧 BufferBuilder | 计数测试保留其几何用途；真实缓冲渲染另验 |
| [ItemUseClientProbe.java:107–112](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ItemUseClientProbe.java:107) | 仍要求征服宝石提供移速、攻击属性和 Strength；当前 GemOfConquestItem:16–17 明确宝石不直接给属性 | 按当前已确认的原版契约改能量/能力断言；这是过期断言/潜在假失败，不是假阳性 |
| [PerfScenarios.java:190–213](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/PerfScenarios.java:190) | 量的是两次 onEachTick 回调的墙钟间隔，可能含调度等待；部分机器无输入处于空闲；无预算断言，最后无条件 succeed | 定位为诊断工具；若要性能验收，先确认真实吞吐负载，再测服务端 tick 工作与预算 |

Boss 条测试对广播钩子计数有价值，但不覆盖编码、真实收包、HUD 或登出；loot table 测试有实质断言，但不覆盖 Boss 死亡、多参战者实际落物、离线奖励和绑定。直接调用 onWornTick/onEquipped 的测试不能证明 Curios 生命周期和重连正确。此类是**覆盖边界**，不把所有单元式夹具都贬为无效测试。

### 4.2 跨测试污染

**TEST-01 · P2：Resoncund 夹具留下全局监听器。** [src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RobustnessGameTests.java:28–31,35–79](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RobustnessGameTests.java:28) 新建未放进区块的花后 setLevel，两个测试总计注册多个实例却从不 setRemoved。[src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/ResoncundBlockEntity.java:55–60,72–98](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/ResoncundBlockEntity.java:55) 虽使用 WeakHashMap，但静态 snapshot 是强引用；未挂入世界的对象也没有正常区块移除回调。后续声音事件会继续遍历这些夹具，且持有 ServerLevel。最小修复是在夹具 finally/测试清理中逐一 setRemoved，优先实际放置需要世界生命周期的实例。生产正常方块的 setRemoved/clearRemoved 已有注册解绑，不将测试缺陷泛化为所有真实花都泄漏。

**TEST-02 · P2：测试直接修改共享世界时间且不恢复。** [src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ExtraBotanyGameTests.java:113–128](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ExtraBotanyGameTests.java:113) 把 dayTime 改为 1000，延迟 3 tick 完成后未恢复。可能影响同一 ServerLevel 的日夜花、燃料和夜间加成测试。即使加 finally 恢复也不能解决真正并行场景；时间敏感测试应串行独立 batch 或可注入局部时钟。其他测试各自恢复时间不能补偿这个异步夹具的修改。

### 4.3 应补的最小回归顺序

1. P1：地雷和进度限制的取消掉落物品守恒；展示框换物后旧锤损坏；雷卡兰满魔力时周围玩家/物品/可燃方块不受伤。
2. P2：新实例载入低血 Boss；Boss 取消的地雷清理；召唤失败不扣成本；无限酒换口味的全部组件与所有者；同 ID 不同属性的饰品模拟。
3. 客户端：持续自修时实际手持动画；真实缓冲器渲染；本地玩家死亡/离场 BGM；EMI 分类有实际条目。
4. 多人/生命周期：击杀奖励逐人分配、离线/跨维规则、实际 Curios 穿脱/死亡/重连、区块边界访问。仅在用户决定相应规则并实施相关修复后运行受影响验证，不以本报告为由启动全套重测试。

### 4.4 通用与兼容测试的其余覆盖（12 类、39 项）

| 测试类 | 数量 | 实际断言与审阅结论 |
| --- | ---: | --- |
| AdvancementGameTests | 3 | 实际 advancement 存在、稀有奖励授予、集齐主进度给奖杯；模拟服务端玩家不替代连接/重连验收 |
| ExtraBotanyGameTests | 5 | 折扣、旧组件保存、腐化边界正反对照、注册和蕴魔土加速；时间污染见 TEST-02 |
| FullLegacyGameTests | 5 | 实体注册/身份往返、历史物品、有限抽奖池恢复、饮料/透镜；名称误导与状态覆盖不足已单列 |
| UpstreamParityGameTests | 4 | 矛扇形朝向、时间锁运动、创造目标不掉血、效果 Holder 序列化；扇形仅固定俯仰条件，不能代表全部三维视角 |
| LexiconGameTests | 1 | en_us 条目数量及若干物品/配方/中英键/多方块 ID；跳过非原始 item 字段，未验证状态属性、日文和运行时页面 |
| ConfirmedLegacyItemGameTests | 3 | 实际饮酒效果、实际符文配方、唱片加载/播放/保存/弹出；断言有可观察结果 |
| LegacyAccessoryParityGameTests | 4 | 核心所有者、足魔力、冷却/保存、扩散戒范围及总额/优先级；直接 worn 回调不代表完整 Curios 生命周期 |
| LegacyCandyGameTests | 2 | 部署堆 NBT 往返/打开、食物值与季节规则；没有泛化为所有本地化/动画验收 |
| UniversalRuneGameTests | 3 | 符文 ingredient、真实合成、符文祭坛催化剂，含拒绝错误材料；重复 getStackingIds 的对象身份比较有缓存契约意义，不算恒真伪断言 |
| StructureGameTests | 3 | 两级祭坛及 Manalink 结构正例/破坏反例；不覆盖所有外部保护/卸载时序 |
| RobustnessGameTests | 3 | 计数溢出、非法声音保存、莱茵金输入/槽拒绝有实质断言；裸 Resoncund 监听器清理遗漏见 TEST-01 |
| ScexAuditGameTests | 3 | custom_data 只读身份与防御返回、无模拟器时查询无副作用、花移除/恢复监听；有主动清理，不能与 Robustness 的裸对象混同 |

## 5. 性能与架构建议

| 优先级 | 建议 | 理由与边界 |
| --- | --- | --- |
| 必要 | 在邻接访问前统一检查区块已加载 | 修正已确认的同步加载路径；FE/流体 BlockCapabilityCache 已检查加载，勿重复包一层复杂框架 |
| 必要 | 物品转移以成功提交后移除原件为共用小工具/既有入口 | 普通缴械已给出可复用实现；修复多个独立丢物入口即可，不需引入通用事务系统 |
| 必要 | 把战斗伤害限幅与存档恢复分开，把战斗取消接入明确清理出口 | 修复错误职责交叉；区块卸载和永久移除必须区分 |
| 必要 | 缓存保存身份和失效条件，使用前核对当前对象 | 基座缓存的是可变 ItemStack；20 tick TTL 不能代替引用有效性 |
| 保持 | 保留现有 long 钳制、机器同步合并、空闲 charger 跳过与 recipe reload 失效路径 | 没有新证据时不要重写已经正确工作的热路径；setChanged 不能为了减少计数而随意删掉 |
| 有数据再做 | Resoncund 监听器按 Level 分组；HUD 魔力/Curios 汇总每客户端 tick 复用 | 目前声音桥遍历全体监听器，HUD 每渲染帧扫描；可减少实际重复工作，但本轮未测得 TPS/帧耗时，不承诺收益数字 |
| 必要 | JEI/EMI 共享可复用的配方数据提取，适配器分别显示 | 当前双实现已有展示漂移；不合并两个不同 UI 框架 |
| 必要 | 测试按“注册冒烟、纯状态、实际世界行为、客户端探针”标明用途 | 防止冒烟通过被当作多人、网络、性能、保护验收 |

## 6. 需要决定的规则（不阻碍本次审计完成）

- **D1：缴械物品保护。** 只保证成功生成后清槽，还是还要求防自然消失、防虚空、死亡回收/物主专属拾取？当前普通掉落实体不具备全部增强保护。建议先修确定丢失，再单独确定长期回收规则。
- **D2：真伤与保护的优先级。** 真伤应否无视常规护盾、第三方伤害取消？/kill 与虚空是否始终绕过永恒、防死物品？建议给管理/世界边界伤害明确豁免，不用 hurt 返回 false 同时代表保护取消和无敌帧。
- **D3：世界改写授权。** Boss、冰霜饰品、产能/功能花是否统一尊重 mobGriefing、实体改块事件及整包领地 API？经典原版常直接改块；不能未经决定把所有原版效果静默禁用。
- **D4：战斗参与、奖励与音乐。** 按实际攻击者还是最初召唤人数发奖？离线/跨维玩家是否保留待领？本地死亡、离场或旁观时音乐应何时停止？当前实现只向可解析的在线同维攻击者结算，音乐主要跟 Boss 存活。
- **D5：原版数值选择。** 采用哪个经典分支的 Failnaught 蓄力/多重射击、双子温差、雷卡兰安全闪电规则？AnnoyingFlower 是否只有加速状态才出宝藏，Tinkle 是否按累计路程产能？1.12 与 1.16 不应拼成一个不存在的统一上游；确定偏差的部分见详审。
- **D6：酒类组件契约。** 明确 Botania brew、原版 PotionContents、自定义效果的支持范围；任何换口味配方都应保持既有绑定/自定义名称等与配方无关的组件，除非有明确洗绑定机制。
- **D7：客户端和语言支持。** EMI 是否承诺与 JEI 同等专属配方展示？日文是否列为完整支持语言？确定后按列表补齐。

上述为后续产品规则，不是要求用户再批准本次只读审计，也不是已经授权修改/发布。

## 7. 上游和依赖依据

用户给出的经典项目在 GitHub 实际名为 `ExtraMeteorP/Extra-Botany`。本轮通过 GitHub 连接器读取固定提交相关源码；本地没有经典源码缓存可复用，未下载完整仓库或新依赖。

- [经典 master / 1.12 基线 4ad2cc9](https://github.com/ExtraMeteorP/Extra-Botany/tree/4ad2cc9b86620801409c17366511156a3ba1b45e)
- [经典 1.16 基线 f96f6d3](https://github.com/ExtraMeteorP/Extra-Botany/tree/f96f6d37c43d807e93a27d2dbe58c4c5efec8280)
- [Reburn 基线 a4d4f2a](https://github.com/Lounode/ExtraBotany/tree/a4d4f2a968d559752fa3bd6e609544473109d983)

当前迁移层、Reburn 新增内容和经典机制分别对照，历史审计文档仅作定位线索。本地依赖从 [build/moddev/artifacts/neoforge-21.1.248-sources.jar](../build/moddev/artifacts/neoforge-21.1.248-sources.jar) 内存读取；Botania 源码参考位于 `D:\Minecraft-Projects\shared-references\Botania-1.21.1`。没有把历史测试报告当作本轮通过证据。



## 8. Boss、场地与伤害详审

审计日期：2026-10-25。源码基线：`45801bfb7070b784f90c64ed6c962e4fc760623e`，项目版本 `2.0.0`。本分报告只读业务源码、测试、资源和本地依赖源码，没有构建、打包、启动游戏或修改业务代码。结论是静态审查结果，不是本轮运行验收。

### 结论与证据标准

确认 1 类 P1 和 5 类 P2 问题：强制丢物品的失败路径会丢失装备；空之律者读档会错误回血；战斗缴械漏掉既有单物品白名单；盖亚受击传送延迟被旧值覆盖；取消 Boss 生成仍扣召唤成本；取消战斗未清掉仍能伤人的地雷。另列 2 项低优先级计时问题及兼容边界。本范围未确认 P0；强制掉落失败按永久装备丢失影响统一列 P1。

“强”表示项目代码和本地 Minecraft/NeoForge 实现能闭合触发链；它不表示已经运行复现。涉及第三方取消事件的条目明确以相应事件被取消为条件，不声称当前整包一定安装了会取消该事件的模组。

下列路径均相对于 仓库根目录。行号按上述基线；同一项中列出的多个位置属于同一触发链。

### 确认问题

#### BOSS-01 · P1（有条件）：地雷和进度限制先移除装备，掉落取消后无法返还

**位置**：

- [src/main/java/io/github/lounode/extrabotany/common/entity/EgoLandmineEntity.java:119–123](../src/main/java/io/github/lounode/extrabotany/common/entity/EgoLandmineEntity.java:119)
- [src/main/java/io/github/lounode/extrabotany/common/entity/SkullLandMineEntity.java:149–152](../src/main/java/io/github/lounode/extrabotany/common/entity/SkullLandMineEntity.java:149)
- [src/main/java/io/github/lounode/extrabotany/common/handler/EgoRequirementHandler.java:124–141,159–161](../src/main/java/io/github/lounode/extrabotany/common/handler/EgoRequirementHandler.java:124)

**触发链**：玩家被绿色地雷命中，或者开启 `relicsNeedEgoDefeat` 后未完成进度却装备受限遗物；代码先把原槽位置空，再调用 `player.drop`，不检查结果。若另一个监听器取消对应物品的 `EntityJoinLevelEvent`，原物品已经从玩家身上移除，地面上又没有成功生成物品。进度限制使用二参数 `drop`，还会经过可取消的 `ItemTossEvent`，同样没有返还。

本地依赖 [build/moddev/artifacts/neoforge-21.1.248-sources.jar](../build/moddev/artifacts/neoforge-21.1.248-sources.jar) 已核对：`ServerPlayer.java:1853–1870` 的三参数 `drop` 忽略 `level.addFreshEntity` 返回值，仍返回 ItemEntity；`Player.java:703–704` 的二参数重载调用 `CommonHooks.onPlayerTossEvent`；`CommonHooks.java:413–427` 在 Toss 取消时返回 `null`，没有代替调用者恢复槽位。因此仅给现有三参数调用补上“返回值非空”检查仍不足以保全物品。

**证据强度**：强，条件式确定。普通主手、装甲和 Curios 的进度限制入口同源；`relicsNeedEgoDefeat` 默认关闭，不夸大默认影响。

**最小修复**：复用普通缴械已采用的“复制栈→尝试生成物品实体→确认成功后清空原槽”的顺序；如需保留 Toss 事件语义，集中处理取消和实体未加入世界时的原槽返还。保留原物品组件、数量及 Curios 正常卸下回调。

**测试缺口**：[EgoGameTests.java:284–292](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/EgoGameTests.java:284) 中绿色地雷只断言主手为空，注释明确承认模拟玩家的掉落未进入世界。[EgoRequirementGameTests.java:112–116](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/EgoRequirementGameTests.java:112) 同样只断言手/头/饰品槽为空。这两处测试在“物品被删除”时也会成功，不能证明掉落保全。需同时断言成功掉落数量、组件与原槽状态，并覆盖取消生成/Toss。

**已排除**：[GaiaDisarm.java:73–80](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaDisarm.java:73) 的普通缴械先 `addFreshEntity`，只有成功才清空槽位；对应 `cancelledDropKeepsOriginalAndConfigDisablesDisarm` 确实注册了取消事件并断言原栈保留。不能把此问题扩大为所有缴械入口均损坏。

#### BOSS-02 · P2：空之律者新实例载入存档时，低血量被限幅到至少 375

**位置**：[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:829–830,917–918](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:829)；[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Gaia.java:102,604–605](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Gaia.java:102)。

**触发链**：单人空之律者已降至 100 HP→正常存档→卸载后构造新实例→构造链把生命值设为默认最大值 400→`readAdditionalSaveData` 进入 `LivingEntity`→虚派发 `setHealth(100)`→子类执行 `max(100,400-25)`→实际载入 375 HP。护盾、阶段和技能冷却却仍按原存档恢复，造成阶段状态与血量不一致。

依赖源码 `LivingEntity.java:729–748` 先读取属性，再直接调用虚方法 `setHealth(Health)`，证实读档不是绕过该重写方法的内部赋值。多人最大生命值由属性另行加载，因此不要把所有多人场景简单表述为“恢复到最大值减 25”；400→375 的单人例子已足以确定缺陷。

**证据强度**：强。经典 master 的 `EntityVoidHerrscher.java:160–161` 也存在同类 `setHealth` 限幅，但继承历史行为不能使当前读档结果正确。

**最小修复**：把“每次受伤下降不超过 25”的规则限定到战斗伤害处理，或在读取存档生命值时明确绕过该限幅。正常读档应恢复保存值；不要通过重复调用 `setHealth` 降血来掩盖问题。

**测试缺口**：[VoidHerrscherGameTests.java:100–136](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/VoidHerrscherGameTests.java:100) 复用满血对象并只核对自定义键，没有设置/核对 `Health`，也没有把低血存档载入另一个新对象。应以低血量、已有阶段/护盾的实例另建对象往返，断言全部关键状态。

#### BOSS-03 · P2：战斗缴械遗漏已有的单物品豁免

**位置**：[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaDisarm.java:73–75](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaDisarm.java:73)；[src/main/java/io/github/lounode/extrabotany/api/gaia/GaiaArena.java:509–528](../src/main/java/io/github/lounode/extrabotany/api/gaia/GaiaArena.java:509)；[src/main/java/io/github/lounode/extrabotany/forge/ForgeExtrabotanyConfig.java:227–233](../src/main/java/io/github/lounode/extrabotany/forge/ForgeExtrabotanyConfig.java:227)。

**触发链**：`checkGuardianAllowed` 明确允许 `enigmaticlegacy:cursed_ring`、`enigmaticlegacyplus:cursed_ring` 和若干 `celestial_artifacts` 单物品；Ego/空之律者召唤库存检查使用该函数。战斗中的 `dropForbidden` 却只检查配置豁免和四个命名空间，默认配置中也只有这四个命名空间。因而佩戴上述明确允许的第三方物品可以通过召唤检查，进入战斗后仍会被缴械。盖亚 III 使用同一缴械行为，也遗漏这些单物品豁免。

**证据强度**：强，发生条件是相应第三方物品存在且配置未另行重复豁免。没有推断这些第三方物品一定在当前实例中安装。

**最小修复**：战斗缴械以 `checkGuardianAllowed(stack) || checkFeasibility(stack)` 为允许条件，保持召唤和战斗使用同一基础白名单。不要把整个第三方命名空间加入白名单来代替现有精确豁免。

**测试缺口**：当前 GaiaDisarm 测试只有 `patchouli:guide_book` 禁止样例与原版胸甲允许样例，没有测试任一显式单物品豁免，也没有检测“召唤允许，战斗收走”的跨入口一致性。

#### BOSS-04 · P2：受击后设为 4 tick 的传送倒计时立即被旧值覆盖

**位置**：[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaTeleport.java:55–68,143–146](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaTeleport.java:55)。

**触发链**：当前传送记忆为 30 且出现 `HURT_BY`→`tick` 先把 30 读进局部变量 `delay`→`resolveHurt` 把记忆设为 4 并抹除受击记忆→后续仍使用局部值，写回 29。剩余时间为 0 时也会直接立即传送，而不是采用新设置的延时。受击响应不能执行注释和代码明确表达的 4 tick 规则，影响 Gaia 与 Gaia III 共用行为。

**证据强度**：强，纯顺序赋值即可确认；不依赖实际帧率或网络。

**最小修复**：在处理受击之后再读取 `delay`，或让 `resolveHurt` 同时更新随后计算使用的局部值；确定该 tick 是否消耗一个倒计时并保持一致。

**测试缺口**：相关 Boss 测试没有直接覆盖 GaiaTeleport 的“已有倒计时 + 新受击记忆”分支。Ego/空之律者使用自己的传送逻辑，其测试通过不能替代此行为的验证。

#### BOSS-05 · P2：实体生成被取消时，召唤入口仍报告成功并扣除成本

**位置**：

- [src/main/java/io/github/lounode/extrabotany/common/entity/gaia/GaiaIII.java:106–115](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/GaiaIII.java:106)
- [src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Ego.java:198–207](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Ego.java:198)
- [src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:246–255](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:246)
- [src/main/java/io/github/lounode/extrabotany/common/item/material/ChallangeTicketItem.java:31–33](../src/main/java/io/github/lounode/extrabotany/common/item/material/ChallangeTicketItem.java:31)
- [src/main/java/io/github/lounode/extrabotany/common/item/VoidCallerItem.java:28–32](../src/main/java/io/github/lounode/extrabotany/common/item/VoidCallerItem.java:28)
- [src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/NatureOrbItem.java:111–113](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/NatureOrbItem.java:111)

**触发链**：场地和库存检查通过→监听器取消对应 Boss 的 `EntityJoinLevelEvent`→`addFreshEntity` 返回 false→三个 spawn 方法忽略结果、触发后续效果并返回 true→物品调用者仍扣挑战券、虚空呼唤或 200000 自然之息，世界没有 Boss。

依赖源码 `PersistentEntitySectionManager.java:78–80` 对取消的加入事件返回 false；`ServerLevel.java:930–940` 传递加入失败。旧 `Gaia.spawn` 还在尝试加入实体前直接 `stack.shrink(1)`（[Gaia.java:141,170](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Gaia.java:141)），但未找到该静态入口的业务调用者，因此不把它作为默认玩家路径的主要影响。

**证据强度**：强，条件式确定。

**最小修复**：服务端仅在实际加入实体成功后返回 true、触发召唤成功进度和扣成本；客户端预测保持现有入口契约。无需建立新的召唤事务框架。

**测试缺口**：Ego 的完整仪式测试验证了不足能量、禁止物品、重复 Boss 和成功消耗；空之律者测试验证了潜行/无效场地；GaiaDisarm 的仪式测试验证了装备检查变化。它们均没有在合法场地下取消 Boss 加入并断言成本未动。

#### BOSS-06 · P2：战斗被取消后，遗留地雷仍能伤害玩家

**位置**：[src/main/java/io/github/lounode/extrabotany/api/gaia/GaiaArena.java:218–220,229–253](../src/main/java/io/github/lounode/extrabotany/api/gaia/GaiaArena.java:218)；[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Ego.java:370–381,529–548](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Ego.java:370)；[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:391–399](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:391)；[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Gaia.java:297–303,365–373](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/Gaia.java:297)。

**触发链**：Boss 已生成地雷→玩家人数超过召唤时人数，或玩家离场触发取消→只调用 `discard()`→没有经过 `die/kill` 的 arena cleanup。Ego 的 `remove` 只移除仆从，Ego 地雷仅在 `die` 中清理；Gaia 基类没有统一的永久移除清理。因此剩余地雷照常倒数，之后对场地中的玩家造成伤害、凋零或缴械。[EgoLandmineEntity.java:91–129](../src/main/java/io/github/lounode/extrabotany/common/entity/EgoLandmineEntity.java:91) 和 [MagicLandMineEntity.java:58–91](../src/main/java/io/github/lounode/extrabotany/common/entity/MagicLandMineEntity.java:58) 均未以主人是否仍活着作为爆炸前提。

**证据强度**：强。尤其“另一名玩家进入导致人数违规”同时满足取消后仍有玩家留在场地，不需要构造“玩家离场却仍站在场地中”的矛盾条件。

**最小修复**：将取消/永久移除时的清理接入一个明确出口；Ego 同时清掉自己的地雷与仆从。区块卸载不等于战斗结束，不应在 `UNLOADED_TO_CHUNK` 时永久删除战斗状态。范围清理应避免误伤其他场地的实体。

**测试缺口**：`EgoGameTests.egoLeavesWhenMorePlayersJoin` 只等待 Boss `isRemoved`，没有先布置地雷并断言其被清理。`egoMinionRules` 确认仆从跟随消失，不涵盖地雷。单纯 Boss 消失的断言不能证明场地结束。

### 低优先级计时问题与需限定的结论

#### BOSS-07 · P3：legacy Gaia 的 RAID 资格会在尚未进入阶段时过期

[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/GaiaAI.java:103](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/GaiaAI.java:103) 在初始化时把 `MOB_SPAWN_TICKS` 同时设为 900 的数值和 900 tick TTL；`updateActivity:69–80` 要求该记忆仍存在才能进入 RAID。依赖 `Brain.java:402–423` 每个 AI tick 都对所有过期记忆倒计时，与当前活动无关。若 900 tick 后才降到 20% 血量，记忆已经被删除，阶段永久跳过。该行为属于注册的 legacy Gaia；未找到其正常物品召唤调用，故降低优先级并不宣称 Gaia III 受影响。最小修复是初始化非过期的剩余阶段计数，只在 RAID 中消耗；增加等待超过 900 tick 后再降血的断言。

#### BOSS-08 · P3：空之律者速杀判断使用不持久化的实体 tickCount

[src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:879](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/VoidHerrscher.java:879) 以 `tickCount <= 3600` 发放速杀进度；其存档方法 `896–940` 没有保存战斗年龄。`Entity.saveWithoutId/load` 没有持久化通用 `tickCount`；新实例从 0 开始。因此一次长战斗经过卸载重载后，速杀窗口会重新计算，90 秒后的诅咒强度计时也重置。这里不把所有短寿命视觉实体的年龄重置都列成独立缺陷。若速杀要求整场累计战斗时间，最小修复为保存明确的战斗 tick 计数，并用其判断成就及持续战斗阶段。

### 世界保护、伤害和卸载边界

1. **Boss 破坏方块没有接入通用领地保护。** [GaiaSmashBlocksAround.java:50–94](../src/main/java/io/github/lounode/extrabotany/common/entity/gaia/behavior/GaiaSmashBlocksAround.java:50) 直接 `destroyBlock`，只判断硬度、免疫标签、地板和水晶柱，未检查 `mobGriefing` 或实体破坏事件。依赖 `Level.destroyBlock` 本身不会补这些授权检查。已实际读取经典 1.16 `EntityEGO.java:507–523` 和 master 两个盖亚 Boss 的同类代码，原作也直接破坏方块。因此这是明确兼容边界，不能仅凭“不检查 gamerule”就声称移植新引入 P1。若整包要求领地/生物破坏保护，应据其明确契约接入对应检查。当前数据没有证明某个具体领地模组已被绕过。
2. **真伤和失去生命会直接 setHealth。** [DamageHandler.java:166–173,190–221](../src/main/java/io/github/lounode/extrabotany/common/handler/DamageHandler.java:166) 的直接扣血不以微量 `hurt` 是否被取消作为前提；保护模组取消普通伤害事件、Eternity/征服宝石取消微量受击，都不能由此保证剩余直接扣血也取消。它们有意实现直接扣血规则，报告将此列为交互边界，不擅自改变“真实伤害”的产品规则。具体哪些取消应成为硬边界需有契约；不能简单以 `hurt` 返回 false 为准，因为无敌帧也会返回 false。
3. **全服 PvP 关闭并非已确认漏洞。** `DamageHandler.checkPassable` 调用来源玩家的 `canHarmPlayer`。本地 `ServerPlayer.java:790–796` 重写此方法并检查服务器 PvP 开关；所以不能误报它“只检查队伍友伤”。创造/旁观及玩家 invulnerable 能力在直接扣血之前被拒绝；`UpstreamParityGameTests.creativeTargetsCannotLoseHealthToLegacyTrueDamage` 对创造模式确有健康值断言。
4. **投射物穿墙与破坏世界应分开。** `TargetedProjectileEntity`/亚空矛通过空 `onHit` 保持穿透，未因穿透而修改方块。[FlowerWeaponEntity.java](../src/main/java/io/github/lounode/extrabotany/common/entity/projectile/FlowerWeaponEntity.java) 的爆炸显式使用 `ExplosionInteraction.NONE`，故未发现该入口破坏地形；这不等同于证明爆炸对玩家/宠物完全无害。
5. **卸载已有一部分正确处理。** `OwnedEntity` 保存 Owner UUID 并在旧引用移除后重新查找；`Ego.remove` 用 `reason.shouldDestroy()` 区分永久移除与卸载；`EgoMinionEntity` 在找不到 Boss 时等待 100 tick。没有把这些已经存在的处理报告成“主人完全不持久化”。矛枪/裂隙/数种投射物在主人暂不可解析时立即消失，尚未通过真实区块加载顺序验证长期战斗恢复，因此只说明恢复覆盖不足，不据此另立高优先级缺陷。
6. **不要误报提前 return 导致 tickCount 永不增长。** 已核对 `ServerLevel.java:766–775`，世界在调用 `Entity.tick()` 前自增 `tickCount`。`PhantomSwordEntity` 假残影和 `TrueShadowKatanaProjectileEntity` 等待期提前返回，仍会在正常世界 tick 中增长年龄；手工直接调用 `entity.tick()` 的夹具不能代表这一点。

### GameTest 的真实覆盖与假阳性名单

本节“假阳性”指测试可以在它名称/注释承诺的目标未实现时通过，不是声称本轮运行了测试或这些测试一定失败。

| 测试 | 实际验证 | 不应据此声称已验证 |
| --- | --- | --- |
| `GaiaDisarmGameTests.equippedItemsDropButStoredItemsRemain` | 手/装甲槽移除、背包保留、地上栈组件与数量、重复执行不增殖 | 真实 Curios 卸下能力/属性完整清理、跨重连物品保全 |
| `GaiaDisarmGameTests.cancelledDropKeepsOriginalAndConfigDisablesDisarm` | 普通缴械的取消加入事件保留原栈；配置关闭该入口 | 地雷或进度限制的丢物品也安全；这些入口实现不同 |
| `GaiaDisarmGameTests.summonWithForbiddenCurioAndCombatDisarm` | 真实场地检查和 spawn；手工写 NEAREST_PLAYERS 后手动调用行为 | 自然感知器范围、真实玩家加入/离开、长期战斗自动缴械链 |
| `EgoGameTests.egoLandmineTypes` | 蓝/红地雷伤害效果、绿色地雷清空主手 | **绿色地雷保全物品**；当前无掉落实体断言（BOSS-01） |
| `EgoRequirementGameTests.theRuleDropsRelicsUntilTheEgoIsDefeated` | 限制条件、槽位清空、创造/进度豁免 | **掉落数量和组件保全**；取消事件完全未覆盖 |
| `VoidHerrscherGameTests.herrscherSaveDataRoundTrip` | 同对象自定义存档键的回填 | **新对象低血量恢复**、实际卸载重载（BOSS-02） |
| `EgoGameTests.egoSaveDataRoundTrip` | 同对象 Stage/武器等键；旧 Gaia III 键不会导致当前读档异常 | 实际磁盘恢复、仆从加载先后、全部战斗状态继续 |
| `BossBarGameTests.grainTimePacketsOnlyWhenTheIntensityChanges` | 重写 broadcast 后计算 12 次操作、用同一接口公式比较噪点强度 | **网络编码/解码、真实客户端收包、Boss 条绘制、登出清理**；测试未经过这些链路 |
| `BossBarGameTests.skullLandmineExplodesAndIgnoresMobs` | 两次手动 tick 的 countdown、Husk 不受伤、地雷消失 | 玩家实际伤害、绿色地雷物品保全、客户端粒子同步 |
| `EgoGameTests.egoAndMinionsFireEveryWeapon` | 每种武器调用后存在至少一个非玩家/非 Boss 实体 | 投射物确实命中、各武器数值和长期清理正确 |
| `EgoGameTests.egoMinionPhaseAndLoot` / `VoidHerrscherGameTests.herrscherLootTable` | 直接调用 loot table 的随机生成及奖励类型 | **Boss 死亡→多名攻击者各一份→实际掉落实体→绑定归属**；未走该端到端链 |
| `EgoGameTests.egoLeavesWhenMorePlayersJoin` | 额外玩家进入后 Boss 被移除 | 场地召唤物已清理（BOSS-06） |
| `RelicSwordGameTests.trueDamageCanKillAndIsHalvedForBosses:219–229` | 两只 ZOMBIE 的致死与非致死真伤 | **Boss 真伤减半**；测试没有创建任何 Boss，也没有减半断言 |
| `HerrscherGameTests.gemChargesEnergyToTheMaximum` | 直接调用 onWornTick 的增能和封顶 | Curios 实际佩戴 tick/客户端同步；其余反击测试确实调用 hurt 并断言血量/能量，但没有经过网络发包 |

掉落结算源码本身使用 `playersWhoAttacked`，UUID 以 INT_ARRAY 正确读回，逐个在线同维度玩家临时设置击杀者/掉落位置再调用父类 loot。没有发现“所有 UUID 读档必然丢失”的旧问题仍存在。玩家离线或已不在该维度时会跳过奖励，这是当前规则边界；并不存在待领队列。现有测试不能证明多人实际掉落及绑定一致，建议把它列为后续最小端到端验收，而不是在静态审计中宣布完成。

### 覆盖清单与本轮未做事项

已实际阅读的业务源码：

- [api/gaia/GaiaArena.java](../src/main/java/io/github/lounode/extrabotany/api/gaia/GaiaArena.java)；`common/entity/gaia/{Gaia,GaiaIII,GaiaAI,GaiaIIIAI,ScriptedBossAI,Ego,VoidHerrscher}.java`。
- `common/entity/gaia/behavior/` 下全部 12 个行为实现（不含 package-info）：缴械、清效果、出场、破坏方块、传送、导弹、地雷、小精灵、召怪、Ego 武器分派。
- `common/entity/{OwnedEntity,EgoMinionEntity,EgoLandmineEntity,MagicLandMineEntity,SkullLandMineEntity,SkullMissileEntity,SubspaceLanceEntity,SwordDomainEntity,VoidFieldEntity}.java`；`EgoLandmineFormations` 的分派及生成入口。
- `common/entity/projectile/` 下全部 9 个实现，以及 `common/entity/subspace/{SubspaceEntity,SubspaceSpearEntity,SubspaceBursts}.java`；审查焦点为主人、生命周期、伤害及方块写入，不替代各遗物所有交互审查。
- `common/handler/{DamageHandler,HerrscherHandler,EgoRequirementHandler}.java`；[common/ExtraBotanyDamageTypes.java](../src/main/java/io/github/lounode/extrabotany/common/ExtraBotanyDamageTypes.java)；`common/bossevents/{ServerGaiaBossEvent,ServerColorfulBossEvent}.java`。
- 召唤成本调用：`ChallangeTicketItem`、`VoidCallerItem`、`NatureOrbItem` 相关方法；配置中的 guardian/disarm 默认值；Gaia III/Ego/VoidHerrscher 生成 loot 表。

已完整阅读重点测试：`GaiaDisarmGameTests`、`BossBarGameTests`、`EgoGameTests`、`VoidHerrscherGameTests`、`HerrscherGameTests`、`EgoRequirementGameTests`；定向读取 `RelicSwordGameTests` 真伤测试、`UpstreamParityGameTests` 创造模式测试，并检索整个 GameTest 目录中 PlayersAttacked/PlayerCount 相关断言。

本地依赖通过 ZIP 只读读取，没有解压新副本或下载依赖。参考条目为 `LivingEntity`、`Player`、`ServerPlayer`、`ServerLevel`、`Level`、`Entity`、`Brain`、`PersistentEntitySectionManager`、`CommonHooks`。报告没有把本地依赖构建版本误当成模组全部运行兼容范围。

经典上游仅用于核实继承的规则，未将历史报告作为当前缺陷证据：

- [1.16 EntityEGO 固定提交](https://github.com/ExtraMeteorP/Extra-Botany/blob/f96f6d37c43d807e93a27d2dbe58c4c5efec8280/src/main/java/com/meteor/extrabotany/common/entities/ego/EntityEGO.java)
- [master EntityGaiaIII 固定提交](https://github.com/ExtraMeteorP/Extra-Botany/blob/4ad2cc9b86620801409c17366511156a3ba1b45e/src/main/java/com/meteor/extrabotany/common/entity/gaia/EntityGaiaIII.java)
- [master EntityVoidHerrscher 固定提交](https://github.com/ExtraMeteorP/Extra-Botany/blob/4ad2cc9b86620801409c17366511156a3ba1b45e/src/main/java/com/meteor/extrabotany/common/entity/gaia/EntityVoidHerrscher.java)

本轮未审称已完成：真实联机血条/BGM播放、图形表现、生产整包领地保护联动、完整存档卸载恢复、长期压力和多人战利品端到端测试。后续修复应按上述具体触发链分别补最小测试，不必为本文重新跑全部既有验收。


## 9. 装备、网络与武器实体详审

审查日期：2026-10-25。基线：`45801bfb7070b784f90c64ed6c962e4fc760623e`。本报告只记录源码、依赖源码与测试源码审阅；没有启动游戏、构建、打包或修改业务源码。本节的“未发现”不能替代实际运行验收。

### 证据和范围

- 本地业务源码：仓库根目录。
- Minecraft/NeoForge 调用顺序：本项目 [build/moddev/artifacts/neoforge-21.1.248-sources.jar](../build/moddev/artifacts/neoforge-21.1.248-sources.jar)；Botania 父类：`D:\Minecraft-Projects\shared-references\Botania-1.21.1\Xplat\src\main\java`。
- 实际读取的经典上游：ExtraMeteorP/Extra-Botany 1.16 提交 `f96f6d37c43d807e93a27d2dbe58c4c5efec8280` 的 [ItemFailnaught.java](https://github.com/ExtraMeteorP/Extra-Botany/blob/f96f6d37c43d807e93a27d2dbe58c4c5efec8280/src/main/java/com/meteor/extrabotany/common/items/relic/ItemFailnaught.java)。其他装备的“当前功能完整性”与“经典一致性”分开判断；没有逐件读取经典实现的项目，不宣称已经完成逐项上游等价证明。
- 证据强度：**A** 为本地调用链及依赖源码可直接推导；**B** 为明确存在代码缺口，但用户感知程度或与外部模组组合后的结果仍需运行验证。
- 本范围未确认 P0；未确认可无条件定级的 P1。以下 P2 包括功能错误、兼容缺口及明确的管理行为决策，不能合并写成“全部都是崩服漏洞”。

### 可执行发现

#### EQ-01 / P2：泰拉钢盾反伤可以再次触发对方泰拉钢盾，形成深层同步重入

位置：[TerrasteelShieldItem.java:31](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/shield/TerrasteelShieldItem.java:31)、[ManasteelShieldItem.java:83](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/shield/ManasteelShieldItem.java:83)。证据 A。

`onShieldBlock` 把格挡伤害乘 0.5 后立即调用攻击者的 `hurt`；反伤源使用 `playerAttack`/`mobAttack`，没有独立反伤标记，也没有重入保护。两个允许 PvP 的玩家正面相对、都在用泰拉钢盾时，一支其中一人先前射出的箭被对方格挡，就能进入“格挡 → 反伤 → 对方格挡 → 再反伤”的链。

依赖中的 `LivingEntity.hurt` 在 1136 行发出盾牌事件，1162 行才检查无敌帧，1174 行才写入新的无敌时间。盾牌耐久扣除也位于事件返回之后，因此这些机制不能截断当前递归。`Player.hurt:919` 在伤害归零后返回，0.5 倍的浮点伤害最终会下溢为零，所以这是**可达的深层递归，不是数学上的无限递归**；普通有限伤害可产生约 150 层反伤。没有运行证据，不能断言必然 `StackOverflowError`。

最小修复：采用明确的反伤 DamageType/标签，并拒绝再次反射该类型；或用只包围这一反伤调用的 `try/finally` 重入保护。若改用原版 thorns，先确认其伤害标签及预期护甲语义。补两个真玩家、双盾、延时箭的测试，断言一次反射且事件次数有界。现有 `shieldEffectsNeedARealBlock` 仅直接投递单个事件，没有覆盖这条链。

#### EQ-02 / P2：精灵王戒模拟多属性饰品时，只保留共用 modifier ID 的第一种属性

位置：[AllForOneRingItem.java:294](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/AllForOneRingItem.java:294)。证据 A。

`applyModifiers` 把已应用项记在 `Map<ResourceLocation, Holder<Attribute>>` 中；生成 ID 只含物品 ID 与原 modifier ID。`!applied.containsKey(id)` 因而把不同 Attribute 上相同 ID 的合法修饰符视为重复。

这是现有物品可直接触发的路径：[AeroStoneItem.java:41](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/AeroStoneItem.java:41) 对移动速度和飞行速度使用同一 `slotId`；[TheCommunityItem.java:32](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/TheCommunityItem.java:32) 聚合四种属性；CoreOfTheVoid、SunRing 也有相同模式。精灵王戒模拟这些饰品时会漏掉后续属性，而不是无限叠加属性。

最小修复：用 `(Attribute, modifierId)` 记录应用和移除项。保留按物品种类去重和真实佩戴排除。测试应模拟 TheCommunity，逐项比较全部属性，并在盒中取出物品、卸戒、换维度后核对恢复。

#### EQ-03 / P2：魔弓多重射击一级生成三箭，收费却使用单箭分支

位置：[FailnaughtItem.java:89](../src/main/java/io/github/lounode/extrabotany/common/item/relic/FailnaughtItem.java:89)、同文件 133–138；虚空万藏的重复实现见 [variants/Failnaught.java:95](../src/main/java/io/github/lounode/extrabotany/common/item/relic/void_archives/variants/Failnaught.java:95)。证据 A。

预检和扣费使用 `multiShoutLevel > 1 ? 3 : 1`，生成箭数使用 `multiShoutLevel > 0 ? 3 : 1`。一级附魔因此生成三箭、仅按一箭收费。虚空万藏的魔弓模式存在相同条件分歧。另一个需要一起核定的规则是：当前 `manaInBurst` 使用乘过箭数的总额，并为每一箭都赋同样的值；不能仅改一级判断后就宣称“总魔力预算守恒”。

最小修复：先确定三箭的总收费及每箭携带魔力，使用同一个 `projectileCount` 计算预检、扣费、生成和分摊。测试应分别覆盖 0/1 级、多箭预算不足、正常充能及两种物品实现。当前装备测试没有这一断言。

#### EQ-04 / P2：魔弓跨蓄力档位时伤害骤降，越多蓄力反而越弱

位置：[FailnaughtItem.java:168](../src/main/java/io/github/lounode/extrabotany/common/item/relic/FailnaughtItem.java:168)、同文件 277–297；[variants/Failnaught.java:141](../src/main/java/io/github/lounode/extrabotany/common/item/relic/void_archives/variants/Failnaught.java:141)、同文件 160–180。证据 A。

伤害是 `baseDamage * (tier * tierProcess)`，而 `tierProcess` 在 0.35、0.7、0.9 三个新档起点重新从 0 开始。无 Power 时，0.35 前接近 10 点，到 0.35 变 0；0.7 前接近 20 点，到 0.7 又变 0。即使某个浮点步长没有精确落在阈值，阈值之后的低伤害区仍然存在。魔力收费公式则采用累计分档，并不会同步下降。

经典 1.16 `ItemFailnaught.onPlayerStoppedUsing` 使用随蓄力 rank 上升的伤害再封顶，没有当前的分档回零。当前差异不能以“原版也这样”解释。

最小修复：使用连续的累计分档伤害公式，或者经玩法确认后恢复经典的单调公式。补阈值两侧、满蓄力、Power 和虚空万藏模式的参数化断言。是否完全采用经典伤害与耗魔数值，是玩法决策；消除意外断崖不需要引入新系统。

#### EQ-05 / P2：自动修复和耐久冷却会触发默认重装备判定

位置：[RelicSwordItem.java:110](../src/main/java/io/github/lounode/extrabotany/common/item/relic/RelicSwordItem.java:110)、[ShadowKatanaItem.java:113](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/tool/ShadowKatanaItem.java:113)、[ManasteelHammerItem.java:42](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/tool/hammer/ManasteelHammerItem.java:42)、[ManasteelShieldItem.java:41](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/shield/ManasteelShieldItem.java:41)、[RodOfDiscordItem.java:70](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/tool/RodOfDiscordItem.java:70)。证据 A（判定链），B（实际视觉频率）。

受损装备在有魔力时逐 tick 写入新的耐久值。Excalibur 通过 Botania `ManasteelSwordItem.inventoryTick` 也走自修。当前工程没有 `shouldCauseReequipAnimation` 覆盖；NeoForge `IItemExtension:524–525` 默认比较旧新 stack，`ClientHooks:545–557` 调该方法，`ItemInHandRenderer:562、575–594` 再决定手中物品下降和重设。耐久组件同步导致旧新组件不同，因此会反复走重装备动画路径。

最小修复：仅为这些物品忽略耐久或明确的内部计数差异；物品类型、槽位和外观组件变化仍应触发动画。再用客户端持物画面验证连续修复期间的手部状态。

排除：Failnaught 当前 `inventoryTick` 只绑定，没有自修，`damageItem` 直接返回 0，不能说“魔弓因自修抖动”。NeoForge 同物品 `canContinueUsing` 默认允许继续使用，也不能把视觉下降直接写成“拉弓必被打断”。

#### EQ-06 / P2：冰霜戒冻结方块绕过常用放置事件，扫描未先排除未加载区块

位置：[FrostRingItem.java:95](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/FrostRingItem.java:95)、[SilentEternityItem.java:110](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/SilentEternityItem.java:110)。证据 A（直接读写），B（外部领地后果）。

`freezeFluid` 在范围内直接 `getBlockState`、`setBlockAndUpdate`、`scheduleTick`，没有 `hasChunkAt`/不加载查询，也没有交给可取消的玩家放置权限事件。戒指普通范围参数 6 会得到半径 8 的检查范围；SilentEternity 每 tick 调用其岩浆版本，参数 4 对应半径 6。玩家靠近领地边缘或加载边界时，源水/源岩浆可以进入这条修改链。

最小修复：先排除未加载位置，再走约定的玩家放置权限事件；取消时不写方块。对 SilentEternity 的岩浆扫描增加与移动/必要时间间隔对应的触发条件。领地模组是否拦截其他更底层 API 需要集成验证，不能声称已对所有保护模组复现越权。代码限定源流体，这不是任意方块或基岩破坏。

SilentEternity 的水面路径调用 `FrostWalkerHelper` 继承附魔效果；不要把它与自写岩浆路径混为同一实现。

#### EQ-07 / P2：诅咒戒直接施加凋零，没有 PvP 和友方过滤

位置：[CurseRingItem.java:28](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/CurseRingItem.java:28)。证据 A。

佩戴者每 30 tick 遍历 6 格范围，筛选只有“不是自己”和注视/视线条件；扣 50 魔力后直接施加 Wither II、Unluck II。没有调用 `DamageHandler.checkPassable`，也没有 `ServerPlayer.canHarmPlayer`。凋零后续造成的伤害源不携带施咒玩家，不能依赖玩家近战 PvP 检查补救。因此在关闭 PvP、同队禁止友伤等情况下，其他玩家仍可被施加这一负面效果。

同类目标选择不统一还见 FrostRing 的减速和 Camera 的 LINK/高亮：Camera 只做队伍判断，不检查全服 PvP。这里不把所有非伤害效果都扩大定性为攻击漏洞，但需要统一定义玩家和受保护生物的目标资格。

最小修复：在扣费前使用适合此技能的敌对目标过滤；普通 PvP 策略至少复用已存在的 `canHarmPlayer`。补关闭 PvP、同队、目标拒绝后不扣魔力的测试。现有 `BaubleGameTests` 的 FrostStar 测试不覆盖 CurseRing。

#### EQ-08 / P2（需要玩法确认）：ETERNITY 和薯片救命拦截了 /kill 与虚空伤害

位置：[NeoForgeEventBridge.java:115](../src/main/java/io/github/lounode/extrabotany/forge/event/NeoForgeEventBridge.java:115)、[PotatoChipsItem.java:46](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/PotatoChipsItem.java:46)。证据 A。

ETERNITY 分支只判断 Player 和效果存在，随后无条件取消 `LivingIncomingDamageEvent`。依赖源码 `LivingEntity.kill:293–294` 通过 `genericKill()` 调 `hurt(Float.MAX_VALUE)`，`onBelowWorld:1934–1935` 通过 `fellOutOfWorld()` 调 `hurt(4)`；`hurt:1124–1125` 发出的事件会被此分支取消。原版伤害的绕过无敌标签并不绕开这个自定义监听。

薯片在 `LivingDeathEvent` 中同样不排除伤害类型。足够魔力且不在冷却时，`/kill` 造成的死亡可以被取消并恢复到 5 生命；它会进入冷却，因此不是“永久无法杀死”。ETERNITY 的持续时间/刷新来源也要纳入场景，不能说任意玩家永久无敌。

最小方案：明确 `/kill` 是否必须保留管理处置能力，并明确虚空是否属于装备承诺的保护。如果保留原版的强制死亡语义，在伤害取消和死亡救援入口先排除约定的 bypass 标签或明确类型；不要无差别禁掉全部魔法伤害。补真实 `kill()` 和下界限伤害链的断言，而不是只人工发普通死亡事件。

#### EQ-09 / P2：坐骑实体加入失败时仍消耗放置物品

位置：[MountItems.java:65](../src/main/java/io/github/lounode/extrabotany/common/item/MountItems.java:65)。证据 A，触发依赖实体加入被拒绝。

放置路径忽略 `level.addFreshEntity(mount)` 的布尔结果，随后扣除物品并返回成功。如果 `EntityJoinLevelEvent` 被兼容模组取消，玩家会失去坐骑物品而没有实体。这里不是复制漏洞，而是取消交易后的物品损失。

最小修复：只有加入成功才扣除和提交成功结果；加入失败保留输入。`motorItemPlacesMotorAndKeepsItsOwner` 证明正常所有者传递，没有覆盖取消事件。其他召唤物品的相同提交顺序由 Boss/总报告归并，避免重复计数。

#### EQ-10 / P3：血嗜击杀计数静态表没有登出/关服清理或持久化

位置：[SanguinePleiadesCombatMaidSuitItem.java:89](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/armor/pleiades_combat_maid/SanguinePleiadesCombatMaidSuitItem.java:89)、[NeoForgeEventBridge.java:241](../src/main/java/io/github/lounode/extrabotany/forge/event/NeoForgeEventBridge.java:241)。证据 A。

`bloodthirstKilled` 是静态 `ConcurrentHashMap<UUID,Integer>`；效果添加时插入，击杀时累加，只在效果移除/到期的替换流程移除。登出监听只清理 CoreOfTheVoid，卸载世界监听只清理 Wind。未走正常效果移除流程离线的 UUID 可留在表中，重启也无法恢复这一进行中的计数。

最小修复：若跨登录应继续计数，存入已有玩家 attachment 并定义复制/恢复；若不继续，登出/服务停止清理即可。不要两套状态同时维护。这里没有 Player/Level 强引用，不应描述为整世界强引用泄漏或确定崩服；重连加载效果是否触发重置需单独验证。

### 优化和待定项

1. **相机重复查询（P3）**：[CameraItem.java:129](../src/main/java/io/github/lounode/extrabotany/common/item/relic/CameraItem.java:129) 判断玩家正使用某个 CameraItem，但没有确认当前 tick 的 `stack == player.getUseItem()`；背包每一台相机都会做同一个大 AABB 查询和高亮刷新。虚空万藏 Camera 模式的 `inventoryTick:47–71` 也只验证当前使用的是任意 VoidArchivesItem。最小优化是在真正使用的那一个 stack 上执行，并将权威效果更新限制在服务端；无需建立全局缓存。多人密集实体环境需要计数采样后才能给出 TPS 数值。
2. **莱茵锤条件不满足仍扣 buff 魔力（P3）**：`IPhotonium:37–43`、`IShadowium:37–43` 在昼夜/洞穴判断之前收费；RheinHammer 的对应 `get*BuffMana:140–146` 非零，因而至少其中一个环境 buff 会在不适用时照扣。把条件判断移到扣费前即可。普通光子/暗影装备默认返回 0，不能把它们全部说成持续空耗。
3. **锤自修费率需确认意图**：`ManasteelHammerItem.inventoryTick:42–45` 使用基类常量 `MANA_PER_DAMAGE * 2`，而损坏路径使用可覆写 `getManaPerDamage()`；高阶锤覆写的 70/200/300 等值没有进入自修计费。代码差异明确，但经典与当前平衡规则尚未逐件验证，本项不定为已确认经济漏洞。
4. **位移目的地**：`RodOfDiscordItem.use:44–65` 直接采用射线命中点上方一格传送；没有类似 Flamescion dash 的实体碰撞检查。低顶/侧壁命中可能不合适，需用实际地形确认后修最小可站立空间检查，不能仅凭它有传送就归为穿墙作弊。

### 已核对并排除的误报

- **C2S 线程**：`ForgePacketHandler` 使用默认 `PayloadRegistrar`；依赖 `PayloadRegistrar` 默认 `HandlerThread.MAIN` 并包装 MainThreadPayloadHandler。直接调用 handler 不等于在网络线程改世界。
- **左键伪造参数**：LeftClickEmpty、Excalibur、Jingwei、VoidArchives 等包没有让客户端指定目标实体、坐标、ItemStack 或伤害。执行端读取服务器主手；RelicSword 检查服务端、非旁观、物品、攻击冷却和归属。Excalibur 还按玩家/游戏 tick 去重。包中的无条件攻击冷却重置本身只能消耗攻击机会。
- **主动技能频率**：Flamescion dash 检查模式和冷却；ultimate 写入过载后才生成。Herrscher 技能检查主手、宝石与能量并先归零。MountInput 只接受当前玩家实际控制的坐骑。未发现普通生存玩家靠伪造这些包获得无限能量的直接路径。
- **经典特殊耗魔**：FirstFractal 的部分扣费尝试失败仍有剑体、SpearOfSubspace 缺魔力以双倍冷却开启领域，是当前明确实现与测试的玩法；不能只按“技能必须先付足魔力”假设报漏洞。是否恢复经典特定版本是独立决策。
- **属性泄漏**：盔甲属性通过默认装备 AttributeModifiers；ShadowKatana 夜间临时属性在主手/时段变化时显式移除。Sanguine 胸甲替换有 `applyComponents` 保留输入组件。没有发现把每 tick 属性无限永久叠加的路径。
- **飞行**：CoreOfTheVoid 有 `updatePlayerFlyStatus` 与登出清理；不能把某件甲没有 `onUnequip` 当成永久飞行证据。玩家所称套装名必须映射到真实注册物品再分析，不能凭“女武神套”泛称臆造一个类。
- **Curios**：ForgeCommonInitializer:243–247 对 BaubleItem 注册真实 Curios capability；ItemUseGameTests 也通过实际能力与槽位验证。AllForOne 有物品种类去重、真实佩戴排除和 tick 防重复机制，未发现简单的“背包 + 佩戴”被动翻倍；EQ-02 是另一个独立的漏属性问题。
- **花园与组件**：KingGarden 的花种数组有长度限制、花种合法性检查和拷贝更新；它不是装完整 ItemStack 的宝库。不能据字段名和用户印象报告容器复制。使用耗魔和服务端生成均可追踪。
- **反伤区别**：Achilles 盾主动反伤使用 thorns；Terrasteel 盾使用普通 attack，两者不能合并判定。Achilles 源码中的抛射物反射块是注释，未作为当前运行能力计算。
- **区块查询区别**：DispersiveRing 使用 `getChunkNow` 且按间隔遍历已加载方块实体，不是直接强制加载整片方块；PureDaisyPendant 的破坏路径调用 PlayerUtil，不能仅因最终世界变化而判定绕保护。FrostRing 的直接读写已单列。
- **武器实体寿命**：本次读过的 Phantom/TrueTerrablade/TrueShadowKatana/InfluxWaver、FlowerWeapon、BottledStar、Photon、Flamescion、Subspace、Judah、KeyOfTruth 等都有寿命、次数或所有者条件。`tick()` 某分支没有调用 `super.tick()` 不足以证明 `tickCount` 不增长，Minecraft 外层也维护实体 tick。没有据此报告无限实体。
- **投射物保护**：大部分伤害经 DamageHandler 的目标过滤，`ServerPlayer.canHarmPlayer` 包含全服 PvP 判断。FlowerWeapon 的爆炸使用 `ExplosionInteraction.NONE`，不能报告炸基岩。穿过方块的某些剑气/裂隙是显式行为，和破坏方块是两回事。
- **全局绑定缺口**：Rheingold 测试有输入不变、实际 relic owner、保存恢复与自定义 marker 断言；不等于全部换配方都保留归属。InfiniteWine 换酿造后丢组件的具体问题由机器/酿造分报告记录。

### 测试质量与优先补测

以下是静态审阅结果，**不是本轮测试通过结果**。

| 测试位置 | 实际覆盖 | 未覆盖或假阳性窗口 |
|---|---|---|
| RelicSwordGameTests `assertSkill` 83–105 | 错误 owner 不扣费/不生成，正确 owner 的扣费与数量 | 第二次左键只查 mana 未变化，没有再次断言实体数量；不能证明零费重复生成也被阻止 |
| RelicSwordGameTests `firstFractalNeedsTheHerrscherAdvancement` 126–146 | 无进度的 mockPlayer 被拒绝、creative 放行 | 没有真正的生存 ServerPlayer 完成 advancement 正例，也不验证不同魔力余量 |
| RelicSwordGameTests `trueDamageCanKillAndIsHalvedForBosses` 219–229 | 普通僵尸真实伤害和致死 | 实际两个目标都是 ZOMBIE，没有 Boss，方法名中“Boss 减半”没有被测 |
| ItemAuditGameTests `leftClickPacketsResetTheAttackCooldown` 174–180 | 包 handler 重置攻击计时 | ChargedPlayer 空手；不测持物、归属、实体、魔力、重复包与 codec，不是反作弊全集 |
| ItemAuditGameTests `shieldEffectsNeedARealBlock` 100–118 | 事件 blocked 标志控制盾效果 | 不经过真实 hurt/对向双盾，不能发现 EQ-01 |
| ArmorGameTests `shootingGuardianPiecesGiveAttributes` 129–167 | 穿装备时若干属性正确 | 卸下后只核对移动速度，其他属性卸装恢复没有完整断言 |
| ArmorGameTests `silverBulletIsFreeWithTheShootingGuardianSet` 235–257 | 一次免费弹幕与冷却 | `burstCount` 搜索半径 16 的全部 ManaBurst，不按 owner；并发相邻测试存在串扰窗口 |
| RelicItemGameTests `kingGardenSummonsWeaponsThatWaitForTheRelease` 120–154 | 生成初始数量、等待期间静止 | 12 tick 时只遍历当时查询到的实体检查位移；如果列表为空，循环不执行仍 succeed。必须同时断言期望实体仍存在/已命中并核对身份 |
| RelicItemGameTests `allForOneSimulatesAccessoriesInTheBox` 207–237 | 仅 AeroStone 的移动属性增加及部分状态 | 没有断言 Aero 飞行属性或 Community 多属性，漏过 EQ-02 |
| BaubleGameTests | 多数伤害和饰品断言有可观察结果 | helper 直接找槽位写 stack，不验证所有物品标签与 UI 装备合法性；CurseRing/PvP、领地、/kill 没覆盖 |
| MountGameTests `motorDashNeedsEnergy` 210–226 | 驾乘尚不足 120 tick 时不能冲刺 | 已设置 500 能量，未测试能量少于门槛；方法名比实际覆盖广 |
| SpearAndJudahGameTests `riftStateIsSavedAndSynchronized` 198–219 | NBT 保存恢复、手动同步 entityData 值 | 不是客户端连接和网络包往返；若拿它证明多人渲染正确会过度解读 |
| FlamescionGameTests | 能量、过载、持久化、位移伤害、模式与一次近战斩击 | 主要直接调用 handler/tick；不替代真实输入频率、主线程网络和客户端显示验证 |
| WandExtensionGameTests | 两种法杖的输入不变、marker 保留、读数标志；额外/重复槽拒绝 | 本测试实质断言较完整；不覆盖 GUI 和读数网络显示 |
| RelicBindingRecipeGameTests | 实际归属、保存加载和不修改输入 | 只覆盖 Rheingold，不能代表 InfiniteWine 等所有配方 |

多处测试用“中心附近全部该类型实体”查数量而没有 owner/本次生成 UUID；是否实际碰撞取决于 GameTest 结构间距、并行批次和旧实体寿命，本轮不将其写成已经出现的跨测试污染。ShadowKatana 的时段测试有 `finally` 恢复时间，不应误报为已知时间污染。

优先顺序：先双盾重入、魔弓连续性与多箭预算、AllForOne 多属性，再 PvP/放置取消、管理伤害、客户端修复动画。既有大量服务端 GameTest 无法替代最后一项客户端验收。

### 本分报告实际审读清单

主代码覆盖：`common/item/equipment/armor` 全部具体类；`bauble` 全部具体类；`shield` 三类；`tool` 及 `tool/hammer` 全部具体类；`relic` 下直接物品类、`voidcore/CoreOfTheVoidItem`、`void_archives/VoidArchivesItem` 及 Camera/Excalibur/Failnaught/FruitOfGrisaia 模式；KingGarden、MountItems；全部 `network/serverbound` 处理入口与 ForgePacketHandler；FlamescionHandler、HerrscherHandler、相关 NeoForgeEventBridge 事件；AttributeUtil、四种材料 buff API、FrostWalkerHelper；`entity/projectile`、`entity/flamescion`、`entity/subspace`、`entity/judah`；MountEntity/Motor/Ufo 主控制链，MagicArrow、KeyOfTruth、PetPixie。依赖只追踪上述发现需要的局部调用，不声称审完 NeoForge/Botania 全库。InfiniteWine 模式及酿造、花/机器和 Boss 专属实体由对应分组负责；客户端渲染/探针由总报告负责。

本分报告全文审读 **11 个测试文件、75 个 @GameTest 方法**；目录均为 `src/gametest/java/io/github/lounode/extrabotany/forge/gametest/`：

1. **RelicSwordGameTests.java（9）**：`trueTerrabladeSkill`、`influxWaverSkill`、`trueShadowKatanaSkill`、`firstFractalNeedsTheHerrscherAdvancement`、`shadowKatanaNightBonusFollowsTheHeldItem`、`shadowKatanaRepairsWithManaAndDropsLegacyModifiers`、`bottledPixieSummonsAPetThatSparesItsOwner`、`trueDamageCanKillAndIsHalvedForBosses`、`manaDrinkHealsAndGivesMana`。
2. **ItemAuditGameTests.java（6）**：`materialBuffUsesManaOncePerInterval`、`shieldEffectsNeedARealBlock`、`achillesShieldCollectsAbsorption`、`featherOfJingweiGivesMaxAbsorption`、`fuelsComeFromTheDataMap`、`leftClickPacketsResetTheAttackCooldown`。
3. **ItemUseGameTests.java（1）**：`conquestHasUsableSlotAndRealCapability`。
4. **ArmorGameTests.java（8）**：`armorValuesMatchTheOriginal`、`manaDiscountNeedsTheFullSet`、`silentSagesSetIsNotTheShadowWarriorSet`、`shootingGuardianPiecesGiveAttributes`、`shootingGuardianSetIgnoresArmorAndStealsLife`、`shootingGuardianSetDecreasesHealing`、`shootingGuardianSetDrawsBowFaster`、`silverBulletIsFreeWithTheShootingGuardianSet`。
5. **BaubleGameTests.java（15）**：`piercingDamageIgnoresArmor`、`neutralDamageRespectsArmor`、`lifeLosingLeavesOneHealth`、`monstersOnlyDamagePlayers`、`peaceAmuletProtectsPassiveMobs`、`elementalStonesExcludeTheCommunity`、`theCommunityCombinesStoneAttributes`、`deathRingChargesOnlyWhenItActs`、`frostStarSlowsEnemiesForMana`、`potatoChipsPreventDeathOnce`、`moonPendantMakesTheWearerFireproof`、`sunRingExtendsBlockReachOnly`、`bottledFlamePlacesATorchInTheDark`、`gemOfConquestHasNoAttributeBonus`、`baubleRegistryIdsAreStable`。
6. **RelicItemGameTests.java（5）**：`kingGardenAcceptsEachOfTheSeventeenFlowers`、`kingGardenSummonsWeaponsThatWaitForTheRelease`、`flowerWeaponDamagesItsTargetAndGivesItsEffect`、`silentEternityIsAManaSourceAndStopsHealing`、`allForOneSimulatesAccessoriesInTheBox`。
7. **RelicBindingRecipeGameTests.java（2）**：`rheingoldChangesActualRelicOwnerAndSurvivesSave`、`malformedRheingoldBindingDoesNotMutateCraftingInput`。
8. **MountGameTests.java（7）**：`motorItemPlacesMotorAndKeepsItsOwner`、`summonedMotorGoesAwayWithoutRider`、`motorCollisionDoesIceDamage`、`motorChargesEnergyAndUsesTheCyclone`、`motorDashNeedsEnergy`、`ufoCatchesAndReleasesTheMobBelow`、`keyOfTruthDamagesItsTargetAndExpires`。
9. **SpearAndJudahGameTests.java（12）**：`spearLeftClickOpensARiftThatShootsOneSpear`、`spearSkillsNeedTheRelicOwner`、`spearChargedSkillOpensTheDomain`、`spearChargedSkillWithNoManaHasADoubleCooldown`、`domainRiftShootsFiveGoldenBursts`、`riftStateIsSavedAndSynchronized`、`subspaceSpearDoesTrueAndAttackDamage`、`judahVariantsGiveMainHandAttributes`、`judahVariantsShareOneCooldown`、`judahSpearHitsOneTargetAndStacksDivineJustice`、`divineJusticeDoesTrueDamageNearItsEnd`、`sakuraSwordFinisherOnlyHitsMobsOnItsPath`。
10. **FlamescionGameTests.java（9）**：`energyChargesToAnOverloadAndRecovers`、`stateBelongsToThePlayerAndIsSaved`、`oldSaveDataIsMigrated`、`ignitionThrowsThePlayerAndNearbyMobs`、`swingsReleaseSwordsAndStrengthenedSlashes`、`voidAndUltimateNeedTheFlamescionMode`、`flameDamageIgnoresArmorAndFireResistance`、`dashMovesThePlayerAndHitsMobsOnThePath`、`meleeHitInTheModeReleasesOneSlash`。
11. **WandExtensionGameTests.java（1）**：`wandUpgradePreservesInputsAndRejectsForeignSlots`。

WandMerge、VoidArchivesInfiniteWine 等跨域测试依总分工由机器/酿造组记录；总报告分配给自身的兼容与历史测试不重复计入这 75 项。


## 10. 机器、花朵与酿造详审

审查基线：`45801bfb7070b784f90c64ed6c962e4fc760623e`；日期：2026-10-25。工作目录：仓库根目录。本次没有修改业务源码、构建、打包或运行游戏。以下“确认”指当前源码和依赖实现能推出触发链，不冒充运行复现；所有建议均尚未实施。

### 结论与分级

本范围发现 2 项 P1、5 项 P2。优先处理基座误删替换物品、雷卡兰危险自召闪电；其次处理无限酒洗掉绑定、区块边界读导致加载、温差公式和保护事件缺失。没有找到默认配置下机器 FE/魔力/液态魔力凭空倍增、负余额、空闲固定全量发包的证据。

|编号|等级|问题|证据强度|
|---|---|---|---|
|M-01|P1|基座缓存旧锤，旧锤断裂会删除展示框后来换入的物品|高；生产分支与原版 ItemFrame 换物语义闭合|
|M-02|P1|雷卡兰已满魔力仍先召唤真实闪电，跳过无害化|高；原版闪电伤害/着火条件及经典实现均已核对|
|M-03|P2|无限酒重做配方清除遗物绑定和其他组件|高；实际 JSON、assemble、遗物自动绑定链闭合|
|M-04|P2|多个机器/花朵跨区块直接读取，缺少未加载区块保护|高；MC Level 与 NeoForge capability cache 语义已核对|
|M-05|P2|双子兰对最低温度用了 Math.min，默认配置忽略冷端|高；算式、配置默认值和经典公式已核对|
|M-06|P2|石中姬/蕴魔瑾及无所有者木兰绕过常规玩家破坏/放置事件|高（事件未触发）；具体领地插件能否拦截取决于插件底层实现|
|M-07|P2|商友兰清除邻居花合法赋予的折扣，造成相互干扰|高；两花重叠范围即可推出|

### M-01：基座旧锤缓存能误删后来换入的物品

位置：[src/main/java/io/github/lounode/extrabotany/common/block/block_entity/PedestalBlockEntity.java:497](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/PedestalBlockEntity.java:497)、`:511`、`:593`、`:634`。

`getPerhapsHammers()` 将展示框当前的 `ItemStack` 引用作为 map 键。缓存每 20 tick 更新一次，自动敲击每 10 tick 使用一次；使用时仅检查展示框是否存活，没有核对它当前是否仍放着这一把锤。旧锤 `hurtAndBreak` 回调无条件调用 `frame.setItem(ItemStack.EMPTY)`。

触发链：刷新缓存时展示框有一把即将损坏的有效锤；在下一次缓存刷新前把它取下并放入其他贵重物品；到中间的 10 tick 自动加工点，旧引用仍通过配方工具检测；旧锤损坏回调清空了当前新物品，且没有掉落返还。这个窗口不足 20 tick，需要基座内存在可加工输入，旧锤这次敲击实际触发损坏。原版 `ItemFrame.java:185–205` 取物时先清空展示框再复制旧堆掉落；`:225–248` 的 `getItem` 返回当前引用、`setItem` 写入新的复制堆，因此取下并没有使缓存旧对象失效。

影响：真实物品丢失；即便旧锤不损坏，已取下的锤仍可免费完成一次加工。按不可逆物品丢失列 P1，发生窗口条件如上，不声称每次换物都会丢失。

最小修复：缓存展示框或位置；每次敲击从框读取当前物品并重新验证。断裂回调只清除仍为本次参与加工的当前堆。增加真实展示框“缓存后移除旧锤、换入新物品、跨过 10 tick”的测试，断言新物品保留、旧锤不再加工。现有 `PedestalStateGameTests.automaticStrikeAndCompletionSurviveSaveReload` 直接调用加工函数，不覆盖此生命周期。

### M-02：雷卡兰满魔力时召唤的闪电仍能伤害和着火

位置：[common/block/flower/generating/ReikarlilyBlockEntity.java:89–96](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/ReikarlilyBlockEntity.java:89)、`:100–114`、`:117–135`，路径均相对 `src/main/java/io/github/lounode/extrabotany/`。

服务端 tick 在产能判定前调用 `trySpawnLightningBolt()`；它创建普通 `LIGHTNING_BOLT` 并直接加入世界，没有设置 `visualOnly`。随后 `resolveActiveProduce()` 遇到满魔力直接返回，原本应执行的 `lightning.setVisualOnly(true)` 不会发生。

触发链：露天下雨、召雷冷却结束、工作冷却结束、花处于满魔力且附近没有另一朵可吸收的雷卡兰。召出的闪电进入原版实体 tick 后仍有伤害。普通/困难难度且 `doFireTick` 开启时，还会尝试点火。依赖来源 `neoforge-21.1.248-sources.jar!/net/minecraft/world/entity/LightningBolt.java:115–116,155–165,181–193` 明确区分真实闪电和视觉闪电。雷卡兰附近的物品、生物及可燃建筑因此面临不符合说明的损失。

经典对照：[ExtraMeteorP 的 SubTileReikarLily](https://github.com/ExtraMeteorP/Extra-Botany/blob/4ad2cc9b86620801409c17366511156a3ba1b45e/src/main/java/com/meteor/extrabotany/common/block/subtile/generating/SubTileReikarLily.java) 自召闪电两处构造都传入 `effectOnly=true`。所以此风险不能以“经典雷卡兰就是会劈坏周围”解释。自然闪电可能在花扫描前已造成影响，是另一种时序，不用该问题推出“所有天然雷击都应被提前取消”。

最小修复：自召闪电在加入世界前即设为无害，并使这次产能显式走容量/冷却判定。不能仅加 `setVisualOnly(true)` 而保留现有扫描，因为当前扫描会跳过视觉闪电，导致产能中断。补测空/满魔力、冷却、雨天、自召雷与自然雷；断言雷实体无害、火焰和生物生命值不变、产能正确。

### M-03：无限酒重做清除所有者绑定和其他组件

位置：[common/crafting/recipe/CopyBrewFromManaCocktailRecipe.java:51–84](../src/main/java/io/github/lounode/extrabotany/common/crafting/recipe/CopyBrewFromManaCocktailRecipe.java:51)；[src/generated/resources/data/extrabotany/recipe/infinite_wine_change_brew.json](../src/generated/resources/data/extrabotany/recipe/infinite_wine_change_brew.json)；[common/item/brew/InfiniteWineItem.java:103–107](../src/main/java/io/github/lounode/extrabotany/common/item/brew/InfiniteWineItem.java:103)。

重做配方确实接受一瓶满无限酒和鸡尾酒。`assemble()` 用配方静态 result 的副本建立输出，只检查原酒满额并写入新的 brew，完全没有复制原酒组件。绑定 `BotaniaDataComponents.SOULBOUND`、自定义名称及其他合法组件因此消失。

触发链：玩家 B 获得已绑定给 A 的满酒，将它与鸡尾酒重做；结果变成未绑定酒。Botania `RelicImpl.java:43–66` 对没有 `SOULBOUND` 的堆在 `tickBinding` 中绑定给当前玩家；无限酒每个服务端物品栏 tick 都调用该方法。原本的 `use()` 所有者限制因此可通过正常配方绕过。没有找到另一个全局 crafting 回调补回绑定；`RelicBindingRecipeGameTests` 只覆盖 Rheingold。

最小修复：对于同物品重做，从输入酒复制保留堆，只修改 brew 及设计上应重置的次数；首次造酒继续从配方结果创建。测试应使用已绑定 A、带名称/标记的满酒重做，再放入 B 的物品栏，断言仍绑定 A、其他组件保留。当前 `cocktailBrewSurvivesWineCreationRemakeAndDrink` 使用新建未绑定堆，只验证 brew 和饮用效果，无法排除此问题。

### M-04：跨区块读取可能同步加载邻区块

主要位置（以下相对 `src/main/java/io/github/lounode/extrabotany/`）：

|调用者|位置|跨边界入口|
|ManaBuffer / Quantum|[common/block/block_entity/ManaBufferBlockEntity.java:112–113](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/ManaBufferBlockEntity.java:112)|每 tick 查输入方向和上方伙伴|
|Generator / Liquefaction 公共路径|[common/block/block_entity/ManaMachineBlockEntity.java:100–101](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/ManaMachineBlockEntity.java:100)|向相邻发射器推魔力|
|Barrel|[common/block/block_entity/LivingrockBarrelBlockEntity.java:69–74](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/LivingrockBarrelBlockEntity.java:69)|六面相邻花药台|
|Charger / PowerFrame|[common/block/block_entity/ChargerBlockEntity.java:217–221](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/ChargerBlockEntity.java:217)|池周围风箱|
|Pedestal|[common/block/block_entity/PedestalBlockEntity.java:526–535](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/PedestalBlockEntity.java:526)|外围四个池与结构判定|
|Bellflower|[common/block/flower/generating/BellflowerBlockEntity.java:152–179](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/BellflowerBlockEntity.java:152)|空间检查及蒲公英方块实体|
|AnnoyingFlower|[common/block/flower/functional/AnnoyingFlowerBlockEntity.java:204–219](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/AnnoyingFlowerBlockEntity.java:204)|缓存基座和范围查找|
|Twinstar|[common/block/flower/generating/TwinstarBlockEntity.java:75–76](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/TwinstarBlockEntity.java:75)|九个流体位置|
|Stonesia / Enchanter / Serenitian|各类 `:58` / `:88–89` / `:57`|候选方块/附近花直接查询|
|StardustLotus|[common/block/flower/functional/StardustLotusBlockEntity.java:235](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/StardustLotusBlockEntity.java:235)|多方块结构检查|

依赖语义：`Level.java:771–777` 的 `getBlockEntity` 经 `getChunkAt` 取得区块；`:191–199` 取 FULL chunk；`:378–393` 的方块/流体读取同样走该路径。没有“getBlockEntity 自然只返回已加载区块”的保证。机器/花处于区块边缘，或使用独立加载器仅维持自身区块时，这些扫描可请求加载原本未加载的邻区块，增加主线程加载工作。范围有限；本报告不把它夸大为无限扩展世界或已经测得的固定 TPS 降幅。

已排除：NeoForge `BlockCapabilityCache.java:137–149` 在查询前检查 `level.isLoaded(pos)`，所以机器 FE/流体的 capability cache 本身不是问题入口；Woodienia `getChunkNow` 跳过未加载区块，Manalink `:172` 对远端目标检查 `isLoaded`，这两条保护已存在。

最小修复：候选读取前检查区块加载状态或从 `getChunkNow` 取数据；缓存失效时不要用会加载的 getter 验证。只对必要扫描加保护，不需要引入新的强制加载系统。测试要让扫描边界之外确实处于未加载状态，并观察读取后仍未加载；把全部块放在 GameTest 已加载区内不能证明此性质。

### M-05：双子兰冷端公式错误

位置：[common/block/flower/generating/TwinstarBlockEntity.java:69–90](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/TwinstarBlockEntity.java:69)；[forge/ForgeExtrabotanyConfig.java:492–501](../src/main/java/io/github/lounode/extrabotany/forge/ForgeExtrabotanyConfig.java:492)。

扫描已经正确收集实际 `tempMin` 和 `tempMax`，但最终计算为 `min(最高上限, tempMax) - min(最低下限, tempMin)`。默认最低下限为 0，因此通常为正温度的流体冷端一律变成 0。放置同种流体也能产生“最高温 / 100”的魔力；增加更冷的流体不会增加产量，违背类说明的温差机制。

最小算式修复是用 `Math.max(最低下限, tempMin)` 约束冷端，产出再限制非负，并为配置的上下限关系作明确约定。经典 [SubTileGeminiOrchid](https://github.com/ExtraMeteorP/Extra-Botany/blob/4ad2cc9b86620801409c17366511156a3ba1b45e/src/main/java/com/meteor/extrabotany/common/block/subtile/generating/SubTileGeminiOrchid.java) 另有 700 的环境基准；是否恢复该基准属于玩法选择，不应把“修掉 min/max 错误”自动当成完整经典还原。至少测试无流体、同温、冷热两种、上限/下限截断。现有花朵 GameTest 没有调用双子兰产能算式。

### M-06：方块改动缺少领地常用事件链

位置：[common/block/flower/generating/StonesiaBlockEntity.java:58–62](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/StonesiaBlockEntity.java:58)；[common/block/flower/functional/EnchanterBlockEntity.java:73](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/EnchanterBlockEntity.java:73)；[common/block/flower/functional/WoodieniaBlockEntity.java:96–104,202–208](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/WoodieniaBlockEntity.java:96)。

石中姬直接 `destroyBlock(pos,false)`，蕴魔瑾直接 `setBlockAndUpdate`；两者都没有放置者身份或玩家破坏/放置许可调用。木兰有所有者时通过带该 UUID 的 fake player 调用 Botania 工具破坏逻辑；没有所有者（例如自动放置、命令创建、缺旧存档所有者信息）时退化为直接 `destroyBlock`。

`Level.java:300–328` 的实现是改状态/掉落/游戏事件，不发布玩家 `BlockEvent.BreakEvent`。因此仅依赖常规玩家破坏/放置事件的领地保护无法在此入口拒绝操作。石中姬还忽略 `destroyBlock` 返回值，即改块被更底层规则拒绝时仍产魔力；木兰也在破坏调用后无条件扣魔力和进入冷却。蕴魔瑾已检查改块返回值，这一部分不应重复指责。

影响必须分条件说明：默认石中姬只吃配方允许的石/矿；没有“默认能吃基岩”的证据。如果数据包把不可破坏块加入配方，当前路径也没有硬度检查。实际服务器保护模组若另有底层 `setBlock` 拦截，可能仍挡住；本次未运行该组合，不能声称绕过所有领地插件。

最小修复：复用已有放置者 UUID/fake-player 与保护事件路径；只有改块成功才授予资源/扣费。明确无所有者时的服务器策略。补测有所有者、无所有者、保护事件拒绝三个分支，验证块/魔力/掉落都没有发生半笔交易。当前 `woodieniaBreaksLogsAndUsesMana` 测的是未设置 owner 的裸花，恰好没有验证受保护分支。

### M-07：相邻商友兰相互删除折扣

位置：[common/block/flower/functional/TradeOrchidBlockEntity.java:69–90,95–102](../src/main/java/io/github/lounode/extrabotany/common/block/flower/functional/TradeOrchidBlockEntity.java:69)。

每朵花计算自己的有效范围和额外外扩 5 格范围，对外圈所有村民无条件移除 `DISCOUNT`，没有判断效果来源。把村民放在 A 花有效范围内、B 花外圈时，A 添加效果后 B 会清除；时序相反则下一周期重复清除，导致折扣不稳定和无谓耗魔。拆除 B 也会清除其范围内由 A 维持的合法效果。

最小修复：当前效果持续时间仅为施加周期加 20 tick，可以优先让短效果自然过期，避免无来源信息的主动删除；若必须立即清除，则需要明确来源并检查是否仍有其他花维持。补两朵花重叠/拆除其中一朵的测试。现有单花测试锁定了“外圈删除”，并不证明多花并存正确。

### 范围覆盖与排除结论

已全文审读 `common/block` 下全部类：`ExtraBotanyBlock`、`ExtraBotanyBlocks`、`CocoonOfDesireBlock`、`EnchantedSoilBlock`、`LivingrockBarrelBlock`、`PedestalBlock`、`TrophyBlock`；`mana` 下 `DimensionCatalystBlock`、`ManaBufferBlock`、`ManaChargerBlock`、`ManaMachineBlock`、`PowerFrameBlock`；`block_entity` 下全部 10 个实体/基类及注册类 `ExtraBotanyBlockEntities`。自然祭坛行为位于 `PedestalBlockEntity`，没有遗漏一个名为 NatureAltar 的独立实现。

已审读两个花朵基类、`ExtrabotanyFlowerBlocks` 及所有 20 种花：Bellflower、BloodEnchantress、Edelweiss、MoonlightLily、Omniviolet、Reikarlily、Resoncund、Stonesia、SunshineLily、Tinkle、Twinstar；AnnoyingFlower、Enchanter、Manalink、Mirrowtunia、Necrofleur（含 Mini）、Serenitian、StardustLotus、TradeOrchid、Woodienia。注册表逐项核对了普通/浮空共享正确 BE type，Necrofleur Mini 独立工厂正确；红石型普通/浮空组合一致。原版花盆映射不等价于 BotanyPots 联动，本工程未发现后者的专用源码/资源集成，见第 3 节。

配方与饮食范围：`ExtraBotanyRecipeTypes`、`EdelweissRecipes`、`OmniVioletsRecipe`、`StonesiasRecipe`、`PedestalsRecipe`，`CopyBrewRecipe`、两种 CopyBrew 实现、`PotionLensChangePotionRecipe`；`BrewUtil`、`ExtraBotanyBrews`、`ExtraBotanyMobEffects` 和全部九种 effect 类；四个 brew item、`HolyWaterGrenadeEntity`、虚空万藏 `variants/InfiniteWine`；`ItemNBTHelper`、`ExtraBotanyDataMaps`，以及相关 JSON。其他装备、糖果、Rheingold/法杖配方由装备/主报告负责，避免把交叉审读冒充独立完整覆盖。

主要无问题/边界结论：

- 机器余额变动使用容量边界与长整型中间计算；标准接口下的 simulate/execute、每批转换、液态往返和最后一笔同步存在有效代码路径。能力对象没有被当作跨维度永远有效的全局缓存。
- `setChanged()` 是区块脏标记。`Level.blockEntityChanged:941–944` 将区块设为 unsaved；它本身不是每 10 秒强制落盘，也不是全服发包。机器的 10 tick 同步闸门和脏标记应分别讨论。现有变更触发的比较器通知可进一步按输出等级变化收敛，但不能据此断言已有磁盘风暴。
- Charger 空槽/无池早退、最终部分充能的 pending flush、槽位保存与发包路径已审；Pedestal 完成标记/漏斗方向限制、手工加工保存、梦魇燃料白天累计及重载重置已审。自动锤错误见 M-01。`strikes` 不在继承的容器 updateTag 中，但当前渲染/客户端没有使用它，不单列用户可见同步故障。
- Cocoon 孵化先查 data map，失败保留物品，实际孵化与取回路径没有发现重复消费；原始实体类型 codec 与服务端专用数据映射用途一致。
- Botania `BREW`/`REMAINING_USES` 等组件是酿造真实存储；`PotionContents.addPotionTooltip` 在无限酒仅用于效果展示。不能因为未给 Botania 酒设置 vanilla `POTION_CONTENTS` 就认定药效丢失。BrewUtil 现代组件优先、旧字符串回退和 setBrew 路径存在；M-03 是重做时丢弃其他组件，与此区分。
- 虚空万藏 InfiniteWine 先验证所有者及足额魔力，瞬时效果没有双重放大，持续效果有相应测试；本范围没有发现基于该分支的免费施法。圣水手雷使用 brew 产生范围效果且瞬时效果和持续效果分流，不把仅检查 getBrew 的测试解释成已验证爆炸整个生命周期。
- Resoncund 生产代码 `setRemoved/clearRemoved` 有注册清理。静态强引用 `snapshot=List.copyOf(LISTENERS)` 意味着 WeakHashMap 并不能挽救未注销的裸测试对象。`RobustnessGameTests` 缺清理由主报告列项；本组 FlowerGameTests 虽在成功路径 setRemoved，但失败路径也宜用 finally 清理。
- 默认 Pedestal 配方输出数量都是 1；容器上限 1 与这些内置配方一致。自定义数据包若给 output count>1 将受容器上限约束，这是需要明示/校验的数据包契约，不据此声称当前内置配方吞默认产物。
- 没有给异常管理员配置、任意恶意第三方 capability 返回值或人为损坏 NBT 构造新的高危漏洞结论。极端 int 配置乘法和未约束配方数值可作为输入校验后续项，不与玩家正常触发缺陷混排。

### 测试源码实审：11 个文件，65 个 GameTest

根目录为 `src/gametest/java/io/github/lounode/extrabotany/forge/gametest/`。以下为实际全文审读范围，数量是源码 `@GameTest` 方法数量，**本次没有执行**。

|文件|方法数|实际覆盖与证据边界|
|---|---:|---|
|BlockGameTests.java|24|全部方法；Buffer/Quantum、比较器、FE、液态魔力、桶、茧、星尘莲结构/传送/保存。有状态守恒断言；无卸载边界、保护事件拒绝、客户端真实收包|
|ChargerGameTests.java|7|全部方法；空闲、正反向、最终部分充能、源耗尽、槽变化、丢池、框架风箱。计数子类正确安装入世界，有 updateTag 验证；未测真实多漏斗/玩家同时操作|
|PedestalStateGameTests.java|4|全部方法；空闲、最后充能、手工/自动敲击保存。CountingPedestal 只 setLevel，没有替换世界内 BE，见下文；自动测试没有真实展示框换物|
|PedestalLegacyParityGameTests.java|3|全部方法；6000 白天 tick、夜晚暂停/移除/重载、满珠不扣池。覆写结构/自动锤以隔离逻辑，不能证明真实结构或自动锤正确|
|LivingrockBarrelRecipeGameTests.java|1|sevenLivingrockCraftOneBarrel；实际配方及错误输入|
|FlowerHotPathGameTests.java|4|typedRecipesKeepPriorityAndObserveReplacement、edelweissPreservesVirtualSelectionAndSecondLookup、edelweissReleasesSnapshotAfterOverrideFailure、tradeOrchidKeepsBudgetOrderAndSendsNoFullSync；同步替换配方有 finally 恢复，雪莲异常路径释放有真实断言；商友兰只测单花|
|FlowerGameTests.java|8|stewEffectsUseRegisteredHolders、onlyRedstoneFlowersHaveThePoweredProperty、serenitianStopsDecay、bellflowerSynchronizesTheWindLevel、resoncundListensOnlyInAServerLevel、woodieniaSelectsOnlyLogsInRange、woodieniaBreaksLogsAndUsesMana、stonesiaEatsOnlyRecipeBlocksAroundIt；覆盖有限功能，无雷卡兰/双子兰产能测试|
|DataPackGameTests.java|4|restoredContentIsInTheTags、lootTablesExist、recordsHaveSongsOfTheCorrectLength、craftingRecipesHaveValidCategories；验证资源注册/基本数据，不等价于玩法跑通|
|BrewRecipeGameTests.java|5|flaskBrewSurvivesCraftSaveAndActualDrink、cocktailBrewSurvivesWineCreationRemakeAndDrink、cocktailBrewRemainsReadableByGrenadeItemAndEntity、potionLensAcceptsModernBrewAndKeepsLensData、legacyBrewKeyStillCopiesAndComponentTakesPrecedence；保存/读/实际饮用存在，重做输入无绑定，直接 finishUsing 绕过 use 所有者入口|
|VoidArchivesInfiniteWineGameTests.java|3|wineRequiresOwnerAndFullManaBeforeApplyingEffects、wineConsumesManaOnceAndAmplifiesInstantEffectOnce、wineKeepsLastingEffectDurationAndSingleAmplifier；真实断言 owner/mana/effects，但直接调 variant，未覆盖完整虚空万藏激活分派|
|WandMergeGameTests.java|2|extraBotanyFlowersExposeWandBindable、manalinkBindsOnLogicalServerAndPersists；前者能力注册，后者服务器 useOn→链接→save/load。没有跨维度卸载/目标失效验证|
|合计|65|未发现空方法、无断言即 succeed 或整段被注释的伪测试|

`BlockGameTests` 24 项依次包括：`manaBufferMovesManaUpwards`、`manaBufferExchangesManaWithPools`、`quantumBufferStartsTransfersWithBothTiers`、`manaBufferComparatorOutput`、`manaGeneratorCapabilities`、`manaGeneratorEnergyLimitsAndSave`、`manaGeneratorConvertsOneLotForEachTick`、`manaGeneratorPullsEnergyFromNeighbours`、`manaGeneratorFeedsSpreaderInFullLots`、`manaLiquefactionConservesResources`、`manaLiquefactionRatesAndLimits`、`manaLiquefactionFeedsSpreader`、`fluidedManaRegistryLinks`、`livingrockBarrelTransactionsAndSave`、`livingrockBarrelFillsApothecary`、`livingrockBarrelBucketInteraction`、`cocoonHatchesBabiesFromTheHatchTable`、`cocoonKeepsItemsThatCannotHatch`、`cocoonTakesOneHatchableDroppedItem`、`cocoonPlayerInteraction`、`stardustLotusKeepsItsDataAndSendsItToClients`、`stardustLotusOpensPermanentPortal`、`stardustLotusNeedsTheStructure`、`binderSetsTheLotusTarget`。

`ChargerGameTests` 7 项：`emptyAndMissingPoolDoNotBroadcastUnchangedInventory`、`chargeAndDrainPreserveRateAndSendPostTransferSnapshots`、`finalPartialChargeFlushesAfterCompletionAndPersists`、`emptySourceAndFullDestinationFlushTheirLastTransfer`、`slotRemovalAndInvalidItemReachUpdatePacket`、`losingPoolFlushesPendingManaAndReloadResumes`、`powerFramePreservesLinkedBellowsRateAndSkipsIdleScans`。

`PedestalStateGameTests` 4 项：`idleAndUnchangedStrikesDoNotDirtyPedestal`、`lastChargePersistsAndFlushesAfterOrbBecomesFull`、`manualPartialStrikePersistsAndFlushesWithoutEnergy`、`automaticStrikeAndCompletionSurviveSaveReload`。其 helper 新建 CountingPedestal 后只 setLevel，世界里仍是 `h.setBlock` 创建的原 BE。断言计数能验证被测对象的 setChanged 分支，但 sendBlockUpdated 实际从世界取的数据不一定来自该对象；“dirtyCalls==2”不能证明部分加工进度真实到达客户端。保存/加载断言有效，不应把这四项全部称为假测试。

`PedestalLegacyParityGameTests` 3 项：`nightmareFuelConvertsAfterSixThousandDayTicksWithoutIdleUpdates`、`nightPausesFuelProgressAndRemovalOrReloadResetsIt`、`fullNatureOrbDoesNotDrainTheFourPools`。dayTime 在同步测试块中 finally 恢复；没有证据表明它们将改后的时间留给别的测试。

花朵随机选点测试属于有限采样，不能证明所有空间分布；伐木测试实际破坏了原木并检查魔力，石中姬测试实际消费了配方块。这些都比只检查注册强，但没有覆盖拒绝事件、边界加载、雷击损害、双子温差、多花折扣争用。

### 性能与后续验证优先级

现有木兰按区块段调色板跳过空段、getChunkNow、空闲回退与位置错峰是有意义的减负；雪莲 tick 内配方快照与 finally 清理也有对应测试。不能为了统一外观把这些改回每 tick 全范围扫描。

尚可收敛的点包括：Bellflower 每次空间检查、Enchanter 为候选建立 HashMap/Random/list、AnnoyingFlower 失配后的范围扫描、Serenitian 周期扫描。静态只能确定调用频率/分配来源，不能给出节省毫秒或宣称某个机器阵列已导致 TPS 下降。先修 M-04 的区块加载边界，再用实际规模评估是否需要缓存/错峰；不要无证据新增全局轮询线程或长期强引用缓存。

建议下一轮行为验证顺序：M-01 展示框换物丢失回归 → M-02 雨天满魔力雷击 → M-03 跨所有者重做 → M-04 已加载/未加载边界 → M-05 温差矩阵 → M-06 保护事件拒绝 → M-07 双花重叠。按用户要求，本轮只给具体可复现条件和修复建议，不启动这些运行测试。


## 11. 全部测试方法索引

以下按源码声明清点，数量不是通过数。完整方法名便于在后续修复中定位现有测试，避免按名称误读实际覆盖。

### [AdvancementGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/AdvancementGameTests.java)（3）

`originalAdvancementsExist`、`supplyBagRareRewardGrantsTheAdvancement`、`allMainAdvancementsGiveTheTrophy`。

### [ArmorGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ArmorGameTests.java)（8）

`armorValuesMatchTheOriginal`、`manaDiscountNeedsTheFullSet`、`silentSagesSetIsNotTheShadowWarriorSet`、`shootingGuardianPiecesGiveAttributes`、`shootingGuardianSetIgnoresArmorAndStealsLife`、`shootingGuardianSetDecreasesHealing`、`shootingGuardianSetDrawsBowFaster`、`silverBulletIsFreeWithTheShootingGuardianSet`。

### [BaubleGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/BaubleGameTests.java)（15）

`piercingDamageIgnoresArmor`、`neutralDamageRespectsArmor`、`lifeLosingLeavesOneHealth`、`monstersOnlyDamagePlayers`、`peaceAmuletProtectsPassiveMobs`、`elementalStonesExcludeTheCommunity`、`theCommunityCombinesStoneAttributes`、`deathRingChargesOnlyWhenItActs`、`frostStarSlowsEnemiesForMana`、`potatoChipsPreventDeathOnce`、`moonPendantMakesTheWearerFireproof`、`sunRingExtendsBlockReachOnly`、`bottledFlamePlacesATorchInTheDark`、`gemOfConquestHasNoAttributeBonus`、`baubleRegistryIdsAreStable`。

### [BlockGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/BlockGameTests.java)（24）

`manaBufferMovesManaUpwards`、`manaBufferExchangesManaWithPools`、`quantumBufferStartsTransfersWithBothTiers`、`manaBufferComparatorOutput`、`manaGeneratorCapabilities`、`manaGeneratorEnergyLimitsAndSave`、`manaGeneratorConvertsOneLotForEachTick`、`manaGeneratorPullsEnergyFromNeighbours`、`manaGeneratorFeedsSpreaderInFullLots`、`manaLiquefactionConservesResources`、`manaLiquefactionRatesAndLimits`、`manaLiquefactionFeedsSpreader`、`fluidedManaRegistryLinks`、`livingrockBarrelTransactionsAndSave`、`livingrockBarrelFillsApothecary`、`livingrockBarrelBucketInteraction`、`cocoonHatchesBabiesFromTheHatchTable`、`cocoonKeepsItemsThatCannotHatch`、`cocoonTakesOneHatchableDroppedItem`、`cocoonPlayerInteraction`、`stardustLotusKeepsItsDataAndSendsItToClients`、`stardustLotusOpensPermanentPortal`、`stardustLotusNeedsTheStructure`、`binderSetsTheLotusTarget`。

### [BossBarGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/BossBarGameTests.java)（2）

`grainTimePacketsOnlyWhenTheIntensityChanges`、`skullLandmineExplodesAndIgnoresMobs`。

### [BrewRecipeGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/BrewRecipeGameTests.java)（5）

`flaskBrewSurvivesCraftSaveAndActualDrink`、`cocktailBrewSurvivesWineCreationRemakeAndDrink`、`cocktailBrewRemainsReadableByGrenadeItemAndEntity`、`potionLensAcceptsModernBrewAndKeepsLensData`、`legacyBrewKeyStillCopiesAndComponentTakesPrecedence`。

### [ChargerGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ChargerGameTests.java)（7）

`emptyAndMissingPoolDoNotBroadcastUnchangedInventory`、`chargeAndDrainPreserveRateAndSendPostTransferSnapshots`、`finalPartialChargeFlushesAfterCompletionAndPersists`、`emptySourceAndFullDestinationFlushTheirLastTransfer`、`slotRemovalAndInvalidItemReachUpdatePacket`、`losingPoolFlushesPendingManaAndReloadResumes`、`powerFramePreservesLinkedBellowsRateAndSkipsIdleScans`。

### [ConfirmedLegacyItemGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ConfirmedLegacyItemGameTests.java)（3）

`infiniteWineAmplifiesInstantAndLastingEffectsOnce`、`confirmedRunesPreserveCountsAndCraft`、`confirmedDiscLoadsPlaysSavesAndEjects`。

### [DataPackGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/DataPackGameTests.java)（4）

`restoredContentIsInTheTags`、`lootTablesExist`、`recordsHaveSongsOfTheCorrectLength`、`craftingRecipesHaveValidCategories`。

### [EgoGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/EgoGameTests.java)（12）

`natureOrbSummonsTheEgo`、`egoSaveDataRoundTrip`、`egoLandmineFormationCounts`、`egoLandmineTypes`、`egoMinionRules`、`egoAndMinionsFireEveryWeapon`、`egoDamageRules`、`egoAttacksAndTeleports`、`egoStartsTheLandmineBarrage`、`egoMinionPhaseAndLoot`、`egoLeavesWhenMorePlayersJoin`、`gaiaThirdHasNoEgoPhase`。

### [EgoRequirementGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/EgoRequirementGameTests.java)（2）

`theRuleIsOffByDefault`、`theRuleDropsRelicsUntilTheEgoIsDefeated`。

### [ExtraBotanyGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ExtraBotanyGameTests.java)（5）

`tradeOrchidDiscountAppliesWhenTrading`、`legacyItemDataSurvivesSaveAndCopy`、`enchantedSoilPreservesHydroangeasAtDecayBoundary`、`coreRegistriesAreAvailable`、`enchantedSoilDoublesGroundFlowerTicks`。

### [FlamescionGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FlamescionGameTests.java)（9）

`energyChargesToAnOverloadAndRecovers`、`stateBelongsToThePlayerAndIsSaved`、`oldSaveDataIsMigrated`、`ignitionThrowsThePlayerAndNearbyMobs`、`swingsReleaseSwordsAndStrengthenedSlashes`、`voidAndUltimateNeedTheFlamescionMode`、`flameDamageIgnoresArmorAndFireResistance`、`dashMovesThePlayerAndHitsMobsOnThePath`、`meleeHitInTheModeReleasesOneSlash`。

### [FlowerGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FlowerGameTests.java)（8）

`stewEffectsUseRegisteredHolders`、`onlyRedstoneFlowersHaveThePoweredProperty`、`serenitianStopsDecay`、`bellflowerSynchronizesTheWindLevel`、`resoncundListensOnlyInAServerLevel`、`woodieniaSelectsOnlyLogsInRange`、`woodieniaBreaksLogsAndUsesMana`、`stonesiaEatsOnlyRecipeBlocksAroundIt`。

### [FlowerHotPathGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FlowerHotPathGameTests.java)（4）

`typedRecipesKeepPriorityAndObserveReplacement`、`edelweissPreservesVirtualSelectionAndSecondLookup`、`edelweissReleasesSnapshotAfterOverrideFailure`、`tradeOrchidKeepsBudgetOrderAndSendsNoFullSync`。

### [FullLegacyGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FullLegacyGameTests.java)（5）

`allLegacyEntitiesConstructAndReload`、`relicSwordOwnershipManaAndDuplicateAttack`、`restoredLegacyStacksAndBinder`、`supplyPoolSurvivesPartialDrawAndReload`、`drinkLensAndEffectBehavior`。

### [GaiaDisarmGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/GaiaDisarmGameTests.java)（4）

`equippedItemsDropButStoredItemsRemain`、`curiosEquipmentDropsAndCreativeIsExempt`、`cancelledDropKeepsOriginalAndConfigDisablesDisarm`、`summonWithForbiddenCurioAndCombatDisarm`。

### [HerrscherGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/HerrscherGameTests.java)（4）

`gemChargesEnergyToTheMaximum`、`counterattackCancelsDamageAndUsesEnergy`、`counterattackNeedsASword`、`thunderSlashNeedsFullEnergyAndKillsAHusk`。

### [ItemAuditGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ItemAuditGameTests.java)（6）

`materialBuffUsesManaOncePerInterval`、`shieldEffectsNeedARealBlock`、`achillesShieldCollectsAbsorption`、`featherOfJingweiGivesMaxAbsorption`、`fuelsComeFromTheDataMap`、`leftClickPacketsResetTheAttackCooldown`。

### [ItemUseGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ItemUseGameTests.java)（1）

`conquestHasUsableSlotAndRealCapability`。

### [LegacyAccessoryParityGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/LegacyAccessoryParityGameTests.java)（4）

`foreignVoidCoreCannotCureOrChargeTheWearer`、`ownedVoidCoreCuresOnceAfterSaveAndRequiresEnoughMana`、`dispersiveRingUsesLegacyBoundsAndConservesMana`、`dispersiveRingKeepsLegacyPriorityWhenManaIsScarce`。

### [LegacyCandyGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/LegacyCandyGameTests.java)（2）

`deployedCandyStacksSurviveDiskNbtAndOpen`、`deployedCandyFoodAndSeasonalContract`。

### [LexiconGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/LexiconGameTests.java)（1）

`lexiconEntriesReferToThingsThatExist`。

### [LivingrockBarrelRecipeGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/LivingrockBarrelRecipeGameTests.java)（1）

`sevenLivingrockCraftOneBarrel`。

### [MountGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/MountGameTests.java)（7）

`motorItemPlacesMotorAndKeepsItsOwner`、`summonedMotorGoesAwayWithoutRider`、`motorCollisionDoesIceDamage`、`motorChargesEnergyAndUsesTheCyclone`、`motorDashNeedsEnergy`、`ufoCatchesAndReleasesTheMobBelow`、`keyOfTruthDamagesItsTargetAndExpires`。

### [PedestalLegacyParityGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/PedestalLegacyParityGameTests.java)（3）

`nightmareFuelConvertsAfterSixThousandDayTicksWithoutIdleUpdates`、`nightPausesFuelProgressAndRemovalOrReloadResetsIt`、`fullNatureOrbDoesNotDrainTheFourPools`。

### [PedestalStateGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/PedestalStateGameTests.java)（4）

`idleAndUnchangedStrikesDoNotDirtyPedestal`、`lastChargePersistsAndFlushesAfterOrbBecomesFull`、`manualPartialStrikePersistsAndFlushesWithoutEnergy`、`automaticStrikeAndCompletionSurviveSaveReload`。

### [RelicBindingRecipeGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RelicBindingRecipeGameTests.java)（2）

`rheingoldChangesActualRelicOwnerAndSurvivesSave`、`malformedRheingoldBindingDoesNotMutateCraftingInput`。

### [RelicItemGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RelicItemGameTests.java)（5）

`kingGardenAcceptsEachOfTheSeventeenFlowers`、`kingGardenSummonsWeaponsThatWaitForTheRelease`、`flowerWeaponDamagesItsTargetAndGivesItsEffect`、`silentEternityIsAManaSourceAndStopsHealing`、`allForOneSimulatesAccessoriesInTheBox`。

### [RelicSwordGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RelicSwordGameTests.java)（9）

`trueTerrabladeSkill`、`influxWaverSkill`、`trueShadowKatanaSkill`、`firstFractalNeedsTheHerrscherAdvancement`、`shadowKatanaNightBonusFollowsTheHeldItem`、`shadowKatanaRepairsWithManaAndDropsLegacyModifiers`、`bottledPixieSummonsAPetThatSparesItsOwner`、`trueDamageCanKillAndIsHalvedForBosses`、`manaDrinkHealsAndGivesMana`。

### [RobustnessGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/RobustnessGameTests.java)（3）

`resoncundKeepsNormalDecayAndDoesNotOverflow`、`resoncundIgnoresInvalidSavedSoundsWithoutLosingValidCounts`、`rheingoldChangesOnlyOneRelicAndRejectsForeignSlots`。

### [ScexAuditGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ScexAuditGameTests.java)（3）

`itemNbtReadsDoNotCopyButReturnDefensiveValues`、`boxSimulatorLookupIsInertWithoutSimulator`、`resoncundStopsListeningWhenRemoved`。

### [SpearAndJudahGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/SpearAndJudahGameTests.java)（12）

`spearLeftClickOpensARiftThatShootsOneSpear`、`spearSkillsNeedTheRelicOwner`、`spearChargedSkillOpensTheDomain`、`spearChargedSkillWithNoManaHasADoubleCooldown`、`domainRiftShootsFiveGoldenBursts`、`riftStateIsSavedAndSynchronized`、`subspaceSpearDoesTrueAndAttackDamage`、`judahVariantsGiveMainHandAttributes`、`judahVariantsShareOneCooldown`、`judahSpearHitsOneTargetAndStacksDivineJustice`、`divineJusticeDoesTrueDamageNearItsEnd`、`sakuraSwordFinisherOnlyHitsMobsOnItsPath`。

### [StructureGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/StructureGameTests.java)（3）

`pedestalTierOneNeedsThePedestalAtTheCenter`、`pedestalTierTwoStructure`、`manalinkStructure`。

### [UniversalRuneGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/UniversalRuneGameTests.java)（3）

`universalRunesAgreeWithRuneIngredients`、`universalRunesWorkInCraftingRecipes`、`universalRunesWorkAsRunicAltarCatalysts`。

### [UpstreamParityGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/UpstreamParityGameTests.java)（4）

`spearFanStaysPerpendicularForEveryView`、`timeLockSlowsFallingMotionWithoutReplacingMoveAttribute`、`creativeTargetsCannotLoseHealthToLegacyTrueDamage`、`effect_holders_survive_serialization`。

### [VoidArchivesInfiniteWineGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/VoidArchivesInfiniteWineGameTests.java)（3）

`wineRequiresOwnerAndFullManaBeforeApplyingEffects`、`wineConsumesManaOnceAndAmplifiesInstantEffectOnce`、`wineKeepsLastingEffectDurationAndSingleAmplifier`。

### [VoidHerrscherGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/VoidHerrscherGameTests.java)（8）

`herrscherSaveDataRoundTrip`、`herrscherAbsorbsHitsCapsDamageAndCounters`、`herrscherShieldsAndDodge`、`herrscherRankTransitions`、`subspaceLanceStandsAndDischarges`、`voidFieldSlowsCreatures`、`herrscherLootTable`、`voidCallerNeedsSneakAndArena`。

### [WandExtensionGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/WandExtensionGameTests.java)（1）

`wandUpgradePreservesInputsAndRejectsForeignSlots`。

### [WandMergeGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/WandMergeGameTests.java)（2）

`extraBotanyFlowersExposeWandBindable`、`manalinkBindsOnLogicalServerAndPersists`。

声明测试合计：**211**。其他三份测试源文件为 PerfScenarios、ItemUseClientProbe、UpstreamRenderProbe，审阅结论已列在第 4 节。

## 12. 包级范围与变更核验

下表按主源码两级目录汇总，包含其全部子包；数字是文件清点，不等于每行已获运行证明。注册/接口/常量、兼容和 mixin 通过入口与引用扫描，Boss/机器/花/装备/网络/渲染按实际风险路径审读。大型静态模型以渲染控制流和资源引用为主。datagen 另清点 65 文件，并与生成资源和实际注册交叉核对。

| 主源码路径（io/github/lounode/extrabotany 下） | Java 文件数 |
| --- | ---: |
| `api` | 4 |
| `api/block` | 5 |
| `api/client` | 2 |
| `api/gaia` | 5 |
| `api/item` | 14 |
| `api/level` | 1 |
| `api/recipe` | 6 |
| `client` | 2 |
| `client/core` | 3 |
| `client/gui` | 13 |
| `client/integration` | 10 |
| `client/model` | 11 |
| `client/network` | 2 |
| `client/patchouli` | 4 |
| `client/renderer` | 53 |
| `client/sound` | 2 |
| `common` | 2 |
| `common/advancements` | 6 |
| `common/attachment` | 3 |
| `common/block` | 52 |
| `common/bossevents` | 6 |
| `common/brew` | 14 |
| `common/crafting` | 13 |
| `common/data` | 1 |
| `common/entity` | 66 |
| `common/fluid` | 1 |
| `common/handler` | 6 |
| `common/helper` | 1 |
| `common/impl` | 3 |
| `common/integration` | 10 |
| `common/item` | 140 |
| `common/lib` | 14 |
| `common/loot` | 3 |
| `common/proxy` | 2 |
| `common/sounds` | 2 |
| `common/util` | 6 |
| `forge` | 4 |
| `forge/client` | 6 |
| `forge/event` | 1 |
| `forge/mixin` | 2 |
| `forge/network` | 2 |
| `forge/xplat` | 3 |
| `mixin` | 6 |
| `mixin/accessor` | 5 |
| `mixin/botania` | 8 |
| `mixin/client` | 5 |
| `network` | 2 |
| `network/clientbound` | 6 |
| `network/serverbound` | 10 |
| `xplat` | 4 |

审计阶段开始时工作区干净；该阶段结束时仅保留新增的本报告。分组审阅草稿已合并并清理；没有临时探针、游戏世界、运行进程或传输包，没有修改业务源码、配置、构建脚本和测试。最终检查限于文档路径/行号有效性、方法清点及 Git 差异，未新增运行测试。

## 13. 三个 P1 修复与定向验收（2026-10-25）

用户后续授权：先修三个 P1 并补回归，暂不打包发布。本轮改动限于 6 个业务源文件（含一个新辅助类）和 3 个新 GameTest 类，版本号与依赖范围保持原状。

### 13.1 实际修复

| 项目 | 结果 |
| --- | --- |
| BOSS-01：地雷/进度限制吞装备 | 先复制完整物品，只有最终掉落实体成功进入世界才清空原槽；取消 Toss 或最终实体加入时保留原装备。Toss、地雷投掷者/拾取延迟/统计以及 Curios 的原卸下路径保留 |
| 自定义掉落实体兼容 | Toss 后解析 NeoForge 的 hasCustomEntity/createEntity；直接提交最终实体。工厂返回 null 或普通 ItemEntity 时避免框架再次延迟替换，关闭“保留原件却延迟生成副本”的复制窗口 |
| M-01：基座旧锤误删换入物品 | 自动加工缓存展示框，执行时读取当前物品；拒绝脱离展示框的旧引用；破损回调只清除实际使用且仍在框中的同一物品栈 |
| M-02：雷卡兰自召危险闪电 | 自召雷在加入世界前设为 visualOnly，独立结算自召产能；保留天然雷优先吸收、容量、工作冷却及余热；满槽自召雷不造成实体伤害或引火 |

业务入口：

- [安全掉落辅助类](../src/main/java/io/github/lounode/extrabotany/common/util/PlayerDropHelper.java)
- [Ego 地雷](../src/main/java/io/github/lounode/extrabotany/common/entity/EgoLandmineEntity.java)、[骷髅地雷](../src/main/java/io/github/lounode/extrabotany/common/entity/SkullLandMineEntity.java)、[进度限制](../src/main/java/io/github/lounode/extrabotany/common/handler/EgoRequirementHandler.java)
- [基座](../src/main/java/io/github/lounode/extrabotany/common/block/block_entity/PedestalBlockEntity.java)
- [雷卡兰](../src/main/java/io/github/lounode/extrabotany/common/block/flower/generating/ReikarlilyBlockEntity.java)

### 13.2 新增回归与运行结果

- [P1DisarmGameTests：12 项](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/P1DisarmGameTests.java)：两种地雷、主手/头部/Curios、完整数量/附魔/耐久/名称/绑定/自定义组件、取消生成、取消 Toss、成功后重复调用不复制；另覆盖自定义实体成功/取消、Toss 后换物、null/普通实体回退。后五项跨两个真实服务器 tick 核对没有延迟副本。自定义物品注册夹具仅存在于 GameTest 源集。
- [P1PedestalGameTests：3 项](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/P1PedestalGameTests.java)：真实展示框取出将损坏的旧锤并换入命名下界合金锭，跨过缓存中间的自动加工点；另验证正常十击产出一份、消耗十点耐久，以及末击损坏时只清本框的锤。
- [P1ReikarlilyGameTests：4 项](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/P1ReikarlilyGameTests.java)：空/满容量、入场时即为视觉雷、原版完整闪烁过程无生物伤害/掉落损失/火焰、余热与冷却、防重复吸收、天然雷与自召雷同 tick。雨天夹具开放天空，天气/规则/监听器在 finally 恢复。

在 Vicerach 复用 Minecraft 1.21.1 / NeoForge 21.1.248 的既有引擎，加载编译后的类和资源运行。**46/46 必需测试通过：19 项新增回归 + 27 项直接相关既有测试。** Java 退出码 0，测试服正常保存后退出，无强制结束。首轮因 GameTest 默认屏障顶盖使雨天夹具不满足前提而失败；已保留原始失败日志，调整 skyAccess 后复测通过，没有将首轮记为通过。

本机 `compileGametestJava` 通过；定向回归覆盖实际服务器行为，不宣称已经完成完整玩家整包或客户端视觉验收。三个修复经交叉代码审查。`git diff --check` 通过。未运行 jar、build 或发布任务；`build/libs` 中既有两个 JAR 的 SHA-256 与修改时间保持不变。

### 13.3 证据与清理

- [验收结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/evidence/fixed-r2/retained/002-result.json)
- [实际游戏日志](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/evidence/fixed-r2/retained/001-process.log)
- [完成回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/evidence/fixed-r2/completion.json)
- [源码指纹](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/source-fingerprint.json)、[编译类/资源输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/input-manifest-r2.json)

两轮远端临时运行副本在所属 Java 退出、必要日志与输入身份保留后清理；初次失败的原记录保持失败状态，另记已由后续运行解决。共享引擎和依赖缓存保留。源码和回归测试保留在原工程，未部署或发布。本机 `D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p1-20261025-r1/transfer` 是 8,951,498 字节的可重建传输副本，目录及内容均无重解析点；清理命令被工具策略拒绝，暂保留，未将其记为已清理。

## 14. 剩余 P2 修复与回归（2026-10-25）

本轮授权为修复剩余 P2、补回归、暂不打包发布。范围是 23 项：BOSS-02～06、EQ-01～09、M-03～07、CLIENT-01、CHUNK-01、TEST-01～02。用户明确选择 `/kill` 和虚空伤害均遵循原版强制死亡语义。版本仍为 2.0.0，依赖范围未收紧；P3 与独立玩法待定项未纳入。

### 14.1 修复对应表

| 编号 | 实际改动 |
| --- | --- |
| BOSS-02 | 载入生命值期间避开战斗单次受伤限幅；加载后恢复正常受伤限幅。保留属性基础值与原版有效上限 |
| BOSS-03 | 战斗缴械复用精确物品白名单，保持同命名空间的其他物品不自动豁免 |
| BOSS-04 | 传送调度先读取受击更新后的倒计时，避免旧值覆盖 4 tick 延迟 |
| BOSS-05 | 四条 Boss 召唤入口返回真实实体加入结果；取消时保留召唤成本，成功重试只扣一次 |
| BOSS-06 | 取消、死亡和销毁按 Boss 归属清理地雷/仆从/新生成精灵；区块卸载保留战斗实体及 UUID 重连能力 |
| EQ-01 | 盾牌反伤使用 THORNS，并明确拒绝继续反射 THORNS，保留第二面盾的正常格挡效果 |
| EQ-02 | 精灵王戒以属性与 modifier ID 的组合记录应用和移除，不再遗漏共用 ID 的其他属性 |
| EQ-03 | 两种魔弓实现统一箭数、预检、总扣费和每箭魔力预算；多重射击一级按三箭计费 |
| EQ-04 | 两种魔弓伤害采用累计档位进度，移除蓄力跨档回零，保留既有档位规模 |
| EQ-05 | 自修/耐久冷却装备仅忽略 DAMAGE 的重装备变化；物品、槽位、数量和其他组件变化仍触发动画 |
| EQ-06 | 冻结跳过未加载位置，写块前发出可取消放置事件；静籁永恒仅在跨方块移动或每 20 tick 扫描，并恢复临时落地状态 |
| EQ-07 | 诅咒戒在扣费前检查敌对目标及玩家伤害许可，关闭 PvP 或禁止友伤时不施咒、不扣费 |
| EQ-08 | ETERNITY 和薯片救命放行 BYPASSES_INVULNERABILITY；本次原版标签中的 /kill 与虚空均不被拦截 |
| EQ-09 | 坐骑成功加入世界后才消耗放置物品；加入被取消时保留原栈 |
| M-03 | 无限酒换口味复制原酒的完整组件与绑定，仅改变酿造内容；新制酒保持原配方行为 |
| M-04 | 机器/花的邻块与扫描先核对加载状态；风箱二级读取和液态机器红石查询的间接邻块访问也纳入 |
| M-05 | 双子兰使用实际冷端温度，分别钳制冷热端，并防止反向边界产生负产能 |
| M-06 | 石中姬、蕴魔瑾、木兰通过放置者或固定无主身份发送保护事件；取消后不改块、不提交本次资源/冷却变化 |
| M-07 | 商友兰不再删除来源未知的折扣，依靠已有短时效果自然到期，避免相邻花互相干扰 |
| CLIENT-01 | EMI 增加石中姬实际条目和四条酿造展示路径；结果及余料调用实际配方计算，组件比较区分不同酿造，合成展示 ID 使用 EMI 规范的 `/` 前缀 |
| CHUNK-01 | 64/80 格定位射线在首个未加载区块前截断；已加载部分使用原版形状、流体和交互命中语义 |
| TEST-01 | 回音花临时夹具使用 try-with-resources 注销监听，并验证断言异常路径也释放监听器 |
| TEST-02 | 蕴魔土测试直接比较真实 commonTick 加速次数，不再修改共享 dayTime |

### 14.2 回归覆盖

- 原 P2 集合新增 **45 项 GameTest**：Boss 13、装备 17、机器/花/酿造 11、射线 3、监听器异常清理 1；另改写共享时间测试并修正既有商友兰断言。后续最初分型与自然蕴息宝珠另新增 8 项，见 14.5、14.7；19 项追加集合含既有测试，不与 208 直接相加为独立用例数。
- 定向集合共 **208 项**，包括上述 45 项、上一轮 19 项 P1 回归和 144 项相关既有测试。
- GameTest 首轮 `gt-r1` 为 204/208。四项失败对应夹具：MAX_HEALTH 原版有效上限 1024、mock 玩家头部朝向、远处 FULL 区块不等于实体区段可查询。已补前置断言和真实实体可见性检查，没有放松效果或物品数量断言。`gt-r2` 因资源入场条件未满足而未启动 Java，没有执行测试，不能计作 GameTest 失败；其未完成回执原样保留。
- 最终 `gt-r3` **208/208 必需测试通过**，Java 退出码 **0**，`process_exited=true`、`saved_after_stop=true`、`forced=false`；测试服正常保存后退出。
- 客户端探针实际读取 EMI 的索引，对照真实配方核对输入、结果组件、空瓶余料及输出反查；另核对真实服务端自修同步与 **30 个客户端 tick** 的手持高度，并保存实际画面。原始回执中的 `repair_continued_during_30_render_ticks` 是旧断言键名，实际采样入口是客户端 tick，不能据此表述为 30 个渲染帧。
- `client-r1` 自动断言通过，但画面复核发现合成展示 ID 警告及手持截图仍在加载画面。随后补 ID 规范、实际世界/界面就绪条件和相关错误日志门禁，首轮不计为完整视觉通过。
- `client-r2` 的自动断言与当时画面检查仍漏掉了空杯和透镜显示缺陷。用户反馈后重新打开问题；该轮“通过”记录只保留为当时的检查结果，不能作为这些物品显示正确的证据。原回执未修改。
- `client-r3` 用新增的同一探针配合旧 `ColorHandler` 做反例：16 个染色样本中 **15 个 alpha=0**，液体与透镜图标缺失；红色染料透镜原本已是不透明 ARGB，因此仍通过。该轮失败结果保留。
- `client-r4` 使用修复后的颜色处理，**16/16 样本的 alpha 均为 255**，非染色层保持 `-1`；同时核对 **11 条石中姬配方、103 条酿造展示**，6 张实际客户端截图人工复核通过。客户端 Java 退出码 0，正常保存退出，无强制结束。

**14.1～14.4 的定向验收完成：GameTest 208/208；客户端自动断言与本轮 6 张截图复核通过。** 此结论限于上述行为和画面；后续用户反馈单独记录，不扩展为整个玩家整包或全部渲染场景均已验收。

### 14.3 证据与执行边界

本轮编译与运行记录位于 `D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1`。通过 FML 加载编译后的类与资源运行，未使用新打包 JAR。已有客户端底座、服务端底座和共享依赖缓存均复用，生产进程未调整。

- 最终 GameTest：[结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/gt-r3/retained/002-result.json)、[实际日志](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/gt-r3/retained/001-process.log)、[完成回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/gt-r3/completion.json)。`gt-r3` 复用 [gt-r2 输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/input-manifest-gt-r2.json) 中的编译类与资源；不能因清单名称将其误记为第二轮已经执行。
- 显示修复后的客户端：[自动结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/retained/002-client-result.json)、[人工画面复核](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/visual-review.json)、[client-r4 输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/input-manifest-client-r4.json)、[compile-r6 编译日志](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/compile-r6.log)。
- 原失败和未完成回执保持原状，包括 `gt-r1`、未入场的 `gt-r2`、`client-r1`，以及显示缺陷反例 `client-r3`。后续通过结果另行记录，不覆盖早期证据。

首批 5 个远端可重建运行副本（`gt-r1`、`gt-r2`、`gt-r3`、`client-r1`、`client-r2`）在进程退出、证据保留后按 [阶段完成清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/cycle-completion.json) 清理；[实际清理回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/cycle-completion.json.cleanup.json) 记录清理 **284,961,318 字节**。后续 `client-r3`、`client-r4`、`fractal-r1`、`followup-r1`、`judah-r1`、`judah-r2` 共 6 个副本也已按 [追加完成清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/followup-completion.json) 清理，[回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/followup-completion.json.cleanup.json) 记录 **235,808,622 字节**。两批共 11 个副本、520,769,940 逻辑字节。对应 9 个辅助计划任务已注销，见 [任务清理记录](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/helper-task-cleanup.json)；其中剩余目录为 0 是该时刻快照，不包括之后创建的影刃验证副本。

共享引擎、依赖缓存、源码及必要证据保留。本机 `transfer` 清理被工具策略拒绝，见 [清理受阻记录](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/local-transfer-cleanup.json)，暂留且未记作已清理；追加验证仍使用该目录，本报告不把早期测量值写成最终占用大小。

测试不代替完整玩家整包验收。保护联动验证的是本次 NeoForge 事件取消和资源守恒，不宣称覆盖所有领地模组。Boss 卸载回归覆盖移除原因和新对象 NBT 重连；升级前没有归属标记的精灵仍按原寿命退出，避免误清其他战斗的旧实体。

### 14.4 用户追加：酒杯液体与透镜图标透明

用户指出酒杯和透镜成品显示为空后，重新检查了物品实际数据、模型层和颜色处理。问题在物品染色返回值：Minecraft 1.21.1 的 `ItemRenderer.renderQuadList` 将颜色的 ARGB alpha 传入顶点；旧 [ColorHandler.java](../src/main/java/io/github/lounode/extrabotany/client/renderer/ColorHandler.java) 返回的酿造 RGB、回退 RGB、未染色透镜白色和彩虹色没有 alpha，因而被当作完全透明。酒杯的 `layer0` 杯身仍可见，`layer1` 液体消失；透镜只有被染色的 `layer0`，整个图标随之消失。同一返回方式还影响盖亚魂钢锤和虚空万藏酒杯模式。普通染料颜色已经由原版转为不透明 ARGB，红色透镜是反例中的正常对照。

满酒的剩余次数与模型选择原本正确：无限酒默认 12 次，`swigs_taken=0` 时使用带液体层的满杯模型，耗尽时才选择无液体层模型。真实配方输出仍带酿造数据，EMI 投影也由实际配方 `assemble` 计算。本次只在物品颜色出口用 `FastColor.ARGB32.opaque` 或显式 alpha 255 补全 ARGB，非染色层继续返回 `-1`；没有改酿造效果、剩余次数、遗物绑定、配方、模型或贴图，也没有把药水透镜改成按酿造色着色。

[P2ClientProbe.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/P2ClientProbe.java) 新增实际配方成品、注册颜色处理器返回值和真实烘焙模型检查，并通过 `GuiGraphics.renderItem` 绘制样本。它同时验证满酒有液体层、空酒无液体层，避免仅检查 alpha 而把空杯强行画满。先用相同探针配旧颜色处理复现失败，再更换颜色处理复测；验证范围包括带酿造的鸡尾酒/满酒/手雷、空酒、五种未染色透镜、彩虹透镜、盖亚锤、虚空万藏酒杯模式、默认鸡尾酒/酒杯、回退酒杯和红色染料透镜，共 16 个样本。

- 旧实现反例：[实际对照画面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r3/verification/item-tints.png)、[15 个 alpha=0 的失败结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r3/verification/result.json)。
- 修复后：[物品显示对照画面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/retained/011-item-tints.png)、[EMI 无限酒成品画面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/retained/009-emi-p2-2.png)、[16/16 自动结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/retained/002-client-result.json)、[6 张截图人工复核](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/client-r4/visual-review.json)。

这次验证确认本地依赖组合下上述物品和 EMI 成品显示恢复，不代表所有资源包、着色器或其他物品外观均经过检查。客户端仍有 24 条 EMI 开发模式的标签未翻译警告；它们不属于本次 alpha 缺陷，未把警告存在误写成显示修复失败，也未声称已清除所有警告。`client-r2` 的漏检和 `client-r3` 的反例保留，不能用最终通过结果抹去此前检查的不足。

### 14.5 用户追加：最初分型的进度限制与空挥技能

用户表示两个 Boss 进度尚未获得，或不清楚是否已获得。本轮没有修改最初分型的业务逻辑；检查时将两条独立条件分开：

- [EgoRequirementHandler.java](../src/main/java/io/github/lounode/extrabotany/common/handler/EgoRequirementHandler.java:60) 的 `relicsNeedEgoDefeat` 是默认关闭的可选配置。开启后，未完成 `extrabotany:main/ego_defeat` 的生存玩家会受到交互限制，持有或装备的标签内遗物会被安全丢出。关闭这条规则，只解除这层限制。
- [FirstFractalItem.java](../src/main/java/io/github/lounode/extrabotany/common/item/relic/FirstFractalItem.java:90) 的幻影剑技能另有固定的空之律者进度门槛 `extrabotany:main/herrscher_defeat`，创造模式豁免。遗物绑定不会自动授予该进度；关闭本我配置也不会解除技能门槛。技能还须满足主手、绑定归属与攻击冷却条件。

[FirstFractalGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/FirstFractalGameTests.java) 新增 5 项回归，覆盖绑定不授予 Boss 进度、真实空之律者击杀后空挥生成四剑、真实参战者获进度后实体攻击生成四剑、本我配置关闭时保留物品但仍需空之律者进度，以及配置开启时真实击杀本我前后掉落行为。进度由实际 Boss 死亡路径授予，没有直接给测试玩家补发进度。

首轮 `fractal-r1` 运行新增 5 项与既有 11 项，共 **15/16 通过**，失败的是空挥测试的包顺序。旧夹具先调用动画包，再调用技能包，并错误断言动画不会消耗冷却。实际 NeoForge 客户端 `Minecraft.startAttack` 先同步触发 `LeftClickEmpty`，模组在事件内发送技能包，随后 `LocalPlayer.swing` 才发送动画包；服务端 `handleAnimate` 调用 `ServerPlayer.swing`，该方法确实重置攻击冷却。自定义包的默认主线程处理只排队一次，没有发现会把两包反序的额外异步调度。

已仅修正夹具为真实顺序：先验证完整冷却，调用技能包并断言恰好四剑和冷却清零；动画包到达前重放不增剑；再调用真实动画包，验证冷却仍为零，动画后重放仍不增剑。没有删除冷却断言或放松剑数要求。此前未解锁限制、真实击杀授予进度等场景已通过；首轮失败结果保留。首轮范围见 [fractal-r1 输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/control/fractal-r1/input-manifest.json)。

**统一复测 `followup-r1` 为 19/19 通过**（最初分型 5、既有 RelicSword 9、EgoRequirement 2、自然蕴息宝珠 3），Java 退出码 0，正常保存退出。见 [实际结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/followup-r1/retained/002-result.json) 与 [完成回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/followup-r1/completion.json)。 测试范围见 [followup-r1 输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/control/followup-r1/input-manifest.json)。本次排查没有移除进度门槛、自动授予玩家进度或更改玩法。

### 14.6 用户追加：犹大的誓约图案接缝

问题位于 [JudahOathRenderer.java](../src/main/java/io/github/lounode/extrabotany/client/renderer/entity/JudahOathRenderer.java:71) 的镜像轴：贴图右边缘是两半图案的接缝，而当前生成模型的 U 坐标方向与旧版 `IconHelper` 的反向 U 坐标不同。沿用旧变换时，镜像绕错了边缘，使两半图案分开。本次业务改动仅在每半模型绘制前增加 `translate(-1F, 0F, 0F)`，将贴图右边缘移到镜像轴；图案尺寸、高度、旋转、颜色、贴图及犹大的攻击行为均保持原状。

[JudahClientProbe.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/JudahClientProbe.java) 使用实际注册实体渲染器和烘焙模型捕获顶点，检查三种变体在 0°、45°、90°、137° 共 **3 × 4** 组旋转下的右边缘接缝；同时用同一模型和冻结的旧变换作负对照，要求旧变换重现分离、修复后接缝闭合。探针另安排正面与 45° 实际客户端画面，不能用纯矩阵计算代替实际渲染检查。

首轮 `judah-r1` 的实际画面已经合拢，但几何夹具把生成模型的像素边壁也纳入最大 U 边界，测得 0.03625 格而失败。NeoForge 会单独修整边壁的 UV；修正探针后按实际输出法线分别取正背图像面，并严格核对每面两个半片的顶点数量。接缝阈值仍为 0.0001，旧变换负对照仍须大于 2.5，没有通过扩大容差或再改业务实现来迎合测试。首轮 [原失败结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/judah-r1/verification/result.json) 保留。

**最终 `judah-r2` 的三种变体、四个朝向正背面检查全部通过**，旧变换仍重现约 2.64 格的单侧接缝偏移。Java 退出码 0，正常保存退出；[正面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/judah-r2/retained/006-judah-front.png) 和 [45° 画面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/judah-r2/retained/007-judah-angle.png) 均已人工复核。见 [实际结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/judah-r2/retained/002-client-result.json)、[画面复核](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/judah-r2/visual-review.json) 与 [输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/input-manifest-judah-r2.json)。

本节 `judah-r2` 的原始运行回执仍保留待人工画面复核状态；后续 `visual-review.json` 与 14.3 所列 `followup-completion.json` 已记录画面通过及流程完成，没有覆盖原回执。

### 14.7 用户追加：自然蕴息宝珠与玩家断开

本轮没有修改 [NatureOrbItem.java](../src/main/java/io/github/lounode/extrabotany/common/item/equipment/bauble/NatureOrbItem.java) 的业务逻辑。新增 [NatureOrbGameTests.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/NatureOrbGameTests.java) 的 3 项回归：

- 将有能量的宝珠放进真实 Curios 槽位，通过实际 Botania Curios 包装器连续调用 120 次佩戴回调，逐次核对产魔、回血、清除两种有害效果、保留有益效果及能量消耗；核对实际发出的两条消效包及其原版编解码。
- 使用运行依赖中的 Curios `SPacketSyncStack.STREAM_CODEC`，在能量消耗前后分别编解码，验证数量、名称、自定义数据和自然能量组件完整，并覆盖消耗后跨能量档位。
- 对空宝珠调用 120 次相同回调，验证不产魔、不回血、不消效、不改写物品组件，也不发送消效包。

这些测试同步推进饰品回调与玩家 `tickCount`，没有运行 120 个完整服务器 tick，也没有建立真实玩家客户端连接。因此它们覆盖回调和包编解码路径，不能冒充在线持续佩戴或踢人复现。三项均在 14.5 所列 `followup-r1` 的 19 项集合中通过；实际结果与日志已保留。

现役 moon11 SCEX 的日志检查为只读，取得最近两日的 6 份压缩日志与 1 份当前日志快照。按 [orb-log-matches.json](D:/SCEX-Work/02-Server-Client/Operations/legacy-extrabotany-orb-20261025/orb-log-matches.json) 的 `lines` 精确求和，共 **7 份、230,219 行**。`NatureOrbItem`、`NatureEnergyImpl`、`onWornTick`、`Failed to handle packet`、`Ticking player` 及 ExtraBotany Java 包名均无命中，没有找到可归因于宝珠的异常堆栈。各文件哈希和断开原因计数保留在该证据中。

日志中的连接断开合计包括普通 `Disconnected` 104 次、`Timed out` 101 次、缺少 NeoForge 或版本不兼容提示 8 次、`Server closed` 6 次及控制台踢出 1 次。这些原因没有与佩戴宝珠的操作对应起来，不能据其次数认定宝珠导致断开，也不能由缺少堆栈反推宝珠一定无问题。

另核对了实际部署身份：[现役 ExtraBotany JAR](D:/SCEX-Work/02-Server-Client/Operations/legacy-extrabotany-orb-20261025/server-mod-identity.json) 的整体 SHA-256 与本机 `build/libs` 旧包不同，未据版本号相同推定代码相同。复用本地同哈希服务端副本，比较宝珠、自然能量、相关辅助和注册、Botania 包装器与 Curios 同步链的 **10 个类、64 个相关方法**，反汇编逐行一致，见 [比较记录](D:/SCEX-Work/02-Server-Client/Operations/legacy-extrabotany-orb-20261025/deployed-orb-code-comparison.json)。该记录之后又只读核对了现役 Botania/Curios 文件，二者 SHA-256 均与本轮受测依赖一致，见 [现役依赖回执](D:/SCEX-Work/02-Server-Client/Operations/legacy-extrabotany-orb-20261025/production-dependency-hashes.json)。

**尚未重现“在线佩戴宝珠导致踢出”，不能称为已修好。** 判定根因仍需发生问题的玩家客户端断开日志及准确时间，与服务端同一时间的记录对应；在此之前保留问题，不新增玩法修改。现役服务器仅接受了只读检查，没有部署、配置修改或重启。

### 14.8 用户追加：影刃在 JEI 与创造栏的异常图标

用户确认“乱码”指图标碎块或异常图案。`shadow_katana.png` 是 16×128 的八帧动画条，但缺少对应动画元数据，Minecraft 将整条当成一张静态纹理。业务修复仅新增 [shadow_katana.png.mcmeta](../src/main/resources/assets/extrabotany/textures/item/shadow_katana.png.mcmeta)，恢复经典配置：`frametime=4`，帧序列 0～7。没有修改攻击、技能或真影刃的静态贴图。

[ShadowKatanaClientProbe.java](../src/gametest/java/io/github/lounode/extrabotany/forge/gametest/ShadowKatanaClientProbe.java) 从真实创造栏构建结果取得两把剑，用实际 `GuiGraphics.renderItem` 绘制原尺寸和四倍图标。检查真实图集尺寸 16×16、八帧、每帧 4 tick、烘焙模型使用该精灵，并保留真影刃静态对照；另从同一 PNG 不带元数据重新构造 `SpriteContents`，验证旧实现为 16×128 且没有动画 ticker。名称和说明检查使用 Minecraft 最终显示的字符及样式。JEI 集成没有影刃专用模型或渲染注册，与创造栏共享标准物品资源；本轮实际截图为这个共同渲染入口，不宣称已打开 JEI 页面逐项验收，也未逐帧采集 GPU 动画换帧。

首轮 `shadow-r1` 中影刃的全部图集、模型、动画元数据及负对照检查通过；真影刃的说明检查失败。原因是 Botania 未绑定遗物提示的合法 `§6/§7` 颜色码被探针按原始文本误判为乱码。仅修正探针，按最终字符与样式判定，并增加合法颜色正对照、`§k`、组件混淆样式及替换字符负对照；实际遗物提示仍须存在，未删除说明检查。首轮失败记录和画面保留，生产元数据未再次修改。

**最终 `shadow-r2` 自动检查与实际截图复核均通过**，Java 退出码 0，正常保存退出。影刃和真影刃图标完整，名称及影刃说明清晰；实际遗物未绑定提示的最终字符为“遗物，未绑定”，颜色由金色和灰色样式表达，没有混淆文字。见 [自动结果](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-r2/retained/002-client-result.json)、[实际图标画面](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-r2/retained/006-shadow-katana.png)、[画面复核](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-r2/visual-review.json)、[输入清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/input-manifest-shadow-r2.json) 及 [编译日志](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/compile-r11.log)。

两轮影刃副本在进程退出、画面复核和证据保留后完成清理；[完成清单](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-completion.json) 与 [清理回执](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-completion.json.cleanup.json) 记录 **75,649,737 字节**。原失败回执与待画面复核回执保持原状，后续完成清单记录流程结束。新增两个辅助任务已注销，见 [任务清理记录](D:/SCEX-Work/01-Mods/Botania/scex-extrabotany/p2-20261025-r1/evidence/shadow-helper-task-cleanup.json)。

最终源码与运行输入核对覆盖 682 个 Java 文件及 2,623 个运行输入。相较犹大完成验证时，Java 仅新增影刃探针；动画元数据的源码、编译资源与实际输入哈希一致。版本保持 2.0.0，既有 JAR 的数量、大小、SHA-256 和修改时间均未变化；未打包、部署或发布。本机传输目录因前述策略拒绝仍保留。
