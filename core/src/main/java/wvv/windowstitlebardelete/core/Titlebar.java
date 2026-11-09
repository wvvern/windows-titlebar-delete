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

/**
 * The one entry point of the core for the adapters, which keeps the style of the game window in step with the config
 * and the toggle key on each client tick.
 */
public final class Titlebar {
    public static final String MOD_ID = "windowstitlebardelete";
    public static final String MOD_NAME = "Windows Titlebar Delete";
    /** The name of the key binding, which is plain text because Fabric without Fabric API loads no language files. */
    public static final String KEY_NAME = "Toggle the titlebar";
    public static final String CONFIG_FILE = "windowstitlebardelete.json";

    private static Platform platform;
    private static File configFile;
    private static volatile Config config = Config.DEFAULTS;
    /** True while the titlebar must be hidden, which the toggle key changes and the config file does not store. */
    private static volatile boolean hidden;
    private static NativeWindow window = NativeWindow.NONE;
    private static Harness harness;

    private Titlebar() {}

    /** Starts the core with the window of this process on Windows, and with no window on other operating systems. */
    public static synchronized void init(Platform p) {
        init(p, window(p, System.getProperty("os.name", "")));
    }

    /** Starts the core with a given window, which tests and adapters that know the window handle use. */
    public static synchronized void init(Platform p, NativeWindow w) {
        platform = p;
        configFile = new File(p.configDir(), CONFIG_FILE);
        config = load();
        hidden = config.hideOnStartup();
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

    /** Runs the harness commands, then sets the window style that the config and the toggle key want; call it on the client thread. */
    public static synchronized void tick() {
        if (platform == null) return;
        if (harness != null) harness.tick();
        if (platform.isFullscreen()) return;
        try {
            if (!window.found()) return;
            int style = window.style();
            int wanted = Styles.wanted(style, hidden, config.keepResizeBorder());
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

    /** Shows or hides the titlebar for this session at the next tick, which does not change the config file. */
    public static void toggle() {
        hidden = !hidden;
    }

    /** True while the titlebar is hidden or is about to be hidden at the next tick. */
    public static boolean hidden() { return hidden; }

    public static Config config() { return config; }

    /** Saves a config to the file, which takes effect at the next tick and does not change {@link #hidden}. */
    public static synchronized void apply(Config next) {
        config = next;
        if (configFile != null) save(next);
    }

    /** Shows the settings screen of the adapter. */
    public static void openSettings() {
        if (platform != null) platform.openSettings();
    }

    private static Config load() {
        if (!configFile.isFile()) {
            save(Config.DEFAULTS);
            return Config.DEFAULTS;
        }
        try {
            return ConfigCodec.decode(new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            platform.warn("The config file " + configFile + " is not valid. The mod uses the default settings.", e);
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
