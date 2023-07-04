package io.github.itzispyder.clickcrystals.guibeta.elements.cc;

import io.github.itzispyder.clickcrystals.guibeta.GuiElement;
import io.github.itzispyder.clickcrystals.guibeta.TexturesIdentifiers;
import io.github.itzispyder.clickcrystals.guibeta.screens.ModulesScreen;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.util.DrawableUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import static io.github.itzispyder.clickcrystals.ClickCrystals.PREFIX;
import static io.github.itzispyder.clickcrystals.ClickCrystals.mc;

public class ModuleElement extends GuiElement {

    private final Module module;

    public ModuleElement(Module module, int x, int y, int width) {
        super(x, y, width, width / 4);
        this.module = module;
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY) {
        Identifier texture = TexturesIdentifiers.MODULE_EMPTY_TEXTURE;

        if (module != null) {
            if (module.isEnabled()) {
                texture = TexturesIdentifiers.MODULE_ON_TEXTURE;
            }
            else {
                texture = TexturesIdentifiers.MODULE_OFF_TEXTURE;
            }
        }

        context.drawTexture(texture, x, y, 0, 0, width, height, width, height);

        if (module != null) {
            DrawableUtils.drawText(context, module.getNameLimited(), x + 7, y + (int)(height * 0.33), 0.5F, true);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            module.toggle();
        }
        else if (button == 1 && mc.currentScreen instanceof ModulesScreen screen) {
            screen.alertWidget.setTitle(PREFIX + module.getName());
            screen.alertWidget.setMessage(module.getDescription());
            screen.alertWidget.setRendering(true);
        }
    }

    public Module getModule() {
        return module;
    }
}
