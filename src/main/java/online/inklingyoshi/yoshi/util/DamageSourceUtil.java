package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class DamageSourceUtil {
    public static DamageSource createArmorPiercingDamageSource(PlayerEntity player) {
        // Use magic damage source which bypasses armor
        World world = player.getWorld();
        return world.getDamageSources().magic();
    }
}
