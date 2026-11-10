# Windows Titlebar Delete

Windows Titlebar Delete is a client-side Minecraft mod that hides the Windows titlebar of the game window.

Press Win+Up to maximize the window. The game then fills the screen as in fullscreen, but it stays a window. This is known as a borderless window.

The mod works only on Windows. On other operating systems it writes one line to the log and does nothing.

## Install

Get the jar for your Minecraft version and loader from [Modrinth](https://modrinth.com/mod/windows-titlebar-delete) or from the [GitHub releases](https://github.com/wvvern/windows-titlebar-delete/releases). Put it in your `mods/` folder.

- Fabric needs only Fabric Loader. It does not need Fabric API.
- Quilt has no jar of its own. Use the Fabric jar, which Modrinth lists for Quilt on Minecraft 1.18.2 and later. The mod is not tested on Quilt.

| Minecraft          | Fabric | Quilt | Forge | NeoForge |
|--------------------|:------:|:-----:|:-----:|:--------:|
| 1.8.9              |        |       | Yes   |          |
| 1.12.2             |        |       | Yes   |          |
| 1.16.5             | Yes    |       | Yes   |          |
| 1.17.1             | Yes    |       | Yes   |          |
| 1.18.2             | Yes    | Yes   | Yes   |          |
| 1.19 to 1.19.2     | Yes    | Yes   | Yes   |          |
| 1.19.4             | Yes    | Yes   | Yes   |          |
| 1.20 and 1.20.1    | Yes    | Yes   | Yes   |          |
| 1.20.3 and 1.20.4  | Yes    | Yes   | Yes   | Yes      |
| 1.20.5 and 1.20.6  | Yes    | Yes   |       | Yes      |
| 1.21 and 1.21.1    | Yes    | Yes   |       | Yes      |
| 1.21.2 and 1.21.3  | Yes    | Yes   |       | Yes      |
| 1.21.4             | Yes    | Yes   |       | Yes      |
| 1.21.5             | Yes    | Yes   |       | Yes      |
| 1.21.6 to 1.21.8   | Yes    | Yes   |       | Yes      |
| 1.21.9 and 1.21.10 | Yes    | Yes   |       | Yes      |
| 1.21.11            | Yes    | Yes   |       | Yes      |
| 26.1 to 26.1.2     | Yes    | Yes   |       | Yes      |
| 26.2               | Yes    | Yes   |       | Yes      |
| 26.3               | Yes    | Yes   |       | Yes      |

Each line of the table has its own jar for each loader. The name of the jar shows the loader and the last Minecraft version of the line, for example `windowstitlebardelete-fabric-2.0.0+1.21.8.jar`.

A `Yes` in the Quilt column shows that Modrinth lists the Fabric jar for Quilt. It does not show a test.

The mod is tested with the last Minecraft version of each line. See [docs/TESTED.md](docs/TESTED.md).

## Key

The key `Toggle the titlebar` shows or hides the titlebar. The default key is F12. You can change it in the Controls screen, in the category Miscellaneous.

The mod remembers the state of the titlebar. When you start the game again, the titlebar is hidden or shown as you left it. A new install starts with no titlebar.

While the titlebar shows, the mod does not change the game window.

## Config file

The config file is `config/windowstitlebardelete.json`. The mod writes the file the first time that the game starts with the mod.

```json
{
  "hidden": true,
  "keepResizeBorder": true
}
```

| Key | Default | Effect |
|-----|:-------:|--------|
| `hidden` | `true` | `true` hides the titlebar. The mod writes this value each time you press the key. |
| `keepResizeBorder` | `true` | With `true`, a window with no titlebar keeps its thin border, so you can change its size with the mouse and maximize it with Win+Up. With `false`, the window has no frame. Only the file can change this value. |

The mod reads the file only when the game starts. Thus, close the game before you edit the file. If the file is not valid, the mod uses the default values and writes a warning to the log.

## Known limits

- The mod works only on Windows.
- The mod does not change the window while the game is in fullscreen. If you press the key in fullscreen, you see the result when you go back to the window.
- If the titlebar is hidden and the window is not maximized, the window can become taller each time you go to fullscreen and back.
- If a different mod makes the window borderless, that mod keeps control of the window. Windows Titlebar Delete does not change such a window.

## Build from source

```bash
cd modern && ./gradlew collectJars   # Fabric, Forge and NeoForge jars for 1.16.5 to 26.3 -> modern/build/libs/
cd legacy && ./gradlew build         # Forge 1.8.9 and 1.12.2 jars -> legacy/versions/<mc>/build/libs/
```

Gradle downloads the JDKs that it needs. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the design and [docs/RELEASING.md](docs/RELEASING.md) for releases.

## License

[Apache-2.0](LICENSE)
