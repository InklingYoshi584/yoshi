package online.inklingyoshi.yoshi.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import online.inklingyoshi.yoshi.Yoshi;

public class YoshiItems {
    public static Item YOSHI_EGG;
    
    public static void registerItems() {
        YOSHI_EGG = Registry.register(Registries.ITEM, new Identifier(Yoshi.MOD_ID, "yoshi_egg"), 
            new YoshiEggItem(new FabricItemSettings().maxCount(16)));
    }
}
