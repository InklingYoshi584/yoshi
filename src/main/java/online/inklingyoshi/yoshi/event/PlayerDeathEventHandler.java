package online.inklingyoshi.yoshi.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import online.inklingyoshi.yoshi.util.GoldenEggManager;
import online.inklingyoshi.yoshi.Yoshi;

public class PlayerDeathEventHandler {
    
    public static void register() {
        // Handle golden egg breaking after respawn
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Yoshi.LOGGER.info("AFTER_RESPAWN event triggered for player: " + newPlayer.getName().getString());
            
            // Check if player has a golden egg respawn point
            BlockPos spawnPos = newPlayer.getSpawnPointPosition();
            if (spawnPos != null) {
                Yoshi.LOGGER.info("Player has spawn point at: " + spawnPos);
                
                // Check if the spawn point is a golden egg
                if (newPlayer.getServerWorld().getBlockState(spawnPos).getBlock() instanceof online.inklingyoshi.yoshi.block.GoldenEggBlock) {
                    Yoshi.LOGGER.info("Spawn point is a Golden Egg! Breaking it...");
                    
                    // Handle the golden egg respawn logic
                    GoldenEggManager.handleGoldenEggRespawn(newPlayer.getServerWorld(), newPlayer, spawnPos);
                } else {
                    Yoshi.LOGGER.info("Spawn point is not a Golden Egg");
                }
            } else {
                Yoshi.LOGGER.info("Player has no spawn point");
            }
        });
    }
}