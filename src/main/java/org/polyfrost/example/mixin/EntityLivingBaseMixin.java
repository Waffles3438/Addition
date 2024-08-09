package org.polyfrost.example.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.polyfrost.example.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(intValue = 10))
    private int jumpTick(int constant) {
        return ModConfig.ndj ? 3 : constant;
    }

}
