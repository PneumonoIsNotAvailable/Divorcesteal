package net.pneumono.divorcesteal;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.pneumono.divorcesteal.command.DivorcestealCommands;
import net.pneumono.divorcesteal.registry.DivorcestealEvents;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Divorcesteal implements ModInitializer {
	public static final String MOD_ID = "divorcesteal";

	public static final Logger LOGGER = LoggerFactory.getLogger("Divorcesteal");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Divorcesteal");
		DivorcestealConfig.registerDivorcestealConfigs();
		DivorcestealRegistry.registerDivorcestealContent();
		DivorcestealEvents.registerDivorcestealEvents();
		DivorcestealCommands.registerDivorcestealCommands();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}