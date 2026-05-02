package com.autoharvester.core;

import com.autoharvester.handler.KeyInputHandler;
import com.autoharvester.handler.TickHandler;
import com.autoharvester.keybind.KeyBindings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "autoharvester", name = "Auto Crop Harvester", version = "1.0.0", acceptedMinecraftVersions = "[1.8.9]")
public class AutoHarvesterMod {

    @Mod.Instance("autoharvester")
    public static AutoHarvesterMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeyBindings.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
}
