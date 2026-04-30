package com.autoharvester.core;

import com.autoharvester.handler.KeyInputHandler;
import com.autoharvester.handler.TickHandler;
import com.autoharvester.keybind.KeyBindings;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = AutoHarvesterMod.MODID,
    name = AutoHarvesterMod.NAME,
    version = AutoHarvesterMod.VERSION,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class AutoHarvesterMod {

    public static final String MODID = "autoharvester";
    public static final String NAME = "Auto Crop Harvester";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static AutoHarvesterMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("AutoHarvester PreInit...");
        KeyBindings.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("AutoHarvester Init...");
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
}
