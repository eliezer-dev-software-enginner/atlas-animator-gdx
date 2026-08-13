package eu.dev.editor.scene;

import java.util.ArrayList;
import java.util.List;

public class SceneObject {
    public String id = "";
    public String type = "sprite";
    public String texture = "";
    public float x, y;
    public float width = 32f, height = 32f;
    public boolean visible = true;

    /**
     * Atlas-based animation. When atlas is non-empty and animationRegions isn't empty, this
     * object plays back the named regions (in order) instead of showing the static texture.
     */
    public String atlas = "";
    public List<String> animationRegions = new ArrayList<>();
    public float animationFrameDuration = 0.1f;
    public boolean animationLoop = true;

    /**
     * Tiled map background layer. Only meaningful when type is "tilemap". Always drawn at
     * world origin (x/y aren't used as a render offset) - a scene has at most one ground
     * layer in practice, and offsetting it correctly would need translating the tilemap
     * renderer's projection matrix separately from everything else in the viewport, which
     * isn't worth the complexity for a background that's always meant to start at 0,0.
     */
    public String tmx = "";

    /**
     * Authoring-time positioning aid: when non-empty, x/y are overwritten every editor frame
     * from the referenced object's bounds (id) + alignment + offset, instead of being edited
     * directly. Resolved and baked into x/y before export — the game only ever sees plain
     * absolute coordinates, this metadata round-trips through the JSON purely so the editor
     * can keep editing the relationship later.
     */
    public String anchorOf = "";
    public String anchorAlignX = "center"; // left | center | right
    public String anchorAlignY = "center"; // bottom | center | top
    public float anchorOffsetX = 0f;
    public float anchorOffsetY = 0f;
}
