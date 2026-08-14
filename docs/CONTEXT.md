# Contexto do Projeto

## O que é
Preview + gerador de snippet de animação por `TextureAtlas` (LibGDX). Não é
mais um editor de cena — isso foi abandonado, só essa funcionalidade ficou
(ver `DECISIONS.md` pro histórico de como chegou aqui e o quê foi removido).

## Estrutura
- Gradle multi-módulo: `core` (`eu.dev.animator`) + `lwjgl3` (launcher
  desktop). Sem módulo `android`.
- `core/src/main/java/eu/dev/animator/`
  - `AnimatorApplication.java` — bootstrap: ciclo de vida do ImGui (GLFW +
    GL3), dono da única `AtlasAnimation` em edição, e o diálogo de arquivo
    ("Selecionar atlas...", roda em thread própria — ver seção abaixo).
  - `AtlasAnimation.java` — o único modelo de dados: `atlas` (caminho),
    `regions` (`List<String>`, ordem de reprodução), `frameDuration`, `loop`.
  - `AtlasAnimationSnippetGenerator.java` — gera só o código de construção da
    `Animation` (sem classe, sem `update()`/`render()`) a partir de uma
    `AtlasAnimation`.
  - `AppStorage.java` — lembra só o último `.atlas` escolhido
    (`com.badlogic.gdx.Preferences`).
  - `viewport/AnimationViewport.java` — câmera orto, zoom (scroll), pan
    (botão do meio), cache de `TextureAtlas`, toca a animação de verdade
    (`Animation<TextureRegion>` reconstruído a cada frame a partir das
    regiões, `stateTime` acumulado com `Gdx.graphics.getDeltaTime()`,
    Pause/Play). Desenha centralizado na origem — não tem cena, não tem
    posição, é só a prévia.
  - `ui/AnimatorPanel.java` — painel único: seleção de atlas, picker de
    região + lista ordenada com remoção, duração/loop, Pause/Play, geração
    de snippet + copiar. `ui/WindowBounds.java` mantém o painel dentro da
    área visível da janela (mesma lógica de antes).

## Fluxo "Selecionar atlas..."
Igual ao "Add Atlas" do projeto anterior: lê o `.atlas` escolhido via
`TextureAtlas.TextureAtlasData` pra descobrir a(s) imagem(ns) de página que
ele referencia, copia o `.atlas` **e** as imagens juntas pra
`assets/atlases/` (um `.atlas` sozinho sem a imagem do lado não carrega).
Diálogo roda em thread própria (`JFileChooser` bloqueia a thread que chama,
rodar na render thread trava o app inteiro); o resultado que toca GL volta
pra render thread via `Gdx.app.postRunnable`.

## Geração de snippet
Só o código de construção — sem classe, sem campos, sem `update()`/`render()`.
Assume que quem cola já tem contexto de LibGDX (`Gdx`, `Array`, etc.) no
arquivo. Formato:

```java
TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("atlases/bird.atlas"));
Array<TextureRegion> frames = new Array<>();
frames.add(atlas.findRegion("welly_asas_baixo_"));
frames.add(atlas.findRegion("welly_planando_transicao_"));
Animation<TextureRegion> animation = new Animation<>(0.2f, frames, Animation.PlayMode.LOOP);
```

## Como rodar
```
./gradlew lwjgl3:run
```
