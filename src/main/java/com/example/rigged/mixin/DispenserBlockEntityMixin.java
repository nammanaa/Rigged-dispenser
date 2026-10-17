package com.example.rigged.mixin;

import com.example.rigged.RiggedDispenser;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin {
    @Inject(method = "chooseNonEmptySlot", at = @At("RETURN"), cancellable = true)
    private void riggeddispenser$rig(Random random, CallbackInfoReturnable<Integer> cir) {
        int result = RiggedDispenser.adjust((DispenserBlockEntity) (Object) this, random, cir.getReturnValue());
        if (result >= 0) cir.setReturnValue(result);
    }
}
