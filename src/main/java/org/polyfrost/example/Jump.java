package org.polyfrost.example;

<<<<<<< Updated upstream:src/main/java/org/polyfrost/example/Jump.java
=======
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderLivingEvent;
>>>>>>> Stashed changes:src/main/java/org/polyfrost/example/MultiCheat.java
import net.minecraftforge.common.MinecraftForge;
import org.polyfrost.example.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.mixin.EntityLivingBaseAccessor;

@Mod(modid = Jump.MODID, name = Jump.NAME, version = Jump.VERSION)
public class Jump {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static ModConfig config;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
    }

<<<<<<< Updated upstream:src/main/java/org/polyfrost/example/Jump.java
=======
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().thePlayer != null && ModConfig.ndj && e.phase.equals(TickEvent.Phase.START)) {
            if(((EntityLivingBaseAccessor) Minecraft.getMinecraft().thePlayer).getJumpTicks() > 3){
                ((EntityLivingBaseAccessor) Minecraft.getMinecraft().thePlayer).setJumpTicks(3);
            }
//            ((EntityLivingBaseAccessorMixin) Minecraft.getMinecraft().thePlayer).setJumpTicks(0);
        }
    }
>>>>>>> Stashed changes:src/main/java/org/polyfrost/example/MultiCheat.java
}
