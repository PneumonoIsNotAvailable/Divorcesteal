package net.pneumono.divorcesteal.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.ReviveBeaconMenu;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ReviveBeaconScreen extends AbstractContainerScreen<ReviveBeaconMenu> {
    private static final Identifier HEART_SLOT_TEXTURE = Divorcesteal.id("heart");
    private static final Identifier HEAD_SLOT_TEXTURE = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Component HEART_SLOT_TOOLTIP = Component.translatable("divorcesteal.gui.revive_beacon.add_heart");
    private static final Component HEAD_SLOT_TOOLTIP = Component.translatable("divorcesteal.gui.revive_beacon.add_head");
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

    public ReviveBeaconScreen(ReviveBeaconMenu handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
        this.imageWidth = 238;
        this.imageHeight = 179;
        this.inventoryLabelX = 39;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float deltaTicks, int mouseX, int mouseY) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256
        );

        renderInputSlot(graphics, mouseX, mouseY, this.menu.getTopHeartSlot(), HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        renderInputSlot(graphics, mouseX, mouseY, this.menu.getLeftHeartSlot(), HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        renderInputSlot(graphics, mouseX, mouseY, this.menu.getRightHeartSlot(), HEART_SLOT_TEXTURE, HEART_SLOT_TOOLTIP);
        renderInputSlot(graphics, mouseX, mouseY, this.menu.getHeadSlot(), HEAD_SLOT_TEXTURE, HEAD_SLOT_TOOLTIP);

        renderWantedPoster(graphics, mouseX, mouseY);

        renderPlayerSelect(graphics, mouseX, mouseY);

        renderReviveButton(graphics, mouseX, mouseY);
    }

    private void renderInputSlot(GuiGraphics graphics, int mouseX, int mouseY, Slot slot, Identifier texture, Component text) {
        if (slot.hasItem()) return;

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, texture,
                this.leftPos + slot.x, this.topPos + slot.y,
                16, 16
        );

        if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, this.font.split(text, 115), mouseX, mouseY);
        }
    }

    private void renderWantedPoster(GuiGraphics graphics, int mouseX, int mouseY) {
        Component wantedText = Component.translatable("divorcesteal.gui.revive_beacon.wanted");
        graphics.drawString(this.font,
                wantedText,
                this.leftPos + 43 - (font.width(wantedText) / 2), this.topPos + 28,
                CommonColors.DARK_GRAY, false
        );

        ResolvableProfile target = resolved(this.menu.getTarget());
        if (target != null) {
            ItemStack targetHeadStack = new ItemStack(Items.PLAYER_HEAD);
            targetHeadStack.set(DataComponents.PROFILE, target);
            graphics.renderItem(targetHeadStack, this.leftPos + 35, this.topPos + 41);
            if (this.isHovering(35, 41, 16, 16, mouseX, mouseY)) {
                Component targetTooltipText = target.name().map(Component::literal).orElseGet(
                        () -> Component.translatable("divorcesteal.unknown")
                ).withStyle(ChatFormatting.YELLOW);
                graphics.setTooltipForNextFrame(this.font, this.font.split(targetTooltipText, 115), mouseX, mouseY);
            }
        }
    }

    private void renderPlayerSelect(GuiGraphics graphics, int mouseX, int mouseY) {
        Identifier scrollTexture = canScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        int scrollOffset = (int)(39.0F * this.scrollPosition);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scrollTexture, this.leftPos + 218, this.topPos + 22 + scrollOffset, 12, 15);

        for (int menuY = 0; menuY < 3; ++menuY) { for (int menuX = 0; menuX < 3; ++menuX) {
            int playerIndex = (menuY + this.visibleTopRow) * 3 + menuX;
            ResolvableProfile profile = resolved(this.menu.getRevivableParticipant(playerIndex));
            if (profile == null) continue;

            renderSelectablePlayer(
                    graphics,
                    mouseX, mouseY, menuX, menuY,
                    profile,
                    playerIndex == this.menu.getSelectedParticipant()
            );
        }}
    }

    private void renderSelectablePlayer(
            GuiGraphics graphics,
            int mouseX, int mouseY,
            int menuX, int menuY,
            ResolvableProfile profile,
            boolean selected
    ) {
        int finalX = this.leftPos + 161 + (menuX * 18);
        int finalY = this.topPos + 22 + (menuY * 18);

        boolean highlighted = this.isPointStrictlyWithinBounds(finalX - this.leftPos, finalY - this.topPos, 18, 18, mouseX, mouseY);

        Identifier texture;
        if (selected) {
            texture = PLAYER_SELECTED_TEXTURE;
        } else if (highlighted) {
            texture = PLAYER_HIGHLIGHTED_TEXTURE;
        } else {
            texture = PLAYER_TEXTURE;
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, finalX, finalY, 18, 18);

        ItemStack targetHeadStack = new ItemStack(Items.PLAYER_HEAD);
        targetHeadStack.set(DataComponents.PROFILE, profile);
        graphics.renderItem(targetHeadStack, finalX + 1, finalY + 1);

        if (highlighted) {
            Component text = profile.name().map(Component::literal).orElseGet(
                    () -> Component.translatable("divorcesteal.unknown")
            );
            graphics.setTooltipForNextFrame(this.font, this.font.split(text, 115), mouseX, mouseY);
        }
    }

    private void renderReviveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.canRevive()) {
            int buttonX = this.leftPos + 87;
            int buttonY = this.topPos + 74;

            boolean highlighted = isPointStrictlyWithinBounds(87, 74, 64, 11, mouseX, mouseY);
            Identifier buttonTexture = highlighted ? REVIVE_BUTTON_HIGHLIGHTED_TEXTURE : REVIVE_BUTTON_TEXTURE;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, buttonTexture, buttonX, buttonY, 64, 11);

            Component reviveText = Component.translatable("divorcesteal.gui.revive_beacon.revive");
            graphics.drawString(this.font,
                    reviveText,
                    buttonX + 32 - (font.width(reviveText) / 2), buttonY + 2,
                    highlighted ? -128 : -9937334, false
            );
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (handlePlayerSelectMouseClick(event.x(), event.y())) return true;
        if (handleReviveButtonMouseClick(event.x(), event.y())) return true;

        return super.mouseClicked(event, bl);
    }

    private boolean handlePlayerSelectMouseClick(double mouseX, double mouseY) {
        Objects.requireNonNull(this.minecraft);
        this.scrollbarClicked = false;

        int finalX = this.leftPos + 161;
        int finalY = this.topPos + 22;
        for (int menuY = 0; menuY < 3; menuY++) { for (int menuX = 0; menuX < 3; menuX++) {

            double mouseXOffset = mouseX - (finalX + menuX * 18);
            double mouseYOffset = mouseY - (finalY + menuY * 18);
            int playerIndex = (menuY + this.visibleTopRow) * 3 + menuX;

            if (
                    mouseXOffset > 0.0 &&
                            mouseYOffset > 0.0 &&
                            mouseXOffset < 18.0 &&
                            mouseYOffset < 18.0 &&
                            this.menu.clickMenuButton(this.minecraft.player, playerIndex)
            ) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(DivorcestealRegistry.REVIVE_BEACON_SELECT_SOUND, 1.0F)
                );
                Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick(this.menu.containerId, playerIndex);
                return true;
            }
        }}

        if (canScroll()) {
            finalX = this.leftPos + 218;
            finalY = this.topPos + 22;
            if (mouseX >= finalX && mouseX < finalX + 12 && mouseY >= finalY && mouseY < finalY + 54) {
                this.scrollbarClicked = true;
                return true;
            }
        }

        return false;
    }

    private boolean handleReviveButtonMouseClick(double mouseX, double mouseY) {
        Objects.requireNonNull(this.minecraft);
        if (!this.menu.canRevive()) return false;

        int finalX = this.leftPos + 88;
        int finalY = this.topPos + 75;
        if (!(mouseX >= finalX && mouseX < finalX + 64 && mouseY >= finalY && mouseY < finalY + 11)) return false;

        if (this.menu.clickMenuButton(this.minecraft.player, -2)) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(DivorcestealRegistry.REVIVE_BEACON_SELECT_SOUND, 1.0F)
            );
            Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick(this.menu.containerId, -2);
            this.minecraft.setScreen(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double d, double e) {
        if (!canScroll() || !this.scrollbarClicked) return super.mouseDragged(mouseButtonEvent, d, e);

        int rows = this.getRows() - 3;
        int topY = this.topPos + 22;
        int bottomY = topY + 54;
        this.scrollPosition = ((float)mouseButtonEvent.y() - topY - 7.5F) / (bottomY - topY - 15.0F);
        this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
        this.visibleTopRow = Math.max((int)(this.scrollPosition * rows + 0.5), 0);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;

        if (canScroll()) {
            int rows = this.getRows() - 3;
            float f = (float)verticalAmount / rows;
            this.scrollPosition = Mth.clamp(this.scrollPosition - f, 0.0F, 1.0F);
            this.visibleTopRow = Math.max((int)(this.scrollPosition * rows + 0.5F), 0);
        }

        return true;
    }

    private boolean isPointStrictlyWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return this.isHovering(x + 1, y + 1, width - 2, height - 2, pointX, pointY);
    }

    private boolean canScroll() {
        return getRows() > 3;
    }

    private int getRows() {
        return Mth.positiveCeilDiv(this.menu.revivableParticipants.size(), 3);
    }

    // Scuffed as hell
    private ResolvableProfile resolved(ResolvableProfile component) {
        if (component == null) return null;
        return ResolvableProfile.createUnresolved(component.name().get());
    }
}
