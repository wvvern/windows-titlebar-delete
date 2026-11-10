package wvv.windowstitlebardelete.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wvv.windowstitlebardelete.core.config.Config;
import wvv.windowstitlebardelete.core.config.ConfigCodec;
import wvv.windowstitlebardelete.core.win32.NativeWindow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlebarTest {
    private static final int HIDDEN = Styles.hidden(FakeWindow.NORMAL, true);
    private static final Config SHOWN = new Config(false, true);

    @TempDir
    File dir;

    private FakePlatform platform;
    private FakeWindow window;

    private void start() {
        platform = new FakePlatform(dir);
        window = new FakeWindow();
        Titlebar.init(platform, window);
    }

    private void startWith(String config) throws IOException {
        Files.write(configFile().toPath(), config.getBytes(StandardCharsets.UTF_8));
        start();
    }

    private File configFile() {
        return new File(dir, Titlebar.CONFIG_FILE);
    }

    private String saved() throws IOException {
        return new String(Files.readAllBytes(configFile().toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void newInstallWritesDefaultsAndHides() throws IOException {
        start();
        assertTrue(Titlebar.hidden());
        assertEquals(ConfigCodec.encode(Config.DEFAULTS), saved());
        assertTrue(platform.warnings.isEmpty());
        assertTrue(platform.infos.isEmpty());
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
    }

    @Test
    void keyWithABadValueWarnsOnceAndUsesDefaults() throws IOException {
        String text = "{\"hidden\": false, \"keepResizeBorder\": no}";
        startWith(text);
        assertTrue(Titlebar.hidden());
        assertEquals(1, platform.warnings.size());
        assertEquals(text, saved());
    }

    @Test
    void writesOnlyWhenTheStyleIsDifferent() {
        start();
        Titlebar.tick();
        Titlebar.tick();
        Titlebar.tick();
        assertEquals(1, window.writes);
    }

    @Test
    void neverWritesWhenTheSavedStateIsShown() throws IOException {
        startWith(ConfigCodec.encode(SHOWN));
        assertFalse(Titlebar.hidden());
        Titlebar.tick();
        window.style = HIDDEN;
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
        assertEquals(0, window.writes);
    }

    @Test
    void toggleToShownRestoresOneTimeAndThenLeavesTheWindow() {
        start();
        Titlebar.tick();
        Titlebar.toggle();
        assertEquals(FakeWindow.NORMAL, window.style);
        assertEquals(2, window.writes);
        window.style = HIDDEN;
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
        assertEquals(2, window.writes);
    }

    @Test
    void toggleSavesTheStateAtOnce() throws IOException {
        start();
        Titlebar.toggle();
        assertEquals(ConfigCodec.encode(SHOWN), saved());
        Titlebar.toggle();
        assertEquals(ConfigCodec.encode(Config.DEFAULTS), saved());
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
    }

    @Test
    void toggleKeepsTheResizeBorderValueOfTheFile() throws IOException {
        startWith(ConfigCodec.encode(new Config(false, false)));
        Titlebar.toggle();
        assertEquals(Styles.hidden(FakeWindow.NORMAL, false), window.style);
        assertEquals(ConfigCodec.encode(new Config(true, false)), saved());
    }

    @Test
    void repairsAStyleThatTheGameResets() {
        start();
        Titlebar.tick();
        window.style = FakeWindow.NORMAL;
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
        assertEquals(2, window.writes);
    }

    @Test
    void skipsFullscreenAndRepairsAfterwards() {
        start();
        platform.fullscreen = true;
        Titlebar.tick();
        Titlebar.toggle();
        Titlebar.toggle();
        assertEquals(0, window.writes);
        platform.fullscreen = false;
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
    }

    @Test
    void waitsForTheWindow() {
        start();
        window.exists = false;
        Titlebar.tick();
        Titlebar.toggle();
        Titlebar.toggle();
        assertEquals(0, window.writes);
        assertNull(Titlebar.windowStyle());
        window.exists = true;
        Titlebar.tick();
        assertEquals(Integer.valueOf(HIDDEN), Titlebar.windowStyle());
    }

    @Test
    void nativeFailureWarnsOnceAndStops() {
        start();
        window.failure = new UnsatisfiedLinkError("test");
        Titlebar.tick();
        Titlebar.tick();
        Titlebar.toggle();
        assertNull(Titlebar.windowStyle());
        assertEquals(1, platform.warnings.size());
    }

    @Test
    void otherOperatingSystemsLogOneLine() {
        platform = new FakePlatform(dir);
        NativeWindow none = Titlebar.window(platform, "Linux");
        assertSame(NativeWindow.NONE, none);
        Titlebar.init(platform, none);
        Titlebar.tick();
        Titlebar.toggle();
        Titlebar.tick();
        assertEquals(1, platform.infos.size());
        assertTrue(platform.warnings.isEmpty());
    }
}
