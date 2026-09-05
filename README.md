# SCEX ExtraBotany 1.21.1

> 这是 Space Creator EX（SCEX）维护的 ExtraBotany 非官方 Minecraft 1.21.1 / NeoForge 移植版。1.21.1 移植、缺陷修复、测试与公开发布整理主要由 OpenAI Codex 在维护者监督下辅助完成；详见 [AI 参与开发声明](AI-GENERATED.md)。

[English](README_en.md)

当前公开预览版本为 **2.0-scex.5-dev**。项目基于 [Lounode/ExtraBotany](https://github.com/Lounode/ExtraBotany) 的 `release-1.20.1-1.9.2` / `a4d4f2a968d559752fa3bd6e609544473109d983`，保留上游作者、MIT 许可证与资源署名。本仓库不是 Lounode、Botania 或 NeoForge 的官方发行版。

## 环境与安装

| 组件 | 已验证版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 |
| Java | 21 |
| Botania | 456-20260822.093314-4（运行时显示 456-SNAPSHOT） |
| Patchouli | 1.21.1-93 |
| Curios | 9.5.1+1.21.1 |

从 [Releases](https://github.com/rianfalltwilight-lab/scex-extrabotany/releases) 下载运行 JAR，客户端与服务端使用同一文件。不要与其他使用 `extrabotany` 模组 ID 的 JAR 同时安装。JEI 与 KubeJS 为可选集成。

SCEX 整包中的森林法杖绑定能力另由 `SCEX-Botania-ExtraBotany-Compat 1.3.0` 提供；它不包含在本仓库发行包内，使用时必须选择与 `2.0-scex.5-dev` 精确匹配的版本。

## 本次恢复范围

- 将目标平台迁移至 Minecraft 1.21.1、NeoForge 21.1.248 与 Java 21。
- 恢复旧版运行包的物品、方块、方块实体、实体、流体、护甲、遗物、花、配方、标签、渲染与存档契约；与旧 `scex.1` 的已盘点注册项相比缺失为零。
- 修复 Gaia III 声音字段、附魔土寿命边界、交易兰效果读取、旧物品注册、旧 ItemStack 数据保留及多项资源兼容问题。
- 保留精确依赖版本、Gradle 锁文件、可复现 JAR 设置、GameTest、资源审计和旧存档夹具。

详细实现与证据边界见 [完整恢复报告](FULL_LEGACY_RESTORE.md)、[移植记录](PORTING.md) 和 [独立复核](AUDIT_REVIEW.md)。

## 验证摘要

- 19 / 19 NeoForge GameTest 通过。
- 与旧 `scex.1` 运行包对照的注册项缺失为零。
- 242 种旧世界物品完整编码、自定义数据与重载结果一致。
- 物理客户端检查了 243 个物品、34 个实体渲染器和 3 套新增护甲。
- 资源/JAR 审计错误为零；两次构建的运行 JAR与 sources JAR 均逐字节一致。

这些数字是限定验收范围，不是玩法完成率。尚未穷尽完整多人 Boss 战、所有坐骑按键、所有可选模组组合、长期负载或全部 GPU 的视觉表现。归档版 `music.ego` 没有音频，`flamescion_weapon` 仅有原始调色板纹理，蝶弹保留原版未完成命中行为。

## 构建与发布

```powershell
$env:JAVA_HOME = '<Java 21 JDK>'
.\gradlew.bat --no-daemon build
.\gradlew.bat --no-daemon runGameTestServer
.\scripts\prepare-release.ps1
```

Linux/macOS 使用 `./gradlew`；发布打包脚本需要 PowerShell 7。完整、可重复的发布门禁与 GitHub Release 流程见 [RELEASING.md](docs/RELEASING.md)。

## 许可证与来源

源代码沿用上游 [MIT License](LICENSE)。SCEX 修改不改变上游代码、名称和素材的权利归属；固定来源、资源恢复方式及 AI 参与范围见 [NOTICE](NOTICE) 与 [AI-GENERATED.md](AI-GENERATED.md)。这些记录是来源说明，不构成法律意见。
