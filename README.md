# Atlas Animator GDX

Small, focused desktop tool: loads a `TextureAtlas` (LibGDX), builds an
animation sequence from the atlas' regions, shows the animation actually
playing (not a frozen frame), and generates the Java code snippet to paste
into your game.

Started out as a bigger scene editor (positioning sprites, anchors,
tilemap...) that got abandoned — only the atlas animation preview part
survived, because that was the part worth keeping.

## Usage

```
./gradlew lwjgl3:run
```

1. "Selecionar atlas..." — pick a `.atlas`. The file and the page image(s)
   it references get copied into `assets/atlases/`.
2. Pick regions (in whatever order you want them to play) via the combo +
   "Adicionar frame". Remove one with the button next to it in the list.
3. Adjust frame duration and looping. Pausar/Reproduzir controls the preview.
4. "Gerar snippet" + "Copiar" (shows "Copiado!" briefly) — paste the code
   into your project.
