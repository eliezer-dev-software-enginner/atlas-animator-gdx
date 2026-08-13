package eu.dev.editor.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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
    private final InspectorPanel inspectorPanel = new InspectorPanel();

    private Scene scene = new Scene();
    private SceneObject selected;

    public EditorUI(SceneViewport viewport) {
        this.viewport = viewport;
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

    private void addSprite() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        File source = chooser.getSelectedFile();
        File spritesDir = new File("sprites");
        spritesDir.mkdirs();
        File dest = new File(spritesDir, source.getName());
        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao copiar sprite: " + source, e);
        }

        String relativePath = "sprites/" + dest.getName();
        Texture texture = viewport.texture(relativePath);

        SceneObject obj = new SceneObject();
        obj.id = stripExtension(dest.getName());
        obj.texture = relativePath;
        obj.width = texture.getWidth();
        obj.height = texture.getHeight();

        scene.objects.add(obj);
        selected = obj;
    }

    private void load() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Scene JSON", "json"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        scene = SceneJsonImporter.load(new FileHandle(chooser.getSelectedFile()));
        selected = null;
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Scene JSON", "json"));
        chooser.setSelectedFile(new File(scene.sceneName + ".json"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        SceneJsonExporter.export(scene, new FileHandle(file));
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
