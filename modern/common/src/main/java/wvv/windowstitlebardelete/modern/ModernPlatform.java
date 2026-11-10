package wvv.windowstitlebardelete.modern;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wvv.windowstitlebardelete.core.Platform;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;

/** The core port on Minecraft 1.16.5 and later, for all loaders. */
final class ModernPlatform implements Platform {
    private static final String MINECRAFT = /*$ minecraft*/ "26.3";
    private static final Logger LOG = LogManager.getLogger(Titlebar.MOD_NAME);

    private final String loader;
    private final File configDir;

    ModernPlatform(String loader, File configDir) {
        this.loader = loader;
        this.configDir = configDir;
    }

    @Override public String describe() { return loader + " " + MINECRAFT; }

    @Override public File configDir() { return configDir; }

    @Override public void info(String message) { LOG.info(message); }

    @Override public void warn(String message, Throwable error) { LOG.warn(message, error); }

    @Override
    public boolean isFullscreen() {
        //? if >=26.3 {
        return Minecraft.getInstance().options.fullscreen().get();
        //?} else {
        /*return Minecraft.getInstance().getWindow().isFullscreen();
        *///?}
    }

    @Override public Harness.Host harnessHost() { return new ModernHarnessHost(); }
}
