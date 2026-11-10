package wvv.windowstitlebardelete.modern;

import net.minecraft.client.Minecraft;
import wvv.windowstitlebardelete.core.dev.Harness;

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
