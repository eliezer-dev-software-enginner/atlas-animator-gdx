# Decisões Arquiteturais

## 2026-08-14 — Renomeado pra `atlas-animator-gdx`, empacotamento via jpackage (como o `hud-creator-gdx`)
Usuário pediu configuração de geração de aplicativo igual à de outro
projeto irmão da suite, `hud-creator-gdx`, e trocou o nome do app pra
`atlas-animator-gdx`.

**`construo` (GraalVM native-image) removido, trocado por `jpackage`
(`application` plugin puro).** O projeto tinha um setup de packaging bem
elaborado herdado do template `gdx-liftoff` original (`construo` +
GraalVM, URLs de JDK hardcoded pra baixar, tasks `jarMac`/`jarLinux`/`jarWin`,
`nativeimage.gradle`) que nunca chegou a ser usado de verdade nesta sessão
inteira. `hud-creator-gdx` usa uma abordagem bem mais simples: `jpackage`
(vem dentro do JDK desde a versão 14, zero dependência nova) rodado por
fora do Gradle, sobre o output de `application`'s `installDist`. Copiei
esse padrão em vez de manter os dois sistemas de packaging em paralelo —
`enableGraalNative`/`graalHelperVersion` também removidos de
`gradle.properties` (só existiam pro `construo`), junto com
`utilsBox2dVersion`/`utilsVersion` (não referenciados em lugar nenhum,
achados enquanto limpava) e `android.useAndroidX`/`android.enableR8.fullMode`
(módulo `android` já nem está no build desde muito antes do pivô pro
animador).

**`appName` centralizado em `gradle.properties`** (`build.gradle` lê
`"$appName"` em vez de ter o nome escrito direto) — mesmo padrão do
`hud-creator-gdx`, trocar o nome do app vira editar um arquivo só. Junto
vieram `appVendor` ("Eliezer Dev", mesmo valor do `hud-creator-gdx` — é o
mesmo autor/vendor pra todos os apps da suite) e `appMenuGroup`
("Games", idem).

**Verificado de verdade nesta máquina antes de escrever `BUILD.md`** (não
só copiado do `hud-creator-gdx` às cegas): rodei
`./gradlew :lwjgl3:installDist`, depois `jpackage --type app-image` e
**executei o binário empacotado** (rodou 8s sem erro, sem log de crash);
depois `jpackage --type deb` e conferi a estrutura com `dpkg -c` (instala
em `/opt/atlas-animator-gdx/`, tem o `.desktop`, o `runtime` embutido). No
processo descobri uma diferença real em relação ao guia do
`hud-creator-gdx`: como este projeto mantém
`application.applicationName = appName` (o `hud-creator-gdx` não define
isso), a pasta gerada por `installDist` chama
`lwjgl3/build/install/atlas-animator-gdx/`, não
`lwjgl3/build/install/lwjgl3/` como o guia original assumia — troquei por
um `lwjgl3/build/install/*/lib/...` (wildcard) nos comandos e no workflow,
que funciona não importa o nome da pasta.

**Não testado**: a parte Windows (`.exe` via jpackage + WiX) — nem aqui
(não tem máquina Windows) nem no runner do GitHub Actions ainda (nunca
rodou). Mesma ressalva que o `hud-creator-gdx` já tinha pro lado Windows.

**Ícone**: reaproveitado `lwjgl3/icons/logo.png`/`.ico`/`.icns`, que já
existiam no projeto (ícone genérico do template `gdx-liftoff`, o mesmo já
usado como ícone da janela via `Lwjgl3Launcher.setWindowIcon`) — não é um
ícone próprio do "Atlas Animator", só um placeholder funcional. Registrado
em `BUILD.md`, não escondido.

## 2026-08-14 — Correções depois do primeiro teste manual do usuário
Usuário testou de verdade (primeira vez desde o pivô) e reportou 3 coisas:

1. **Sprite aparecia no canto inferior esquerdo**, atrás/fora da área do
   painel. Causa: o preview desenha centralizado na origem do mundo
   (`-width/2,-height/2`), e a câmera ortográfica padrão do gdx centraliza
   a origem do mundo no canto inferior esquerdo da tela (câmera com
   `yDown=false` posicionada em `width/2,height/2` por padrão). Corrigido
   deslocando `camera.position.x` em `resize()` proporcionalmente à largura
   da janela, pra origem cair a ~72% da largura (área livre à direita do
   painel de ~380px) em vez de ~0%. Matemática conferida à mão (não
   visualmente — sem screenshot, por preferência do usuário): pra
   `PREVIEW_X_FRACTION=0.72` numa janela 960x540, `camera.position.x` fica
   em `-211.2`, o que coloca a origem em `screen_x=691` (72% de 960).

1b. **Correção do item 1**: depois de corrigir o X, o sprite apareceu embaixo
   à direita em vez de à direita centralizado verticalmente — só ajustei
   `camera.position.x`, deixei `camera.position.y` no padrão
   (`height/2`), que pelo mesmo motivo do item 1 põe a origem do mundo na
   borda INFERIOR da tela, não no centro vertical. Usuário pediu "lá em
   cima", não centralizado — adicionado `PREVIEW_Y_FRACTION=0.8` com a
   mesma fórmula do X (`camera.position.y = height*(0.5-0.8)`), colocando a
   origem a 80% da altura (de baixo pra cima), perto do topo com margem.

2. **Painel devia ficar fixo, não só voltar pra tela se arrastado.**
   `WindowBounds.keepOnScreen()` (clamp reativo, corrigia DEPOIS do
   usuário arrastar) foi trocado por `ImGuiWindowFlags.NoMove | NoResize`
   no `ImGui.begin()`, mais `ImGuiCond.Always` (em vez de `FirstUseEver`)
   no `setNextWindowPos`/`setNextWindowSize` — força a posição/tamanho
   todo frame, então nem uma posição antiga salva no `.ini` de uma versão
   anterior consegue mover o painel. `WindowBounds.java` removido (ficou
   sem nenhum uso).

3. **Conferido que `lastAtlasPath` persiste**: usuário selecionou o atlas de
   verdade pela primeira vez, e `~/.prefs/gdx-atlas-animator` já tinha o
   caminho salvo (`.../libgdx-example-game/assets/sprites/atlases/bird.atlas`)
   — confirmado direto no arquivo, não só por leitura de código. Sem bug
   aqui, só verificação.

## 2026-08-14 — Pivô: só o preview de animação por atlas sobrevive
Usuário desistiu do projeto de editor de cena inteiro. Da lista de
funcionalidades construídas, só a de animação por `TextureAtlas` valia a pena
manter. Pedido: remover tudo que for inútil, renomear a aplicação pra algo
envolvendo "gdx atlas animator" (em inglês), e trocar geração de classe por
geração de snippet puro (sem classe).

**Removido** (arquivos deletados, não só desabilitados): `Scene`/`SceneObject`
(o conceito de cena inteiro), `SceneJsonExporter`/`SceneJsonImporter`
(sem mais export/import de cena), `AnchorResolver` (sem mais posicionamento
relativo — não tem mais "objetos" pra posicionar), `HierarchyPanel`
(sem mais lista de objetos), `EditorUI` (menu com Add Sprite/Add
Tilemap/Load/Export — nenhum faz sentido mais), suporte a tilemap (`.tmx`) e
a sprites estáticos, o toggle `visible`, `ClassCodeGenerator` (virou
`AtlasAnimationSnippetGenerator`, sem classe).

**Mantido e adaptado**: a lógica de reprodução de animação por atlas
(`SceneViewport` → `AnimationViewport`, bem mais simples: sem picking, sem
drag, sem sprites/tilemap, só toca a única animação atual centralizada na
origem), o fluxo de importar atlas (copia `.atlas` + imagem(ns) de página,
mesma lógica de `TextureAtlas.TextureAtlasData`), `WindowBounds` (painel
preso na tela), e o padrão de diálogo em thread própria +
`Gdx.app.postRunnable`.

**Renomeado**: pacote `eu.dev.editor` → `eu.dev.animator`;
`EditorApplication` → `AnimatorApplication`; `SceneViewport` →
`AnimationViewport`; `InspectorPanel`+`HierarchyPanel`+`EditorUI` → um único
`AnimatorPanel` (só existe uma coisa sendo editada agora, não faz sentido
dividir em painéis separados nem ter uma barra de menu com várias ações —
sobrou só "Selecionar atlas..." e Pausar/Reproduzir, ambos cabem direto no painel).
`appName` no Gradle e o título da janela viraram "gdx-atlas-animator"/"GDX
Atlas Animator". `editor-layout.ini` → `animator-layout.ini`.

**Modelo de dados**: `SceneObject` (14 campos: id, type, texture, x, y,
width, height, visible, atlas, animationRegions, animationFrameDuration,
animationLoop, anchorOf, anchorAlignX...) virou `AtlasAnimation` (4 campos:
atlas, regions, frameDuration, loop). Sem id/posição/tamanho/visibilidade —
nenhum desses conceitos existe mais fora do contexto de uma cena.

**Assets**: `assets/sprites/atlases/` → `assets/atlases/` (sem mais pasta
`sprites/` já que não importa sprite estático). Todo o resto em `assets/`
(sobras do jogo original: doors, enemies, maps/tilemap, ui, sounds, música,
sprites soltos, arquivos de projeto do Tiled) foi deletado — nada disso é
usado por este projeto, e ainda existe nos projetos de jogo se for
necessário de novo. `docs/PROMPT.md` também removido (já estava marcado como
"desconsiderar" em `RULES.md`, e descrevia um escopo que não existe mais).

**Dependências**: `gdx-box2d`/`gdx-freetype` removidas do `core`/`lwjgl3`
(nunca foram usadas nem no editor de cena nem aqui — sobra do template
original do jogo).

Histórico anterior a este ponto (entradas abaixo) documenta decisões do
editor de cena que não existe mais — mantido como registro, não como
descrição do estado atual (isso é o que `CONTEXT.md` faz).

## 2026-08-13 — Editor construído sobre a cópia do jogo, não do zero
O repositório já era uma cópia de `final-roz-game-new-java` com as telas do
jogo (`GameScreen`, `Player`, etc.) staged para remoção numa sessão anterior.
Continuamos esse trabalho em vez de criar um projeto novo, já que a estrutura
Gradle (core/lwjgl3/android, gdx 1.14.0) e os assets já estavam prontos.

## 2026-08-13 — `.git` removido, projeto desacoplado do histórico do jogo
A pedido do usuário. O editor é um projeto separado do jogo (não deve carregar
o histórico de commits de gameplay). Vai precisar de `git init` + primeiro
commit quando o usuário quiser começar a versionar.

## 2026-08-13 — Módulo `android` excluído do build, não deletado
O MVP é uma ferramenta desktop. Removido de `settings.gradle` (e da referência
em `build.gradle`) em vez de apagar a pasta `android/` — reversível, sem perda
de código caso um launcher mobile seja necessário depois.

## 2026-08-13 — `core` depende diretamente de `gdx-backend-lwjgl3`
Quebra a separação usual core/platform do LibGDX, mas como não existe (nem vai
existir tão cedo) outro backend além do desktop, foi a forma mais simples de
`EditorApplication` (que mora em `core`, junto com `viewport`/`ui`/`scene`)
alcançar o handle da janela GLFW que o ImGui precisa para inicializar.

## 2026-08-13 — ImGui em vez de Scene2D para a chrome do editor
Pedido explícito do usuário — permite widgets como inputs numéricos/lista
prontos, sem construir a UI do editor em cima de Scene2D. Scene2D fica
reservado para o viewport da cena, mas nem esse chegou a ser necessário: a
cena é desenhada direto com `SpriteBatch`/`OrthographicCamera`.

## 2026-08-13 — Painéis ImGui flutuantes sobre a cena em tela cheia
Em vez de dividir a janela em áreas fixas (um layout tipo dock), a cena ocupa
a janela inteira e os painéis do ImGui flutuam por cima. Evita ter que calcular
o retângulo do viewport da cena subtraindo a área ocupada pelos painéis —
`ImGui.getIO().getWantCaptureMouse()` já resolve quando um clique deve ir para
a cena ou para a UI.

## 2026-08-13 — `JFileChooser` (Swing) para diálogos de arquivo
Sem dependência nova: já é parte do JDK. Usado para importar sprite, e para
load/export de JSON (inclusive é como o usuário aponta manualmente para a
pasta `assets` do jogo ao exportar).

## 2026-08-13 — `AppStorage` sobre `com.badlogic.gdx.Preferences`
Persiste o último caminho usado em Add Sprite / Load / Export entre execuções,
pra cada `JFileChooser` abrir de novo no mesmo lugar. Usa a API de
`Preferences` do próprio gdx (backend desktop grava um arquivo local) em vez
de escrever um arquivo de config próprio — evita depender de outra lib e
segue o mesmo princípio já usado pro JSON (usar o utilitário do gdx antes de
inventar um novo).

## 2026-08-13 — Diálogos de arquivo movidos pra thread própria
`JFileChooser.showXDialog()` bloqueia a thread que o chama até fechar. Rodando
na render thread (GLFW), o processo inteiro travava com o dialog aberto — o SO
chega a reportar "não está respondendo". Cada dialog agora roda numa `Thread`
própria; qualquer resultado que precise de GL (carregar `Texture`, trocar a
`Scene` em uso) volta pra render thread via `Gdx.app.postRunnable`, porque
chamadas GL só são válidas na thread dona do contexto.

## 2026-08-13 — Layout dos painéis via `.ini` nativo do ImGui, não código próprio
Pedido do usuário: lembrar posição/tamanho/colapsado dos painéis entre
execuções. Dear ImGui já resolve isso sozinho salvando um `.ini` — bastou
trocar `io.setIniFilename(null)` (desabilitado antes pra não gerar arquivo à
toa) por um path real. `ImGuiCond.FirstUseEver` nas chamadas de
`setNextWindowPos/Size` (já existentes) garante que o layout default só se
aplica quando ainda não há nada salvo.

## 2026-08-13 — "Última cena" = último caminho usado em Load/Export
`AppStorage.lastScenePath` é atualizado tanto no Load quanto no Export (o
arquivo mais recentemente usado, seja lendo ou escrevendo, vira "a cena
atual"). `EditorApplication.create()` tenta carregar esse arquivo automaticamente;
se não existir mais ou o JSON estiver corrompido, cai pra uma `Scene` vazia em
vez de travar a abertura do editor.

## 2026-08-13 — Geração de boilerplate: snippet copiável, não escrita direta no jogo
Usuário deu 3 opções (gerar classe automaticamente + editar à mão / editor de
código dentro do editor / gerar código visual pra copiar) e pediu recomendação.
Descartei a opção de editor de código embutido — imgui-java não tem um widget
de code editor pronto, e mais importante: editor e jogo são projetos/processos
separados, então o texto digitado ali ainda precisaria virar um `.java` real
no projeto do jogo por algum outro caminho, não elimina a complexidade da
opção de escrever arquivo, só adiciona uma UI de texto em cima.

Entre "escrever o .java direto no projeto do jogo" e "gerar um snippet pra
copiar", escolhi o snippet: zero risco de sobrescrever código já editado à
mão (o problema central de qualquer geração automática — recomendação B do
usuário, escrever só se o arquivo não existir, resolveria isso mas ainda
exige o editor ter acesso de escrita a um projeto que não é o dele).
Mantém a fronteira já estabelecida desde o MVP original: o editor não toca no
projeto do jogo, só produz JSON (e agora também um texto pra copiar).

`ClassCodeGenerator` gera uma classe com campo `Sprite` (não `Texture` cru +
`batch.draw` manual, como ficou o rascunho do `Player` em `GameScreen.java`)
— ganha rotação/escala/tint de graça e é mais idiomático libGDX.

## 2026-08-13 — Anchors resolvidos só no editor (decisão delegada: "você decide")
Usuário pediu posicionamento relativo (ex: objeto no topo de outro, com
espaçamento, centralizado) configurável via campos tipo "fromXOf"/"fromYOf" no
Inspector — like em engines como Godot/Unity. A pergunta em aberto era:
resolver só na hora de editar (posição final vira x/y fixo no JSON) ou em
tempo real dentro do jogo (objeto de fato segue outro que se move durante a
partida)? Usuário delegou a escolha, pedindo só que ficasse registrada aqui.

Escolhi resolver só no editor. Motivos:
- Mantém a mesma fronteira de sempre (editor não roda lógica de jogo) — se
  anchors resolvessem em tempo real, o parser do lado do jogo precisaria
  entender a relação e recalcular posição todo frame, o que é lógica de jogo.
- O jogo não precisa mudar o *schema*: `SceneObject` no `libgdx-example-game`
  continua lendo só x/y absolutos, sem precisar saber o que é anchor.
  **Correção (2026-08-13):** achei que gdx `Json` ignorava campo desconhecido
  por padrão — errado, `ignoreUnknownFields` é `false` por padrão e quebrou em
  produção (`SerializationException: Field not found: anchorOf`) assim que o
  editor passou a exportar os campos de anchor. Corrigido chamando
  `json.setIgnoreUnknownFields(true)` no `SceneLoader` do jogo (e também no
  `SceneJsonImporter` do editor, pelo mesmo motivo). Precisa disso em toda
  classe que só lê um subconjunto dos campos do JSON.
- É a opção reversível: se um dia for preciso anchor dinâmico de verdade (item
  flutuando sobre o player enquanto ele anda, por exemplo), dá pra adicionar
  isso depois sem quebrar cenas já exportadas — o JSON já carrega tanto o
  x/y calculado quanto os campos de anchor originais.

Contrapartida: um objeto ancorado não segue seu alvo durante o jogo de fato,
só na hora de posicionar no editor. Se isso virar um requisito real (não só
conveniência de autoria), a decisão deste registro precisa ser revisitada.

## 2026-08-13 — Anchor também contra os bounds da própria cena, não só objetos
Usuário apontou um problema real: só dá pra ancorar em outro objeto, mas x/y
absoluto sozinho não significa nada de confiável entre dispositivos de
tamanhos diferentes — "canto superior direito da tela" não é a mesma coisa que
"x=600, y=340" se a cena não tem um tamanho de referência conhecido.

Resolvido reaproveitando o mesmo mecanismo de anchor já existente: `Scene`
ganhou `sceneWidth`/`sceneHeight` (default 640x360, mesma convenção do
`FitViewport` já usado no `final-roz-game-new-java` original e no
`libgdx-example-game`), e `anchorOf` aceita um valor especial
(`AnchorResolver.SCENE_ANCHOR`) que faz o objeto ancorar contra `0,0`..`sceneWidth,sceneHeight`
em vez de contra outro `SceneObject` — mesma matemática de alinhamento
(`alignedX`/`alignedY`), só troca o que conta como "base". Não criou um
sistema novo, só uma segunda origem possível de bounds pro que já existia.

Pra isso fazer sentido visualmente, o viewport agora desenha um retângulo nos
bounds da cena (`ShapeRenderer`) — sem ver onde a cena realmente termina,
ancorar "na cena" seria ancorar numa caixa invisível.

## 2026-08-13 — Painéis presos na tela, `visible` no objeto, `update()` na classe gerada
Três pedidos diretos do usuário, implementados sem exigir decisão adicional:
- `WindowBounds.keepOnScreen()` (chamado logo após `ImGui.begin()` na Hierarchy
  e no Inspector) força o `x`/`y` da janela pra dentro de `[0, displaySize - windowSize]`
  todo frame — a janela nunca fica arrastável pra fora da área visível.
- `SceneObject.visible` (default `true`, checkbox "Visível" no Inspector).
  `SceneViewport` não desenha nem permite clicar num objeto invisível (clicar
  em cima da posição dele não seleciona nada — seleção continua possível pela
  Hierarchy, que sempre lista todos os objetos independente de visibilidade).
- `ClassCodeGenerator` agora gera `update(float delta)` (vazio, com comentário
  `TODO`) e `render()` passa a checar `visible` antes de desenhar — o
  construtor da classe gerada recebe `visible` como parâmetro, no mesmo padrão
  já usado pra x/y/width/height (dado vem de fora, não é hardcoded).

O rascunho de `Player` em `GameScreen.java` (`libgdx-example-game`) não foi
migrado pra esse formato novo — foi editado à mão pelo usuário, não é o
editor que deveria tocar nele de novo sem pedido explícito. Registrado em
`TODO.md` como sugestão.

## 2026-08-13 — Animações, eventos/sinais e gaps de gameplay: planejado, não implementado
Usuário pediu 3 coisas maiores nesta mesma mensagem, todas tratadas como
"registrar plano" em vez de "implementar agora" (por tamanho/risco, não por
falta de valor — ver `TODO.md` pros planos completos):
- **Animações**: usuário foi explícito que o jogo precisa rodar de verdade
  (não só metadado no JSON). Plano detalhado em `TODO.md` cobre schema
  (`animationFrames`/`animationFrameDuration`/`animationLoop`), UI do editor
  (nova seção no Inspector, reaproveitando o fluxo de import de sprite já
  existente) e reprodução real tanto no editor (WYSIWYG, o motivo original
  deste projeto ser LibGDX e não um editor de imagem genérico) quanto no jogo
  (`Animation<TextureRegion>` do próprio gdx).
- **Sistema de eventos/sinais**: usuário mesmo marcou como "considere, se
  fizer sentido". Avaliação em `TODO.md` separa duas ideias bem diferentes —
  um event bus simples só do lado do jogo (baixo risco, mas prematuro sem uma
  segunda classe gerada pra conversar com a primeira) vs. um sistema
  condição→ação autorado no editor (visual scripting de verdade, quebraria a
  fronteira "editor não roda lógica de jogo" que toda decisão até aqui
  preservou de propósito). Recomendação: adiar os dois, o primeiro até ter uso
  real, o segundo até uma discussão de design dedicada.
- **Gaps pro primeiro jogo de teste**: auditoria registrada em `TODO.md`
  (input/movimento, fábrica id→classe, interface comum de entidade, colisão
  AABB, câmera de cena maior que uma tela) — nenhum item implementado, só
  documentado pra não serem descobertos um por um por acidente depois.

## 2026-08-13 — Animações via atlas implementadas (não o plano de frames soltos)
Usuário pediu pra começar pelo caminho mais simples: `TextureAtlas` do próprio
gdx em vez do plano anterior de `animationFrames: List<String>` (um arquivo de
textura solto por frame). Ele já tinha um atlas de verdade em
`libgdx-example-game/assets/sprites/atlases/bird.atlas` — testado empiricamente
(`TextureAtlas.TextureAtlasData` parseado num programinha Java isolado antes
de mexer no código) e confirmado que carrega certo com a classe `TextureAtlas`
real do gdx, apesar de usar `bounds:` em vez de `xy:`/`size:` (formato mais
antigo/tolerado pelo parser).

Schema mudou de "lista de arquivos" pra "atlas + lista de nomes de região"
(`SceneObject.atlas` + `animationRegions`) — mais simples de importar (um
`.atlas` + uma imagem, não N arquivos), e usa `TextureAtlas.findRegion(name)`
em vez de gerenciar N `Texture` separadas.

`ClassCodeGenerator` agora produz duas formas de classe (estática/`Sprite` ou
animada/`Animation<TextureRegion>`) dependendo se o objeto tem atlas — quem
monta a `Animation` é o código que instancia a classe gerada (`Gamescreen`),
não a classe em si, mesmo padrão de "dado vem de fora" já usado pra x/y/width/height.

**Jogo**: dessa vez os campos de animação tiveram que ser espelhados de
verdade no `SceneObject` do `libgdx-example-game` (diferente do que aconteceu
com anchor) — o jogo *usa* esse dado pra montar a animação, não dá pra só
ignorar campo desconhecido. `Gamescreen.render()` ganhou desenho genérico de
objeto animado (qualquer objeto com atlas, não só `player`/`bullet`) — assim
o pipeline já funciona pra qualquer objeto novo que o usuário criar no editor
com uma animação, sem precisar tocar em `Gamescreen.java` de novo.

Durante essa mudança descobri que o usuário reestruturou o projeto do jogo
por conta própria, fora desta sessão: `GameScreen` virou `eu.dev.screens.Gamescreen`,
`Player` virou `eu.dev.objects.Player` (exatamente no formato que
`ClassCodeGenerator` gera), e apareceu um `eu.dev.objects.Bullet` novo — sinal
de que o fluxo gerar-classe→copiar-colar→editar à mão está sendo usado de
verdade. Autorizado a modificar esses arquivos livremente ("pode modificar a
vontade").

**Não verificado interativamente**: não tenho como clicar no botão "start" do
menu, importar o atlas pelo Inspector, nem observar visualmente a animação
rodando — só compilação e ausência de crash nos primeiros segundos de
execução (ambos os projetos), mais a validação isolada do parser do atlas.

## 2026-08-13 — Tilemap: sem copiar arquivo, sempre na origem da cena
Usuário pediu visualização de tilemap no editor, com um `.tmx` real já em
`assets/maps/` (`mundo1.tmx`) pra testar, e pediu explicitamente pra eu não
depender só dele e decidir o que fosse relevante. Duas decisões de design
tomadas sem perguntar:

**Não copiar o `.tmx` ao importar** (diferente de Add Sprite/Add Atlas, que
copiam). Um `.tmx` pode referenciar tilesets externos (`.tsx`, cada um com
sua própria imagem) e tilesets inline com caminho relativo que apontam pra
qualquer lugar — o `mundo1.tmx` de teste tem exatamente isso
(`../doors/porta_tramela_azul.png`, fora de `assets/maps/`). Copiar esse
grafo de dependências mantendo toda referência relativa intacta é bem mais
arriscado que só exigir que o `.tmx` já esteja em `assets/` (que é como o
mapa de teste chegou lá mesmo). "Add Tilemap..." só referencia o caminho,
abrindo o diálogo já em `assets/maps/`.

**Tilemap sempre desenha em `0,0`**, ignorando `x`/`y` do objeto como offset
de renderização — nem arrastável no viewport. `TiledMapRenderer` não tem uma
forma simples de deslocar a posição desenhada sem transladar a matriz de
projeção separadamente do resto da cena, e na prática uma cena tem no máximo
um tilemap, que já nasce como "o chão". Não vale a complexidade agora;
documentado como limitação conhecida, não escondido.

Também precisei de uma segunda decisão pequena: **seleção por clique
prioriza qualquer objeto não-tilemap**, numa passada separada, independente
da ordem em que os objetos foram criados — um tilemap cobre a cena inteira e
sempre desenha por baixo, então sem essa regra ele "roubaria" cliques de
objetos menores por cima dele dependendo de quando cada um foi adicionado à
lista.

**Verificação**: como não consigo clicar no menu do jogo nem no Inspector,
verifiquei carregando o `mundo1.tmx` real (não um mapa de brinquedo) dentro
de cada projeto de verdade — um diagnóstico temporário em
`EditorApplication.create()` (editor) e trocando `Main.java` pra abrir
`Gamescreen` direto + um objeto tilemap temporário em `level_01.json` (jogo),
rodando dentro do contexto GL real de cada um, checando ausência de exceção,
e revertendo os dois antes do commit. Isso pega o caso realmente arriscado
(tileset externo + tilesets inline com path relativo saindo da pasta) que um
teste isolado de parsing não pegaria.
