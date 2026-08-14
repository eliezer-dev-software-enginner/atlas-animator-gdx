package eu.dev.animator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ScreenUtils;
import eu.dev.animator.ui.AnimatorPanel;
import eu.dev.animator.viewport.AnimationViewport;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AnimatorApplication extends ApplicationAdapter {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final AtlasAnimation animation = new AtlasAnimation();

    private AnimationViewport viewport;
    private AnimatorPanel panel;
    private AppStorage storage;
    private volatile boolean dialogOpen;

    @Override
    public void create() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("animator-layout.ini");
        io.getFonts().addFontDefault();
        io.getFonts().build();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");

        viewport = new AnimationViewport();
        viewport.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        panel = new AnimatorPanel();
        storage = new AppStorage();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.resize(width, height);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1f);

        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();

        viewport.handleInput();
        viewport.render(animation);
        panel.render(animation, viewport, this::selectAtlas);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    /**
     * JFileChooser blocks its calling thread while the dialog is open. Calling it from the
     * render thread freezes the GLFW loop (the OS then reports the app as "not responding"),
     * so the dialog runs on its own background thread; GL-touching results are handed back to
     * the render thread via Gdx.app.postRunnable.
     */
    private void selectAtlas() {
        if (dialogOpen) return;
        dialogOpen = true;
        Thread thread = new Thread(() -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Atlas", "atlas"));
                String lastAtlasPath = storage.getLastAtlasPath();
                if (!lastAtlasPath.isEmpty()) chooser.setSelectedFile(new File(lastAtlasPath));
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

                File source = chooser.getSelectedFile();
                storage.setLastAtlasPath(source.getAbsolutePath());

                File atlasesDir = new File("atlases");
                atlasesDir.mkdirs();

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

                String relativePath = "atlases/" + destAtlas.getName();
                Gdx.app.postRunnable(() -> {
                    animation.atlas = relativePath;
                    animation.regions.clear();
                });
            } catch (IOException e) {
                throw new RuntimeException("Falha ao importar atlas", e);
            } finally {
                dialogOpen = false;
            }
        }, "animator-file-dialog");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void dispose() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
        viewport.dispose();
    }
}
