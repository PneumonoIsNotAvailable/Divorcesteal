package net.pneumono.divorcesteal.mixin;

import net.minecraft.item.equipment.ArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ArmorMaterials.class)
public interface ArmorMaterialsMixin {
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(floatValue = 3.0F)
    )
    private static float modifyArmorToughness(float constant) {
        return 2.0F;
    }

    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(floatValue = 0.1F)
    )
    private static float modifyKnockbackResistance(float constant) {
        return 0.0F;
    }
}
