package online.inklingyoshi.yoshi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import online.inklingyoshi.yoshi.util.AbilityManager;

import java.util.Collection;

public class YoshiCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("yoshi")
            .requires(source -> source.hasPermissionLevel(2)) // Requires OP permission
            .then(CommandManager.argument("player", StringArgumentType.word())
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> executeSetAbility(context, StringArgumentType.getString(context, "player"), BoolArgumentType.getBool(context, "enabled")))))
            .executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal("Usage: /yoshi <player> <true|false>"), false);
                return 1;
            })
        );
    }
    
    private static int executeSetAbility(CommandContext<ServerCommandSource> context, String playerName, boolean enabled) {
        ServerCommandSource source = context.getSource();
        
        // Get all players on the server
        Collection<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
        ServerPlayerEntity targetPlayer = null;
        
        // Find the target player
        for (ServerPlayerEntity player : players) {
            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                targetPlayer = player;
                break;
            }
        }
        
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found or not online"));
            return 0;
        }
        
        // Set ability state
        AbilityManager.setPlayerAbilitiesEnabled(targetPlayer, enabled);
        
        String status = enabled ? "enabled" : "disabled";
        String playerName2 = targetPlayer.getName().getString();
        source.sendFeedback(() -> Text.literal("Yoshi abilities " + status + " for player " + playerName2), true);
        
        // Notify the target player
        if (enabled) {
            targetPlayer.sendMessage(Text.literal("Your Yoshi abilities have been enabled!"), false);
        } else {
            targetPlayer.sendMessage(Text.literal("Your Yoshi abilities have been disabled."), false);
        }
        
        return 1;
    }
}