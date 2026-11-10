package wvv.windowstitlebardelete.core.dev;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wvv.windowstitlebardelete.core.FakeCanvas;
import wvv.windowstitlebardelete.core.FakePlatform;
import wvv.windowstitlebardelete.core.FakeWindow;
import wvv.windowstitlebardelete.core.Styles;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.config.Config;
import wvv.windowstitlebardelete.core.ui.SettingsEditor;

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

    /** Runs ticks in the order of the game, where the harness runs before the reconcile of each tick. */
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
    void togglesAndLogsTheStyleAfterTheReconcile() throws IOException {
        start("style", "toggle", "style", "# a comment", "", "toggle", "style", "wait 3", "quit");
        ticks(10);
        int hidden = Styles.wanted(FakeWindow.NORMAL, true, true);
        int shown = Styles.wanted(hidden, false, true);
        assertEquals(Arrays.asList("> style", style(hidden, true), "> toggle", "> style", style(shown, false), "> toggle", "> style",
                style(hidden, true), "> wait 3", "> quit"), log());
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
    void forwardsHostCommands() throws IOException {
        start("settings", "close", "fullscreen on", "fullscreen off", "shot menu", "shot b.png");
        ticks(1);
        assertEquals(Arrays.asList("openSettings", "closeScreen", "setFullscreen true", "setFullscreen false", "screenshot menu.png",
                "screenshot b.png"), platform.calls);
    }

    @Test
    void setChangesTheSavedConfig() throws IOException {
        start("set keepResizeBorder off", "set hideOnStartup off", "set other on", "set keepResizeBorder maybe", "jump");
        ticks(1);
        assertEquals(new Config(false, false), Titlebar.config());
        List<String> log = log();
        assertEquals(8, log.size());
        assertTrue(log.get(3).startsWith("! "));
        assertTrue(log.get(5).startsWith("! "));
        assertTrue(log.get(7).startsWith("! unknown command"));
    }

    @Test
    void clicksTheSettingsScreen() throws IOException {
        start("click Done", "click Keep the resize border", "click Nothing");
        SettingsEditor editor = new SettingsEditor("F12");
        editor.render(new FakeCanvas(854, 480), 0, 0);
        ticks(1);
        assertTrue(editor.done());
        assertEquals(Config.DEFAULTS.withKeepResizeBorder(false), Titlebar.config());
        assertTrue(log().get(3).startsWith("! click"));
        editor.close();
    }

    @Test
    void parsesOptions() {
        assertEquals(new Config(true, false), Harness.withOption(Config.DEFAULTS, "keepResizeBorder off"));
        assertEquals(new Config(false, true), Harness.withOption(Config.DEFAULTS, "hideOnStartup  off"));
        assertThrows(IllegalArgumentException.class, () -> Harness.withOption(Config.DEFAULTS, "hideOnStartup"));
        assertThrows(IllegalArgumentException.class, () -> Harness.onOff("yes"));
    }
}
