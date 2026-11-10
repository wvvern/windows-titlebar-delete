package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import wvv.windowstitlebardelete.core.dev.Harness;

final class LegacyHarnessHost implements Harness.Host {
    @Override
    public void setFullscreen(boolean on) {
        throw new UnsupportedOperationException("LWJGL 2 fullscreen changes the display mode of the monitor");
    }

    @Override public void quit() { Minecraft.getMinecraft().shutdown(); }
}
