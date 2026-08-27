# EasyPlaceFix

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-brightgreen?style=flat-square&logo=minecraft)](README.md)
[![Platform](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square&logo=fabric)](README.md)
[![Java Target](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](README.md)
[![Mod Version](https://img.shields.io/badge/Version-0.6.5-purple?style=flat-square)](README.md)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

Client-side Fabric mod that makes Litematica Easy Place reliable in multiplayer through orientation correction, anti-cheat pacing, noteblock auto-tuning, and seamless container interaction.

## Русский

### Что это

`EasyPlaceFix` — клиентский Fabric-мод, повышающий надёжность и стабильность режима Litematica Easy Place на многопользовательских серверах: исправляет ориентацию блоков, синхронизирует задержки с игровым тикрейтом для обхода ложных срабатываний античитов, автоматически настраивает нотные блоки и выполняет многоэтапные взаимодействия со сложными блоками.

### Что дает мод

- **Безошибочную установку сложных блоков:** ступеней, люков, дверей, табличек (настенных и висячих), полок, кафедр, крафтеров, наблюдателей, поршней, рельсов, голов, баннеров, цветочных горшков, яиц черепах, морских огурцов, рычагов, повторителей и компараторов;
- **Автоматическую настройку нотных блоков:** считывает нужную тональность из схемы Litematica и автоматически настраивает нотный блок через безопасную очередь кликов;
- **Защиту от античитов (Anti-Cheat Friendly):** расчет интервалов установки в реальных клиентских тиках (вместо системного времени), распределение множественных кликов по тикам и рандомизация таймингов (`placementJitter`);
- **Взаимодействие с контейнерами:** открытие сундуков, бочек, шалкеров, печей, воронок и крафтеров с блоком в руке без помех со стороны Easy Place (`AllowInteraction`);
- **Режим ослабленного совпадения (`loosenMode`):** гибкая подстановка аналогичных строительных материалов (различные виды камня, дерева, шерсти и т.д.);
- **Встроенную вкладку `Easy Fix`:** меню настроек прямо в конфигурации Litematica с подробными многострочными подсказками и поддержкой 5 языков (RU, EN, ES-ES, ES-MX, ZH-CN);
- **Совместимость с API:** бесшовная поддержка как старых, так и новых версий MaLiLib (`0.28.9` / `0.29.3`) и Litematica (`0.27.10` / `0.28.4`).

### Основные параметры (вкладка Easy Fix)

- **`placementPreset`** — пресеты скорости установки:
  - `Balanced` — задержка 2 тика (рекомендуемый баланс скорости и надежности на большинстве серверов);
  - `Safe` — задержка 4 тика (максимальная стабильность при высоком пинге или строгих античитах);
  - `Fast` — задержка 1 тик (максимальный темп в рамках ванильной механики);
  - `Custom` — ручная установка произвольной задержки (включая 0 тиков).
- **`placementJitter`** — случайное добавление 0–1 тика задержки между установками для обхода эвристических проверок на строгий фиксированный темп кликов.
- **`AllowInteraction`** — разрешение на свободное открытие контейнеров и использование интерфейсов при включенном Easy Place.
- **`clientRotationRevert`** — мгновенный возврат серверного направления взгляда игрока после позиционирования блоков с защитным авто-таймаутом 1.5 с.
- **`nbtIgnore`** — игнорирование различий в NBT-компонентах блоков при проверке совпадения со схемой.
- **`observerDetect`** — специализированный алгоритм ориентации наблюдателей.

### Особенности архитектуры

- Строго ограниченные очереди действий, предотвращающие утечки памяти и зависания при лагах;
- Автоматическая очистка всех очередей и корректный сброс угла обзора при выходе из мира или отключении от сервера;
- Атомарное сохранение списков `config/loosenMode.json` через временные файлы (`.tmp`) с резервными копиями (`.bak`);
- Корректное закрытие контекстов предсказания блоков в `ClientLevel`.

### Установка

1. Установите **Fabric Loader** 0.19.3+ и **Java 25**.
2. Установите **MaLiLib** и **Litematica**.
3. Поместите JAR-файл из `build/libs/` в папку `.minecraft/mods/`.

### Совместимость

- **Minecraft 26.1.2:** Litematica `0.27.10`, MaLiLib `0.28.9`, Fabric API `0.153.0+26.1.2`;
- **Minecraft 26.2:** Litematica `0.28.4`, MaLiLib `0.29.3`, Fabric API `0.153.0+26.2`;
- **Fabric Loader:** 0.19.3+;
- **Java:** 25.

### Сборка

```powershell
# Сборка под Minecraft 26.1.2 (по умолчанию)
.\gradlew.bat clean build --warning-mode all

# Сборка под Minecraft 26.2
.\gradlew.bat clean build '-Pminecraft_version=26.2' --warning-mode all
```

Итоговый файл: `build/libs/easyplacefix-0.6.5.jar`.

---

## English

### What It Is

`EasyPlaceFix` is a client-side Fabric mod that enhances Litematica Easy Place reliability on multiplayer servers by correcting block orientations, pacing placements to avoid server anti-cheat kicks, auto-tuning note blocks, and bridging complex multi-click interactions.

### What It Adds

- **Accurate placement algorithms:** stairs, trapdoors, doors, signs (wall and hanging), shelves, lecterns, crafters, observers, pistons, rails, skulls, banners, flower pots, turtle eggs, sea pickles, levers, repeaters, and comparators;
- **Note block auto-tuning:** automatically tunes placed note blocks to the schematic pitch via a rate-limited, anti-cheat safe queue;
- **Anti-cheat friendly pacing:** placement intervals counted in real client ticks (instead of wall-clock time), tick-spread multi-clicks, and timing jitter (`placementJitter`) to prevent timer kicks;
- **Seamless container interaction:** open chests, barrels, shulker boxes, furnaces, hoppers, and crafters while holding blocks without disabling Easy Place (`AllowInteraction`);
- **`loosenMode`:** relaxed building block substitution with customizable substitution lists;
- **Native `Easy Fix` tab:** dedicated configuration tab inside Litematica GUI with detailed multi-line tooltips and 5-language localization (EN, RU, ES-ES, ES-MX, ZH-CN);
- **API compatibility bridge:** full runtime compatibility with both legacy and modern MaLiLib (`0.28.9` / `0.29.3`) and Litematica (`0.27.10` / `0.28.4`).

### Key Settings (Easy Fix Tab)

- **`placementPreset`** — speed presets for placement:
  - `Balanced` — 2 ticks delay (recommended balance of speed and reliability on most servers);
  - `Safe` — 4 ticks delay (maximum stability under high latency or strict anti-cheat checks);
  - `Fast` — 1 tick delay (fastest vanilla-legal cadence);
  - `Custom` — manual delay configuration (including 0 ticks).
- **`placementJitter`** — adds random 0–1 tick delay between placements to defeat strict fixed-cadence heuristics.
- **`AllowInteraction`** — allows container interaction and GUI opening while holding blocks.
- **`clientRotationRevert`** — restores server-side rotation immediately after placement with a 1.5 s safety timeout.
- **`nbtIgnore`** — ignores NBT component differences during state matching.
- **`observerDetect`** — specialized orientation algorithm for observers.

### Features & Safety

- Bounded action queues preventing memory leaks and game freezes under high packet lag;
- Automated queue flush and view reset on server disconnects and world transitions;
- Atomic persistence for `config/loosenMode.json` with temporary file swaps and `.bak` recovery;
- Clean block prediction handler scoping in `ClientLevel`.

### Installation

1. Install **Fabric Loader** 0.19.3+ with **Java 25**.
2. Install **MaLiLib** and **Litematica**.
3. Place the JAR file from `build/libs/` into `.minecraft/mods/`.

### Compatibility

- **Minecraft 26.1.2:** Litematica `0.27.10`, MaLiLib `0.28.9`, Fabric API `0.153.0+26.1.2`;
- **Minecraft 26.2:** Litematica `0.28.4`, MaLiLib `0.29.3`, Fabric API `0.153.0+26.2`;
- **Fabric Loader:** 0.19.3+;
- **Java:** 25.

### Build

```powershell
# Build for Minecraft 26.1.2 (default)
.\gradlew.bat clean build --warning-mode all

# Build for Minecraft 26.2
.\gradlew.bat clean build '-Pminecraft_version=26.2' --warning-mode all
```

Output: `build/libs/easyplacefix-0.6.5.jar`.

## Credits

Original project by [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix). Licensed under MIT.
