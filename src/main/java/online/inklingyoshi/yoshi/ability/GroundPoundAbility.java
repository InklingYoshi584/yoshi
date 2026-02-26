package online.inklingyoshi.yoshi.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.util.CooldownManager;
import online.inklingyoshi.yoshi.util.GroundPoundDamageSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroundPoundAbility {
    
    // Static maps to track ground pound state for each player
    private static final Map<ServerPlayerEntity, Double> startYMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, Boolean> groundPoundingMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, Integer> endLagMap = new HashMap<>();
    
    public static void activate(ServerPlayerEntity player) {
        // Check if player is in air and ability is ready
        if (!player.isOnGround() && CooldownManager.canUseAbility(player, "ground_pound")) {
            // Start ground pound
            CooldownManager.startAbility(player, "ground_pound", 200); // 10 second cooldown
            
            // Set X/Z velocity to 0 and apply fast downward velocity
            Vec3d currentVelocity = player.getVelocity();
            player.setVelocity(0, -2.0, 0); // Fast downward movement
            player.velocityModified = true;
            
            // Store starting height and ground pounding state
            startYMap.put(player, player.getY());
            groundPoundingMap.put(player, true);
            
            // Play activation sound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5f, 0.7f);
        }
    }
    
    public static void onPlayerTick(ServerPlayerEntity player) {
        // Check if player is ground pounding
        if (groundPoundingMap.getOrDefault(player, false)) {
            // Check if player hit the ground
            if (player.isOnGround()) {
                handleGroundImpact(player);
            } else {
                // Continue downward acceleration while in air
                Vec3d currentVelocity = player.getVelocity();
                if (currentVelocity.y > -3.0) { // Cap maximum downward velocity
                    player.setVelocity(currentVelocity.x, currentVelocity.y - 0.2, currentVelocity.z);
                    player.velocityModified = true;
                }
                
                // Create falling particles
                World world = player.getWorld();
                world.addParticle(ParticleTypes.CLOUD, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    0.5, 0.1, 0.5);
            }
        }
    }
    
    private static void handleGroundImpact(ServerPlayerEntity player) {
        World world = player.getWorld();
        
        // Get starting height from map
        double startY = startYMap.getOrDefault(player, player.getY());
        double fallDistance = startY - player.getY();
        
        float damage;
        // Calculate damage (4-20 based on height, max at 100 blocks)
        if (fallDistance > 1) {
            damage = 4.0f + (float)(Math.min(fallDistance, 100.0) / 100.0f) * 16.0f;
        } else {
            damage = 0f;
        }
        // Calculate area of effect (2-6 blocks based on height)
        double radius = 1.0 + (Math.min(fallDistance, 100.0) / 100.0f) * 4.0;
        
        // Find entities in range
        Box impactArea = new Box(
            player.getX() - radius, player.getY() - 1, player.getZ() - radius,
            player.getX() + radius, player.getY() + 2, player.getZ() + radius
        );
        
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, impactArea, 
            entity -> entity != player && entity.isAlive());
        
        // Apply effects to entities
        for (LivingEntity entity : entities) {
            // Deal damage using custom ground pound damage source
            DamageSource groundPoundDamage = GroundPoundDamageSource.create(player);
            entity.damage(groundPoundDamage, damage);
            
            // Calculate launch vector (upwards and backwards relative to player)
            Vec3d toEntity = entity.getPos().subtract(player.getPos());
            Vec3d launchDirection = new Vec3d(toEntity.x, 0.5 + (fallDistance / 50.0), toEntity.z).normalize();
            double launchStrength = 0.8 + (Math.min(fallDistance, 100.0) / 100.0f) * 0.7;
            
            entity.setVelocity(launchDirection.multiply(launchStrength));
            entity.velocityModified = true;
        }
        
        // Play impact effects
        playGroundImpactEffects(player, world, radius);
        
        // Apply 20-tick end lag and clear ground pounding flag
        endLagMap.put(player, 20);
        groundPoundingMap.put(player, false);
        startYMap.remove(player);
        
    }
    
    public static void handleEndLag(ServerPlayerEntity player) {
        if (endLagMap.containsKey(player)) {
            int endLagTicks = endLagMap.get(player);
            if (endLagTicks > 0) {
                // Prevent movement during end lag
                player.setVelocity(0, 0, 0);
                player.velocityModified = true;
                
                endLagMap.put(player, endLagTicks - 1);
            } else {
                endLagMap.remove(player);
            }
        }
    }
    
    // Cleanup method to remove players from maps when they leave the server
    public static void cleanupPlayer(ServerPlayerEntity player) {
        startYMap.remove(player);
        groundPoundingMap.remove(player);
        endLagMap.remove(player);
    }
    
    private static void playGroundImpactEffects(ServerPlayerEntity player, World world, double radius) {
        // Play impact sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.PLAYERS, 1.0f, 0.8f);
        
        // Spawn impact particles
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * radius * 2;
            double offsetZ = (world.random.nextDouble() - 0.5) * radius * 2;
            world.addParticle(ParticleTypes.EXPLOSION, 
                player.getX() + offsetX, player.getY() + 0.5, player.getZ() + offsetZ, 
                0, 0, 0);
        }
    }
}