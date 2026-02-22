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
    private static boolean wasFluttering = false;
    @Override
    public void onInitializeClient() {
        // LOGGER.info("Initializing Yoshi client mod");
        
        // Register entity renderers
        EntityRendererRegistry.register(YoshiEntityType.YOSHI_EGG, YoshiEggEntityRenderer::new);
        // LOGGER.info("Entity renderers registered");
        
        // Register keybindings
        KeyBindings.registerKeyBindings();
        // LOGGER.info("Keybindings registered");
        
        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {            
            // Tick cooldowns on client
            CooldownManager.tick();
            
            if (KeyBindings.REEL_IN.wasPressed()) {
                // LOGGER.info("Reel-in key pressed");
                // Start reel-in ability
                if (client.player != null) {
                    boolean canUse = CooldownManager.canUseAbility(client.player, "reel_in");
                    int cooldown = CooldownManager.getCooldown(client.player, "reel_in");
                    // LOGGER.info("Reel-in ability check: canUse={}, cooldown={}", canUse, cooldown);
                    if (canUse) {
                        // LOGGER.info("Starting reel-in ability");
                        reelInAbility.startReelIn(client.player);
                        // Send packet to server (server will handle cooldown)
                        ClientNetworking.sendReelInPacket();
                        // LOGGER.info("Reel-in ability packet sent to server");
                    } else {
                        // LOGGER.info("Reel-in ability on cooldown, cannot use");
                    }
                } else {
                    // LOGGER.info("Client player is null, cannot use reel-in ability");
                }
            }
            
            if (KeyBindings.CREATE_EGG.wasPressed()) {
                // LOGGER.info("Create egg key pressed");
                // Handle create egg ability
                if (client.player != null) {
                    boolean canUse = CooldownManager.canUseAbility(client.player, "create_egg");
                    int cooldown = CooldownManager.getCooldown(client.player, "create_egg");
                    // LOGGER.info("Create egg ability check: canUse={}, cooldown={}", canUse, cooldown);
                    if (canUse) {
                        // LOGGER.info("Sending create egg packet to server");
                        // Send packet to server (server will handle hunger/saturation and cooldown)
                        ClientNetworking.sendCreateEggPacket();
                        // LOGGER.info("Create egg packet sent to server");
                    } else {
                        // LOGGER.info("Create egg ability on cooldown, cannot use");
                    }
                } else {
                    // LOGGER.info("Client player is null, cannot use create egg ability");
                }
            }
            
            // Update reel-in ability only if there's an active target
            if (client.player != null && reelInAbility.isReeling()) {
                // LOGGER.info("Updating reel-in ability for active target");
                reelInAbility.updateReelIn(client.player);
            }
            KeyBinding jumpKey = client.options.jumpKey;
            if (!jumpKey.isPressed()) {
                jumpTime = 0;
            }
            // Handle flutter jump
            handleFlutterJump(client);
        });
        
        // LOGGER.info("Tick handler registered successfully");
    }
    
    private void handleFlutterJump(MinecraftClient client) {
        if (client.player != null) {
            PlayerEntity player = client.player;
            KeyBinding jumpKey = client.options.jumpKey;
            
            // Check if player was fluttering but released spacebar
            if (wasFluttering && !jumpKey.isPressed()) {
                // LOGGER.info("---Flutter jump ended prematurely (spacebar released)");
                // Send packet to end flutter jump
                ClientNetworking.sendFlutterJumpPacket(true, true);
                wasFluttering = false;
                jumpTime = 0;
            }
            
            // Check if player is in midair and holding spacebar
            if (!player.isOnGround() && !player.isTouchingWater() && jumpKey.isPressed() && !player.isFallFlying() && !player.isCreative()) {
                // LOGGER.info("Flutter jump conditions met: onGround={}, touchingWater={}, jumpPressed={}, velocityY={}", 
                    // player.isOnGround(), player.isTouchingWater(), jumpKey.isPressed(), player.getVelocity().y);
                // LOGGER.info("wasFluttering={}", wasFluttering);
                    // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "flutter_jump");
                int cooldown = CooldownManager.getCooldown(player, "flutter_jump");
                // LOGGER.info("Flutter jump ability check: canUse={}, cooldown={}", canUse, cooldown);
                
                if (canUse) {
                    Vec3d velocity = player.getVelocity();
                    if (velocity.y < 0.2 || wasFluttering) {
                        // LOGGER.info("Sending flutter jump packet to server");
                        jumpTime++;
                        // LOGGER.info("jumptime: {}", jumpTime);
                        // Send packet to server (server will handle velocity AND cooldown)
                        ClientNetworking.sendFlutterJumpPacket(true, jumpTime >= 15);
                        wasFluttering = true;
                        // LOGGER.info("Flutter jump packet sent to server");
                    } else {
                        // LOGGER.info("Flutter jump not applied: player not falling (velocity.y={})", velocity.y);
                    }
                } else {
                    // LOGGER.info("Flutter jump on cooldown, cannot use");
                    wasFluttering = false;
                }
            } else {
                // Player is on ground or not holding spacebar
                if(wasFluttering){
                    // LOGGER.info("---Flutter jump ended (player on ground)");
                    // Send packet to end flutter jump
                    ClientNetworking.sendFlutterJumpPacket(true, true);
                }
                wasFluttering = false;
                jumpTime = 0;
            }
        }
    }
}
