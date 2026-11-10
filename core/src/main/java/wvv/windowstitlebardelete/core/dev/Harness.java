package wvv.windowstitlebardelete.core.dev;

import wvv.windowstitlebardelete.core.Platform;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.config.Config;
import wvv.windowstitlebardelete.core.ui.SettingsEditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Development harness, created only with {@code -Dwtd.harness=<directory>}, that runs the lines of
 * {@code <dir>/commands.txt} and appends to {@code <dir>/log.txt}, so a game on a hidden desktop can be tested.
 */
public final class Harness {
    /** What the harness needs from the game, which each version adapter implements. */
    public interface Host {
        void setFullscreen(boolean on);

        /** Writes the next rendered frame to {@code png}, then calls {@link Harness#log}. */
        void screenshot(File png);

        void closeScreen();

        void quit();
    }

    /** The ticks that the harness waits for the game window before it runs the commands anyway. */
    static final int WINDOW_TICKS = 1200;
    /** The age in milliseconds that a command file must have, so that the harness does not take a file that is still being written. */
    private static final long SETTLE_MS = 500;

    private static volatile File logFile;

    private final File dir;
    private final Platform platform;
    private final Host host;
    private final Deque<String> queue = new ArrayDeque<String>();
    private int waitTicks;
    /** The ticks that passed before the game window was found, or -1 after it was found or after the limit. */
    private int windowTicks;
    /** A {@code style} command ran, so the next tick logs the style that the reconcile of this tick set. */
    private boolean styleDue;

    Harness(File dir, Platform platform) {
        this.dir = dir;
        this.platform = platform;
        this.host = platform.harnessHost();
        logFile = new File(dir, "log.txt");
    }

    /** @return the harness, or null when the system property is not set */
    public static Harness fromSystemProperty(Platform platform) {
        String path = System.getProperty("wtd.harness");
        if (path == null || path.trim().isEmpty()) return null;
        File dir = new File(path).getAbsoluteFile();
        dir.mkdirs();
        platform.info("Test harness enabled in " + dir);
        return new Harness(dir, platform);
    }

    /** Runs the commands that are due, before the reconcile of {@link Titlebar#tick}. */
    public void tick() {
        takeCommandFile();
        if (windowTicks >= 0) {
            if (Titlebar.windowStyle() == null && ++windowTicks < WINDOW_TICKS) return;
            if (windowTicks >= WINDOW_TICKS) log("! no game window after " + WINDOW_TICKS + " ticks");
            windowTicks = -1;
        }
        if (styleDue) {
            styleDue = false;
            logStyle();
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        while (!queue.isEmpty() && waitTicks == 0 && !styleDue) {
            String line = queue.poll().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            log("> " + line);
            try {
                execute(line);
            } catch (RuntimeException e) {
                log("! " + e);
            }
        }
    }

    private void execute(String line) {
        String[] words = line.split("\\s+", 2);
        String verb = words[0];
        String arg = words.length > 1 ? words[1].trim() : "";
        if (verb.equals("wait")) waitTicks = Integer.parseInt(arg);
        else if (verb.equals("toggle")) Titlebar.toggle();
        else if (verb.equals("set")) Titlebar.apply(withOption(Titlebar.config(), arg));
        else if (verb.equals("style")) styleDue = true;
        else if (verb.equals("settings")) platform.openSettings();
        else if (verb.equals("close")) host.closeScreen();
        else if (verb.equals("click")) click(arg);
        else if (verb.equals("fullscreen")) host.setFullscreen(onOff(arg));
        else if (verb.equals("shot")) host.screenshot(new File(dir, arg.endsWith(".png") ? arg : arg + ".png"));
        else if (verb.equals("quit")) host.quit();
        else log("! unknown command: " + verb);
    }

    /** The config with one option set from {@code <hideOnStartup|keepResizeBorder> <on|off>}. */
    static Config withOption(Config config, String arg) {
        String[] words = arg.split("\\s+");
        if (words.length != 2) throw new IllegalArgumentException("set <hideOnStartup|keepResizeBorder> <on|off>");
        boolean on = onOff(words[1]);
        if (words[0].equals("hideOnStartup")) return config.withHideOnStartup(on);
        if (words[0].equals("keepResizeBorder")) return config.withKeepResizeBorder(on);
        throw new IllegalArgumentException("unknown option: " + words[0]);
    }

    static boolean onOff(String word) {
        if (word.equals("on")) return true;
        if (word.equals("off")) return false;
        throw new IllegalArgumentException("expected on or off: " + word);
    }

    private static void click(String label) {
        SettingsEditor editor = SettingsEditor.current();
        if (editor == null) log("! click: no settings screen is open");
        else if (!editor.click(label)) log("! click: the settings screen has no control with the label " + label);
    }

    private static void logStyle() {
        Integer style = Titlebar.windowStyle();
        if (style == null) log("! style: no game window");
        else log(String.format(Locale.ROOT, "style 0x%08X hidden=%b", style, Titlebar.hidden()));
    }

    private void takeCommandFile() {
        File commands = new File(dir, "commands.txt");
        if (!commands.isFile() || System.currentTimeMillis() - commands.lastModified() < SETTLE_MS) return;
        File taken = new File(dir, "commands.taken");
        taken.delete();
        if (!commands.renameTo(taken)) return; // the writer can still hold it, so the next tick tries again
        try {
            queue.addAll(Files.readAllLines(taken.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log("! cannot read commands: " + e);
        }
    }

    /** Appends a line to the harness log, from any thread, and never throws. */
    public static synchronized void log(String message) {
        if (logFile == null) return;
        try {
            byte[] line = (message + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8);
            Files.write(logFile.toPath(), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Logging must never break the game.
        }
    }
}
