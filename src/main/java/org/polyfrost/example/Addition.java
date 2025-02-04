package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.example.command.StatsCommand;
import org.polyfrost.example.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.mixin.EntityLivingBaseAccessor;

@Mod(modid = Addition.MODID, name = Addition.NAME, version = Addition.VERSION)
public class Addition {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static ModConfig config;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
        CommandManager.INSTANCE.registerCommand(new StatsCommand());
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
