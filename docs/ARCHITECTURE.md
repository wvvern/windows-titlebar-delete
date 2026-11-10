# Architecture

Windows Titlebar Delete uses ports and adapters. The core has all of the logic and no Minecraft types. Each Minecraft era has a small adapter.

```
 Fabric, Forge, NeoForge 1.16.5 to 26.3         Forge 1.8.9 and 1.12.2
+---------------------------------------+   +---------------------------+
| modern/  mixins, entry points         |   | legacy/  Forge events     |
+-------------------+-------------------+   +-------------+-------------+
                    | Platform, Harness.Host              |
                    v                                     v
+-----------------------------------------------------------------------+
| core/   Titlebar, Styles, config, dev/Harness                         |
+-----------------------------------+-----------------------------------+
                                    | NativeWindow
                                    v
+-----------------------------------------------------------------------+
| core/win32/JnaWindow   user32 through the JNA of the game             |
+-----------------------------------------------------------------------+
```

| Module | Contents |
|--------|----------|
| `core/` | Plain Java 8, no Minecraft, loader, Mixin or LWJGL types. It compiles into every jar. It has unit tests and Checkstyle. |
| `modern/` | 18 Minecraft versions from 1.16.5 to 26.3 with Mojang names. `common/` has the adapter and the mixins. `fabric/`, `forge/` and `neoforge/` have only the entry point. |
| `legacy/` | Forge 1.8.9 and 1.12.2 with MCP names. `src/main` is the adapter, and it is the same source for the two versions. |

One tool chain cannot build both 1.8.9 Forge and 26.x, so there are two builds. Both compile `core/src`.

## Core

- `Styles.hidden(style, keepBorder)` and `Styles.shown(style)` are pure functions. They give the style bits of a window without and with its titlebar. They change only `WS_CAPTION`, `WS_SYSMENU`, `WS_MINIMIZEBOX`, `WS_MAXIMIZEBOX` and `WS_THICKFRAME`. They always keep `WS_MAXIMIZEBOX`, so Win+Up maximizes a window that has the resize border.
- Popup rule: a window with `WS_POPUP` keeps its style. The two functions give the same style back. Such a window is in fullscreen, or a different mod made it borderless.
- `Titlebar` is the only entry point for the adapters: `init`, `tick` and `toggle`.
- `config/` has the immutable `Config` and `ConfigCodec`. The file is `windowstitlebardelete.json` in the config folder that `Platform.configDir` gives. `ConfigCodec.decode` finds each of the two keys in the text of the file with one pattern and does not examine the other text. A key that is not in the text keeps its default. A key with a value that is not `true` or `false` gives one warning and the defaults of the two keys.
- The config has two values. `hidden` (default `true`) is the state of the titlebar. `keepResizeBorder` (default `true`) keeps the thin resize border while the titlebar is hidden. Only the file can change `keepResizeBorder`.
- `Titlebar.init` loads the config before a tick can run. It does not look at the file again, so a change to the file has an effect at the next start.

### Tick and toggle

The config value `hidden` is the only state. `Titlebar` has no other flag.

- `Titlebar.tick`, while `hidden` is `false`: the mod does nothing. It does not read and does not write the style. A game that starts with `hidden` set to `false` never writes a style.
- `Titlebar.tick`, while `hidden` is `true`: the mod reads the style with one `GetWindowLongW` call and writes `Styles.hidden` only when the style is different. The read is necessary because the game puts the titlebar back when it leaves fullscreen (GLFW, SDL) or makes a new window (LWJGL 2), and it gives no signal to the mod.
- `Titlebar.toggle` changes `hidden`, saves the config file at once, and then does the same read and write one time for the new value. For the change to shown, this is the one write of `Styles.shown`. The toggle runs on the client thread, from the key handler.
- The read and write does nothing while the game is in fullscreen or while no window is found. This is correct for the change to shown also. In fullscreen, the game gives the window its titlebar again when it leaves fullscreen. With no window, the mod did not hide a titlebar.

## Window

- `win32/NativeWindow` is the port to the window: `found`, `style` and `setStyle`. `NativeWindow.NONE` is a window that is never found.
- `win32/JnaWindow` finds the window with `EnumWindows`. The window must be visible, belong to the process id of the game, and have the window class `LWJGL` (LWJGL 2), `GLFW30` or a name that starts with `SDL`. The title is not used, because a window of a different program can have the same title.
- `JnaWindow` keeps the handle and searches again only when `IsWindow` is false.
- `JnaWindow.setStyle` calls `SetWindowLongW` and then `SetWindowPos` with `SWP_FRAMECHANGED`, `SWP_NOMOVE` and `SWP_NOSIZE`. The outer size and the position of the window stay the same.
- JNA version rule: each game version has its own JNA, from 3.4.0 on 1.8.9 to 5.x. The core uses only the API that is the same in all of them: direct mapping with `Native.register`, `Pointer` and callbacks. It does not use jna-platform. The core compiles against JNA and does not put it in the jar.
- All native calls are on the client thread, from `Titlebar.tick`, `Titlebar.toggle` and the code that they call.
- On an operating system that is not Windows, `init` logs one line and uses `NativeWindow.NONE`. A native failure logs one warning and also changes to `NativeWindow.NONE`.

## Ports

| Port | The adapter gives |
|------|-------------------|
| `Platform` | A name for logs, the config folder, the log, `isFullscreen` and the harness host. |
| `dev/Harness.Host` | `setFullscreen` and `quit`, only for tests. |

A mixin handler or a loader event has no logic. It calls `Titlebar`, directly or through `ModernHooks`.

## Hooks

| Point | Modern | Legacy |
|-------|--------|--------|
| Start | The entry point of the loader calls `ModernHooks.bootstrap(loader, configDir)`, which calls `Titlebar.init` | `FMLInitializationEvent` |
| Tick | `MinecraftMixin` at the head of `Minecraft.tick` | `TickEvent.ClientTickEvent`, phase `END` |
| Key binding | `OptionsMixin` at the head of `Options.load()` adds the key to `keyMappings` | `ClientRegistry.registerKeyBinding` |
| Key press | The tick hook reads the clicks of the key and calls `Titlebar.toggle` | The same, in the tick event |

- Modern uses two mixins and no loader events, so the same code runs on all loaders and Fabric does not need Fabric API.
- Legacy uses only Forge events, so the legacy jar has no Mixin and no tweaker.
- The name of the key is the plain text `Toggle the titlebar`, because Fabric without Fabric API loads no language files of the mod. The key is in the vanilla category Miscellaneous. The default is F12.

## Modern adapter

`modern/common` (package `wvv.windowstitlebardelete.modern`) is the same for all loaders:

| Class | Function |
|-------|----------|
| `ModernHooks` | `bootstrap(loader, configDir)` starts the core with a `ModernPlatform`. `tick()` calls `Titlebar.toggle` for each click of the key, then `Titlebar.tick`. `withKey(keys)` gives the key array of the game with the toggle key, which it adds only one time. `key()` makes the key at its first use. |
| `ModernPlatform` | The `Platform` port. The name for logs is the loader and the Minecraft version, which the Stonecutter swap `/*$ minecraft*/` writes. The config folder is the folder that the loader entry point gave. |
| `ModernHarnessHost` | The `Harness.Host` port: fullscreen, stop the game. |
| `mixin/OptionsMixin` | `@Inject` at the head of `Options.load()`. It replaces `keyMappings` with `ModernHooks.withKey(keyMappings)` before the game reads `options.txt`, so the game loads, saves and shows the key in Controls. |
| `mixin/MinecraftMixin` | `@Inject` at the head of `Minecraft.tick`. It calls `ModernHooks.tick()` on the client thread, also when no world is open. |

Each loader folder has only the entry point:

| Loader | Entry point |
|--------|-------------|
| Fabric | `TitlebarFabric` (`ClientModInitializer`) |
| Forge | The constructor of `TitlebarForge`, only when `FMLEnvironment.dist` is `Dist.CLIENT` |
| NeoForge | The constructor of `TitlebarNeoForge`. On 1.20.4 it starts the mod only when `FMLEnvironment.dist` is `Dist.CLIENT`. From 1.20.5, `@Mod(dist = Dist.CLIENT)` does this. |

- Each entry point gives the config folder of its loader to `ModernHooks.bootstrap(loader, configDir)`. The adapter does not calculate the folder from the game folder, because Forge and NeoForge can start the mod before the game object has its folder.
- The mixin config is `windowstitlebardelete.mixins.json`. Fabric gets it from `fabric.mod.json`, Forge from `loom.forge.mixinConfig`, and NeoForge from `[[mixins]]` in the mods file.
- Client only: `fabric.mod.json` has `"environment": "client"`. A dedicated Forge or NeoForge server loads the mod, and the mod does nothing. Forge 1.16.5 to 1.20.4 and NeoForge 1.20.4 set a display test, so the server list does not show the mod as a mod that the server does not have. Forge 1.16.5 to 1.18.2 register it in `TitlebarForge`. Later versions read the `displayTest` key of the mods file.
- `fabric.mod.json` asks for a low minimum Fabric Loader version, not for the Fabric Loader version of the build (`fabric_loader_version`).
- The metadata of each jar has the Minecraft range of its node (`minecraft_range_fabric`, `minecraft_range_forge`, `minecraft_range_neoforge`).

### Stonecutter boundaries

These are all `//? if` conditions in `modern/`. `>=X` is the first version that has the new API. `<X` is code that only the versions before X have.

| Boundary | File | API change |
|----------|------|------------|
| `<1.17` | `forge/TitlebarForge` | The display test is `ExtensionPoint.DISPLAYTEST` with a `Pair`, and `IGNORESERVERONLY` is in `net.minecraftforge.fml.network.FMLNetworkConstants` |
| `<1.18` | `forge/TitlebarForge` | The display test is `IExtensionPoint.DisplayTest`, and `IGNORESERVERONLY` is in `net.minecraftforge.fmllegacy.network.FMLNetworkConstants` |
| `<1.19` | `forge/TitlebarForge` | `IGNORESERVERONLY` is in `net.minecraftforge.network.NetworkConstants`, and from 1.19 the mods file sets the display test, so the class registers none |
| `<1.20.5`, `>=1.20.5` | `neoforge/TitlebarNeoForge` | `@Mod(dist = Dist.CLIENT)` replaces the `FMLEnvironment.dist` check |
| `>=1.21.9` | `ModernHooks` | `KeyMapping.Category.MISC` replaces the category text `key.categories.misc` |
| `>=26.3` | `ModernPlatform`, `ModernHarnessHost` | `options.fullscreen()` replaces `Window.isFullscreen()` and `Window.toggleFullScreen()` |

The build scripts have two more boundaries, with `stonecutter.eval`: `>=26.1` in `gradle/shared.gradle` (the game has no obfuscation, so the build uses `loom-no-remap`) and `>=1.20.5` in `neoforge/build.gradle` (`META-INF/neoforge.mods.toml` replaces `META-INF/mods.toml` and `pack.mcmeta`).

## Legacy adapter

`legacy/src/main` (package `wvv.windowstitlebardelete.legacy`) is the same for 1.8.9 and 1.12.2:

| Class | Function |
|-------|----------|
| `TitlebarForge` | The `@Mod` class, with `clientSideOnly = true`. `FMLInitializationEvent` calls `Titlebar.init`, registers the key and registers the class on the FML bus. The client tick event, phase `END`, calls `Titlebar.toggle` for each press of the key, then `Titlebar.tick`. |
| `LegacyPlatform` | The `Platform` port. The config folder is `Loader.instance().getConfigDir()`. |
| `LegacyHarnessHost` | The `Harness.Host` port. |

The build writes the class `BuildInfo` for each version, and the `@Mod` annotation uses it. `BuildInfo.VERSION` is `modVersion`. `BuildInfo.MINECRAFT_RANGE` is the accepted Minecraft version, `[1.8.9]` or `[1.12.2]`.

## Build

| Build | Tools | Java |
|-------|-------|------|
| `modern/` | Stonecutter 0.9, Architectury Loom 1.17 (`loom` to 1.21.11, `loom-no-remap` for 26.x), Gradle 9.8 | Gradle runs on JDK 25. Each node compiles with its `java_version`: 8, 16, 17, 21 or 25. |
| `legacy/` | Essential Loom 0.10, Gradle 8.8 | Gradle runs on JDK 21. The game compiles and runs on Java 8. |

```bash
cd modern && ./gradlew collectJars                  # 37 jars -> modern/build/libs/
cd modern && ./gradlew :fabric:1.21.11:releaseJar   # one jar -> modern/fabric/versions/1.21.11/build/libs/
cd legacy && ./gradlew build                        # 2 jars -> legacy/versions/<mc>/build/libs/
```

- The 37 modern jars are 18 Fabric, 7 Forge and 12 NeoForge. With the 2 legacy jars, a release has 39 jars.
- If a JDK is missing, Gradle downloads it.
- A node is a folder in `modern/versions`. Its `gradle.properties` has the Minecraft version, the Java version, the loader versions and the version ranges for the mod metadata. The node `26.1` builds for Minecraft 26.1.2.
- `modern/settings.gradle` lists the nodes of each loader: Fabric on all, Forge on 1.16.5 to 1.20.4, NeoForge on 1.20.4 to 26.3.
- Stonecutter comments (`//? if >=1.20.5 {` ... `//?} else {` ... `//?}`) mark the version differences. They are only in `modern/`, never in the core.
- The version of the mod is in two places: `mod_version` in `modern/gradle.properties` and `modVersion` in `legacy/gradle.properties`.
- Dev client: `./gradlew :fabric:26.3:runClient` in `modern/`, `./gradlew :1.8.9:runClientDev` in `legacy/`. Do not start it on the visible desktop. Use the harness.
- Releases: see [RELEASING.md](RELEASING.md).

## Add a Minecraft version (modern)

1. Add `modern/versions/<v>/gradle.properties`. For Forge and NeoForge, also add `modern/<loader>/versions/<v>/gradle.properties` with `loom.platform=<loader>`.
2. Add `<v>` to the `versions` list in `modern/settings.gradle` and to the list of each loader branch that has it.
3. Run `./gradlew --configure-on-demand :fabric:<v>:releaseJar` in `modern/`, and the same for the other loaders. The compile errors show the names that changed. Fix each one with a `//? if` branch, and add the boundary to the table above.
4. Run `tools/harness.ps1` for each new target, one time with each commands file. It must print `PASS`.
5. Add a line to the matrix in `.github/workflows/ci.yml` and to the table in `README.md`.

For a legacy version, add `legacy/versions/<mc>/gradle.properties` and the version in `legacy/settings.gradle.kts`.

## Check and test

- Before you push: `./gradlew :core:check` in `modern/` (compile, Checkstyle, tests) and `bash tools/lint.sh` in the repository root. See [CODING_STANDARDS.md](CODING_STANDARDS.md).
- The unit tests use a fake `Platform` and a fake `NativeWindow`. They examine `Styles`, the tick and toggle rules of `Titlebar`, the config codec and the harness commands. `JnaWindowTest` links the natives on Windows and changes no window.

## Harness

- `tools/harness.ps1` starts the dev client of each target on the hidden desktop `WtdHarness` (`tools/run-hidden.ps1`), with no sound. Do not start a visible game for tests.
- A target is `<loader>-<node>`, for example `fabric-1.21.11` or `forge-1.8.9`. There are 39 targets, one for each jar.
- The script starts the Gradle run task with `-Pharness=<dir>`. The build then gives the game `-Dwtd.harness=<dir>`. For a `modern/` target, the script also gives Gradle `--configure-on-demand`.
- The game runs `core/dev/Harness`, which exists only with `-Dwtd.harness=<dir>`. It reads `<dir>/commands.txt` one time, when `Titlebar.init` makes it. Its only state is the queue of commands and the counter of `wait`. The commands are `wait <ticks>`, `toggle`, `style`, `fullscreen <on|off>` and `quit`.
- `Titlebar.tick` calls the harness after the style check. Thus a `style` command logs the style that the window has after the mod examined it in the same tick.
- `toggle` calls `Titlebar.toggle`, the same call that the key makes.
- Each `style` command is a checkpoint with two proofs. Proof 1: the game logs the style that it reads through `JnaWindow`. Proof 2: the script reads the style of the same window from outside the game, with `EnumDesktopWindows` and `GetWindowLong`. The two values must be equal, and the style must agree with the `# expect` line before the command. An `# expect` line gives style bits (`caption off thickframe on`, `popup on`) or the full style (`style 0x16CF0000`). The commands file has `wait 40` after each `style`, so the window does not change while the script reads it.
- The script, not the game, sets the config file of a run. With no `# config-start` line in the commands file, the script deletes the config file before the start. With a `# config-start <json>` line, the script writes that text as the config file. After the game stops, the config file must contain the text of the `# config-end` line.
- A run passes when all checkpoints pass, the harness log has no line that starts with `!`, the game gets to `quit`, and the config file agrees with the `# config-end` line. The result is in `<OutDir>\<target>\result.txt`, with the logs.
- Forge and NeoForge targets of `modern/` get `earlyWindowControl = false` in `config/fml.toml`, so that the game makes its own window as vanilla does.
- The dev game of Forge 1.16.5 stops when it loads the models, because Architectury Loom keeps the SRG names of two calls in the Forge patches. Only with `-Pharness`, `modern/forge/build.gradle` adds `forge/dev-1.16.5` with `TransformationMixin`, which gives the game these two methods. No jar has this mixin.

Each target has two runs, one for each commands file:

| Commands file | Start | Checkpoints | Config file after the run |
|---------------|-------|-------------|---------------------------|
| `titlebar.txt` (default) | No config file | 5: no titlebar with the resize border, toggle to shown, toggle to hidden, the popup style in fullscreen, hidden after fullscreen off | `"hidden": true` |
| `titlebar-legacy.txt` | No config file | 3: the same with no fullscreen step | `"hidden": true` |
| `remember.txt` | `{"hidden": false, "keepResizeBorder": false}` | 2: the style `0x16CF0000` of a window that no program changed, then toggle to hidden with no resize border | `"hidden": true` |

- Legacy targets use `titlebar-legacy.txt` in place of `titlebar.txt`. LWJGL 2 fullscreen can change the display mode of the real monitor, also from a hidden desktop.
- `remember.txt` shows what one run from a new install cannot show: the mod uses the saved `hidden` value at the start and does not change a window with a titlebar, the toggle saves the new value, and `keepResizeBorder` set to `false` removes the resize border.

```powershell
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -OutDir build\harness"
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -Commands remember.txt -OutDir build\harness-remember"
```

- Pass `-Targets` through `pwsh -Command`, so that PowerShell reads the list as an array. With no `-Targets`, the script runs only `forge-1.8.9` and `forge-1.12.2`.
- Keep `-OutDir` relative to the repository root.
- The harness folder of each target is `%TEMP%\wtd-harness\<target>`. Two runs of the same target at the same time must have different `TEMP` folders.

## Known gaps

- The harness toggles through the core. No automated test presses F12.
- The toggle key does nothing while a screen is open: the title screen, a menu, chat or an inventory. The game counts the clicks of a key binding only while no screen is open, and the mod reads only these clicks.
- 32-bit Java uses the stdcall calling convention in `JnaWindow`, which is a different code path from 64-bit Java. No test runs it.
- The harness skips fullscreen on 1.8.9 and 1.12.2, because LWJGL 2 changes the display mode of the real monitor.
- With the titlebar hidden in a window that is not maximized, each change to fullscreen and back can make the window taller by the height of the titlebar. The cause is that the mod keeps the outer size of the window when it removes the titlebar. This is not measured. The 1.0.0 mod did the same.
- The harness runs the dev clients with `earlyWindowControl = false`. The early loading window of Forge and NeoForge in a normal install is the same game window, but no test uses it.
- Quilt uses the Fabric jar and has no harness run.
- A jar declares all patch versions of its Minecraft range, for example 1.21.6 to 1.21.8. Only the build version of each jar has a harness run.
