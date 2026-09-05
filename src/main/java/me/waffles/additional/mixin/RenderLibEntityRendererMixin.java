package me.waffles.additional.mixin;

import me.waffles.additional.config.ModConfig;
import me.waffles.additional.render.NameTagESP;
import me.waffles.additional.util.BotUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records actual RenderLib occlusion results for the Legit Mode label-only pass.
 * The string target and @Pseudo keep RenderLib optional.
 */
@Pseudo
@Mixin(
        targets = "meldexun.renderlib.renderer.entity.EntityRenderer",
        remap = false,
        priority = 900
)
public class RenderLibEntityRendererMixin {

    @Inject(
            method = "isOcclusionCulled(Lnet/minecraft/entity/Entity;)Z",
            at = @At("RETURN"),
            remap = false,
            require = 0
    )
    private void additional$recordOccludedPlayers(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.isLegitModeActive()
                || !cir.getReturnValue()
                || !(entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        if (!BotUtils.isBot(player)) {
            NameTagESP.markOccludedPlayer(player);
        }
    }
}
