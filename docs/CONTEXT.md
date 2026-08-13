# Contexto do Projeto

## O que é
Editor de cena visual para jogos LibGDX (não roda lógica de jogo — só posiciona
sprites e exporta um JSON de cena que o jogo real (`final-roz-game-new-java`)
vai importar via um parser próprio, fora deste repositório).

Este repositório nasceu como uma cópia do projeto do jogo (mesmo histórico até
o commit `b506e98`) e foi convertido para o editor. O `.git` original foi
removido — é um projeto novo e independente agora.

## Estrutura
- Gradle multi-módulo: `core` (lógica do editor) + `lwjgl3` (launcher desktop).
  Módulo `android` foi removido do build (ferramenta é desktop-only), a pasta
  `android/` continua no disco mas não é mais compilada.
- `core/src/main/java/eu/dev/editor/`
  - `EditorApplication.java` — ApplicationAdapter principal, liga o ciclo de
    vida do ImGui (GLFW + GL3) ao loop do LibGDX.
  - `scene/` — `SceneObject`, `Scene`, `SceneJsonExporter`, `SceneJsonImporter`
    (usa `com.badlogic.gdx.utils.Json`), `AnchorResolver` (posicionamento
    relativo — ver seção Anchors abaixo).
  - `viewport/SceneViewport.java` — câmera orto, SpriteBatch, zoom (scroll),
    pan (botão do meio), seleção/drag (botão esquerdo). Usa `ImGui.getIO().getWantCaptureMouse()`
    para não interagir com a cena quando o clique é sobre um painel ImGui.
    Objetos com `anchorOf` preenchido não são arrastáveis (posição vem do
    `AnchorResolver`, arrastar seria sobrescrito no frame seguinte). Desenha
    um retângulo (`ShapeRenderer`) nos bounds da cena (`0,0`..`sceneWidth,sceneHeight`)
    como referência visual pros anchors de cena.
  - `ui/` — `EditorUI` (menu + wiring), `HierarchyPanel` (nome/tamanho da cena,
    lista/seleção/remoção de objetos), `InspectorPanel` (campos id/x/y/width/height,
    checkbox `visible`, combo de anchor, geração de classe), `WindowBounds`
    (mantém a janela atual do ImGui dentro da área visível — chamado logo
    após todo `ImGui.begin()` da Hierarchy/Inspector).
  - `codegen/ClassCodeGenerator.java` — gera um snippet de classe Java
    (`Sprite` + construtor + `update`/`render`, respeitando `visible`) a
    partir de um `SceneObject`.
- Tilemap (Tiled/`.tmx`) via `com.badlogic.gdx.maps.tiled` — já é parte do gdx
  core, não precisou de dependência nova.
- UI usa `imgui-java` (io.github.spair, v1.92.7.1) — painéis flutuantes sobre a
  cena renderizada em tela cheia (sem docking, sem Scene2D na chrome).
- Diálogos de arquivo (importar sprite, load/export JSON) usam `javax.swing.JFileChooser`
  — sem dependência extra. Cada diálogo roda na sua própria thread (não na
  thread de render/GLFW, que travaria com o dialog aberto); o resultado que
  toca GL (carregar textura, trocar a cena) volta pra render thread via
  `Gdx.app.postRunnable`.
- "Add Sprite" copia a imagem escolhida para `<cwd>/sprites/` (confirmado que é
  `assets/sprites/` quando rodado via `./gradlew lwjgl3:run` — o `workingDir`
  configurado em `lwjgl3/build.gradle` aponta pra lá).
- "Export" abre um save dialog nativo — o usuário navega até a pasta `assets`
  do projeto do jogo manualmente.
- `AppStorage` (usa `com.badlogic.gdx.Preferences`) persiste entre execuções o
  último caminho usado em Add Sprite, Load e Export (cada `JFileChooser` abre
  já apontando pro último local usado) e o caminho da última cena carregada ou
  exportada — `EditorApplication.create()` carrega essa cena automaticamente
  na abertura, se o arquivo ainda existir.
- Layout dos painéis ImGui (posição, tamanho, colapsado/expandido) persiste em
  `assets/editor-layout.ini` — arquivo padrão do próprio Dear ImGui
  (`io.setIniFilename(...)`), salvo automaticamente e recarregado na próxima
  abertura. Gitignored (é estado local da máquina, não conteúdo do projeto).

## Anchors (posicionamento relativo)
`SceneObject` tem `anchorOf`, `anchorAlignX`/`anchorAlignY` (`left/center/right`,
`bottom/center/top`) e `anchorOffsetX`/`anchorOffsetY`. `anchorOf` pode ser:
- vazio → posição absoluta (x/y editados direto, como sempre foi)
- id de outro objeto da cena → ancorado a esse objeto
- `AnchorResolver.SCENE_ANCHOR` (sentinel `"__scene__"`) → ancorado aos bounds
  da própria cena (`0,0`..`sceneWidth,sceneHeight`), pra posicionamento que
  faz sentido independente do dispositivo (ex: "canto superior direito da
  tela") em vez de sempre depender de outro objeto existir

Editável só pelo combo "Anchor" do Inspector (lista "(nenhum)", "(cena)" e os
outros objetos da cena). `Scene.sceneWidth`/`sceneHeight` (default 640x360,
mesma convenção do `FitViewport` já usado nos dois projetos de jogo desta
suite) são editáveis no topo do Hierarchy.

`AnchorResolver.resolve(scene)` roda todo frame (`EditorApplication.render()`,
antes do viewport) e sobrescreve `x`/`y` do objeto ancorado a partir dos bounds
já resolvidos do objeto-base (ou da cena) + alinhamento + offset. Referência
quebrada (objeto deletado/renomeado), auto-anchor e ciclo (A→B→A) todos
degradam pra "mantém a última posição conhecida" em vez de travar ou entrar
em loop.

Resolvido só no editor — ver decisão em `DECISIONS.md`. O JSON exportado
carrega tanto o `x`/`y` já resolvido (o jogo lê isso e ignora o resto) quanto
os campos de anchor (pra continuar editável se você reabrir a cena no editor).

## Animações (atlas)
`SceneObject` tem `atlas` (caminho pro `.atlas`), `animationRegions`
(`List<String>`, nomes de região na ordem de reprodução), `animationFrameDuration`
e `animationLoop`. `atlas` + `animationRegions` não-vazios = objeto animado;
senão continua estático (`texture`, como sempre foi).

- Inspector: seção "Animação (atlas)" no objeto selecionado. "Selecionar
  atlas..." abre um `.atlas` (mesmo padrão de thread/`postRunnable` dos outros
  diálogos); copia o `.atlas` **e** a(s) imagem(ns) de página que ele
  referencia (lidas via `TextureAtlas.TextureAtlasData`, não só o nome do
  arquivo) pra `assets/sprites/atlases/`. Combo lista os nomes de região
  disponíveis no atlas (`TextureAtlas.getRegions()`); "Adicionar frame" anexa
  o nome escolhido em `animationRegions`; cada frame já adicionado tem botão
  de remover. Duração do frame e loop são campos separados.
- `SceneViewport` toca a animação de verdade (`Animation<TextureRegion>`
  construído a partir das regiões do atlas, `Gdx.graphics.getDeltaTime()`
  acumulado por objeto) — WYSIWYG real, não só um frame parado. Botão
  "Pause"/"Play" na barra de menu (`SceneViewport.togglePause()`) para tudo
  ao mesmo tempo; enquanto pausado o `stateTime` simplesmente não avança.
- `AppStorage.lastAtlasPath` lembra o último `.atlas` escolhido, mesmo padrão
  dos outros diálogos.

## Tilemap (Tiled)
`SceneObject.tmx` (caminho pro `.tmx`) + `type = "tilemap"`. Diferente de
todo resto no editor, um tilemap **sempre desenha na origem da cena (0,0)** —
`x`/`y` do objeto não viram offset de renderização. Motivo: `TiledMapRenderer`
não tem um jeito simples de deslocar onde o mapa desenha sem transladar a
própria matriz de projeção separadamente do resto do viewport, e na prática
uma cena tem no máximo um tilemap, que já nasce como "o chão" — não vale a
complexidade de suportar arrastar isso por enquanto (documentado, não é uma
omissão silenciosa).

- "Add Tilemap..." (menu Scene) abre um `.tmx` — **não copia** o arquivo pra
  lugar nenhum (diferente de Add Sprite/Add Atlas). Um `.tmx` pode referenciar
  outros tilesets externos (`.tsx`, cada um com sua própria imagem) e
  tilesets inline com caminho relativo que podem apontar pra fora de
  `assets/maps/` inteiramente (o `mundo1.tmx` de teste faz isso:
  `../doors/porta_tramela_azul.png`). Copiar esse grafo de dependências
  mantendo toda referência relativa intacta é bem mais arriscado que só
  exigir que o arquivo já esteja em `assets/` — que é como o mapa de teste já
  chegou lá mesmo. O diálogo abre direto em `assets/maps/` por padrão.
- Ao importar, o editor carrega o mapa uma vez (`TmxMapLoader`) só pra ler
  `width`/`height`/`tilewidth`/`tileheight` das propriedades e calcular o
  tamanho em pixels do objeto (usado só informativamente no Inspector).
- `SceneViewport` desenha o tilemap (`OrthogonalTiledMapRenderer`, batch
  compartilhado com os sprites) **antes** de tudo, no próprio ciclo
  begin/end — não dá pra aninhar dentro do `batch.begin()` dos sprites porque
  o renderer do tilemap gerencia o batch por conta própria internamente.
- Seleção: tilemap é checado numa passada separada, sempre por último,
  independente da ordem na lista — um tilemap cobre a cena inteira, então sem
  isso ele "roubaria" cliques de objetos menores desenhados por cima dele
  dependendo da ordem em que foram criados.
- Inspector: view dedicada e bem mais simples pra `type == "tilemap"` — só
  id, caminho do `.tmx`, tamanho calculado e visibilidade. Sem posição, sem
  anchor, sem animação, sem geração de classe (nenhum desses conceitos se
  aplica a uma camada de fundo).
- **Verificado com o mapa real** (`mundo1.tmx`, 50x30 tiles, tileset externo
  `Tiles64.tsx` + tilesets inline com caminho relativo pra fora de
  `assets/maps/`) via diagnóstico temporário rodado dentro do editor de
  verdade (não só um parser isolado) — carregou sem exceção, junto com
  player/bullet/animação simultaneamente no lado do jogo. Removido antes do
  commit.

## Geração de classe (boilerplate)
Botão "Gerar classe" no Inspector chama `ClassCodeGenerator.generate(object)`,
mostra o resultado num campo de texto somente-leitura e tem um botão "Copiar"
(`ImGui.setClipboardText`). O usuário cola manualmente no projeto do jogo — o
editor nunca escreve arquivo `.java` em outro projeto (ver decisão).

Gera duas formas diferentes dependendo do objeto: `Sprite`-based (estático,
como sempre) ou `Animation<TextureRegion>`-based (quando o objeto tem atlas +
`animationRegions`) — o construtor já recebe a `Animation` pronta, montada por
quem instancia a classe (ver seção do jogo abaixo).

## Do lado do jogo (`libgdx-example-game`)
Esse projeto evoluiu bastante durante o desenvolvimento do editor — não é só
um alvo de teste estático:
- `eu.dev.Main` → `eu.dev.screens.MenuScreen` (botão "start" carregado via
  `scene2d-hud-loader`) → `eu.dev.screens.Gamescreen` (renomeado de
  `GameScreen`).
- `eu.dev.objects.Player`/`Bullet` — classes extraídas pro formato que
  `ClassCodeGenerator` produz (construtor recebe textura/posição prontos,
  `update()`/`render(SpriteBatch)`), já com um objeto `bullet` ancorado no
  `player` (`anchorOf: "player"`) no `assets/scenes/level_01.json` real.
- `Gamescreen` mantém tanto o desenho genérico (qualquer objeto da cena que
  não seja `player`/`bullet` — incluindo, agora, objetos animados por atlas e
  tilemaps) quanto os objetos especiais construídos à mão.
  `eu.dev.scene.SceneObject` nesse projeto precisou ganhar os mesmos campos
  de atlas/animação/tilemap do editor — dessa vez não dá pra só ignorar campo
  desconhecido (`ignoreUnknownFields`), porque o jogo precisa *usar* esse
  dado, não só tolerar sua presença.

## Como rodar
```
./gradlew lwjgl3:run
```
