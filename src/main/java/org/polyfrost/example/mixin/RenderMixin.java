package org.polyfrost.example.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.polyfrost.example.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = Render.class)
public class RenderMixin {

    @Redirect(
            method = "renderLivingLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;disableDepth()V"
            )
    )
    private void disableDepth(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
        if(ModConfig.nametagsThroughWalls && !isBot(entityIn)){
            GlStateManager.depthFunc(519);
            return;
        }
        GlStateManager.disableDepth();
    }

    @Redirect(
            method = "renderLivingLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;enableDepth()V"
            )
    )
    private void enableDepth(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
        if(ModConfig.nametagsThroughWalls && !isBot(entityIn)){
            return;
        }
        GlStateManager.enableDepth();
    }

    @Inject(
            method = "renderLivingLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V"
            )
    )
    private void disable(Entity entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci){
        if(ModConfig.nametagsThroughWalls && !isBot(entityIn)){
            GlStateManager.disableDepth();
        }
    }

    @Inject(
            method = "renderLivingLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V"
            )
    )
    private void enable(Entity entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci){
        if(ModConfig.nametagsThroughWalls && !isBot(entityIn)){
            GlStateManager.enableDepth();
        }
    }

    @Redirect(
            method = "renderLivingLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getDistanceSqToEntity(Lnet/minecraft/entity/Entity;)D"
            )
    )
    private double extendNametagRange(Entity entityIn, Entity instance) {
        if(ModConfig.extendNametagRange && !isBot(entityIn)) {
            return 0.0D;
        }
        return entityIn.getDistanceSqToEntity(instance);
    }

    public boolean isBot(Entity entity){
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).getDisplayNameString().contains("§c") || ((EntityPlayer) entity).getDisplayNameString().contains("[NPC]") || ((EntityPlayer) entity).getDisplayNameString().contains("[BOT]") || ((EntityPlayer) entity).getDisplayNameString().contains("iAT3") || ((EntityPlayer) entity).getDisplayNameString().isEmpty() || (entity.getUniqueID().version() == 2) || (((EntityPlayer) entity).getDisplayNameString().contains("§") && (((EntityPlayer) entity).getDisplayNameString().contains("SHOP") || ((EntityPlayer) entity).getDisplayNameString().contains("UPGRADE"))))) {
            return true;
        } else {
            for (String name : getAllPlayerNamesFromTabList()) {
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getDisplayNameString().contains(name)) {
                    return false;
                }
            }
            return true;
        }
    }

    public ArrayList<String> getAllPlayerNamesFromTabList() {
        ArrayList<String> playerNames = new ArrayList<>();
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();

        if (netHandler != null) {
            for (NetworkPlayerInfo info : netHandler.getPlayerInfoMap()) {
                playerNames.add(info.getGameProfile().getName());
            }
        }

        return playerNames;
    }

}
