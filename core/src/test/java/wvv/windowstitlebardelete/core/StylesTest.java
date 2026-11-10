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
        assertEquals(OTHER | WS_THICKFRAME | WS_MAXIMIZEBOX, Styles.hidden(OTHER | ALL, true));
    }

    @Test
    void hiddenWithoutBorderKeepsOnlyMaximizeBox() {
        assertEquals(OTHER | WS_MAXIMIZEBOX, Styles.hidden(OTHER | ALL, false));
    }

    @Test
    void shownSetsAllTitlebarBits() {
        assertEquals(OTHER | ALL, Styles.shown(OTHER));
        assertEquals(FakeWindow.NORMAL, Styles.shown(Styles.hidden(FakeWindow.NORMAL, false)));
    }

    @Test
    void popupWindowKeepsItsStyle() {
        for (int popup : new int[]{Styles.WS_POPUP | OTHER, Styles.WS_POPUP | OTHER | ALL}) {
            assertEquals(popup, Styles.hidden(popup, true));
            assertEquals(popup, Styles.hidden(popup, false));
            assertEquals(popup, Styles.shown(popup));
        }
    }

    @Test
    void isIdempotentAndKeepsOtherBits() {
        int[] styles = {0, OTHER, OTHER | ALL, OTHER | WS_CAPTION, ~0, ~ALL, FakeWindow.NORMAL};
        for (int style : styles) {
            int[] results = {Styles.hidden(style, true), Styles.hidden(style, false), Styles.shown(style)};
            assertEquals(results[0], Styles.hidden(results[0], true));
            assertEquals(results[1], Styles.hidden(results[1], false));
            assertEquals(results[2], Styles.shown(results[2]));
            for (int result : results) assertEquals(style & ~ALL, result & ~ALL);
        }
    }
}
