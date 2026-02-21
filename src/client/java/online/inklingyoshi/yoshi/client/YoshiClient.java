package online.inklingyoshi.yoshi.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import online.inklingyoshi.yoshi.ability.ReelInAbility;
import online.inklingyoshi.yoshi.client.renderer.YoshiEggEntityRenderer;
import online.inklingyoshi.yoshi.entity.YoshiEntityType;
import online.inklingyoshi.yoshi.network.ModPackets;
import online.inklingyoshi.yoshi.util.CooldownManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoshiClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yoshi/Client");
    private final ReelInAbility reelInAbility = new ReelInAbility();
    public static int jumpTime = 0;
    @Override
    public void onInitializeClient() {
        LOGGER.debug("Initializing Yoshi client mod");
        
        // Register entity renderers
        EntityRendererRegistry.register(YoshiEntityType.YOSHI_EGG, YoshiEggEntityRenderer::new);
        LOGGER.debug("Entity renderers registered");
        
        // Register keybindings
        KeyBindings.registerKeyBindings();
        LOGGER.debug("Keybindings registered");
        
        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {            
            // Tick cooldowns on client
            CooldownManager.tick();
            
            if (KeyBindings.REEL_IN.wasPressed()) {
                LOGGER.debug("Reel-in key pressed");
                // Start reel-in ability
                if (client.player != null) {
                    boolean canUse = CooldownManager.canUseAbility(client.player, "reel_in");
                    int cooldown = CooldownManager.getCooldown(client.player, "reel_in");
                    LOGGER.debug("Reel-in ability check: canUse={}, cooldown={}", canUse, cooldown);
                    if (canUse) {
                        LOGGER.debug("Starting reel-in ability");
                        reelInAbility.startReelIn(client.player);
                        // Send packet to server (server will handle cooldown)
                        ClientNetworking.sendReelInPacket();
                        LOGGER.debug("Reel-in ability packet sent to server");
                    } else {
                        LOGGER.debug("Reel-in ability on cooldown, cannot use");
                    }
                } else {
                    LOGGER.debug("Client player is null, cannot use reel-in ability");
                }
            }
            
            if (KeyBindings.CREATE_EGG.wasPressed()) {
                LOGGER.debug("Create egg key pressed");
                // Handle create egg ability
                if (client.player != null) {
                    boolean canUse = CooldownManager.canUseAbility(client.player, "create_egg");
                    int cooldown = CooldownManager.getCooldown(client.player, "create_egg");
                    LOGGER.debug("Create egg ability check: canUse={}, cooldown={}", canUse, cooldown);
                    if (canUse) {
                        LOGGER.debug("Sending create egg packet to server");
                        // Send packet to server (server will handle hunger/saturation and cooldown)
                        ClientNetworking.sendCreateEggPacket();
                        LOGGER.debug("Create egg packet sent to server");
                    } else {
                        LOGGER.debug("Create egg ability on cooldown, cannot use");
                    }
                } else {
                    LOGGER.debug("Client player is null, cannot use create egg ability");
                }
            }
            
            // Update reel-in ability only if there's an active target
            if (client.player != null && reelInAbility.isReeling()) {
                LOGGER.debug("Updating reel-in ability for active target");
                reelInAbility.updateReelIn(client.player);
            }
            KeyBinding jumpKey = client.options.jumpKey;
            if (!jumpKey.isPressed()) {
                jumpTime = 0;
            }
            // Handle flutter jump
            handleFlutterJump(client);
        });
        
        LOGGER.debug("Tick handler registered successfully");
    }
    
    private void handleFlutterJump(MinecraftClient client) {
        if (client.player != null) {
            PlayerEntity player = client.player;
            KeyBinding jumpKey = client.options.jumpKey;
            
            // Check if player is in midair and holding spacebar
            if (!player.isOnGround() && !player.isTouchingWater() && jumpKey.isPressed()) {
                LOGGER.debug("Flutter jump conditions met: onGround={}, touchingWater={}, jumpPressed={}, velocityY={}", 
                    player.isOnGround(), player.isTouchingWater(), jumpKey.isPressed(), player.getVelocity().y);
                
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "flutter_jump");
                int cooldown = CooldownManager.getCooldown(player, "flutter_jump");
                LOGGER.debug("Flutter jump ability check: canUse={}, cooldown={}", canUse, cooldown);
                
                if (canUse) {
                    Vec3d velocity = player.getVelocity();
                    if (velocity.y < 0.0) {
                        LOGGER.debug("Sending flutter jump packet to server");
                        jumpTime++;
                        LOGGER.debug("jumptime: {}", jumpTime);
                        // Send packet to server (server will handle velocity AND cooldown)
                        ClientNetworking.sendFlutterJumpPacket(true, jumpTime >= 15);
                        LOGGER.debug("Flutter jump packet sent to server");
                    } else {
                        LOGGER.debug("Flutter jump not applied: player not falling (velocity.y={})", velocity.y);
                    }
                } else {
                    LOGGER.debug("Flutter jump on cooldown, cannot use");
                }
            }
        }
    }
}
