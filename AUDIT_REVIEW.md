# ExtraBotany 1.21.1 独立复核 — 2026-09-05

> 后续生产数据验收已发现本报告未覆盖的实际注册缺口。本报告是 scex.3
> 开发审查历史，不能作为 scex.1 存档无损升级许可。scex.4 糖果修复及仍存
> 升级阻塞详见 `LEGACY_CANDY_COMPAT.md`。

## 判定

原 `2.0-scex.2-dev` 不能直接认定为功能迁移完成：独立测试证实存在附魔土保护寿命边界失效、交易兰折扣失效两项源码缺陷，另有语言键与构建锁定声明不准确。

这些问题已在 `2.0-scex.3-dev` 修复。当前可判定为**已完成本次审查范围的开发候选**，可交付隔离整包验收；**生产验收尚未完成**。该判定不等同于穷举所有玩法或证明不存在其他缺陷。

没有部署生产、操作玩家环境、提交 Git 或丢弃原有工作树改动。初始分支为 `scex/1.21.1-port`，HEAD 为 `a4d4f2a968d559752fa3bd6e609544473109d983`，初始状态为 235 modified、6 deleted、16 untracked groups。原始差异另存 `audit-review/initial.patch`，并逐文件对照原源码归档检查资产保留。

## 按严重级别排列的发现

| 级别 | 发现、影响与处理 | 独立证据 |
| --- | --- | --- |
| P2，已修复 | Hydroangeas 在 `passiveDecayTicks=72000` 时，即使位于附魔土上也先被 Botania `++age` 销毁；TAIL 恢复计数无法撤销销毁。现在在 HEAD 保存并临时置零计数，TAIL 恢复原年龄，保留正常产能执行。 | `gametest-before-fix.log`：原两项通过、新寿命边界项失败；最终测试验证保护花存活、普通土对照花死亡、保护花保存年龄仍为 72000。 |
| P2，已修复 | 交易兰 Mixin 未迁移 Holder 访问；`getEffect() instanceof DiscountMobEffect` 恒不匹配注册的 Holder，导致折扣不生效。现在使用 `.value()` 判断与转换。 | `trade-before-fix.log`：实际村民交易测试失败；修复后 20 绿宝石报价在 50% 效果下得到 `specialPriceDiff=-10`。 |
| P2，已修复 | 原“依赖锁定”只固定直接声明，没有传递依赖锁。增加四个 compile/runtime/test classpath 的 `gradle.lockfile`。 | `dependencies.log` 与后续不带 `--write-locks` 的成功构建。Botania 时间戳 POM 自报 SNAPSHOT，须排除自动锁并保留精确时间戳声明，不能换成浮动 SNAPSHOT。 |
| P3，已修复 | Patchouli 模板回调调用客户端类，却列在 common Mixins。移动到 client 列表。原普通专服未触发异常，因此这是加载范围收紧，不声称已复现专服崩溃。 | 实际 Patchouli JAR 的 `BookRegistry.reloadContents(Level)` 字节码及本地回调源码；调整后的专服与客户端均完成启动。 |
| P3，已修复 | en_us 缺少 zh_cn 中两条 `sekai_no_hakoniwa` 旧成就键；此前“语言键对齐”不成立。补齐英文。 | 原 JAR/源码直接比较；最终两种语言均为 707 键。 |
| P3，已处理 | Gradle 分发版本为 9.2.1，但 wrapper JAR 并非该版本官方校验值；重生成 wrapper JAR 和脚本，保留 120 秒超时。机器 JAVA_HOME 原指向 Java 17，复现命令需显式指定 Java 21。 | [Gradle 官方校验清单](https://gradle.org/release-checksums/)：9.2.1 wrapper 为 `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`；本地一致。 |

未发现需阻止本次开发候选交付的 P0/P1 问题。上表是本次独立复核发现，不代表对原 235 个改动文件逐行形式化证明。

## 实际改动文件

路径均相对本仓库：

- `Xplat/src/main/java/io/github/lounode/extrabotany/mixin/botania/HydroangeasEnchantedSoilMixin.java`：寿命保护边界修复。
- `Xplat/src/main/java/io/github/lounode/extrabotany/mixin/TradeOrchidEditSpecialPrice.java`：效果 Holder 解包。
- `Forge/src/main/java/io/github/lounode/extrabotany/forge/gametest/ExtraBotanyGameTests.java`：增加三项回归测试，并覆盖全部五种 damage type。
- `Xplat/src/main/resources/extrabotany_xplat.mixins.json`：模板回调限定客户端。
- `Xplat/src/main/resources/assets/extrabotany/lang/en_us.json`：补两条键。
- `build.gradle`、新增 `gradle.lockfile`：锁定解析后的 classpath；Botania 保留时间戳例外。
- `gradle.properties`：版本升为 `2.0-scex.3-dev`。
- `gradle/wrapper/gradle-wrapper.jar`、`gradle/wrapper/gradle-wrapper.properties`、`gradlew`、`gradlew.bat`：统一 9.2.1 wrapper。
- 新增 `scripts/audit_port.py`、`scripts/isolated-review-runs.gradle`：资源/JAR 审计和隔离启动复现工具。
- `PORTING.md`、本报告：更新结论、证据边界，明确原记录为历史声明。
- `audit-review/`：本次本地证据、初始差异、日志和审计结果；不作为游戏资源打包。

未修改任何模型、PNG、动画或 OGG。重新 runData 后，规范生成资源与原源码归档一致。

## 构建输入与实现检查

MC 1.21.1 / NeoForge 21.1.248 / Gradle 9.2.1 / 本次 Oracle Java 21.0.12+7；Java 编译目标 21。Botania 精确坐标 `456-20260822.093314-4`，运行显示 `456-SNAPSHOT`；Patchouli 1.21.1-93、Curios 9.5.1+1.21.1、JEI 19.44.0.405。EMI、KubeJS 仅编译依赖，未补做可选组合运行测试。

上游 HEAD 与交接 commit 一致，MIT LICENSE 保留并打入 JAR。源集只包含 Xplat/Forge 当前源码和 `src/generated/resources`；Fabric 和旧生成目录未进入目标 JAR。JAR 禁用文件时间戳、启用稳定排序。版本锁不覆盖全部插件/工具配置，也没有启用完整 Gradle artifact checksum verification；两次相同机器/缓存构建一致不等同于跨机器、跨 JDK 厂商的复现证明。

核对了 common/client Mixin 列表和实际目标签名，包括 SpecialFlower `commonTick`、Hydroangeas `tickFlower`、Daffomill accessor、Wand 绑定、Gaia arena/missile、LootTable consumer、ServerPlayerGameMode、HUD 和 Patchouli。`defaultRequire=1`，物理 client/server 实际加载提供动态补充证据；没有做所有目标方法的玩法触发穷举。

注册入口显式绑定方块/物品/实体/配方/属性/效果/声音与能力事件；附魔土注册名为 `extrabotany:enchanted_soil`，Enchanter、奖励、方块状态、模型、掉落和标签引用该兼容对象。加速钩子核对“同一在场方块实体、非浮空花、下方附魔土”后执行额外 flower tick。ExtraBotany 花桥接 registry-aware NBT 保存和同步；物品通过 `CUSTOM_DATA` 保留旧键，测试了保存读取和复制隔离。

自定义 payload 注册方向、codec 和服务器处理入口已做源码核对；客户端处理器延迟解析。该静态检查与启动成功不能代替真实连接、各包往返与恶意输入测试。所有五种 damage type 在实际注册访问中存在；声音 Holder 在启动注册并由测试核对；配方、标签、掉落的数据加载由 runData 和普通专服加载提供证据。

## 实际执行与结果

在仓库根执行，显式设置 `$env:JAVA_HOME='<Java 21 JDK>'`：

| 命令/检查 | 实际结果/证据（audit-review 下） |
| --- | --- |
| `gradlew.bat clean build --console=plain`，修复前 | 成功、无编译警告；`clean-build.log`。`test NO-SOURCE`，不能把它算作单元测试通过。 |
| `gradlew.bat dependencies --write-locks --console=plain` | 生成锁；Botania 自动 SNAPSHOT 锁冲突经明确排除该模块解决，精确时间戳未变。 |
| `gradlew.bat runData runGameTestServer build --console=plain` | runData 所有 provider 完成；初轮修复后 3/3 通过；`validation-after-fix.log`。 |
| `gradlew.bat runGameTestServer --console=plain`，最终源码 | **5/5，通过，1.137 s**；`final-gametest.log`。另保留两项缺陷各自修复前失败证据。 |
| `gradlew.bat -I audit-review/isolated-runs.gradle runServer --console=plain` | 新建测试世界、25577，`Done (7.022s)`；2,927 recipes / 2,500 advancements；输入 stop 后保存全部维度退出。`server/logs/latest.log`。此普通启动早于最后的交易 Holder 修复，最终该修复由真实服务器 GameTest 覆盖。可用归档中的 `scripts/isolated-review-runs.gradle` 重现相同配置。 |
| `gradlew.bat -I audit-review/isolated-runs.gradle runClient --console=plain` | 最终源码物理客户端在新目录加载，完成图集/音频、Patchouli 342 JSON、JEI 图集；`final-client.log`。观察日志稳定后主动终止仅本次测试客户端 PID，因此 Gradle 的终止退出不计作程序自然崩溃。未进行标题界面截图或游戏内目检。 |
| 两次最终 `clean build` | 均成功、无编译警告；`final-clean-1.log` / `final-clean-2.log`；比较 `build-1-hashes.json` / `build-2-hashes.json`，运行与 sources JAR 均字节一致。 |
| `py scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.3-dev.jar` | `final-resource-audit.json`；校验 ZIP CRC、Java 21 classes、JSON、必备 metadata/Mixin class、局部模型纹理/声音引用、PNG chunks CRC、动画帧范围、语言键和规范资源路径。 |
| Java 21 `jar --validate --file <runtime.jar>` | 成功，exit 0；输出留在 `jar-validate.log`；与资源审计互补。 |

最终资源口径：585 Java classes、1,059 JSON、383 models/blockstates、193 PNG、11 OGG；规范源资源文件 1,274（排除 `.cache`，不计目录和模板展开输出）。旧记录的 1,303 未作为本轮计数结论沿用。审计不声称已解析所有动态字符串、外部命名空间或 Patchouli 文本内全部链接；真实客户端 reload 补充验证其启动资源路径。

日志存在非 ExtraBotany 问题：Botania 的 `patchouli_books/.../README` 被记录为 **ERROR 并忽略**；全新专服目录首次缺少 `server.properties` 也记录 ERROR，随后生成默认配置并正常启动；缺失 refmap、vanilla 命令歧义、union resource URL、山羊音效、Patchouli/JEI keybinding 时序及 shader sampler 警告。不能称“零 ERROR/WARN”。没有将已复现的 ExtraBotany 测试失败归咎于依赖。

## 交付产物

以下路径均以仓库根为基准。

| 版本 2.0-scex.3-dev | 字节 | SHA-256 |
| --- | ---: | --- |
| `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.3-dev.jar` | 3,850,956 | `f04cd57145ac8c5407d317205ad8a26f3675375557b7b1f9409263c7dc243fc8` |
| `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.3-dev-sources.jar` | 3,326,358 | `7a9744dc3e88595dc26fdf75d565ac3b203e110a71a23294e60d7a89727cff17` |

当时的独立源码归档以版本化文件名保存；大小和 SHA 记录在外部审计清单，避免归档内自引用校验值。归档从实际工作树生成，含未提交源码；排除 `.git`、`.gradle`、`build`、`run`、`.cache`、`audit-review`、`deliverables` 和两个旧生成目录。旧 scex.2 归档只读保留，SHA/大小已与交接值核对一致。逐文件比较旧归档：没有缺失文件，生成资源未改变，差异限于本报告列出的修复与文档。

## 生产验收与回滚

仍需用整包测试副本验证：真实客户端—服务端所有 payload、Gaia III 完整战斗/重连/切维度、1.20 世界升级与物品/方块实体数据、附魔土注册变化、所有盔甲/渲染器目检、JEI 实际交互及可选 EMI/KubeJS、广泛玩法和平衡。这些明确边界不应冒充已验证，也不应单凭它们把本轮修复后的开发源码判为未完成。

交由 `SCEX-长期维护` 执行版本核对、停服备份、测试副本验收后再决定部署。保留原 JAR、依赖、配置和完整世界备份；如果新版本已写入世界，回滚应恢复配套世界备份，不能仅换回旧 JAR。不要同时放入 scex.2 和 scex.3；Botania 必须使用已验证的精确构建。未对生产目录做任何修改。
