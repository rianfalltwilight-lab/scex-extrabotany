# ExtraBotany 2.0-scex.7-dev：装备使用修复

2026-09-12，Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21.0.12。
基于已有 scex.6 可维护源码；上游 a4d4f2a968d559752fa3bd6e609544473109d983（MIT）加 SCEX 工作区改动，未覆盖旧交付。
本轮开始的源码输入身份见 audit-items-20260912/baseline.json；冻结源码包含逐文件清单。

## 修复与使用方式

- 亚空之矛：右键释放或按满 200 tick 都发动领域。原来按满时走默认完成入口，完全没有技能；魔力不足时还会进入 1,200 tick 冷却。现在不足会提示所需魔力且允许补充后重试，成功仍花费 10,000 魔力、生成 24 个传送门、冷却 600 tick。服务端检查归属、创造旁观规则中的旁观禁用及冷却，防止完成/释放重复扣费；保留左键 600 魔力攻击与遗物绑定。
- 强力手套：补上原说明承诺的按住左键、攻击冷却恢复后自动攻击。沿用原版攻击入口及服务端判定；GUI、失焦、旁观、正在使用物品、挖方块时不触发。移除手套后停止自动攻击。
- 征服宝石：原来只有 charm 标签，本模组未定义对应槽位。增加 curios:curio 通用装备标签并同步数据生成器，保留 charm 标签。实际 Curios 能力提供原有伤害 +15%、攻速 +10%、移速 +5% 与近期进攻时的力量效果；增加缺失的物品提示。
- 焢煌之境：劫炎永燎：技能逻辑在规定状态下能够使用。操作为潜行右键点燃，再跳起进入薪炎状态；左键发射、右键展开虚空，再按潜行冲刺，R 释放终结技。原来终结技直接读取固定 R 键且没有操作提示；本轮注册可重绑定按键（与原坐骑召唤共用），增加操作/过载恢复提示。未更改技能伤害、能量与过载机制。
- 诸神套装：当前源码与注册中没有 ogchest/oghelm/oglegs/ogboots，按用户说明不恢复，也不声称修好了旧版本的 NBT 故障。

主要实现：LegacySubspaceSpearItem、PowerGloveInputMixin、LegacyAccessories、ForgeItemTagProvider、LegacyKeyMappings、LegacyMountInput、LegacyFlamescionItem、中英文语言文件和通用 Curios 标签。

## 构建与验证

Gradle 9.2.1 / ModDev 2.0.141；Botania 456-20260822.093314-4、Curios 9.5.1+1.21.1、Patchouli 1.21.1-93，依赖锁保留。此前搁置的软依赖/版本范围审计不在本轮范围。

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
.\gradlew.bat build validationJar --offline --console=plain
python scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.7-dev.jar
```

运行使用 Vicerach 冻结 NeoForge 底座及最小依赖，独立测试世界、2 GiB 堆、资源窗口锁和正常退出控制；原始日志与候选哈希见交付 evidence/manifest.json。

- 原实现复现：按满时长不发动、缺魔力后锁定一分钟。首次测试也暴露 Curios 查询缓存和模拟玩家坠落这两个测试夹具问题；分别跨 tick 查询和取消夹具重力后再验，不作为产品缺陷。
- 最终 extra-items-final-gt-20260912：28/28 GameTest 通过，包含原有回归、长按完成/松开发动、补魔力重试、重复扣费防护、真实 Curios 能力与薪炎进入状态。
- 最终 extra-items-final-client-20260912：真实图形客户端与集成服务端 14/14 断言通过，覆盖手套连击/卸下停止、宝石同步/属性/力量、亚空之矛客户端释放与扣费/领域、薪炎点燃/同步/按键包/终结技实体。两轮客户端均正常保存退出。
- 资源审计 errors=[]；运行包所有条目与最终验证包完全一致，唯一差别是验证包额外包含 GameTest/客户端探针。玩家 JAR 排除整个 forge/gametest 包；validation JAR 仅作证据，不能装到正式服。
- 兼容层 1.4.1 的正式 Java 类与 1.4.0 完全一致，仅适配 scex.7 版本元数据；JUnit 2/2 通过，最终两种运行均加载配套 1.4.1。

未做完整整合包、真实公网多人、长期性能或已有受损玩家 NBT 修复验收。客户端截图记录实际画面，技能粒子可遮挡视野；不据此声称完成全部渲染验收。

## 交付

运行 JAR、sources JAR、完整源码 ZIP、兼容层 JAR/源码及 SHA-256 以交付 manifest.json 为准。源码 ZIP 另归档 D:/Minecraft-Projects。
部署由 SCEX-长期维护执行：备份世界/配置及原本体与兼容层，成对换为 scex.7 + 1.4.1；删除重复旧 JAR，完整整包验收后上线。回滚成对恢复旧版本，涉及已写入世界时使用对应备份。本任务未改生产服。
