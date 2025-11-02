package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.pneumono.divorcesteal.content.ReviveBeaconMenu;
import net.pneumono.divorcesteal.content.ReviveBeaconInfoS2CPayload;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(ReviveBeaconInfoS2CPayload.ID, DivorcestealClient::syncReviveBeaconTarget);

		BlockRenderLayerMap.putBlock(DivorcestealRegistry.REVIVE_BEACON_BLOCK, ChunkSectionLayer.CUTOUT);

		ResourceManagerHelper.registerBuiltinResourcePack(
				Divorcesteal.id("retextured_hearts"),
				FabricLoader.getInstance().getModContainer(Divorcesteal.MOD_ID).orElseThrow(),
				Component.translatable("divorcesteal.resource_pack.retextured_hearts"),
				ResourcePackActivationType.NORMAL
		);
	}

	private static void syncReviveBeaconTarget(ReviveBeaconInfoS2CPayload payload, ClientPlayNetworking.Context context) {
		AbstractContainerMenu currentMenu = context.player().containerMenu;
		if (currentMenu.containerId != payload.containerId() || !(currentMenu instanceof ReviveBeaconMenu beaconHandler)) {
			Divorcesteal.LOGGER.warn("Failed to sync revive beacon target!");
			return;
		}

		beaconHandler.setTarget(payload.target());
		beaconHandler.setRevivableParticipants(payload.revivableParticipants());
	}
}