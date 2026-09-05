# SCEX ExtraBotany 1.21.1

> This is an unofficial Minecraft 1.21.1 / NeoForge port of ExtraBotany maintained by Space Creator EX (SCEX). The 1.21.1 port, fixes, tests, and public-release preparation were primarily assisted by OpenAI Codex under maintainer supervision. See [AI development disclosure](AI-GENERATED.md).

[中文](README.md)

The current public preview is **2.0-scex.5-dev**. It is based on [Lounode/ExtraBotany](https://github.com/Lounode/ExtraBotany) at `release-1.20.1-1.9.2` / `a4d4f2a968d559752fa3bd6e609544473109d983`. Upstream authorship, the MIT license, and resource credits are retained. This repository is not an official release from Lounode, Botania, or NeoForge.

## Runtime baseline

| Component | Validated version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 |
| Java | 21 |
| Botania | 456-20260822.093314-4 (reports 456-SNAPSHOT at runtime) |
| Patchouli | 1.21.1-93 |
| Curios | 9.5.1+1.21.1 |

Download the runtime JAR from [Releases](https://github.com/rianfalltwilight-lab/scex-extrabotany/releases) and use the same file on both client and server. Do not install another JAR with the `extrabotany` mod ID. JEI and KubeJS integrations are optional.

SCEX's forest-wand binding behavior is supplied separately by `SCEX-Botania-ExtraBotany-Compat 1.3.0`; it is not bundled here and must be paired with exactly `2.0-scex.5-dev`.

## Restored scope

- Ported the target to Minecraft 1.21.1, NeoForge 21.1.248, and Java 21.
- Restored the inventoried legacy items, blocks, block entities, entities, fluids, armor, relics, flowers, recipes, tags, rendering, and save contracts. The runtime registry comparison against `scex.1` has zero missing legacy entries.
- Fixed the Gaia III sound lookup, enchanted-soil lifetime boundary, Trade Orchid effect lookup, legacy registrations, old ItemStack preservation, and multiple resource-compatibility defects.
- Retained locked dependencies, Gradle locks, reproducible JAR settings, GameTests, resource audits, and old-world fixtures.

See [full restoration report](FULL_LEGACY_RESTORE.md), [porting record](PORTING.md), and [independent review](AUDIT_REVIEW.md) for implementation details and evidence boundaries.

## Validation summary

- 19 / 19 NeoForge GameTests passed.
- Zero missing inventoried registry entries compared with the archived `scex.1` runtime.
- Full serialized stacks and custom data for 242 old-world items survived reload unchanged.
- A physical client checked 243 items, 34 entity renderers, and three restored armor sets.
- The resource/JAR audit reported zero errors; two builds produced byte-identical runtime and sources JARs.

These are bounded checks, not a gameplay-completion percentage. Full multiplayer boss fights, every mount control, every optional-mod combination, long-running load, and all GPU-specific rendering remain outside the proven scope. The archived `music.ego` has no audio, `flamescion_weapon` only has its original palette texture, and the butterfly projectile retains its unfinished upstream hit behavior.

## Build and release

```powershell
$env:JAVA_HOME = '<Java 21 JDK>'
.\gradlew.bat --no-daemon build
.\gradlew.bat --no-daemon runGameTestServer
.\scripts\prepare-release.ps1
```

Use `./gradlew` on Linux/macOS. The release-packaging script requires PowerShell 7. See [RELEASING.md](docs/RELEASING.md) for the reproducible gate and GitHub Release workflow.

## License and provenance

The source remains under the upstream [MIT License](LICENSE). SCEX changes do not replace upstream ownership of code, names, or assets. Fixed provenance, resource-restoration methods, and AI involvement are documented in [NOTICE](NOTICE) and [AI-GENERATED.md](AI-GENERATED.md). These records are provenance notes, not legal advice.
