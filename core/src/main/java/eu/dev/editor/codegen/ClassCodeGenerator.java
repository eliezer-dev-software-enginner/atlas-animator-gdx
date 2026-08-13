package eu.dev.editor.codegen;

import eu.dev.editor.scene.SceneObject;

/** Produces a starter Java class for a scene object, meant to be copied into the game project. */
public class ClassCodeGenerator {

    public static String generate(SceneObject object) {
        String className = className(object.id);
        boolean animated = !object.atlas.isEmpty() && !object.animationRegions.isEmpty();
        return animated ? generateAnimated(className) : generateStatic(className);
    }

    private static String generateStatic(String className) {
        return "package eu.dev;\n\n" +
                "import com.badlogic.gdx.graphics.Texture;\n" +
                "import com.badlogic.gdx.graphics.g2d.Sprite;\n" +
                "import com.badlogic.gdx.graphics.g2d.SpriteBatch;\n\n" +
                "public class " + className + " {\n" +
                "    final Sprite sprite;\n" +
                "    boolean visible;\n\n" +
                "    public " + className + "(Texture texture, float x, float y, float width, float height, boolean visible) {\n" +
                "        sprite = new Sprite(texture);\n" +
                "        sprite.setBounds(x, y, width, height);\n" +
                "        this.visible = visible;\n" +
                "    }\n\n" +
                "    public void update(float delta) {\n" +
                "        // TODO: lógica do objeto (movimento, etc.)\n" +
                "    }\n\n" +
                "    public void render(SpriteBatch batch) {\n" +
                "        if (visible) sprite.draw(batch);\n" +
                "    }\n" +
                "}\n";
    }

    private static String generateAnimated(String className) {
        return "package eu.dev;\n\n" +
                "import com.badlogic.gdx.graphics.g2d.Animation;\n" +
                "import com.badlogic.gdx.graphics.g2d.SpriteBatch;\n" +
                "import com.badlogic.gdx.graphics.g2d.TextureRegion;\n\n" +
                "public class " + className + " {\n" +
                "    final Animation<TextureRegion> animation;\n" +
                "    final float x, y, width, height;\n" +
                "    boolean visible;\n" +
                "    private float stateTime;\n\n" +
                "    public " + className + "(Animation<TextureRegion> animation, float x, float y, float width, float height, boolean visible) {\n" +
                "        this.animation = animation;\n" +
                "        this.x = x;\n" +
                "        this.y = y;\n" +
                "        this.width = width;\n" +
                "        this.height = height;\n" +
                "        this.visible = visible;\n" +
                "    }\n\n" +
                "    public void update(float delta) {\n" +
                "        stateTime += delta;\n" +
                "        // TODO: lógica adicional do objeto\n" +
                "    }\n\n" +
                "    public void render(SpriteBatch batch) {\n" +
                "        if (visible) batch.draw(animation.getKeyFrame(stateTime), x, y, width, height);\n" +
                "    }\n" +
                "}\n";
    }

    private static String className(String id) {
        StringBuilder name = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : id.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                name.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            } else {
                capitalizeNext = true;
            }
        }
        if (name.length() == 0 || Character.isDigit(name.charAt(0))) {
            name.insert(0, "Scene");
        }
        return name.toString();
    }
}
