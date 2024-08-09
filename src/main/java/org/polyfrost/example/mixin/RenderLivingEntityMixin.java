package org.polyfrost.example.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.polyfrost.example.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RendererLivingEntity.class)
public class RenderLivingEntityMixin {
    @Redirect(
            method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;isSneaking()Z"
            )
    )
    private boolean cancel(EntityLivingBase instance) {
        if(instance.isSneaking()){
            GlStateManager.translate(0.0F, 9.374999F, 0.0F);
        }
        return !ModConfig.nametagsOnShift && instance.isSneaking();
    }
}
