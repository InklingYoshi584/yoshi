package online.inklingyoshi.yoshi.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.server.ServerTickHandler;
import online.inklingyoshi.yoshi.util.CooldownManager;
import online.inklingyoshi.yoshi.Yoshi;

public class ModPackets {
    public static final Identifier FLUTTER_JUMP_PACKET = new Identifier(Yoshi.MOD_ID, "flutter_jump");
    public static final Identifier REEL_IN_PACKET = new Identifier(Yoshi.MOD_ID, "reel_in");
    
    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(FLUTTER_JUMP_PACKET, (server, player, handler, buf, responseSender) -> {
            boolean isHoldingJump = buf.readBoolean();
            
            System.out.println("Server: Received flutter jump packet for player " + player.getName().getString() + 
                " - isHoldingJump=" + isHoldingJump);
            
            server.execute(() -> {
                if (isHoldingJump) {
                    System.out.println("Server: Processing flutter jump for player " + player.getName().getString());
                    
                    // Check cooldown
                    boolean canUse = CooldownManager.canUseAbility(player, "flutter_jump");
                    System.out.println("Server: Flutter jump cooldown check - canUse=" + canUse);
                    
                    if (canUse) {
                        // Handle flutter jump on server
                        boolean onGround = player.isOnGround();
                        boolean touchingWater = player.isTouchingWater();
                        double velocityY = player.getVelocity().y;
                        
                        System.out.println("Server: Flutter jump conditions - onGround=" + onGround + 
                            ", touchingWater=" + touchingWater + ", velocityY=" + velocityY);
                        
                        if (!onGround && !touchingWater && velocityY < 0.0) {
                            // Apply flutter jump velocity (reduce downward velocity)
                            Vec3d currentVelocity = player.getVelocity();
                            Vec3d newVelocity = new Vec3d(currentVelocity.x, currentVelocity.y * 0.7, currentVelocity.z);
                            player.setVelocity(newVelocity);
                            player.velocityModified = true;
                            
                            // Start cooldown
                            CooldownManager.startCooldown(player, "flutter_jump");
                            
                            // Log the velocity change
                            System.out.println("Server: Applied flutter jump to player " + player.getName().getString() + 
                                " - velocity changed from " + currentVelocity + " to " + newVelocity);
                        } else {
                            System.out.println("Server: Flutter jump conditions not met");
                        }
                    } else {
                        System.out.println("Server: Flutter jump on cooldown");
                    }
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(REEL_IN_PACKET, (server, player, handler, buf, responseSender) -> {
            System.out.println("Server: Received reel-in packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "reel_in");
                System.out.println("Server: Reel-in cooldown check - canUse=" + canUse);
                
                if (canUse) {
                    // Handle reel-in ability on server
                    System.out.println("Server: Starting reel-in ability for player " + player.getName().getString());
                    ServerTickHandler.startReelIn(player);
                    // Start cooldown
                    CooldownManager.startCooldown(player, "reel_in");
                    System.out.println("Server: Reel-in ability activated and cooldown started");
                } else {
                    System.out.println("Server: Reel-in ability on cooldown");
                }
            });
        });
    }
};
 