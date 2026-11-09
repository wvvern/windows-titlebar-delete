package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

/** Gives the Config button of the Forge 1.8.9 mod list the settings screen. */
public final class GuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft mc) {}

    @Override public Class<? extends GuiScreen> mainConfigGuiClass() { return GuiSettings.class; }

    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }

    @Override public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }
}
