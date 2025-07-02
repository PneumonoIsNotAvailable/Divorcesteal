package net.pneumono.divorcesteal;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.pneumono.divorcesteal.content.DivorcestealRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Divorcesteal implements ModInitializer {
	public static final String MOD_ID = "divorcesteal";

	public static final Logger LOGGER = LoggerFactory.getLogger("Divorcesteal");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Divorcesteal");
		DivorcestealRegistry.registerDivorcestealContent();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}