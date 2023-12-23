package io.github.itzispyder.clickcrystals.gui.elements.overviewmode;

import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.misc.Gray;
import io.github.itzispyder.clickcrystals.gui.misc.brushes.RoundRectBrush;
import io.github.itzispyder.clickcrystals.gui.misc.organizers.GridOrganizer;
import io.github.itzispyder.clickcrystals.modules.Category;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.util.minecraft.RenderUtils;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

public class CategoryElement extends GuiElement {

    private static final Map<Category, Boolean> collapsionCache = new HashMap<>();
    private final Category category;
    private final GridOrganizer modules;
    private final int collapsedHeight = 25;
    private int uncollapsedHeight;
    private boolean collapsed;

    public CategoryElement(Category category, int x, int y, int width) {
        super(x, y, width, 0);
        this.category = category;
        this.modules = new GridOrganizer(x + 5, y + 20, width - 10, 10, 1, 2);

        for (Module m : system.getModuleByCategory(category)) {
            ModuleElement me = new ModuleElement(m, 0, 0, modules.getCellWidth(), modules.getCellHeight());
            modules.addEntry(me);
            this.addChild(me);
        }
        modules.organize();

        this.setCollapsed(collapsionCache.getOrDefault(category, true));
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY) {
        RoundRectBrush.drawRoundRect(context, x, y, width, height, 5, Gray.BLACK);
        RenderUtils.drawTexture(context, category.texture(), x + 5, y + 7, 10, 10);
        RenderUtils.drawText(context, category.name(), x + 18, y + 9, 1.0F, false);
        RenderUtils.drawText(context, collapsed ? "§7>" : "§b^", x + width - 10, y + 9, 1.0F, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isHoverCollapsion((int)mouseX, (int)mouseY)) {
            setCollapsed(!isCollapsed());
        }
    }

    public Category getCategory() {
        return category;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        this.uncollapsedHeight = collapsedHeight + modules.getEntries().size() * (modules.getCellHeight() + modules.getGap());
        this.height = collapsed ? collapsedHeight : uncollapsedHeight;

        if (!collapsed) {
            modules.getEntries().forEach(m -> m.setRendering(true));
        }
        else {
            modules.getEntries().forEach(m -> m.setRendering(false));
        }
        collapsionCache.put(category, collapsed);
    }

    public int getCollapsedHeight() {
        return collapsedHeight;
    }

    public int getUncollapsedHeight() {
        return uncollapsedHeight;
    }

    public boolean isHoverCollapsion(int mouseX, int mouseY) {
        return rendering && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + collapsedHeight;
    }
}
