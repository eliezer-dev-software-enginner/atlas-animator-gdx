package eu.dev.editor.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SceneJsonImporter {

    public static Scene load(FileHandle file) {
        Json json = new Json();
        return json.fromJson(Scene.class, file);
    }
}
