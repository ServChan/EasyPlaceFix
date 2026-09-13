# EasyPlaceFix

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-brightgreen?style=flat-square&logo=minecraft)](README.md)
[![Platform](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square&logo=fabric)](README.md)
[![Java Target](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](README.md)
[![Mod Version](https://img.shields.io/badge/Version-0.6.6-purple?style=flat-square)](README.md)
[![Requires](https://img.shields.io/badge/Requires-Litematica%20%2B%20MaLiLib-lightgrey?style=flat-square)](README.md)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

Client-side Fabric mod that makes Litematica Easy Place reliable in multiplayer through orientation correction, tick-accurate anti-cheat pacing, note-block auto-tuning, and seamless container interaction.

## Русский

### Что это

`EasyPlaceFix` (mod id `easyplacefix`) — клиентский Fabric-мод для Minecraft 26.1.2–26.2, повышающий надёжность режима Litematica Easy Place на многопользовательских серверах: исправляет ориентацию блоков, синхронизирует задержки с игровым тикрейтом для обхода ложных срабатываний античитов, автоматически настраивает нотные блоки и выполняет многоэтапные взаимодействия со сложными блоками. Форк проекта [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix).

### Возможности

- корректная установка сложных блоков: ступеней, люков, дверей, табличек (настенных и висячих), полок, кафедр, крафтеров, наблюдателей, поршней, рельсов, голов, баннеров, цветочных горшков, яиц черепах, морских огурцов, рычагов, повторителей и компараторов;
- автонастройка нотных блоков: тональность читается из схемы Litematica и выставляется через безопасную очередь кликов с ограничением темпа;
- защита от античитов: интервалы установки считаются в реальных клиентских тиках (не по системному времени), множественные клики распределяются по тикам, тайминги рандомизируются (`placementJitter`);
- взаимодействие с контейнерами: сундуки, бочки, шалкеры, печи, воронки, крафтеры открываются с блоком в руке без отключения Easy Place (`AllowInteraction`);
- режим ослабленного совпадения (`loosenMode`): гибкая подстановка аналогичных материалов;
- вкладка `Easy Fix` прямо в настройках Litematica с многострочными подсказками и локализацией на 5 языков (EN, RU, ES-ES, ES-MX, ZH-CN);
- совместимость с обеими версиями MaLiLib (`0.28.9` / `0.29.3`) и Litematica (`0.27.10` / `0.28.4`);
- строго ограниченные очереди действий, автоочистка очередей и сброс угла обзора при выходе из мира;
- опциональная **защита при раскопках** (`excavationGuard`, по умолчанию ВЫКЛ): пока включён режим Easy Place у Litematica, не даёт ломать блоки за пределами загруженной схемы и блоки, уже установленные точно так, как требует схема.
- опциональная **автосмена инструмента** (`autoToolSwitch`, по умолчанию ВЫКЛ): пока включён режим Easy Place у Litematica, перед началом ломания блока подставляет в руку лучший подходящий инструмент из инвентаря — так же, как Easy Place уже подставляет нужный блок при установке.
- опциональная **автозамена земли** (`terrainAutoReplace`, по умолчанию ВЫКЛ): если схема хочет другой вариант земли/травы (в любом снежном состоянии)/грубой земли/тропинки, чем сейчас стоит на месте, сначала ломает неверный блок и сразу повторяет установку — вместо отказа «нельзя заменить».

**Ключевые параметры (вкладка Easy Fix):** `placementPreset` (`Balanced` — 2 тика, `Safe` — 4 тика, `Fast` — 1 тик, `Custom` — вручную) · `placementJitter` (случайные 0–1 тик) · `AllowInteraction` · `clientRotationRevert` (возврат серверного взгляда, авто-таймаут 1.5 с) · `nbtIgnore` · `observerDetect`.

### Управление

Мод не регистрирует собственных горячих клавиш и работает поверх обычной клавиши Litematica Easy Place. Настройки — на вкладке **Easy Fix** в конфигурации Litematica.

### Настройки

Списки `loosenMode` хранятся в `config/loosenMode.json` (атомарная запись через `.tmp`, резервная копия `.bak`). Остальные параметры — на вкладке `Easy Fix` в GUI Litematica.

### Установка

1. **Fabric Loader** `0.19.3+` и **Fabric API**.
2. Установите **MaLiLib** и **Litematica** (см. таблицу совместимости версий ниже).
3. Скопируйте `EasyPlaceFix-0.6.6.jar` из `build/libs/` в папку `mods/`.

**Совместимость:**

| Minecraft | Litematica | MaLiLib | Fabric API |
|---|---|---|---|
| `26.1.2` | `0.27.10` | `0.28.9` | `0.153.0+26.1.2` |
| `26.2` | `0.28.4` | `0.29.3` | `0.153.0+26.2` |

**Требования:** Minecraft `26.1.2`–`26.2` (один JAR) · Java `25` · только клиент.

### Сборка

```powershell
.\gradlew.bat clean build                            # 26.1.2 (по умолчанию)
.\gradlew.bat clean build "-Pminecraft_version=26.2" # проверка на 26.2
```

Готовый JAR: `build/libs/EasyPlaceFix-0.6.6.jar`.

---

## English

### What It Is

`EasyPlaceFix` (mod id `easyplacefix`) is a client-side Fabric mod for Minecraft 26.1.2–26.2 that enhances Litematica Easy Place reliability on multiplayer servers by correcting block orientations, pacing placements to avoid anti-cheat kicks, auto-tuning note blocks, and bridging multi-click interactions. A fork of [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix).

### Features

- accurate placement for stairs, trapdoors, doors, signs (wall and hanging), shelves, lecterns, crafters, observers, pistons, rails, skulls, banners, flower pots, turtle eggs, sea pickles, levers, repeaters, and comparators;
- note-block auto-tuning: the schematic pitch is read from Litematica and applied via a rate-limited, anti-cheat-safe click queue;
- anti-cheat friendly pacing: placement intervals counted in real client ticks (not wall-clock time), tick-spread multi-clicks, and timing jitter (`placementJitter`);
- seamless container interaction: chests, barrels, shulkers, furnaces, hoppers, and crafters open while holding blocks without disabling Easy Place (`AllowInteraction`);
- `loosenMode`: relaxed building-block substitution with customizable lists;
- a native `Easy Fix` tab inside the Litematica GUI with multi-line tooltips and 5-language localization (EN, RU, ES-ES, ES-MX, ZH-CN);
- runtime compatibility with both MaLiLib (`0.28.9` / `0.29.3`) and Litematica (`0.27.10` / `0.28.4`);
- bounded action queues, automatic queue flush, and view reset on world transitions;
- optional **Excavation guard** (`excavationGuard`, OFF by default): while Litematica's Easy Place mode is on, stops breaking blocks outside the loaded schematic and blocks already placed exactly as the schematic expects.
- optional **Auto tool switch** (`autoToolSwitch`, OFF by default): while Litematica's Easy Place mode is on, swaps your held item for the best matching tool in your inventory right before a block starts breaking, the same way Easy Place already swaps your held item for the right block on placement.
- optional **Terrain auto-replace** (`terrainAutoReplace`, OFF by default): if the schematic wants a different dirt / grass block (any snow state) / coarse dirt / dirt path than what is currently there, mines out the wrong one first and immediately retries the placement instead of failing with "not replaceable".

**Key settings (Easy Fix tab):** `placementPreset` (`Balanced` — 2 ticks, `Safe` — 4 ticks, `Fast` — 1 tick, `Custom` — manual) · `placementJitter` (random 0–1 tick) · `AllowInteraction` · `clientRotationRevert` (restore server rotation, 1.5 s safety timeout) · `nbtIgnore` · `observerDetect`.

### Controls

No mod-specific key bindings; it works on top of Litematica's normal Easy Place key. Settings live on the **Easy Fix** tab in the Litematica config.

### Configuration

`loosenMode` lists are stored in `config/loosenMode.json` (atomic write via `.tmp`, `.bak` backup). All other options are on the `Easy Fix` tab in the Litematica GUI.

### Installation

1. **Fabric Loader** `0.19.3+` and **Fabric API**.
2. Install **MaLiLib** and **Litematica** (see the version matrix below).
3. Copy `EasyPlaceFix-0.6.6.jar` from `build/libs/` into `mods/`.

| Minecraft | Litematica | MaLiLib | Fabric API |
|---|---|---|---|
| `26.1.2` | `0.27.10` | `0.28.9` | `0.153.0+26.1.2` |
| `26.2` | `0.28.4` | `0.29.3` | `0.153.0+26.2` |

**Requirements:** Minecraft `26.1.2`–`26.2` (single JAR) · Java `25` · client-only.

### Building

```powershell
.\gradlew.bat clean build                            # 26.1.2 (default)
.\gradlew.bat clean build "-Pminecraft_version=26.2" # verify against 26.2
```

Output JAR: `build/libs/EasyPlaceFix-0.6.6.jar`.

## Лицензия / License

Original project by [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix). Licensed under [MIT](LICENSE).
