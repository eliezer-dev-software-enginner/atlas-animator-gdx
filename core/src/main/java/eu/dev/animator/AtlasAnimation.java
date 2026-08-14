package eu.dev.animator;

import java.util.ArrayList;
import java.util.List;

/** The one thing this tool edits: an ordered sequence of atlas regions played back as an animation. */
public class AtlasAnimation {
    public String atlas = "";
    public List<String> regions = new ArrayList<>();
    public float frameDuration = 0.1f;
    public boolean loop = true;
}
