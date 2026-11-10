package wvv.windowstitlebardelete.core;

/** The Win32 window style bits that the mod changes, and the styles of a window with and without its titlebar. */
final class Styles {
    static final int WS_POPUP = 0x80000000;
    static final int WS_CAPTION = 0x00C00000;
    static final int WS_SYSMENU = 0x00080000;
    static final int WS_THICKFRAME = 0x00040000;
    static final int WS_MINIMIZEBOX = 0x00020000;
    static final int WS_MAXIMIZEBOX = 0x00010000;

    /** The bits that a hidden titlebar removes, where the maximize box stays so that Win+Up still works. */
    private static final int TITLEBAR = WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX;

    private Styles() {}

    /** The style of the window without its titlebar, with or without the resize border, which is the same style for a popup window. */
    static int hidden(int style, boolean keepBorder) {
        if (isPopup(style)) return style;
        int bare = (style & ~TITLEBAR) | WS_MAXIMIZEBOX;
        return keepBorder ? bare | WS_THICKFRAME : bare & ~WS_THICKFRAME;
    }

    /** The style of the window with its titlebar and its resize border, which is the same style for a popup window. */
    static int shown(int style) {
        return isPopup(style) ? style : style | TITLEBAR | WS_MAXIMIZEBOX | WS_THICKFRAME;
    }

    /** True for a window that is in fullscreen or borderless on purpose, so its style stays. */
    private static boolean isPopup(int style) {
        return (style & WS_POPUP) != 0;
    }
}
