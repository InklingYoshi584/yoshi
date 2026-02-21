package online.inklingyoshi.yoshi.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import online.inklingyoshi.yoshi.util.EntityDetection;
import online.inklingyoshi.yoshi.util.DamageSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReelInAbility {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/ReelInAbility");
    private Entity targetEntity;
    private int reelInTicks;
    private static final int MAX_REEL_IN_TICKS = 20; // 1 second
    
    public void startReelIn(PlayerEntity player) {
        LOGGER.debug("Starting reel-in ability for player {}", player.getName().getString());
        targetEntity = EntityDetection.getTargetEntity(player);
        reelInTicks = 0;
        
        if (targetEntity != null) {
            LOGGER.debug("Reel-in ability started with target: {}", targetEntity.getName().getString());
        } else {
            LOGGER.debug("Reel-in ability started but no target found");
        }
    }
    
    public void updateReelIn(PlayerEntity player) {
        if (targetEntity == null || !targetEntity.isAlive() || targetEntity.isRemoved()) {
            LOGGER.debug("Reel-in target is invalid (null={}, alive={}, removed={}), resetting", 
                targetEntity == null, targetEntity != null && targetEntity.isAlive(), targetEntity != null && targetEntity.isRemoved());
            reset();
            return;
        }
        
        // Calculate direction from entity to player
        Vec3d playerPos = player.getPos().add(0, player.getHeight() / 2, 0);
        Vec3d entityPos = targetEntity.getPos();
        Vec3d direction = playerPos.subtract(entityPos).normalize();
        
        // Calculate distance
        double distance = entityPos.distanceTo(playerPos);
        
        LOGGER.debug("Reel-in update: target={}, distance={}, ticks={}/{}", 
            targetEntity.getName().getString(), distance, reelInTicks, MAX_REEL_IN_TICKS);
        
        // Check if entity is close enough
        if (distance < 1.0) {
            LOGGER.debug("Target {} is close enough, performing throw-back and damage", targetEntity.getName().getString());
            // Perform throw-back and damage
            throwBackAndDamage(player);
            reset();
            return;
        }
        
        // Calculate smooth movement
        double speed = distance * 0.1; // Slower as it gets closer
        Vec3d velocity = direction.multiply(speed);
        
        LOGGER.debug("Applying velocity to target: {} (speed={})", velocity, speed);
        
        // Apply velocity
        targetEntity.setVelocity(velocity);
        targetEntity.velocityModified = true;
        
        // Increment ticks
        reelInTicks++;
        
        // Check if max time reached
        if (reelInTicks >= MAX_REEL_IN_TICKS) {
            LOGGER.debug("Max reel-in time reached, resetting");
            reset();
        }
    }
    
    public boolean isReeling() {
        return targetEntity != null;
    }
    
    public Entity getTargetEntity() {
        return targetEntity;
    }
    
    public void reset() {
        targetEntity = null;
        reelInTicks = 0;
    }
    
    private void throwBackAndDamage(PlayerEntity player) {
        if (targetEntity == null) {
            LOGGER.debug("Cannot perform throw-back: target is null");
            return;
        }
        
        LOGGER.debug("Performing throw-back and damage on target {}", targetEntity.getName().getString());
        
        // Calculate throw-back direction (opposite to player's view)
        Vec3d viewDirection = player.getRotationVec(1.0F);
        Vec3d throwBackDirection = viewDirection.multiply(-1);
        
        // Apply throw-back velocity
        double throwBackSpeed = 1.5;
        Vec3d throwBackVelocity = throwBackDirection.multiply(throwBackSpeed);
        targetEntity.setVelocity(throwBackVelocity);
        targetEntity.velocityModified = true;
        
        LOGGER.debug("Applied throw-back velocity: {}", throwBackVelocity);
        
        // Deal 6 armor piercing damage
        // Use a damage source that bypasses armor
        targetEntity.damage(DamageSourceUtil.createArmorPiercingDamageSource(player), 6.0F);
        LOGGER.debug("Applied 6 armor piercing damage to {}", targetEntity.getName().getString());
    }
}
