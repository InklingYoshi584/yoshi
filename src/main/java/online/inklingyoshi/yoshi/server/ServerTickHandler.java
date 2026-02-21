package online.inklingyoshi.yoshi.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.util.CooldownManager;
import online.inklingyoshi.yoshi.util.EntityDetection;
import online.inklingyoshi.yoshi.util.DamageSourceUtil;
import online.inklingyoshi.yoshi.item.YoshiItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTickHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/ServerTickHandler");
    
    private static class ReelInData {
        public java.util.List<Entity> targetEntities;
        public int reelInTicks;
        public int windupTicks;
        public static final int WINDUP_TICKS = 10; // 0.5 second windup
        public static final int MAX_REEL_IN_TICKS = 15; // 0.75 second reeling (faster)
        
        public ReelInData() {
            this.targetEntities = new java.util.ArrayList<>();
        }
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
                    
                    if (data.targetEntities.isEmpty()) {
                        reelInAbilities.remove(player);
                    }
                }
            });
        });
    }
    
    public static void startReelIn(PlayerEntity player) {
        ReelInData data = new ReelInData();
        data.targetEntities = online.inklingyoshi.yoshi.util.EntityDetection.getTargetEntities(player);
        data.reelInTicks = 0;
        data.windupTicks = 0;
        
        if (!data.targetEntities.isEmpty()) {
            LOGGER.debug("Server: Starting reel-in ability for player {} with {} targets (windup phase)", 
                player.getName().getString(), data.targetEntities.size());
            
            // Grant invincibility to player during gulp ability
            
            reelInAbilities.put(player, data);  
        } else {
            LOGGER.debug("Server: No targets found for reel-in ability");
        }
    }
    
    private static void updateReelIn(PlayerEntity player, ReelInData data) {
        // Remove invalid entities
        data.targetEntities.removeIf(entity -> 
            entity == null || !entity.isAlive() || entity.isRemoved()
        );
        
        // If no valid targets remain, end the ability
        if (data.targetEntities.isEmpty()) {
            LOGGER.debug("Server: No valid targets remaining, ending reel-in ability");
            // Remove invincibility
            player.setInvulnerable(false);
            return;
        }
        
        // Handle windup phase (start lag)
        if (data.windupTicks < ReelInData.WINDUP_TICKS) {
            data.windupTicks++;
            LOGGER.debug("Server: Reel-in windup phase: {}/{}", data.windupTicks, ReelInData.WINDUP_TICKS);
            return; // Don't apply movement during windup
        }
        
        LOGGER.debug("Server: Reel-in active phase: {} targets, ticks={}/{}", 
            data.targetEntities.size(), data.reelInTicks, ReelInData.MAX_REEL_IN_TICKS);
        
        // Process each target entity
        java.util.Iterator<Entity> iterator = data.targetEntities.iterator();
        while (iterator.hasNext()) {
            Entity target = iterator.next();
            
            // Calculate direction from entity to player
            Vec3d playerPos = player.getPos().add(0, player.getHeight() / 2, 0);
            Vec3d entityPos = target.getPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            
            // Calculate distance
            double distance = entityPos.distanceTo(playerPos);
            
            LOGGER.debug("Server: Processing target {} at distance {}", 
                target.getName().getString(), String.format("%.2f", distance));
            
            // Check if entity is close enough
            if (distance < 0.5) {
                LOGGER.debug("Server: Target {} is close enough, performing throw-back and damage", target.getName().getString());
                throwBackAndDamage(player, target);
                iterator.remove();
                continue;
            }
        
            player.setInvulnerable(true);
            // Calculate faster movement (0.2 instead of 0.1)
            double reelInStrength = 0.2 * data.reelInTicks;
            double speed = distance * 0.2 + reelInStrength; // Faster reeling
            Vec3d velocity = direction.multiply(speed);
            LOGGER.debug("Server: Applying velocity to target: {} (speed={})", velocity, speed);
            
            // Apply velocity on server (this will sync to clients)
            target.setVelocity(velocity);
            target.velocityModified = true;
        }
        
        // Increment ticks
        data.reelInTicks++;
    }
    
    private static void throwBackAndDamage(PlayerEntity player, Entity target) {
        LOGGER.debug("Server: Performing throw-back and damage on target {}", target.getName().getString());
        
        // Store target's max health before dealing damage
        float targetMaxHealth = 0;
        if (target instanceof net.minecraft.entity.LivingEntity) {
            targetMaxHealth = ((net.minecraft.entity.LivingEntity) target).getMaxHealth();
        }
        
        // Calculate throw-back direction (opposite to player's view)
        Vec3d viewDirection = player.getRotationVec(1.0F);
        Vec3d throwVelocity = viewDirection.multiply(1.5); // Throw back and up (increased Y velocity)
        // Apply throw-back velocity
        target.setVelocity(throwVelocity);
        target.velocityModified = true;
        player.setInvulnerable(false);
        // Apply armor-piercing damage
        float damageAmount = 7.0f;
        target.damage(Yoshi.of(target.getWorld(), Yoshi.GULP_DAMAGE, player), damageAmount);
        
        // Check if the entity died from this damage
        if (!target.isAlive()) {
            // Entity died from gulp damage, drop YoshiEgg
            dropYoshiEgg(target, target.getWorld());
            
            // Reward player with hunger and saturation based on opponent's health
            rewardPlayerForGulpKill(player, targetMaxHealth);
        }
        
        LOGGER.debug("Server: Applied throw-back and damage to target {} with velocity {}", 
            target.getName().getString(), throwVelocity);
    }
    
    private static void rewardPlayerForGulpKill(PlayerEntity player, float targetMaxHealth) {
        if (targetMaxHealth <= 0) return;
        
        // Calculate rewards based on target's max health
        // Health scaling: 1 hunger/saturation point per 5 health points, minimum 1, maximum 10
        int healthReward = Math.max(1, Math.min(10, (int) (targetMaxHealth / 5)));
        
        LOGGER.debug("Server: Rewarding player {} with {} hunger/saturation points for killing target with {} max health", 
            player.getName().getString(), healthReward, String.format("%.1f", targetMaxHealth));
        
        // Get current hunger and saturation
        net.minecraft.entity.player.HungerManager hungerManager = player.getHungerManager();
        int currentHunger = hungerManager.getFoodLevel();
        float currentSaturation = hungerManager.getSaturationLevel();
        
        // Add hunger points (capped at 20)
        int newHunger = Math.min(20, currentHunger + healthReward);
        hungerManager.setFoodLevel(newHunger);
        
        // Add saturation points (capped at current hunger level)
        float newSaturation = Math.min(newHunger, currentSaturation + healthReward);
        hungerManager.setSaturationLevel(newSaturation);
        
        LOGGER.debug("Server: Player hunger updated from {} to {}, saturation from {} to {}", 
            currentHunger, newHunger, String.format("%.1f", currentSaturation), String.format("%.1f", newSaturation));
    }
    
    private static void dropYoshiEgg(Entity entity, World world) {
        if (world.isClient) return;
        
        // Only drop YoshiEgg for living entities
        if (entity instanceof LivingEntity) {
            // Create YoshiEgg item stack
            ItemStack yoshiEgg = new ItemStack(YoshiItems.YOSHI_EGG, 1);
            
            // Drop the item at the entity's position
            entity.dropStack(yoshiEgg);
            
            // Log the drop
            LOGGER.debug("Server: Dropped YoshiEgg for entity {}", entity.getName().getString());
        }
    }
}
