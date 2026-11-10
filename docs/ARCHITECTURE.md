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
- Popup rule: a window with `WS_POPUP` keeps its style. `Styles.wanted` gives the same style back. Such a window is in fullscreen, or a different mod made it borderless.
- `Titlebar` is the only entry point for the adapters: `init`, `tick`, `toggle`, `apply(config)` and `openSettings`.
- `Titlebar.tick` reconciles. If the game is not in fullscreen and the window is found, it reads the style, calculates the wanted style, and writes it only when it is different.
- There is no state machine and no edge detection. The reconcile gives the correct style after fullscreen, after a new window, and after the game or a loader sets the style again. No code must know that these events occurred.
- `hidden` is the only state. It starts as `hideOnStartup`, and the toggle key changes it for the session. The config file does not store it.
- `config/` has the immutable `Config`, `ConfigCodec` and a small JSON reader. The file is `windowstitlebardelete.json` in the config folder that `Platform.configDir` gives. A missing key keeps its default.
- `ui/SettingsEditor` is the settings screen of all versions. It draws through `Canvas`, and a click on a switch saves the config at once.

## Window

- `win32/NativeWindow` is the port to the window: `found`, `style` and `setStyle`. `NativeWindow.NONE` is a window that is never found.
- `win32/JnaWindow` finds the window with `EnumWindows`. The window must be visible, belong to the process id of the game, and have the window class `LWJGL` (LWJGL 2), `GLFW30` or a name that starts with `SDL`. The title is not used, because a window of a different program can have the same title.
- `JnaWindow` keeps the handle and searches again only when `IsWindow` is false.
- `JnaWindow.setStyle` calls `SetWindowLongW` and then `SetWindowPos` with `SWP_FRAMECHANGED`, `SWP_NOMOVE` and `SWP_NOSIZE`. The outer size and the position of the window stay the same.
- JNA version rule: each game version has its own JNA, from 3.4.0 on 1.8.9 to 5.x. The core uses only the API that is the same in all of them: direct mapping with `Native.register`, `Pointer` and callbacks. It does not use jna-platform. The core compiles against JNA and does not put it in the jar.
- All native calls are on the client thread, from `Titlebar.tick` and the code that it calls.
- On an operating system that is not Windows, `init` logs one line and uses `NativeWindow.NONE`. A native failure logs one warning and also changes to `NativeWindow.NONE`.

## Ports

| Port | The adapter gives |
|------|-------------------|
| `Platform` | A name for logs, the config folder, the log, `isFullscreen`, `openSettings` and the harness host. |
| `ui/Canvas` | `width`, `height`, `fill`, `text` and `textWidth`. The screen class of each adapter implements it and sends draws and clicks to `SettingsEditor`. |
| `dev/Harness.Host` | `setFullscreen`, `screenshot`, `closeScreen` and `quit`, only for tests. |

A mixin handler or a loader event has no logic. It calls `Titlebar`, directly or through `ModernHooks`.

## Hooks

| Point | Modern | Legacy |
|-------|--------|--------|
| Start | The entry point of the loader calls `ModernHooks.bootstrap(loader, configDir)`, which calls `Titlebar.init` | `FMLInitializationEvent` |
| Tick | `MinecraftMixin` at the head of `Minecraft.tick` | `TickEvent.ClientTickEvent`, phase `END` |
| Key binding | `OptionsMixin` at the head of `Options.load()` adds the key to `keyMappings` | `ClientRegistry.registerKeyBinding` |
| Key press | The tick hook reads the clicks of the key and calls `Titlebar.toggle` | The same, in the tick event |
| Settings screen | Mod Menu (Fabric), the Config button of the mod list (Forge, NeoForge) | `GuiFactory` for the Config button of the mod list |

- Modern uses two mixins and no loader events, so the same code runs on all loaders and Fabric does not need Fabric API.
- Legacy uses only Forge events, so the legacy jar has no Mixin and no tweaker.
- The name of the key is the plain text `Toggle the titlebar`, because Fabric without Fabric API loads no language files of the mod. The key is in the vanilla category Miscellaneous. The default is F12.

## Modern adapter

`modern/common` (package `wvv.windowstitlebardelete.modern`) is the same for all loaders:

| Class | Function |
|-------|----------|
| `ModernHooks` | `bootstrap(loader, configDir)` starts the core with a `ModernPlatform`. `tick()` calls `Titlebar.toggle` for each click of the key, then `Titlebar.tick`. `withKey(keys)` gives the key array of the game with the toggle key, which it adds only one time. `key()` makes the key at its first use. |
| `ModernPlatform` | The `Platform` port. The name for logs is the loader and the Minecraft version, which the Stonecutter swap `/*$ minecraft*/` writes. The config folder is the folder that the loader entry point gave. |
| `SettingsScreen` | A `Screen` that implements `Canvas`. It draws the `SettingsEditor` each frame and sends a click of any mouse button to it. It goes back to the parent screen after Done or Escape. |
| `ModernHarnessHost` | The `Harness.Host` port: fullscreen, screenshot of the main render target, close the screen, stop the game. |
| `mixin/OptionsMixin` | `@Inject` at the head of `Options.load()`. It replaces `keyMappings` with `ModernHooks.withKey(keyMappings)` before the game reads `options.txt`, so the game loads, saves and shows the key in Controls. |
| `mixin/MinecraftMixin` | `@Inject` at the head of `Minecraft.tick`. It calls `ModernHooks.tick()` on the client thread, also on the title screen. |

Each loader folder has the entry point and the config button:

| Loader | Entry point | Config button |
|--------|-------------|---------------|
| Fabric | `TitlebarFabric` (`ClientModInitializer`) | `ModMenuIntegration` (`ModMenuApi`), the `modmenu` entry point in `fabric.mod.json`. The game loads it only when Mod Menu is installed. Mod Menu is a compile-only dependency. |
| Forge | The constructor of `TitlebarForge`, only when `FMLEnvironment.dist` is `Dist.CLIENT` | `ExtensionPoint.CONFIGGUIFACTORY` on 1.16.5, `ConfigGuiHandler.ConfigGuiFactory` on 1.17.1 and 1.18.2, `ConfigScreenHandler.ConfigScreenFactory` from 1.19 |
| NeoForge | The constructor of `TitlebarNeoForge`. On 1.20.4 it starts the mod only when `FMLEnvironment.dist` is `Dist.CLIENT`. From 1.20.5, `@Mod(dist = Dist.CLIENT)` does this. | `ConfigScreenHandler.ConfigScreenFactory` on 1.20.4, `IConfigScreenFactory` from 1.20.5 |

- Each entry point gives the config folder of its loader to `ModernHooks.bootstrap(loader, configDir)`. The adapter does not calculate the folder from the game folder, because Forge and NeoForge can start the mod before the game object has its folder.
- Each config button makes `new SettingsScreen(parent)`.
- The mixin config is `windowstitlebardelete.mixins.json`. Fabric gets it from `fabric.mod.json`, Forge from `loom.forge.mixinConfig`, and NeoForge from `[[mixins]]` in the mods file.
- Client only: `fabric.mod.json` has `"environment": "client"`. A dedicated Forge or NeoForge server loads the mod, and the mod does nothing. Forge 1.16.5 to 1.20.4 and NeoForge 1.20.4 set `displayTest`, so the server list does not show the mod as a mod that the server does not have.
- `fabric.mod.json` asks for a low minimum Fabric Loader version, not for the Fabric Loader version of the build (`fabric_loader_version`).
- The metadata of each jar has the Minecraft range of its node (`minecraft_range_fabric`, `minecraft_range_forge`, `minecraft_range_neoforge`).

### Stonecutter boundaries

These are all `//? if` conditions in `modern/`. `>=X` is the first version that has the new API.

| Boundary | File | API change |
|----------|------|------------|
| `>=1.17` | `ModernHarnessHost` | `Screenshot.takeScreenshot(target)` has no width and height parameters |
| `>=1.17` | `forge/TitlebarForge` | `ConfigGuiHandler.ConfigGuiFactory` in `net.minecraftforge.fmlclient` replaces `ExtensionPoint.CONFIGGUIFACTORY` |
| `>=1.18` | `forge/TitlebarForge` | `ConfigGuiHandler` is in `net.minecraftforge.client` |
| `>=1.19` | `forge/TitlebarForge` | `ConfigScreenHandler.ConfigScreenFactory` replaces `ConfigGuiHandler.ConfigGuiFactory` |
| `>=1.20` | `SettingsScreen` | `GuiGraphics` replaces `PoseStack` as the draw target of `render`, `fill` and the text |
| `>=1.20.2` | `SettingsScreen` | `super.render` draws the background, where older versions call `renderBackground` |
| `>=1.20.5` | `neoforge/TitlebarNeoForge` | `@Mod(dist = Dist.CLIENT)` and `IConfigScreenFactory` replace the `FMLEnvironment.dist` check and `ConfigScreenHandler.ConfigScreenFactory` |
| `>=1.21.5` | `ModernHarnessHost` | `Screenshot.takeScreenshot` gives the image to a callback |
| `>=1.21.9` | `ModernHooks` | `KeyMapping.Category.MISC` replaces the category text `key.categories.misc` |
| `>=1.21.9` | `SettingsScreen` | `mouseClicked(MouseButtonEvent, boolean)` replaces `mouseClicked(double, double, int)` |
| `>=26.1` | `SettingsScreen` | `extractRenderState` and `GuiGraphicsExtractor` replace `render` and `GuiGraphics`, and `text` replaces `drawString` |
| `>=26.2` | `ModernPlatform` | `Minecraft.gui.screen()` and `gui.setScreen` replace `Minecraft.screen` and `Minecraft.setScreen` |
| `>=26.2` | `ModernHarnessHost` | `gameRenderer.mainRenderTarget()` replaces `Minecraft.getMainRenderTarget()` |
| `>=26.3` | `ModernPlatform`, `ModernHarnessHost` | `options.fullscreen()` replaces `Window.isFullscreen()` and `Window.toggleFullScreen()` |

The build scripts have two more boundaries, with `stonecutter.eval`: `>=26.1` in `gradle/shared.gradle` (the game has no obfuscation, so the build uses `loom-no-remap`) and `>=1.20.5` in `neoforge/build.gradle` (`META-INF/neoforge.mods.toml` replaces `META-INF/mods.toml` and `pack.mcmeta`).

## Legacy adapter

`legacy/src/main` (package `wvv.windowstitlebardelete.legacy`) is the same for 1.8.9 and 1.12.2:

| Class | Function |
|-------|----------|
| `TitlebarForge` | The `@Mod` class, with `clientSideOnly = true`. `FMLInitializationEvent` calls `Titlebar.init`, registers the key and registers the class on the FML bus. The client tick event, phase `END`, calls `Titlebar.toggle` for each press of the key, then `Titlebar.tick`. |
| `LegacyPlatform` | The `Platform` port. The config folder is `Loader.instance().getConfigDir()`. |
| `GuiSettings` | A `GuiScreen` that implements `Canvas`. It sends a click of the left mouse button to `SettingsEditor`. |
| `LegacyHarnessHost` | The `Harness.Host` port. |

`legacy/src/v1_8_9` and `legacy/src/v1_12_2` have the classes that are different:

| Class | 1.8.9 | 1.12.2 |
|-------|-------|--------|
| `GuiFactory` (`IModGuiFactory`) | `mainConfigGuiClass` gives `GuiSettings.class` | `hasConfigGui` and `createConfigGui` |
| `McCompat` | `fontRendererObj`, accepted version `[1.8.9]` | `fontRenderer`, accepted version `[1.12.2]` |

The build writes `BuildInfo.VERSION` from `modVersion`, and the `@Mod` annotation uses it.

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
3. Run `./gradlew :fabric:<v>:releaseJar` in `modern/`, and the same for the other loaders. The compile errors show the names that changed. Fix each one with a `//? if` branch, and add the boundary to the table above.
4. Run `tools/harness.ps1` for each new target. It must print `PASS`. Add the result to [TESTED.md](TESTED.md).
5. Add a line to the matrix in `.github/workflows/ci.yml` and to the table in `README.md`.

For a legacy version, add `legacy/versions/<mc>/gradle.properties`, the folder `legacy/src/v<mc>` with `McCompat` and `GuiFactory`, and the version in `legacy/settings.gradle.kts`.

## Check and test

- Before you push: `./gradlew :core:check` in `modern/` (compile, Checkstyle, tests) and `bash tools/lint.sh` in the repository root. See [CODING_STANDARDS.md](CODING_STANDARDS.md).
- The unit tests use a fake `Platform` and a fake `NativeWindow`. They examine `Styles`, the reconcile of `Titlebar`, the config codec, the settings screen and the harness commands. `JnaWindowTest` links the natives on Windows and changes no window.

## Harness

- `tools/harness.ps1` starts the dev client of each target on the hidden desktop `WtdHarness` (`tools/run-hidden.ps1`), with no sound and with the default config. Do not start a visible game for tests.
- A target is `<loader>-<node>`, for example `fabric-1.21.11` or `forge-1.8.9`. There are 39 targets, one for each jar.
- The script starts the Gradle run task with `-Pharness=<dir>`. The build then gives the game `-Dwtd.harness=<dir>` and a window of 1280 x 720.
- The game runs `core/dev/Harness`, which exists only with `-Dwtd.harness=<dir>`. `Titlebar.tick` calls it before the reconcile. It waits for the game window, then reads the commands of `tools/harness/titlebar.txt`: `wait <ticks>`, `toggle`, `set <option> <on|off>`, `style`, `settings`, `click <label>`, `close`, `fullscreen <on|off>`, `shot <name>` and `quit`.
- The commands go through the core: `toggle` calls `Titlebar.toggle`, `set` calls `Titlebar.apply`, `settings` calls `Platform.openSettings`, and `click` calls `SettingsEditor.click` with the label of the control.
- Each `style` command is a checkpoint with two proofs. Proof 1: the game logs the style that it reads through `JnaWindow`, one tick after the command. Proof 2: the script reads the style of the same window from outside the game, with `EnumDesktopWindows` and `GetWindowLong`. The two values must be equal, and the bits must agree with the `# expect` line before the command.
- `titlebar.txt` has 6 checkpoints: start, key off, key on, after fullscreen on and off, no resize border, and after two clicks in the settings screen.
- A target passes when all checkpoints pass, the harness log has no line that starts with `!`, and the game gets to `quit`. The result is in `<OutDir>\<target>\result.txt`, with the log and the screenshots of the settings screen. [TESTED.md](TESTED.md) has the results of the last full run.
- Legacy targets use `tools/harness/titlebar-legacy.txt`, which has no fullscreen step and 5 checkpoints. LWJGL 2 fullscreen can change the display mode of the real monitor, also from a hidden desktop.
- Forge and NeoForge targets of `modern/` get `earlyWindowControl = false` in `config/fml.toml`, so that the game makes its own window as vanilla does.
- The dev game of Forge 1.16.5 stops when it loads the models, because Architectury Loom keeps the SRG names of two calls in the Forge patches. Only with `-Pharness`, `modern/forge/build.gradle` adds `forge/dev-1.16.5` with `TransformationMixin`, which gives the game these two methods. No jar has this mixin.

```powershell
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -OutDir build\harness"
```

- Pass `-Targets` through `pwsh -Command`, so that PowerShell reads the list as an array. With no `-Targets`, the script runs only `forge-1.8.9` and `forge-1.12.2`.
- Keep `-OutDir` relative to the repository root.
- The harness folder of each target is `%TEMP%\wtd-harness\<target>`. Two runs of the same target at the same time must have different `TEMP` folders.

## Known gaps

- The harness toggles through the core and opens the settings screen with its own command. No automated test presses F12, clicks with the real mouse, or uses the Config button of Mod Menu or of the mod list.
- The harness skips fullscreen on 1.8.9 and 1.12.2, because LWJGL 2 changes the display mode of the real monitor.
- With the titlebar hidden in a window that is not maximized, each change to fullscreen and back can make the window taller by the height of the titlebar. The cause is that the mod keeps the outer size of the window when it removes the titlebar. This is not measured. The 1.0.0 mod did the same.
- The harness runs the dev clients with `earlyWindowControl = false`. The early loading window of Forge and NeoForge in a normal install is the same game window, but no test uses it.
- Quilt uses the Fabric jar and has no harness run.
- A jar declares all patch versions of its Minecraft range, for example 1.21.6 to 1.21.8. Only the build version of each jar has a harness run.
