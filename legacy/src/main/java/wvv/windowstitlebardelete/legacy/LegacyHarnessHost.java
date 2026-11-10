package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import wvv.windowstitlebardelete.core.dev.Harness;

/** What the development test harness needs from Forge 1.8.9 and 1.12.2. */
final class LegacyHarnessHost implements Harness.Host {
    @Override
    public void setFullscreen(boolean on) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isFullScreen() != on) mc.toggleFullscreen();
    }

    @Override public void quit() { Minecraft.getMinecraft().shutdown(); }
}
