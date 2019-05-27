package grondag.instruments.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import grondag.instruments.ChunkRebuildCounters;
import grondag.instruments.Instruments;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkRenderTask;
import net.minecraft.client.render.chunk.ChunkRenderWorker;
import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Mixin(ChunkRenderWorker.class)
public class MixinChunkRenderWorker {
    
    private static volatile ChunkRebuildCounters counters = new ChunkRebuildCounters();
    
    @Redirect(method = "runTask", require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRenderer;rebuildChunk(FFFLnet/minecraft/client/render/chunk/ChunkRenderTask;)V"))
    private void timeChunkRebuild(ChunkRenderer chunk, float x, float y, float z, ChunkRenderTask task) {
        
        final ChunkRebuildCounters counts = counters;
        
        final World world = chunk.getWorld();
        final BlockPos origin = chunk.getOrigin();
        if(world == null || origin == null) {
            chunk.rebuildChunk(x, y, z, task);
            return;
        }
        
        final Chunk worldChunk = world.getChunk(origin);
        if(worldChunk == null) {
            chunk.rebuildChunk(x, y, z, task);
            return;
        }
        
        int blockCount = 0;
        int fluidCount = 0;
        
        Iterator<BlockPos> it = BlockPos.iterate(origin, origin.add(15, 15, 15)).iterator();
        while(it.hasNext()) {
            BlockState state = worldChunk.getBlockState(it.next());
            if(state.getBlock().getRenderType(state) == BlockRenderType.MODEL) {
                blockCount++;
            }
            if(!state.getFluidState().isEmpty()) {
                fluidCount++;
            }
        }
       
        
        final long start = counts.counter.startRun();

        chunk.rebuildChunk(x, y, z, task);

        final long nanos = counts.counter.endRun(start);
        final int chunkCount = counts.counter.addCount(1);
        blockCount = counts.blockCounter.addAndGet(blockCount);
        fluidCount = counts.fluidCounter.addAndGet(fluidCount);
        
        if(chunkCount == 2000) {
            counters = new ChunkRebuildCounters();
            
            int total = blockCount + fluidCount;
            Instruments.LOG.info(String.format("ChunkRenderer.chunkRebuild elapsed time per chunk for last 2000 chunks = %,dns", nanos / 2000));
            
            Instruments.LOG.info(String.format("Time per fluid/block = %,dns  Count = %,d  fluid:block ratio = %d:%d",
                    Math.round((double)nanos / total), total, 
                    Math.round(fluidCount * 100f / total), Math.round(blockCount * 100f / total)));
            
            Instruments.LOG.info("");
        }
    }
}
