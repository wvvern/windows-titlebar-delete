package wvv.windowstitlebardelete.core;

import wvv.windowstitlebardelete.core.win32.NativeWindow;

/** A window for tests that keeps its style in a field and counts the writes. */
public final class FakeWindow implements NativeWindow {
    /** A style of a normal game window with its titlebar: WS_OVERLAPPEDWINDOW, WS_VISIBLE and WS_CLIPCHILDREN. */
    public static final int NORMAL = 0x16CF0000;

    public int style = NORMAL;
    public int writes;
    public boolean exists = true;
    /** An error that each native call throws, or null. */
    public Error failure;

    @Override
    public boolean found() {
        if (failure != null) throw failure;
        return exists;
    }

    @Override
    public int style() {
        if (failure != null) throw failure;
        return style;
    }

    @Override
    public void setStyle(int value) {
        if (failure != null) throw failure;
        style = value;
        writes++;
    }
}
