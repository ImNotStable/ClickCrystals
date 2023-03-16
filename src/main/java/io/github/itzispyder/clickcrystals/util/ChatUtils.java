package io.github.itzispyder.clickcrystals.util;

import net.minecraft.text.Text;

import static io.github.itzispyder.clickcrystals.ClickCrystals.mc;
import static io.github.itzispyder.clickcrystals.ClickCrystals.starter;

/**
 * Chat utils for the client player entity
 */
public abstract class ChatUtils {

    public static void sendMessage(String message) {
         if (message == null) return;
         mc.player.sendMessage(Text.literal(message));
    }

    public static void sendPrefixMessage(String message) {
        sendMessage(starter + message);
    }
}