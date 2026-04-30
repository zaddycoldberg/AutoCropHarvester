package com.autoharvester.core;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class SnakeEngine {

    private static final SnakeEngine INSTANCE = new SnakeEngine();
    public static SnakeEngine get() { return INSTANCE; }

    private boolean active      = false;
    private boolean angleLocked = false;
    private float   lockedYaw   = 0f;
    private int     stripWidth  = 16;
    private boolean movingLeft  = false;
    private float   baseYaw     = 0f;
    private int     colIndex    = 0;
    private int     phaseTimer  = 0;
    private int     phase       = 0;
    // 0=MOVING 1=HARVESTING 2=TURNING_START 3=STEPPING_DOWN 4=TURNING_END

    private static final int TICKS_PER_BLOCK  = 5;
    private static final int HARVEST_TICKS    = 3;

    public boolean isActive()       { return active; }
    public boolean isAngleLocked()  { return angleLocked; }
    public float   getLockedYaw()   { return lockedYaw; }
    public int     getStripWidth()  { return stripWidth; }
    public boolean isMovingLeft()   { return movingLeft; }

    public void setStripWidth(int w)      { stripWidth = Math.max(2, Math.min(64, w)); }
    public void setAngleLocked(boolean v) { angleLocked = v; }
    public void setLockedYaw(float y)     { lockedYaw = y; }

    public void activate() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        active     = true;
        movingLeft = false;
        colIndex   = 0;
        phase      = 0;
        phaseTimer = TICKS_PER_BLOCK;
        baseYaw    = mc.thePlayer.rotationYaw;
        applyYaw(mc, baseYaw);
    }

    public void deactivate() {
        active = false;
        releaseAll(Minecraft.getMinecraft());
    }

    public void toggle() {
        if (active) deactivate(); else activate();
    }

    public void lockCurrentAngle() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            lockedYaw   = mc.thePlayer.rotationYaw;
            angleLocked = true;
        }
    }

    public void unlockAngle() { angleLocked = false; }

    public void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) { deactivate(); return; }

        if (angleLocked) applyYaw(mc, lockedYaw);

        releaseAll(mc);

        if      (phase == 0) tickMoving(mc);
        else if (phase == 1) tickHarvesting(mc);
        else if (phase == 2) tickTurningStart(mc);
        else if (phase == 3) tickSteppingDown(mc);
        else if (phase == 4) tickTurningEnd(mc);
    }

    private void tickMoving(Minecraft mc) {
        BlockPos ahead = blockAhead(mc);
        if (isMatureCrop(mc.theWorld, ahead)) {
            phase = 1; phaseTimer = HARVEST_TICKS;
            setKey(mc.gameSettings.keyBindAttack, true);
            return;
        }
        setKey(mc.gameSettings.keyBindForward, true);
        phaseTimer--;
        if (phaseTimer <= 0) {
            colIndex++;
            if (colIndex >= stripWidth) {
                colIndex = 0; phase = 2; phaseTimer = TICKS_PER_BLOCK;
            } else {
                phaseTimer = TICKS_PER_BLOCK;
            }
        }
    }

    private void tickHarvesting(Minecraft mc) {
        setKey(mc.gameSettings.keyBindAttack, true);
        phaseTimer--;
        if (phaseTimer <= 0) { phase = 0; phaseTimer = TICKS_PER_BLOCK; }
    }

    private void tickTurningStart(Minecraft mc) {
        float target = normalizeYaw(travelYaw() + (movingLeft ? -90f : 90f));
        smoothYaw(mc, target);
        phaseTimer--;
        if (phaseTimer <= 0) {
            applyYaw(mc, target);
            phase = 3; phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private void tickSteppingDown(Minecraft mc) {
        setKey(mc.gameSettings.keyBindForward, true);
        phaseTimer--;
        if (phaseTimer <= 0) {
            movingLeft = !movingLeft;
            phase = 4; phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private void tickTurningEnd(Minecraft mc) {
        float target = travelYaw();
        smoothYaw(mc, target);
        phaseTimer--;
        if (phaseTimer <= 0) {
            applyYaw(mc, target);
