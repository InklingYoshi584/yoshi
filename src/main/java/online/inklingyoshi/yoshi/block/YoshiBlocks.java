package online.inklingyoshi.yoshi.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import online.inklingyoshi.yoshi.Yoshi;

public class YoshiBlocks {
    
    public static Block GOLDEN_EGG;
    
    public static void registerBlocks() {
        GOLDEN_EGG = Registry.register(Registries.BLOCK, new Identifier(Yoshi.MOD_ID, "golden_egg"), 
            new GoldenEggBlock());
    }
}