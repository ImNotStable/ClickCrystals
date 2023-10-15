package io.github.itzispyder.clickcrystals.gui_beta.elements.client.module;

import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui_beta.misc.Gray;
import io.github.itzispyder.clickcrystals.gui_beta.misc.brushes.RoundRectBrush;
import io.github.itzispyder.clickcrystals.modules.settings.DoubleSetting;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;

public class DoubleSettingElement extends SettingElement<DoubleSetting> {

    private boolean settingRenderUpdates;
    private int fillEnd;

    public DoubleSettingElement(DoubleSetting setting, int x, int y) {
        super(setting, x, y);
        this.settingRenderUpdates = true;
        this.fillEnd = x;
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY) {
        this.renderSettingDetails(context);
        int drawY = y + height / 2;
        int drawX = x + width / 4 * 3;
        int len;

        if (mc.currentScreen instanceof GuiScreen screen && screen.selected == this) {
            this.fillEnd = MathUtils.minMax(mouseX, drawX + 10, x + width);
            double range = setting.getMax() - setting.getMin();
            double ratio = (double)(fillEnd - drawX) / (double)(x + width - drawX);
            double value = range * ratio;
            setting.setVal(value + setting.getMin());
        }

        double range = setting.getMax() - setting.getMin();
        double value = setting.getVal() - setting.getMin();
        double ratio = value / range;
        len = (int)((x + width - drawX) * ratio);

        if (settingRenderUpdates) {
            setting.setVal(range * ratio + setting.getMin());
        }

        RoundRectBrush.drawRoundHoriLine(context, drawX, drawY, width / 4, 10, Gray.GRAY);
        RoundRectBrush.drawRoundHoriLine(context, drawX, drawY, len, 10, Gray.GENERIC);
        RenderUtils.drawRightText(context, "" + setting.getVal(), fillEnd, drawY - 6, 0.7F, false);
        this.fillEnd = drawX + len;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (mc.currentScreen instanceof GuiScreen screen) {
            screen.selected = this;
        }
    }

    public void enableSettingRenderUpdates() {
        settingRenderUpdates = true;
    }

    public void disableSettingRenderUpdates() {
        settingRenderUpdates = false;
    }

    public DoubleSetting getSetting() {
        return setting;
    }
}