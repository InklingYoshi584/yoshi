package online.inklingyoshi.yoshi.mixin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import online.inklingyoshi.yoshi.util.AbilityManager;
import java.util.UUID;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    private static final UUID YOSHI_SPEED_BOOST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final UUID YOSHI_ATTACK_DAMAGE_REDUCTION_UUID = UUID.fromString("12345678-1234-1234-1234-123456789014");
    private static final UUID YOSHI_ARMOR_REDUCTION_UUID = UUID.fromString("12345678-1234-1234-1234-123456789015");
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        boolean hasYoshiAbilities = AbilityManager.canPlayerUseAbilities(player);
        boolean hasSpeedAbility = AbilityManager.canPlayerUseAbility(player, "speed");
        boolean hasJumpHeightAbility = AbilityManager.canPlayerUseAbility(player, "jump_height");
        
        // Disable fall damage for Yoshi players
        if (hasJumpHeightAbility) {
            player.fallDistance = 0.0f;
        }
        
        // Apply speed boost
        EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(YOSHI_SPEED_BOOST_UUID);
            if (hasSpeedAbility) {
                speedAttribute.addPersistentModifier(
                    new EntityAttributeModifier(
                        YOSHI_SPEED_BOOST_UUID,
                        "Yoshi speed boost",
                        .5, // 50% speed increase
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                    )
                );
            }
        }
        
        // Apply jump height boost using Pehkui
        if (hasJumpHeightAbility) {
            // Set jump height to 4 blocks (normal jump height is ~1.25 blocks)
            // To achieve 4 blocks, we need a scale of approximately 3.2x
            ScaleData jumpHeightScale = ScaleRegistries.SCALE_TYPES.get(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, ScaleTypes.JUMP_HEIGHT)).getScaleData(player);
            if (jumpHeightScale != null) {
                jumpHeightScale.setScale(1.5f); // 4 blocks / 1.25 blocks ≈ 3.2x
            }
        } else {
            // Reset jump height to normal when abilities are disabled
            ScaleData jumpHeightScale = ScaleRegistries.SCALE_TYPES.get(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, ScaleTypes.JUMP_HEIGHT)).getScaleData(player);
            if (jumpHeightScale != null) {
                jumpHeightScale.setScale(1.0f); // Normal jump height
            }
        }
        
        // Shrink Yoshi players by 0.9 scale
        if (hasYoshiAbilities) {
            ScaleData baseScale = ScaleRegistries.SCALE_TYPES.get(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, ScaleTypes.BASE)).getScaleData(player);
            if (baseScale != null) {
                baseScale.setScale(0.9f); // 90% of normal size
            }
        } else {
            // Reset player size to normal when abilities are disabled
            ScaleData baseScale = ScaleRegistries.SCALE_TYPES.get(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, ScaleTypes.BASE)).getScaleData(player);
            if (baseScale != null) {
                baseScale.setScale(1.0f); // Normal size
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
        
        // Apply armor reduction
        EntityAttributeInstance armorAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttribute != null) {
            armorAttribute.removeModifier(YOSHI_ARMOR_REDUCTION_UUID);
            if (hasYoshiAbilities) {
                armorAttribute.addPersistentModifier(
                    new EntityAttributeModifier(
                        YOSHI_ARMOR_REDUCTION_UUID,
                        "Yoshi armor reduction",
                        -0.3, // 30% armor reduction
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                    )
                );
            }
        }
    }
}