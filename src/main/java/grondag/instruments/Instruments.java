package grondag.instruments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;

public class Instruments implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // nothing yet
    }
    
    public static final String MODID = "instruments";
    
    public static final Logger LOG = LogManager.getLogger("Instruments");
}
