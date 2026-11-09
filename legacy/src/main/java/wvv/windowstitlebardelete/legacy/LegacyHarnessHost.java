package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;
import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;

/** What the development test harness needs from Forge 1.8.9 and 1.12.2. */
final class LegacyHarnessHost implements Harness.Host {
    @Override
    public void setFullscreen(boolean on) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isFullScreen() != on) mc.toggleFullscreen();
    }

    @Override
    public void screenshot(File png) {
        Minecraft mc = Minecraft.getMinecraft();
        // The game writes <dir>/screenshots/<name>, so the file moves next to the other harness output.
        File dir = png.getParentFile();
        ScreenShotHelper.saveScreenshot(dir, png.getName(), mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        File folder = new File(dir, "screenshots");
        File written = new File(folder, png.getName());
        png.delete();
        boolean moved = written.renameTo(png);
        folder.delete();
        Harness.log(moved ? "saved " + png.getName() : "! screenshot failed: " + png.getName());
    }

    @Override public void closeScreen() { Minecraft.getMinecraft().displayGuiScreen(null); }

    @Override public void quit() { Minecraft.getMinecraft().shutdown(); }
}
