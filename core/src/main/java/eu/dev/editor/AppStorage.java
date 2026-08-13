package eu.dev.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Persists small editor preferences (last file dialog paths) between runs. */
public class AppStorage {
    private static final String PREFS_NAME = "scene-editor";
    private static final String KEY_LAST_SPRITE_PATH = "lastSpritePath";
    private static final String KEY_LAST_LOAD_PATH = "lastLoadPath";
    private static final String KEY_LAST_EXPORT_PATH = "lastExportPath";

    private final Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

    public String getLastSpritePath() {
        return prefs.getString(KEY_LAST_SPRITE_PATH, "");
    }

    public void setLastSpritePath(String path) {
        prefs.putString(KEY_LAST_SPRITE_PATH, path).flush();
    }

    public String getLastLoadPath() {
        return prefs.getString(KEY_LAST_LOAD_PATH, "");
    }

    public void setLastLoadPath(String path) {
        prefs.putString(KEY_LAST_LOAD_PATH, path).flush();
    }

    public String getLastExportPath() {
        return prefs.getString(KEY_LAST_EXPORT_PATH, "");
    }

    public void setLastExportPath(String path) {
        prefs.putString(KEY_LAST_EXPORT_PATH, path).flush();
    }
}
