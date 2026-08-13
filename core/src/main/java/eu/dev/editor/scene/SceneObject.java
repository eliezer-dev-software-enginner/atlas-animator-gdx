package eu.dev.editor.scene;

public class SceneObject {
    public String id = "";
    public String type = "sprite";
    public String texture = "";
    public float x, y;
    public float width = 32f, height = 32f;
    public boolean visible = true;

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
