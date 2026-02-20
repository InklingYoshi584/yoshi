package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private static final Map<PlayerEntity, Map<String, Integer>> cooldowns = new HashMap<>();
    private static final int FLUTTER_JUMP_COOLDOWN = 20; // 1 second
    private static final int REEL_IN_COOLDOWN = 40; // 2 seconds
    
    public static void tick() {
        // Decrement cooldowns for all players
        cooldowns.forEach((player, abilities) -> {
            abilities.forEach((ability, cooldown) -> {
                if (cooldown > 0) {
                    abilities.put(ability, cooldown - 1);
                }
            });
        });
    }
    
    public static boolean canUseAbility(PlayerEntity player, String ability) {
        if (!cooldowns.containsKey(player)) {
            cooldowns.put(player, new HashMap<>());
        }
        
        Map<String, Integer> playerCooldowns = cooldowns.get(player);
        return !playerCooldowns.containsKey(ability) || playerCooldowns.get(ability) <= 0;
    }
    
    public static void startCooldown(PlayerEntity player, String ability) {
        if (!cooldowns.containsKey(player)) {
            cooldowns.put(player, new HashMap<>());
        }
        
        Map<String, Integer> playerCooldowns = cooldowns.get(player);
        int cooldownTime = 0;
        
        switch (ability) {
            case "flutter_jump":
                cooldownTime = FLUTTER_JUMP_COOLDOWN;
                break;
            case "reel_in":
                cooldownTime = REEL_IN_COOLDOWN;
                break;
        }
        
        playerCooldowns.put(ability, cooldownTime);
    }
    
    public static int getCooldown(PlayerEntity player, String ability) {
        if (!cooldowns.containsKey(player)) {
            cooldowns.put(player, new HashMap<>());
        }
        
        Map<String, Integer> playerCooldowns = cooldowns.get(player);
        return playerCooldowns.getOrDefault(ability, 0);
    }
}
