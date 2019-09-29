package grondag.instruments.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.instruments.ChunkRebuildCounters;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Inject(method = "reload", at = @At("HEAD"), cancellable = false, require = 1)
    private void onReload(CallbackInfo ci) {
        ChunkRebuildCounters.reset();
    }
}