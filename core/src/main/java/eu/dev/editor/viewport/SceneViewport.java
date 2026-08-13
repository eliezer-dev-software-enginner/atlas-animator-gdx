package eu.dev.editor.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import eu.dev.editor.scene.Scene;
import eu.dev.editor.scene.SceneObject;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.HashMap;
import java.util.Map;

public class SceneViewport {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, TextureAtlas> atlases = new HashMap<>();
    private final Map<String, TiledMap> tiledMaps = new HashMap<>();
    private final Map<String, TiledMapRenderer> tiledMapRenderers = new HashMap<>();
    private final Map<SceneObject, Float> animationStateTimes = new HashMap<>();

    private SceneObject dragging;
    private float dragOffsetX, dragOffsetY;
    private boolean panning;
    private float lastMouseX, lastMouseY;
    private boolean paused;

    public SceneViewport() {
        camera.setToOrtho(false);
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public Texture texture(String relativePath) {
        return textures.computeIfAbsent(relativePath, p -> new Texture(Gdx.files.internal(p)));
    }

    public TextureAtlas atlas(String relativePath) {
        return atlases.computeIfAbsent(relativePath, p -> new TextureAtlas(Gdx.files.internal(p)));
    }

    public TiledMap tiledMap(String relativePath) {
        return tiledMaps.computeIfAbsent(relativePath, p -> new TmxMapLoader().load(p));
    }

    private TiledMapRenderer tiledMapRenderer(String relativePath) {
        return tiledMapRenderers.computeIfAbsent(relativePath, p -> new OrthogonalTiledMapRenderer(tiledMap(p), batch));
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    private static boolean isAnimated(SceneObject obj) {
        return !obj.atlas.isEmpty() && !obj.animationRegions.isEmpty();
    }

    private Animation<TextureRegion> buildAnimation(SceneObject obj) {
        TextureAtlas textureAtlas = atlas(obj.atlas);
        Array<TextureRegion> frames = new Array<>();
        for (String region : obj.animationRegions) {
            TextureRegion found = textureAtlas.findRegion(region);
            if (found != null) frames.add(found);
        }
        if (frames.size == 0) return null;
        return new Animation<>(obj.animationFrameDuration, frames,
                obj.animationLoop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    }

    public void render(Scene scene) {
        camera.update();

        // Tilemaps render in their own begin/end cycle (TiledMapRenderer manages the batch
        // internally, can't nest inside the sprite batch's own begin/end below), always at
        // world origin, and always first so it reads as a background/ground layer.
        for (SceneObject obj : scene.objects) {
            if (obj.visible && obj.type.equals("tilemap") && !obj.tmx.isEmpty()) {
                TiledMapRenderer renderer = tiledMapRenderer(obj.tmx);
                renderer.setView(camera);
                renderer.render();
            }
        }

        // Reference frame for scene-relative anchors - without seeing where the scene bounds
        // actually are, "anchor to the scene" would just be anchoring to an invisible box.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(0, 0, scene.sceneWidth, scene.sceneHeight);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (SceneObject obj : scene.objects) {
            if (!obj.visible || obj.type.equals("tilemap")) continue;

            if (isAnimated(obj)) {
                Animation<TextureRegion> animation = buildAnimation(obj);
                if (animation == null) continue;
                float stateTime = animationStateTimes.getOrDefault(obj, 0f);
                if (!paused) stateTime += Gdx.graphics.getDeltaTime();
                animationStateTimes.put(obj, stateTime);
                batch.draw(animation.getKeyFrame(stateTime), obj.x, obj.y, obj.width, obj.height);
            } else if (!obj.texture.isEmpty()) {
                batch.draw(texture(obj.texture), obj.x, obj.y, obj.width, obj.height);
            }
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
                // the way to move it. Tilemaps always render at world origin, not x/y - nothing
                // to drag.
                if (picked.anchorOf.isEmpty() && !picked.type.equals("tilemap")) {
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

    /**
     * Tilemaps are checked in a separate, lower-priority pass regardless of list order - a
     * tilemap's bounds usually cover the whole scene, and it's always drawn first/beneath, so
     * it should never steal a click from a smaller object sitting on top of it.
     */
    private SceneObject pick(Scene scene, float worldX, float worldY) {
        for (int i = scene.objects.size() - 1; i >= 0; i--) {
            SceneObject obj = scene.objects.get(i);
            if (!obj.visible || obj.type.equals("tilemap")) continue;
            if (contains(obj, worldX, worldY)) return obj;
        }
        for (int i = scene.objects.size() - 1; i >= 0; i--) {
            SceneObject obj = scene.objects.get(i);
            if (!obj.visible || !obj.type.equals("tilemap")) continue;
            if (contains(obj, worldX, worldY)) return obj;
        }
        return null;
    }

    private static boolean contains(SceneObject obj, float worldX, float worldY) {
        return worldX >= obj.x && worldX <= obj.x + obj.width && worldY >= obj.y && worldY <= obj.y + obj.height;
    }

    public void releaseTexture(String relativePath) {
        Texture texture = textures.remove(relativePath);
        if (texture != null) texture.dispose();
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        textures.values().forEach(Texture::dispose);
        atlases.values().forEach(TextureAtlas::dispose);
        tiledMaps.values().forEach(TiledMap::dispose);
    }
}
