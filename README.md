# Windows Titlebar Delete

Windows Titlebar Delete is a client-side Minecraft mod that hides the Windows titlebar of the game window.

Press Win+Up to maximize the window. The game then fills the screen as in fullscreen, but it stays a window. This is known as a borderless window.

The mod works only on Windows. On other operating systems it writes one line to the log and does nothing.

## Install

Get the jar for your Minecraft version and loader from [Modrinth](https://modrinth.com/mod/windows-titlebar-delete) or from the [GitHub releases](https://github.com/wvvern/windows-titlebar-delete/releases). Put it in your `mods/` folder.

- Fabric needs only Fabric Loader, not Fabric API.
- Quilt uses the Fabric jar.
- [Mod Menu](https://modrinth.com/mod/modmenu) is optional on Fabric and Quilt. It adds the button that opens the settings screen.

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

Each line of the table has its own jar. The name of the jar shows the loader and the last Minecraft version of the line, for example `windowstitlebardelete-fabric-2.0.0+1.21.8.jar`.

## Key

The key `Toggle the titlebar` shows or hides the titlebar. The default key is F12. You can change it in the Controls screen, in the category Miscellaneous.

The key changes the titlebar only until you close the game. It does not change the settings.

## Settings

Open the settings screen from Mod Menu (Fabric, Quilt) or with the Config button of the mod list (Forge, NeoForge).

| Option | Default | Effect |
|--------|:-------:|--------|
| Hide the titlebar at startup | On | The game starts with no titlebar. If the option is off, the titlebar stays until you press the key. |
| Keep the resize border | On | The window keeps its thin border, so you can change its size with the mouse and maximize it with Win+Up. If the option is off, the window has no frame. |

## Config file

The settings are in `config/windowstitlebardelete.json`. The mod writes the file at the first start.

```json
{
  "hideOnStartup": true,
  "keepResizeBorder": true
}
```

The mod reads the file when the game starts. If the file is not valid, the mod uses the default settings and writes a warning to the log.

## Build from source

```bash
cd modern && ./gradlew collectJars   # Fabric, Forge and NeoForge jars for 1.16.5 to 26.3 -> modern/build/libs/
cd legacy && ./gradlew build         # Forge 1.8.9 and 1.12.2 jars -> legacy/versions/<mc>/build/libs/
```

Gradle downloads the JDKs that it needs. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the design and [docs/RELEASING.md](docs/RELEASING.md) for releases.

## License

[Apache-2.0](LICENSE)
