package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.block.GoldenEggBlock;
import online.inklingyoshi.yoshi.item.YoshiItems;

import java.util.HashMap;
import java.util.Map;

public class GoldenEggManager {
    
    // Track respawn points for each player
    private static final Map<ServerPlayerEntity, BlockPos> respawnPoints = new HashMap<>();
    
    // Track golden apple consumption timers
    private static final Map<ServerPlayerEntity, Long> goldenAppleTimers = new HashMap<>();
    
    // Duration for golden apple effect (5 minutes in ticks)
    private static final int GOLDEN_APPLE_DURATION = 20 * 60 * 5; // 5 minutes
    
    public static boolean canPlayerBreakGoldenEgg(PlayerEntity player) {
        // Only Yoshi players with active golden apple effect can break golden eggs
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return false;
        }
        
        return hasActiveGoldenAppleEffect(serverPlayer);
    }
    
    public static void setRespawnPoint(ServerPlayerEntity player, BlockPos pos) {
        respawnPoints.put(player, pos);
        
        // Save to player data for persistence
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        nbt.putLong("yoshi_golden_egg_pos", pos.asLong());
        player.readNbt(nbt);
        
    }
    
    public static void removeRespawnPoint(ServerPlayerEntity player, BlockPos pos) {
        respawnPoints.remove(player);
        
        // Remove from player data
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        nbt.remove("yoshi_golden_egg_pos");
        player.readNbt(nbt);
    }
    
    public static BlockPos getRespawnPoint(ServerPlayerEntity player) {
        // Check current map first
        if (respawnPoints.containsKey(player)) {
            return respawnPoints.get(player);
        }
        
        // Check player data for persistence
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        if (nbt.contains("yoshi_golden_egg_pos")) {
            long posLong = nbt.getLong("yoshi_golden_egg_pos");
            return BlockPos.fromLong(posLong);
        }
        
        return null;
    }
    
    public static boolean hasActiveGoldenAppleEffect(ServerPlayerEntity player) {
        if (!goldenAppleTimers.containsKey(player)) {
            return false;
        }
        
        long startTime = goldenAppleTimers.get(player);
        long currentTime = player.getWorld().getTime();
        
        return (currentTime - startTime) <= GOLDEN_APPLE_DURATION;
    }
    
    public static void startGoldenAppleTimer(ServerPlayerEntity player) {
        goldenAppleTimers.put(player, player.getWorld().getTime());
        
        // Save to player data for persistence
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        nbt.putLong("yoshi_golden_apple_start", player.getWorld().getTime());
        player.readNbt(nbt);
        
    }
    
    public static void onPlayerTick(ServerPlayerEntity player) {
        // Check if golden apple effect has expired
        if (goldenAppleTimers.containsKey(player)) {
            long startTime = goldenAppleTimers.get(player);
            long currentTime = player.getWorld().getTime();
            
            if ((currentTime - startTime) > GOLDEN_APPLE_DURATION) {
                goldenAppleTimers.remove(player);
                
                // Remove from player data
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                nbt.remove("yoshi_golden_apple_start");
                player.readNbt(nbt);
                
                // Give the player a golden egg as a reward
                if (player.isAlive()) {
                    player.giveItemStack(new ItemStack(YoshiItems.GOLDEN_EGG));
                }
                
            }
        }
        
        // Load persistent data on first tick
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        if (nbt.contains("yoshi_golden_apple_start") && !goldenAppleTimers.containsKey(player)) {
            long startTime = nbt.getLong("yoshi_golden_apple_start");
            goldenAppleTimers.put(player, startTime);
        }
        
        if (nbt.contains("yoshi_golden_egg_pos") && !respawnPoints.containsKey(player)) {
            long posLong = nbt.getLong("yoshi_golden_egg_pos");
            respawnPoints.put(player, BlockPos.fromLong(posLong));
        }
    }
    
    public static BlockPos getGoldenEggRespawnPoint(ServerPlayerEntity player) {
        // Check if player has a golden egg respawn point
        BlockPos respawnPos = getRespawnPoint(player);
        if (respawnPos != null) {
            World world = player.getWorld();
            
            // Check if golden egg still exists
            if (world.getBlockState(respawnPos).getBlock() instanceof GoldenEggBlock) {
                return respawnPos;
            } else {
                // Golden egg was destroyed, remove respawn point
                removeRespawnPoint(player, respawnPos);
            }
        }
        return null;
    }
    
    public static void handleGoldenEggRespawn(ServerWorld world, ServerPlayerEntity player, BlockPos eggPos) {
        // Handle the golden egg respawn logic
        if (world.getBlockState(eggPos).getBlock() instanceof GoldenEggBlock) {
            GoldenEggBlock.handleRespawn(world, player, eggPos);
        }
    }
    
    // Cleanup when player leaves
    public static void cleanupPlayer(ServerPlayerEntity player) {
        respawnPoints.remove(player);
        goldenAppleTimers.remove(player);
    }
}