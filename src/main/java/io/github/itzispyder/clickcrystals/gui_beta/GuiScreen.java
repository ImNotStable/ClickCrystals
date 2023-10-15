package io.github.itzispyder.clickcrystals.gui_beta;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.gui_beta.callbacks.*;
import io.github.itzispyder.clickcrystals.gui_beta.elements.Typeable;
import io.github.itzispyder.clickcrystals.gui_beta.elements.interactive.ScrollPanelElement;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.modules.clickcrystals.GuiBorders;
import io.github.itzispyder.clickcrystals.util.RenderUtils;
import io.github.itzispyder.clickcrystals.util.misc.Pair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;

public abstract class GuiScreen extends Screen implements Global {

    public final List<MouseMoveCallback> mouseMoveListeners;
    public final List<MouseClickCallback> mouseClickListeners;
    public final List<MouseDragCallback> mouseDragListeners;
    public final List<MouseScrollCallback> mouseScrollListeners;
    public final List<ScreenRenderCallback> screenRenderListeners;
    public final List<KeyPressCallback> keyActionListeners;
    public final List<GuiElement> children;
    public GuiElement selected, mostRecentlyAdded;
    public boolean shiftKeyPressed, altKeyPressed, ctrlKeyPressed;
    public Pair<Integer, Integer> cursor;

    public GuiScreen(String title) {
        super(Text.literal(title));

        this.mouseMoveListeners = new ArrayList<>();
        this.mouseClickListeners = new ArrayList<>();
        this.mouseDragListeners = new ArrayList<>();
        this.mouseScrollListeners = new ArrayList<>();
        this.screenRenderListeners = new ArrayList<>();
        this.keyActionListeners = new ArrayList<>();
        this.children = new ArrayList<>();
        this.selected = null;
        this.mostRecentlyAdded = null;
        this.cursor = Pair.of(0, 0);
    }

    public static boolean matchCurrent(Class<? extends GuiScreen> type) {
        return mc.currentScreen != null && mc.currentScreen.getClass() == type;
    }

    public abstract void baseRender(DrawContext context, int mouseX, int mouseY, float delta);

    @Override
    public void tick() {
        for (GuiElement child : children) {
            child.onTick();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (selected != null && selected.isDraggable()) {
            int dx = mouseX - cursor.left;
            int dy = mouseY - cursor.right;
            selected.move(dx, dy);
            selected.boundIn(context.getScaledWindowWidth(), context.getScaledWindowHeight());
            this.cursor = Pair.of(mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
        this.baseRender(context, mouseX, mouseY, delta);

        try {
            for (GuiElement guiElement : children) {
                guiElement.render(context, mouseX, mouseY);
            }
            for (ScreenRenderCallback callback : screenRenderListeners) {
                callback.handleScreen(context, mouseX, mouseY, delta);
            }
        }
        catch (ConcurrentModificationException ignore) {}

        Module guiBorders = Module.get(GuiBorders.class);
        if (guiBorders.isEnabled()) {
            GuiElement element = getHoveredElement(mouseX, mouseY);
            if (element != null) {
                tagGuiElement(context, mouseX, mouseY, element);
            }
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);

        for (MouseMoveCallback callback : mouseMoveListeners) {
            callback.handleMouse(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement child = children.get(i);
            if (child.isMouseOver((int)mouseX, (int)mouseY)) {
                this.selected = child;
                this.cursor = Pair.of((int)mouseX, (int)mouseY);
                child.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        for (MouseClickCallback callback : mouseClickListeners) {
            callback.handleMouse(mouseX, mouseY, button, ClickType.CLICK);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        if (!(selected instanceof Typeable)) {
            this.selected = null;
        }

        for (MouseClickCallback callback : mouseClickListeners) {
            callback.handleMouse(mouseX, mouseY, button, ClickType.RELEASE);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        for (MouseDragCallback callback : mouseDragListeners) {
            callback.handleMouse(mouseX, mouseY, button, deltaX, deltaY);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);

        for (MouseScrollCallback callback : mouseScrollListeners) {
            callback.handleMouse(mouseX, mouseY, amount);
        }

        for (GuiElement child : children) {
            if (child instanceof ScrollPanelElement panel && panel.isMouseOver((int)mouseX, (int)mouseY)) {
                panel.onScroll(amount);
                break;
            }
            scrollAt(child, (int)mouseX, (int)mouseY, amount);
        }

        return true;
    }

    private void scrollAt(GuiElement element, int mouseX, int mouseY, double amount) {
        if (element instanceof ScrollPanelElement panel && panel.isMouseOver(mouseX, mouseY)) {
            panel.onScroll(amount);
            return;
        }

        for (GuiElement child : element.getChildren()) {
            scrollAt(child, mouseX, mouseY, amount);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.shiftKeyPressed = true;
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.altKeyPressed = true;
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.ctrlKeyPressed = true;
        }

        super.keyPressed(keyCode, scanCode, modifiers);

        for (KeyPressCallback callback : keyActionListeners) {
            callback.handleKey(keyCode, ClickType.CLICK, scanCode, modifiers);
        }

        if (selected instanceof Typeable typeable) {
            typeable.onKey(keyCode, scanCode);
        }

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.shiftKeyPressed = false;
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.altKeyPressed = false;
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.ctrlKeyPressed = false;
        }

        super.keyReleased(keyCode, scanCode, modifiers);

        for (KeyPressCallback callback : keyActionListeners) {
            callback.handleKey(keyCode, ClickType.RELEASE, scanCode, modifiers);
        }

        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !(selected instanceof Typeable);
    }

    public List<GuiElement> getChildren() {
        return children;
    }

    public void clearChildren() {
        children.clear();
    }

    public void forEachChild(Consumer<GuiElement> action) {
        children.forEach(action);
    }

    public void addChild(GuiElement child) {
        if (child != null) {
            mostRecentlyAdded = child;
            children.add(child);
        }
    }

    public void removeChild(GuiElement child) {
        children.remove(child);
    }

    public void tagGuiElement(DrawContext context, int mouseX, int mouseY, GuiElement element) {
        String name = element.getClass().getSimpleName();
        double textScale = 0.7;
        int width = mc.textRenderer.getWidth(name) + 2;
        RenderUtils.fill(context, mouseX, mouseY, (int)(width * textScale), 9, 0xFF000000);
        RenderUtils.drawText(context, name, mouseX + 2, mouseY + (int)(9 * 0.33), 0.7F, true);
    }

    public GuiElement getHoveredElement(double mouseX, double mouseY) {
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement child = children.get(i);
            if (child.isHovered((int)mouseX, (int)mouseY)) {
                return child;
            }
        }
        return null;
    }
}