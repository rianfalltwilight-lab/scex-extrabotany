![](docs/img/logo.png)
# 额外植物学：重燃

## [English](README_en.md)

## 关于本分支

本分支由 Magellan67 接手维护，承接自 Lounode 的《额外植物学：重燃》，目标版本为 Minecraft 1.21.1（NeoForge）。

谨以此项目向似水流年致敬，感谢他做出了这么好的模组。

移植以 ExtraMeteorP 的原版《额外植物学》为准：原版有的内容和数值照原版恢复，重燃新增的内容保留。

- [docs/OVERVIEW.md](docs/OVERVIEW.md)：这一版做了什么、和原版的差异、哪些地方还没有验证。
- [docs/PORT_AUDIT.md](docs/PORT_AUDIT.md)：逐项的对照和修复记录。
- [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md)：需要在游戏里确认的项目和需要决定的事项。

![](docs/img/overview.png)
![](docs/img/overview2.png)

## 运行环境

| 组件 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 或更高的 21.1.x |
| 植物魔法（Botania） | 456 |
| Patchouli | 1.21.1-93 或更高 |
| Curios | 9.5.1 或更高 |
| JEI、EMI、KubeJS | 可选 |

本分支只支持 NeoForge。重燃的 Forge 1.20.1 和 Fabric 版本不在这个仓库里。

旧存档保持兼容：物品、方块、实体的注册名和存档里的数据键保持不变。

## 内容

- 遗物：王之花园、焢煌之境：劫炎永燎、亚空之矛、犹大的誓约、静籁永恒、全能指环、王者圣剑、虚空万藏、空之律者核心等。
- 饰品：征服宝石（佩戴者成为雷之律者）、大师魔力之戒、四种符石和四元归一石、机车和飞碟两种坐骑等。
- 盔甲：哥布林猎人、暗影武士、星空歌姬、昴宿星团战斗女仆、银翼护卫、静默贤者。
- 花：商友兰、伐木花、雷卡兰、石中姬、雪绒花、全知瑾、星尘莲、魔链星等。
- 方块：魔力缓存器、量子魔力缓存器、FE转换器、液态魔力转化器、活石桶、不随想之茧、祭坛、魔力充能基座。
- Boss：盖亚守护者III、空之律者、本我。
- 重燃重绘的材质，更接近原版游戏的风格。

## 整合包作者

配方、数据映射和 KubeJS 的用法见 [docs/kubejs.md](docs/kubejs.md)。

## 从源码构建

需要 JDK 21。其他依赖由 Gradle 下载。

```bash
./gradlew build
```

成品在 `build/libs/extrabotany-neoforge-1.21.1-<版本>.jar`。

| 命令 | 作用 |
|---|---|
| `./gradlew runClient` | 启动开发用的客户端。 |
| `./gradlew runServer` | 启动开发用的专用服务器。 |
| `./gradlew runGameTestServer` | 运行全部游戏内测试，然后退出。 |
| `./gradlew runGameTestServer -Pgametest_namespaces=extrabotany_perf` | 运行性能场景，不运行测试。结果在日志里，以 `EB_PERF` 开头。 |
| `./gradlew runData` | 运行数据生成器，输出到 `src/generated/resources`。 |

### 目录

| 目录 | 内容 |
|---|---|
| `src/main` | 模组本身。发布的 JAR 只包含这里的内容。 |
| `src/generated/resources` | 数据生成器的输出。不要手工修改，`runData` 会重新生成整个目录。 |
| `src/datagen` | 数据生成器的代码，不进入发布的 JAR。 |
| `src/gametest` | 游戏内测试和它们的结构文件，不进入发布的 JAR。 |
| `docs` | 项目说明、移植审计记录、验收清单、注释规范、整合包作者指南。 |

代码注释是中英对照的，写法见 [docs/COMMENT_STYLE.md](docs/COMMENT_STYLE.md)。

## 致谢

- ExtraMeteorP：《额外植物学》原作者。
- Lounode：《额外植物学：重燃》作者。
- Urranium（材质）、Keletu（盔甲模型）。
- Vazkii 和植物魔法的全体贡献者。

## 许可

MIT，见 [LICENSE](LICENSE)。
