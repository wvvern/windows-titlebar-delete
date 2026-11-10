package wvv.windowstitlebardelete.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
//? if <1.20.5 {
/*import net.neoforged.fml.loading.FMLEnvironment;
*///?}
import net.neoforged.fml.loading.FMLPaths;
import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on NeoForge; a dedicated server loads it and does nothing. */
//? if >=1.20.5 {
@Mod(value = Titlebar.MOD_ID, dist = Dist.CLIENT)
//?} else {
/*@Mod(Titlebar.MOD_ID)
*///?}
public final class TitlebarNeoForge {
    public TitlebarNeoForge() {
        //? if <1.20.5 {
        /*if (FMLEnvironment.dist != Dist.CLIENT) return;
        *///?}
        ModernHooks.bootstrap("NeoForge", FMLPaths.CONFIGDIR.get().toFile());
    }
}
