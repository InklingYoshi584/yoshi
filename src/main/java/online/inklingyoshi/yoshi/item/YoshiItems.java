package online.inklingyoshi.yoshi.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import online.inklingyoshi.yoshi.Yoshi;
import online.inklingyoshi.yoshi.block.YoshiBlocks;

public class YoshiItems {
    public static Item YOSHI_EGG;
    
    // Music Disc Items
    public static Item MUSIC_DISC_OPENING_MELODY;
    public static Item MUSIC_DISC_TITLE_THEME;
    public static Item MUSIC_DISC_FLOWER_GARDEN;
    public static Item MUSIC_DISC_ATHLETIC_THEME;
    public static Item MUSIC_DISC_FLUFFY_SNOW;
    
    // Golden Egg Item
    public static Item GOLDEN_EGG;
    
    // Reinforced Obsidian Item
    public static Item REINFORCED_OBSIDIAN;
    
    public static void registerItems() {
        YOSHI_EGG = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "yoshi_egg"), 
            new YoshiEggItem(new FabricItemSettings().maxCount(16)));
        
        // Register Music Discs
        MUSIC_DISC_OPENING_MELODY = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "music_disc_opening_melody"), 
            new MusicDiscItem(1, Yoshi.MUSIC_DISC_OPENING_MELODY, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 120));
        
        MUSIC_DISC_TITLE_THEME = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "music_disc_title_theme"), 
            new MusicDiscItem(2, Yoshi.MUSIC_DISC_TITLE_THEME, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 180));
        
        MUSIC_DISC_FLOWER_GARDEN = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "music_disc_flower_garden"), 
            new MusicDiscItem(3, Yoshi.MUSIC_DISC_FLOWER_GARDEN, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 150));
        
        MUSIC_DISC_ATHLETIC_THEME = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "music_disc_athletic_theme"), 
            new MusicDiscItem(4, Yoshi.MUSIC_DISC_ATHLETIC_THEME, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 140));
        
        MUSIC_DISC_FLUFFY_SNOW = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "music_disc_fluffy_snow"), 
            new MusicDiscItem(5, Yoshi.MUSIC_DISC_FLUFFY_SNOW, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 160));
        
        // Register Golden Egg Item
        GOLDEN_EGG = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "golden_egg"), 
            new GoldenEggItem((online.inklingyoshi.yoshi.block.GoldenEggBlock) YoshiBlocks.GOLDEN_EGG, 
                new FabricItemSettings().maxCount(1)));
        
        // Register Reinforced Obsidian Item
        REINFORCED_OBSIDIAN = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "reinforced_obsidian"), 
            new net.minecraft.item.BlockItem(YoshiBlocks.REINFORCED_OBSIDIAN, new FabricItemSettings()));
    }
}
