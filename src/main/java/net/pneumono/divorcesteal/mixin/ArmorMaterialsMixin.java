package net.pneumono.divorcesteal.mixin;

import net.minecraft.item.equipment.ArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ArmorMaterials.class)
public interface ArmorMaterialsMixin {
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(intValue = 37)
    )
    private static int modifyDurability(int constant) {
        return 33;
    }

    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(intValue = 15, ordinal = 3)
    )
    private static int modifyEnchantability(int constant) {
        return 10;
    }

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
