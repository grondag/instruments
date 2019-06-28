package grondag.instruments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.InvalidateRenderStateCallback;

public class Instruments implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InvalidateRenderStateCallback.EVENT.register(ChunkRebuildCounters::reset);
    }
    
    public static final String MODID = "instruments";
    
    public static final Logger LOG = LogManager.getLogger("Instruments");
}
