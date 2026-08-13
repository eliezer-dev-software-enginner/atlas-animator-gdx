package eu.dev.editor.codegen;

import eu.dev.editor.scene.SceneObject;

/** Produces a starter Java class for a scene object, meant to be copied into the game project. */
public class ClassCodeGenerator {

    public static String generate(SceneObject object) {
        String className = className(object.id);
        return "package eu.dev;\n\n" +
                "import com.badlogic.gdx.graphics.Texture;\n" +
                "import com.badlogic.gdx.graphics.g2d.Sprite;\n" +
                "import com.badlogic.gdx.graphics.g2d.SpriteBatch;\n\n" +
                "public class " + className + " {\n" +
                "    final Sprite sprite;\n\n" +
                "    public " + className + "(Texture texture, float x, float y, float width, float height) {\n" +
                "        sprite = new Sprite(texture);\n" +
                "        sprite.setBounds(x, y, width, height);\n" +
                "    }\n\n" +
                "    public void render(SpriteBatch batch) {\n" +
                "        sprite.draw(batch);\n" +
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
