package online.inklingyoshi.yoshi.item;

import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;

public class YoshiMusicDiscItem extends MusicDiscItem {
    
    public YoshiMusicDiscItem(int comparatorOutput, SoundEvent sound, Settings settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
    }
    
    public static MusicDiscItem create(int comparatorOutput, SoundEvent sound, String name, int lengthInSeconds) {
        return new MusicDiscItem(
            comparatorOutput, 
            sound, 
            new Item.Settings().maxCount(1).rarity(Rarity.RARE),
            lengthInSeconds
        );
    }
}