# EasyPlaceFix

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.3-brightgreen?style=flat-square&logo=minecraft)](README.md)
[![Platform](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square&logo=fabric)](README.md)
[![Java Target](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](README.md)
[![Mod Version](https://img.shields.io/badge/Version-0.7.0-purple?style=flat-square)](README.md)
[![Requires](https://img.shields.io/badge/Requires-Litematica%20%2B%20MaLiLib-lightgrey?style=flat-square)](README.md)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

Client-side Fabric mod that makes Litematica Easy Place reliable in multiplayer through orientation correction, tick-accurate anti-cheat pacing, note-block auto-tuning, and seamless container interaction.

## Русский

### Что это

`EasyPlaceFix` (mod id `easyplacefix`) — клиентский Fabric-мод для Minecraft 26.3, повышающий надёжность режима Litematica Easy Place на многопользовательских серверах: исправляет ориентацию блоков, синхронизирует задержки с игровым тикрейтом для обхода ложных срабатываний античитов, автоматически настраивает нотные блоки и выполняет многоэтапные взаимодействия со сложными блоками. Форк проекта [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix).

### Возможности

- корректная установка сложных блоков: ступеней, люков, дверей, табличек (настенных и висячих), полок, кафедр, крафтеров, наблюдателей, поршней, рельсов, голов, баннеров, цветочных горшков, яиц черепах, морских огурцов, рычагов, повторителей и компараторов;
- автонастройка нотных блоков: тональность читается из схемы Litematica и выставляется через безопасную очередь кликов с ограничением темпа;
- защита от античитов: интервалы установки считаются в реальных клиентских тиках (не по системному времени), множественные клики распределяются по тикам, тайминги рандомизируются (`placementJitter`);
- взаимодействие с контейнерами: сундуки, бочки, шалкеры, печи, воронки, крафтеры открываются с блоком в руке без отключения Easy Place (`AllowInteraction`);
- режим ослабленного совпадения (`loosenMode`): гибкая подстановка аналогичных материалов;
- вкладка `Easy Fix` прямо в настройках Litematica с многострочными подсказками и локализацией на 5 языков (EN, RU, ES-ES, ES-MX, ZH-CN);
- совместимость с MaLiLib `0.30.2` и Litematica `0.29.1`;
- строго ограниченные очереди действий, автоочистка очередей и сброс угла обзора при выходе из мира;
- опциональная **защита при раскопках** (`excavationGuard`, по умолчанию ВЫКЛ): пока включён режим Easy Place у Litematica, не даёт ломать блоки за пределами загруженной схемы и блоки, уже установленные точно так, как требует схема.
- опциональная **автосмена инструмента** (`autoToolSwitch`, по умолчанию ВЫКЛ): пока включён режим Easy Place у Litematica, перед началом ломания блока подставляет в руку лучший подходящий инструмент из инвентаря — так же, как Easy Place уже подставляет нужный блок при установке.
- опциональная **автозамена земли** (`terrainAutoReplace`, по умолчанию ВЫКЛ): если схема хочет другой вариант земли/травы (в любом снежном состоянии)/грубой земли/тропинки, чем сейчас стоит на месте, сначала ломает неверный блок и сразу повторяет установку — вместо отказа «нельзя заменить».
- опциональная **замена неверных блоков** (`breakWrongBlocks`, по умолчанию ВЫКЛ): если на месте стоит другой блок или тот же блок с неверным направлением/осью/половиной/петлёй, он выкапывается (с автосменой инструмента) и установка повторяется; не трогает контейнеры с инвентарём, неразрушаемые блоки, жидкости и блоки, отличающиеся только питанием/свечением/открытием/соединениями.
- опциональная **установка сущностей из схемы** (`entityPlacement`, по умолчанию ВЫКЛ): рамки и светящиеся рамки (с предметом и поворотом), картины и стойки для брони (с поворотом и бронёй) — наведитесь на призрачную сущность и нажмите клавишу Easy Place.
- **HUD настройки нотных блоков** (`noteTuningHud`, по умолчанию ВКЛ): панель в правом верхнем углу с числом блоков в очереди, оставшимися кликами и следующим блоком.
- **работа со списком материалов Litematica**: сводка и список недостающего в чате, экспорт в Excel (`.xlsx` с листами «Сводка», «Собрать», «Все материалы»), CSV, Markdown и JSON; в сундуках/бочках/шалкерах подсвечиваются нужные предметы (зелёным) и шалкеры с ними (жёлтым), кнопка «Взять материалы» и горячая клавиша переносят их в инвентарь (`materialHelper`, по умолчанию ВКЛ).

**Ключевые параметры (вкладка Easy Fix):** `placementPreset` (`Balanced` — 2 тика, `Safe` — 4 тика, `Fast` — 1 тик, `Custom` — вручную) · `placementJitter` (случайные 0–1 тик) · `AllowInteraction` · `clientRotationRevert` (возврат серверного взгляда, авто-таймаут 1.5 с) · `nbtIgnore` · `observerDetect`.

### Управление

Мод работает поверх обычной клавиши Litematica Easy Place. Настройки — на вкладке **Easy Fix** в конфигурации Litematica; у каждого переключателя можно назначить свою клавишу, а в списке горячих клавиш Litematica есть «Взять материалы» (работает в открытом сундуке/шалкере).

Команды:

| Команда | Что делает |
|---|---|
| `/easyplacefix materials` | сводка по списку материалов Litematica и 10 самых нужных предметов |
| `/easyplacefix materials missing` | что ещё собрать: количество, стаки, шалкеры |
| `/easyplacefix materials export [xlsx/csv/md/json/all]` | сохранить список в `.minecraft/easyplacefix/materials/` (по умолчанию Excel) |
| `/easyplacefix loosen add/remove [предмет]`, `list`, `clear`, `reload` | список замен для режима loosen |
| `/easyplacefix report`, `copy-report`, `last` | отчёт совместимости и последняя диагностика |

Если список материалов ещё не открыт, команда `materials` сама создаст его для выбранного размещения — повторите её через пару секунд.

### Настройки

Список `loosenMode` хранится в `config/loosenMode.json` в виде ID предметов (`minecraft:stone`; атомарная запись через `.tmp`, резервная копия `.bak`) и редактируется командами `/easyplacefix loosen add [предмет]`, `remove [предмет]`, `list`, `clear`, `reload` (без аргумента берётся предмет в руке). Остальные параметры — на вкладке `Easy Fix` в GUI Litematica.

### Установка

1. **Fabric Loader** `0.19.3+` и **Fabric API**.
2. Установите **MaLiLib** и **Litematica** (см. таблицу совместимости версий ниже).
3. Скопируйте `EasyPlaceFix-0.7.0.jar` из `build/libs/` в папку `mods/`.

**Совместимость:**

| Minecraft | Litematica | MaLiLib | Fabric API |
|---|---|---|---|
| `26.3` | `0.29.1` | `0.30.2` | `0.161.0+26.3` |

**Требования:** Minecraft `26.3` · Java `25` · только клиент.

### Сборка

```powershell
.\gradlew.bat clean build
```

Готовый JAR: `build/libs/EasyPlaceFix-0.7.0.jar`.

---

## English

### What It Is

`EasyPlaceFix` (mod id `easyplacefix`) is a client-side Fabric mod for Minecraft 26.3 that enhances Litematica Easy Place reliability on multiplayer servers by correcting block orientations, pacing placements to avoid anti-cheat kicks, auto-tuning note blocks, and bridging multi-click interactions. A fork of [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix).

### Features

- accurate placement for stairs, trapdoors, doors, signs (wall and hanging), shelves, lecterns, crafters, observers, pistons, rails, skulls, banners, flower pots, turtle eggs, sea pickles, levers, repeaters, and comparators;
- note-block auto-tuning: the schematic pitch is read from Litematica and applied via a rate-limited, anti-cheat-safe click queue;
- anti-cheat friendly pacing: placement intervals counted in real client ticks (not wall-clock time), tick-spread multi-clicks, and timing jitter (`placementJitter`);
- seamless container interaction: chests, barrels, shulkers, furnaces, hoppers, and crafters open while holding blocks without disabling Easy Place (`AllowInteraction`);
- `loosenMode`: relaxed building-block substitution with customizable lists;
- a native `Easy Fix` tab inside the Litematica GUI with multi-line tooltips and 5-language localization (EN, RU, ES-ES, ES-MX, ZH-CN);
- runtime compatibility with MaLiLib `0.30.2` and Litematica `0.29.1`;
- bounded action queues, automatic queue flush, and view reset on world transitions;
- optional **Excavation guard** (`excavationGuard`, OFF by default): while Litematica's Easy Place mode is on, stops breaking blocks outside the loaded schematic and blocks already placed exactly as the schematic expects.
- optional **Auto tool switch** (`autoToolSwitch`, OFF by default): while Litematica's Easy Place mode is on, swaps your held item for the best matching tool in your inventory right before a block starts breaking, the same way Easy Place already swaps your held item for the right block on placement.
- optional **Terrain auto-replace** (`terrainAutoReplace`, OFF by default): if the schematic wants a different dirt / grass block (any snow state) / coarse dirt / dirt path than what is currently there, mines out the wrong one first and immediately retries the placement instead of failing with "not replaceable".
- optional **Break wrong blocks** (`breakWrongBlocks`, OFF by default): a different block, or the same block with the wrong facing / axis / half / hinge, is mined out (with Auto tool switch) and the placement is retried; containers with an inventory, unbreakable blocks, fluids and blocks that only differ in powered / lit / open / connection state are never touched.
- optional **Schematic entities** (`entityPlacement`, OFF by default): item frames and glow item frames (with the item and its rotation), paintings, and armor stands (rotation and armor) — aim at the ghost entity and press the Easy Place key.
- **Note block tuning HUD** (`noteTuningHud`, ON by default): a top-right panel with queued blocks, clicks left and the next block.
- **Litematica material list tools**: chat summary and still-needed list, export to Excel (`.xlsx` with Summary / To gather / All materials sheets), CSV, Markdown and JSON; chests, barrels and shulker boxes highlight needed items (green) and shulker boxes that contain them (yellow), and the "Take materials" button or hotkey moves them into your inventory (`materialHelper`, ON by default).

**Key settings (Easy Fix tab):** `placementPreset` (`Balanced` — 2 ticks, `Safe` — 4 ticks, `Fast` — 1 tick, `Custom` — manual) · `placementJitter` (random 0–1 tick) · `AllowInteraction` · `clientRotationRevert` (restore server rotation, 1.5 s safety timeout) · `nbtIgnore` · `observerDetect`.

### Controls

It works on top of Litematica's normal Easy Place key. Settings live on the **Easy Fix** tab in the Litematica config; every toggle can have its own key, and Litematica's hotkey list has "Take materials" (works inside an open chest / shulker box).

Commands:

| Command | What it does |
|---|---|
| `/easyplacefix materials` | Litematica material list summary and the 10 most needed items |
| `/easyplacefix materials missing` | what is still to gather: count, stacks, shulker boxes |
| `/easyplacefix materials export [xlsx/csv/md/json/all]` | save the list to `.minecraft/easyplacefix/materials/` (Excel by default) |
| `/easyplacefix loosen add/remove [item]`, `list`, `clear`, `reload` | substitute list for loosen mode |
| `/easyplacefix report`, `copy-report`, `last` | compatibility report and last diagnostic |

If no material list is open yet, `materials` creates one for the selected placement — run it again after a couple of seconds.

### Configuration

The `loosenMode` list is stored in `config/loosenMode.json` as item ids (`minecraft:stone`; atomic write via `.tmp`, `.bak` backup) and is managed with `/easyplacefix loosen add [item]`, `remove [item]`, `list`, `clear`, `reload` (no argument uses the held item). All other options are on the `Easy Fix` tab in the Litematica GUI.

### Installation

1. **Fabric Loader** `0.19.3+` and **Fabric API**.
2. Install **MaLiLib** and **Litematica** (see the version matrix below).
3. Copy `EasyPlaceFix-0.7.0.jar` from `build/libs/` into `mods/`.

| Minecraft | Litematica | MaLiLib | Fabric API |
|---|---|---|---|
| `26.3` | `0.29.1` | `0.30.2` | `0.161.0+26.3` |

**Requirements:** Minecraft `26.3` · Java `25` · client-only.

### Building

```powershell
.\gradlew.bat clean build
```

Output JAR: `build/libs/EasyPlaceFix-0.7.0.jar`.

## Лицензия / License

Original project by [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix). Licensed under [MIT](LICENSE).
