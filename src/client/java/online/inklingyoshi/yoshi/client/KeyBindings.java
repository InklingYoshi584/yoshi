package online.inklingyoshi.yoshi.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding REEL_IN;
    public static KeyBinding CREATE_EGG;
    
    public static void registerKeyBindings() {
        REEL_IN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.yoshi.reel_in",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.yoshi.abilities"
        ));
        
        CREATE_EGG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.yoshi.create_egg",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.yoshi.abilities"
        ));
    }
}
