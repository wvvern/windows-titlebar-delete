package wvv.windowstitlebardelete.forge;

import net.minecraftforge.api.distmarker.Dist;
//? if >=1.19 {
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
//?} elif >=1.18 {
/*import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import static net.minecraftforge.network.NetworkConstants.IGNORESERVERONLY;
*///?} elif >=1.17 {
/*import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import static net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY;
*///?} else {
/*import net.minecraftforge.fml.ExtensionPoint;
import org.apache.commons.lang3.tuple.Pair;
import static net.minecraftforge.fml.network.FMLNetworkConstants.IGNORESERVERONLY;
*///?}
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import wvv.windowstitlebardelete.modern.ModernHooks;
import wvv.windowstitlebardelete.modern.SettingsScreen;

/** Starts the mod on Forge and gives it a "Config" button in the mod list; a dedicated server loads it and does nothing. */
@Mod("windowstitlebardelete")
public final class TitlebarForge {
    public TitlebarForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) client();
    }

    private static void client() {
        ModernHooks.bootstrap("Forge", FMLPaths.CONFIGDIR.get().toFile());
        ModLoadingContext context = ModLoadingContext.get();
        // Forge before 1.19 does not read displayTest in mods.toml, so the display test here keeps a server that does not have the mod compatible.
        //? if >=1.19 {
        context.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((mc, parent) -> new SettingsScreen(parent)));
        //?} elif >=1.17 {
        /*context.registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((mc, parent) -> new SettingsScreen(parent)));
        context.registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> IGNORESERVERONLY, (version, network) -> true));
        *///?} else {
        /*context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new SettingsScreen(parent));
        context.registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> IGNORESERVERONLY, (version, network) -> true));
        *///?}
    }
}
