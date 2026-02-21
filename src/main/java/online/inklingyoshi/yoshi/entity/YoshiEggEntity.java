package online.inklingyoshi.yoshi.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.item.YoshiItems;

public class YoshiEggEntity extends ThrownItemEntity {
    
    public YoshiEggEntity(EntityType<? extends YoshiEggEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public YoshiEggEntity(World world, LivingEntity owner) {
        super(YoshiEntityType.YOSHI_EGG, owner, world);
    }
    
    @Override
    protected Item getDefaultItem() {
        return YoshiItems.YOSHI_EGG;
    }
    
    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        
        if (!this.getWorld().isClient) {
            // Apply damage to the hit entity
            entityHitResult.getEntity().damage(Yoshi.of(this.getWorld(), DamageTypes.THROWN, (LivingEntity) this.getOwner()), 8.0F);
        }
    }
    
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        
        if (!this.getWorld().isClient) {
            // Play splash sound            
            // Destroy the egg
            this.discard();
        }
    }
}
