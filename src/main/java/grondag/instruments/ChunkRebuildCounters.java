package grondag.instruments;

import java.util.concurrent.atomic.AtomicInteger;

public class ChunkRebuildCounters {
    public final ConcurrentPerformanceCounter counter = new ConcurrentPerformanceCounter();
    public final AtomicInteger blockCounter = new AtomicInteger();
    public final AtomicInteger fluidCounter = new AtomicInteger();
}