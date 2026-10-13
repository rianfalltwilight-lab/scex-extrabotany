# ExtraBotany 2.0-scex.6-dev：盖亚三装备缴械与活石桶配方

针对 2026-09-05 玩家群反馈，经用户确认调整盖亚三规则。来源是当前可维护 scex.5 源码，继承 FULL_LEGACY_RESTORE.md 中上游版本、MIT 许可证与资源来源说明。

## 实际行为

- 补充活石桶 extrabotany:elfjar 合成：7 块 botania:livingrock 按 U 形（R R / R R / RRR）合成 1 个；获取活石解锁配方书。归档 scex.1 也无此配方，因此这是新增可合成路径，不是声称恢复原配方。正式数据生成器同步维护配方与解锁条件。

- 盖亚三不再用背包或 Curios 饰品白名单拒绝召唤；难度、真实玩家、祭坛、场地及已有 Boss 检查继续生效。
- 战斗核心 AI 丢出违规主手、副手、四件盔甲及 Curios 实际装备槽物品。未选中的快捷栏、普通背包和饰品外观槽不清空；切换到违规物品所在的快捷栏后会缴械。
- 默认允许 minecraft、botania、extrabotany、mythicbotany。保留 gaiaSpawnUnCheckList 配置键作为额外允许列表；不再把原来的特定第三方戒指/刻印例外自动应用到盖亚三缴械。
- Curios 强制战斗卸下不经过手动 canUnequip 限制；使用 Curios 堆栈处理器变更装备，由其正常流程处理卸下与同步。
- 掉落实体保存完整 ItemStack 组件和数量，90 tick（正常 TPS 下 4.5 秒）后可拾取。只有实体成功加入世界后才清空原槽位；实体生成被取消时保留原物品。
- 保留创造/旁观豁免、虚空万藏物品豁免、guardianItemCheck 及 disableGaiaDisarm 开关。空之律者的召唤规则不在本次修改范围。

## 实现与验证

修改 GaiaIII.java、GaiaDisarm.java、ForgeExtrabotanyConfig.java、中英文命令提示和版本号；新增 GaiaDisarmGameTests.java。修正原有 FullLegacyGameTests 雾波计数测试的区块加载前置条件，避免随机测试原点导致统计不足。

构建环境：Minecraft 1.21.1、NeoForge 21.1.248、Java 21.0.12、Gradle 9.2.1、ModDev 2.0.141；原依赖版本和 gradle.lockfile 保留。

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12'
.\gradlew.bat runGameTestServer build --offline --console=plain
python scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.6-dev.jar
```

最终 GameTest 24/24 通过，资源校验 errors=[]。新增测试覆盖违规主副手/盔甲掉落、背包和未选中快捷栏保留、选中后掉落、允许盔甲保留、完整数据/数量/拾取延迟、重复执行不复制、Curios 装备卸下、创造豁免、取消掉落保留原物品、关闭配置不缴械，以及真实祭坛入口携带违规背包/手持/饰品成功召唤并执行战斗行为。活石桶测试通过实际配方管理器合成并验证产量、拒绝圆石替代和配方书解锁资源。测试阶段修正了模拟玩家网络握手、祭坛尺寸和区块加载设置，最终日志见 evidence。

## 配套兼容层与交付

森林法杖兼容层 1.3.0 精确依赖 scex.5，不能搭配本包。基于归档兼容层源码构建 1.4.0，精确依赖 scex.6；正式 Java 实现保持一致，仅版本元数据更新。独立开发副本位于相邻 scex-botania-extrabotany-compat-scex6，未覆盖 D 盘原 1.3.0 源码。其既有 JUnit 2/2 通过；在 Windows 含中文路径下使用 ASCII buildOutput 临时目录解决测试类加载问题。

```powershell
.\gradlew.bat test build -PbuildOutput=C:/Users/Monarch/AppData/Local/Temp/scex-compat6-build --offline --console=plain
.\gradlew.bat runClient -PbuildOutput=C:/Users/Monarch/AppData/Local/Temp/scex-compat6-build --offline --console=plain
```

产物版本、字节数、SHA-256、客户端结果、源码 ZIP 和证据以本次交付目录 manifest.json 为准。

## 验证边界与生产注意

本次针对本体回归和隔离客户端配套绑定验证；不将 scex.5 的 236-JAR 整包验收当成本次 scex.6 的整包结果。Curios 使用真实 API 和第三方测试物品验证，但未逐个测试所有第三方戒指自己的诅咒副作用；强制卸下是本次明确选择的战斗规则。原素材缺失及旧 Manalink 空字段日志仍以旧报告/新客户端日志为准。

未修改生产目录，也未修改 scex.5 冻结目录。生产交由长期维护：先备份世界、配置及两只旧 JAR，再成对替换为本体 scex.6 和兼容层 1.4.0；移除重复旧版本，校验哈希。上线前复验实际七宗罪戒指场景与整包，回滚时同时恢复本体及兼容层；涉及已保存世界时按备份恢复。
