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

1. "Select atlas..." — pick a `.atlas`. The file and the page image(s) it
   references get copied into `assets/atlases/`.
2. Pick regions (in whatever order you want them to play) via the combo +
   "Add frame". Remove one with the button next to it in the list.
3. Adjust frame duration and looping. Pause/Play controls the preview.
4. "Generate snippet" + "Copy" (shows "Copied!" briefly) — paste the code
   into your project.

## Advantages

- **Real playback, not a guess** — the animation actually runs in the
  preview (the same LibGDX `Animation`/`TextureRegion` pipeline your game
  uses), so a wrong frame order or an off-feeling duration shows up before
  it's in your codebase.
- **No boilerplate to type by hand** — pick the atlas and regions through
  the UI, get the `TextureAtlas`/`Animation` construction code generated
  for you. No guessing region-name spelling.
- **Doesn't touch your project** — reads your `.atlas`, writes only into
  its own `assets/atlases/` copy, and the generated snippet is plain code
  you paste yourself. No hidden writes into your game's source tree.
- **Nothing extra to install to build it** — `jpackage` (bundled in the
  JDK) for distributable installers, `imgui-java` for the UI, both already
  wired into the project.
- **Small and focused** — one job (atlas animation preview + code
  snippet), not a general-purpose scene/level editor.

## FAQ

**Does this run my game's logic?**
No. It only previews the animation and generates code — your game owns all
the real logic.

**Can I use loose image files instead of an atlas?**
Not right now — only `TextureAtlas` (`.atlas` + its page image) is
supported.

**Can I reorder frames after adding them?**
Not directly yet — remove the ones after the point you want to move and
re-add them in the order you want.

**Where does it remember my last atlas?**
`~/.prefs/atlas-animator-gdx` (via LibGDX's own `Preferences`), local to
your machine.

**Does it modify my original `.atlas`/image files?**
No — it copies them into this project's own `assets/atlases/` folder and
only reads from there afterward.

**Does it work on Windows/macOS?**
Built and tested on Linux. Windows should work (LWJGL3/LibGDX support it)
but hasn't been verified on this machine; macOS is untested too.

## Support

If this tool saved you some time, you can
[buy me a coffee](https://buymeacoffee.com/eliezerdev).
