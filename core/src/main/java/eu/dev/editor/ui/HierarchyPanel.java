package eu.dev.editor.ui;

import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneObject;
import eu.dev.editor.viewport.SceneViewport;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImFloat;
import imgui.type.ImString;

import java.util.Iterator;

public class HierarchyPanel {
    private static final ImString sceneNameBuffer = new ImString(64);
    private static final ImFloat sceneWidthField = new ImFloat();
    private static final ImFloat sceneHeightField = new ImFloat();

    public static SceneObject render(Scene scene, SceneObject selected, SceneViewport viewport) {
        ImGui.setNextWindowPos(20, 40, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(280, 400, ImGuiCond.FirstUseEver);
        ImGui.begin("Hierarchy");

        sceneNameBuffer.set(scene.sceneName);
        if (ImGui.inputText("Scene", sceneNameBuffer)) {
            scene.sceneName = sceneNameBuffer.get();
        }

        sceneWidthField.set(scene.sceneWidth);
        if (ImGui.inputFloat("Scene Width", sceneWidthField)) scene.sceneWidth = sceneWidthField.get();

        sceneHeightField.set(scene.sceneHeight);
        if (ImGui.inputFloat("Scene Height", sceneHeightField)) scene.sceneHeight = sceneHeightField.get();

        ImGui.separator();

        SceneObject toRemove = null;
        Iterator<SceneObject> it = scene.objects.iterator();
        while (it.hasNext()) {
            SceneObject obj = it.next();
            boolean isSelected = obj == selected;
            if (ImGui.selectable(obj.id.isEmpty() ? "(sem nome)" : obj.id, isSelected)) {
                selected = obj;
            }
            ImGui.sameLine();
            if (ImGui.smallButton("remover##" + System.identityHashCode(obj))) {
                toRemove = obj;
            }
        }

        if (toRemove != null) {
            scene.objects.remove(toRemove);
            if (!toRemove.texture.isEmpty()) viewport.releaseTexture(toRemove.texture);
            if (selected == toRemove) selected = null;
        }

        ImGui.end();
        return selected;
    }
}
