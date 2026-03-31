package net.pneumono.gacha;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.gacha.content.*;
import net.pneumono.gacha.networking.GachaBeaconResultS2CPayload;
import net.pneumono.gacha.networking.GachaBeaconSpinDataS2CPayload;

public class GachaClientRegistry {
    public static final ModelLayerLocation GACHA_RARITY_CUBE = new ModelLayerLocation(Divorcesteal.id("gacha_beacon"), "main");

    public static void register() {
        BlockRenderLayerMap.putBlock(GachaRegistry.GACHA_BEACON_BLOCK, ChunkSectionLayer.TRANSLUCENT);
        BlockEntityRenderers.register(GachaRegistry.GACHA_BEACON_ENTITY, GachaBeaconRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(GACHA_RARITY_CUBE, GachaBeaconRenderer::createLayer);
        SpecialModelRenderers.ID_MAPPER.put(Divorcesteal.id("gacha_beacon"), GachaBeaconSpecialRenderer.Unbaked.MAP_CODEC);

        MenuScreens.register(GachaRegistry.GACHA_BEACON_MENU, GachaBeaconScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(GachaBeaconSpinDataS2CPayload.ID, GachaClientRegistry::syncGachaBeaconSpinData);
        ClientPlayNetworking.registerGlobalReceiver(GachaBeaconResultS2CPayload.ID, GachaClientRegistry::syncGachaBeaconResult);

        ParticleFactoryRegistry.getInstance().register(GachaRegistry.GACHA_FINISH_PARTICLE, GachaFinishParticle.Provider::new);
    }

    private static void syncGachaBeaconSpinData(GachaBeaconSpinDataS2CPayload payload, ClientPlayNetworking.Context context) {
        AbstractContainerMenu currentMenu = context.player().containerMenu;
        if (currentMenu.containerId != payload.containerId() || !(currentMenu instanceof GachaBeaconMenu beaconMenu)) {
            Divorcesteal.LOGGER.warn("Failed to sync gacha beacon spin data!");
            return;
        }

        beaconMenu.setPossibleResults(payload.possibleResults());
        beaconMenu.setRandomSpinMultiplier(payload.randomSpinMultiplier());
        beaconMenu.setFinalResult(GachaBeaconBlock.calcuateFinalResult(payload.randomSpinMultiplier(), payload.possibleResults()));
    }

    private static void syncGachaBeaconResult(GachaBeaconResultS2CPayload payload, ClientPlayNetworking.Context context) {
        AbstractContainerMenu currentMenu = context.player().containerMenu;
        if (currentMenu.containerId != payload.containerId() || !(currentMenu instanceof GachaBeaconMenu beaconMenu)) {
            Divorcesteal.LOGGER.warn("Failed to sync gacha beacon result!");
            return;
        }

        beaconMenu.setPossibleResults(null);
        beaconMenu.setRandomSpinMultiplier(0);
        beaconMenu.setFinalResult(payload.result());
    }
}
