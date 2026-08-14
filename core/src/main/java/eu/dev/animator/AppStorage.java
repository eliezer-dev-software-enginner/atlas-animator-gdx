package eu.dev.animator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Persists the last atlas file picked, so the file dialog reopens where you left off. */
public class AppStorage {
    private static final String PREFS_NAME = "atlas-animator-gdx";
    private static final String KEY_LAST_ATLAS_PATH = "lastAtlasPath";

    private final Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

    public String getLastAtlasPath() {
        return prefs.getString(KEY_LAST_ATLAS_PATH, "");
    }

    public void setLastAtlasPath(String path) {
        prefs.putString(KEY_LAST_ATLAS_PATH, path).flush();
    }
}
