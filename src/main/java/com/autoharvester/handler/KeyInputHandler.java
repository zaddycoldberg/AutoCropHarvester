package com.autoharvester.handler;

import com.autoharvester.core.SnakeEngine;
import com.autoharvester.gui.GuiHarvester;
import com.autoharvester.keybind.KeyBindings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (KeyBindings.KEY_TOGGLE.isPressed()) {
            SnakeEngine.get().toggle();
            String msg = SnakeEngine.get().isActive() ? "\u00a7aHarvester: ON" : "\u00a7cHarvester: OFF";
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }

        if (KeyBindings.KEY_GUI.isPressed()) {
            mc.displayGuiScreen(new GuiHarvester());
        }

        if (KeyBindings.KEY_LOCK_ANGLE.isPressed()) {
            SnakeEngine engine = SnakeEngine.get();
            if (engine.isAngleLocked()) {
                engine.unlockAngle();
                mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7eAngle: UNLOCKED"));
            } else {
                engine.lockCurrentAngle();
                mc.thePlayer.addChatMessage(new ChatComponentText(
                    String.format("\u00a7bAngle locked: %.1f", engine.getLockedYaw())));
            }
        }
    }
}
