package com.easyrefuel.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.easyrefuel.data.DataManager;
import com.easyrefuel.model.Position;
import com.easyrefuel.model.Region;
import com.easyrefuel.region.RegionManager;
import com.easyrefuel.region.RegionSelector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CommandRegistry {
    // Common fuel items for suggestions
    private static final String[] COMMON_FUELS = {
        "minecraft:coal",
        "minecraft:charcoal",
        "minecraft:coal_block",
        "minecraft:lava_bucket",
        "minecraft:blaze_rod",
        "minecraft:dried_kelp_block",
        "minecraft:bamboo",
        "minecraft:stick"
    };

    private static final SuggestionProvider<FabricClientCommandSource> FUEL_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(COMMON_FUELS, builder);
    };

    private static final SuggestionProvider<FabricClientCommandSource> REGION_SUGGESTIONS = (context, builder) -> {
        RegionManager manager = RegionManager.getInstance();
        return CommandSource.suggestMatching(
            manager.getAllRegions().stream().map(Region::getName),
            builder
        );
    };

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("easyrefuel")
                            .then(ClientCommandManager.literal("create")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .then(ClientCommandManager.argument("fuel_type", StringArgumentType.greedyString())
                                                    .suggests(FUEL_SUGGESTIONS)
                                                    .executes(CommandRegistry::createRegion))))
                            .then(ClientCommandManager.literal("list")
                                    .executes(CommandRegistry::listRegions))
                            .then(ClientCommandManager.literal("delete")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .suggests(REGION_SUGGESTIONS)
                                            .executes(CommandRegistry::deleteRegion)))
                            .then(ClientCommandManager.literal("toggle")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .suggests(REGION_SUGGESTIONS)
                                            .executes(CommandRegistry::toggleRegion)))
                            .then(ClientCommandManager.literal("setfuel")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .suggests(REGION_SUGGESTIONS)
                                            .then(ClientCommandManager.argument("fuel_type", StringArgumentType.greedyString())
                                                    .suggests(FUEL_SUGGESTIONS)
                                                    .executes(CommandRegistry::setFuelType))))
            );
        });
    }

    private static int createRegion(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        String fuelType = StringArgumentType.getString(context, "fuel_type");

        RegionSelector selector = RegionSelector.getInstance();
        if (!selector.hasBothPositions()) {
            sendMessage(context, "You must select two positions first using wooden axe!", Formatting.RED);
            return 0;
        }

        RegionManager manager = RegionManager.getInstance();
        if (manager.hasRegion(name)) {
            sendMessage(context, "Region with name '" + name + "' already exists!", Formatting.RED);
            return 0;
        }

        Position pos1 = selector.convertToPosition(selector.getPos1());
        Position pos2 = selector.convertToPosition(selector.getPos2());
        Region region = new Region(name, pos1, pos2, fuelType);

        manager.addRegion(region);
        DataManager.saveRegions();
        selector.reset();

        sendMessage(context, "Region '" + name + "' created successfully!", Formatting.GREEN);
        return 1;
    }

    private static int listRegions(CommandContext<FabricClientCommandSource> context) {
        RegionManager manager = RegionManager.getInstance();
        List<Region> regions = manager.getAllRegions();

        if (regions.isEmpty()) {
            sendMessage(context, "No regions found!", Formatting.YELLOW);
            return 0;
        }

        sendMessage(context, "=== Regions ===", Formatting.GOLD);
        for (Region region : regions) {
            String status = region.isEnabled() ? "ENABLED" : "DISABLED";
            Formatting color = region.isEnabled() ? Formatting.GREEN : Formatting.RED;

            context.getSource().sendFeedback(
                    Text.literal("- " + region.getName())
                            .formatted(Formatting.WHITE)
                            .append(Text.literal(" [" + status + "]").formatted(color))
                            .append(Text.literal(" Fuel: " + region.getFuelType()).formatted(Formatting.GRAY))
            );
        }

        return 1;
    }

    private static int deleteRegion(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        RegionManager manager = RegionManager.getInstance();

        if (manager.removeRegion(name)) {
            DataManager.saveRegions();
            sendMessage(context, "Region '" + name + "' deleted successfully!", Formatting.GREEN);
            return 1;
        } else {
            sendMessage(context, "Region '" + name + "' not found!", Formatting.RED);
            return 0;
        }
    }

    private static int toggleRegion(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        RegionManager manager = RegionManager.getInstance();

        return manager.getRegion(name).map(region -> {
            region.setEnabled(!region.isEnabled());
            DataManager.saveRegions();

            String status = region.isEnabled() ? "enabled" : "disabled";
            sendMessage(context, "Region '" + name + "' " + status + "!", Formatting.GREEN);
            return 1;
        }).orElseGet(() -> {
            sendMessage(context, "Region '" + name + "' not found!", Formatting.RED);
            return 0;
        });
    }

    private static int setFuelType(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        String fuelType = StringArgumentType.getString(context, "fuel_type");
        RegionManager manager = RegionManager.getInstance();

        return manager.getRegion(name).map(region -> {
            region.setFuelType(fuelType);
            DataManager.saveRegions();
            sendMessage(context, "Region '" + name + "' fuel type set to: " + fuelType, Formatting.GREEN);
            return 1;
        }).orElseGet(() -> {
            sendMessage(context, "Region '" + name + "' not found!", Formatting.RED);
            return 0;
        });
    }

    private static void sendMessage(CommandContext<FabricClientCommandSource> context, String message, Formatting color) {
        context.getSource().sendFeedback(
                Text.literal("[Easy Refuel] ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal(message).formatted(color))
        );
    }
}
