package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/** Minecraft 1.8.9 (MCP stable_22) names that differ between legacy versions. */
final class McCompat {
    static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.8.9]";

    private McCompat() {
    }

    static boolean isFullscreen() {
        return Minecraft.getMinecraft().isFullScreen();
    }

    static FontRenderer fontRenderer() {
        return Minecraft.getMinecraft().fontRendererObj;
    }
}
