# Decisões Arquiteturais

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
- O jogo não precisa de nenhuma mudança: `SceneObject`/`SceneLoader` no
  `libgdx-example-game` continuam lendo só x/y absolutos, ignorando os campos
  de anchor (gdx `Json` já tolera campos extras).
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
