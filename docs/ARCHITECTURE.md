# Architecture

Windows Titlebar Delete uses ports and adapters. The core has all of the logic and no Minecraft types. Each Minecraft era has a small adapter.

```
 Fabric, Forge, NeoForge 1.16.5 to 26.3         Forge 1.8.9 and 1.12.2
+---------------------------------------+   +---------------------------+
| modern/  mixins, screen, entry points |   | legacy/  Forge events,    |
|                                       |   |          screen           |
+-------------------+-------------------+   +-------------+-------------+
                    | Platform, Canvas, Harness.Host      |
                    v                                     v
+-----------------------------------------------------------------------+
| core/   Titlebar, Styles, config, ui/SettingsEditor, dev/Harness      |
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
| `modern/` | 18 Minecraft versions from 1.16.5 to 26.3 with Mojang names. `common/` has the adapter and the mixins. `fabric/`, `forge/` and `neoforge/` have only the entry point and the config button. |
| `legacy/` | Forge 1.8.9 and 1.12.2 with MCP names. `src/main` is the adapter for the two versions. `src/v1_8_9` and `src/v1_12_2` have the two classes that are different. |

One tool chain cannot build both 1.8.9 Forge and 26.x, so there are two builds. Both compile `core/src`.

## Core

- `Styles.wanted(style, hidden, keepBorder)` is a pure function. It gives the style bits that the window must have. It changes only `WS_CAPTION`, `WS_SYSMENU`, `WS_MINIMIZEBOX`, `WS_MAXIMIZEBOX` and `WS_THICKFRAME`. It always keeps `WS_MAXIMIZEBOX`, so Win+Up maximizes a window that has the resize border.
- `Titlebar` is the only entry point for the adapters: `init`, `tick`, `toggle`, `apply(config)` and `openSettings`.
- `Titlebar.tick` reconciles. If the game is not in fullscreen and the window is found, it reads the style, calculates the wanted style, and writes it only when it is different.
- There is no state machine and no edge detection. The reconcile gives the correct style after fullscreen, after a new window, and after the game or a loader sets the style again. No code must know that these events occurred.
- `hidden` is the only state. It starts as `hideOnStartup`, and the toggle key changes it for the session. The config file does not store it.
- `config/` has the immutable `Config`, `ConfigCodec` and a small JSON reader. The file is `config/windowstitlebardelete.json`. A missing key keeps its default.
- `ui/SettingsEditor` is the settings screen of all versions. It draws through `Canvas`, and a click on a switch saves the config at once.

## Window

- `win32/NativeWindow` is the port to the window: `found`, `style` and `setStyle`. `NativeWindow.NONE` is a window that is never found.
- `win32/JnaWindow` finds the window with `EnumWindows`. The window must be visible, belong to the process id of the game, and have the window class `LWJGL` (LWJGL 2), `GLFW30` or a name that starts with `SDL`. The title is not used, because a window of a different program can have the same title.
- `JnaWindow` keeps the handle and searches again only when `IsWindow` is false.
- JNA version rule: each game version has its own JNA, from 3.4.0 on 1.8.9 to 5.x. The core uses only the API that is the same in all of them: direct mapping with `Native.register`, `Pointer` and callbacks. It does not use jna-platform. The core compiles against JNA and does not put it in the jar.
- All native calls are on the client thread, from `Titlebar.tick` and the code that it calls.
- On an operating system that is not Windows, `init` logs one line and uses `NativeWindow.NONE`. A native failure logs one warning and also changes to `NativeWindow.NONE`.

## Ports

| Port | The adapter gives |
|------|-------------------|
| `Platform` | A name for logs, the config folder, the log, `isFullscreen`, `openSettings` and the harness host. |
| `ui/Canvas` | `width`, `height`, `fill`, `text` and `textWidth`. The screen class of each adapter implements it and sends draws and clicks to `SettingsEditor`. |
| `dev/Harness.Host` | `setFullscreen`, `screenshot`, `closeScreen` and `quit`, only for tests. |

A mixin handler or a loader event has no logic. It calls `Titlebar`.

## Hooks

| Point | Modern | Legacy |
|-------|--------|--------|
| Start | The entry point of the loader calls `Titlebar.init` on the client | `FMLInitializationEvent` |
| Tick | Mixin into `Minecraft.tick` | `TickEvent.ClientTickEvent`, phase `END` |
| Key binding | Mixin into `Options` that adds the key to the key list | `ClientRegistry.registerKeyBinding` |
| Key press | The tick hook reads the clicks of the key and calls `Titlebar.toggle` | The same, in the tick event |
| Settings screen | Mod Menu (Fabric, Quilt), the Config button of the mod list (Forge, NeoForge) | `GuiFactory` for the Config button of the mod list |

- Modern uses two mixins and no loader events, so the same code runs on all loaders and Fabric does not need Fabric API. `ModernHooks` gets the calls of the mixins.
- Legacy uses only Forge events, so the legacy jar has no Mixin and no tweaker.
- The name of the key is the plain text `Toggle the titlebar`, because Fabric without Fabric API loads no language files of the mod. The key is in the vanilla category Miscellaneous.

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

- If a JDK is missing, Gradle downloads it.
- A node is a folder in `modern/versions`. Its `gradle.properties` has the Minecraft version, the Java version, the loader versions and the version ranges for the mod metadata. The node `26.1` builds for Minecraft 26.1.2.
- `modern/settings.gradle` lists the nodes of each loader: Fabric on all, Forge on 1.16.5 to 1.20.4, NeoForge on 1.20.4 to 26.3.
- Stonecutter comments (`//? if >=1.20.5 {` ... `//?} else {` ... `//?}`) mark the version differences. They are only in `modern/`, never in the core.
- Dev client: `./gradlew :fabric:26.3:runClient` in `modern/`, `./gradlew :1.8.9:runClientDev` in `legacy/`. Do not start it on the visible desktop. Use the harness.
- Releases: see [RELEASING.md](RELEASING.md).

## Add a Minecraft version (modern)

1. Add `modern/versions/<v>/gradle.properties`. For Forge and NeoForge, also add `modern/<loader>/versions/<v>/gradle.properties` with `loom.platform=<loader>`.
2. Add `<v>` to the `versions` list in `modern/settings.gradle` and to the list of each loader branch that has it.
3. Run `./gradlew :fabric:<v>:releaseJar` in `modern/`, and the same for the other loaders. The compile errors show the names that changed. Fix each one with a `//? if` branch.
4. Run `tools/harness.ps1` for each new target. It must print `PASS`.
5. Add a line to the matrix in `.github/workflows/ci.yml` and to the table in `README.md`.

For a legacy version, add `legacy/versions/<mc>/gradle.properties`, the folder `legacy/src/v<mc>` with `McCompat` and `GuiFactory`, and the version in `legacy/settings.gradle.kts`.

## Check and test

- Before you push: `./gradlew :core:check` in `modern/` (compile, Checkstyle, tests) and `bash tools/lint.sh` in the repository root. See [CODING_STANDARDS.md](CODING_STANDARDS.md).
- The unit tests use a fake `Platform` and a fake `NativeWindow`. They examine `Styles`, the reconcile of `Titlebar`, the config codec, the settings screen and the harness commands. `JnaWindowTest` links the natives on Windows and changes no window.
- `tools/harness.ps1` starts the dev client of each target on the hidden desktop `WtdHarness` (`tools/run-hidden.ps1`), with no sound and with the default config. Do not start a visible game for tests.
- The game runs `core/dev/Harness`, which exists only with `-Dwtd.harness=<dir>`. It reads the commands of `tools/harness/titlebar.txt`: `wait <ticks>`, `toggle`, `set <option> <on|off>`, `style`, `settings`, `click <label>`, `close`, `fullscreen <on|off>`, `shot <name>` and `quit`.
- Each `style` command is a checkpoint with two proofs. Proof 1: the game logs the style that it reads through `JnaWindow`. Proof 2: the script reads the style of the same window from outside the game, with `EnumDesktopWindows` and `GetWindowLong`. The two values must be equal, and the bits must agree with the `# expect` line before the command.
- A target passes when all checkpoints pass, the log has no error line, and the game gets to `quit`. The result is in `<OutDir>\<target>\result.txt`, with the log and the screenshots of the settings screen.
- Legacy targets use `tools/harness/titlebar-legacy.txt`, which has no fullscreen step. LWJGL 2 fullscreen can change the display mode of the real monitor, also from a hidden desktop.
- Forge and NeoForge targets of `modern/` get `earlyWindowControl = false` in `config/fml.toml`, so that the game makes its own window as vanilla does.

```powershell
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -OutDir build\harness"
```

- Pass `-Targets` through `pwsh -Command`, so that PowerShell reads the list as an array.
- Keep `-OutDir` relative to the repository root.
- The harness folder of each target is `%TEMP%\wtd-harness\<target>`. Two runs of the same target at the same time must have different `TEMP` folders.
