package eu.dev.editor.scene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bakes anchored objects' x/y from their base object's bounds + alignment + offset.
 * Runs every editor frame so dragging a base object keeps its anchored dependents in sync.
 * Dangling references, self-anchors, and cycles all degrade gracefully: the object just
 * keeps whatever x/y it last had instead of resolving further.
 */
public class AnchorResolver {
    private final Set<SceneObject> resolved = new HashSet<>();

    public void resolve(Scene scene) {
        resolved.clear();
        Map<String, SceneObject> byId = new HashMap<>();
        for (SceneObject object : scene.objects) {
            byId.put(object.id, object);
        }
        for (SceneObject object : scene.objects) {
            resolve(object, byId, new HashSet<>());
        }
    }

    private void resolve(SceneObject object, Map<String, SceneObject> byId, Set<SceneObject> visiting) {
        if (resolved.contains(object)) return;
        if (object.anchorOf.isEmpty()) {
            resolved.add(object);
            return;
        }

        SceneObject base = byId.get(object.anchorOf);
        if (base == null || base == object || visiting.contains(base)) {
            resolved.add(object);
            return;
        }

        visiting.add(object);
        resolve(base, byId, visiting);
        visiting.remove(object);

        object.x = alignedX(base, object) + object.anchorOffsetX;
        object.y = alignedY(base, object) + object.anchorOffsetY;
        resolved.add(object);
    }

    private static float alignedX(SceneObject base, SceneObject object) {
        switch (object.anchorAlignX) {
            case "left": return base.x;
            case "right": return base.x + base.width - object.width;
            default: return base.x + base.width / 2f - object.width / 2f; // center
        }
    }

    private static float alignedY(SceneObject base, SceneObject object) {
        switch (object.anchorAlignY) {
            case "bottom": return base.y;
            case "top": return base.y + base.height;
            default: return base.y + base.height / 2f - object.height / 2f; // center
        }
    }
}
