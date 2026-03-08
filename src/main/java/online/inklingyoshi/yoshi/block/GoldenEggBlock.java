package online.inklingyoshi.yoshi.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import online.inklingyoshi.yoshi.util.AbilityManager;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.util.GoldenEggManager;

public class GoldenEggBlock extends Block {
    
    // Custom voxel shape for 6x8x6 block size
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.27, 0, 0.27, 0.73, 0.5, 0.73);
    
    public GoldenEggBlock() {
        super(Settings.copy(Blocks.DRAGON_EGG)
            .strength(0.5f, 0.5f) // Can be broken quickly
            .sounds(BlockSoundGroup.METAL) // Golden egg sound
            .nonOpaque() // Non-opaque for custom shape
        );
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        
        // Clear the respawn point when broken
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.setSpawnPoint(null, null, 0.0f, false, false);
        }
        
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (placer instanceof PlayerEntity player) {
            player.sendMessage(net.minecraft.text.Text.literal("Golden Egg set as respawn point!"));
        }
        if (!world.isClient && placer instanceof ServerPlayerEntity serverPlayer) {
            // Check if player is a Yoshi player with gulp ability enabled
            if (AbilityManager.canPlayerUseAbilities(serverPlayer) && 
                AbilityManager.canPlayerUseAbility(serverPlayer, "gulp_ability")) {
                
                // Set this as the player's respawn point using Minecraft's respawn system
                serverPlayer.setSpawnPoint(world.getRegistryKey(), pos, 0.0f, true, false);
                
                // Also register it in our respawn point tracking system
                GoldenEggManager.setRespawnPoint(serverPlayer, pos);
            }
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }
    
    // Handle respawn - called when player dies and respawns at golden egg
    public static void handleRespawn(ServerWorld world, ServerPlayerEntity player, BlockPos eggPos) {
        // Break the golden egg
        world.breakBlock(eggPos, false); // false = no drops
        player.teleport(eggPos.getX() + 0.5, eggPos.getY(), eggPos.getZ() + 0.5);
        // Clear the respawn point
        player.setSpawnPoint(null, null, 0.0f, false, false);
        
        // Send message to player
        player.sendMessage(net.minecraft.text.Text.literal("Your Golden Egg has hatched!"));
    }
}