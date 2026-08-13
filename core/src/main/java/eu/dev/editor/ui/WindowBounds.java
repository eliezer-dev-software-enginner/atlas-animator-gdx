package eu.dev.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;

/** Keeps the current ImGui window fully within the visible display area. */
final class WindowBounds {
    private WindowBounds() {}

    /** Call right after ImGui.begin() for a window that should never be draggable off-screen. */
    static void keepOnScreen() {
        ImVec2 displaySize = ImGui.getIO().getDisplaySize();
        ImVec2 pos = ImGui.getWindowPos();
        ImVec2 size = ImGui.getWindowSize();

        float maxX = Math.max(0f, displaySize.x - size.x);
        float maxY = Math.max(0f, displaySize.y - size.y);
        float x = clamp(pos.x, 0f, maxX);
        float y = clamp(pos.y, 0f, maxY);

        if (x != pos.x || y != pos.y) {
            ImGui.setWindowPos(x, y);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
