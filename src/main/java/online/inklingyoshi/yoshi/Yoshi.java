package online.inklingyoshi.yoshi;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import online.inklingyoshi.yoshi.network.ModPackets;
import online.inklingyoshi.yoshi.server.ServerTickHandler;

public class Yoshi implements ModInitializer {
    public static final String MOD_ID = "yoshi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModPackets.registerPackets();
        ServerTickHandler.register();
    }
}
