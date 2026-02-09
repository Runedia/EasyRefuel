package com.easyrefuel.config;

import com.easyrefuel.data.DataManager;
import com.easyrefuel.model.Region;
import com.easyrefuel.region.RegionManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.easyrefuel.title"))
                .setSavingRunnable(ConfigScreen::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ModConfig config = ModConfig.getInstance();

        // General Settings Category
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.easyrefuel.category.general"));

        general.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.easyrefuel.tick_delay"),
                        config.getTickDelay(),
                        1, 30)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("config.easyrefuel.tick_delay.tooltip"))
                .setSaveConsumer(config::setTickDelay)
                .build());

        // Behavior Settings Category
        ConfigCategory behavior = builder.getOrCreateCategory(Text.translatable("config.easyrefuel.category.behavior"));

        behavior.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("config.easyrefuel.fuel_slot_mode"),
                        ModConfig.FuelSlotMode.class,
                        config.getFuelSlotMode())
                .setDefaultValue(ModConfig.FuelSlotMode.TYPE_MATCH)
                .setTooltip(
                        Text.translatable("config.easyrefuel.fuel_slot_mode.tooltip.empty_only"),
                        Text.translatable("config.easyrefuel.fuel_slot_mode.tooltip.type_match"),
                        Text.translatable("config.easyrefuel.fuel_slot_mode.tooltip.force_replace"),
                        Text.translatable("config.easyrefuel.fuel_slot_mode.tooltip.max_fill"))
                .setSaveConsumer(config::setFuelSlotMode)
                .build());

        behavior.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("config.easyrefuel.inventory_shortage_mode"),
                        ModConfig.InventoryShortageMode.class,
                        config.getInventoryShortageMode())
                .setDefaultValue(ModConfig.InventoryShortageMode.FILL_PARTIAL)
                .setTooltip(
                        Text.translatable("config.easyrefuel.inventory_shortage_mode.tooltip.fill_partial"),
                        Text.translatable("config.easyrefuel.inventory_shortage_mode.tooltip.cancel_on_shortage"))
                .setSaveConsumer(config::setInventoryShortageMode)
                .build());

        // Region Management Category
        ConfigCategory regions = builder.getOrCreateCategory(Text.translatable("config.easyrefuel.category.regions"));

        RegionManager manager = RegionManager.getInstance();
        List<Region> regionList = manager.getAllRegions();

        if (regionList.isEmpty()) {
            regions.addEntry(entryBuilder.startTextDescription(
                    Text.translatable("config.easyrefuel.regions.empty"))
                    .build());
        } else {
            for (Region region : regionList) {
                final String regionName = region.getName();

                // Region header
                regions.addEntry(entryBuilder.startTextDescription(
                        Text.literal("=== " + regionName + " ===")
                            .append(Text.literal(" (" + region.getFuelType() + ")").formatted(Formatting.GRAY)))
                    .build());

                // Enable/Disable toggle
                regions.addEntry(entryBuilder.startBooleanToggle(
                                Text.translatable("config.easyrefuel.regions.enabled"),
                                region.isEnabled())
                        .setDefaultValue(true)
                        .setTooltip(Text.translatable("config.easyrefuel.regions.toggle.tooltip"))
                        .setSaveConsumer(enabled -> region.setEnabled(enabled))
                        .build());

                // Delete button
                regions.addEntry(entryBuilder.startBooleanToggle(
                                Text.translatable("config.easyrefuel.regions.delete.confirm")
                                    .formatted(Formatting.RED),
                                false)
                        .setDefaultValue(false)
                        .setTooltip(Text.translatable("config.easyrefuel.regions.delete.tooltip"))
                        .setSaveConsumer(delete -> {
                            if (delete) {
                                manager.removeRegion(regionName);
                                DataManager.saveRegions();
                            }
                        })
                        .build());
            }
        }

        return builder.build();
    }

    private static void saveConfig() {
        ModConfig.getInstance().save();
        DataManager.saveRegions();
    }
}
