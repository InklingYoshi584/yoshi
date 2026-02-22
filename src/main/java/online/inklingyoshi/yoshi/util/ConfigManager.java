package online.inklingyoshi.yoshi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/ConfigManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/yoshi_abilities.json");
    private static Map<UUID, Map<String, Boolean>> playerAbilities = new HashMap<>();
    
    // Default ability states
    private static final Map<String, Boolean> DEFAULT_ABILITIES = new HashMap<String, Boolean>() {
        {
            put("flutter_jump", false);
            put("gulp_ability", false);
            put("create_egg", false);
            put("reel_in", false);
            put("speed", false);
            put("jump_height", false);
        }
    };
    
    static {
        loadConfig();
    }
    
    public static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) {
                // LOGGER.info("Config file does not exist, creating default");
                saveConfig();
                return;
            }
            
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<UUID, Map<String, Boolean>>>() {}.getType();
                Map<UUID, Map<String, Boolean>> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    playerAbilities = loaded;
                    // LOGGER.info("Loaded {} player ability states from config", playerAbilities.size());
                } else {
                    playerAbilities = new HashMap<>();
                    LOGGER.warn("Config file was empty or invalid, using default");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config file: {}", e.getMessage());
            playerAbilities = new HashMap<>();
        }
    }
    
    public static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs(); // Ensure config directory exists
            
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(playerAbilities, writer);
                // LOGGER.info("Saved {} player ability states to config", playerAbilities.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save config file: {}", e.getMessage());
        }
    }
    
    private static Map<String, Boolean> getPlayerAbilitiesMap(UUID playerUuid) {
        playerAbilities.computeIfAbsent(playerUuid, k -> new HashMap<>(DEFAULT_ABILITIES));
        return playerAbilities.get(playerUuid);
    }
    
    public static boolean hasAbilities(ServerPlayerEntity player) {
        // Check if all abilities are enabled
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        return abilities.values().stream().allMatch(b -> b);
    }
    
    public static boolean hasAbilities(net.minecraft.entity.player.PlayerEntity player) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        return abilities.values().stream().allMatch(b -> b);
    }
    
    public static void setAbilities(ServerPlayerEntity player, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        for (String ability : abilities.keySet()) {
            abilities.put(ability, enabled);
        }
        saveConfig();
        // LOGGER.info("Set all abilities for {} to {}", player.getName().getString(), enabled);
    }
    
    public static void setAbilities(net.minecraft.entity.player.PlayerEntity player, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        for (String ability : abilities.keySet()) {
            abilities.put(ability, enabled);
        }
        saveConfig();
        // LOGGER.info("Set all abilities for {} to {}", player.getName().getString(), enabled);
    }
    
    public static void setAbilities(UUID playerUuid, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(playerUuid);
        for (String ability : abilities.keySet()) {
            abilities.put(ability, enabled);
        }
        saveConfig();
        // LOGGER.info("Set all abilities for UUID {} to {}", playerUuid, enabled);
    }
    
    public static boolean hasSpecificAbility(ServerPlayerEntity player, String abilityName) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        return abilities.getOrDefault(abilityName, DEFAULT_ABILITIES.getOrDefault(abilityName, true));
    }
    
    public static boolean hasSpecificAbility(net.minecraft.entity.player.PlayerEntity player, String abilityName) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        return abilities.getOrDefault(abilityName, DEFAULT_ABILITIES.getOrDefault(abilityName, true));
    }
    
    public static void setSpecificAbility(ServerPlayerEntity player, String abilityName, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        abilities.put(abilityName, enabled);
        saveConfig();
        // LOGGER.info("Set ability '{}' for {} to {}", abilityName, player.getName().getString(), enabled);
    }
    
    public static void setSpecificAbility(net.minecraft.entity.player.PlayerEntity player, String abilityName, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(player.getUuid());
        abilities.put(abilityName, enabled);
        saveConfig();
        // LOGGER.info("Set ability '{}' for {} to {}", abilityName, player.getName().getString(), enabled);
    }
    
    public static void setSpecificAbility(UUID playerUuid, String abilityName, boolean enabled) {
        Map<String, Boolean> abilities = getPlayerAbilitiesMap(playerUuid);
        abilities.put(abilityName, enabled);
        saveConfig();
        // LOGGER.info("Set ability '{}' for UUID {} to {}", abilityName, playerUuid, enabled);
    }
    
    public static void removePlayer(ServerPlayerEntity player) {
        playerAbilities.remove(player.getUuid());
        saveConfig();
        // LOGGER.info("Removed ability state for {}", player.getName().getString());
    }
    
    public static void removePlayer(net.minecraft.entity.player.PlayerEntity player) {
        playerAbilities.remove(player.getUuid());
        saveConfig();
        // LOGGER.info("Removed ability state for {}", player.getName().getString());
    }
    
    public static void removePlayer(UUID playerUuid) {
        playerAbilities.remove(playerUuid);
        saveConfig();
        // LOGGER.info("Removed ability state for UUID {}", playerUuid);
    }
    
    public static Map<UUID, Map<String, Boolean>> getAllPlayerAbilities() {
        return new HashMap<>(playerAbilities);
    }
    
    public static void setAllPlayerAbilities(Map<UUID, Map<String, Boolean>> abilities) {
        playerAbilities = new HashMap<>(abilities);
        saveConfig();
        // LOGGER.info("Set all player abilities ({} entries)", playerAbilities.size());
    }
    
    public static Map<String, Boolean> getPlayerAbilityMap(UUID playerUuid) {
        return new HashMap<>(getPlayerAbilitiesMap(playerUuid));
    }
}