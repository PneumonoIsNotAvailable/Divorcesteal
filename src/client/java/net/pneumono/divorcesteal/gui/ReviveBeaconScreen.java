package net.pneumono.divorcesteal.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;

public class ReviveBeaconScreen extends HandledScreen<ReviveBeaconScreenHandler> {
    private static final Identifier HEART_SLOT_TEXTURE = Divorcesteal.id("heart");
    private static final Identifier HEAD_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/helmet");
    private static final Text HEART_SLOT_TOOLTIP = Text.translatable("divorcesteal.gui.revive_beacon.add_heart");
    private static final Text HEAD_SLOT_TOOLTIP = Text.translatable("divorcesteal.gui.revive_beacon.add_head");
    private static final Identifier TEXTURE = Divorcesteal.id("textures/gui/revive_beacon.png");

    public ReviveBeaconScreen(ReviveBeaconScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 238;
        this.backgroundHeight = 179;
        this.playerInventoryTitleX = 39;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.x, this.y,
                0.0F, 0.0F,
                this.backgroundWidth, this.backgroundHeight,
                256, 256
        );

        Slot topHeartSlot = this.handler.getTopHeartSlot();
        Slot leftHeartSlot = this.handler.getLeftHeartSlot();
        Slot rightHeartSlot = this.handler.getRightHeartSlot();
        Slot headSlot = this.handler.getHeadSlot();
        drawEmptySlot(context, mouseX, mouseY, topHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, leftHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, rightHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, headSlot, HEAD_SLOT_TEXTURE, HEAD_SLOT_TOOLTIP);

        Text wantedText = Text.translatable("divorcesteal.gui.revive_beacon.wanted");
        context.drawText(this.textRenderer,
                wantedText,
                this.x + 43 - (textRenderer.getWidth(wantedText) / 2), this.y + 28,
                Colors.DARK_GRAY, false
        );

        ProfileComponent target = this.handler.getTarget();
        if (target != null) {
            ItemStack targetHeadStack = new ItemStack(Items.PLAYER_HEAD);
            targetHeadStack.set(DataComponentTypes.PROFILE, target);
            context.drawItem(targetHeadStack, this.x + 35, this.y + 41);
            Text targetTooltipText = target.name().map(Text::literal).orElseGet(() -> Text.translatable("divorcesteal.unknown"));
            if (this.isPointWithinBounds(35, 41, 16, 16, mouseX, mouseY)) {
                context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(targetTooltipText, 115), mouseX, mouseY);
            }
        }
    }

    private void drawEmptySlot(DrawContext context, int mouseX, int mouseY, Slot slot, Identifier texture, Text text) {
        if (!slot.hasStack()) {
            context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED, texture,
                    this.x + slot.x, this.y + slot.y,
                    16, 16
            );

            if (this.isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
            }
        }
    }
}
