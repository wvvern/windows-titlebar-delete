# Changelog

## 2.0.0

The mod is new code, with jars for Minecraft 1.8.9 to 26.3.

- feat: Add jars for Fabric, Forge and NeoForge on Minecraft 1.12.2 and 1.16.5 to 26.3
- feat: Add the key Toggle the titlebar to the Controls screen of all versions, with F12 as the default
- feat: Add a settings screen that opens from Mod Menu and from the Config button of the mod list
- feat: Add the option Hide the titlebar at startup, which is on by default
- feat: Add the option Keep the resize border, which you can turn off to get a window with no frame
- fix: Keep the titlebar hidden after you leave fullscreen on 1.21.4 when you are not in a world
- fix: Change only the game window, and not a window of a different program with Minecraft in its title
- compat: Remove the need for Fabric API, so Fabric needs only Fabric Loader
- compat: Show the Fabric jars for Minecraft 1.18.2 and later as Quilt jars on Modrinth, with no test on Quilt
- config: Hide the titlebar at startup on 1.21.4 also, so turn off the option to start with a titlebar
- config: Set the key again on 1.21.4 if you changed it, because the key has a new name
