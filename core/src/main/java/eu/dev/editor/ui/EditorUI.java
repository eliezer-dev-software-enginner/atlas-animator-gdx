package eu.dev.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import eu.dev.editor.AppStorage;
import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneJsonExporter;
import eu.dev.editor.scene.SceneJsonImporter;
import eu.dev.editor.scene.SceneObject;
import eu.dev.editor.viewport.SceneViewport;
import imgui.ImGui;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class EditorUI {
    private final SceneViewport viewport;
    private final AppStorage storage;
    private final InspectorPanel inspectorPanel = new InspectorPanel();

    private Scene scene;
    private SceneObject selected;
    private volatile boolean dialogOpen;

    public EditorUI(SceneViewport viewport, AppStorage storage, Scene initialScene) {
        this.viewport = viewport;
        this.storage = storage;
        this.scene = initialScene != null ? initialScene : new Scene();
    }

    public Scene getScene() {
        return scene;
    }

    public SceneObject getSelected() {
        return selected;
    }

    public void setSelected(SceneObject selected) {
        this.selected = selected;
    }

    public void render() {
        renderMenuBar();
        selected = HierarchyPanel.render(scene, selected, viewport);
        inspectorPanel.render(selected);
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Scene")) {
                if (ImGui.menuItem("Add Sprite...")) addSprite();
                if (ImGui.menuItem("Load...")) load();
                if (ImGui.menuItem("Export...")) export();
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    /**
     * JFileChooser blocks its calling thread while the dialog is open. Calling it from the
     * render thread freezes the GLFW loop (the OS then reports the app as "not responding"),
     * so every dialog runs on its own background thread; GL-touching results are handed back
     * to the render thread via Gdx.app.postRunnable.
     */
    private void runDialog(Runnable dialogAndFollowUp) {
        if (dialogOpen) return;
        dialogOpen = true;
        Thread thread = new Thread(() -> {
            try {
                dialogAndFollowUp.run();
            } finally {
                dialogOpen = false;
            }
        }, "editor-file-dialog");
        thread.setDaemon(true);
        thread.start();
    }

    private void addSprite() {
        runDialog(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
            preselect(chooser, storage.getLastSpritePath());
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File source = chooser.getSelectedFile();
            storage.setLastSpritePath(source.getAbsolutePath());
            File spritesDir = new File("sprites");
            spritesDir.mkdirs();
            File dest = new File(spritesDir, source.getName());
            try {
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Falha ao copiar sprite: " + source, e);
            }

            String relativePath = "sprites/" + dest.getName();
            Gdx.app.postRunnable(() -> {
                Texture texture = viewport.texture(relativePath);

                SceneObject obj = new SceneObject();
                obj.id = stripExtension(dest.getName());
                obj.texture = relativePath;
                obj.width = texture.getWidth();
                obj.height = texture.getHeight();

                scene.objects.add(obj);
                selected = obj;
            });
        });
    }

    private void load() {
        runDialog(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Scene JSON", "json"));
            preselect(chooser, storage.getLastLoadPath());
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();
            storage.setLastLoadPath(file.getAbsolutePath());
            storage.setLastScenePath(file.getAbsolutePath());

            Scene loaded = SceneJsonImporter.load(new FileHandle(file));
            Gdx.app.postRunnable(() -> {
                scene = loaded;
                selected = null;
            });
        });
    }

    private void export() {
        runDialog(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Scene JSON", "json"));
            String lastExportPath = storage.getLastExportPath();
            File lastExportDir = lastExportPath.isEmpty() ? null : new File(lastExportPath).getParentFile();
            if (lastExportDir != null) chooser.setCurrentDirectory(lastExportDir);
            chooser.setSelectedFile(new File(scene.sceneName + ".json"));
            if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }
            storage.setLastExportPath(file.getAbsolutePath());
            storage.setLastScenePath(file.getAbsolutePath());
            SceneJsonExporter.export(scene, new FileHandle(file));
        });
    }

    private static void preselect(JFileChooser chooser, String lastPath) {
        if (!lastPath.isEmpty()) chooser.setSelectedFile(new File(lastPath));
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
