package wvv.windowstitlebardelete.core.dev;

import wvv.windowstitlebardelete.core.Platform;
import wvv.windowstitlebardelete.core.Titlebar;

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

        void quit();
    }

    private final File logFile;
    private final Host host;
    private final Deque<String> queue = new ArrayDeque<String>();
    private int waitTicks;

    Harness(File dir, Host host) {
        this.logFile = new File(dir, "log.txt");
        this.host = host;
        try {
            queue.addAll(Files.readAllLines(new File(dir, "commands.txt").toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log("! cannot read commands: " + e);
        }
    }

    /** @return the harness, or null when the system property is not set */
    public static Harness fromSystemProperty(Platform platform) {
        String path = System.getProperty("wtd.harness");
        if (path == null) return null;
        File dir = new File(path).getAbsoluteFile();
        platform.info("Test harness enabled in " + dir);
        return new Harness(dir, platform.harnessHost());
    }

    /** Runs the commands up to the next {@code wait}, or counts one tick of the current {@code wait}. */
    public void tick() {
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        while (!queue.isEmpty() && waitTicks == 0) {
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
        String arg = words.length > 1 ? words[1] : "";
        if (verb.equals("wait")) waitTicks = Integer.parseInt(arg);
        else if (verb.equals("toggle")) Titlebar.toggle();
        else if (verb.equals("style")) logStyle();
        else if (verb.equals("fullscreen") && (arg.equals("on") || arg.equals("off"))) host.setFullscreen(arg.equals("on"));
        else if (verb.equals("quit")) host.quit();
        else log("! unknown command: " + line);
    }

    private void logStyle() {
        Integer style = Titlebar.windowStyle();
        if (style == null) log("! style: no game window");
        else log(String.format(Locale.ROOT, "style 0x%08X hidden=%b", style, Titlebar.hidden()));
    }

    private void log(String message) {
        try {
            byte[] line = (message + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            Files.write(logFile.toPath(), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // A log failure must not stop the game.
        }
    }
}
