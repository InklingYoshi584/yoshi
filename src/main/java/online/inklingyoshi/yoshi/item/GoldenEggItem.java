package online.inklingyoshi.yoshi.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.block.GoldenEggBlock;
import online.inklingyoshi.yoshi.util.AbilityManager;
import online.inklingyoshi.yoshi.util.GoldenEggManager;

public class GoldenEggItem extends BlockItem {
    
    public GoldenEggItem(GoldenEggBlock block, Settings settings) {
        super(block, settings);
    }
    
    
    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        
        // Only allow placement if player is a Yoshi with gulp ability and has golden apple effect
        if (player instanceof ServerPlayerEntity serverPlayer) {
            
            if (AbilityManager.canPlayerUseAbility(serverPlayer, "gulp_ability")) {
                context.getStack().decrement(1);
                return super.place(context, state);
            } 
        }
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // Check if player is a Yoshi player with gulp ability enabled
        if (user instanceof ServerPlayerEntity serverPlayer) {
            if (!AbilityManager.canPlayerUseAbility(serverPlayer, "gulp_ability")) {
                return TypedActionResult.fail(itemStack);
            }                
            itemStack.decrement(1);
            return super.use(world, user, hand);
        }
        return TypedActionResult.fail(itemStack);
    }
}