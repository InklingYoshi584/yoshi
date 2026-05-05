package online.inklingyoshi.yoshi.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import online.inklingyoshi.yoshi.util.AbilityManager;
import online.inklingyoshi.yoshi.util.GoldenEggManager;

@Mixin(Item.class)
public class ItemMixin {
    
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        // Check if the item being finished is a golden apple and the user is a player
        if (stack.getItem() == net.minecraft.item.Items.GOLDEN_APPLE && user instanceof ServerPlayerEntity serverPlayer) {
            // Check if player is a Yoshi player with gulp ability enabled
            if (AbilityManager.canPlayerUseAbilities(serverPlayer) && 
                AbilityManager.canPlayerUseAbility(serverPlayer, "gulp_ability")) {
                
                // The golden apple was consumed
                GoldenEggManager.startGoldenAppleTimer(serverPlayer);
            }
        }
    }
}