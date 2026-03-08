package online.inklingyoshi.yoshi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.command.YoshiCommand;
import online.inklingyoshi.yoshi.entity.YoshiEntityType;
import online.inklingyoshi.yoshi.item.YoshiItems;
import online.inklingyoshi.yoshi.network.ModPackets;
import online.inklingyoshi.yoshi.server.ServerTickHandler;
import online.inklingyoshi.yoshi.util.CooldownManager;

public class Yoshi implements ModInitializer {
    public static final String MOD_ID = "yoshi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<DamageType> GULP_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "gulp"));
    public static final RegistryKey<DamageType> GROUND_POUND_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "ground_pound"));
    
    // Sound Events
    public static final SoundEvent FLUTTER_JUMP_SOUND = SoundEvent.of(new Identifier(MOD_ID, "flutter_jump"));
    public static final SoundEvent GULP_SOUND = SoundEvent.of(new Identifier(MOD_ID, "gulp"));
    
    // Music Disc Sound Events
    public static final SoundEvent MUSIC_DISC_OPENING_MELODY = registerSoundEvent("music_disc_opening_melody");
    public static final SoundEvent MUSIC_DISC_TITLE_THEME = registerSoundEvent("music_disc_title_theme");
    public static final SoundEvent MUSIC_DISC_FLOWER_GARDEN = registerSoundEvent("music_disc_flower_garden");
    public static final SoundEvent MUSIC_DISC_ATHLETIC_THEME = registerSoundEvent("music_disc_athletic_theme");
    public static final SoundEvent MUSIC_DISC_FLUFFY_SNOW = registerSoundEvent("music_disc_fluffy_snow");
    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
    public static DamageSource of(World world, RegistryKey<DamageType> key, net.minecraft.entity.LivingEntity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
    }
    
    @Override
    public void onInitialize() {
        ModPackets.registerPackets();
        ServerTickHandler.register();
        
        // Register blocks
        online.inklingyoshi.yoshi.block.YoshiBlocks.registerBlocks();
        
        // Register items
        YoshiItems.registerItems();
        
        // Register entity types
        YoshiEntityType.registerEntityTypes();
        
        // Register event handlers
        online.inklingyoshi.yoshi.event.GoldenAppleEventHandler.register();
        online.inklingyoshi.yoshi.event.PlayerDeathEventHandler.register();
        
        // Register sound events
        Registry.register(Registries.SOUND_EVENT, new Identifier(MOD_ID, "flutter_jump"), FLUTTER_JUMP_SOUND);
        Registry.register(Registries.SOUND_EVENT, new Identifier(MOD_ID, "gulp"), GULP_SOUND);
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            YoshiCommand.register(dispatcher, registryAccess, environment);
        });
        
        // Clean up player data on disconnect to prevent memory leaks
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            CooldownManager.removePlayer(handler.getPlayer());
        });
        
        // LOGGER.info("Yoshi mod initialized successfully!");
    }
    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
