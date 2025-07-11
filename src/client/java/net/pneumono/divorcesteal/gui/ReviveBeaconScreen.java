package net.pneumono.divorcesteal.gui;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;

import java.util.Objects;
import java.util.Optional;

public class ReviveBeaconScreen extends HandledScreen<ReviveBeaconScreenHandler> {
    private static final Identifier HEART_SLOT_TEXTURE = Divorcesteal.id("heart");
    private static final Identifier HEAD_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/helmet");
    private static final Text HEART_SLOT_TOOLTIP = Text.translatable("divorcesteal.gui.revive_beacon.add_heart");
    private static final Text HEAD_SLOT_TOOLTIP = Text.translatable("divorcesteal.gui.revive_beacon.add_head");
    private static final Identifier PLAYER_TEXTURE = Divorcesteal.id("player");
    private static final Identifier PLAYER_HIGHLIGHTED_TEXTURE = Divorcesteal.id("player_highlighted");
    private static final Identifier PLAYER_SELECTED_TEXTURE = Divorcesteal.id("player_selected");
    private static final Identifier SCROLLER_TEXTURE = Divorcesteal.id("scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Divorcesteal.id("scroller_disabled");
    private static final Identifier REVIVE_BUTTON_TEXTURE = Divorcesteal.id("revive_button");
    private static final Identifier REVIVE_BUTTON_HIGHLIGHTED_TEXTURE = Divorcesteal.id("revive_button_highlighted");
    private static final Identifier TEXTURE = Divorcesteal.id("textures/gui/revive_beacon.png");

    private float scrollPosition = 0.0F;
    private int visibleTopRow = 0;
    private boolean scrollbarClicked = false;

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
        // GUI frame
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.x, this.y,
                0.0F, 0.0F,
                this.backgroundWidth, this.backgroundHeight,
                256, 256
        );

        // Item input
        Slot topHeartSlot = this.handler.getTopHeartSlot();
        Slot leftHeartSlot = this.handler.getLeftHeartSlot();
        Slot rightHeartSlot = this.handler.getRightHeartSlot();
        Slot headSlot = this.handler.getHeadSlot();
        drawEmptySlot(context, mouseX, mouseY, topHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, leftHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, rightHeartSlot, HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        drawEmptySlot(context, mouseX, mouseY, headSlot, HEAD_SLOT_TEXTURE, HEAD_SLOT_TOOLTIP);

        // Wanted poster
        Text wantedText = Text.translatable("divorcesteal.gui.revive_beacon.wanted");
        context.drawText(this.textRenderer,
                wantedText,
                this.x + 43 - (textRenderer.getWidth(wantedText) / 2), this.y + 28,
                Colors.DARK_GRAY, false
        );

        ProfileComponent target = resolved(this.handler.getTarget());
        if (target != null) {
            ItemStack targetHeadStack = new ItemStack(Items.PLAYER_HEAD);
            targetHeadStack.set(DataComponentTypes.PROFILE, target);
            context.drawItem(targetHeadStack, this.x + 35, this.y + 41);
            if (this.isPointWithinBounds(35, 41, 16, 16, mouseX, mouseY)) {
                Text targetTooltipText = target.name().map(Text::literal).orElseGet(
                        () -> Text.translatable("divorcesteal.unknown")
                );
                context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(targetTooltipText, 115), mouseX, mouseY);
            }
        }

        // Player select
        Identifier scrollTexture = canScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        int scrollOffset = (int)(39.0F * this.scrollPosition);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, scrollTexture, this.x + 218, this.y + 22 + scrollOffset, 12, 15);

        for (int menuY = 0; menuY < 3; ++menuY) { for (int menuX = 0; menuX < 3; ++menuX) {
            int playerIndex = (menuY + this.visibleTopRow) * 3 + menuX;
            ProfileComponent profile = resolved(this.handler.getRevivablePlayer(playerIndex));
            if (profile == null) break;

            drawPlayerSelect(
                    context,
                    mouseX, mouseY, menuX, menuY,
                    profile,
                    playerIndex == this.handler.getSelectedPlayer()
            );
        }}

        // Revive button
        if (this.handler.canRevive()) {
            int buttonX = this.x + 87;
            int buttonY = this.y + 75;

            boolean highlighted = isPointStrictlyWithinBounds(87, 75, 64, 11, mouseX, mouseY);
            Identifier buttonTexture = highlighted ? REVIVE_BUTTON_HIGHLIGHTED_TEXTURE : REVIVE_BUTTON_TEXTURE;

            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, buttonTexture, buttonX, buttonY, 64, 11);

            Text reviveText = Text.translatable("divorcesteal.gui.revive_beacon.revive");
            context.drawText(this.textRenderer,
                    reviveText,
                    buttonX + 32 - (textRenderer.getWidth(reviveText) / 2), buttonY + 2,
                    highlighted ? -128 : -9937334, false
            );
        }
    }

    private void drawEmptySlot(DrawContext context, int mouseX, int mouseY, Slot slot, Identifier texture, Text text) {
        if (slot.hasStack()) return;

        context.drawGuiTexture(
                RenderPipelines.GUI_TEXTURED, texture,
                this.x + slot.x, this.y + slot.y,
                16, 16
        );

        if (this.isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
        }
    }

    private void drawPlayerSelect(
            DrawContext context,
            int mouseX, int mouseY,
            int menuX, int menuY,
            ProfileComponent profile,
            boolean selected
    ) {
        int finalX = this.x + 161 + (menuX * 18);
        int finalY = this.y + 22 + (menuY * 18);

        boolean highlighted = this.isPointStrictlyWithinBounds(finalX - this.x, finalY - this.y, 18, 18, mouseX, mouseY);

        Identifier texture;
        if (selected) {
            texture = PLAYER_SELECTED_TEXTURE;
        } else if (highlighted) {
            texture = PLAYER_HIGHLIGHTED_TEXTURE;
        } else {
            texture = PLAYER_TEXTURE;
        }

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, finalX, finalY, 18, 18);

        ItemStack targetHeadStack = new ItemStack(Items.PLAYER_HEAD);
        targetHeadStack.set(DataComponentTypes.PROFILE, profile);
        context.drawItem(targetHeadStack, finalX + 1, finalY + 1);

        if (highlighted) {
            Text text = profile.name().map(Text::literal).orElseGet(
                    () -> Text.translatable("divorcesteal.unknown")
            );
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handlePlayerSelectMouseClick(mouseX, mouseY)) return true;
        if (handleReviveButtonMouseClick(mouseX, mouseY)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handlePlayerSelectMouseClick(double mouseX, double mouseY) {
        Objects.requireNonNull(this.client);
        this.scrollbarClicked = false;

        int finalX = this.x + 161;
        int finalY = this.y + 22;
        for (int menuY = 0; menuY < 3; menuY++) { for (int menuX = 0; menuX < 3; menuX++) {

            double mouseXOffset = mouseX - (finalX + menuX * 18);
            double mouseYOffset = mouseY - (finalY + menuY * 18);
            int playerIndex = (menuY + this.visibleTopRow) * 3 + menuX;

            if (
                    mouseXOffset > 0.0 &&
                            mouseYOffset > 0.0 &&
                            mouseXOffset < 18.0 &&
                            mouseYOffset < 18.0 &&
                            this.handler.onButtonClick(this.client.player, playerIndex)
            ) {
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
                );
                Objects.requireNonNull(this.client.interactionManager).clickButton(this.handler.syncId, playerIndex);
                return true;
            }
        }}

        if (canScroll()) {
            finalX = this.x + 218;
            finalY = this.y + 22;
            if (mouseX >= finalX && mouseX < finalX + 12 && mouseY >= finalY && mouseY < finalY + 54) {
                this.scrollbarClicked = true;
                return true;
            }
        }

        return false;
    }

    private boolean handleReviveButtonMouseClick(double mouseX, double mouseY) {
        Objects.requireNonNull(this.client);
        if (!this.handler.canRevive()) return false;

        int finalX = this.x + 88;
        int finalY = this.y + 76;
        if (!(mouseX >= finalX && mouseX < finalX + 64 && mouseY >= finalY && mouseY < finalY + 11)) return false;

        if (this.handler.onButtonClick(this.client.player, -2)) {
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
            );
            Objects.requireNonNull(this.client.interactionManager).clickButton(this.handler.syncId, -2);
            this.client.setScreen(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!canScroll() || !this.scrollbarClicked) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        int rows = this.getRows() - 3;
        int topY = this.y + 22;
        int bottomY = topY + 54;
        this.scrollPosition = ((float)mouseY - topY - 7.5F) / (bottomY - topY - 15.0F);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
        this.visibleTopRow = Math.max((int)(this.scrollPosition * rows + 0.5), 0);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;

        if (canScroll()) {
            int rows = this.getRows() - 3;
            float f = (float)verticalAmount / rows;
            this.scrollPosition = MathHelper.clamp(this.scrollPosition - f, 0.0F, 1.0F);
            this.visibleTopRow = Math.max((int)(this.scrollPosition * rows + 0.5F), 0);
        }

        return true;
    }

    private boolean isPointStrictlyWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return this.isPointWithinBounds(x + 1, y + 1, width - 2, height - 2, pointX, pointY);
    }

    private boolean canScroll() {
        return getRows() > 3;
    }

    private int getRows() {
        return MathHelper.ceilDiv(this.handler.revivablePlayers.size(), 3);
    }

    // Scuffed as hell
    private ProfileComponent resolved(ProfileComponent component) {
        if (component == null) return null;
        return new ProfileComponent(component.name(), Optional.empty(), new PropertyMap()).resolve();
    }
}
