package wvv.windowstitlebardelete.legacy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Forge entry point shared by every legacy Minecraft version. */
@Mod(modid = TitlebarForge.MOD_ID, name = TitlebarForge.NAME, version = TitlebarForge.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = McCompat.ACCEPTED_MINECRAFT_VERSIONS)
public class TitlebarForge {
    public static final String MOD_ID = "windowstitlebardelete";
    public static final String NAME = "Windows Titlebar Delete";
    public static final String VERSION = "2.0.0";

    private static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("{} {} loaded, fullscreen {}", NAME, VERSION, McCompat.isFullscreen());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick();
        }
    }

    private void tick() {
    }
}
