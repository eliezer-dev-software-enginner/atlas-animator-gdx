package eu.dev.editor.ui;

import eu.dev.editor.codegen.ClassCodeGenerator;
import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneObject;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

public class InspectorPanel {
    private static final String NONE = "(nenhum)";
    private static final String[] ALIGN_X = {"left", "center", "right"};
    private static final String[] ALIGN_Y = {"bottom", "center", "top"};

    private final ImString idField = new ImString(64);
    private final ImFloat xField = new ImFloat();
    private final ImFloat yField = new ImFloat();
    private final ImFloat widthField = new ImFloat();
    private final ImFloat heightField = new ImFloat();
    private final ImFloat offsetXField = new ImFloat();
    private final ImFloat offsetYField = new ImFloat();
    private final ImInt anchorIndex = new ImInt();
    private final ImInt alignXIndex = new ImInt();
    private final ImInt alignYIndex = new ImInt();
    private final ImString codeBuffer = new ImString(2048);
    private SceneObject codeGeneratedFor;

    public void render(Scene scene, SceneObject selected) {
        ImGui.setNextWindowPos(320, 40, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(300, 480, ImGuiCond.FirstUseEver);
        ImGui.begin("Inspector");

        if (selected == null) {
            ImGui.text("Nenhum objeto selecionado");
        } else {
            idField.set(selected.id);
            if (ImGui.inputText("Id", idField)) selected.id = idField.get();

            ImGui.text("Texture: " + selected.texture);
            ImGui.separator();

            renderAnchorCombo(scene, selected);

            if (selected.anchorOf.isEmpty()) {
                xField.set(selected.x);
                if (ImGui.inputFloat("X", xField)) selected.x = xField.get();

                yField.set(selected.y);
                if (ImGui.inputFloat("Y", yField)) selected.y = yField.get();
            } else {
                renderAlignCombo("Align X", ALIGN_X, alignXIndex, selected.anchorAlignX,
                        value -> selected.anchorAlignX = value);
                renderAlignCombo("Align Y", ALIGN_Y, alignYIndex, selected.anchorAlignY,
                        value -> selected.anchorAlignY = value);

                offsetXField.set(selected.anchorOffsetX);
                if (ImGui.inputFloat("Offset X", offsetXField)) selected.anchorOffsetX = offsetXField.get();

                offsetYField.set(selected.anchorOffsetY);
                if (ImGui.inputFloat("Offset Y", offsetYField)) selected.anchorOffsetY = offsetYField.get();

                ImGui.text(String.format("X: %.1f  Y: %.1f (calculado)", selected.x, selected.y));
            }

            widthField.set(selected.width);
            if (ImGui.inputFloat("Width", widthField)) selected.width = widthField.get();

            heightField.set(selected.height);
            if (ImGui.inputFloat("Height", heightField)) selected.height = heightField.get();

            ImGui.separator();
            renderCodeGeneration(selected);
        }

        ImGui.end();
    }

    private void renderAnchorCombo(Scene scene, SceneObject selected) {
        List<String> options = new ArrayList<>();
        options.add(NONE);
        for (SceneObject object : scene.objects) {
            if (object != selected) options.add(object.id);
        }

        int current = selected.anchorOf.isEmpty() ? 0 : options.indexOf(selected.anchorOf);
        anchorIndex.set(Math.max(current, 0));

        String[] items = options.toArray(new String[0]);
        if (ImGui.combo("Anchor", anchorIndex, items)) {
            String choice = items[anchorIndex.get()];
            selected.anchorOf = choice.equals(NONE) ? "" : choice;
        }
    }

    private void renderCodeGeneration(SceneObject selected) {
        if (ImGui.button("Gerar classe")) {
            codeBuffer.set(ClassCodeGenerator.generate(selected));
            codeGeneratedFor = selected;
        }
        if (codeGeneratedFor == selected) {
            ImGui.inputTextMultiline("##generatedCode", codeBuffer, 260, 180, ImGuiInputTextFlags.ReadOnly);
            if (ImGui.button("Copiar")) {
                ImGui.setClipboardText(codeBuffer.get());
            }
        }
    }

    private interface StringSetter {
        void set(String value);
    }

    private void renderAlignCombo(String label, String[] values, ImInt indexField, String current, StringSetter setter) {
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) index = i;
        }
        indexField.set(index);
        if (ImGui.combo(label, indexField, values)) {
            setter.set(values[indexField.get()]);
        }
    }
}
