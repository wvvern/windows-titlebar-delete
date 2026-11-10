package wvv.windowstitlebardelete.forge;

import net.minecraftforge.api.distmarker.Dist;
//? if <1.17 {
/*import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import static net.minecraftforge.fml.network.FMLNetworkConstants.IGNORESERVERONLY;
*///?} elif <1.18 {
/*import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import static net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY;
*///?} elif <1.19 {
/*import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import static net.minecraftforge.network.NetworkConstants.IGNORESERVERONLY;
*///?}
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on Forge; a dedicated server loads it and does nothing. */
@Mod("windowstitlebardelete")
public final class TitlebarForge {
    public TitlebarForge() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        ModernHooks.bootstrap("Forge", FMLPaths.CONFIGDIR.get().toFile());
        // Forge before 1.19 does not read displayTest in mods.toml, so the display test here keeps a server that does not have the mod compatible.
        //? if <1.17 {
        /*ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> IGNORESERVERONLY, (version, network) -> true));
        *///?} elif <1.19 {
        /*ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> IGNORESERVERONLY, (version, network) -> true));
        *///?}
    }
}
