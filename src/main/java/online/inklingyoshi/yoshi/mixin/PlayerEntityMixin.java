package online.inklingyoshi.yoshi.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // Check if player is in midair and falling
        if (!player.isOnGround() && !player.isTouchingWater() && player.getVelocity().y < 0.0) {
            // Apply flutter jump velocity boost
            Vec3d velocity = player.getVelocity();
            player.setVelocity(velocity.x, velocity.y * 0.7, velocity.z);
            player.velocityModified = true;
        }
    }
}
