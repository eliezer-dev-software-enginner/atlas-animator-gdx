package eu.dev.editor.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SceneJsonImporter {

    public static Scene load(FileHandle file) {
        Json json = new Json();
        // Don't crash loading a scene that has fields this build's SceneObject doesn't know
        // about yet (e.g. a JSON hand-written before some field existed, or from a newer editor).
        json.setIgnoreUnknownFields(true);
        return json.fromJson(Scene.class, file);
    }
}
