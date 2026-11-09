package wvv.windowstitlebardelete.modern.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wvv.windowstitlebardelete.modern.ModernHooks;

/** Client tick without any loader event API. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void wtd$tick(CallbackInfo ci) {
        ModernHooks.tick();
    }
}
