package online.inklingyoshi.yoshi.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.util.AbilityManager;
import online.inklingyoshi.yoshi.util.GoldenEggManager;

public class GoldenAppleEventHandler {
    
    public static void register() {
        // This event handler is no longer needed as golden apple consumption
        // is now handled by the PlayerEntityMixin.onEatFood() method
        // The mixin approach is more reliable for detecting actual consumption
        
        // Keeping the file for potential future use or as a reference
    }
}