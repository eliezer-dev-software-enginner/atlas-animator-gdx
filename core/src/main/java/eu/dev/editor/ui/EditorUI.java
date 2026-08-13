package eu.dev.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
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
import java.nio.file.Path;
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
        inspectorPanel.render(scene, selected, viewport, this::addAtlas);
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Scene")) {
                if (ImGui.menuItem("Add Sprite...")) addSprite();
                if (ImGui.menuItem("Add Tilemap...")) addTilemap();
                if (ImGui.menuItem("Load...")) load();
                if (ImGui.menuItem("Export...")) export();
                ImGui.endMenu();
            }
            if (ImGui.menuItem(viewport.isPaused() ? "Play" : "Pause")) {
                viewport.togglePause();
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

    private void addAtlas(SceneObject target) {
        runDialog(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Atlas", "atlas"));
            preselect(chooser, storage.getLastAtlasPath());
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File source = chooser.getSelectedFile();
            storage.setLastAtlasPath(source.getAbsolutePath());

            File atlasesDir = new File("sprites/atlases");
            atlasesDir.mkdirs();

            try {
                // Reads the atlas' own header to find its page image(s), so both the .atlas
                // and the .png(s) it points at get copied together - a lone .atlas file with
                // no matching image next to it wouldn't load.
                TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(
                        new FileHandle(source), new FileHandle(source.getParentFile()), false);

                File destAtlas = new File(atlasesDir, source.getName());
                Files.copy(source.toPath(), destAtlas.toPath(), StandardCopyOption.REPLACE_EXISTING);

                for (TextureAtlas.TextureAtlasData.Page page : data.getPages()) {
                    File pageSource = page.textureFile.file();
                    File pageDest = new File(atlasesDir, pageSource.getName());
                    Files.copy(pageSource.toPath(), pageDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                String relativePath = "sprites/atlases/" + destAtlas.getName();
                Gdx.app.postRunnable(() -> {
                    target.atlas = relativePath;
                    target.animationRegions.clear();
                });
            } catch (IOException e) {
                throw new RuntimeException("Falha ao importar atlas: " + source, e);
            }
        });
    }

    /**
     * Unlike Add Sprite/Add Atlas, this doesn't copy the .tmx anywhere - a Tiled map can
     * reference other tilesets (external .tsx files, each with their own image) and inline
     * tilesets with relative image paths that may point outside assets/maps/ entirely.
     * Reliably copying that whole dependency graph while keeping every relative reference
     * intact is a lot riskier than just requiring the file to already live under assets/,
     * which is also just how the map already on disk got here.
     */
    private void addTilemap() {
        runDialog(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Tiled Map", "tmx"));
            String lastTilemapPath = storage.getLastTilemapPath();
            if (!lastTilemapPath.isEmpty()) {
                preselect(chooser, lastTilemapPath);
            } else {
                File mapsDir = new File("maps");
                if (mapsDir.isDirectory()) chooser.setCurrentDirectory(mapsDir);
            }
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File source = chooser.getSelectedFile();
            storage.setLastTilemapPath(source.getAbsolutePath());
            String relativePath = relativeToAssets(source);

            Gdx.app.postRunnable(() -> {
                TiledMap map = viewport.tiledMap(relativePath);
                MapProperties props = map.getProperties();
                int mapWidth = props.get("width", Integer.class);
                int mapHeight = props.get("height", Integer.class);
                int tileWidth = props.get("tilewidth", Integer.class);
                int tileHeight = props.get("tileheight", Integer.class);

                SceneObject obj = new SceneObject();
                obj.id = stripExtension(source.getName());
                obj.type = "tilemap";
                obj.tmx = relativePath;
                obj.width = mapWidth * tileWidth;
                obj.height = mapHeight * tileHeight;

                scene.objects.add(obj);
                selected = obj;
            });
        });
    }

    /** Assumes cwd is the assets/ root, same convention "sprites/..." paths already rely on. */
    private static String relativeToAssets(File file) {
        Path assetsRoot = new File(".").getAbsoluteFile().toPath().normalize();
        Path chosen = file.getAbsoluteFile().toPath().normalize();
        return assetsRoot.relativize(chosen).toString().replace(File.separatorChar, '/');
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
