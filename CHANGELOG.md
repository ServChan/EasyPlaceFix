# Changelog

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
