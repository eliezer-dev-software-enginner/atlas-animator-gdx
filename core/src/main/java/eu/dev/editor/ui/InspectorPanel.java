package eu.dev.editor.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import eu.dev.editor.codegen.ClassCodeGenerator;
import eu.dev.editor.scene.AnchorResolver;
import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneObject;
import eu.dev.editor.viewport.SceneViewport;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class InspectorPanel {
    private static final String NONE_LABEL = "(nenhum)";
    private static final String SCENE_LABEL = "(cena)";
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
    private final ImBoolean visibleField = new ImBoolean();
    private final ImInt regionPickIndex = new ImInt();
    private final ImFloat animFrameDurationField = new ImFloat();
    private final ImBoolean animLoopField = new ImBoolean();
    private SceneObject codeGeneratedFor;

    public void render(Scene scene, SceneObject selected, SceneViewport viewport, Consumer<SceneObject> onAddAtlas) {
        ImGui.setNextWindowPos(320, 40, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(300, 480, ImGuiCond.FirstUseEver);
        ImGui.begin("Inspector");
        WindowBounds.keepOnScreen();

        if (selected == null) {
            ImGui.text("Nenhum objeto selecionado");
        } else if (selected.type.equals("tilemap")) {
            renderTilemapView(selected);
        } else {
            idField.set(selected.id);
            if (ImGui.inputText("Id", idField)) selected.id = idField.get();

            ImGui.text("Texture: " + selected.texture);

            visibleField.set(selected.visible);
            if (ImGui.checkbox("Visível", visibleField)) selected.visible = visibleField.get();

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
            renderAnimationSection(viewport, selected, onAddAtlas);

            ImGui.separator();
            renderCodeGeneration(selected);
        }

        ImGui.end();
    }

    /**
     * Tilemaps skip position/anchor/animation/codegen entirely - none of that applies to a
     * background layer that's always drawn at world origin (see SceneViewport/SceneObject).
     */
    private void renderTilemapView(SceneObject selected) {
        idField.set(selected.id);
        if (ImGui.inputText("Id", idField)) selected.id = idField.get();

        ImGui.text("Tilemap: " + selected.tmx);
        ImGui.text(String.format("Tamanho: %.0f x %.0f px", selected.width, selected.height));

        visibleField.set(selected.visible);
        if (ImGui.checkbox("Visível", visibleField)) selected.visible = visibleField.get();

        ImGui.separator();
        ImGui.textWrapped("Tilemap sempre desenha na origem da cena (0,0) — posição, anchor e animação não se aplicam.");
    }

    private void renderAnchorCombo(Scene scene, SceneObject selected) {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        labels.add(NONE_LABEL);
        values.add("");
        labels.add(SCENE_LABEL);
        values.add(AnchorResolver.SCENE_ANCHOR);
        for (SceneObject object : scene.objects) {
            if (object != selected) {
                labels.add(object.id);
                values.add(object.id);
            }
        }

        int current = values.indexOf(selected.anchorOf);
        anchorIndex.set(Math.max(current, 0));

        String[] items = labels.toArray(new String[0]);
        if (ImGui.combo("Anchor", anchorIndex, items)) {
            selected.anchorOf = values.get(anchorIndex.get());
        }
    }

    private void renderAnimationSection(SceneViewport viewport, SceneObject selected, Consumer<SceneObject> onAddAtlas) {
        ImGui.text("Animação (atlas): " + (selected.atlas.isEmpty() ? "(nenhum)" : selected.atlas));
        if (ImGui.button("Selecionar atlas...")) {
            onAddAtlas.accept(selected);
        }
        if (!selected.atlas.isEmpty()) {
            ImGui.sameLine();
            if (ImGui.button("Remover atlas")) {
                selected.atlas = "";
                selected.animationRegions.clear();
            }
        }

        if (selected.atlas.isEmpty()) return;

        TextureAtlas textureAtlas = viewport.atlas(selected.atlas);
        Set<String> regionNames = new LinkedHashSet<>();
        for (TextureAtlas.AtlasRegion region : textureAtlas.getRegions()) {
            regionNames.add(region.name);
        }

        if (!regionNames.isEmpty()) {
            String[] items = regionNames.toArray(new String[0]);
            regionPickIndex.set(Math.max(0, Math.min(regionPickIndex.get(), items.length - 1)));
            ImGui.combo("Região", regionPickIndex, items);
            ImGui.sameLine();
            if (ImGui.button("Adicionar frame")) {
                selected.animationRegions.add(items[regionPickIndex.get()]);
            }
        }

        int removeIndex = -1;
        for (int i = 0; i < selected.animationRegions.size(); i++) {
            ImGui.text((i + 1) + ". " + selected.animationRegions.get(i));
            ImGui.sameLine();
            if (ImGui.smallButton("remover##anim" + i)) removeIndex = i;
        }
        if (removeIndex >= 0) selected.animationRegions.remove(removeIndex);

        animFrameDurationField.set(selected.animationFrameDuration);
        if (ImGui.inputFloat("Duração do frame", animFrameDurationField)) {
            selected.animationFrameDuration = Math.max(0.01f, animFrameDurationField.get());
        }

        animLoopField.set(selected.animationLoop);
        if (ImGui.checkbox("Loop", animLoopField)) selected.animationLoop = animLoopField.get();
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
