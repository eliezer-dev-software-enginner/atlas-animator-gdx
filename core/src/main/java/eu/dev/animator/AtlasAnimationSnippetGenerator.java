package eu.dev.animator;

/** Bare code snippet (no wrapping class) for constructing the current AtlasAnimation in LibGDX. */
public class AtlasAnimationSnippetGenerator {

    public static String generate(AtlasAnimation animation) {
        StringBuilder code = new StringBuilder();
        code.append("TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(\"")
                .append(animation.atlas).append("\"));\n");
        code.append("Array<TextureRegion> frames = new Array<>();\n");
        for (String region : animation.regions) {
            code.append("frames.add(atlas.findRegion(\"").append(region).append("\"));\n");
        }
        code.append("Animation<TextureRegion> animation = new Animation<>(")
                .append(animation.frameDuration).append("f, frames, Animation.PlayMode.")
                .append(animation.loop ? "LOOP" : "NORMAL").append(");\n");
        return code.toString();
    }
}
