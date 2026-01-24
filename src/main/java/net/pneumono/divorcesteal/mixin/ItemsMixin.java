package net.pneumono.divorcesteal.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Items.class)
public abstract class ItemsMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/Item$Properties;spear(Lnet/minecraft/world/item/ToolMaterial;FFFFFFFFF)Lnet/minecraft/world/item/Item$Properties;",
                    ordinal = 6
            )
    )
    private static Item.Properties netheriteSpear(Item.Properties instance, ToolMaterial toolMaterial, float f, float g, float h, float i, float j, float k, float l, float m, float n) {
        return instance.spear(toolMaterial, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F);
    }
}
