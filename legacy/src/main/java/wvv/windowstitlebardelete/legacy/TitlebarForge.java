package wvv.windowstitlebardelete.legacy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import wvv.windowstitlebardelete.core.Titlebar;

/** Forge entry point shared by every legacy Minecraft version. */
@Mod(modid = Titlebar.MOD_ID, name = Titlebar.MOD_NAME, version = TitlebarForge.VERSION, clientSideOnly = true,
        acceptedMinecraftVersions = McCompat.ACCEPTED_MINECRAFT_VERSIONS, guiFactory = "wvv.windowstitlebardelete.legacy.GuiFactory")
public class TitlebarForge {
    public static final String VERSION = "2.0.0";

    /** The toggle key, which the settings screen names. */
    static KeyBinding key;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Titlebar.init(new LegacyPlatform());
        key = new KeyBinding(Titlebar.KEY_NAME, Keyboard.KEY_F12, "key.categories.misc");
        ClientRegistry.registerKeyBinding(key);
        // The FML bus gets the tick events on 1.8.9 and is the Forge bus on 1.12.2.
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (key.isPressed()) Titlebar.toggle();
        Titlebar.tick();
    }
}
