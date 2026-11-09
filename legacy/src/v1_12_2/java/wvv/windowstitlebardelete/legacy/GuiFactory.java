package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

/** Gives the Config button of the Forge 1.12.2 mod list the settings screen. */
public final class GuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft mc) {}

    @Override public boolean hasConfigGui() { return true; }

    @Override public GuiScreen createConfigGui(GuiScreen parent) { return new GuiSettings(parent); }

    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
}
