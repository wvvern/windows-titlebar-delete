package wvv.windowstitlebardelete.core.dev;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wvv.windowstitlebardelete.core.FakePlatform;
import wvv.windowstitlebardelete.core.FakeWindow;
import wvv.windowstitlebardelete.core.Titlebar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarnessTest {
    /** {@link FakeWindow#NORMAL} without its titlebar and with the resize border. */
    private static final int HIDDEN = 0x16050000;

    @TempDir
    File dir;

    private FakePlatform platform;
    private FakeWindow window;
    private Harness harness;

    private void start(String... commands) throws IOException {
        platform = new FakePlatform(new File(dir, "config"));
        window = new FakeWindow();
        Titlebar.init(platform, window);
        Files.write(new File(dir, "commands.txt").toPath(), Arrays.asList(commands), StandardCharsets.UTF_8);
        harness = new Harness(dir, platform);
    }

    /** Runs ticks in the order of {@link Titlebar#tick}, where the harness runs after the core examines the window. */
    private void ticks(int n) {
        for (int i = 0; i < n; i++) {
            Titlebar.tick();
            harness.tick();
        }
    }

    private List<String> log() throws IOException {
        return Files.readAllLines(new File(dir, "log.txt").toPath(), StandardCharsets.UTF_8);
    }

    private static String style(int bits, boolean hidden) {
        return String.format(Locale.ROOT, "style 0x%08X hidden=%b", bits, hidden);
    }

    @Test
    void togglesAndLogsTheStyleAtOnce() throws IOException {
        start("style", "toggle", "style", "# a comment", "", "toggle", "style", "wait 3", "quit");
        ticks(1);
        assertEquals(Arrays.asList("> style", style(HIDDEN, true), "> toggle", "> style", style(FakeWindow.NORMAL, false), "> toggle",
                "> style", style(HIDDEN, true), "> wait 3"), log());
        ticks(4);
        assertEquals("> quit", log().get(9));
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
    void styleAfterWaitIsTheStyleThatTheSameTickRepaired() throws IOException {
        start("wait 1", "style");
        ticks(2);
        window.style = FakeWindow.NORMAL;
        ticks(1);
        assertEquals(Arrays.asList("> wait 1", "> style", style(HIDDEN, true)), log());
    }

    @Test
    void styleWithNoWindowLogsAnError() throws IOException {
        start("style");
        window.exists = false;
        ticks(1);
        assertEquals(Arrays.asList("> style", "! style: no game window"), log());
    }

    @Test
    void missingCommandsFileLogsAnError() throws IOException {
        new Harness(dir, null);
        assertTrue(log().get(0).startsWith("! cannot read commands"));
    }

    @Test
    void forwardsFullscreenAndRejectsOtherText() throws IOException {
        start("fullscreen on", "fullscreen off", "fullscreen maybe", "wait x", "jump");
        ticks(1);
        assertEquals(Arrays.asList("setFullscreen true", "setFullscreen false"), platform.calls);
        List<String> log = log();
        assertEquals(Arrays.asList("> fullscreen on", "> fullscreen off", "> fullscreen maybe", "! unknown command: fullscreen maybe", "> wait x"),
                log.subList(0, 5));
        assertTrue(log.get(5).startsWith("! java.lang.NumberFormatException"));
        assertEquals(Arrays.asList("> jump", "! unknown command: jump"), log.subList(6, 8));
    }
}
