# ExtraBotany NeoForge 1.21.1 port

This document records the provenance, compatibility decisions, build inputs, and
validation evidence for the SCEX Legacy Genesis port. It is a development handoff,
not an instruction to deploy the artifact to a production pack or server.

## Status and scope

- Port version: `2.0-scex.5-dev`
- Target: Minecraft 1.21.1, NeoForge, and Java 21
- Supported release artifact: NeoForge only
- Validation date: 2026-09-05
- Forest wand integration: compat `1.3.0` passed isolated client/server and save
  acceptance against the frozen scex.5 artifact; it remains a separate SCEX module.
- Current state: public preview; it compiles, generates data, starts a dedicated
  server and client, passes 19 NeoForge GameTests, and has zero missing entries in
  the bounded legacy registry comparison. See `FULL_LEGACY_RESTORE.md`.
- Repository publication is independent from production deployment. A server or
  modpack must still perform its own backup, paired client/server update, health
  checks, and rollback acceptance.

The shared `Xplat` source set and historical `Fabric` directory are retained for
source history, but the Gradle build and the produced JAR target NeoForge only.
Fabric has not been ported or validated and must not be advertised as supported.

## Provenance and license

- Upstream source: Lounode/ExtraBotany, tag `release-1.20.1-1.9.2`
- Upstream commit: `a4d4f2a968d559752fa3bd6e609544473109d983`
- Upstream license: MIT
- The upstream `LICENSE` is retained in the source tree and packaged in the JAR
- Botania comparison input: fixed 1.21.1 source and binary snapshots matching the
  versions listed below; those external reference trees are not included here.
- Initial audit evidence was retained outside the public source tree; public
  conclusions and limits are summarized in `AUDIT_REVIEW.md`.

Decompiled binaries were used only as behavioral evidence. No decompiled source,
model, texture, or animation was copied into the maintained source tree without an
independent compatible license. The one edited bitmap, `void_archives.png`, comes
from the MIT upstream tree; its ninth frame was a byte-for-pixel duplicate of its
first frame and was removed to match the intended eight-frame loop.

## Locked build inputs

| Component | Locked version |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.248` |
| Java | `21` |
| Gradle wrapper | `9.2.1` |
| Botania | `456-20260822.093314-4` (`456-SNAPSHOT` at runtime) |
| Patchouli | `1.21.1-93` |
| Curios | `9.5.1+1.21.1` |
| JEI | `19.44.0.405` |
| EMI compile API | `1.1.22+1.21.1` / Modrinth `ouSj7NfF` |
| KubeJS compile API | `2101.7.2-build.374` |

The wrapper download is pinned by SHA-256 in
`gradle/wrapper/gradle-wrapper.properties`. Artifact JAR tasks disable file
timestamps and use reproducible entry ordering.

The independent review added `gradle.lockfile` for compile/runtime/test
classpaths, including transitive dependencies. Botania is deliberately excluded
from Gradle's generated lock because its POM reports `456-SNAPSHOT`, which
conflicts with the required timestamp coordinate; its exact
`456-20260822.093314-4` declaration remains unchanged. This is not a claim that
every plugin/tool configuration or downloaded artifact is checksum-locked.

The wrapper scripts and JAR now come from Gradle 9.2.1 itself. Wrapper JAR SHA-256:
`423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`, checked against
https://gradle.org/release-checksums/. The existing 120-second network timeout is retained.

## Porting work

### Production candy compatibility (scex.4)

The real deployed scex.1 binary contains `candy_bag`, `candy_eins`, `candy_zwei`,
and `candy_drei`, absent from the selected MIT 1.20.1 source baseline. Restore all
four exact registry IDs with functional bag/food behavior. Existing ID/count and
custom components are retained without rewriting worlds. The bag opens three
equal-weight candy rolls; foods retain 2 nutrition, 0.15 saturation modifier,
14-tick use, 4 health healing, and 200-tick amplifier-1 speed/jump/haste effects.
December 16 through January 2 retains deployed seasonal naming and model changes.

Behavior was checked against the exact user-provided scex.1 JAR bytecode. Java
implementations were written in this maintained tree, not copied from decompiler
output. Exact candy models, textures, recipes, advancements and loot data were
recovered from that user-provided JAR, which declares MIT in neoforge.mods.toml;
the source JAR hash and every copied file hash are in
`compatibility/candy-resource-provenance.json`. Upstream MIT attribution remains.

The legacy malformed LootTable override fallback is also restored. Two additional
GameTests verify old count=4 stacks, NBT serialization/reload, custom component
preservation, using a reloaded bag, correct drops, food/effects and calendar edges.
The follow-up cold-backup audit confirmed element_rune, sin_rune, and
music_disc_herrscher_of_the_void in real ItemStack structures. These exact IDs,
original rune recipes, actual jukebox song/sound, music-disc tag, and Gaia III
disc pool are now restored. The existing salvation.ogg matches the deployed
audio byte-for-byte. Additional GameTests cover stored rune counts, real recipe
matching/assembly, the count=2 legacy disc, playback, save/reload and removal.
Final GameTests: 9/9. The old-world upgrade gate remains blocked by other IDs;
startup and these bounded tests are not a no-loss migration proof for the pack.

### NeoForge and Minecraft API migration

- Rebuilt the project as a single NeoForge 1.21.1 Gradle target and replaced the
  legacy Forge metadata with `META-INF/neoforge.mods.toml`.
- Migrated registries, event listeners, capabilities, networking payloads, data
  components, attributes, effects, enchantment interactions, food/item behavior,
  loot APIs, block entities, entity data, rendering hooks, and client setup to the
  1.21.1 APIs.
- Migrated recipes and serializers to `RecipeOutput`, `RecipeHolder`, current
  codecs, and the Botania 1.21.1 recipe contracts.
- Migrated advancements, loot tables, tags, models, blockstates, sounds,
  Patchouli data, and all other providers to the current data-generator APIs.
- Updated Botania mixin targets to their current descriptors and separated
  client-only mixins from common/server mixins.
- Registered starry armor render extensions through NeoForge's
  `RegisterClientExtensionsEvent` instead of the obsolete item mixin.
- Updated JEI subtype and crafting-category integration to current typed APIs.
  Optional EMI and KubeJS code compiles against locked APIs but has not received
  a dedicated runtime integration test.

### Botania compatibility and enchanted soil

The locked Botania 456 build no longer contains the older Overgrowth Seed and
Enchanted Soil implementation expected by ExtraBotany 1.20.1. This port therefore
owns the compatibility object `extrabotany:enchanted_soil` rather than guessing a
removed Botania registry name.

The replacement includes:

- block and item registration with generated blockstate, item model, loot, tags,
  translations, and moss-compatible textures;
- the Enchanter transformation and wand HUD interaction;
- reward-bag integration;
- persistence of the affected flower's custom state;
- passive-flower decay pause on enchanted soil;
- double-tick behavior for supported ExtraBotany and Botania special flowers;
- a dedicated compatibility path preserving Hydroangeas behavior.

This is an intentional compatibility divergence from the upstream 1.20.1 registry
layout. Existing worlds must be tested with a copy before relying on automatic
upgrade behavior.

### Data and resources

- The canonical generated-resource root is `src/generated/resources`.
- Historical `Xplat/src/generated/resources` and
  `Forge/src/generated/resources` trees are deliberately excluded from the build
  so stale outputs cannot shadow canonical data.
- Generated damage types: `backfire`, `excalibur`, `jingwei`, `link`, and
  `reverse_heal`.
- Common material and boss tags use the current `c:` namespace and current plural
  paths, including `c:cobblestones`, `c:ores/<material>`, and `c:bosses`.
- The packaged resource tree contains no legacy plural tag directories, `forge:`
  resource references, `data/forge` payload, duplicate resource paths, or missing
  direct ExtraBotany texture/sound references in the performed audit.

## Historical validation claims for scex.2 (superseded by independent review)

The following table preserves the earlier handoff claims, not new review results.
In particular, its language-key alignment claim was disproved by the independent
audit. Consult `AUDIT_REVIEW.md` for current evidence and corrected limits.

| Gate | Result |
| --- | --- |
| Java compilation | Passed with zero compiler warnings after API cleanup |
| `runData` | Passed; all providers completed |
| JSON parse audit | 1,059 JSON files parsed successfully |
| Resource-path audit | 1,303 entries; zero duplicate relative paths, legacy plural paths, `data/forge` files, or `forge:` references |
| Model/texture audit | 383 models and blockstates; zero missing direct ExtraBotany model/texture references |
| Audio/image signature audit | 193 valid PNG signatures and 11 valid OGG signatures; five direct local sound references present and resolved |
| NeoForge GameTests | 2/2 passed in 612.4 ms |
| Dedicated server | Reached `Done` on isolated development port 25576; loaded 2,928 recipes and 2,501 advancements |
| Physical client | Applied mixins and completed resource, atlas, audio, Patchouli, and JEI reloads; remained stable at the title screen |
| Final clean build | Passed twice from `clean`; 585 classes and 1,059 JSON resources packaged |
| JAR structure validation | Passed with the Java 21 `jar --validate` tool; all required metadata/resources present and no forbidden or stale entries |
| Reproducible artifact comparison | Passed; runtime and sources JAR sizes and SHA-256 values matched byte-for-byte across two clean builds |

The included GameTests cover registry/damage-type/sound-holder resolution and the
enchanted-soil double-tick behavior. They run from
`Forge/src/main/java/io/github/lounode/extrabotany/forge/gametest` using the
`extrabotany:empty` structure.

The ordinary dedicated-server validation used port 25576 because port 25565 was
already occupied. The unrelated process on 25565 was not changed. The client test
was intentionally stopped at the title screen after full reload; it is startup and
resource evidence, not an exhaustive visual or gameplay sign-off.

Development logs still contain non-fatal warnings originating in dependencies or
the development environment: absent development refmaps also seen for other mods,
a Botania README resource-path warning, vanilla goat-horn sound warnings, a
Botania/renderer shader sampler warning, and Patchouli/JEI keybinding timing. No
ExtraBotany exception remained in the final client startup pass.

## Validation still required before production release

- Upgrade a disposable copy of a real 1.20.1 world and verify registry/data fixes,
  block entities, inventories, relic bindings, and enchanted-soil state.
- Exercise a real client/server connection and all custom payload round trips.
- Complete a Gaia III fight, including arena checks, boss events, loot, death,
  reconnect, and dimension-change paths.
- Inspect every armor set and custom renderer in-world on representative GPUs.
- Exercise JEI recipes and optional EMI/KubeJS integration at runtime.
- Run broader gameplay and balance regression coverage for flowers, relics,
  brews, entities, recipes, and reward bags.

These are explicit release-risk boundaries. Successful startup and GameTests do
not prove those scenarios.

## Reproduction commands

Run from the repository root with Java 21:

```powershell
$env:JAVA_HOME = '<Java 21 JDK>'
.\gradlew.bat runData --console=plain
.\gradlew.bat compileJava --console=plain
.\gradlew.bat runGameTestServer --console=plain
.\gradlew.bat runServer --console=plain
.\gradlew.bat runClient --console=plain
.\gradlew.bat clean build --console=plain
py scripts/audit_port.py build/libs/extrabotany-neoforge-1.21.1-2.0-scex.3-dev.jar
```

The review machine's default `JAVA_HOME` pointed to Java 17, even though `java`
on PATH was Java 21. Set JAVA_HOME explicitly to a local Java 21 JDK.

## Independent review fixes (scex.3)

- Fixed Hydroangeas destruction at its decay boundary on enchanted soil: protect
  the in-tick decay counter before Botania's destruction check, then restore the
  stored age. A regression test failed before this fix and passed afterward.
- Fixed Trade Orchid's discount Mixin to unwrap `MobEffectInstance.getEffect()`
  with `.value()` before testing/casting the effect. An actual villager trading
  GameTest failed before this fix and passed afterward.
- Moved the Patchouli template-reload Mixin into the client-only list because its
  callback uses client template classes, despite targeting a common-package class.
- Added the two missing English legacy advancement keys; both languages now have
  707 matching keys.
- Expanded GameTests from two to five, adding decay-boundary, item-data save/copy,
  and villager-trading regression coverage, and checking all five damage types.
- Added `scripts/audit_port.py` for repeatable packaged resource/class validation.
- Added transitive classpath locks and normalized the wrapper as described above.

Production acceptance is still incomplete. These fixes and tests do not establish
world-upgrade correctness, real network round trips, complete boss fights, optional
integration runtime compatibility, or in-world visual correctness.

`runServer` should use an unoccupied development port. Do not point any run task at
a production pack. Production inputs used during the historical audit were opened
only for read-only comparison.

## Historical scex.2 artifact handoff

The values below identify the original reviewed input only. Use the scex.3
artifact table in `AUDIT_REVIEW.md` for the corrected development candidate.

The following JAR values matched across both clean builds:

- Runtime JAR: `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.2-dev.jar`
  - Size: 3,848,432 bytes
  - SHA-256: `5806a9cf9d55faff07a281f8d84fb5401181e495687cb634a192f4736869f1c6`
- Sources JAR:
  `build/libs/extrabotany-neoforge-1.21.1-2.0-scex.2-dev-sources.jar`
  - Size: 3,325,255 bytes
  - SHA-256: `e2ca0d17d9cee6367ee72a21211e0d564a9a4eac362bbae1ffd2160acab04cac`
- Working-tree source archive: versioned scex.2 development archive retained as
  historical evidence outside this repository.

The source archive is made from the final working tree rather than `git archive`,
so it includes uncommitted port changes. It excludes `.git`, `.gradle`, `build`,
`run`, `.cache`, and both obsolete generated-resource roots. Its checksum is
reported alongside the delivered files because an archive cannot contain its own
final checksum without changing that checksum.

Before any production handoff, the `SCEX-长期维护` workflow must independently
back up the pack and worlds, verify dependency versions, stage the JAR in a test
copy, perform acceptance tests, record rollback instructions, and only then deploy.
This development task does not modify the production installation.
