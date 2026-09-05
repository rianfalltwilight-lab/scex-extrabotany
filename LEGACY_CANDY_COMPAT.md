# scex.4 糖果旧物品兼容修复 — 2026-09-05

**七个已确认旧物品 ID 的功能兼容已恢复，可供森林法杖隔离联调；整体 scex.1 → scex.4 生产升级仍阻塞。**

本轮不删除或改写任何玩家数据，不注册空占位物品，不启动/停止/修改生产。
此前 scex.3 的开发测试结论不证明旧世界无损兼容；真实存量的未知物品风险成立。

## 输入与实际缺口

现役只读基线 JAR：
`extrabotany-neoforge-1.21.1-2.0-scex.1-gaia3-hotfix.jar`（归档基线，不包含在仓库中）

SHA-256：`8051956c2b045b9f28e78fe9c25b36235c83d118ab4823bcc31dc3c188857f42`。
该值与已有精确二进制注册盘点一致；又用 javap 直接检查本次现役文件的糖果类。

维护任务提供 `old-item-nbt-paths.json`，确认糖果袋不仅出现于配方字符串，
还存在于 sophisticatedbackpacks 的 count=4 栈、AE cell key、物品实体和手持物品。
148 个字符串匹配、122 个 id 字段**不是玩家物品总数**，其中含实体及历史备份。

缺失是基线差异造成：选用的 MIT 1.20.1 源码无生产 1.21.1 二进制增补的糖果内容。
现在恢复 `extrabotany:candy_bag`、`candy_eins`、`candy_zwei`、`candy_drei` 原 ID。
不需要 ID 映射或世界 NBT 迁移；原 count 和 CUSTOM_DATA 组件通过正常 ItemStack codec 保留。

## 行为与资源

- 糖果袋是功能性 RewardBagItem：打开一袋消耗一件，原 loot table 三次等权抽取三类糖果，掉落立即可拾取，原开袋音效保留。
- 三类糖果均为始终可食用、2 饱食度、0.15 saturation modifier、14 tick 食用；服务端玩家治疗4生命并获得200 tick、amplifier=1 的速度/跳跃/急迫。
- 12月16日至1月2日使用原圣诞名称及 `seasonal_christmas` 模型 predicate。
- 原四条合成配方、解锁 advancement、loot table、8模型和8纹理按字节恢复；没有生成替代贴图或借用错误模型。
- 发现并修复共享 RewardBagItem 中另一个旧行为回退差异：无效 LootTable 字符串现在回退默认掉落，不再对 null 调用 withPrefix。

Java 在可维护源码中按行为契约重新实现。资源取自用户明确提供的现役 JAR，
其 neoforge.mods.toml 声明 MIT License；保留现有上游 MIT 与作者署名。
每个复制资源的精确路径和 SHA 在 `compatibility/candy-resource-provenance.json`。
反编译参考没有作为正式 Java 源码或源码归档的一部分复制。

## 验证

后续冷备份真实 NBT 解析又确认 `element_rune`、`sin_rune` 和
`music_disc_herrscher_of_the_void`：已全部加入本轮。两个符文在现役本来就是
普通材料 Item，现恢复原配方（9种原料合成8件），这不是用占位类替代功能。
唱片保留 EPIC、不可堆叠默认属性、真实 `herrscher_of_the_void` 歌曲、
`music.herrscher` 声音、201秒/comparator1；同名 salvation.ogg 与现役逐字节一致。
恢复唱片标签及 Gaia III 原20%唱片池中的两种等权条目。额外资源来源与哈希见
`compatibility/confirmed-item-resource-provenance.json`。

维护输入 `missing-items-backup-summary.json` / `missing-items-backup-hits.json`
读取已验证冷备份，7,712 region、1,571,652记录；三种ID有11处栈表示，包含
过滤容器和AE存储键嵌套，不能相加为唯一玩家存量。其余24种仅非id引用，不能
当作库存；一个旧临时playerdata EOF为既存边界，未擅自修复生产数据。

Java 21.0.12 / NeoForge 21.1.248 / Botania `456-20260822.093314-4` / Gradle 9.2.1。

```powershell
$env:JAVA_HOME='<Java 21 JDK>'
.\gradlew.bat compileJava runData --offline --console=plain
.\gradlew.bat runGameTestServer --offline --console=plain
.\gradlew.bat clean build --offline --console=plain
py scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.4-dev.jar
```

- 编译、runData、独立 clean build 成功。不要将 `runGameTestServer clean build` 合成一次 Gradle 调用：本次遇到 Gradle 清理任务调度死锁，测试已通过，拆分调用后构建成功，日志保留。
- 最终 GameTest **9/9 通过，907.6ms**，包含原五项、两项糖果兼容和两项新增真实存量回归。
- 旧 count=4 的四种物品分别成功 parse、save、写入二进制 NBT、再次读回并逐栈比对；带 LootTable 和未知附加键的 CUSTOM_DATA 保存重载一致。
- 实际使用重载后的旧袋：数量4→3，世界产生总数3的合法糖果；测试食用数量、治疗、饱食度、效果和节日边界。无效旧 LootTable override 回退通过。
- 静态审计验证 loot table 与现役字节相同（各分支默认相同权重），不是依靠少量随机抽样声称概率均匀。
- 元灵符文旧count=2/4、大罪符文count=2/6保存重载保留；两种原配方在真实 RecipeManager 匹配并 assemble 出8件。旧唱片count=2不会被默认最大堆叠1静默截断；拆1件插入实际唱片机后开始播放，歌曲/比较器正确，方块实体保存重载并取回准确唱片。
- 资源/JAR审计零错误，Java 21 jar --validate exit 0。旧数据保留验证是限定夹具和正常 ItemStack codec；尚未在所有 AE/背包/女仆系统的实际容器中完成全世界加载保存。
- 本轮日志在 `audit-legacy/`。客户端资源加载证据另存 `client.log`，不能替代游戏内外观和实际网络交互验收。

## 其他注册缺口：不得解除升级阻塞

对哈希完全相同的现役 JAR 注册声明与本次实际运行 ITEM registry 比对，糖果四 ID 修复后曾有 **73个普通物品 ID 缺失**；上述新增三项已修复，最终剩 **70项**。
这是实际 make/ALL 注册声明与运行注册表的差异，不是模型文件名猜测。
清单、旧类名和字段名在 `compatibility/scex1-registry-gap-report.json`，同时保留初始73项、最终70项和实际运行物品注册表。

其中 binder、bottled_flame/star/pixie、photon_shotgun、miku盔甲、universal_petal、element_rune、sin_rune、peace_amulet、若干外观饰品及虚空唱片在此前世界字符串扫描中有匹配。
这些旧字符串匹配不能直接当作存量数量；当前冷备份的真实 NBT 字段交集由维护任务完成，上述三项已修复，其余非id引用不能冒称库存。

报告还列出10个方块/流体相关候选 ID；这是对 item registry 的候选比对，其中如流体方块本身可能无对应物品，不能把这10项直接计为已证明缺失的普通物品。必须针对对应 registry/真实 NBT 再核验。

本轮按紧急修复范围完成七项实证关联内容，不用空占位类掩盖其余数十种功能缺失。
**本 JAR 只供继续隔离兼容验收，不批准替换生产加载旧存档。**

## 交付与联动

版本 `2.0-scex.4-dev`；以下路径均以仓库根为基准。

| 文件 | 字节 | SHA-256 |
| --- | ---: | --- |
| `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.4-dev.jar` | 3,892,865 | `9b55f02f6ba72b395dcb9eaa650b0b5268faaaf0e7eb530dcbe8bdffae644f35` |
| `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.4-dev-sources.jar` | 3,360,241 | `190d1a406cd06c354cab25beb8880f268ef886cb76964f979425ff04de624a0a` |

联调交付使用同哈希的冻结副本 `deliverables/scex.4-legacy-compat-final/`，
交付后不再重写这些文件。任何后续源码修复必须产生另一个版本和新的验收哈希。

完整源码归档采用独立 scex.4 文件名，不覆盖 scex.2/3；最终路径/大小/哈希见外部交付清单和任务回复。
源码归档包含 compatibility 证据清单，排除构建缓存、运行目录及审计中间文件。
已通知森林法杖任务 `01a06c4f-5a74-7520-b418-003e076ce7ef`：兼容模组必须改为精确锁定 scex.4，不能沿用 scex.3 的1.1.0候选。

原冷备份保持不动。后续如批准升级，必须先验证所有真实存量缺失 ID 的功能和保存重载；回滚须恢复匹配的 JAR/配置/世界备份，不能只替换 JAR。

## 2026-09-05 森林法杖 1.2.0 联调验收补记

**森林法杖兼容层的隔离联调通过；总体生产发布仍为 BLOCKED。** 本补记依据
森林法杖兼容项目的 `VALIDATION.md` 及其 evidence 日志记录，属于接收并核对的
外部验收结果，本次文档更新未重新运行测试。

兼容模组版本 `1.2.0`，精确依赖 ExtraBotany `[2.0-scex.4-dev]`；JAR 大小
7,408 字节，SHA-256 `f5f6bdbc7a58d627760867377a19309d52bd4df7aec266c19147c61be601f7a1`。
源码与验收证据保留在独立兼容项目中，不包含在本仓库。
被测 ExtraBotany 为上述冻结运行 JAR，SHA-256
`9b55f02f6ba72b395dcb9eaa650b0b5268faaaf0e7eb530dcbe8bdffae644f35`。

| 验收项 | 结果与范围 |
| --- | --- |
| 兼容层构建 | `gradle.bat --no-daemon --offline clean test build` 通过，契约测试2项通过；发布 JAR 不含测试 harness。 |
| 真实客户端 | 7/7 交互断言通过；Reikarlily → Mana Spreader、Agricarnation → Mana Pool、Mana Pool → Manalink 三组绑定保存、关闭、重开后保留。 |
| Manalink 写入 | `SCEX_MANALINK_BIND_APPLIED` 恰好2条：客户端1次、服务端1次。 |
| 最小独立服务器 | `Done (3.823s)`，正常保存3个维度并停止，无 ERROR。 |
| SCEX 整包隔离服务器 | 临时236 JAR，`Done (1.487s)`，正常保存9个维度并停止；ERROR 共164条，数量及 logger 分布与 scex.3 基线一致，未发现兼容层新增错误特征。 |
| 测试环境恢复 | 整包测试实例恢复为235 JAR；未部署生产环境。 |

上述7项是兼容层客户端交互断言，不替代也不改写本项目既有的9/9 GameTest。
整包 ERROR 基线一致不代表整包无错误；客户端仍有 Manalink 旧空字段
`Not a map: null` 日志噪声，报告确认写入及保存重载成功。

已核对的证据文件位于该归档根的 `evidence/`：

| 日志 | SHA-256 |
| --- | --- |
| `client-interaction-scex4-final.log` | `043288dc6da89df179ebd261aa95c4d22fd2022b42176925d4af37fc99f0cdeb` |
| `dedicated-server-scex4-final.log` | `284a095704707470586116452511256620ebbd0d45eb5c444188484b745dd4d6` |
| `full-pack-server-scex4-final.log` | `bfaef90f23d18762d5fe14161fd62cf6d316662c9de5f2c2d1547ee5076702d3` |

本次仅更新工作树验收文档；`deliverables/scex.4-legacy-compat-final/` 内运行
JAR、sources JAR、完整源码 ZIP 和 `manifest.json` 均保持冻结，不把本补记回填
到冻结归档。仍有70个普通物品注册缺口及其他 registry/真实 NBT 的未完成核验，
本次森林法杖验收不能解除旧世界升级阻塞或作为生产部署授权。
