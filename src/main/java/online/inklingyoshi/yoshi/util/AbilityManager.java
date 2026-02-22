package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.player.PlayerEntity;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    
    /**
     * Check if a player can use Yoshi abilities
     */
    public static boolean canPlayerUseAbilities(PlayerEntity player) {
        return ConfigManager.hasAbilities(player);
    }
    
    /**
     * Set whether a player can use Yoshi abilities
     */
    public static void setPlayerAbilitiesEnabled(PlayerEntity player, boolean enabled) {
        ConfigManager.setAbilities(player, enabled);
    }
    
    /**
     * Check if a player can use a specific Yoshi ability
     */
    public static boolean canPlayerUseAbility(PlayerEntity player, String abilityName) {
        return ConfigManager.hasSpecificAbility(player, abilityName);
    }
    
    /**
     * Set whether a player can use a specific Yoshi ability
     */
    public static void setPlayerAbilityEnabled(PlayerEntity player, String abilityName, boolean enabled) {
        ConfigManager.setSpecificAbility(player, abilityName, enabled);
    }
    
    /**
     * Remove a player from the ability tracking (when they leave the server)
     */
    public static void removePlayer(PlayerEntity player) {
        ConfigManager.removePlayer(player);
    }
    
    /**
     * Get the current state of a player's abilities
     */
    public static boolean getPlayerAbilitiesState(PlayerEntity player) {
        return ConfigManager.hasAbilities(player);
    }
    
    /**
     * Get the current state of a specific ability for a player
     */
    public static boolean getPlayerAbilityState(PlayerEntity player, String abilityName) {
        return ConfigManager.hasSpecificAbility(player, abilityName);
    }
    
    /**
     * Toggle a player's abilities
     */
    public static boolean togglePlayerAbilities(PlayerEntity player) {
        boolean newState = !getPlayerAbilitiesState(player);
        setPlayerAbilitiesEnabled(player, newState);
        return newState;
    }
    
    /**
     * Toggle a specific ability for a player
     */
    public static boolean togglePlayerAbility(PlayerEntity player, String abilityName) {
        boolean newState = !getPlayerAbilityState(player, abilityName);
        setPlayerAbilityEnabled(player, abilityName, newState);
        return newState;
    }
    
    /**
     * Get all player ability states
     */
    public static Map<UUID, Map<String, Boolean>> getAllPlayerAbilities() {
        return ConfigManager.getAllPlayerAbilities();
    }
    
    /**
     * Get a player's specific ability states
     */
    public static Map<String, Boolean> getPlayerAbilityMap(PlayerEntity player) {
        return ConfigManager.getPlayerAbilityMap(player.getUuid());
    }
}