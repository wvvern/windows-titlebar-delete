package wvv.windowstitlebardelete.core.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Collections;
import java.util.Map;

/**
 * The game window of this process through JNA direct mapping, with only the JNA API that is the same in JNA 3.4.0 of
 * Minecraft 1.8.9 and in the later versions.
 */
public final class JnaWindow implements NativeWindow {
    private static final int GWL_STYLE = -16;
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOZORDER = 0x0004;
    private static final int SWP_FRAMECHANGED = 0x0020;

    private final int pid = Kernel32.GetCurrentProcessId();
    /** The window that the last search found, or null. */
    private Pointer hwnd;
    /** The callback object stays in a field, so that the garbage collector does not free it while Windows can call it. */
    private final EnumWindowsProc search = new EnumWindowsProc() {
        @Override
        public boolean callback(Pointer candidate, Pointer data) {
            if (!isGameWindow(candidate)) return true;
            hwnd = candidate;
            return false;
        }
    };

    @Override
    public boolean found() {
        if (hwnd != null && User32.IsWindow(hwnd)) return true;
        hwnd = null;
        User32.EnumWindows(search, null);
        return hwnd != null;
    }

    @Override
    public int style() {
        return User32.GetWindowLongW(hwnd, GWL_STYLE);
    }

    @Override
    public void setStyle(int style) {
        User32.SetWindowLongW(hwnd, GWL_STYLE, style);
        User32.SetWindowPos(hwnd, null, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER);
    }

    /** True for a visible top-level window of this process with the window class of LWJGL 2, GLFW or SDL. */
    private boolean isGameWindow(Pointer candidate) {
        int[] windowPid = new int[1];
        User32.GetWindowThreadProcessId(candidate, windowPid);
        if (windowPid[0] != pid || !User32.IsWindowVisible(candidate)) return false;
        char[] className = new char[256];
        String name = new String(className, 0, User32.GetClassNameW(candidate, className, className.length));
        return name.equals("LWJGL") || name.equals("GLFW30") || name.startsWith("SDL");
    }

    /** The stdcall option on 32-bit Windows, and no option on 64-bit Windows, where a direct mapping with stdcall crashes JNA. */
    private static Map<String, Object> stdcall() {
        if (Native.POINTER_SIZE == 8) return Collections.<String, Object>emptyMap();
        return Collections.<String, Object>singletonMap(Library.OPTION_CALLING_CONVENTION, StdCallLibrary.STDCALL_CONVENTION);
    }

    /** The EnumWindows callback, which is stdcall on 32-bit Windows and public so that JNA can call it. */
    public interface EnumWindowsProc extends StdCallLibrary.StdCallCallback {
        boolean callback(Pointer hwnd, Pointer data);
    }

    /** The user32 functions, whose Java names must be the Win32 names for direct mapping. */
    @SuppressWarnings("checkstyle:methodname")
    private static final class User32 {
        static {
            Native.register(User32.class, NativeLibrary.getInstance("user32", stdcall()));
        }

        private User32() {}

        static native boolean EnumWindows(EnumWindowsProc proc, Pointer data);

        static native int GetWindowThreadProcessId(Pointer hwnd, int[] pid);

        static native int GetClassNameW(Pointer hwnd, char[] name, int length);

        static native boolean IsWindowVisible(Pointer hwnd);

        static native boolean IsWindow(Pointer hwnd);

        static native int GetWindowLongW(Pointer hwnd, int index);

        static native int SetWindowLongW(Pointer hwnd, int index, int value);

        static native boolean SetWindowPos(Pointer hwnd, Pointer after, int x, int y, int width, int height, int flags);
    }

    /** The kernel32 functions, whose Java names must be the Win32 names for direct mapping. */
    @SuppressWarnings("checkstyle:methodname")
    private static final class Kernel32 {
        static {
            Native.register(Kernel32.class, NativeLibrary.getInstance("kernel32", stdcall()));
        }

        private Kernel32() {}

        static native int GetCurrentProcessId();
    }
}
