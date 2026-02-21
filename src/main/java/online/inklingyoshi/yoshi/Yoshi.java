package online.inklingyoshi.yoshi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import online.inklingyoshi.yoshi.command.YoshiCommand;
import online.inklingyoshi.yoshi.entity.YoshiEntityType;
import online.inklingyoshi.yoshi.item.YoshiItems;
import online.inklingyoshi.yoshi.network.ModPackets;
import online.inklingyoshi.yoshi.server.ServerTickHandler;

public class Yoshi implements ModInitializer {
    public static final String MOD_ID = "yoshi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<DamageType> GULP_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "gulp"));
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
        
        // Register items
        YoshiItems.registerItems();
        
        // Register entity types
        YoshiEntityType.registerEntityTypes();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            YoshiCommand.register(dispatcher, registryAccess, environment);
        });
        
        LOGGER.info("Yoshi mod initialized successfully!");
    }
}
