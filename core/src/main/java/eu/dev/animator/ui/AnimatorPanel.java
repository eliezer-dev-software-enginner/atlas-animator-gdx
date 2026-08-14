package eu.dev.animator.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import eu.dev.animator.AtlasAnimation;
import eu.dev.animator.AtlasAnimationSnippetGenerator;
import eu.dev.animator.viewport.AnimationViewport;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.LinkedHashSet;
import java.util.Set;

public class AnimatorPanel {
    private static final int FIXED_FLAGS = ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize;

    private final ImInt regionPickIndex = new ImInt();
    private final ImFloat frameDurationField = new ImFloat();
    private final ImBoolean loopField = new ImBoolean();
    private final ImString codeBuffer = new ImString(2048);
    private boolean codeGenerated;
    private float copyFeedbackTimer;

    public void render(AtlasAnimation animation, AnimationViewport viewport, Runnable onSelectAtlas) {
        // ImGuiCond.Always (not FirstUseEver) so this stays put even if an old animator-layout.ini
        // has a different saved position - the panel is meant to be fixed, not just clamped on-screen.
        ImGui.setNextWindowPos(20, 20, ImGuiCond.Always);
        ImGui.setNextWindowSize(380, 480, ImGuiCond.Always);
        ImGui.begin("Animador", FIXED_FLAGS);

        ImGui.text("Atlas: " + (animation.atlas.isEmpty() ? "(nenhum)" : animation.atlas));
        if (ImGui.button("Selecionar atlas...")) {
            onSelectAtlas.run();
        }
        if (!animation.atlas.isEmpty()) {
            ImGui.sameLine();
            if (ImGui.button("Remover atlas")) {
                animation.atlas = "";
                animation.regions.clear();
                codeGenerated = false;
            }
        }

        if (animation.atlas.isEmpty()) {
            ImGui.end();
            return;
        }

        ImGui.separator();
        renderRegionPicker(viewport, animation);

        ImGui.separator();
        frameDurationField.set(animation.frameDuration);
        if (ImGui.inputFloat("Duração do frame", frameDurationField)) {
            animation.frameDuration = Math.max(0.01f, frameDurationField.get());
        }
        loopField.set(animation.loop);
        if (ImGui.checkbox("Repetir", loopField)) animation.loop = loopField.get();

        ImGui.separator();
        if (ImGui.button(viewport.isPaused() ? "Reproduzir" : "Pausar")) {
            viewport.togglePause();
        }

        ImGui.separator();
        renderSnippetGeneration(animation);

        ImGui.end();
    }

    private void renderRegionPicker(AnimationViewport viewport, AtlasAnimation animation) {
        TextureAtlas textureAtlas = viewport.atlas(animation.atlas);
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
                animation.regions.add(items[regionPickIndex.get()]);
            }
        }

        int removeIndex = -1;
        for (int i = 0; i < animation.regions.size(); i++) {
            ImGui.text((i + 1) + ". " + animation.regions.get(i));
            ImGui.sameLine();
            if (ImGui.smallButton("remover##anim" + i)) removeIndex = i;
        }
        if (removeIndex >= 0) animation.regions.remove(removeIndex);
    }

    private void renderSnippetGeneration(AtlasAnimation animation) {
        if (ImGui.button("Gerar snippet")) {
            codeBuffer.set(AtlasAnimationSnippetGenerator.generate(animation));
            codeGenerated = true;
        }
        if (codeGenerated) {
            ImGui.inputTextMultiline("##snippet", codeBuffer, 340, 160, ImGuiInputTextFlags.ReadOnly);
            if (ImGui.button("Copiar")) {
                ImGui.setClipboardText(codeBuffer.get());
                copyFeedbackTimer = 1.5f;
            }
            if (copyFeedbackTimer > 0f) {
                copyFeedbackTimer -= Gdx.graphics.getDeltaTime();
                ImGui.sameLine();
                ImGui.textColored(0.4f, 1f, 0.4f, 1f, "Copiado!");
            }
        }
    }
}
