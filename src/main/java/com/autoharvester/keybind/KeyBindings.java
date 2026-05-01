package com.autoharvester.keybind;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static KeyBinding KEY_TOGGLE;
    public static KeyBinding KEY_GUI;
    public static KeyBinding KEY_LOCK_ANGLE;

    public static void register() {
        KEY_TOGGLE = new KeyBinding(
            "key.autoharvester.toggle",
            Keyboard.KEY_H,
            "key.category.autoharvester"
        );
        KEY_GUI = new KeyBinding(
            "key.autoharvester.gui",
            Keyboard.KEY_G,
            "key.category.autoharvester"
        );
        KEY_LOCK_ANGLE = new KeyBinding(
            "key.autoharvester.lockangle",
            Keyboard.KEY_L,
            "key.category.autoharvester"
        );

        ClientRegistry.registerKeyBinding(KEY_TOGGLE);
        ClientRegistry.registerKeyBinding(KEY_GUI);
        ClientRegistry.registerKeyBinding(KEY_LOCK_ANGLE);
    }
}
