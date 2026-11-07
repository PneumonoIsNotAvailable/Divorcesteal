package net.pneumono.divorcesteal.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Function;

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
    private static Item.Properties netheriteSpear(
            Item.Properties instance, ToolMaterial material,
            float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold,
            float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold
    ) {
        return instance.spear(material, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F);
    }

    @Inject(
            method = "registerItem(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;",
            at = @At("HEAD")
    )
    private static void modifyItemProperties(ResourceKey<Item> id, Function<Item.Properties, Item> itemFactory, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        Consumer<Item.Properties> modifier = DivorcestealRegistry.ITEM_PROPERTY_MODIFIERS.get(id);
        if (modifier != null) {
            modifier.accept(properties);
        }
    }
}
