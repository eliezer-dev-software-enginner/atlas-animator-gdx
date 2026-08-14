package eu.dev.animator.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import eu.dev.animator.AtlasAnimation;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.HashMap;
import java.util.Map;

/**
 * Plays back the single current AtlasAnimation, drawn centered at the world origin. The
 * camera is offset so that origin lands in the upper-right area of the window instead of the
 * bottom-left corner (where an unshifted y-up ortho camera centered at width/2,height/2 would
 * otherwise put it) - clear of the fixed Animator panel on the left, and near the top rather
 * than vertically centered.
 */
public class AnimationViewport {
    private static final float PREVIEW_X_FRACTION = 0.72f;
    private static final float PREVIEW_Y_FRACTION = 0.8f;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final Map<String, TextureAtlas> atlases = new HashMap<>();

    private boolean panning;
    private float lastMouseX, lastMouseY;
    private float stateTime;
    private boolean paused;

    public AnimationViewport() {
        camera.setToOrtho(false);
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.x = width * (0.5f - PREVIEW_X_FRACTION);
        camera.position.y = height * (0.5f - PREVIEW_Y_FRACTION);
    }

    public TextureAtlas atlas(String relativePath) {
        return atlases.computeIfAbsent(relativePath, p -> new TextureAtlas(Gdx.files.internal(p)));
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    public void handleInput() {
        boolean capturedByUi = ImGui.getIO().getWantCaptureMouse();
        ImVec2 mouse = ImGui.getMousePos();

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
    }

    public void render(AtlasAnimation animation) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Animation<TextureRegion> anim = build(animation);
        if (anim != null) {
            if (!paused) stateTime += Gdx.graphics.getDeltaTime();
            TextureRegion frame = anim.getKeyFrame(stateTime);
            float width = frame.getRegionWidth();
            float height = frame.getRegionHeight();
            batch.draw(frame, -width / 2f, -height / 2f, width, height);
        }

        batch.end();
    }

    private Animation<TextureRegion> build(AtlasAnimation animation) {
        if (animation.atlas.isEmpty() || animation.regions.isEmpty()) return null;

        TextureAtlas textureAtlas = atlas(animation.atlas);
        Array<TextureRegion> frames = new Array<>();
        for (String region : animation.regions) {
            TextureRegion found = textureAtlas.findRegion(region);
            if (found != null) frames.add(found);
        }
        if (frames.size == 0) return null;

        return new Animation<>(animation.frameDuration, frames,
                animation.loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    }

    public void dispose() {
        batch.dispose();
        atlases.values().forEach(TextureAtlas::dispose);
    }
}
