package com.easyrefuel.fuel;

import com.easyrefuel.config.ModConfig;
import com.easyrefuel.model.Region;
import com.easyrefuel.region.RegionManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FuelSupplyHandler {
    private static final double MAX_INTERACTION_DISTANCE = 5.5;

    // Furnace ScreenHandler slot indices
    private static final int FURNACE_FUEL_SLOT = 1;

    // State machine
    // IDLE → WAITING_FOR_SCREEN → DELAY_BEFORE_INSERT → INSERTING
    //      → DELAY_BEFORE_CLOSE → DELAY_BEFORE_NEXT → (next furnace) → DONE
    private enum State {
        IDLE,
        WAITING_FOR_SCREEN,
        DELAY_BEFORE_INSERT,
        DELAY_BEFORE_CLOSE,
        DELAY_BEFORE_NEXT,
        DONE
    }

    private static final double CANCEL_MOVE_THRESHOLD = 0.5;

    private static State state = State.IDLE;
    private static List<FurnaceTask> pendingTasks = new ArrayList<>();
    private static int currentTaskIndex = 0;
    private static int waitTicks = 0;
    private static int filledCount = 0;
    private static int totalCount = 0;
    private static String lastFuelType = "";
    private static Vec3d startPosition = null;

    private record FurnaceTask(BlockPos pos, Item fuelItem, String fuelType) {}

    public static void registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (state == State.IDLE) return;
            tick(client);
        });
    }

    public static void executeFuelSupply() {
        // P key pressed while running -> cancel
        if (state != State.IDLE) {
            MinecraftClient c = MinecraftClient.getInstance();
            if (c.player != null) {
                closeScreen(c.player);
                sendMessage(c.player, String.format("Cancelled! Filled %d/%d furnaces.",
                        filledCount, totalCount), Formatting.YELLOW);
            }
            reset();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        World world = client.world;

        if (player == null || world == null) {
            return;
        }

        RegionManager regionManager = RegionManager.getInstance();
        List<Region> enabledRegions = regionManager.getEnabledRegions();

        if (enabledRegions.isEmpty()) {
            sendMessage(player, "No enabled regions found!", Formatting.RED);
            return;
        }

        // Build task list
        pendingTasks.clear();
        currentTaskIndex = 0;
        filledCount = 0;
        totalCount = 0;
        lastFuelType = "";

        for (Region region : enabledRegions) {
            if (!region.isEnabled()) continue;

            String fuelType = region.getFuelType();
            Item fuelItem = Registries.ITEM.get(Identifier.of(fuelType));

            for (BlockPos pos : region.getAllPositions()) {
                if (world.getBlockState(pos).getBlock() != Blocks.FURNACE) {
                    continue;
                }

                totalCount++;

                double distance = player.getPos().distanceTo(pos.toCenterPos());
                if (distance > MAX_INTERACTION_DISTANCE) {
                    continue;
                }

                // Check if player has fuel
                if (findFuelSlotInInventory(player, fuelItem) == -1) {
                    continue;
                }

                pendingTasks.add(new FurnaceTask(pos, fuelItem, fuelType));
            }
        }

        if (pendingTasks.isEmpty()) {
            sendMessage(player, "No furnaces found in enabled regions!", Formatting.RED);
            return;
        }

        // Start processing
        startPosition = player.getPos();
        state = State.WAITING_FOR_SCREEN;
        waitTicks = 0;
        openNextFurnace(client);
    }

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            reset();
            return;
        }

        // Cancel: player moved
        if (startPosition != null && player.getPos().distanceTo(startPosition) > CANCEL_MOVE_THRESHOLD) {
            closeScreen(player);
            sendMessage(player, String.format("Cancelled! (moved) Filled %d/%d furnaces.",
                    filledCount, totalCount), Formatting.YELLOW);
            reset();
            return;
        }

        int delay = ModConfig.getInstance().getTickDelay();

        switch (state) {
            case WAITING_FOR_SCREEN -> {
                waitTicks++;

                // Timeout after 20 ticks (1 second)
                if (waitTicks > 20) {
                    closeScreen(player);
                    advanceToNext();
                    return;
                }

                // Check if furnace screen is open
                if (player.currentScreenHandler instanceof FurnaceScreenHandler) {
                    // Screen opened -> wait before inserting
                    state = State.DELAY_BEFORE_INSERT;
                    waitTicks = 0;
                }
            }

            case DELAY_BEFORE_INSERT -> {
                // ESC pressed: screen closed unexpectedly
                if (!(player.currentScreenHandler instanceof FurnaceScreenHandler)) {
                    sendMessage(player, String.format("Cancelled! Filled %d/%d furnaces.",
                            filledCount, totalCount), Formatting.YELLOW);
                    reset();
                    return;
                }
                waitTicks++;
                if (waitTicks >= delay) {
                    insertFuel(client, player);
                    state = State.DELAY_BEFORE_CLOSE;
                    waitTicks = 0;
                }
            }

            case DELAY_BEFORE_CLOSE -> {
                waitTicks++;
                if (waitTicks >= delay) {
                    closeScreen(player);
                    advanceToNext();
                }
            }

            case DELAY_BEFORE_NEXT -> {
                waitTicks++;
                if (waitTicks >= delay) {
                    openNextFurnace(client);
                }
            }

            case DONE -> {
                if (filledCount > 0) {
                    if (filledCount == totalCount) {
                        sendMessage(player,
                            String.format("Successfully filled %d furnaces with %s",
                                filledCount, lastFuelType),
                            Formatting.GREEN);
                    } else {
                        sendMessage(player,
                            String.format("Filled %d/%d furnaces (fuel shortage or out of range)",
                                filledCount, totalCount),
                            Formatting.YELLOW);
                    }
                } else {
                    sendMessage(player, "No furnaces could be filled!", Formatting.RED);
                }
                reset();
            }

            default -> {}
        }
    }

    private static void insertFuel(MinecraftClient client, ClientPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof FurnaceScreenHandler furnaceHandler)) {
            return;
        }

        FurnaceTask task = pendingTasks.get(currentTaskIndex);
        int playerInvSlot = findFuelSlotInInventory(player, task.fuelItem);
        if (playerInvSlot == -1) {
            return;
        }

        int screenSlot = playerInvToScreenSlot(playerInvSlot);

        // Step 1: Pick up fuel from player inventory
        client.interactionManager.clickSlot(
            furnaceHandler.syncId, screenSlot, 0,
            SlotActionType.PICKUP, player
        );

        // Step 2: Place into fuel slot (index 1)
        client.interactionManager.clickSlot(
            furnaceHandler.syncId, FURNACE_FUEL_SLOT, 0,
            SlotActionType.PICKUP, player
        );

        // Step 3: If there was an existing item in fuel slot, put it back
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                furnaceHandler.syncId, screenSlot, 0,
                SlotActionType.PICKUP, player
            );
        }

        filledCount++;
        lastFuelType = task.fuelType;
    }

    private static void closeScreen(ClientPlayerEntity player) {
        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.closeHandledScreen();
        }
    }

    private static void advanceToNext() {
        currentTaskIndex++;
        if (currentTaskIndex < pendingTasks.size()) {
            state = State.DELAY_BEFORE_NEXT;
            waitTicks = 0;
        } else {
            state = State.DONE;
        }
    }

    private static void openNextFurnace(MinecraftClient client) {
        if (currentTaskIndex >= pendingTasks.size()) {
            state = State.DONE;
            return;
        }

        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (player == null || interactionManager == null) {
            reset();
            return;
        }

        FurnaceTask task = pendingTasks.get(currentTaskIndex);

        BlockHitResult hitResult = new BlockHitResult(
            Vec3d.ofCenter(task.pos),
            Direction.UP,
            task.pos,
            false
        );

        interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
        state = State.WAITING_FOR_SCREEN;
        waitTicks = 0;
    }

    /**
     * Convert PlayerInventory slot index to FurnaceScreenHandler slot index.
     * PlayerInventory: 0-8 = hotbar, 9-35 = main inventory
     * FurnaceScreenHandler: 0 = input, 1 = fuel, 2 = output, 3-29 = main, 30-38 = hotbar
     */
    private static int playerInvToScreenSlot(int playerSlot) {
        if (playerSlot < 9) {
            // Hotbar: PlayerInventory 0-8 → Screen 30-38
            return playerSlot + 30;
        } else {
            // Main inventory: PlayerInventory 9-35 → Screen 3-29
            return playerSlot - 9 + 3;
        }
    }

    private static int findFuelSlotInInventory(ClientPlayerEntity player, Item fuelItem) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == fuelItem && !stack.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static void reset() {
        state = State.IDLE;
        pendingTasks.clear();
        currentTaskIndex = 0;
        waitTicks = 0;
        filledCount = 0;
        totalCount = 0;
        lastFuelType = "";
        startPosition = null;
    }

    private static void sendMessage(ClientPlayerEntity player, String message, Formatting color) {
        player.sendMessage(
            Text.literal("[Auto-Fuel] ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(message).formatted(color)),
            false
        );
    }
}
