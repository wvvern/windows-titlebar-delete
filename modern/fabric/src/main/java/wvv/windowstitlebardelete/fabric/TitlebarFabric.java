package wvv.windowstitlebardelete.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on Fabric and Quilt. */
public final class TitlebarFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModernHooks.bootstrap("Fabric", FabricLoader.getInstance().getConfigDir().toFile());
    }
}
