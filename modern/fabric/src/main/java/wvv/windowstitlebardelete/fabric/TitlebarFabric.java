package wvv.windowstitlebardelete.fabric;

import net.fabricmc.api.ClientModInitializer;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Starts the mod on Fabric and Quilt. */
public final class TitlebarFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModernHooks.bootstrap("Fabric");
    }
}
