package wvv.windowstitlebardelete.core.ui;

/** The drawing primitives of every Minecraft version since 1.8, in GUI-scaled pixels and 0xAARRGGBB colors. */
public interface Canvas {
    int width();

    int height();

    /** Fills the rectangle with its top-left at x, y, w pixels wide and h pixels high. */
    void fill(int x, int y, int w, int h, int argb);

    /** Draws text with no shadow and its top-left at x, y, where the letters are 8 pixels high as in vanilla. */
    void text(String text, int x, int y, int argb);

    int textWidth(String text);
}
