package online.inklingyoshi.yoshi.util;

import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    private static final Map<UUID, Boolean> playerAbilities = new HashMap<>();
    
    /**
     * Check if a player can use Yoshi abilities
     */
    public static boolean canPlayerUseAbilities(PlayerEntity player) {
        // Default to true if not explicitly set
        return playerAbilities.getOrDefault(player.getUuid(), true);
    }
    
    /**
     * Set whether a player can use Yoshi abilities
     */
    public static void setPlayerAbilitiesEnabled(PlayerEntity player, boolean enabled) {
        playerAbilities.put(player.getUuid(), enabled);
    }
    
    /**
     * Remove a player from the ability tracking (when they leave the server)
     */
    public static void removePlayer(PlayerEntity player) {
        playerAbilities.remove(player.getUuid());
    }
    
    /**
     * Get the current state of a player's abilities
     */
    public static boolean getPlayerAbilitiesState(PlayerEntity player) {
        return playerAbilities.getOrDefault(player.getUuid(), true);
    }
    
    /**
     * Toggle a player's abilities
     */
    public static boolean togglePlayerAbilities(PlayerEntity player) {
        boolean newState = !getPlayerAbilitiesState(player);
        setPlayerAbilitiesEnabled(player, newState);
        return newState;
    }
}