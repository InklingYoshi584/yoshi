package online.inklingyoshi.yoshi.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import online.inklingyoshi.yoshi.Yoshi;

public class YoshiBlocks {
    
    public static Block GOLDEN_EGG;
    public static Block REINFORCED_OBSIDIAN;
    
    public static void registerBlocks() {
        GOLDEN_EGG = Registry.register(Registries.BLOCK, new Identifier(Yoshi.MOD_ID, "golden_egg"), 
            new GoldenEggBlock());
            
        REINFORCED_OBSIDIAN = Registry.register(Registries.BLOCK, new Identifier(Yoshi.MOD_ID, "reinforced_obsidian"), 
            new ReinforcedObsidianBlock(FabricBlockSettings.create()
                .strength(70.0f, 1800.0f).requiresTool()));
    }
}