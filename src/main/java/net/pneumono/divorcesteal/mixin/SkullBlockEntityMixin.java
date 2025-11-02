package net.pneumono.divorcesteal.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private ItemLore lore;
    @Unique
    @Nullable
    private Component itemName; // Not to be confused with custom_name, they're different I swear!
    @Unique
    @Nullable
    private CustomData customData;
    @Unique
    @Nullable
    private KilledByComponent killer;

    @Inject(
            method = "saveAdditional",
            at = @At("RETURN")
    )
    private void saveExtra(ValueOutput view, CallbackInfo ci) {
        view.storeNullable("lore", ItemLore.CODEC, this.lore);
        view.storeNullable("item_name", ComponentSerialization.CODEC, this.itemName);
        view.storeNullable("custom_data", CustomData.CODEC, this.customData);
        view.storeNullable("killer", KilledByComponent.CODEC, this.killer);
    }

    @Inject(
            method = "loadAdditional",
            at = @At("RETURN")
    )
    private void loadExtra(ValueInput view, CallbackInfo ci) {
        this.lore = view.read("lore", ItemLore.CODEC).orElse(null);
        this.itemName = parseCustomNameSafe(view, "item_name");
        this.customData = view.read("custom_data", CustomData.CODEC).orElse(null);
        this.killer = view.read("killer", KilledByComponent.CODEC).orElse(null);
    }

    @Inject(
            method = "applyImplicitComponents",
            at = @At("RETURN")
    )
    private void applyExtraComponents(DataComponentGetter components, CallbackInfo ci) {
        this.lore = components.get(DataComponents.LORE);
        this.itemName = components.get(DataComponents.ITEM_NAME);
        this.customData = components.get(DataComponents.CUSTOM_DATA);
        this.killer = components.get(DivorcestealRegistry.KILLED_BY_COMPONENT);
    }

    @Inject(
            method = "collectImplicitComponents",
            at = @At("RETURN")
    )
    private void collectExtraComponents(DataComponentMap.Builder builder, CallbackInfo ci) {
        builder.set(DataComponents.LORE, this.lore);
        builder.set(DataComponents.ITEM_NAME, this.itemName);
        builder.set(DataComponents.CUSTOM_DATA, this.customData);
        builder.set(DivorcestealRegistry.KILLED_BY_COMPONENT, this.killer);
    }

    @Inject(
            method = "removeComponentsFromTag",
            at = @At("RETURN")
    )
    private void removeExtraComponentsFromTag(ValueOutput view, CallbackInfo ci) {
        view.discard("lore");
        view.discard("item_name");
        view.discard("custom_data");
    }
}
