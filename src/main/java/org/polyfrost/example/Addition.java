package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.example.command.BedwarsStatsCommand;
import org.polyfrost.example.command.DuelsStatsCommand;
import org.polyfrost.example.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.mixin.EntityLivingBaseAccessor;
import org.polyfrost.example.util.Bedwars;
import org.polyfrost.example.util.Duels;
import org.polyfrost.example.util.EldestRemovalMap;
import org.polyfrost.example.util.Ranks;

@Mod(modid = Addition.MODID, name = Addition.NAME, version = Addition.VERSION)
public class Addition {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static ModConfig config;
    public static int maxSize = 16;
    public static EldestRemovalMap<String, Duels> duelsStatsList = new EldestRemovalMap<>(maxSize);
    public static EldestRemovalMap<String, Bedwars> bedwarsStatsList = new EldestRemovalMap<>(maxSize);
    public static EldestRemovalMap<String, Ranks> playerRanks = new EldestRemovalMap<>(maxSize);

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
        CommandManager.INSTANCE.registerCommand(new BedwarsStatsCommand());
        CommandManager.INSTANCE.registerCommand(new DuelsStatsCommand());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().thePlayer != null && ModConfig.ndj && e.phase.equals(TickEvent.Phase.START)) {
            if(((EntityLivingBaseAccessor) Minecraft.getMinecraft().thePlayer).getJumpTicks() > ModConfig.jumpTicks){
                ((EntityLivingBaseAccessor) Minecraft.getMinecraft().thePlayer).setJumpTicks(ModConfig.jumpTicks);
            }
        }
    }
}
