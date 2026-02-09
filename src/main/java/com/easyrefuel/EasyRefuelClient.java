package com.easyrefuel;

import com.easyrefuel.command.CommandRegistry;
import com.easyrefuel.data.DataManager;
import com.easyrefuel.fuel.FuelSupplyHandler;
import com.easyrefuel.keybind.KeyBindingHandler;
import com.easyrefuel.region.RegionSelectionHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyRefuelClient implements ClientModInitializer {
    public static final String MOD_ID = "easyrefuel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Easy Refuel initializing...");

        // Initialize data manager
        DataManager.initialize();

        // Register event handlers
        RegionSelectionHandler.register();
        KeyBindingHandler.register();
        CommandRegistry.register();
        FuelSupplyHandler.registerTickHandler();

        // Load regions on client start
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DataManager.loadRegions();
        });

        // Save regions on client stopping
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            DataManager.saveRegions();
        });

        LOGGER.info("Easy Refuel initialized successfully");
    }
}
