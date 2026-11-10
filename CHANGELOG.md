# Changelog

## 2.0.0

The mod is new code, with jars for Minecraft 1.8.9 to 26.3.

- feat: Add jars for Fabric, Forge and NeoForge on Minecraft 1.12.2 and 1.16.5 to 26.3
- feat: Add the key Toggle the titlebar to the Controls screen of all versions, with F12 as the default
- feat: Keep the titlebar hidden or shown as you left it when you start the game again
- feat: Add "keepResizeBorder" to the config file, which you set to false to get a window with no frame
- fix: Keep the titlebar hidden after you leave fullscreen on 1.21.4 when you are not in a world
- fix: Change only the game window, and not a window of a different program with Minecraft in its title
- compat: Remove the need for Fabric API, so Fabric needs only Fabric Loader
- compat: Show the Fabric jars for Minecraft 1.18.2 and later as Quilt jars on Modrinth, with no test on Quilt
- config: Hide the titlebar in a new install, also on 1.21.4, so press the key to show it
- config: Set the key again on 1.21.4 if you changed it, because the key has a new name
