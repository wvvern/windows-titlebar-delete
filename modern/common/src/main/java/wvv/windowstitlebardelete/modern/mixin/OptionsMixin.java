package wvv.windowstitlebardelete.modern.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Adds the toggle key to the key bindings before the game reads options.txt, without any loader key API. */
@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow @Final @Mutable public KeyMapping[] keyMappings;

    @Inject(method = "load()V", at = @At("HEAD"))
    private void wtd$addKey(CallbackInfo ci) {
        keyMappings = ModernHooks.withKey(keyMappings);
    }
}
