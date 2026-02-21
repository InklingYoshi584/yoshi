package online.inklingyoshi.yoshi.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.entity.YoshiEggEntity;

public class YoshiEggItem extends Item {
    
    public YoshiEggItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // Play throw sound
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        
        if (!world.isClient) {
            // Create and spawn YoshiEggEntity
            YoshiEggEntity yoshiEggEntity = new YoshiEggEntity(world, user);
            yoshiEggEntity.setItem(itemStack);
            yoshiEggEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 3F, 1.0F);
            world.spawnEntity(yoshiEggEntity);
        }
        
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
