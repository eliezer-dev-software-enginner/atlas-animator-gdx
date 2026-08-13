package eu.dev.editor.ui;

import eu.dev.editor.scene.SceneObject;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImString;

public class InspectorPanel {
    private final ImString idField = new ImString(64);
    private final ImFloat xField = new ImFloat();
    private final ImFloat yField = new ImFloat();
    private final ImFloat widthField = new ImFloat();
    private final ImFloat heightField = new ImFloat();

    public void render(SceneObject selected) {
        ImGui.begin("Inspector");

        if (selected == null) {
            ImGui.text("Nenhum objeto selecionado");
        } else {
            idField.set(selected.id);
            if (ImGui.inputText("Id", idField)) selected.id = idField.get();

            ImGui.text("Texture: " + selected.texture);

            xField.set(selected.x);
            if (ImGui.inputFloat("X", xField)) selected.x = xField.get();

            yField.set(selected.y);
            if (ImGui.inputFloat("Y", yField)) selected.y = yField.get();

            widthField.set(selected.width);
            if (ImGui.inputFloat("Width", widthField)) selected.width = widthField.get();

            heightField.set(selected.height);
            if (ImGui.inputFloat("Height", heightField)) selected.height = heightField.get();
        }

        ImGui.end();
    }
}
