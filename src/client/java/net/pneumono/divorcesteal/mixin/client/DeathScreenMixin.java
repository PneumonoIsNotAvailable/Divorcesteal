package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {
    protected DeathScreenMixin(Text title) {
        super(title);
    }

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screen/DeathScreen;title:Lnet/minecraft/text/Text;"
            )
    )
    private Text useDeathbanTitle(DeathScreen instance, Operation<Text> original) {
        if ((Object)this instanceof DeathbanScreen) {
            return Text.translatable("divorcesteal.gui.deathban.title");
        }

        return original.call(instance);
    }
}
