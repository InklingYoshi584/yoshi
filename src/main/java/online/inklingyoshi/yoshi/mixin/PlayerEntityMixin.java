package online.inklingyoshi.yoshi.mixin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import online.inklingyoshi.yoshi.util.AbilityManager;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    private static final UUID YOSHI_SPEED_BOOST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final UUID YOSHI_JUMP_BOOST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789013");
    private static final UUID YOSHI_ATTACK_DAMAGE_REDUCTION_UUID = UUID.fromString("12345678-1234-1234-1234-123456789014");
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        boolean hasYoshiAbilities = AbilityManager.canPlayerUseAbilities(player);
        
        // Apply speed boost
        EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(YOSHI_SPEED_BOOST_UUID);
            if (hasYoshiAbilities) {
                speedAttribute.addPersistentModifier(
                    new EntityAttributeModifier(
                        YOSHI_SPEED_BOOST_UUID,
                        "Yoshi speed boost",
                        1.2, // 120% speed increase
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                    )
                );
            }
        }
        
        // Apply jump boost
        EntityAttributeInstance jumpAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (jumpAttribute != null) {
            jumpAttribute.removeModifier(YOSHI_JUMP_BOOST_UUID);
            if (hasYoshiAbilities) {
                jumpAttribute.addPersistentModifier(
                    new EntityAttributeModifier(
                        YOSHI_JUMP_BOOST_UUID,
                        "Yoshi armor nerf",
                        -0.3, // 30% armor reduction
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                    )
                );
            }
        }
        
        // Apply attack damage reduction
        EntityAttributeInstance attackDamageAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(YOSHI_ATTACK_DAMAGE_REDUCTION_UUID);
            if (hasYoshiAbilities) {
                attackDamageAttribute.addPersistentModifier(
                    new EntityAttributeModifier(
                        YOSHI_ATTACK_DAMAGE_REDUCTION_UUID,
                        "Yoshi attack damage reduction",
                        -0.2, // 20% attack damage reduction
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                    )
                );
            }
        }
    }
}