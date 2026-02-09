package com.easyrefuel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.easyrefuel.EasyRefuelClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public enum FuelSlotMode {
        EMPTY_ONLY,        // Only insert if slot is empty
        TYPE_MATCH,        // Insert if same type or empty
        FORCE_REPLACE,     // Replace any fuel
        MAX_FILL          // Fill to max stack size
    }

    public enum InventoryShortageMode {
        FILL_PARTIAL,     // Fill as many as possible
        CANCEL_ON_SHORTAGE // Cancel if not enough fuel
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("easyrefuel");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("config.json").toFile();

    private static ModConfig instance;

    // Settings
    private int tickDelay = 1;
    private FuelSlotMode fuelSlotMode = FuelSlotMode.TYPE_MATCH;
    private InventoryShortageMode inventoryShortageMode = InventoryShortageMode.FILL_PARTIAL;

    private ModConfig() {}

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
            instance.load();
        }
        return instance;
    }

    public void load() {
        if (!CONFIG_FILE.exists()) {
            EasyRefuelClient.LOGGER.info("Config file not found, using defaults");
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                this.tickDelay = data.tickDelay;
                this.fuelSlotMode = data.fuelSlotMode;
                this.inventoryShortageMode = data.inventoryShortageMode;
                EasyRefuelClient.LOGGER.info("Config loaded successfully");
            }
        } catch (IOException e) {
            EasyRefuelClient.LOGGER.error("Failed to load config", e);
        }
    }

    public void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            ConfigData data = new ConfigData();
            data.tickDelay = this.tickDelay;
            data.fuelSlotMode = this.fuelSlotMode;
            data.inventoryShortageMode = this.inventoryShortageMode;

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
                EasyRefuelClient.LOGGER.info("Config saved successfully");
            }
        } catch (IOException e) {
            EasyRefuelClient.LOGGER.error("Failed to save config", e);
        }
    }

    public int getTickDelay() {
        return tickDelay;
    }

    public void setTickDelay(int tickDelay) {
        this.tickDelay = Math.max(1, Math.min(30, tickDelay));
    }

    public FuelSlotMode getFuelSlotMode() {
        return fuelSlotMode;
    }

    public void setFuelSlotMode(FuelSlotMode mode) {
        this.fuelSlotMode = mode;
    }

    public InventoryShortageMode getInventoryShortageMode() {
        return inventoryShortageMode;
    }

    public void setInventoryShortageMode(InventoryShortageMode mode) {
        this.inventoryShortageMode = mode;
    }

    private static class ConfigData {
        int tickDelay;
        FuelSlotMode fuelSlotMode;
        InventoryShortageMode inventoryShortageMode;
    }
}
