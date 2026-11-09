package wvv.windowstitlebardelete.core;

/** The Win32 window style bits that the mod changes, and the style that a window must have. */
public final class Styles {
    public static final int WS_CAPTION = 0x00C00000;
    public static final int WS_SYSMENU = 0x00080000;
    public static final int WS_THICKFRAME = 0x00040000;
    public static final int WS_MINIMIZEBOX = 0x00020000;
    public static final int WS_MAXIMIZEBOX = 0x00010000;

    /** The bits that a hidden titlebar removes, where the maximize box stays so that Win+Up still works. */
    private static final int TITLEBAR = WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX;
    private static final int SHOWN = TITLEBAR | WS_MAXIMIZEBOX | WS_THICKFRAME;

    private Styles() {}

    /**
     * The style that the window must have, which changes only the titlebar and border bits and gives the same result
     * when it gets its own result.
     *
     * @param keepBorder true to keep the resize border while the titlebar is hidden
     */
    public static int wanted(int style, boolean hidden, boolean keepBorder) {
        if (!hidden) return style | SHOWN;
        int bare = (style & ~TITLEBAR) | WS_MAXIMIZEBOX;
        return keepBorder ? bare | WS_THICKFRAME : bare & ~WS_THICKFRAME;
    }
}
