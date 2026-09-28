# Changelog

## 0.7.0

### Minecraft 26.3

- Ported to Minecraft 26.3; 26.1.2 and 26.2 are no longer supported. Requires Litematica 0.29.1, MaLiLib 0.30.2 and Fabric API 0.161.0+26.3.

### New features

- **Litematica material list tools.** `/easyplacefix materials` shows a summary and the most needed items, `materials missing` lists everything still to gather (count, stacks, shulker boxes), and `materials export [xlsx|csv|md|json|all]` saves the list to `.minecraft/easyplacefix/materials/`. The Excel file is written natively (no extra libraries) with Summary / To gather / All materials sheets, a styled frozen header, autofilter and column widths; files are written off the main thread and the chat message links to the file and its folder. If no list is open, one is created for the selected placement.
- **Take materials from storage.** In chests, barrels, shulker boxes, dispensers and hoppers, slots with items the material list still needs are highlighted green and shulker boxes containing them yellow. The "Take materials" button (or `materialRefillHotkey`) shift-clicks them into your inventory one slot per tick, never more than your free slots allow (`materialHelper`, ON by default).
- **Break wrong blocks** (`breakWrongBlocks`, OFF by default): Easy Place mines out a different block, or the same block with the wrong facing / axis / half / hinge / orientation, and retries the placement. Mining time follows the real break speed (up to 30 s). Containers with an inventory, unbreakable blocks, fluids and state-only differences (powered, lit, open, connections, snowy) are never broken.
- **Schematic entities** (`entityPlacement`, OFF by default): item frames and glow item frames are hung on the right face, filled with the right item and rotated; paintings are hung on the right face (exact motif when you carry the exact painting item); armor stands are placed with the saved rotation and dressed with armor from your inventory. Mismatches that clicking cannot fix (wrong item in a frame, wrong painting motif, wrong stand rotation) are reported instead of guessed.
- **Note block tuning HUD** (`noteTuningHud`, ON by default): top-right panel with queued blocks, clicks left, the next block and a progress bar.

### Fixes

- End rods placed in a column no longer alternate direction; Easy Place explains that the far end has to go first instead of placing a flipped rod.
- Door upper halves and bed heads are no longer placed one block too high when the other half's cell is occupied.
- Hanging signs under a crafting table, chest or other interactive block are placed without opening it (and without turning into an attached sign).
- The delayed-action scheduler counts real client ticks (Fabric client tick events) instead of 50 ms wall-clock steps, so pacing follows `/tick rate` and client lag.
- The source folder is tracked as `mixin/` in git (was `Mixin/`, which only worked on case-insensitive file systems).

### Tests

- Unit tests for the XLSX writer (well-formed parts, escaping, frozen header, autofilter), export file names and formats, the loosen list format and migration, and placement presets (25 tests in total).

### Placement fixes

- Double slabs: completing a slab now clicks the correct face (UP on a bottom slab, DOWN on a top slab); previously the server refused and could place a stray slab one block lower.
- Shulker boxes are placed with the schematic facing (vanilla uses the clicked face, not the look direction); previously every box faced UP.
- Wall hanging signs attach to a side support with the schematic facing; previously they always came out rotated 90 degrees or in the neighbouring cell.
- Glow lichen / sculk veins / resin clumps attach to the correct face, and multi-face states add the next missing face instead of re-targeting the first one.
- Cocoa clicks the jungle log it hangs from instead of the empty block behind it.
- Redstone dust is placed directly into its own cell, so the second "dot" click toggles the wire instead of clicking (and possibly opening) the block underneath.
- Extra clicks (cake bites, powered levers, open doors, candles, dot redstone) now target the placed block itself; they no longer hit the supporting block, skip while you are sneaking, and re-send the placement rotation so fence gates do not flip.
- Doors, fence gates and trapdoors only get the open/close toggle click when the placed state differs from the schematic, so a redstone-powered open block is no longer closed.
- Chests: a double-chest half no longer lands in the partner cell when that cell holds water, grass or cave air; a stray raw sneak packet that could leave the server thinking you sneak is gone.
- Ascending north/south rails get the north/south orientation.
- Wall heads are placed on any non-replaceable support (fences, walls, ...), matching vanilla; tripwire hooks are skipped until their support exists.
- Torches, candles, levers, buttons, bells, doors, signs, ladders, cake, coral wall fans and heads now sneak when placed onto crafting tables, barrels, furnaces, note blocks, repeaters/comparators, jukeboxes, lecterns and other interactive blocks instead of opening or toggling them.
- Piston placement prediction override is cleared after every use and never leaks onto the integrated server thread in singleplayer.

### Reliability

- Note block tuning no longer overshoots on high ping: it sends exactly the clicks needed, waits for the server to confirm, and only resends after a ping-based timeout. It also skips while you sneak and clicks the face nearest to you.
- Terrain auto-replace no longer loops on a correct grass block whose only difference is the snowy state, and the immediate retry no longer fails on the per-position cooldown.
- Excavation guard no longer blocks every break when no schematic placement is loaded, and "correctly placed" now ignores neighbour-driven properties (fence/wall/pane connections, redstone wire shape, snowy).
- Container and sign-editor packet suppression is time-boxed (3 s), scales with ping, never drops the player's own inventory contents, and can no longer stay stuck after a dropped task.
- Crafter container-id prediction wraps like the server (`id % 100 + 1`).
- "Revert client rotation" really restores the server-side rotation (the look lock no longer rewrites the revert packet).
- Auto tool switch skips tools that would break on the next block.

### Loosen list

- `config/loosenMode.json` now stores item ids (`minecraft:stone`) instead of raw numeric registry ids that change between versions; old numeric files are migrated on load. Writes happen off the main thread.
- New commands: `/easyplacefix loosen add [item]`, `remove [item]`, `list`, `clear`, `reload` (no argument = held item).

### Interface

- Spanish (es_es / es_mx) translations completed; about 75% of the strings were still English.
- Placement preset names and the four extra hotkeys are localized.
- Diagnostic line no longer has a double space after "EasyPlaceFix"; the rotation-revert tooltip now describes what the option actually does.

## 0.6.6

- New optional **Auto tool switch** (`autoToolSwitch`, OFF by default). While Litematica Easy Place is active, the mod selects the fastest suitable tool from the player's inventory before mining begins. Creative mode is left unchanged.
- New optional **Terrain auto-replace** (`terrainAutoReplace`, OFF by default). Easy Place can now clear and replace mismatched dirt, grass blocks, coarse dirt, and dirt paths when both the existing and schematic blocks belong to that terrain group. The retry is capped at 40 ticks and its state is cleared on disconnect.
- New optional **Excavation guard** (OFF by default, config tab + optional hotkey). While Litematica's Easy Place mode is on:
  - cancels breaking any block outside every loaded schematic placement (`excavationGuardProtectOutside`);
  - cancels breaking a block inside the schematic that already matches it exactly (`excavationGuardProtectCorrect`); a spot where the schematic expects air is never protected;
  - shows a short action bar hint explaining why a break attempt was blocked (`excavationGuardHint`).
  - Reuses the existing `EasyPlaceHandler.isSchematicBlock` placement-geometry check (already tolerant of the MaLiLib bounding-box API transition), so it shares the same schematic-area definition as the rest of Easy Place.
- Fixed the 4 new Excavation guard hotkeyed toggles not actually being bindable: they were registered as `ConfigBooleanHotkeyed` but never added to `Hotkeys.init()` / `easyPlaceFixHotkeys.addCallbacks()`, so a keybind assigned to them in the Litematica GUI would do nothing.
- Fixed the same pre-existing gap for `placementJitter` ("Randomize placement timing"), which had the identical problem from an earlier version.
- Fixed the new Auto tool switch and Terrain auto-replace toggles not being registered with MaLiLib's keybind manager.
- Fixed Terrain auto-replace being able to clear an eligible terrain block when the schematic actually required an unrelated block type.

## 0.6.5

- Fixed players being kicked by server "timer" anti-cheat (e.g. `build: timer-A`) while building fast with Easy Place.
  - Global placement pacing is now counted in real client ticks instead of wall-clock time, so the cadence follows the game loop the server expects.
  - Multi-click blocks (repeater delay, trapdoor toggles, ...) no longer send all their extra interactions in a single tick; each extra click is spread over one client tick.
  - `markGlobalPlacement` re-rolls optional timing jitter and the limiter resets cleanly on disconnect.
- Placement presets retimed for headroom: Balanced 1 -> 2 ticks, Safe 3 -> 4 ticks, Fast 0 -> 1 tick (vanilla-legal max). Only `Custom` can still disable the limit (delay 0).
- New option **Randomize placement timing** (`placementJitter`): adds a random 0-1 extra tick between placements to defeat strict "even interval" timer checks.
- Config tab reworked: clearer option names, logical grouping (core / pacing / matching / interaction / debug) and detailed multi-line hover tooltips explaining what each setting does and when to change it (English, Russian, Chinese).

### Audit fixes

- Look lock is now time-boxed: if the "restore rotation" task is dropped (bounded queue full) or throws, the player's server-side view no longer stays frozen - it releases automatically after 1.5 s.
- Note block tuning routes every click through one shared pump (max one interaction per 2 ticks, round-robin across all note blocks) instead of one independent per-tick clicker per block, so tuning a wall of note blocks no longer trips "timer" anti-cheat.
- `ClientLevel` sequence lookup no longer leaks the block-state prediction handler open (`startPredicting()` is now balanced with `close()`), preventing client block-prediction desync on the crafter / sign paths.
- Loosen mode no longer reads and parses `loosenMode.json` from disk on every fallback placement; it uses the already-loaded in-memory list.
- Piston placement-state override is armed immediately before its interaction instead of ~2 ticks earlier, so an unrelated `getStateForPlacement` call can no longer consume it.
- Removed dead, unreachable `MixinPlayerInteractBlockC2SPacket` / `IisSimpleHitPos` (never activated; carried an incomplete packet serializer).

## 0.6.4

- Version bump; container & interactive block placement mixins, persistence hardening, and translation sync.

## 0.6.3

- Fixed Easy Place crashes with Litematica 0.28.x and MaLiLib 0.29.x caused by the bounding-box API transition.
- Added runtime compatibility with both `containsPos(Vec3i)` and `contains(Vec3i)` bounding-box methods.
- Broadened dependency metadata to accept compatible Litematica 0.27.x/0.28.x and MaLiLib 0.28.x/0.29.x releases.

## 0.6.2

- Fixed `AllowInteraction` with Servux/V3 and the `SLAB_ONLY` fallback path.
- Added one mod build compatible with Minecraft 26.1.2 through 26.2.
- Added compatibility with Litematica 0.27.10/MaLiLib 0.28.9 on Minecraft 26.1.2 while retaining Litematica 0.28.4/MaLiLib 0.29.3 support on Minecraft 26.2.
- CI now compiles and uploads artifacts for both supported Minecraft versions.

## 0.6.1

- Updated Litematica placement bounding-box compatibility for Minecraft 26.2.

## 0.6.0

- Ported EasyPlaceFix to Minecraft 26.2.
