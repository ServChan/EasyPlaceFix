# EasyPlaceFix — User Guide

Mod version: 0.6.7 · Minecraft 26.3 · Fabric · client-only · a Litematica add-on

[Русская версия](USER_GUIDE_RU.md)

EasyPlaceFix is a fork of [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix).

## Contents

1. [What the mod does](#what-the-mod-does)
2. [What you need](#what-you-need)
3. [Installation](#installation)
4. [Getting started](#getting-started)
5. [Placement speed and anti-cheat](#placement-speed-and-anti-cheat)
6. [Complex blocks](#complex-blocks)
7. [Breaking and excavation](#breaking-and-excavation)
8. [Schematic entities](#schematic-entities)
9. [Note blocks](#note-blocks)
10. [Material list](#material-list)
11. [Loosen mode](#loosen-mode)
12. [All settings](#all-settings)
13. [Commands](#commands)
14. [Files](#files)
15. [FAQ](#faq)
16. [Building from source](#building-from-source)

## What the mod does

[Litematica](https://modrinth.com/mod/litematica) has an **Easy Place** mode: hold the right mouse button and blocks go exactly where the schematic wants them. On servers it often goes wrong: stairs and trapdoors face the wrong way, complex blocks fail, and packets go out so fast that the anti-cheat kicks you.

EasyPlaceFix takes over Easy Place:

- it places blocks with the right facing, half, shape and note;
- it paces placement in real game ticks so the anti-cheat does not kick you;
- it tunes note blocks by itself;
- optionally it protects correct blocks from accidental breaking, switches tools, replaces wrong blocks and places item frames, paintings and armor stands from the schematic;
- it helps with the material list: Excel export and highlights in chests.

If you want a complete Litematica replacement without MaLiLib, see [Litematica Plus LTS](https://github.com/ServChan/LitematicaPlus-LTS): every EasyPlaceFix feature is built into it.

## What you need

| Mod | Version |
|---|---|
| Minecraft | 26.3 |
| [Fabric Loader](https://fabricmc.net/use/installer/) | 0.19.3 or newer |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.161.0+26.3 |
| [Litematica](https://modrinth.com/mod/litematica) | 0.29.1 (from 0.29.1 up to 0.30) |
| [MaLiLib](https://modrinth.com/mod/malilib) | 0.30.2 (from 0.30.2 up to 0.31) |

Java 25 is required.

## Installation

1. Install Fabric Loader, Fabric API, MaLiLib and Litematica in those versions.
2. Put `EasyPlaceFix-0.6.7.jar` into `mods` (see [Building](#building-from-source)).
3. Start the game.

## Getting started

**The mod is off by default.** Turn it on:

1. Open the Litematica menu (`M` by default) → Configuration.
2. Go to the **Easy Fix** tab.
3. Turn on "Enable EasyPlaceFix".
4. Turn on Litematica's Easy Place mode itself (Generic tab → `easyPlaceMode`, or its hotkey).

Now, as usual with Easy Place, aim at a ghost block of the schematic and press the right mouse button. EasyPlaceFix picks the right item, direction and timing.

If something does not get placed, turn on "Diagnostic status messages": the action bar then shows the reason ("Missing item: …", "Waiting for server sync…").

## Placement speed and anti-cheat

The server watches how often you send build actions. Too fast, and the "timer" anti-cheat kicks you. The "Placement speed preset" setting:

| Preset | Pause between blocks | For |
|---|---|---|
| Balanced (default) | 2 ticks (about 10 blocks a second) | most servers |
| Safe | 4 ticks (about 5 a second) | laggy or strict servers |
| Fast | 1 tick (about 20 a second) | singleplayer and lenient servers |
| Custom | "Custom placement delay" in ticks | manual tuning; 0 removes the limit and will almost certainly get you kicked |

"Randomize placement timing" adds a random 0–1 tick between placements so the rhythm is not perfectly even. It slows building a little; turn it on if you are kicked even on Balanced.

To place a directional block the mod briefly tells the server you look another way. "Revert client rotation" sends your real view back right after placing (one extra packet per block). When off, the server keeps the placement rotation until you move the mouse.

## Complex blocks

The mod places correctly:

- stairs, trapdoors, doors, signs (wall and hanging), shelves, lecterns, crafters;
- observers, pistons, rails, heads, banners, flower pots;
- turtle eggs, sea pickles, levers, repeaters and comparators;
- multi-step blocks: it sends the follow-up clicks when a block needs turning or adjusting.

Also:

- **Allow block interactions**: right-clicking correct containers and interactive blocks (chests, furnaces, doors) works as usual instead of trying to place something into them.
- **Observer safety check**: does not place an observer until the schematic around it is built, so it never fires early.
- **Ignore NBT / components**: matches items only by type when the schematic stores extra data (a name, a dye colour).

## Breaking and excavation

Everything in this section is off by default and works only while Easy Place is on.

- **Excavation guard** stops you from breaking blocks outside the loaded schematic ("Guard: protect outside schematic") and blocks already placed exactly as the schematic wants ("Guard: protect correctly placed blocks"). Spots where the schematic wants air are never protected. "Guard: show action bar hint" explains why a block will not break.
- **Auto tool switch** puts the best tool from your inventory into your hand right before breaking. It does nothing in creative mode.
- **Terrain auto-replace** — when the schematic wants dirt but grass is there (or coarse dirt, or a path), the mod breaks the wrong block and places the right one at once.
- **Break wrong blocks** — when a different block stands there, or the same block with the wrong facing, axis, half or hinge, the mod digs it out and places it again. Containers with items, unbreakable blocks, fluids and blocks that differ only in power, light, open state or connections are left alone.

## Schematic entities

"Place schematic entities" (off by default): aim at a ghost item frame, painting or armor stand and press the Easy Place key.

- **Item frames and glow item frames** go on the right face, get the right item and the stored rotation.
- **Paintings** are placed, but the server picks the motif. If it is wrong, the mod suggests breaking it and placing it more precisely.
- **Armor stands** are placed with their rotation and get their armor.

## Note blocks

A note block's pitch is read from the schematic, and the mod clicks the block the right number of times through a rate-limited safe queue. "Note block tuning HUD" (on) shows at the top right how many blocks are queued, how many clicks are left and which block is next.

## Material list

You need Litematica's material list: open it once from the placement menu.

- `/easyplacefix materials` shows a summary and the 10 most needed items.
- `/easyplacefix materials missing` shows what is left to gather: amount, stacks, shulker boxes.
- `/easyplacefix materials export` saves the list as Excel (`.xlsx` with Summary, To gather and All materials sheets). Other formats: `csv`, `md`, `json` or `all`. Files go to `easyplacefix/materials/` in the game folder.
- **Container helper** (on): in chests, barrels, shulker boxes, dispensers and hoppers, items the schematic still needs are highlighted green and shulker boxes holding them yellow. The "Take materials" button (or its hotkey) moves them into your inventory.

If the material list is not open yet, the `materials` command asks Litematica to count it; run it again a couple of seconds later.

## Loosen mode

"Loosen mode (block substitutes)" (off): when the exact item from the schematic is missing, a compatible substitute is allowed — any wall for a wall, any gate, any trapdoor, any coral fan, or an item from your own substitute list. Handy when changing a palette, but it can place "the wrong" block.

The substitute list is managed with commands:

| Command | What it does |
|---|---|
| `/easyplacefix loosen add [item]` | add an item (without an argument, the one in your hand) |
| `/easyplacefix loosen remove [item]` | remove it |
| `/easyplacefix loosen list` | show the list |
| `/easyplacefix loosen clear` | clear it |
| `/easyplacefix loosen reload` | reload the file |

## All settings

The **Easy Fix** tab in the Litematica configuration. Switches can get their own hotkey.

| Setting | Default |
|---|---|
| Enable EasyPlaceFix | off |
| Placement speed preset | Balanced |
| Custom placement delay (ticks) | 2 (Custom preset only) |
| Randomize placement timing | off |
| Loosen mode (block substitutes) | off |
| Ignore NBT / components | off |
| Allow block interactions | off |
| Observer safety check | off |
| Revert client rotation | off |
| Diagnostic status messages | off |
| Excavation guard | off |
| Guard: protect outside schematic | on |
| Guard: protect correctly placed blocks | on |
| Guard: show action bar hint | on |
| Auto tool switch (breaking) | off |
| Terrain auto-replace (dirt/grass/path) | off |
| Break wrong blocks | off |
| Place schematic entities | off |
| Note block tuning HUD | on |
| Material list helper in containers | on |

Hotkeys (unbound by default): loosen mode, ignore NBT, allow interactions, diagnostic messages, Take materials.

## Commands

| Command | What it does |
|---|---|
| `/easyplacefix materials`, `materials missing`, `materials export [xlsx\|csv\|md\|json\|all]` | material list |
| `/easyplacefix loosen add\|remove\|list\|clear\|reload` | substitute list |
| `/easyplacefix report` | compatibility report: mod versions, server, ping, preset, delay |
| `/easyplacefix copy-report` | copy the report to the clipboard |
| `/easyplacefix last` | the last placement diagnostic |

## Files

- `config/loosenMode.json` holds the loosen substitute list (item IDs such as `minecraft:stone`). It is written atomically with a `.bak` copy.
- The other settings live in Litematica's configuration file.
- `easyplacefix/materials/` in the game folder holds exported material lists.

## FAQ

**Nothing changed after installing.** The mod is off by default: turn on "Enable EasyPlaceFix" on the Easy Fix tab.

**The server kicks me for placing too fast.** Pick the Safe preset and turn on "Randomize placement timing".

**A block is placed facing the wrong way.** Turn on "Diagnostic status messages" and read the reason. `/easyplacefix copy-report` helps when you open an issue.

**A chest does not open; Easy Place tries to place a block into it.** Turn on "Allow block interactions".

**A block will not break.** Most likely the excavation guard: the block is outside the schematic or already correct. The action bar hint says which.

**The material export is not saved.** The file is open in Excel or another program. Close it and try again.

## Building from source

JDK 25 is required.

```powershell
git clone https://github.com/ServChan/EasyPlaceFix.git
cd EasyPlaceFix
.\gradlew.bat build
```

On Linux and macOS use `./gradlew`. The jar is `build/libs/EasyPlaceFix-0.6.7.jar`.

The `backport/1.21.11` branch holds the version for Minecraft 1.21.11.
