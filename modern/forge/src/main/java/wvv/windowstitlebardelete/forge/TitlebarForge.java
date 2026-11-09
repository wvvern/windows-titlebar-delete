package wvv.windowstitlebardelete.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on Forge; a dedicated server loads it and does nothing. */
@Mod("windowstitlebardelete")
public final class TitlebarForge {
    public TitlebarForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModernHooks.bootstrap();
        }
    }
}
