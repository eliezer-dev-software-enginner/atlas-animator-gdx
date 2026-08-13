package eu.dev.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.ScreenUtils;
import eu.dev.editor.scene.SceneObject;
import eu.dev.editor.ui.EditorUI;
import eu.dev.editor.viewport.SceneViewport;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class EditorApplication extends ApplicationAdapter {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private SceneViewport viewport;
    private EditorUI editorUI;

    @Override
    public void create() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");

        viewport = new SceneViewport();
        viewport.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        editorUI = new EditorUI(viewport);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.resize(width, height);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1f);

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        SceneObject selected = viewport.handleInput(editorUI.getScene(), editorUI.getSelected());
        editorUI.setSelected(selected);

        viewport.render(editorUI.getScene());
        editorUI.render();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public void dispose() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
        viewport.dispose();
    }
}
