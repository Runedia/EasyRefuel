package com.easyrefuel.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.easyrefuel.EasyRefuelClient;
import com.easyrefuel.model.Region;
import com.easyrefuel.region.RegionManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("easyrefuel");
    private static final File REGIONS_FILE = CONFIG_DIR.resolve("regions.json").toFile();

    public static void initialize() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                EasyRefuelClient.LOGGER.info("Created config directory: {}", CONFIG_DIR);
            }
        } catch (IOException e) {
            EasyRefuelClient.LOGGER.error("Failed to create config directory", e);
        }
    }

    public static void saveRegions() {
        try {
            RegionManager manager = RegionManager.getInstance();
            RegionData data = new RegionData(manager.getAllRegions());

            try (FileWriter writer = new FileWriter(REGIONS_FILE)) {
                GSON.toJson(data, writer);
                EasyRefuelClient.LOGGER.info("Saved {} regions to {}", data.regions.size(), REGIONS_FILE);
            }
        } catch (IOException e) {
            EasyRefuelClient.LOGGER.error("Failed to save regions", e);
        }
    }

    public static void loadRegions() {
        if (!REGIONS_FILE.exists()) {
            EasyRefuelClient.LOGGER.info("No regions file found, starting with empty regions");
            return;
        }

        try (FileReader reader = new FileReader(REGIONS_FILE)) {
            RegionData data = GSON.fromJson(reader, RegionData.class);
            if (data != null && data.regions != null) {
                RegionManager.getInstance().setRegions(data.regions);
                EasyRefuelClient.LOGGER.info("Loaded {} regions from {}", data.regions.size(), REGIONS_FILE);
            }
        } catch (IOException e) {
            EasyRefuelClient.LOGGER.error("Failed to load regions", e);
        }
    }

    private static class RegionData {
        List<Region> regions;

        RegionData(List<Region> regions) {
            this.regions = regions;
        }
    }
}
