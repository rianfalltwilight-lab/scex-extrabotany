# scex.5 完整旧注册恢复（2026-09-05）

版本：`2.0-scex.5-dev`。目标：Minecraft 1.21.1、NeoForge 21.1.248、Java 21。
这是供 SCEX 隔离联调的完整开发交付，生产部署仍由 `SCEX-长期维护` 执行备份、整包验收与批准。
此前冻结的 scex.4 交付不作修改。本文取代它的“仍缺少其余旧注册项”开发阻塞结论，不追认生产升级许可。

## 输入、来源与实现范围

基线是归档 `extrabotany-neoforge-1.21.1-2.0-scex.1-gaia3-hotfix.jar`，
SHA-256 `8051956c2b045b9f28e78fe9c25b36235c83d118ab4823bcc31dc3c188857f42`。
可维护源码基于 MIT 上游 1.20.1 / 1.9.2，Git HEAD `a4d4f2a968d559752fa3bd6e609544473109d983`，
保留所有既有 SCEX 工作区修改。未找到与 scex.1 完全一致的可维护 Java 源码；
反编译仅用于核对行为、NBT、模型数字与注册契约，新实现落在 `Xplat`、`Forge`，不将反编译 Java 打包成正式源码。
复制的原始资源及哈希见 `compatibility/full-resource-provenance.json`，上游 LICENSE、作者和素材署名保留。

独立开发服读取 BuiltInRegistries 后逐项比较，旧注册项缺失为零：

| 注册表 | scex.1 | scex.5 | 旧项缺失 |
|---|---:|---:|---:|
| Item | 242 | 243 | 0 |
| Block | 98 | 99 | 0 |
| BlockEntityType | 30 | 30 | 0 |
| EntityType | 34 | 34 | 0 |
| Fluid / FluidType | 2 / 1 | 2 / 1 | 0 |
| ArmorMaterial / MobEffect / SoundEvent | 7 / 11 / 22 | 7 / 11 / 22 | 0 |
| 其余已存在注册表 | 相同 | 相同 | 0 |

额外的一种物品/方块是前轮修复保留的 `enchanted_soil`。没有用空 Item 替代旧功能。
此前七个已确认旧物品（四种糖果、两个符文、律者唱片）及其测试继续保留。

## 关键源码与行为

- `common/item/legacy`：70 个普通物品缺口，包含绑定器、魔力饮料、透镜、饰品、三套护甲、工具、遗物剑、犹大、薪炎武器、坐骑及召唤物品。
  保留旧 CUSTOM_DATA、遗物归属、魔力消耗、有限补给袋 SavedData、饰品盒模拟、冷却和服务端判定；普通材料仍使用其原本的材料行为。
- `common/block/legacy`、`functional/StardustLotusBlockEntity`：缓冲器、量子缓冲器、发电机、液化机、流体桶、精灵罐、欲望之茧、奖杯、星尘莲及浮空/盆栽形式。
  迁移 NeoForge 能量和流体能力；处理容量溢出、双向转换、纸票已支付魔力、目标坐标、孵化进度和存档字段。
- `common/entity/Legacy*`、`gaia/LegacyVoidHerrscher`：恢复全部缺失实体及投射物行为、坐骑控制、旧实体 UUID/NBT、虚空律者完整阶段/技能/支援/奖励流程。
- `gaia/GaiaIII`、`gaia/behavior/EgoWeaponFire`：恢复血量阶段、六波阵列、四个分身、分身阶段回血及分阶段遗物攻击。
  保留原阶段 NBT；修复卸载世界时误清除分身。恢复飞弹的可配置伤害、真实伤害和附加状态。
- `ForgeExtrabotanyCommands`、`GaiaArena`、`GaiaDisarm`：恢复权限等级 2 的 `/exbot itemcheck [true|false]`、配置持久化、背包/饰品白名单与遗物豁免。
- `client/renderer`、`client/model`、Forge 客户端初始化：使用原材质和数值几何映射恢复投射物、摩托/UFO、律者、护甲、饰品及星尘莲的渲染。
  静默贤者仍使用旧版指定的 `shadow_warrior.png`，没有生成替代模型或贴图。

68 个归档的复数标签目录迁移为 1.21 单数目录，并映射六个已经更名的 Botania 魔力池/发射器 ID。
车钥匙的旧末影空气瓶引用迁移为当前 Botania 的 `pure_ender_essence`。
可重复导入工具为 `scripts/restore_legacy_resources.py` 和 `scripts/migrate_imported_tags.py`；已有修复内容优先保留。

## 验证及复现

固定 Gradle Wrapper 9.2.1、ModDev 2.0.141、Oracle Java 21.0.12。
Botania 固定 `456-20260822.093314-4`，Curios `9.5.1+1.21.1`，Patchouli `1.21.1-93`，JEI `19.44.0.405`；
版本和锁文件在 `gradle.properties`、`gradle.lockfile`。

```powershell
$env:JAVA_HOME='<Java 21 JDK>'
.\gradlew.bat runGameTestServer build --offline --console=plain
python scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.5-dev.jar
.\gradlew.bat -p audit-full/harness runServer -Pcandidate=scex5 --offline --console=plain
```

- 19/19 GameTest 通过：既有修复、全部旧实体构造/保存重载、旧物品组件、绑定器、有限补给池、透镜/药水、魔力缓冲和液化守恒、满容量发电保护、星尘莲、孵化、遗物归属/扣费/重复攻击、薪炎过载与终结技、守护者阶段 NBT、六波阵列数量。
- 最后加载日志没有配方/标签错误；资源审计验证 ZIP CRC、Java 21 字节码、JSON、PNG CRC、资源引用和路径，错误数 0。
- 独立物理客户端实际打开隔离世界，绘制 243 个物品、34 个实体渲染器以及三套新增护甲，输出三张截图并逐张检查；静默贤者缺图问题已修复。
- 使用归档 scex.1 真正保存包含 242 种物品和未知组件的世界，再由 scex.5 读取，逐项核对注册名、标记和整数数组；没有接触生产玩家数据。
- 独立 harness 仅为开发验证工具，不包含在运行 JAR 中。最终交付中保存日志、注册表、存档断言及截图；世界副本留在开发目录。

## 明确的验证边界与归档版遗留情况

- 这不是完整整合包、所有多人 Boss 战阶段或所有坐骑按键的人工通关测试。需在冻结包上重新进行森林法杖及整包隔离联调；此前 scex.4 的结果不能直接当作 scex.5 验收。
- 客户端 Botania 自带 README 资源路径告警仍存在；审计工具退出客户端时可出现 ClosedChannelException，不是游戏内资源加载异常。
- 归档版的 `music.ego` 未提供音频且未启用 Ego BGM。保留声音注册项，使用空声音列表消除不存在的文件引用；不伪造音频。
- 归档版 `flamescion_weapon.png` 本身是调色板，物品模型引用这一原始资源。保持该原样视觉，不宣称已获得未提供的原始武器三维模型。
- 原版蝶弹的未完成命中行为按归档契约保留，未擅自加入新的伤害机制。

## 交付与生产路由

最终 manifest 给出运行 JAR、sources JAR、源码 ZIP 的大小和 SHA-256；公开版本由仓库发布脚本生成并附加到对应 GitHub Release。
先将完整冻结包交给森林法杖兼容任务联调，再由 `SCEX-长期维护` 接管生产备份、部署、玩家更新及回滚。
回滚必须配套恢复升级前世界/玩家数据与原 JAR，不能只替换 JAR 后继续使用已改写的世界。
