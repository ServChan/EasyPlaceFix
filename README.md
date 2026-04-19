# EasyPlaceFix

Client-side Fabric mod for Litematica that makes Easy Place much more reliable in multiplayer by improving placement logic, orientation handling, and interaction timing.

## Русский

### Что это

EasyPlaceFix это клиентский Fabric-мод для [Litematica](https://modrinth.com/mod/litematica), который улучшает Easy Place в мультиплеере и делает установку блоков заметно стабильнее на серверах.

Он нужен в тех случаях, когда стандартный Easy Place:
- ставит блоки с неправильной ориентацией;
- ошибается на интерактивных или сложных блоках;
- делает лишние клики;
- нестабильно работает при пинге или серверных задержках.

### Что дает мод

EasyPlaceFix добавляет к обычному Easy Place:
- более корректную ориентацию блоков при установке;
- более надежную работу на серверах с задержкой;
- улучшенную обработку блоков, которым нужны дополнительные взаимодействия;
- меньше лишних повторных действий и меньше рассинхрона;
- отдельные настройки и хоткеи для тонкой подстройки поведения;
- встроенную логику, из-за которой отдельный TickPrediction больше не нужен.

### Особенности

Мод включает специальную обработку для многих направляемых и интерактивных блоков, включая:
- stairs;
- trapdoors;
- signs и hanging signs;
- shelves и lecterns;
- crafters;
- observers;
- pistons;
- rails;
- skulls, banners и wall-mounted блоки;
- другие блоки, которым нужны особые hit result, допклики или аккуратная работа с состояниями.

Также мод:
- добавляет вкладку `Easy Fix` в GUI настроек Litematica;
- учитывает состояние блока перед повторной установкой;
- использует задержки там, где это нужно для стабильной постановки;
- лучше обрабатывает сложные случаи мультиплеера.

### Настройки

Мод добавляет собственные настройки в Litematica:
- `enableFix`: включает собственную логику EasyPlaceFix;
- `loosenMode`: разрешает более свободный подбор подходящего блока, если точный предмет не найден;
- `nbtIgnore`: ищет предмет без строгой проверки NBT;
- `AllowInteraction`: разрешает обычное взаимодействие с рядом контейнеров и интерактивных блоков;
- `observerDetect`: не дает ставить observer в потенциально некорректной ситуации, если целевой блок по схеме еще не совпадает;
- `clientRotationRevert`: возвращает клиентский поворот после служебного разворота игрока.

Также есть отдельные хоткеи для:
- `loosenMode`;
- `nbtIgnore`;
- `AllowInteraction`.

Для `loosenMode` используется файл:
- `config/loosenMode.json`

### Установка

Для работы нужны:
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Litematica](https://modrinth.com/mod/litematica)
- [MaLiLib](https://modrinth.com/mod/malilib)

Страница мода на Modrinth:
- https://modrinth.com/mod/easyplacefix-fork

Важно:
- мод клиентский;
- отдельная установка TickPrediction не требуется;
- в мультиплеере используйте протокол Easy Place: `SLAB_ONLY`.

### Совместимость

- Minecraft `26.1.1`
- Java `25`
- Fabric Loader `0.19.0+`
- Fabric API `0.145.4+26.1.1`
- Litematica `0.27.1`
- MaLiLib `0.28.2`
- Текущая версия мода в проекте: `0.5.9`

### Сборка

Требования:
- JDK 25

Команда сборки:
```bash
./gradlew clean build
```

Для Windows:
```bat
gradlew.bat clean build
```

Результат:
- `build/libs/*.jar`

## English

### What It Is

EasyPlaceFix is a client-side Fabric mod for [Litematica](https://modrinth.com/mod/litematica) that improves Easy Place in multiplayer and makes block placement much more reliable on servers.

It is useful when default Easy Place:
- places blocks with incorrect orientation;
- struggles with interactive or complex blocks;
- performs extra unwanted clicks;
- becomes unreliable under latency or server delay.

### What It Adds

EasyPlaceFix extends normal Easy Place with:
- more accurate block orientation during placement;
- better reliability on multiplayer servers;
- improved handling for blocks that need follow-up interactions;
- fewer duplicate actions and less desync;
- extra settings and hotkeys for fine-tuning behavior;
- built-in logic that makes a separate TickPrediction install unnecessary.

### Features

The mod includes dedicated handling for many directional and interactive blocks, including:
- stairs;
- trapdoors;
- signs and hanging signs;
- shelves and lecterns;
- crafters;
- observers;
- pistons;
- rails;
- skulls, banners, wall-mounted blocks;
- multiface blocks and other blocks that need custom hit results, extra clicks, or careful state handling.

The mod also:
- adds its own `Easy Fix` tab to the Litematica config GUI;
- checks block state before retrying placement;
- uses delayed interactions where needed for better stability;
- handles difficult multiplayer edge cases more safely.

### Settings

The mod adds its own settings to Litematica:
- `enableFix`: enables the EasyPlaceFix replacement logic;
- `loosenMode`: allows looser item matching if the exact item is not found;
- `nbtIgnore`: searches items without strict NBT matching;
- `AllowInteraction`: allows normal interaction with selected containers and interactive blocks;
- `observerDetect`: prevents observer placement in cases where the required target block in the schematic is not yet matched;
- `clientRotationRevert`: restores the client view rotation after the temporary placement rotation.

It also adds separate hotkeys for:
- `loosenMode`;
- `nbtIgnore`;
- `AllowInteraction`.

`loosenMode` uses:
- `config/loosenMode.json`

### Installation

Required:
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Litematica](https://modrinth.com/mod/litematica)
- [MaLiLib](https://modrinth.com/mod/malilib)

Modrinth page:
- https://modrinth.com/mod/easyplacefix-fork

Important:
- this is a client-side mod;
- a separate TickPrediction install is no longer required;
- for multiplayer, use Easy Place protocol: `SLAB_ONLY`.

### Compatibility

- Minecraft `26.1.1`
- Java `25`
- Fabric Loader `0.19.0+`
- Fabric API `0.145.4+26.1.1`
- Litematica `0.27.1`
- MaLiLib `0.28.2`
- Current project mod version: `0.5.9`

### Build

Requirements:
- JDK 25

Build command:
```bash
./gradlew clean build
```

Windows:
```bat
gradlew.bat clean build
```

Output:
- `build/libs/*.jar`

## Credits

Original mod source:
- https://github.com/223225zzzkkk/easyplaceFix
