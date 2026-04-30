package com.autoharvester.handler;

import com.autoharvester.core.SnakeEngine;
import com.autoharvester.gui.GuiHarvester;
import com.autoharvester.keybind.KeyBindings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (KeyBindings.KEY_TOGGLE.isPressed()) {
            SnakeEngine.get().toggle();
            String msg = SnakeEngine.get().isActive() ? "§aHarvester: ON" : "§cHarvester: OFF";
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }

        if (KeyBindings.KEY_GUI.isPressed()) {
            mc.displayGuiScreen(new GuiHarvester());
        }

        if (KeyBindings.KEY_LOCK_ANGLE.isPressed()) {
            SnakeEngine engine = SnakeEngine.get();
            if (engine.isAngleLocked()) {
                engine.unlockAngle();
                mc.thePlayer.addChatMessage(new ChatComponentText("§eAngle: UNLOCKED"));
            } else {
                engine.lockCurrentAngle();
                mc.thePlayer.addChatMessage(new ChatComponentText(
                    String.format("§bAngle locked: %.1f", engine.getLockedYaw())));
            }
        }
    }
}
