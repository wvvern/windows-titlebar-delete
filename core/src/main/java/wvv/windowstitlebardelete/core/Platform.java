package wvv.windowstitlebardelete.core;

import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;

/** The port each loader and version adapter implements, which is the only way the core reaches the game. */
public interface Platform {
    /** A name for logs, for example "Fabric 1.21.11". */
    String describe();

    /** The game's config folder, which holds the config file of the mod. */
    File configDir();

    void info(String message);

    void warn(String message, Throwable error);

    /** True while the game shows its window in fullscreen, where the mod does not change the window. */
    boolean isFullscreen();

    /** The development test harness host, which the core uses only when {@code -Dwtd.harness=<dir>} is set. */
    Harness.Host harnessHost();
}
