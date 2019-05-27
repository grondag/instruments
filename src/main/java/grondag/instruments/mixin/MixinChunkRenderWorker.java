package grondag.instruments.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import grondag.instruments.ConcurrentPerformanceCounter;
import grondag.instruments.Instruments;
import net.minecraft.client.render.chunk.ChunkRenderTask;
import net.minecraft.client.render.chunk.ChunkRenderWorker;
import net.minecraft.client.render.chunk.ChunkRenderer;

@Mixin(ChunkRenderWorker.class)
public class MixinChunkRenderWorker {
    private static final ConcurrentPerformanceCounter counter = new ConcurrentPerformanceCounter();

    @Redirect(method = "runTask", require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRenderer;rebuildChunk(FFFLnet/minecraft/client/render/chunk/ChunkRenderTask;)V"))
    private void timeChunkRebuild(ChunkRenderer chunk, float x, float y, float z, ChunkRenderTask task) {
        final long start = counter.startRun();

        chunk.rebuildChunk(x, y, z, task);

        counter.endRun(start);
        int total = counter.addCount(1);
        if(total == 2000) {
            Instruments.LOG.info("ChunkRenderer.chunkRebuild " + counter.stats());
            counter.clearStats();
        }
    }
}
