package online.inklingyoshi.yoshi.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import online.inklingyoshi.yoshi.Yoshi;

public class YoshiEntityType {
    public static final EntityType<YoshiEggEntity> YOSHI_EGG = EntityType.Builder.<YoshiEggEntity>create(YoshiEggEntity::new, SpawnGroup.MISC)
        .setDimensions(0.5f, 0.5f) // Increased hitbox size (default egg is 0.25f)
        .maxTrackingRange(4)
        .trackingTickInterval(10)
        .build("yoshi_egg");
    
    public static void registerEntityTypes() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier(Yoshi.MOD_ID, "yoshi_egg"), YOSHI_EGG);
    }
}