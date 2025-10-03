package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;
import net.pneumono.divorcesteal.content.ReviveBeaconInfoS2CPayload;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(ReviveBeaconInfoS2CPayload.ID, DivorcestealClient::syncReviveBeaconTarget);

		BlockRenderLayerMap.putBlock(DivorcestealRegistry.REVIVE_BEACON_BLOCK, BlockRenderLayer.CUTOUT);

		ResourceManagerHelper.registerBuiltinResourcePack(
				Divorcesteal.id("retextured_hearts"),
				FabricLoader.getInstance().getModContainer(Divorcesteal.MOD_ID).orElseThrow(),
				Text.translatable("divorcesteal.resource_pack.retextured_hearts"),
				ResourcePackActivationType.NORMAL
		);
	}

	private static void syncReviveBeaconTarget(ReviveBeaconInfoS2CPayload payload, ClientPlayNetworking.Context context) {
		ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
		if (currentScreenHandler.syncId != payload.syncId() || !(currentScreenHandler instanceof ReviveBeaconScreenHandler beaconHandler)) {
			Divorcesteal.LOGGER.warn("Failed to sync revive beacon target!");
			return;
		}

		beaconHandler.setTarget(payload.target());
		beaconHandler.setRevivableParticipants(payload.revivableParticipants());
	}
}