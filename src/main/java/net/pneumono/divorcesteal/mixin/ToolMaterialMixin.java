package net.pneumono.divorcesteal.mixin;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin {
    @SuppressWarnings("unused")
    @Mutable
    @Final
    @Shadow
    public static ToolMaterial NETHERITE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 3.0F, 15, ItemTags.NETHERITE_TOOL_MATERIALS);
}
