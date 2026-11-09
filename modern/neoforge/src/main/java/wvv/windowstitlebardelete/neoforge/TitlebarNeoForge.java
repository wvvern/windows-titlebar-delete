package wvv.windowstitlebardelete.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
//? if >=1.20.5 {
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
//?} else {
/*import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory;
*///?}
import wvv.windowstitlebardelete.modern.ModernHooks;
import wvv.windowstitlebardelete.modern.SettingsScreen;

/** Starts the mod on NeoForge and gives it a "Config" button in the mod list; a dedicated server loads it and does nothing. */
//? if >=1.20.5 {
@Mod(value = "windowstitlebardelete", dist = Dist.CLIENT)
//?} else {
/*@Mod("windowstitlebardelete")
*///?}
public final class TitlebarNeoForge {
    public TitlebarNeoForge(ModContainer container) {
        //? if >=1.20.5 {
        ModernHooks.bootstrap("NeoForge");
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new SettingsScreen(parent));
        //?} else {
        /*if (FMLEnvironment.dist == Dist.CLIENT) client(container);
        *///?}
    }

    //? if <1.20.5 {
    /*private static void client(ModContainer container) {
        ModernHooks.bootstrap("NeoForge");
        container.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((mc, parent) -> new SettingsScreen(parent)));
    }
    *///?}
}
