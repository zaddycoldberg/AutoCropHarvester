package com.autoharvester.gui;

import com.autoharvester.core.SnakeEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

public class GuiHarvester extends GuiScreen {

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 220;
    private static final int BTN_W   = 110;
    private static final int BTN_H   = 20;

    private GuiTextField fieldWidth;
    private GuiTextField fieldAngle;

    private static final int ID_TOGGLE     = 1;
    private static final int ID_SET_WIDTH  = 2;
    private static final int ID_LOCK_ANGLE = 3;
    private static final int ID_SET_ANGLE  = 4;
    private static final int ID_USE_LOOK   = 5;
    private static final int ID_CLOSE      = 6;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int cx = width / 2;
        int cy = height / 2;
        int pl = cx - PANEL_W / 2;
        int pt = cy - PANEL_H / 2;
        SnakeEngine eng = SnakeEngine.get();

        buttonList.add(new GuiButton(ID_TOGGLE, pl+10, pt+40, BTN_W*2+10, BTN_H,
            eng.isActive() ? "§cHarvester: ON (click OFF)" : "§aHarvester: OFF (click ON)"));

        fieldWidth = new GuiTextField(0, fontRendererObj, pl+10, pt+75, BTN_W-5, BTN_H);
        fieldWidth.setMaxStringLength(4);
        fieldWidth.setText(String.valueOf(eng.getStripWidth()));

        buttonList.add(new GuiButton(ID_SET_WIDTH, pl+10+BTN_W, pt+73, BTN_W, BTN_H, "Set Width"));

        fieldAngle = new GuiTextField(1, fontRendererObj, pl+10, pt+110, BTN_W-5, BTN_H);
        fieldAngle.setMaxStringLength(7);
        fieldAngle.setText(String.format("%.1f", eng.getLockedYaw()));

        buttonList.add(new GuiButton(ID_SET_ANGLE, pl+10+BTN_W, pt+108, BTN_W/2-2, BTN_H, "Set"));
        buttonList.add(new GuiButton(ID_USE_LOOK, pl+10+BTN_W+BTN_W/2+2, pt+108, BTN_W/2-2, BTN_H, "Look"));
        buttonList.add(new GuiButton(ID_LOCK_ANGLE, pl+10, pt+140, BTN_W*2+10, BTN_H,
            eng.isAngleLocked()
                ? String.format("§bLocked: %.1f° (click unlock)", eng.getLockedYaw())
                : "§7Unlocked (click to lock)"));
        buttonList.add(new GuiButton(ID_CLOSE, pl+10, pt+PANEL_H-35, BTN_W*2+10, BTN_H, "§eClose"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        SnakeEngine eng = SnakeEngine.get();
        Minecraft mc = Minecraft.getMinecraft();
        switch (button.id) {
            case ID_TOGGLE:
                eng.toggle();
                buttonList.clear(); initGui(); break;
            case ID_SET_WIDTH:
                try {
                    eng.setStripWidth(Integer.parseInt(fieldWidth.getText().trim()));
                    mc.thePlayer.addChatMessage(new ChatComponentText("§aWidth: " + eng.getStripWidth()));
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§cInvalid number!"));
                } break;
            case ID_SET_ANGLE:
                try {
                    float a = Float.parseFloat(fieldAngle.getText().trim());
                    eng.setLockedYaw(a); eng.setAngleLocked(true);
                    buttonList.clear(); initGui();
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§cInvalid angle!"));
                } break;
            case ID_USE_LOOK:
                eng.lockCurrentAngle();
                fieldAngle.setText(String.format("%.1f", eng.getLockedYaw()));
                buttonList.clear(); initGui(); break;
            case ID_LOCK_ANGLE:
                if (eng.isAngleLocked()) eng.unlockAngle();
                else eng.lockCurrentAngle();
                buttonList.clear(); initGui(); break;
            case ID_CLOSE:
                mc.displayGuiScreen(null); break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int cx = width/2, cy = height/2;
        int pl = cx-PANEL_W/2, pt = cy-PANEL_H/2;
        drawRect(pl, pt, pl+PANEL_W, pt+PANEL_H, 0xCC222233);
        drawCenteredString(fontRendererObj, "§l§bAuto Crop Harvester", cx, pt+10, 0xFFFFFF);
        drawString(fontRendererObj, "§eStrip Width:", pl+10, pt+63, 0xFFFF88);
        drawString(fontRendererObj, "§eAngle (degrees):", pl+10, pt+98, 0xFFFF88);
        SnakeEngine eng = SnakeEngine.get();
        String status = eng.isActive()
            ? "§aRUNNING | " + (eng.isMovingLeft() ? "◄ LEFT" : "► RIGHT")
            : "§cSTOPPED";
        drawCenteredString(fontRendererObj, status, cx, pt+PANEL_H-50, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "§7[H] Toggle  [G] GUI  [L] Lock", cx, pt+PANEL_H-10, 0x888888);
        fieldWidth.drawTextBox();
        fieldAngle.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { Minecraft.getMinecraft().displayGuiScreen(null); return; }
        fieldWidth.textboxKeyTyped(c, key);
        fieldAngle.textboxKeyTyped(c, key);
        super.keyTyped(c, key);
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        fieldWidth.mouseClicked(x, y, btn);
        fieldAngle.mouseClicked(x, y, btn);
        super.mouseClicked(x, y, btn);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
}
