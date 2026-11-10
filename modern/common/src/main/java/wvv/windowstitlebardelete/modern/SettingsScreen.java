package wvv.windowstitlebardelete.modern;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.ui.Canvas;
import wvv.windowstitlebardelete.core.ui.SettingsEditor;

/** Hosts the core {@link SettingsEditor} on Minecraft 1.16.5 and later. */
public final class SettingsScreen extends Screen implements Canvas {
    private final Screen parent;
    private final SettingsEditor editor = new SettingsEditor(ModernHooks.key().getTranslatedKeyMessage().getString());
    /** The drawing target of the frame that the game draws now. */
    //? if >=26.1 {
    private net.minecraft.client.gui.GuiGraphicsExtractor target;
    //?} elif >=1.20 {
    /*private net.minecraft.client.gui.GuiGraphics target;
    *///?} else {
    /*private com.mojang.blaze3d.vertex.PoseStack target;
    *///?}

    /** The config buttons of Mod Menu, Forge and NeoForge give the screen that they return to. */
    public SettingsScreen(Screen parent) {
        super(Component.nullToEmpty(Titlebar.MOD_NAME));
        this.parent = parent;
    }

    @Override
    //? if >=26.1 {
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    //?} elif >=1.20.2 {
    /*public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
    *///?} elif >=1.20 {
    /*public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
    *///?} else {
    /*public void render(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
    *///?}
        target = graphics;
        editor.render(this, mouseX, mouseY);
    }

    @Override
    public void tick() {
        if (editor.done()) onClose();
    }

    @Override
    //? if >=1.21.9 {
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        editor.mouseClicked((int) event.x(), (int) event.y());
    //?} else {
    /*public boolean mouseClicked(double x, double y, int button) {
        editor.mouseClicked((int) x, (int) y);
    *///?}
        return true;
    }

    /** The game calls this for the Escape key. */
    @Override
    public void onClose() {
        ModernPlatform.setScreen(parent);
    }

    @Override
    public void removed() {
        editor.close();
    }

    @Override public int width() { return width; }

    @Override public int height() { return height; }

    @Override
    public void fill(int x, int y, int w, int h, int argb) {
        //? if >=1.20 {
        target.fill(x, y, x + w, y + h, argb);
        //?} else {
        /*fill(target, x, y, x + w, y + h, argb);
        *///?}
    }

    @Override
    public void text(String text, int x, int y, int argb) {
        //? if >=26.1 {
        target.text(font, text, x, y, argb, false);
        //?} elif >=1.20 {
        /*target.drawString(font, text, x, y, argb, false);
        *///?} else {
        /*font.draw(target, text, x, y, argb);
        *///?}
    }

    @Override public int textWidth(String text) { return font.width(text); }
}
