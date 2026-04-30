package com.autoharvester.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class SnakeEngine {

    private static final SnakeEngine INSTANCE = new SnakeEngine();
    public static SnakeEngine get() { return INSTANCE; }

    private boolean active = false;
    private boolean angleLocked = false;
    private float lockedYaw = 0f;
    private int stripWidth = 16;
    private boolean movingLeft = false;
    private float baseYaw = 0f;
    private int colIndex = 0;
    private int phaseTimer = 0;
    private int phase = 0;

    private static final int TICKS_PER_BLOCK = 5;
    private static final int HARVEST_TICKS = 3;

    public boolean isActive() { return active; }
    public boolean isAngleLocked() { return angleLocked; }
    public float getLockedYaw() { return lockedYaw; }
    public int getStripWidth() { return stripWidth; }
    public boolean isMovingLeft() { return movingLeft; }

    public void setStripWidth(int w) {
        stripWidth = Math.max(2, Math.min(64, w));
    }

    public void setAngleLocked(boolean v) {
        angleLocked = v;
    }

    public void setLockedYaw(float y) {
        lockedYaw = y;
    }

    public void activate() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        active = true;
        movingLeft = false;
        colIndex = 0;
        phase = 0;
        phaseTimer = TICKS_PER_BLOCK;
        baseYaw = mc.thePlayer.rotationYaw;
        applyYaw(mc, baseYaw);
    }

    public void deactivate() {
        active = false;
        releaseAll(Minecraft.getMinecraft());
    }

    public void toggle() {
        if (active) {
            deactivate();
        } else {
            activate();
        }
    }

    public void lockCurrentAngle() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            lockedYaw = mc.thePlayer.rotationYaw;
            angleLocked = true;
        }
    }

    public void unlockAngle() {
        angleLocked = false;
    }

    public void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            deactivate();
            return;
        }
        if (angleLocked) {
            applyYaw(mc, lockedYaw);
        }
        releaseAll(mc);
        if (phase == 0) {
            tickMoving(mc);
        } else if (phase == 1) {
            tickHarvesting(mc);
        } else if (phase == 2) {
            tickTurningStart(mc);
        } else if (phase == 3) {
            tickSteppingDown(mc);
        } else if (phase == 4) {
            tickTurningEnd(mc);
        }
    }

    private void tickMoving(Minecraft mc) {
        BlockPos ahead = blockAhead(mc);
        if (isMatureCrop(mc.theWorld, ahead)) {
            phase = 1;
            phaseTimer = HARVEST_TICKS;
            setKey(mc.gameSettings.keyBindAttack, true);
            return;
        }
        setKey(mc.gameSettings.keyBindForward, true);
        phaseTimer--;
        if (phaseTimer <= 0) {
            colIndex++;
            if (colIndex >= stripWidth) {
                colIndex = 0;
                phase = 2;
                phaseTimer = TICKS_PER_BLOCK;
            } else {
                phaseTimer = TICKS_PER_BLOCK;
            }
        }
    }

    private void tickHarvesting(Minecraft mc) {
        setKey(mc.gameSettings.keyBindAttack, true);
        phaseTimer--;
        if (phaseTimer <= 0) {
            phase = 0;
            phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private void tickTurningStart(Minecraft mc) {
        float target = normalizeYaw(travelYaw() + (movingLeft ? -90f : 90f));
        smoothYaw(mc, target);
        phaseTimer--;
        if (phaseTimer <= 0) {
            applyYaw(mc, target);
            phase = 3;
            phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private void tickSteppingDown(Minecraft mc) {
        setKey(mc.gameSettings.keyBindForward, true);
        phaseTimer--;
        if (phaseTimer <= 0) {
            movingLeft = !movingLeft;
            phase = 4;
            phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private void tickTurningEnd(Minecraft mc) {
        float target = travelYaw();
        smoothYaw(mc, target);
        phaseTimer--;
        if (phaseTimer <= 0) {
            applyYaw(mc, target);
            if (angleLocked) {
                lockedYaw = target;
            }
            phase = 0;
            phaseTimer = TICKS_PER_BLOCK;
        }
    }

    private float travelYaw() {
        return normalizeYaw(movingLeft ? baseYaw + 180f : baseYaw);
    }

    private BlockPos blockAhead(Minecraft mc) {
        EnumFacing f = mc.thePlayer.getHorizontalFacing();
        int x = (int) Math.floor(mc.thePlayer.posX) + f.getFrontOffsetX();
        int y = (int) Math.floor(mc.thePlayer.posY);
        int z = (int) Math.floor(mc.thePlayer.posZ) + f.getFrontOffsetZ();
        return new BlockPos(x, y, z);
    }

    private boolean isMatureCrop(World w, BlockPos pos) {
        Block b = w.getBlockState(pos).getBlock();
        if (b instanceof BlockCrops) {
            return (int) w.getBlockState(pos).getValue(BlockCrops.AGE) >= 7;
        }
        if (b == Blocks.melon_block || b == Blocks.pumpkin) {
            return true;
        }
        if (b instanceof BlockReed) {
            return true;
        }
        if (b == Blocks.nether_wart) {
            return (int) w.getBlockState(pos).getValue(BlockNetherWart.AGE) >= 3;
        }
        return false;
    }

    private void applyYaw(Minecraft mc, float yaw) {
        mc.thePlayer.rotationYaw = normalizeYaw(yaw);
        mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
        baseYaw = mc.thePlayer.rotationYaw;
    }

    private void smoothYaw(Minecraft mc, float target) {
        float cur = mc.thePlayer.rotationYaw;
        float diff = normalizeYaw(target - cur);
        mc.thePlayer.rotationYaw = normalizeYaw(cur + diff * 0.5f);
    }

    private float normalizeYaw(float y) {
        while (y > 180f) { y -= 360f; }
        while (y < -180f) { y += 360f; }
        return y;
    }

    private void releaseAll(Minecraft mc) {
        setKey(mc.gameSettings.keyBindForward, false);
        setKey(mc.gameSettings.keyBindLeft, false);
        setKey(mc.gameSettings.keyBindRight, false);
        setKey(mc.gameSettings.keyBindAttack, false);
    }

    private void setKey(KeyBinding kb, boolean pressed) {
        KeyBinding.setKeyBindState(kb.getKeyCode(), pressed);
    }
}
