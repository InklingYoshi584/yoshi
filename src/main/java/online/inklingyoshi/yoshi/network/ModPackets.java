package online.inklingyoshi.yoshi.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.server.ServerTickHandler;
import online.inklingyoshi.yoshi.util.AbilityManager;
import online.inklingyoshi.yoshi.util.CooldownManager;
import online.inklingyoshi.yoshi.Yoshi;

public class ModPackets {
    public static final Identifier FLUTTER_JUMP_PACKET = new Identifier(Yoshi.MOD_ID, "flutter_jump");
    public static final Identifier REEL_IN_PACKET = new Identifier(Yoshi.MOD_ID, "reel_in");
    public static final Identifier CREATE_EGG_PACKET = new Identifier(Yoshi.MOD_ID, "create_egg");
    
    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(FLUTTER_JUMP_PACKET, (server, player, handler, buf, responseSender) -> {
            boolean isHoldingJump = buf.readBoolean();
            boolean shouldResetCD = buf.readBoolean();
            
            System.out.println("Server: Received flutter jump packet for player " + player.getName().getString() + 
                " - isHoldingJump=" + isHoldingJump + ", shouldResetCD=" + shouldResetCD);
            
            server.execute(() -> {
                if (isHoldingJump) {
                    if (shouldResetCD) {
                        CooldownManager.startCooldown(player, "flutter_jump");
                        Vec3d currentVelocity = player.getVelocity();
                        Vec3d newVelocity = new Vec3d(currentVelocity.x * 0.2, -0.1, currentVelocity.z  * 0.2);
                        player.setVelocity(newVelocity);
                        return;
                    }
                    System.out.println("Server: Processing flutter jump for player " + player.getName().getString());
                    int jump_ticks = 0;
                    
                    // Check if player has abilities enabled
                    boolean abilitiesEnabled = AbilityManager.canPlayerUseAbilities(player);
                    if (!abilitiesEnabled) {
                        System.out.println("Server: Flutter jump blocked - player abilities disabled");
                        return;
                    }
                    
                    // Check if we can start or continue fluttering
                    boolean canUse = CooldownManager.canUseAbility(player, "flutter_jump");
                    boolean isFluttering = CooldownManager.isAbilityActive(player, "flutter_jump");
                    
                    System.out.println("Server: Flutter jump state - canUse=" + canUse + ", isFluttering=" + isFluttering);
                    
                    // Handle flutter jump on server
                    boolean onGround = player.isOnGround();
                    boolean touchingWater = player.isTouchingWater();
                    double velocityY = player.getVelocity().y;
                    
                    System.out.println("Server: Flutter jump conditions - onGround=" + onGround + 
                        ", touchingWater=" + touchingWater + ", velocityY=" + velocityY);
                    
                    if (!onGround && !touchingWater && velocityY < 0.0) {
                        if (canUse && !isFluttering) {
                            // Start flutter jump (first activation)
                            System.out.println("Server: Starting flutter jump for player " + player.getName().getString());
                            CooldownManager.startAbility(player, "flutter_jump", 20); // 1 second max duration
                            isFluttering = true; // Update local variable
                        }
                        
                        if (isFluttering && jump_ticks < 20 && !shouldResetCD) {
                            // Apply continuous flutter jump velocity (reduce downward velocity)
                            Vec3d currentVelocity = player.getVelocity();
                            Vec3d newVelocity = new Vec3d(currentVelocity.x * 1.3, 0.08, currentVelocity.z  * 1.3);
                            player.setVelocity(newVelocity);
                            player.velocityModified = true;
                            jump_ticks++;
                            
                            // Log the velocity change
                            System.out.println("Server: Applied flutter jump to player " + player.getName().getString() + 
                                " - velocity changed from " + currentVelocity + " to " + newVelocity);
                        }
                    } else {
                        // Reset flutter jump if conditions are not met
                        if (isFluttering) {
                            CooldownManager.endAbility(player, "flutter_jump");
                            System.out.println("Server: Flutter jump ended due to conditions not met");
                        }
                        System.out.println("Server: Flutter jump conditions not met");
                    }
                } else {
                    // Player released spacebar - end flutter jump
                    if (CooldownManager.isAbilityActive(player, "flutter_jump")) {
                        CooldownManager.endAbility(player, "flutter_jump");
                        System.out.println("Server: Flutter jump ended (spacebar released)");
                    }
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(REEL_IN_PACKET, (server, player, handler, buf, responseSender) -> {
            System.out.println("Server: Received reel-in packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check if player has abilities enabled
                boolean abilitiesEnabled = AbilityManager.canPlayerUseAbilities(player);
                if (!abilitiesEnabled) {
                    System.out.println("Server: Reel-in blocked - player abilities disabled");
                    return;
                }
                
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
        
        ServerPlayNetworking.registerGlobalReceiver(CREATE_EGG_PACKET, (server, player, handler, buf, responseSender) -> {
            System.out.println("Server: Received create egg packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check if player has abilities enabled
                boolean abilitiesEnabled = AbilityManager.canPlayerUseAbilities(player);
                if (!abilitiesEnabled) {
                    System.out.println("Server: Create egg blocked - player abilities disabled");
                    return;
                }
                
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "create_egg");
                System.out.println("Server: Create egg cooldown check - canUse=" + canUse);
                
                if (canUse) {
                    // Handle create egg ability on server
                    System.out.println("Server: Starting create egg ability for player " + player.getName().getString());
                    
                    // Check if player has enough hunger
                    int currentHunger = player.getHungerManager().getFoodLevel();
                    float currentSaturation = player.getHungerManager().getSaturationLevel();
                    
                    System.out.println("Server: Player hunger=" + currentHunger + ", saturation=" + currentSaturation);
                    
                    if (currentHunger >= 6 || currentSaturation >= 6) {
                        // Consume hunger and saturation
                        player.getHungerManager().addExhaustion(24.0f); // 6 hunger points converted to exhaustion
                        
                        // Create Yoshi egg item
                        net.minecraft.item.ItemStack yoshiEgg = new net.minecraft.item.ItemStack(online.inklingyoshi.yoshi.item.YoshiItems.YOSHI_EGG, 1);
                        
                        // Give egg to player or drop it if inventory is full
                        if (!player.getInventory().insertStack(yoshiEgg)) {
                            player.dropItem(yoshiEgg, false);
                        }
                        
                        // Play egg creation sound
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                            net.minecraft.sound.SoundEvents.ENTITY_CHICKEN_EGG, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                        
                        // Start cooldown
                        CooldownManager.startCooldown(player, "create_egg"); // 2 seconds
                        System.out.println("Server: Create egg ability activated and cooldown started");
                    } else {
                        System.out.println("Server: Create egg failed - insufficient hunger/saturation");
                    }
                } else {
                    System.out.println("Server: Create egg ability on cooldown");
                }
            });
        });
    }
};
 