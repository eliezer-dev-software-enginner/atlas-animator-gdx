package eu.dev.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Persists small editor preferences (last file dialog paths) between runs. */
public class AppStorage {
    private static final String PREFS_NAME = "scene-editor";
    private static final String KEY_LAST_SPRITE_PATH = "lastSpritePath";
    private static final String KEY_LAST_LOAD_PATH = "lastLoadPath";
    private static final String KEY_LAST_EXPORT_PATH = "lastExportPath";
    private static final String KEY_LAST_SCENE_PATH = "lastScenePath";
    private static final String KEY_LAST_ATLAS_PATH = "lastAtlasPath";
    private static final String KEY_LAST_TILEMAP_PATH = "lastTilemapPath";

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

    /** File the editor should reopen automatically on the next launch. */
    public String getLastScenePath() {
        return prefs.getString(KEY_LAST_SCENE_PATH, "");
    }

    public void setLastScenePath(String path) {
        prefs.putString(KEY_LAST_SCENE_PATH, path).flush();
    }

    public String getLastAtlasPath() {
        return prefs.getString(KEY_LAST_ATLAS_PATH, "");
    }

    public void setLastAtlasPath(String path) {
        prefs.putString(KEY_LAST_ATLAS_PATH, path).flush();
    }

    public String getLastTilemapPath() {
        return prefs.getString(KEY_LAST_TILEMAP_PATH, "");
    }

    public void setLastTilemapPath(String path) {
        prefs.putString(KEY_LAST_TILEMAP_PATH, path).flush();
    }
}
