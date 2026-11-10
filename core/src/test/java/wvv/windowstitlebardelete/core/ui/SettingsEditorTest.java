package wvv.windowstitlebardelete.core.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wvv.windowstitlebardelete.core.FakeCanvas;
import wvv.windowstitlebardelete.core.FakePlatform;
import wvv.windowstitlebardelete.core.FakeWindow;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.config.Config;
import wvv.windowstitlebardelete.core.config.ConfigCodec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsEditorTest {
    @TempDir
    File dir;

    private SettingsEditor open(FakeCanvas canvas) {
        Titlebar.init(new FakePlatform(dir), new FakeWindow());
        SettingsEditor editor = new SettingsEditor("F12");
        editor.render(canvas, -1, -1);
        return editor;
    }

    @Test
    void drawsTheTitleTheSwitchesAndTheHint() {
        FakeCanvas canvas = new FakeCanvas(854, 480);
        open(canvas);
        assertTrue(canvas.texts.contains(Titlebar.MOD_NAME));
        assertTrue(canvas.texts.contains(SettingsEditor.HIDE_ON_STARTUP));
        assertTrue(canvas.texts.contains(SettingsEditor.KEEP_RESIZE_BORDER));
        assertTrue(canvas.texts.contains("Push F12 to show or hide the titlebar. Change the key in Controls."));
        assertTrue(canvas.texts.contains(SettingsEditor.DONE));
    }

    @Test
    void wrapsTheHintOnANarrowScreen() {
        FakeCanvas canvas = new FakeCanvas(320, 240);
        open(canvas);
        assertTrue(canvas.texts.contains("Push F12 to show or hide the titlebar."));
        assertTrue(canvas.texts.contains("Change the key in Controls."));
    }

    @Test
    void switchClicksSaveTheConfigAtOnce() throws IOException {
        SettingsEditor editor = open(new FakeCanvas(854, 480));
        assertTrue(editor.click(SettingsEditor.KEEP_RESIZE_BORDER));
        assertEquals(new Config(true, false), Titlebar.config());
        assertTrue(editor.click("hide the titlebar at startup"));
        Config expected = new Config(false, false);
        assertEquals(expected, Titlebar.config());
        String saved = new String(Files.readAllBytes(new File(dir, Titlebar.CONFIG_FILE).toPath()), StandardCharsets.UTF_8);
        assertEquals(ConfigCodec.encode(expected), saved);
        assertTrue(Titlebar.hidden());
        assertFalse(editor.done());
    }

    @Test
    void doneAndClose() {
        SettingsEditor editor = open(new FakeCanvas(854, 480));
        assertSame(editor, SettingsEditor.current());
        assertFalse(editor.click("Nothing"));
        editor.mouseClicked(0, 0);
        assertFalse(editor.done());
        assertTrue(editor.click(SettingsEditor.DONE));
        assertTrue(editor.done());
        assertEquals(Config.DEFAULTS, Titlebar.config());
        editor.close();
        assertNull(SettingsEditor.current());
    }
}
