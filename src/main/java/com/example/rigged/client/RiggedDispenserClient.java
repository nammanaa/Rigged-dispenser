package com.example.rigged.client;

import com.example.rigged.ModePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RiggedDispenserClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("riggeddispenser", "main"));
        KeyBinding left = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.riggeddispenser.left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, category));
        KeyBinding right = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.riggeddispenser.right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (left.wasPressed()) ClientPlayNetworking.send(new ModePayload(1));
            while (right.wasPressed()) ClientPlayNetworking.send(new ModePayload(2));
        });
    }
}
