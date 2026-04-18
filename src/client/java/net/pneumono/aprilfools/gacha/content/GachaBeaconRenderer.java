package net.pneumono.aprilfools.gacha.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.aprilfools.gacha.GachaClientRegistry;
import net.pneumono.aprilfools.gacha.GachaRarity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class GachaBeaconRenderer implements BlockEntityRenderer<GachaBeaconBlockEntity, GachaBeaconRenderState> {
    public static final Material UNROLLED_CUBE_TEXTURE = getMaterial("unrolled");
    public static final Material COMMON_CUBE_TEXTURE = getMaterial("common");
    public static final Material UNCOMMON_CUBE_TEXTURE = getMaterial("uncommon");
    public static final Material RARE_CUBE_TEXTURE = getMaterial("rare");
    public static final Material EPIC_CUBE_TEXTURE = getMaterial("epic");
    public static final Material LEGENDARY_CUBE_TEXTURE = getMaterial("legendary");
    public static final Material MYTHIC_CUBE_TEXTURE = getMaterial("mythic");

    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final MaterialSet materials;
    private final PlayerModel widePlayerModel;
    private final PlayerModel slimPlayerModel;
    private final ModelPart cube;

    private static Material getMaterial(String name) {
        return Sheets.BLOCKS_MAPPER.apply(Divorcesteal.id("gacha_beacon_" + name));
    }

    public GachaBeaconRenderer(BlockEntityRendererProvider.Context context) {
        this(context.playerSkinRenderCache(), context.materials(), context.entityModelSet());
    }

    public GachaBeaconRenderer(SpecialModelRenderer.BakingContext context) {
        this(context.playerSkinRenderCache(), context.materials(), context.entityModelSet());
    }

    public GachaBeaconRenderer(PlayerSkinRenderCache cache, MaterialSet materials, EntityModelSet modelSet) {
        this.playerSkinRenderCache = cache;
        this.materials = materials;
        this.widePlayerModel = new PlayerModel(modelSet.bakeLayer(ModelLayers.PLAYER), false);
        this.slimPlayerModel = new PlayerModel(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.cube = modelSet.bakeLayer(GachaClientRegistry.GACHA_RARITY_CUBE);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("cube", new CubeListBuilder().texOffs(0, 0).addBox(0, 0, 0, 10, 11, 10), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public GachaBeaconRenderState createRenderState() {
        return new GachaBeaconRenderState();
    }

    @Override
    public void extractRenderState(
            GachaBeaconBlockEntity blockEntity,
            GachaBeaconRenderState state,
            float tickProgress, @NonNull Vec3 vec3,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, vec3, crumblingOverlay);

        if (blockEntity.getCurrentState() != GachaBeaconState.UNROLLED) {
            GachaResult finalResult = blockEntity.getFinalResult();
            List<GachaResult> possibleResults = blockEntity.getPossibleResults();

            if (finalResult == null) {
                return;
            } else if (possibleResults == null) {
                state.displayPlayer = createDisplayPlayer(finalResult);
            } else {
                GachaResult result = GachaBeaconBlock.calcuateResult(
                        blockEntity.getSpinTicks(), blockEntity.getRandomSpinMultiplier(), possibleResults
                );
                if (result == null) {
                    return;
                } else {
                    state.displayPlayer = createDisplayPlayer(result);
                }
            }

            Level level = blockEntity.getLevel();
            if (level != null) {
                state.rotation = ((blockEntity.getClientRenderAge() + tickProgress) * 5) % 360;
            }
        }
    }

    public DisplayPlayer createDisplayPlayer(GachaResult data) {
        ResolvableProfile profile = ResolvableProfile.createUnresolved(data.nameAndId().id());
        PlayerSkinRenderCache.RenderInfo renderInfo = this.playerSkinRenderCache.getOrDefault(profile);
        return new DisplayPlayer(
                data.rarity(), renderInfo.playerSkin().model(), renderInfo.renderType()
        );
    }

    @Override
    public void submit(
            GachaBeaconRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            @NonNull CameraRenderState cameraState
    ) {
        if (state.displayPlayer != null) {
            submitWithPlayer(state.rotation, state.displayPlayer, poseStack, collector, state.lightCoords, state.breakProgress);
        } else {
            submitWithoutPlayer(poseStack, collector, state.lightCoords, state.breakProgress);
        }
    }

    public void submitWithPlayer(
            float rotation,
            DisplayPlayer displayPlayer,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            int lightCoords,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        submitCube(displayPlayer.rarity(), poseStack, collector, lightCoords, breakProgress);
        submitPlayer(
                rotation, displayPlayer.modelType(), displayPlayer.renderType(),
                poseStack, collector, lightCoords, breakProgress
        );
    }

    public void submitWithoutPlayer(
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            int lightCoords,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        submitCube(null, poseStack, collector, lightCoords, breakProgress);
    }

    private void submitCube(
            @Nullable GachaRarity rarity,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        poseStack.pushPose();

        poseStack.scale(-1, -1, 1);
        poseStack.translate(-0.8125, -0.875, 0.1875);

        Material material = getCubeMaterial(rarity);
        TextureAtlasSprite sprite = this.materials.get(material);
        collector.submitModelPart(
                this.cube, poseStack,
                material.renderType(RenderTypes::entityTranslucent),
                lightCoords, OverlayTexture.NO_OVERLAY,
                sprite,
                -1, breakProgress
        );

        poseStack.popPose();
    }

    private void submitPlayer(
            float rotation,
            PlayerModelType modelType,
            RenderType renderType,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        poseStack.pushPose();

        poseStack.scale(-1, -1, 1);
        poseStack.translate(-0.5, -0.5625, 0.5);
        poseStack.scale(0.25f, 0.25f, 0.25f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        PlayerModel model = modelType == PlayerModelType.WIDE ? this.widePlayerModel : this.slimPlayerModel;
        AvatarRenderState avatarState = new AvatarRenderState();
        collector.submitModel(
                model, avatarState,
                poseStack, renderType,
                lightCoords, OverlayTexture.NO_OVERLAY,
                0, breakProgress
        );

        poseStack.popPose();
    }

    private Material getCubeMaterial(@Nullable GachaRarity rarity) {
        if (rarity == null) return UNROLLED_CUBE_TEXTURE;
        return switch (rarity) {
            case COMMON -> COMMON_CUBE_TEXTURE;
            case UNCOMMON -> UNCOMMON_CUBE_TEXTURE;
            case RARE -> RARE_CUBE_TEXTURE;
            case EPIC -> EPIC_CUBE_TEXTURE;
            case LEGENDARY -> LEGENDARY_CUBE_TEXTURE;
            case MYTHIC -> MYTHIC_CUBE_TEXTURE;
        };
    }
}
