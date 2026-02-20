package online.inklingyoshi.yoshi.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import online.inklingyoshi.yoshi.util.CooldownManager;
import online.inklingyoshi.yoshi.util.EntityDetection;
import online.inklingyoshi.yoshi.util.DamageSourceUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTickHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/ServerTickHandler");
    
    private static class ReelInData {
        public Entity targetEntity;
        public int reelInTicks;
        public static final int MAX_REEL_IN_TICKS = 20;
    }
    
    private static final Map<PlayerEntity, ReelInData> reelInAbilities = new HashMap<>();
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Tick cooldowns
            CooldownManager.tick();
            
            // Update reel-in abilities
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (reelInAbilities.containsKey(player)) {
                    ReelInData data = reelInAbilities.get(player);
                    updateReelIn(player, data);
                    
                    if (data.targetEntity == null) {
                        reelInAbilities.remove(player);
                    }
                }
            });
        });
    }
    
    public static void startReelIn(PlayerEntity player) {
        ReelInData data = new ReelInData();
        data.targetEntity = EntityDetection.getTargetEntity(player);
        data.reelInTicks = 0;
        
        if (data.targetEntity != null) {
            LOGGER.info("Server: Starting reel-in ability for player {} with target {}", 
                player.getName().getString(), data.targetEntity.getName().getString());
            reelInAbilities.put(player, data);
        } else {
            LOGGER.info("Server: No target found for reel-in ability");
        }
    }
    
    private static void updateReelIn(PlayerEntity player, ReelInData data) {
        if (data.targetEntity == null || !data.targetEntity.isAlive() || data.targetEntity.isRemoved()) {
            LOGGER.info("Server: Reel-in target is invalid, resetting");
            data.targetEntity = null;
            return;
        }
        
        // Calculate direction from entity to player
        Vec3d playerPos = player.getPos().add(0, player.getHeight() / 2, 0);
        Vec3d entityPos = data.targetEntity.getPos();
        Vec3d direction = playerPos.subtract(entityPos).normalize();
        
        // Calculate distance
        double distance = entityPos.distanceTo(playerPos);
        
        LOGGER.info("Server: Reel-in update: target={}, distance={}, ticks={}/{}", 
            data.targetEntity.getName().getString(), distance, data.reelInTicks, ReelInData.MAX_REEL_IN_TICKS);
        
        // Check if entity is close enough
        if (distance < 1.0) {
            LOGGER.info("Server: Target {} is close enough, performing throw-back and damage", data.targetEntity.getName().getString());
            throwBackAndDamage(player, data.targetEntity);
            data.targetEntity = null;
            return;
        }
        
        // Calculate smooth movement
        double speed = distance * 0.1; // Slower as it gets closer
        Vec3d velocity = direction.multiply(speed);
        
        LOGGER.info("Server: Applying velocity to target: {} (speed={})", velocity, speed);
        
        // Apply velocity on server (this will sync to clients)
        data.targetEntity.setVelocity(velocity);
        data.targetEntity.velocityModified = true;
        
        // Increment ticks
        data.reelInTicks++;
        
        // Check if max time reached
        if (data.reelInTicks >= ReelInData.MAX_REEL_IN_TICKS) {
            LOGGER.info("Server: Max reel-in time reached, resetting");
            data.targetEntity = null;
        }
    }
    
    private static void throwBackAndDamage(PlayerEntity player, Entity target) {
        LOGGER.info("Server: Performing throw-back and damage on target {}", target.getName().getString());
        
        // Calculate throw-back direction (opposite to player's view)
        Vec3d viewDirection = player.getRotationVec(1.0F);
        Vec3d throwVelocity = viewDirection.multiply(-1.5).add(0, 0.5, 0); // Throw back and up
        
        // Apply throw-back velocity
        target.setVelocity(throwVelocity);
        target.velocityModified = true;
        
        // Apply armor-piercing damage
        World world = player.getWorld();
        target.damage(DamageSourceUtil.createArmorPiercingDamageSource(player), 6.0f);
        
        LOGGER.info("Server: Applied throw-back and damage to target {}", target.getName().getString());
    }
}
