package net.pneumono.divorcesteal.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.pneumono.divorcesteal.content.component.KilledByComponent;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public abstract class SkullBlockEntityMixin extends BlockEntity {
    public SkullBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    @Nullable
    private LoreComponent lore;
    @Unique
    @Nullable
    private Text itemName; // Not to be confused with custom_name, they're different I swear!
    @Unique
    @Nullable
    private NbtComponent customData;
    @Unique
    @Nullable
    private KilledByComponent killer;

    @Inject(
            method = "writeData",
            at = @At("RETURN")
    )
    private void writeExtraData(WriteView view, CallbackInfo ci) {
        view.putNullable("lore", LoreComponent.CODEC, this.lore);
        view.putNullable("item_name", TextCodecs.CODEC, this.itemName);
        view.putNullable("custom_data", NbtComponent.CODEC, this.customData);
        view.putNullable("killer", KilledByComponent.CODEC, this.killer);
    }

    @Inject(
            method = "readData",
            at = @At("RETURN")
    )
    private void readExtraData(ReadView view, CallbackInfo ci) {
        this.lore = view.read("lore", LoreComponent.CODEC).orElse(null);
        this.itemName = tryParseCustomName(view, "item_name");
        this.customData = view.read("custom_data", NbtComponent.CODEC).orElse(null);
        this.killer = view.read("killer", KilledByComponent.CODEC).orElse(null);
    }

    @Inject(
            method = "readComponents",
            at = @At("RETURN")
    )
    private void readExtraComponents(ComponentsAccess components, CallbackInfo ci) {
        this.lore = components.get(DataComponentTypes.LORE);
        this.itemName = components.get(DataComponentTypes.ITEM_NAME);
        this.customData = components.get(DataComponentTypes.CUSTOM_DATA);
        this.killer = components.get(DivorcestealRegistry.KILLER_COMPONENT);
    }

    @Inject(
            method = "addComponents",
            at = @At("RETURN")
    )
    private void addExtraComponents(ComponentMap.Builder builder, CallbackInfo ci) {
        builder.add(DataComponentTypes.LORE, this.lore);
        builder.add(DataComponentTypes.ITEM_NAME, this.itemName);
        builder.add(DataComponentTypes.CUSTOM_DATA, this.customData);
        builder.add(DivorcestealRegistry.KILLER_COMPONENT, this.killer);
    }

    @Inject(
            method = "removeFromCopiedStackData",
            at = @At("RETURN")
    )
    private void removeExtraFromCopiedStackData(WriteView view, CallbackInfo ci) {
        view.remove("lore");
        view.remove("item_name");
        view.remove("custom_data");
    }
}
