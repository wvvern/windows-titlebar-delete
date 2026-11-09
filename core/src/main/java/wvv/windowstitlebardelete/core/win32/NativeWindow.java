package wvv.windowstitlebardelete.core.win32;

/** The port to the game window of the operating system, which {@link JnaWindow} implements on Windows. */
public interface NativeWindow {
    /** A window that is never found, for operating systems other than Windows and after a native failure. */
    NativeWindow NONE = new NativeWindow() {
        @Override
        public boolean found() { return false; }

        @Override
        public int style() { throw new IllegalStateException("no window"); }

        @Override
        public void setStyle(int style) { throw new IllegalStateException("no window"); }
    };

    /** Finds the game window when the known handle is not valid, and returns true when a window is known. */
    boolean found();

    /** The style bits of the window, which only a call after {@link #found} gave true can read. */
    int style();

    /** Sets the style bits of the window and redraws its frame, which only a call after {@link #found} gave true can do. */
    void setStyle(int style);
}
