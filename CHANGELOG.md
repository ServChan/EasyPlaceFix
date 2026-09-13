# Changelog

## 0.6.6-1.21.11

- Requires Java 21 or newer.
- Backported note-block auto-tuning for issue #10. Tuning uses one shared queue and sends at most one interaction every two ticks.
- Fixed `AllowInteraction` for issue #11. Container and interactive-block use is now allowed before the Easy Place protocol check, including AUTO/Servux V3, and no longer depends on an empty hand.
- Extended `AllowInteraction` to beacons, brewing stands, cartography tables, crafting tables, enchanting tables, respawn anchors, and smithing tables.
- Added Balanced, Safe, Fast, and Custom placement-speed presets plus optional 0-1 tick timing jitter.
- Spread all follow-up clicks for repeaters, trapdoors, and other multi-click blocks over separate ticks.
- Added optional Excavation guard, Auto tool switch, and Terrain auto-replace features. All are disabled by default.
- Bounded delayed-action queues, added a 1.5-second rotation-lock timeout, and clear delayed feature state on disconnect.
- Added English, Russian, Spanish, Latin American Spanish, and Simplified Chinese interface translations.
- Removed an unreachable custom interaction-packet serializer and stopped reading `loosenMode.json` on every fallback placement; writes now use a temporary file and backup recovery.
