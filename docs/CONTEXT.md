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
    (usa `com.badlogic.gdx.utils.Json`).
  - `viewport/SceneViewport.java` — câmera orto, SpriteBatch, zoom (scroll),
    pan (botão do meio), seleção/drag (botão esquerdo). Usa `ImGui.getIO().getWantCaptureMouse()`
    para não interagir com a cena quando o clique é sobre um painel ImGui.
  - `ui/` — `EditorUI` (menu + wiring), `HierarchyPanel` (lista/seleção/remoção),
    `InspectorPanel` (campos id/x/y/width/height).
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

## Como rodar
```
./gradlew lwjgl3:run
```
