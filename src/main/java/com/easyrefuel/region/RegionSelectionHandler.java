package com.easyrefuel.region;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class RegionSelectionHandler {
    private static final int MAX_FURNACES = 20;
    private static final int MIN_FURNACES = 1;

    public static void register() {
        // Left click - Set position 1
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getMainHandStack().getItem() == Items.WOODEN_AXE) {
                if (world.isClient) {
                    RegionSelector selector = RegionSelector.getInstance();
                    // Skip if same position as before
                    if (pos.equals(selector.getPos1())) {
                        return ActionResult.SUCCESS;
                    }
                    selector.setPos1(pos);
                    player.sendMessage(
                            Text.literal("[Auto-Fuel] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("Position 1: " + pos.toShortString())
                                            .formatted(Formatting.WHITE)),
                            false
                    );
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        // Right click - Set position 2 and validate
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.getMainHandStack().getItem() == Items.WOODEN_AXE) {
                if (world.isClient) {
                    BlockPos pos = hitResult.getBlockPos();
                    RegionSelector selector = RegionSelector.getInstance();
                    // Skip if same position as before
                    if (pos.equals(selector.getPos2())) {
                        return ActionResult.SUCCESS;
                    }
                    selector.setPos2(pos);

                    player.sendMessage(
                            Text.literal("[Auto-Fuel] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("Position 2: " + pos.toShortString())
                                            .formatted(Formatting.WHITE)),
                            false
                    );

                    // Validate if both positions are set
                    if (selector.hasBothPositions()) {
                        validateAndPromptRegionCreation(player, world, selector);
                    }
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private static void validateAndPromptRegionCreation(
            net.minecraft.entity.player.PlayerEntity player,
            net.minecraft.world.World world,
            RegionSelector selector) {

        BlockPos pos1 = selector.getPos1();
        BlockPos pos2 = selector.getPos2();

        // Calculate region bounds
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Count furnaces and check for non-furnace blocks
        int furnaceCount = 0;
        boolean hasNonFurnaceBlock = false;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    if (world.getBlockState(checkPos).getBlock() == Blocks.FURNACE) {
                        furnaceCount++;
                    } else {
                        hasNonFurnaceBlock = true;
                    }
                }
            }
        }

        // Validation
        if (hasNonFurnaceBlock) {
            player.sendMessage(
                    Text.literal("[Auto-Fuel] ")
                            .formatted(Formatting.RED)
                            .append(Text.literal("Region contains non-furnace blocks! Region creation cancelled.")
                                    .formatted(Formatting.WHITE)),
                    false
            );
            selector.reset();
            return;
        }

        if (furnaceCount < MIN_FURNACES || furnaceCount > MAX_FURNACES) {
            player.sendMessage(
                    Text.literal("[Auto-Fuel] ")
                            .formatted(Formatting.RED)
                            .append(Text.literal(String.format(
                                    "Invalid furnace count: %d (must be between %d and %d). Region creation cancelled.",
                                    furnaceCount, MIN_FURNACES, MAX_FURNACES))
                                    .formatted(Formatting.WHITE)),
                    false
            );
            selector.reset();
            return;
        }

        // Success - ready to create region
        player.sendMessage(
                Text.literal("[Auto-Fuel] ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal(String.format(
                                "Valid region with %d furnaces! Use /refill create <name> <fuel_type> to save this region.",
                                furnaceCount))
                                .formatted(Formatting.WHITE)),
                false
        );
    }
}
