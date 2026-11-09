package wvv.windowstitlebardelete.modern;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;
import java.io.IOException;

/** What the development test harness needs from Minecraft 1.16.5 and later. */
final class ModernHarnessHost implements Harness.Host {
    @Override
    public void setFullscreen(boolean on) {
        //? if >=26.3 {
        Minecraft.getInstance().options.fullscreen().set(on);
        //?} else {
        /*com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
        if (window.isFullscreen() != on) window.toggleFullScreen();
        *///?}
    }

    @Override
    public void screenshot(File png) {
        Minecraft mc = Minecraft.getInstance();
        //? if >=26.2 {
        RenderTarget target = mc.gameRenderer.mainRenderTarget();
        //?} else {
        /*RenderTarget target = mc.getMainRenderTarget();
        *///?}
        //? if >=1.21.5 {
        Screenshot.takeScreenshot(target, image -> save(image, png));
        //?} elif >=1.17 {
        /*save(Screenshot.takeScreenshot(target), png);
        *///?} else {
        /*save(Screenshot.takeScreenshot(mc.getWindow().getWidth(), mc.getWindow().getHeight(), target), png);
        *///?}
    }

    private static void save(NativeImage image, File png) {
        try (NativeImage owned = image) {
            owned.writeToFile(png);
            Harness.log("saved " + png.getName());
        } catch (IOException e) {
            Harness.log("! screenshot failed: " + e);
        }
    }

    @Override public void closeScreen() { ModernPlatform.setScreen(null); }

    @Override
    public void quit() {
        // The dev run of Architectury keeps thread pools that are not daemons, so the JVM must stop when the game thread ends.
        Thread game = Thread.currentThread();
        Thread exit = new Thread(() -> {
            try {
                game.join();
            } catch (InterruptedException e) {
                return;
            }
            System.exit(0);
        }, "wtd-harness-exit");
        exit.setDaemon(true);
        exit.start();
        Minecraft.getInstance().stop();
    }
}
