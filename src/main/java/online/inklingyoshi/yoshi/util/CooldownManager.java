package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private static final Map<PlayerEntity, Map<String, Integer>> cooldowns = new HashMap<>();
    private static final Map<PlayerEntity, Map<String, Integer>> activeAbilities = new HashMap<>();
    private static final int FLUTTER_JUMP_COOLDOWN = 4; // 4 ticks
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
        
        // Decrement active ability durations
        tickAbilities();
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
    
    // Ability tracking methods for continuous abilities
    public static void startAbility(PlayerEntity player, String ability, int duration) {
        if (!activeAbilities.containsKey(player)) {
            activeAbilities.put(player, new HashMap<>());
        }
        
        Map<String, Integer> playerAbilities = activeAbilities.get(player);
        playerAbilities.put(ability, duration);
    }
    
    public static void endAbility(PlayerEntity player, String ability) {
        if (activeAbilities.containsKey(player)) {
            Map<String, Integer> playerAbilities = activeAbilities.get(player);
            playerAbilities.remove(ability);
            
            // Start cooldown when ability ends
            startCooldown(player, ability);
        }
    }
    
    public static boolean isAbilityActive(PlayerEntity player, String ability) {
        if (!activeAbilities.containsKey(player)) {
            return false;
        }
        
        Map<String, Integer> playerAbilities = activeAbilities.get(player);
        return playerAbilities.containsKey(ability) && playerAbilities.get(ability) > 0;
    }
    
    public static void tickAbilities() {
        // Create a copy of the entries to avoid ConcurrentModificationException
        Map<PlayerEntity, Map<String, Integer>> copy = new HashMap<>(activeAbilities);
        
        // Decrement active ability durations
        copy.forEach((player, abilities) -> {
            // Create a copy of the abilities map for this player
            Map<String, Integer> abilitiesCopy = new HashMap<>(abilities);
            
            abilitiesCopy.forEach((ability, duration) -> {
                if (duration > 0) {
                    // Update duration in the original map
                    if (activeAbilities.containsKey(player) && activeAbilities.get(player).containsKey(ability)) {
                        activeAbilities.get(player).put(ability, duration - 1);
                    }
                } else {
                    // Ability duration ended
                    endAbility(player, ability);
                }
            });
        });
    }
}
