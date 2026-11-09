package wvv.windowstitlebardelete.forge;

import net.minecraftforge.api.distmarker.Dist;
//? if >=1.19 {
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
//?} elif >=1.18 {
/*import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
*///?} elif >=1.17 {
/*import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;
*///?} else {
/*import net.minecraftforge.fml.ExtensionPoint;
*///?}
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import wvv.windowstitlebardelete.modern.ModernHooks;
import wvv.windowstitlebardelete.modern.SettingsScreen;

/** Starts the mod on Forge and gives it a "Config" button in the mod list; a dedicated server loads it and does nothing. */
@Mod("windowstitlebardelete")
public final class TitlebarForge {
    public TitlebarForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) client();
    }

    private static void client() {
        ModernHooks.bootstrap("Forge");
        ModLoadingContext context = ModLoadingContext.get();
        //? if >=1.19 {
        context.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((mc, parent) -> new SettingsScreen(parent)));
        //?} elif >=1.17 {
        /*context.registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((mc, parent) -> new SettingsScreen(parent)));
        *///?} else {
        /*context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new SettingsScreen(parent));
        *///?}
    }
}
