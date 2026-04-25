package lire.textfinder;

import lire.textfinder.event.ClientEventHandler;
import lire.textfinder.event.CommandRegistrationHandler;
import lire.textfinder.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFinder implements ClientModInitializer {
    public static final String MOD_ID = "textfinder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();
        LOGGER.info("[TextFinder] Config loaded");

        LOGGER.info("[TextFinder] Initializing");
        ClientEventHandler.registerEvents();
        CommandRegistrationHandler.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (config != null) {
                config.save();
                LOGGER.info("[TextFinder] Saved config and shutting down TextFinder");
            } else {
                LOGGER.warn("[TextFinder] Config instance is null, skipping save");
            }
        });

        LOGGER.info("[TextFinder] Initialization complete");
    }

}