package online.inklingyoshi.yoshi.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import online.inklingyoshi.yoshi.item.YoshiItems;
import online.inklingyoshi.yoshi.util.AbilityManager;
import online.inklingyoshi.yoshi.util.CooldownManager;

public class CreateEggAbility {
    private static final int HUNGER_COST = 6;
    private static final int SATURATION_COST = 6;
    private static final int COOLDOWN_TICKS = 40; // 2 seconds
    
    public ActionResult createEgg(PlayerEntity player) {
        // Check if player has abilities enabled
        if (!AbilityManager.canPlayerUseAbilities(player)) {
            return ActionResult.FAIL;
        }
        
        // Check if player is on cooldown
        if (!CooldownManager.canUseAbility(player, "create_egg")) {
            return ActionResult.FAIL;
        }
        
        // Check if player has enough hunger
        if (player.getHungerManager().getFoodLevel() < HUNGER_COST) {
            return ActionResult.FAIL;
        }
        
        // Check if player has enough saturation
        if (player.getHungerManager().getSaturationLevel() < SATURATION_COST) {
            return ActionResult.FAIL;
        }
        
        // Consume hunger and saturation
        player.getHungerManager().addExhaustion(HUNGER_COST * 4.0f); // Convert to exhaustion
        player.getHungerManager().setSaturationLevel(player.getHungerManager().getSaturationLevel() - SATURATION_COST);
        
        // Create Yoshi egg item
        ItemStack yoshiEgg = new ItemStack(YoshiItems.YOSHI_EGG, 1);
        
        // Give egg to player or drop it if inventory is full
        if (!player.getInventory().insertStack(yoshiEgg)) {
            player.dropItem(yoshiEgg, false);
        }
        
        // Play egg creation sound
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        // Start cooldown
        CooldownManager.startCooldown(player, "create_egg");
        
        return ActionResult.SUCCESS;
    }
}