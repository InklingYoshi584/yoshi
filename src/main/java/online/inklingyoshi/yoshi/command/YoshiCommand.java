package online.inklingyoshi.yoshi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import online.inklingyoshi.yoshi.util.AbilityManager;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YoshiCommand {
    
    // Ability name suggestions for autocompletion
    private static final SuggestionProvider<ServerCommandSource> ABILITY_SUGGESTIONS = 
        (context, builder) -> {
            // List of available abilities for autocompletion
            String[] abilities = {
                "flutter_jump",
                "gulp_ability", 
                "create_egg",
                "reel_in",
                "speed",
                "jump_height",
                "all"
            };
            
            for (String ability : abilities) {
                builder.suggest(ability);
            }
            
            return builder.buildFuture();
        };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("yoshi")
            .requires(source -> source.hasPermissionLevel(2)) // Requires OP permission
            
            // /yoshi player <player> <true|false>
            .then(CommandManager.literal("player")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> executeSetAbility(context, EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "enabled"))))))
            
            // /yoshi abilities <player> <ability> <true|false>
            .then(CommandManager.literal("abilities")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("ability", StringArgumentType.string())
                        .suggests(ABILITY_SUGGESTIONS)
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(context -> executeSetSpecificAbility(context, EntityArgumentType.getPlayer(context, "player"), StringArgumentType.getString(context, "ability"), BoolArgumentType.getBool(context, "enabled")))))))
            
            // /yoshi list
            .then(CommandManager.literal("list")
                .executes(context -> executeListAbilities(context)))
            
            // /yoshi reload
            .then(CommandManager.literal("reload")
                .executes(context -> executeReloadConfig(context)))
            
            .executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal(
                    "Yoshi Ability Management Commands:\n" +
                    "/yoshi player <player> <true|false> - Set abilities for specific player\n" +
                    "/yoshi abilities <player> <ability> <true|false> - Set specific ability for player\n" +
                    "/yoshi list - List all players with abilities enabled\n" +
                    "/yoshi reload - Reload config from file"
                ), false);
                return 1;
            })
        );
    }
    
    private static int executeSetAbility(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer, boolean enabled) {
        
        // Set ability state
        AbilityManager.setPlayerAbilitiesEnabled(targetPlayer, enabled);
        
        // Send feedback
        String message = "Yoshi abilities " + (enabled ? "enabled" : "disabled") + " for " + targetPlayer.getName().getString();
        context.getSource().sendFeedback(() -> Text.literal(message), true);
        
        return 1;
    }
    
    private static int executeSetAbilityByUuid(CommandContext<ServerCommandSource> context, String uuidString, boolean enabled) {
        try {
            UUID playerUuid = UUID.fromString(uuidString);
            
            // Set ability state by UUID
            online.inklingyoshi.yoshi.util.ConfigManager.setAbilities(playerUuid, enabled);
            
            // Send feedback
            String message = "Yoshi abilities " + (enabled ? "enabled" : "disabled") + " for UUID " + playerUuid;
            context.getSource().sendFeedback(() -> Text.literal(message), true);
            
            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("Invalid UUID format: " + uuidString));
            return 0;
        }
    }
    
    private static int executeSetAllAbilities(CommandContext<ServerCommandSource> context, boolean enabled) {
        ServerCommandSource source = context.getSource();
        Collection<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
        
        int affectedPlayers = 0;
        
        // Set abilities for all online players
        for (ServerPlayerEntity player : players) {
            AbilityManager.setPlayerAbilitiesEnabled(player, enabled);
            affectedPlayers++;
        }
        
        // Send feedback
        String message = "Yoshi abilities " + (enabled ? "enabled" : "disabled") + " for " + affectedPlayers + " online players";
        source.sendFeedback(() -> Text.literal(message), true);
        
        return 1;
    }
    
    private static int executeListAbilities(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Map<UUID, Map<String, Boolean>> allAbilities = AbilityManager.getAllPlayerAbilities();
        
        if (allAbilities.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No players have Yoshi abilities enabled"), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("Players with Yoshi abilities enabled:"), false);
        
        // Use an array wrapper to make the count effectively final for lambda
        final int[] enabledCount = {0};
        
        for (Map.Entry<UUID, Map<String, Boolean>> entry : allAbilities.entrySet()) {
            boolean hasEnabled = false;
            for (Boolean enabled : entry.getValue().values()) {
                if (Boolean.TRUE.equals(enabled)) {
                    hasEnabled = true;
                    break;
                }
            }
            if (hasEnabled) {
                final UUID playerUuid = entry.getKey(); // Make UUID effectively final
                source.sendFeedback(() -> Text.literal("  - " + playerUuid), false);
                enabledCount[0]++;
            }
        }
        
        source.sendFeedback(() -> Text.literal("Total: " + enabledCount[0] + " players"), false);
        
        return 1;
    }
    
    private static int executeSetSpecificAbility(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer, String abilityName, boolean enabled) {
        // Set specific ability state
        AbilityManager.setPlayerAbilityEnabled(targetPlayer, abilityName, enabled);
        
        // Send feedback
        String message = "Yoshi ability '" + abilityName + "' " + (enabled ? "enabled" : "disabled") + " for " + targetPlayer.getName().getString();
        context.getSource().sendFeedback(() -> Text.literal(message), true);
        
        // Notify the target player
        if (enabled) {
            targetPlayer.sendMessage(Text.literal("Your Yoshi ability '" + abilityName + "' has been enabled!"), false);
        } else {
            targetPlayer.sendMessage(Text.literal("Your Yoshi ability '" + abilityName + "' has been disabled."), false);
        }
        
        return 1;
    }
    
    private static int executeReloadConfig(CommandContext<ServerCommandSource> context) {
        online.inklingyoshi.yoshi.util.ConfigManager.loadConfig();
        context.getSource().sendFeedback(() -> Text.literal("Yoshi config reloaded from file"), true);
        return 1;
    }
}