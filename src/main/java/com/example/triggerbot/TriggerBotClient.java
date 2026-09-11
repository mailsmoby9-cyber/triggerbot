package com.example.triggerbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class TriggerBotClient implements ClientModInitializer {
    private static final double MAX_DISTANCE_SQ = 9.0;
    private static KeyBinding toggleKey;
    private boolean enabled = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.triggerbot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("TriggerBot: " + (enabled ? "ON" : "OFF")),
                            true
                    );
                }
            }

            if (!enabled) return;
            if (client.player == null || client.world == null) return;
            if (client.currentScreen != null) return;
            if (client.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

            PlayerEntity target = findTarget(client);
            if (target != null) {
                client.player.attack(target);
                client.player.swingHand(Hand.MAIN_HAND);
            }
        });
    }

    private PlayerEntity findTarget(MinecraftClient client) {
        PlayerEntity best = null;
        double bestDist = MAX_DISTANCE_SQ;

        for (PlayerEntity p : client.world.getPlayers()) {
            if (p == client.player) continue;
            if (!p.isAlive()) continue;

            double dist = client.player.squaredDistanceTo(p);
            if (dist <= bestDist) {
                bestDist = dist;
                best = p;
            }
        }

        return best;
    }
}
