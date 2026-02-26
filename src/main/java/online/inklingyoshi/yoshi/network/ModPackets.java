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
    public static final Identifier GROUND_POUND_PACKET = new Identifier(Yoshi.MOD_ID, "ground_pound");
    
    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(FLUTTER_JUMP_PACKET, (server, player, handler, buf, responseSender) -> {
            boolean isHoldingJump = buf.readBoolean();
            boolean shouldResetCD = buf.readBoolean();
            
            // System.out.println("Server: Received flutter jump packet for player " + player.getName().getString() + 
            //     " - isHoldingJump=" + isHoldingJump + ", shouldResetCD=" + shouldResetCD);
            
            server.execute(() -> {  
                if (isHoldingJump) {
                    if (shouldResetCD) {
                        CooldownManager.startCooldown(player, "flutter_jump");
                        Vec3d currentVelocity = player.getVelocity();
                        Vec3d newVelocity = new Vec3d(currentVelocity.x, 0.2, currentVelocity.z);
                        if (currentVelocity.y > 0.4) {
                            player.setVelocity(newVelocity);
                        }
                        player.setNoGravity(false);
                        return;
                    }
                    // System.out.println("Server: Processing flutter jump for player " + player.getName().getString());
                    
                    // Check if player has flutter jump ability enabled
                    boolean hasFlutterJump = AbilityManager.canPlayerUseAbility(player, "flutter_jump");
                    if (!hasFlutterJump) {
                        // System.out.println("Server: Flutter jump blocked - flutter_jump ability disabled");
                        player.setNoGravity(false);
                        return;
                    }
                    
                    // Check if we can start or continue fluttering
                    boolean canUse = CooldownManager.canUseAbility(player, "flutter_jump");
                    boolean isFluttering = CooldownManager.isAbilityActive(player, "flutter_jump");
                    
                    // System.out.println("Server: Flutter jump state - canUse=" + canUse + ", isFluttering=" + isFluttering);
                    
                    // Handle flutter jump on server
                    boolean onGround = player.isOnGround();
                    boolean touchingWater = player.isTouchingWater();
                    double velocityY = player.getVelocity().y;
                    
                    // System.out.println("Server: Flutter jump conditions - onGround=" + onGround + 
                    //    ", touchingWater=" + touchingWater + ", velocityY=" + velocityY);
                    
                    if (!onGround && !touchingWater) {
                        if (canUse && !CooldownManager.isAbilityActive(player, "flutter_jump")) {
                            // Start flutter jump (first activation)
                            // System.out.println("Server: Starting flutter jump for player " + player.getName().getString());
                            CooldownManager.startAbility(player, "flutter_jump", 20); // 1 second max duration
                            isFluttering = true; // Update local variable
                            
                            // Play flutter jump sound
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), Yoshi.FLUTTER_JUMP_SOUND, player.getSoundCategory(), 1.0f, 1.0f);
                        }
                        
                        if (isFluttering && CooldownManager.getAbilityRemainingDuration(player, "flutter_jump") > 0 && !shouldResetCD && !player.isOnGround()) {
                            // Apply continuous flutter jump velocity in the direction the player is looking
                            Vec3d currentVelocity = player.getVelocity();
                            player.setNoGravity(true);
                            
                            // Get player's look direction
                            Vec3d lookDirection = player.getRotationVec(1.0f).normalize();
                            
                            // Calculate acceleration based on player's looking direction
                            double forwardAcceleration = 0.03; // Forward acceleration
                            double upwardAcceleration = currentVelocity.y < -0.1 ? 0.1 : (currentVelocity.y > 0.12 ? 0.02 : 0.04); // Vertical acceleration
                            if (currentVelocity.y < -0.2 || currentVelocity.y > 0.06) {
                                Vec3d newVelocity = new Vec3d(currentVelocity.x, -0.1, currentVelocity.z);
                                player.setVelocity(newVelocity);
                                player.velocityModified = true;
                                // System.out.println("Server: Flutter jump velocity reset for player " + player.getName().getString());
                                return;
                            }
                            // Calculate new velocity components
                            double newX = lookDirection.x * forwardAcceleration;
                            double newY = upwardAcceleration;
                            double newZ = lookDirection.z * forwardAcceleration;
                            
                            if (newY + currentVelocity.y > 0.06) {
                                newY = 0.06 - currentVelocity.y;
                            }
                            // Apply the acceleration
                            Vec3d acceleration = new Vec3d(newX, newY, newZ);
                            player.addVelocity(acceleration);
                            
                            player.velocityModified = true;
                            
                            // Play looping flutter jump sound every 5 ticks
                            int remainingDuration = CooldownManager.getAbilityRemainingDuration(player, "flutter_jump");
                            int elapsedTicks = 20 - remainingDuration; // Calculate how many ticks have passed
                            if (elapsedTicks > 0 && elapsedTicks % 2 == 0) {
                                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), Yoshi.FLUTTER_JUMP_SOUND, player.getSoundCategory(), 0.7f, 1.0f + (float) Math.random() * 0.1f);
                            }
                        }
                    } else {
                        // Reset flutter jump if conditions are not met
                        if (isFluttering) {
                            CooldownManager.endAbility(player, "flutter_jump");
                            // System.out.println("Server: Flutter jump ended due to conditions not met");
                            
                        }
                        player.setNoGravity(false);
                        // System.out.println("Server: Flutter jump conditions not met");
                    }
                } else {
                    // Player released spacebar - end flutter jump
                    if (CooldownManager.isAbilityActive(player, "flutter_jump")) {
                        CooldownManager.endAbility(player, "flutter_jump");
                        player.setNoGravity(false);
                        // System.out.println("Server: Flutter jump ended (spacebar released)");
                    }
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(REEL_IN_PACKET, (server, player, handler, buf, responseSender) -> {
            // System.out.println("Server: Received reel-in packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check if player has reel-in ability enabled
                boolean hasReelInAbility = AbilityManager.canPlayerUseAbility(player, "reel_in");
                if (!hasReelInAbility) {
                    // System.out.println("Server: Reel-in blocked - reel_in ability disabled");
                    return;
                }
                
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "reel_in");
                // System.out.println("Server: Reel-in cooldown check - canUse=" + canUse);
                
                if (canUse) {
                    // Handle reel-in ability on server
                    // System.out.println("Server: Starting reel-in ability for player " + player.getName().getString());
                    
                    // Play gulp sound when button is pressed
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), Yoshi.GULP_SOUND, player.getSoundCategory(), 1.0f, 1.0f);
                    
                    ServerTickHandler.startReelIn(player);
                    // Start cooldown
                    CooldownManager.startCooldown(player, "reel_in");
                    // System.out.println("Server: Reel-in ability activated and cooldown started");
                } else {
                    // System.out.println("Server: Reel-in ability on cooldown");
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(CREATE_EGG_PACKET, (server, player, handler, buf, responseSender) -> {
            // System.out.println("Server: Received create egg packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check if player has create egg ability enabled
                boolean hasCreateEggAbility = AbilityManager.canPlayerUseAbility(player, "create_egg");
                if (!hasCreateEggAbility) {
                    // System.out.println("Server: Create egg blocked - create_egg ability disabled");
                    return;
                }
                
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "create_egg");
                // System.out.println("Server: Create egg cooldown check - canUse=" + canUse);
                
                if (canUse) {
                    // Handle create egg ability on server
                    // System.out.println("Server: Starting create egg ability for player " + player.getName().getString());
                    
                    // Check if player has enough hunger
                    int currentHunger = player.getHungerManager().getFoodLevel();
                    float currentSaturation = player.getHungerManager().getSaturationLevel();
                    
                    // System.out.println("Server: Player hunger=" + currentHunger + ", saturation=" + currentSaturation);
                    
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
                        // System.out.println("Server: Create egg ability activated and cooldown started");
                    } else {
                        // System.out.println("Server: Create egg failed - insufficient hunger/saturation");
                    }
                } else {
                    // System.out.println("Server: Create egg ability on cooldown");
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
            // System.out.println("Server: Received ground pound packet for player " + player.getName().getString());
            
            server.execute(() -> {
                // Check if player has ground pound ability enabled
                boolean hasGroundPoundAbility = AbilityManager.canPlayerUseAbility(player, "ground_pound");
                if (!hasGroundPoundAbility) {
                    // System.out.println("Server: Ground pound blocked - ground_pound ability disabled");
                    return;
                }
                
                // Check cooldown
                boolean canUse = CooldownManager.canUseAbility(player, "ground_pound");
                // System.out.println("Server: Ground pound cooldown check - canUse=" + canUse);
                
                if (canUse) {
                    // Handle ground pound ability on server
                    // System.out.println("Server: Starting ground pound ability for player " + player.getName().getString());
                    
                    // Activate ground pound ability
                    online.inklingyoshi.yoshi.ability.GroundPoundAbility.activate(player);
                    
                    // Start cooldown
                    CooldownManager.startCooldown(player, "ground_pound");
                    // System.out.println("Server: Ground pound ability activated and cooldown started");
                } else {
                    // System.out.println("Server: Ground pound ability on cooldown");
                }
            });
        });
    }
};
