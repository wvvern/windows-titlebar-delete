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
    private static final int HIDDEN = Styles.wanted(FakeWindow.NORMAL, true, true);

    @TempDir
    File dir;

    private FakePlatform platform;
    private FakeWindow window;

    private void start() {
        platform = new FakePlatform(dir);
        window = new FakeWindow();
        Titlebar.init(platform, window);
    }

    private File configFile() {
        return new File(dir, Titlebar.CONFIG_FILE);
    }

    private String read(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void missingConfigWritesDefaults() throws IOException {
        start();
        assertEquals(Config.DEFAULTS, Titlebar.config());
        assertEquals(ConfigCodec.encode(Config.DEFAULTS), read(configFile()));
        assertTrue(platform.warnings.isEmpty());
        assertTrue(platform.infos.isEmpty());
    }

    @Test
    void brokenConfigWarnsOnceAndUsesDefaults() throws IOException {
        Files.write(configFile().toPath(), "{ broken".getBytes(StandardCharsets.UTF_8));
        start();
        assertEquals(Config.DEFAULTS, Titlebar.config());
        assertEquals(1, platform.warnings.size());
        assertEquals("{ broken", read(configFile()));
    }

    @Test
    void hidesAtStartupAndWritesOnlyWhenDifferent() {
        start();
        assertTrue(Titlebar.hidden());
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
        assertEquals(1, window.writes);
        Titlebar.tick();
        Titlebar.tick();
        assertEquals(1, window.writes);
    }

    @Test
    void staysShownWhenTheConfigSaysSo() throws IOException {
        Files.write(configFile().toPath(), "{\"hideOnStartup\": false}".getBytes(StandardCharsets.UTF_8));
        start();
        assertFalse(Titlebar.hidden());
        Titlebar.tick();
        assertEquals(FakeWindow.NORMAL, window.style);
        assertEquals(0, window.writes);
    }

    @Test
    void toggleShowsAndHidesWithoutSaving() throws IOException {
        start();
        Titlebar.tick();
        Titlebar.toggle();
        Titlebar.tick();
        assertEquals(FakeWindow.NORMAL, window.style);
        Titlebar.toggle();
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
        assertEquals(ConfigCodec.encode(Config.DEFAULTS), read(configFile()));
    }

    @Test
    void skipsFullscreenAndRepairsAfterwards() {
        start();
        platform.fullscreen = true;
        Titlebar.tick();
        assertEquals(0, window.writes);
        platform.fullscreen = false;
        Titlebar.tick();
        assertEquals(HIDDEN, window.style);
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
    void waitsForTheWindow() {
        start();
        window.exists = false;
        Titlebar.tick();
        assertEquals(0, window.writes);
        assertNull(Titlebar.windowStyle());
        window.exists = true;
        Titlebar.tick();
        assertEquals(Integer.valueOf(HIDDEN), Titlebar.windowStyle());
    }

    @Test
    void applySavesAndTakesEffectWithoutChangingHidden() throws IOException {
        start();
        Titlebar.tick();
        Config next = new Config(false, false);
        Titlebar.apply(next);
        assertTrue(Titlebar.hidden());
        assertEquals(ConfigCodec.encode(next), read(configFile()));
        Titlebar.tick();
        assertEquals(Styles.wanted(FakeWindow.NORMAL, true, false), window.style);
    }

    @Test
    void nativeFailureWarnsOnceAndStops() {
        start();
        window.failure = new UnsatisfiedLinkError("test");
        Titlebar.tick();
        Titlebar.tick();
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
