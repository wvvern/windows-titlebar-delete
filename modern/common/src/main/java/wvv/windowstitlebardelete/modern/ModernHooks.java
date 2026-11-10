package wvv.windowstitlebardelete.modern;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import wvv.windowstitlebardelete.core.Titlebar;

import java.io.File;
import java.util.Arrays;

/** The loader-independent entry points that the loader adapters and the mixins call. */
public final class ModernHooks {
    private static KeyMapping key;

    private ModernHooks() {}

    /** Starts the mod on the client with the name of the loader for the logs and the config folder that the loader gives. */
    public static void bootstrap(String loader, File configDir) {
        Titlebar.init(new ModernPlatform(loader, configDir));
    }

    /** Runs at the start of every client tick, also on the title screen. */
    public static void tick() {
        while (key().consumeClick()) Titlebar.toggle();
        Titlebar.tick();
    }

    /** The key bindings of the game with the toggle key, which the options then load, save and show in Controls. */
    public static KeyMapping[] withKey(KeyMapping[] keys) {
        if (Arrays.asList(keys).contains(key())) return keys;
        KeyMapping[] all = Arrays.copyOf(keys, keys.length + 1);
        all[keys.length] = key();
        return all;
    }

    /** The toggle key, made on first use because the game makes its key bindings only when it makes the options. */
    static KeyMapping key() {
        if (key == null) {
            // The key name gives the GLFW code up to 1.21.11 and the SDL scancode on 26.x.
            int f12 = InputConstants.getKey("key.keyboard.f12").getValue();
            //? if >=1.21.9 {
            key = new KeyMapping(Titlebar.KEY_NAME, f12, KeyMapping.Category.MISC);
            //?} else {
            /*key = new KeyMapping(Titlebar.KEY_NAME, f12, "key.categories.misc");
            *///?}
        }
        return key;
    }
}
