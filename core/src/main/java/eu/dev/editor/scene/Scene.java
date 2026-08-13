package eu.dev.editor.scene;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    public String sceneName = "level_01";

    // Canonical/virtual size the scene is authored at (matches the game's FitViewport size).
    // Lets objects anchor to the scene bounds instead of only to raw x/y - a fixed pixel
    // offset only means the same thing across devices if it's relative to a known size.
    public float sceneWidth = 640f;
    public float sceneHeight = 360f;

    public List<SceneObject> objects = new ArrayList<>();
}
