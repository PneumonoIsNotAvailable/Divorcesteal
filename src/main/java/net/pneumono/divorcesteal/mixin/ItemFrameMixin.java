package net.pneumono.divorcesteal.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends HangingEntity {
    protected ItemFrameMixin(EntityType<? extends HangingEntity> type, Level level) {
        super(type, level);
    }

    @Shadow
    private boolean fixed;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract SoundEvent getRotateItemSound();

    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void customInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (level().isClientSide() || this.fixed) return;

        ItemStack itemStack = player.getItemInHand(hand);

        if (!this.getItem().isEmpty()) {
            if (itemStack.is(Items.GLASS_PANE) && !hasAttached(DivorcestealRegistry.ROTATION_LOCKED)) {
                setAttached(DivorcestealRegistry.ROTATION_LOCKED, Unit.INSTANCE);

                this.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                itemStack.consume(1, player);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;

            } else if (itemStack.is(Items.AMETHYST_SHARD) && !isInvisible()) {
                setInvisible(true);

                this.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                itemStack.consume(1, player);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
        }

        if (hasAttached(DivorcestealRegistry.ROTATION_LOCKED)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(
            method = "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0
            )
    )
    private void dropResources(ServerLevel level, Entity causedBy, boolean withFrame, CallbackInfo ci) {
        if (hasAttached(DivorcestealRegistry.ROTATION_LOCKED)) {
            spawnAtLocation(level, Items.GLASS_PANE);
            removeAttached(DivorcestealRegistry.ROTATION_LOCKED);
        }

        if (isInvisible()) {
            spawnAtLocation(level, Items.AMETHYST_SHARD);
            setInvisible(false);
        }
    }
}
