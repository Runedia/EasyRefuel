package com.easyrefuel.keybind;

import com.easyrefuel.fuel.FuelSupplyHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindingHandler {
    private static KeyBinding fuelSupplyKey;

    public static void register() {
        fuelSupplyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.easyrefuel.supply",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.easyrefuel"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (fuelSupplyKey.wasPressed()) {
                FuelSupplyHandler.executeFuelSupply();
            }
        });
    }

    public static KeyBinding getFuelSupplyKey() {
        return fuelSupplyKey;
    }
}
