package online.inklingyoshi.yoshi.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ReinforcedObsidianBlock extends Block {
    
    public ReinforcedObsidianBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        ItemStack heldItem = player.getMainHandStack();
        
        // Check if the held item is a pickaxe
        if (heldItem.getItem() instanceof MiningToolItem tool) {
            // Check the tool material
            if (tool.getMaterial() == ToolMaterials.DIAMOND || tool.getMaterial() == ToolMaterials.NETHERITE) {
                // Diamond or netherite pickaxe - allow breaking
                return super.calcBlockBreakingDelta(state, player, world, pos);
            } else if (tool.getMaterial() == ToolMaterials.IRON) {
                // Iron pickaxe - show breaking animation but very slowly
                return super.calcBlockBreakingDelta(state, player, world, pos);
            }
        }
        
        // Not holding a suitable tool - no breaking cracks appear
        return 0.0f;
    }
}