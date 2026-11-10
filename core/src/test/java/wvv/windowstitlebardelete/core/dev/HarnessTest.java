package wvv.windowstitlebardelete.core.dev;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wvv.windowstitlebardelete.core.FakePlatform;
import wvv.windowstitlebardelete.core.FakeWindow;
import wvv.windowstitlebardelete.core.Styles;
import wvv.windowstitlebardelete.core.Titlebar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarnessTest {
    @TempDir
    File dir;

    private FakePlatform platform;
    private FakeWindow window;
    private Harness harness;

    private void start(String... commands) throws IOException {
        platform = new FakePlatform(new File(dir, "config"));
        window = new FakeWindow();
        Titlebar.init(platform, window);
        harness = new Harness(dir, platform);
        File file = new File(dir, "commands.txt");
        Files.write(file.toPath(), Arrays.asList(commands), StandardCharsets.UTF_8);
        assertTrue(file.setLastModified(System.currentTimeMillis() - 5000));
    }

    /** Runs ticks in the order of the game, where the harness runs before the core examines the window. */
    private void ticks(int n) {
        for (int i = 0; i < n; i++) {
            harness.tick();
            Titlebar.tick();
        }
    }

    private List<String> log() throws IOException {
        return Files.readAllLines(new File(dir, "log.txt").toPath(), StandardCharsets.UTF_8);
    }

    private static String style(int bits, boolean hidden) {
        return String.format(Locale.ROOT, "style 0x%08X hidden=%b", bits, hidden);
    }

    @Test
    void togglesAndLogsTheStyleOfTheNextTick() throws IOException {
        start("style", "toggle", "style", "# a comment", "", "toggle", "style", "wait 3", "quit");
        ticks(10);
        int hidden = Styles.hidden(FakeWindow.NORMAL, true);
        assertEquals(Arrays.asList("> style", style(hidden, true), "> toggle", "> style", style(FakeWindow.NORMAL, false), "> toggle",
                "> style", style(hidden, true), "> wait 3", "> quit"), log());
        assertEquals(Arrays.asList("quit"), platform.calls);
    }

    @Test
    void waitDelaysTheNextCommand() throws IOException {
        start("wait 2", "quit");
        ticks(3);
        assertTrue(platform.calls.isEmpty());
        ticks(1);
        assertEquals(Arrays.asList("quit"), platform.calls);
    }

    @Test
    void waitsForTheWindow() throws IOException {
        start("quit");
        window.exists = false;
        ticks(5);
        assertTrue(platform.calls.isEmpty());
        window.exists = true;
        ticks(1);
        assertEquals(Arrays.asList("quit"), platform.calls);
    }

    @Test
    void runsAnywayAfterTheWindowLimit() throws IOException {
        start("quit");
        window.exists = false;
        ticks(Harness.WINDOW_TICKS);
        assertEquals(Arrays.asList("! no game window after " + Harness.WINDOW_TICKS + " ticks", "> quit"), log());
    }

    @Test
    void forwardsFullscreenAndRejectsOtherText() throws IOException {
        start("fullscreen on", "fullscreen off", "fullscreen maybe", "jump");
        ticks(1);
        assertEquals(Arrays.asList("setFullscreen true", "setFullscreen false"), platform.calls);
        List<String> log = log();
        assertEquals(6, log.size());
        assertTrue(log.get(3).startsWith("! "));
        assertEquals("! unknown command: jump", log.get(5));
        assertThrows(IllegalArgumentException.class, () -> Harness.onOff("yes"));
    }
}
