package net.pneumono.aprilfools.gacha.content;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.aprilfools.gacha.GachaRarity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class GachaBeaconScreen extends AbstractContainerScreen<GachaBeaconMenu> {
    private static final Identifier GACHA_WHEEL_COMMON_TEXTURE = Divorcesteal.id("gacha_wheel_common");
    private static final Identifier GACHA_WHEEL_UNCOMMON_TEXTURE = Divorcesteal.id("gacha_wheel_uncommon");
    private static final Identifier GACHA_WHEEL_RARE_TEXTURE = Divorcesteal.id("gacha_wheel_rare");
    private static final Identifier GACHA_WHEEL_EPIC_TEXTURE = Divorcesteal.id("gacha_wheel_epic");
    private static final Identifier GACHA_WHEEL_LEGENDARY_TEXTURE = Divorcesteal.id("gacha_wheel_legendary");
    private static final Identifier GACHA_WHEEL_MYTHIC_TEXTURE = Divorcesteal.id("gacha_wheel_mythic");
    private static final Identifier GACHA_RESULT_COMMON_TEXTURE = Divorcesteal.id("gacha_result_common");
    private static final Identifier GACHA_RESULT_UNCOMMON_TEXTURE = Divorcesteal.id("gacha_result_uncommon");
    private static final Identifier GACHA_RESULT_RARE_TEXTURE = Divorcesteal.id("gacha_result_rare");
    private static final Identifier GACHA_RESULT_EPIC_TEXTURE = Divorcesteal.id("gacha_result_epic");
    private static final Identifier GACHA_RESULT_LEGENDARY_TEXTURE = Divorcesteal.id("gacha_result_legendary");
    private static final Identifier GACHA_RESULT_MYTHIC_TEXTURE = Divorcesteal.id("gacha_result_mythic");
    private static final Identifier GACHA_LEVER_1_TEXTURE = Divorcesteal.id("gacha_lever_1");
    private static final Identifier GACHA_LEVER_2_TEXTURE = Divorcesteal.id("gacha_lever_2");
    private static final Identifier GACHA_LEVER_3_TEXTURE = Divorcesteal.id("gacha_lever_3");
    private static final Identifier GACHA_LEVER_4_TEXTURE = Divorcesteal.id("gacha_lever_4");
    private static final Identifier GACHA_LEVER_SELECTED_TEXTURE = Divorcesteal.id("gacha_lever_selected");
    private static final Identifier TEXTURE = Divorcesteal.id("textures/gui/gacha_beacon.png");

    // DIAMETER = sqrt(11^2 + 25^2 - 2*11*25*cos90)
    private static final float DIAMETER = 27.313f;
    private static final double ANGLE = Math.atan(11f / 25f);

    private final Map<UUID, ResolvableProfile> PROFILE_MAP = new HashMap<>();

    private long age = 0;

    public GachaBeaconScreen(GachaBeaconMenu abstractContainerMenu, Inventory inventory, Component title) {
        super(abstractContainerMenu, inventory, title);
        this.imageHeight = 169;
        this.inventoryLabelY = 75;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.age++;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        if (isMouseInLeverArea((int) event.x(), (int) event.y()) && this.menu.clickMenuButton(Objects.requireNonNull(this.minecraft.player), 0)) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(DivorcestealRegistry.REVIVE_BEACON_SELECT_SOUND, 1.0F)
            );
            Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick(this.menu.containerId, -2);
            return true;
        }

        return super.mouseClicked(event, bl);
    }

    public boolean isMouseInLeverArea(int mouseX, int mouseY) {
        int leverX = this.leftPos + 131;
        int leverY = this.topPos + 13;
        return mouseX >= leverX && mouseX < leverX + 22 && mouseY >= leverY && mouseY < leverY + 22;
    }

    @Override
    protected void renderBg(@NonNull GuiGraphics graphics, float tickProgress, int mouseX, int mouseY) {
        if (this.menu.getState() != GachaBeaconState.ROLLED) {
            int spinTicks = this.menu.getSpinTicks();
            int shake = spinTicks == 3 ? 2 : 0;
            renderSpinBg(graphics, mouseX, mouseY, spinTicks, tickProgress, shake);
        } else {
            renderResultBg(graphics, this.menu.getFinalResult(), tickProgress);
        }
    }

    private void renderResultBg(GuiGraphics graphics, GachaResult result, float tickProgress) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256
        );

        renderLever(graphics, 0, 0, 4, 0);

        if (result != null) {
            renderResult(graphics, result, tickProgress);
        }
    }

    private void renderSpinBg(GuiGraphics graphics, int mouseX, int mouseY, int spinTicks, float tickProgress, int shake) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos + shake,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256
        );

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, GACHA_RESULT_COMMON_TEXTURE,
                this.leftPos + 60, this.topPos + 17,
                56, 56
        );

        renderWheel(graphics, spinTicks + tickProgress, shake);

        renderLever(graphics, mouseX, mouseY, spinTicks + 1, shake);
    }

    private void renderResult(GuiGraphics graphics, GachaResult result, float tickProgress) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, getResultTexture(result),
                this.leftPos + 60, this.topPos + 17,
                56, 56
        );

        renderResultPlayer(graphics, result, tickProgress);

        graphics.drawCenteredString(
                this.font, Component.translatable(result.rarity().getTranslationKey()),
                this.leftPos + 88, this.topPos + 62,
                CommonColors.WHITE
        );
    }

    private Identifier getResultTexture(GachaResult result) {
        if (result == null) return GACHA_RESULT_COMMON_TEXTURE;

        GachaRarity rarity = result.rarity();
        return switch (rarity) {
            case COMMON -> GACHA_RESULT_COMMON_TEXTURE;
            case UNCOMMON -> GACHA_RESULT_UNCOMMON_TEXTURE;
            case RARE -> GACHA_RESULT_RARE_TEXTURE;
            case EPIC -> GACHA_RESULT_EPIC_TEXTURE;
            case LEGENDARY -> GACHA_RESULT_LEGENDARY_TEXTURE;
            case MYTHIC -> GACHA_RESULT_MYTHIC_TEXTURE;
        };
    }

    private void renderResultPlayer(GuiGraphics graphics, GachaResult result, float tickProgress) {
        AvatarRenderState state = new AvatarRenderState();
        state.entityType = EntityType.PLAYER;
        state.skin = this.minecraft.playerSkinRenderCache()
                .getOrDefault(getProfile(result.nameAndId().id())).playerSkin();
        int x0 = this.leftPos + 61;
        int x1 = x0 + 54;
        int y0 = this.topPos + 18;
        int y1 = y0 + 54;

        Vector3f translation = new Vector3f(0.0F, 1.0F, 0.0F);

        float rotationDegrees;
        if (this.minecraft.level != null) {
            rotationDegrees = ((this.age + tickProgress) * 5) % 360;
        } else {
            rotationDegrees = 90;
        }
        Quaternionf angle = new Quaternionf().rotationXYZ(0, (float) (rotationDegrees * Math.PI / 180), (float) Math.PI);

        int angleMaybeIdk = 25;
        graphics.submitEntityRenderState(
                state,
                angleMaybeIdk, translation, angle, null,
                x0, y0, x1, y1
        );
    }

    private void renderWheel(GuiGraphics graphics, float spinTicks, int shake) {
        double change = GachaBeaconBlock.spinTickDoubleFunction(spinTicks, this.menu.getRandomSpinMultiplier());

        double changeDifference = change - ((int) change);
        double rotationOffset = 2 * ANGLE * changeDifference;

        int changes = (int) change;
        List<GachaResult> results = this.menu.getPossibleResults();

        graphics.pose().pushMatrix();

        graphics.pose().translate(this.leftPos + 61, this.topPos + 18 + shake);

        renderWheelFace(graphics, getResultSafe(changes + 2, results), (Math.PI / 2f) - (5 * ANGLE) + rotationOffset);
        renderWheelFace(graphics, getResultSafe(changes + 1, results), (Math.PI / 2f) - (3 * ANGLE) + rotationOffset);
        renderWheelFace(graphics, getResultSafe(changes, results), (Math.PI / 2f) - ANGLE + rotationOffset);
        renderWheelFace(graphics, getResultSafe(changes - 1, results), (Math.PI / 2f) + (1 * ANGLE) + rotationOffset);
        renderWheelFace(graphics, getResultSafe(changes - 2, results), (Math.PI / 2f) + (3 * ANGLE) + rotationOffset);

        graphics.pose().popMatrix();
    }

    private void renderWheelFace(GuiGraphics graphics, GachaResult result, double rotation) {
        graphics.pose().pushMatrix();

        float yOffset = (float) (27 - (DIAMETER * Math.cos(rotation - ANGLE)));
        graphics.pose().translate(0, yOffset);
        graphics.pose().scale(1, (float) Math.cos((Math.PI / 2f) - rotation));

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, getWheelTexture(result),
                0, 0,
                54, 22
        );
        if (result != null) {
            ItemStack resultHeadStack = new ItemStack(Items.PLAYER_HEAD);
            ResolvableProfile profile = getProfile(result.nameAndId().id());
            resultHeadStack.set(DataComponents.PROFILE, profile);
            graphics.renderItem(resultHeadStack, 19, 3);
        }

        graphics.pose().popMatrix();
    }

    private GachaResult getResultSafe(int index, List<GachaResult> results) {
        if (results == null) return null;
        int size = results.size();
        int newIndex = index % size;
        while (newIndex < 0) {
            newIndex += size;
        }
        return results.get(newIndex);
    }

    private Identifier getWheelTexture(GachaResult result) {
        if (result == null) return GACHA_WHEEL_COMMON_TEXTURE;

        GachaRarity rarity = result.rarity();
        return switch (rarity) {
            case COMMON -> GACHA_WHEEL_COMMON_TEXTURE;
            case UNCOMMON -> GACHA_WHEEL_UNCOMMON_TEXTURE;
            case RARE -> GACHA_WHEEL_RARE_TEXTURE;
            case EPIC -> GACHA_WHEEL_EPIC_TEXTURE;
            case LEGENDARY -> GACHA_WHEEL_LEGENDARY_TEXTURE;
            case MYTHIC -> GACHA_WHEEL_MYTHIC_TEXTURE;
        };
    }

    private void renderLever(GuiGraphics graphics, int mouseX, int mouseY, int leverState, int shake) {
        Identifier texture = switch (leverState) {
            case 0 -> isMouseInLeverArea(mouseX, mouseY) ? GACHA_LEVER_SELECTED_TEXTURE : GACHA_LEVER_1_TEXTURE;
            case 1 -> GACHA_LEVER_2_TEXTURE;
            case 2 -> GACHA_LEVER_3_TEXTURE;
            default -> GACHA_LEVER_4_TEXTURE;
        };

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, texture,
                this.leftPos + 130, this.topPos + 12 + shake,
                24, 66
        );
    }

    public ResolvableProfile getProfile(UUID uuid) {
        return PROFILE_MAP.computeIfAbsent(uuid, ResolvableProfile::createUnresolved);
    }
}
