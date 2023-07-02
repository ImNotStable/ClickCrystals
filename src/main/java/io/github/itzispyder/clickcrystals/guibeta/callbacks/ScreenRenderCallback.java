package io.github.itzispyder.clickcrystals.guibeta.callbacks;

import net.minecraft.client.gui.DrawContext;

@FunctionalInterface
public interface ScreenRenderCallback {

    void handleScreen(DrawContext context, int mouseX, int mouseY, float delta);
}
