package wvv.windowstitlebardelete.core.win32;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JnaWindowTest {
    /** The natives link, and a process with no game window finds none, with no window opened and no window changed. */
    @Test
    void findsNoWindowInATestProcess() {
        assumeTrue(System.getProperty("os.name", "").startsWith("Windows"), "only on Windows");
        assertFalse(new JnaWindow().found());
    }
}
