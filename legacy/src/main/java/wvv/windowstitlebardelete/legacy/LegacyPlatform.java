package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wvv.windowstitlebardelete.core.Platform;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;

final class LegacyPlatform implements Platform {
    private static final Logger LOG = LogManager.getLogger(Titlebar.MOD_NAME);

    @Override public String describe() { return "Forge " + Loader.MC_VERSION; }

    @Override public File configDir() { return Loader.instance().getConfigDir(); }

    @Override public void info(String message) { LOG.info(message); }

    @Override public void warn(String message, Throwable error) { LOG.warn(message, error); }

    @Override public boolean isFullscreen() { return Minecraft.getMinecraft().isFullScreen(); }

    @Override public Harness.Host harnessHost() { return new LegacyHarnessHost(); }
}
