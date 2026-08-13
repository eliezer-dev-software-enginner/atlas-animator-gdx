package eu.dev.editor.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneObject;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.HashMap;
import java.util.Map;

public class SceneViewport {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final Map<String, Texture> textures = new HashMap<>();

    private SceneObject dragging;
    private float dragOffsetX, dragOffsetY;
    private boolean panning;
    private float lastMouseX, lastMouseY;

    public SceneViewport() {
        camera.setToOrtho(false);
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public Texture texture(String relativePath) {
        return textures.computeIfAbsent(relativePath, p -> new Texture(Gdx.files.internal(p)));
    }

    public void render(Scene scene) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (SceneObject obj : scene.objects) {
            if (obj.texture.isEmpty()) continue;
            batch.draw(texture(obj.texture), obj.x, obj.y, obj.width, obj.height);
        }
        batch.end();
    }

    /** Returns the newly selected object, or the previous selection if the click missed empty space over the viewport. */
    public SceneObject handleInput(Scene scene, SceneObject selected) {
        boolean capturedByUi = ImGui.getIO().getWantCaptureMouse();

        ImVec2 mouse = ImGui.getMousePos();
        Vector3 world = camera.unproject(new Vector3(mouse.x, mouse.y, 0));

        float wheel = ImGui.getIO().getMouseWheel();
        if (wheel != 0 && !capturedByUi) {
            camera.zoom = Math.max(0.1f, camera.zoom - wheel * 0.1f);
        }

        boolean middleDown = ImGui.isMouseDown(2);
        if (middleDown && !capturedByUi) {
            if (panning) {
                camera.position.x -= (mouse.x - lastMouseX) * camera.zoom;
                camera.position.y += (mouse.y - lastMouseY) * camera.zoom;
            }
            panning = true;
            lastMouseX = mouse.x;
            lastMouseY = mouse.y;
        } else {
            panning = false;
        }

        if (!capturedByUi && ImGui.isMouseClicked(0)) {
            SceneObject picked = pick(scene, world.x, world.y);
            if (picked != null) {
                selected = picked;
                // Anchored objects are positioned by AnchorResolver every frame - dragging one
                // directly would just get overwritten. Editing its offset in the inspector is
                // the way to move it.
                if (picked.anchorOf.isEmpty()) {
                    dragging = picked;
                    dragOffsetX = world.x - picked.x;
                    dragOffsetY = world.y - picked.y;
                }
            }
        }
        if (dragging != null && ImGui.isMouseDown(0)) {
            dragging.x = world.x - dragOffsetX;
            dragging.y = world.y - dragOffsetY;
        }
        if (!ImGui.isMouseDown(0)) {
            dragging = null;
        }

        return selected;
    }

    private SceneObject pick(Scene scene, float worldX, float worldY) {
        for (int i = scene.objects.size() - 1; i >= 0; i--) {
            SceneObject obj = scene.objects.get(i);
            if (worldX >= obj.x && worldX <= obj.x + obj.width && worldY >= obj.y && worldY <= obj.y + obj.height) {
                return obj;
            }
        }
        return null;
    }

    public void releaseTexture(String relativePath) {
        Texture texture = textures.remove(relativePath);
        if (texture != null) texture.dispose();
    }

    public void dispose() {
        batch.dispose();
        textures.values().forEach(Texture::dispose);
    }
}
