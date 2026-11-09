package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import wvv.windowstitlebardelete.core.ui.Canvas;
import wvv.windowstitlebardelete.core.ui.SettingsEditor;

/** Hosts the core {@link SettingsEditor} on Forge 1.8.9 and 1.12.2. */
public final class GuiSettings extends GuiScreen implements Canvas {
    /** The LWJGL 2 number of the left mouse button. */
    private static final int LEFT_BUTTON = 0;

    private final GuiScreen parent;
    private final SettingsEditor editor = new SettingsEditor(GameSettings.getKeyDisplayString(TitlebarForge.key.getKeyCode()));

    /** The Forge 1.8.9 mod list calls this constructor by reflection. */
    public GuiSettings(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        editor.render(this, mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        if (editor.done()) mc.displayGuiScreen(parent);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        if (button == LEFT_BUTTON) editor.mouseClicked(x, y);
    }

    @Override
    protected void keyTyped(char c, int code) {
        if (code == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }

    @Override
    public void onGuiClosed() {
        editor.close();
    }

    @Override public int width() { return width; }

    @Override public int height() { return height; }

    @Override public void fill(int x, int y, int w, int h, int argb) { Gui.drawRect(x, y, x + w, y + h, argb); }

    @Override public void text(String text, int x, int y, int argb) { McCompat.fontRenderer().drawString(text, x, y, argb); }

    @Override public int textWidth(String text) { return McCompat.fontRenderer().getStringWidth(text); }
}
