# EasyPlaceFix

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2--26.2-62B47A)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-client--side-DBD0B4)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-E76F00)](https://adoptium.net/)
[![Version](https://img.shields.io/badge/version-0.6.4-4C8BF5)](https://modrinth.com/mod/easyplacefix-fork)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

## Русский

Клиентский Fabric-мод повышает надёжность Litematica Easy Place на multiplayer-серверах: корректирует ориентацию, дополнительные взаимодействия, задержки и повторные попытки для сложных блоков.

### Возможности

- Специальная обработка лестниц, люков, знаков, полок, кафедр, крафтеров, наблюдателей, поршней, рельсов, голов, баннеров и настенных блоков.
- Вкладка `Easy Fix` в настройках Litematica.
- `loosenMode`, `nbtIgnore`, `AllowInteraction`, `observerDetect` и `clientRotationRevert`.
- Совместимость с изменившимся API bounding box в MaLiLib 0.28.9/0.29.3.
- Ограниченная очередь отложенных действий; очередь и фиксация взгляда очищаются при выходе, scheduler закрывается при остановке клиента.

### Данные и ограничения

- `config/loosenMode.json` хранит список предметов режима ослабленного совпадения; запись выполняется через временный файл.
- Мод не заменяет серверный протокол. С Servux используйте `AUTO`; без серверной поддержки обычно требуется `SLAB_ONLY`.
- Результат зависит от задержки сервера и защиты взаимодействий. Установка на сервер не требуется.

### Совместимость

- MC 26.1.2: Litematica 0.27.10, MaLiLib 0.28.9.
- MC 26.2: Litematica 0.28.4, MaLiLib 0.29.3.
- Fabric Loader 0.19.3+, Java 25. Отдельный TickPrediction не нужен.

### Сборка

```powershell
.\gradlew.bat clean build --warning-mode all
.\gradlew.bat clean build '-Pminecraft_version=26.2' --warning-mode all
.\gradlew.bat clean build --warning-mode all
```

## English

This client-side Fabric mod makes Litematica Easy Place more reliable on multiplayer servers by improving orientation, follow-up interactions, timing, and retries for complex blocks.

### Features

- Dedicated handling for stairs, trapdoors, signs, shelves, lecterns, crafters, observers, pistons, rails, heads, banners, and wall-mounted blocks.
- An `Easy Fix` Litematica settings tab.
- `loosenMode`, `nbtIgnore`, `AllowInteraction`, `observerDetect`, and `clientRotationRevert`.
- A compatibility bridge for the MaLiLib bounding-box API change.
- A bounded delayed-action queue that is invalidated on disconnect and shut down with the client.

### Data and limitations

`config/loosenMode.json` stores loosened item matching through temporary-file replacement. The mod does not replace server protocol support: use `AUTO` with Servux and generally `SLAB_ONLY` without it. Server latency and interaction protection can still affect placement.

Minecraft 26.1.2 uses Litematica 0.27.10/MaLiLib 0.28.9; 26.2 uses 0.28.4/0.29.3. Run the three commands above and install the normal JAR from `build/libs/`.

## Credits and license

Original project: [223225zzzkkk/easyplaceFix](https://github.com/223225zzzkkk/easyplaceFix). Licensed under MIT; see [LICENSE](LICENSE).
