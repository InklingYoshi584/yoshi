package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import online.inklingyoshi.yoshi.Yoshi;

public class GroundPoundDamageSource {
    
    public static DamageSource create(PlayerEntity attacker) {
        return Yoshi.of(attacker.getWorld(), Yoshi.GROUND_POUND_DAMAGE, attacker);
    }
    
    public static Text getDeathMessage(LivingEntity entity, DamageSource source) {
        if (source.getAttacker() instanceof PlayerEntity attacker) {
            return Text.translatable("death.attack.ground_pound.player", 
                entity.getDisplayName(), attacker.getDisplayName());
        } else {
            return Text.translatable("death.attack.ground_pound", entity.getDisplayName());
        }
    }
}