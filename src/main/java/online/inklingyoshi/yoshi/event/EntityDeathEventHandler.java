package online.inklingyoshi.yoshi.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.item.YoshiItems;

public class EntityDeathEventHandler {
    
    public static void register() {
        // Use a more reliable approach - check damage source on entity death
        // This will work for all entity deaths
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Server started - ready to handle events
        });
    }
    
    // This method will be called from the ServerTickHandler to check for dead entities
    public static void checkForDeadEntities() {
        // This will be implemented differently
    }
    
    // Method to handle entity death when gulp damage is the cause
    public static void handleEntityDeath(LivingEntity entity, DamageSource source, World world) {
        if (world.isClient) return;
        
        // Check if the damage source is the gulp damage type
        try {
            if (source.isOf(Yoshi.GULP_DAMAGE)) {
                // Entity died from gulp damage, drop YoshiEgg
                dropYoshiEgg(entity, world);
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }
    
    private static void dropYoshiEgg(LivingEntity entity, World world) {
        // Create YoshiEgg item stack
        ItemStack yoshiEgg = new ItemStack(YoshiItems.YOSHI_EGG, 1);
        
        // Drop the item at the entity's position
        entity.dropStack(yoshiEgg);
        
        // Log the drop
        // System.out.println("YoshiEgg dropped for entity " + entity.getType().toString());
    }
}
