package wvv.windowstitlebardelete.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static wvv.windowstitlebardelete.core.Styles.WS_CAPTION;
import static wvv.windowstitlebardelete.core.Styles.WS_MAXIMIZEBOX;
import static wvv.windowstitlebardelete.core.Styles.WS_MINIMIZEBOX;
import static wvv.windowstitlebardelete.core.Styles.WS_SYSMENU;
import static wvv.windowstitlebardelete.core.Styles.WS_THICKFRAME;

class StylesTest {
    private static final int ALL = WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_THICKFRAME;
    private static final int OTHER = 0x16000000 | 0x00000004;

    @Test
    void hiddenWithBorderKeepsThickFrameAndMaximizeBox() {
        assertEquals(OTHER | WS_THICKFRAME | WS_MAXIMIZEBOX, Styles.wanted(OTHER | ALL, true, true));
    }

    @Test
    void hiddenWithoutBorderKeepsOnlyMaximizeBox() {
        assertEquals(OTHER | WS_MAXIMIZEBOX, Styles.wanted(OTHER | ALL, true, false));
    }

    @Test
    void shownSetsAllTitlebarBits() {
        assertEquals(OTHER | ALL, Styles.wanted(OTHER, false, true));
        assertEquals(OTHER | ALL, Styles.wanted(OTHER, false, false));
    }

    @Test
    void isIdempotentAndKeepsOtherBits() {
        int[] styles = {0, OTHER, OTHER | ALL, OTHER | WS_CAPTION, ~0, ~ALL, FakeWindow.NORMAL};
        for (int style : styles) {
            for (int combo = 0; combo < 4; combo++) {
                boolean hidden = (combo & 1) != 0, keep = (combo & 2) != 0;
                int once = Styles.wanted(style, hidden, keep);
                assertEquals(once, Styles.wanted(once, hidden, keep));
                assertEquals(style & ~ALL, once & ~ALL);
            }
        }
    }
}
