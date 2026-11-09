package wvv.windowstitlebardelete.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
//? if <1.20.5
/*import net.neoforged.fml.loading.FMLEnvironment;*/
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on NeoForge; a dedicated server loads it and does nothing. */
//? if >=1.20.5 {
@Mod(value = "windowstitlebardelete", dist = Dist.CLIENT)
public final class TitlebarNeoForge {
    public TitlebarNeoForge() {
        ModernHooks.bootstrap();
    }
}
//?} else {
/*@Mod("windowstitlebardelete")
public final class TitlebarNeoForge {
    public TitlebarNeoForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModernHooks.bootstrap();
        }
    }
}
*///?}
