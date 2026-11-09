package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/** Minecraft 1.12.2 (MCP stable_39) names that differ between legacy versions. */
final class McCompat {
    static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2]";

    private McCompat() {}

    static FontRenderer fontRenderer() { return Minecraft.getMinecraft().fontRenderer; }
}
