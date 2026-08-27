# EasyPlaceFix

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-brightgreen?style=flat-square&logo=minecraft)](README.md)
[![Platform](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square&logo=fabric)](README.md)
[![Java Target](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](README.md)
[![Mod Version](https://img.shields.io/badge/Version-0.6.5-purple?style=flat-square)](README.md)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

Client-side Fabric mod that makes Litematica Easy Place reliable in multiplayer through orientation correction, retries, and delay bridges.

## Русский

### Что это

`EasyPlaceFix` — клиентский Fabric-мод, повышающий надёжность и стабильность режима Litematica Easy Place на многопользовательских серверах: исправляет ориентацию, выполняет повторные попытки при рассинхроне и корректно рассчитывает тайминги для сложных блоков.

### Что дает мод

- безошибочную установку сложных блоков: ступеней, люков, табличек, полок, крафтеров, наблюдателей, поршней, рельсов, голов и баннеров;
- режим ослабленного совпадения (`loosenMode`) для строительных материалов;
- опции `nbtIgnore`, `AllowInteraction`, `observerDetect` и `clientRotationRevert`;
- встроенную вкладку `Easy Fix` прямо в конфигурационном окне Litematica;
- совместимость с изменением пакета BoundingBox в MaLiLib 0.28.9 / 0.29.3.

### Особенности

- строго ограниченная очередь отложенных действий, исключающая переполнение памяти;
- автоматическая очистка очередей и сброс взгляда игрока при выходе из мира или отключении от сервера;
- корректное завершение планировщика потоков при остановке клиента.

### Настройки

Файл конфигурации: `config/loosenMode.json` — хранит списки блоков режима ослабленного совпадения. Настройки параметров Easy Place производятся во вкладке `Easy Fix` настроек Litematica. Запись выполняется атомарно через временные файлы.

### Установка

1. Установите **Fabric Loader** 0.19.3+ и **Java 25**.
2. Установите **MaLiLib** и **Litematica**.
3. Поместите JAR из `build/libs/` в папку `mods/`.

### Совместимость

- **MC 26.1.2:** Litematica `0.27.10`, MaLiLib `0.28.9`;
- **MC 26.2:** Litematica `0.28.4`, MaLiLib `0.29.3`;
- **Fabric Loader:** 0.19.3+;
- **Java:** 25.

### Сборка

```powershell
.\gradlew.bat clean build --warning-mode all
.\gradlew.bat clean build '-Pminecraft_version=26.2' --warning-mode all
.\gradlew.bat clean build --warning-mode all
```

Итоговый файл: `build/libs/easyplacefix-0.6.5.jar`.

---

## English

### What It Is

`EasyPlaceFix` is a client-side Fabric mod that dramatically enhances Litematica Easy Place reliability on multiplayer servers by correcting block orientations, handling latency retries, and bridging complex block interactions.

### What It Adds

- specialized placement algorithms for stairs, trapdoors, signs, shelves, lecterns, crafters, observers, pistons, rails, heads, and banners;
- `loosenMode` for relaxed building block substitution;
- `nbtIgnore`, `AllowInteraction`, `observerDetect`, and `clientRotationRevert` toggles;
- native `Easy Fix` settings tab embedded inside Litematica configuration GUI;
- runtime compatibility bridge for MaLiLib 0.28.9/0.29.3 bounding box package updates.

### Features

- bounded action queue preventing memory leaks under high packet lag;
- automated queue flush and view reset on server disconnects and world transitions;
- clean scheduler daemon termination on game shutdown.

### Settings

Configuration file `config/loosenMode.json` stores relaxed matching rules with atomic write safety. Configuration switches are managed within Litematica's `Easy Fix` menu.

### Installation

1. Install **Fabric Loader** 0.19.3+ with **Java 25**.
2. Install **MaLiLib** and **Litematica**.
3. Place the JAR file from `build/libs/` into `.minecraft/mods`.

### Compatibility

- **MC 26.1.2:** Litematica `0.27.10`, MaLiLib `0.28.9`;
- **MC 26.2:** Litematica `0.28.4`, MaLiLib `0.29.3`;
- **Fabric Loader:** 0.19.3+;
- **Java:** 25.

### Build

```powershell
.\gradlew.bat clean build --warning-mode all
.\gradlew.bat clean build '-Pminecraft_version=26.2' --warning-mode all
.\gradlew.bat clean build --warning-mode all
```

Output: `build/libs/easyplacefix-0.6.5.jar`.

## Credits

Original project by [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix). Licensed under MIT.
