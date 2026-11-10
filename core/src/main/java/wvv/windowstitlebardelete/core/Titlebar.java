package wvv.windowstitlebardelete.core;

import wvv.windowstitlebardelete.core.config.Config;
import wvv.windowstitlebardelete.core.config.ConfigCodec;
import wvv.windowstitlebardelete.core.dev.Harness;
import wvv.windowstitlebardelete.core.win32.JnaWindow;
import wvv.windowstitlebardelete.core.win32.NativeWindow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** The one entry point of the core for the adapters, which hides the titlebar of the game window while the config says so. */
public final class Titlebar {
    public static final String MOD_ID = "windowstitlebardelete";
    public static final String MOD_NAME = "Windows Titlebar Delete";
    /** The name of the key binding, which is plain text because Fabric without Fabric API loads no language files. */
    public static final String KEY_NAME = "Toggle the titlebar";
    static final String CONFIG_FILE = "windowstitlebardelete.json";

    private static Platform platform;
    private static File configFile;
    private static volatile Config config = Config.DEFAULTS;
    private static NativeWindow window = NativeWindow.NONE;
    private static Harness harness;

    private Titlebar() {}

    /** Starts the core with the window of this process on Windows, and with no window on other operating systems. */
    public static synchronized void init(Platform p) {
        init(p, window(p, System.getProperty("os.name", "")));
    }

    /** Starts the core with a given window, which loads the config before a tick can change the window. */
    public static synchronized void init(Platform p, NativeWindow w) {
        platform = p;
        configFile = new File(p.configDir(), CONFIG_FILE);
        config = load();
        window = w;
        harness = Harness.fromSystemProperty(p);
    }

    /** The window for an operating system, where all but Windows get a window that is never found. */
    static NativeWindow window(Platform p, String osName) {
        if (!osName.startsWith("Windows")) {
            p.info(MOD_NAME + " does nothing on " + osName + ", because it changes only windows on Windows.");
            return NativeWindow.NONE;
        }
        try {
            return new JnaWindow();
        } catch (Throwable e) {
            nativeFailed(p, e);
            return NativeWindow.NONE;
        }
    }

    /** Hides the titlebar again if the game put it back, then runs the harness commands; call it on the client thread. */
    public static synchronized void tick() {
        if (platform == null) return;
        // The game gives no signal when it leaves fullscreen or makes a new window, so each tick reads the style while the titlebar is hidden.
        if (config.hidden()) writeStyle();
        // The harness runs last, so that its style command logs the style that this tick gave the window.
        if (harness != null) harness.tick();
    }

    /** Shows or hides the titlebar and saves that state for the next start; call it on the client thread. */
    public static synchronized void toggle() {
        if (platform == null) return;
        config = config.withHidden(!config.hidden());
        save(config);
        // Only this call writes the style with a titlebar, so the mod does not touch the window while the titlebar shows.
        writeStyle();
    }

    /** Writes the style that the config wants when the game is not in fullscreen and the window is found and has a different style. */
    private static void writeStyle() {
        if (platform.isFullscreen()) return;
        try {
            if (!window.found()) return;
            int style = window.style();
            int wanted = config.hidden() ? Styles.hidden(style, config.keepResizeBorder()) : Styles.shown(style);
            if (wanted != style) window.setStyle(wanted);
        } catch (Throwable e) {
            nativeFailed(platform, e);
            window = NativeWindow.NONE;
        }
    }

    /** The style of the game window, or null when no window is found. */
    public static synchronized Integer windowStyle() {
        try {
            return window.found() ? Integer.valueOf(window.style()) : null;
        } catch (Throwable e) {
            nativeFailed(platform, e);
            window = NativeWindow.NONE;
            return null;
        }
    }

    private static void nativeFailed(Platform p, Throwable e) {
        p.warn(MOD_NAME + " cannot change the game window on " + p.describe() + ". The titlebar stays as it is.", e);
    }

    /** True while the mod keeps the titlebar hidden. */
    public static boolean hidden() { return config.hidden(); }

    private static Config load() {
        if (!configFile.isFile()) {
            save(Config.DEFAULTS);
            return Config.DEFAULTS;
        }
        try {
            return ConfigCodec.decode(new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            platform.warn("The config file " + configFile + " is not valid. The mod uses the default values.", e);
            return Config.DEFAULTS;
        }
    }

    private static void save(Config c) {
        try {
            File dir = configFile.getParentFile();
            if (dir != null) Files.createDirectories(dir.toPath());
            Files.write(configFile.toPath(), ConfigCodec.encode(c).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            platform.warn("Cannot save " + configFile, e);
        }
    }
}
