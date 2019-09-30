package grondag.instruments.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import grondag.instruments.ChunkRebuildCounters;
import grondag.instruments.Instruments;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkBatcher.ChunkRenderer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBatcher$ChunkRenderer$class_4578")
public class MixinChunkBatcher {
	@Shadow protected ChunkRendererRegion field_20838;

	@Shadow protected ChunkRenderer field_20839;

	private long start;
	private ChunkRendererRegion world;

	@SuppressWarnings("rawtypes")
	@Inject(method = "method_22785", require = 1, at = @At(value = "HEAD"))
	private void timeChunkRebuildStart(CallbackInfoReturnable ci) {
		if (field_20838 == null) return;

		this.world = field_20838;
		start = ChunkRebuildCounters.get().counter.startRun();
	}

	@SuppressWarnings("rawtypes")
	@Inject(method = "method_22785", require = 1, at = @At(value = "RETURN"))
	private void timeChunkRebuildEnd(CallbackInfoReturnable ci) {
		final ChunkRendererRegion world = this.world;
		if (world == null) return;

		final ChunkRebuildCounters counts = ChunkRebuildCounters.get();
		final long nanos = counts.counter.endRun(start);

		int blockCount = 0;
		int fluidCount = 0;

		Iterator<BlockPos> it = BlockPos.iterate(field_20839.getOrigin(), field_20839.getOrigin().add(15, 15, 15)).iterator();
		while(it.hasNext()) {
			BlockState state = world.getBlockState(it.next());
			if(state.getBlock().getRenderType(state) == BlockRenderType.MODEL) {
				blockCount++;
			}
			if(!state.getFluidState().isEmpty()) {
				fluidCount++;
			}
		}

		final int chunkCount = counts.counter.addCount(1);
		blockCount = counts.blockCounter.addAndGet(blockCount);
		fluidCount = counts.fluidCounter.addAndGet(fluidCount);

		if(chunkCount == 2000) {
			ChunkRebuildCounters.reset();

			int total = blockCount + fluidCount;
			Instruments.LOG.info(String.format("ChunkRenderer.chunkRebuild elapsed time per chunk for last 2000 chunks = %,dns", nanos / 2000));

			Instruments.LOG.info(String.format("Time per fluid/block = %,dns  Count = %,d  fluid:block ratio = %d:%d",
					Math.round((double)nanos / total), total, 
					Math.round(fluidCount * 100f / total), Math.round(blockCount * 100f / total)));

			Instruments.LOG.info("");
		}

		this.world = null;
	}
}
