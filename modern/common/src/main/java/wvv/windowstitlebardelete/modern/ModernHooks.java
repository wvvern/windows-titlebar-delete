package wvv.windowstitlebardelete.modern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The loader-independent entry points that the loader adapters and mixins call. */
public final class ModernHooks {
    private static final Logger LOGGER = LogManager.getLogger("Windows Titlebar Delete");

    private ModernHooks() {
    }

    /** Starts the mod on the client. */
    public static void bootstrap() {
        LOGGER.info("Windows Titlebar Delete loaded");
    }

    /** Runs at the start of every client tick. */
    public static void tick() {
    }
}
