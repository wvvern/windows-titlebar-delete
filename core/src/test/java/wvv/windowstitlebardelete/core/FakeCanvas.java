package wvv.windowstitlebardelete.core;

import wvv.windowstitlebardelete.core.ui.Canvas;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** A canvas for tests with 6 pixels for each letter that records the text it draws. */
public final class FakeCanvas implements Canvas {
    public final List<String> texts = new ArrayList<String>();
    private final int width, height;

    public FakeCanvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int width() { return width; }

    @Override
    public int height() { return height; }

    @Override
    public void fill(int x, int y, int w, int h, int argb) {}

    @Override
    public void text(String text, int x, int y, int argb) {
        assertTrue(x >= 0 && y >= 0 && x + textWidth(text) <= width && y + 8 <= height, "outside the screen: " + text);
        texts.add(text);
    }

    @Override
    public int textWidth(String text) { return text.length() * 6; }
}
