# Extra Botany 1.21.1: project overview and status

[中文](OVERVIEW.md)

This is an **unofficial** NeoForge 1.21.1 port of "Extra Botany". This document tells what the branch is, what version 2.0.0 did, how it is different from the original, and what is not verified yet.

- The record of each change: [PORT_AUDIT.md](PORT_AUDIT.md)
- The in-game acceptance checklist: [ACCEPTANCE.md](ACCEPTANCE.md)

**Status**: the code, the data and the automatic tests are complete. The in-game acceptance (visuals, feel, multiplayer, old worlds) is not complete.

## What it is

"Extra Botany" is an add-on for Botania: new flowers, relics, baubles, armor sets and bosses.

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 or a later 21.1.x |
| Java | 21 |
| Botania | 456 |
| Patchouli | 1.21.1-93 or later |
| Curios | 9.5.1 or later |
| JEI, EMI, KubeJS | optional |

## Lineage

| Version | Repository | Role in this branch |
|---|---|---|
| Original 1.12.2 | `ExtraMeteorP/Extra-Botany`, `master` (`4ad2cc9`) | The reference for behavior and values (content that only 1.12 has) |
| Original 1.16.5 | `ExtraMeteorP/Extra-Botany`, branch `1.16` (`f96f6d3`) | The reference for behavior and values (content of 1.16, and content of the two branches) |
| "ExtraBotany: Reburn" 1.20.1 | `Lounode/ExtraBotany`, `release-1.20.1-1.9.2` (`a4d4f2a`) | The base of the project; the content that Reburn added stays |
| SCEX previews | `2.0-scex.1` to `2.0-scex.10-dev` | The initial 1.21.1 port, the start point of this version |
| This version | `2.0.0` | The first release version number |

The rule of this version: **the source code of the original is the reference**.

- Content that the original has was written again, class by class, from the source of 1.12 and 1.16.
- Content that Reburn added and the original does not have (for example the Resoncund, the Trade Orchid, the Woodienia, the Enchanter) keeps its design. Only defects were corrected.
- The registry IDs and the keys of the saved data did not change, thus old worlds stay usable.

## What this version did

### Content and behavior restored from the original

In the start version, a large part of the original content only had an item and a texture with no function, or its behavior and values were different from the original. This version wrote it again from the source:

- **Damage system**: the piercing damage and the direct health decrease of the original had no implementation. They are restored.
- **Herrscher of Thunder** (Gem of Conquest): the gem had no effect. Now it has the energy, the counterattack, the Thunder Slash, the energy bar and the armor on the player.
- **Shooting Guardian set**: the four set effects and the attributes of each piece were not implemented.
- **Mounts** (Motor, UFO): written again with the movement rules of the original, with the Key of Truth, the energy bar and the sounds.
- **Relics**: Key of Flamescion, Spear of Subspace, Judah Oath (with the Divine Justice effect), Garden of King's Law, Silent Eternity, Supply Bag Limited Edition, the five relic swords.
- **Machines and blocks**: Mana Buffer, Quantum Mana Buffer, Flux Manafield, Mana Liquefaction Device, Livingrock Barrel, Cocoon of Desire, Stardust Lotus.
- **Herrscher of the Void** (the boss of 1.12): written again with the pace and the rules of the original, with the shields, the special attacks, the lightning chain of the lances and the supporters.
- **Ego** (the boss of 1.16): a separate boss with the name and the skin of the summoner. The stages, the attack intervals, the eight landmine formations, the minions and the attributes were compared item by item with the original.
- **Universal runes**: the Rune of Element and the Rune of Sin can replace the runes of Botania again.
- The **Trophy** and the advancement "Minefield Active": the data was always there, but no code granted them.

### The content of the two original branches together

The content of the original 1.12 and of the original 1.16 is not the same. This branch has both:

- Only in 1.12: Herrscher of the Void, Stardust Lotus, Garden of King's Law, Spear of Subspace, Judah Oath, Cocoon of Desire, Quantum Mana Buffer, Mana Liquefaction Device, Supply Bag Limited Edition, Trophy.
- Only in 1.16: Ego, Motor and UFO, Key of Flamescion, Gem of Conquest, universal runes, Universal Petal.
- Guardian of Gaia III (the Reburn port of the boss of 1.12) and the Ego (1.16) are two different bosses with separate summon methods.

### Defects that were corrected

The audit record has approximately 190 numbered defects. Each one gives the symptom, what the original does, and the correction. Apart from the "different from the original" items, there are:

- **Function defects**. Examples: the custom data of all flowers was not synchronized to the clients; the suspicious stew effect of five flowers only worked after the world was entered again; the block effects of the shields also started when the damage was not blocked; the "release" of the Achilles Shield could never be used after 1.20.5; one use of the Pure Daisy Pendant converted two times; the buffs of four materials were applied and used mana on 59 of each 60 ticks because of an inverted condition.
- **Waste on each tick**. Examples: the climbing accessories searched the accessory slots of each mob in the world on each tick; each pedestal tried some hundreds of origins for each 5 seconds to examine its multiblock structure; five flowers sent a full block entity packet to the clients on each second; the mana bar searched all mana items of the player on each frame.
- **Telemetry**. The inherited code had telemetry that was on by default (bStats and a custom statistics server). It is removed completely: **this mod sends no data to a server**.

### Project structure and data

- One NeoForge module with the standard Gradle layout.
- Client code and common code are separate: classes outside the client packages do not refer to client classes. A script examined the release JAR for this.
- The data generators run again (before, they stopped with an error at the third generator), and two runs give exactly the same output. Generated files and hand-written files are in different directories.
- The mechanisms of 1.21.1 are used: data attachments (the energy and the state of the player), data maps (the hatch table of the cocoon, the burn times of the fuels), tags, data-driven damage types and jukebox songs. A modpack can adjust them with a data pack.
- The KubeJS recipe support was verified at run time. See [kubejs.md](kubejs.md).
- The code comments are in Chinese and English. See [COMMENT_STYLE.md](COMMENT_STYLE.md).

### Tests

The number of automatic game tests (GameTest) went from 88 to 211, in 40 test classes. All are successful.

### Performance

- Each of 30 flower and machine types has a measurement scenario: 64 blocks of one type, and the time of each server tick is measured. The most expensive type (the Blood Enchantress) costs 1 to 1.4 microseconds for each block on each tick, which is less than 3% of a tick for 1000 blocks.
- The only clear hot spot was the Woodienia: with 64 flowers, the cost of each flower on each tick went from 5.3 to 0.7 microseconds, and the extra time of the slowest 5% of ticks went from 7.2 to 0.15 milliseconds.
- Command: `gradlew runGameTestServer -Pgametest_namespaces=extrabotany_perf`.
- Not measured: the render performance of the client, and the cost of many projectiles at the same time in a boss fight.

### Lexicon and advancements

- 14 lexicon entries were restored. The text comes from the language files of the original 1.12.
- 13 entries were written new for content that had none (the Ego included). The text tells the function of today.
- 6 advancements of the original 1.12 were restored, and the advancement "defeat the Ego" was added.

## Differences from the original

The original is the reference, but this version is not a literal copy. The differences are in the groups below. The details of each item are under "Differences from the original" in the sections of [PORT_AUDIT.md](PORT_AUDIT.md).

### Differences that come from the platform

- The target is 1.21.1 / NeoForge / Botania 456. Item data uses data components, player state uses data attachments, and accessories use Curios.
- The original shows the values of the machines with WAILA and The One Probe. This port uses the display of the Wand of the Forest.
- The original puts the Rune of Element and the Rune of Sin into the rune tags of Botania. The recipes of Botania 1.21.1 no longer use these tags, thus this port gets the same effect with a mixin. The cost: JEI, EMI and the recipe book do not show the universal runes as possible ingredients.

### How the content is combined

- The content of 1.12, of 1.16 and of Reburn is there at the same time.
- The Nature Orb (200,000 Nature Aura) summons the Ego, and the Challenge Ticket summons Guardian of Gaia III. In the original 1.12 the two items summon Guardian of Gaia III, and in the original 1.16 the two items summon the Ego.
- The "advancement requirement" of the original 1.16 (nine relics need the defeat of the Ego first) is a configuration option that is **off by default**. The cause: Reburn changed how some of these relics are obtained, and a player can craft them before the boss is defeated.
- Reburn gave some items new names and registry names. For example, `buddhistrelics` is now the Void Archives (`void_archives`), and `treasurebox` is now the Pandora's Box (`pandoras_box`).

### Defects of the original that were not copied

The rule of the audit: a clear defect of the original is not ported, and the record tells this. Examples:

- The Strengthened Slash (Key of Flamescion) never does damage in the original, because the constructor does not keep the owner.
- The Key of Truth and the spears of the Judah Oath keep the owner in a static field, thus all instances share the owner of the newest one.
- The Oath of Sakura changes the global magic and lava damage objects so that they ignore armor permanently.
- The set check of the Silent Sages refers to the four Shadow Warrior items, thus the set can never be complete.
- "No mana cost with the full Shooting Guardian set" for the Silver Bullet is only in the lexicon, and the code does not implement it. This port does what the lexicon says.
- The warning messages for the special attacks of the Herrscher of the Void are never sent because of an incorrect condition. After rank II selects the "Void Judge", the boss uses no special attack until rank III.
- The Death Ring takes mana before it checks the interval, thus it takes mana on each tick with no effect.
- An Ego that more than one player summons does not get full health: with two players it only has half of its health and starts the landmine barrage on the first tick.
- The ride-on sound of the Motor never plays, because the server calls a method that only works on the client.

### Multiplayer and server-side checks

- The original trusts data from the client in some places: the position and the energy of the Thunder Slash, the summon and the catch of the mounts, the dash and the ultimate of the Key of Flamescion. In this port the server examines and calculates them.
- Skill damage records the user as the attacker, thus the PvP rules and the Peace Amulet apply. Some damage kinds of the original have no attacker.
- In many places "one packet to the player on each tick" was changed: the client calculates the value itself, and the server only synchronizes when the state changes.

### No network access

- The original downloads the list of contributors from the internet at startup, and downloads skins by name. This port has the list (45 names) in the code. The Ego uses the skin of the summoner from the player list of the server, and the Ego Minions use the skin of the viewer.

### Common rules of the bosses

- The Herrscher of the Void and the Ego use the common rules of the Guardians of Gaia of this mod: the summon examines the inventory and the accessory slots of each player in the arena, and during the fight only the illegal items that are held or worn are removed. The original only examines the summoner, and it drops all illegal items of the inventory.
- The attacks of a boss only hurt players. The target of an attack is the nearest player; the original always selects the first player of the player list.

### Adjusted values and rules

- The transfer speed of the Mana Buffer is 1000 mana for each tick (the value of the original 1.16; the default of 1.12 is 400).
- The Flux Manafield and the Mana Liquefaction Device do not lose resources and do not make resources from nothing.
- The Stardust Lotus does not have the rule "a Nature Orb decreases the cost by 20%". A creature that was teleported is not teleported again for 40 ticks.
- Supply Bag Limited Edition: the first bag that a player opens always gives an Empty Herrscher Core, because the Core of The Void needs it. The pool is kept in the saved data of the world.
- The Ego has a 20% chance to drop its music disc. In the original this disc has no source.
- A swing of the Shadow Katana at nothing only summons a pixie against a monster. The original attacks each living entity within 8 blocks.

### Content that is not ported

- The function that attaches the Judah Oath to an accessory as a cosmetic item.
- The four configuration switches of the Herrscher of the Void.
- The rule of the original 1.12 that gives items at 6, 12 and 18 advancements, and the other advancements of 1.12.
- The butterflies of the Saigyouji Yuyuko's Fan have no hit effect. This agrees with the original, which is not complete.

## Verification status

**Verified**:

- All 211 automatic tests are successful.
- A second run of the data generators changes nothing.
- The release JAR has no duplicate files and no test or data generator code.
- The development client starts and gets to the title screen.

**Not verified yet**:

- The visuals and the feel in the game (model positions, animations, particles, screens, the pace of the boss fights).
- A real dedicated server with the release JAR.
- Real multiplayer.
- A real old world.
- A modpack environment, with the recipe display in JEI and EMI.

In short: **the logic has tests, and the presentation still needs a person to look at it**.

## Credits and license

- Original author: ExtraMeteorP (似水流年). This project is a tribute to him. Thank you for such a great mod.
- Author of "ExtraBotany: Reburn": Lounode.
- Initial 1.21.1 port and branch maintenance: Magellan67.
- Textures: Urranium. Armor model: Keletu.
- Botania: Vazkii and all contributors.
- The models for the Key of Flamescion and the Gem of Conquest come from Honkai Impact 3rd.

The code keeps the MIT license of Reburn. See [LICENSE](../LICENSE).

The GitHub repository of the original has no license file. In addition to the content that Reburn already brought, this version took the content below from the original repository. The rights to it belong to the original author or to the respective rights holders:

- Textures: the Key of Truth, the energy beam, the armor and the energy bar of the Herrscher of Thunder, the 35 frames of the Thunder Slash, the icon of Divine Justice, the texture of the Ego music disc.
- Music: the fight music of the Ego (`swordland.ogg` of the original 1.16).
- Text: the text of 14 lexicon entries and the titles of 6 advancements (from the language files of the original 1.12).
- The list of contributors (the 45 names in `contributors.md`).

This branch is not an official release of the original author, of Lounode, of Botania or of NeoForge. If a rights holder objects to the use of any of the content above, it will be adjusted or removed as the rights holder wishes.

## Build

```
./gradlew build
```

The result is `build/libs/extrabotany-neoforge-1.21.1-2.0.0.jar`. The client and the server use the same file. To run all tests: `./gradlew runGameTestServer`.
