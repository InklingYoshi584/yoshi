package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

public class EntityDetection {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/EntityDetection");
    private static final double MAX_RANGE = 4.0;
    private static final double MAX_ANGLE_DEGREES = 40.0;
    private static final double MAX_ANGLE_RADIANS = Math.toRadians(MAX_ANGLE_DEGREES);
    
    public static Entity getTargetEntity(PlayerEntity player) {
        List<Entity> targets = getTargetEntities(player);
        if (targets.isEmpty()) {
            return null;
        }
        
        // Return the closest entity for backward compatibility
        return targets.get(0);
    }
    
    public static List<Entity> getTargetEntities(PlayerEntity player) {
        if (player.getWorld() == null) {
            // LOGGER.info("Player world is null, cannot detect entities");
            return java.util.Collections.emptyList();
        }
        
        // LOGGER.info("Starting entity detection for player {}", player.getName().getString());
        // LOGGER.info("Detection parameters: max range = {} blocks, max angle = {} degrees", MAX_RANGE, MAX_ANGLE_DEGREES);
        
        // Get all entities within range
        List<Entity> entitiesInRange = player.getWorld().getOtherEntities(
            player,
            player.getBoundingBox().expand(MAX_RANGE),
            entity -> {
                // Filter out invalid entities
                if (entity == player || entity.isRemoved() || !entity.isAlive()) {
                    // LOGGER.info("Filtering out entity {}: invalid (player={}, removed={}, alive={})", 
                        // entity.getName().getString(), entity == player, entity.isRemoved(), entity.isAlive());
                    return false;
                }
                
                // Check if entity's bounding box is within range
                boolean withinRange = isEntityBoundingBoxInRange(player, entity, MAX_RANGE);
                
                // Check if entity is within view angle
                boolean withinAngle = isWithinViewAngle(player, entity);
                
                // Calculate closest distance between bounding boxes
                double closestDistance = getClosestDistanceBetweenBoundingBoxes(player, entity);
                // LOGGER.info("Entity {}: within range = {}, within angle = {}, closest distance = {} blocks", 
                    // entity.getName().getString(), withinRange, withinAngle, String.format("%.2f", closestDistance));
                
                return withinRange && withinAngle;
            }
        );
        
        // LOGGER.info("Found {} entities in range and angle", entitiesInRange.size());
        
        // Sort by closest distance
        List<Entity> sortedTargets = entitiesInRange.stream()
            .sorted((e1, e2) -> {
                double dist1 = getClosestDistanceBetweenBoundingBoxes(player, e1);
                double dist2 = getClosestDistanceBetweenBoundingBoxes(player, e2);
                return Double.compare(dist1, dist2);
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (!sortedTargets.isEmpty()) {
            double distance = getClosestDistanceBetweenBoundingBoxes(player, sortedTargets.get(0));
            // LOGGER.info("Selected {} target entities, closest at distance {} blocks", 
                // sortedTargets.size(), String.format("%.2f", distance));
        } else {
            // LOGGER.info("No valid target entities found");
        }
        
        return sortedTargets;
    }
    
    private static boolean isEntityBoundingBoxInRange(PlayerEntity player, Entity entity, double maxRange) {
        // Check if the entity's bounding box is within the specified range of the player
        Box playerBox = player.getBoundingBox();
        Box entityBox = entity.getBoundingBox();
        
        // Calculate the closest distance between the two bounding boxes
        double closestDistance = getClosestDistanceBetweenBoundingBoxes(player, entity);
        
        return closestDistance <= maxRange;
    }
    
    private static double getClosestDistanceBetweenBoundingBoxes(PlayerEntity player, Entity entity) {
        Box playerBox = player.getBoundingBox();
        Box entityBox = entity.getBoundingBox();
        
        // Calculate closest distance between the two bounding boxes
        double dx = Math.max(playerBox.minX - entityBox.maxX, entityBox.minX - playerBox.maxX);
        double dy = Math.max(playerBox.minY - entityBox.maxY, entityBox.minY - playerBox.maxY);
        double dz = Math.max(playerBox.minZ - entityBox.maxZ, entityBox.minZ - playerBox.maxZ);
        
        // If boxes overlap, distance is 0
        if (dx < 0 && dy < 0 && dz < 0) {
            return 0.0;
        }
        
        // Otherwise, calculate Euclidean distance
        return Math.sqrt(Math.max(0, dx) * Math.max(0, dx) + 
                        Math.max(0, dy) * Math.max(0, dy) + 
                        Math.max(0, dz) * Math.max(0, dz));
    }
    
    private static boolean isWithinViewAngle(PlayerEntity player, Entity entity) {
    // 1. Get the direction the player is actually looking
    Vec3d lookVec = player.getRotationVec(1.0F);
    
    // 2. Get the direction from player eyes to the entity center
    Vec3d playerPos = player.getEyePos();
    Vec3d entityPos = entity.getBoundingBox().getCenter(); // Better than manual height math
    Vec3d toEntity = entityPos.subtract(playerPos).normalize();
    
    // 3. Use the Dot Product to find the cosine of the angle
    // dot = cos(theta) because both vectors are normalized
    double dot = lookVec.dotProduct(toEntity);
    
    // 4. Convert your max angle to a cosine threshold
    // (Pre-calculate this if MAX_ANGLE_DEGREES is constant)
    double cosThreshold = Math.cos(Math.toRadians(MAX_ANGLE_DEGREES));
    
    // Since cos(0) is 1, a larger dot product means a smaller angle.
    return dot >= cosThreshold;
    }
}
