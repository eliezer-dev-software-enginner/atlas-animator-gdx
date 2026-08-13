# TODO

## Feito (MVP)
- [x] Viewport com zoom (scroll) e pan (botão do meio)
- [x] Adicionar sprite (importa imagem para `assets/sprites/`)
- [x] Selecionar e mover sprite (drag no viewport + campos no inspector)
- [x] Nome/id do objeto
- [x] Lista de objetos da cena (Hierarchy)
- [x] Remover sprite
- [x] Exportar para JSON
- [x] Importar/carregar JSON existente
- [x] Salvar direto na pasta assets do jogo (via save dialog nativo)
- [x] Testado manualmente pelo usuário (`./gradlew lwjgl3:run`): abre, painéis
  não sobrepõem mais, Add Sprite/Load/Export não travam mais o app
- [x] Lembrar o último caminho usado em Add Sprite / Load / Export
  (`AppStorage`)
- [x] Lembrar layout dos painéis (posição/tamanho/colapsado) entre execuções
  (`.ini` nativo do ImGui)
- [x] Reabrir automaticamente a última cena usada (Load ou Export) ao iniciar
- [x] `git init` + commits
- [x] Anchors (posicionamento relativo a outro objeto OU aos bounds da cena,
  resolvido no editor — ver `DECISIONS.md`)
- [x] Geração de snippet de classe (`Sprite`-based) com botão de copiar
- [x] `sceneWidth`/`sceneHeight` editáveis, com outline visual no viewport
- [x] Painéis (Hierarchy/Inspector) não saem mais da área visível da janela
- [x] Objeto pode ser marcado invisível (`visible`, checkbox no Inspector;
  viewport não desenha nem permite clicar num objeto invisível)
- [x] Classe gerada tem `update(float delta)` (stub) e respeita `visible` no
  `render()`
- [x] Corrigido: `SceneJsonImporter`/`SceneLoader` quebravam ao ler campo
  desconhecido no JSON (ver `DECISIONS.md`)

## Não testado ainda
- Anchors: compilou e rodou sem crash, mas o fluxo completo (selecionar
  "Anchor" no combo — objeto ou "(cena)" —, ver o objeto seguir a base ao
  arrastá-la/redimensionar a cena, salvar/reabrir e a relação continuar
  editável) não foi clicado manualmente.
- Geração de classe: botão "Gerar classe" + "Copiar" não foi testado
  manualmente (só verificado que compila e o texto gerado bate com o
  esperado por leitura de código).
- Painéis presos na tela / checkbox de visibilidade: compilou e rodou sem
  crash, não testado manualmente arrastando os painéis pra fora ou clicando o
  checkbox.

## Plano: Animações (ainda não implementado)
Pedido do usuário, com o requisito de que o **jogo realmente rode** a
animação (não só a exportação de um frame estático). Plano:

**Schema** (`SceneObject`):
- `animationFrames: List<String>` — caminhos de textura, na ordem de
  reprodução. Vazio = objeto estático, comportamento atual (`texture`)
  continua valendo.
- `animationFrameDuration: float` (default `0.1`) — segundos por frame.
- `animationLoop: boolean` (default `true`).

**Editor:**
- Inspector ganha uma seção "Animação": botão "Adicionar frame..." reaproveita
  o mesmo fluxo de import do "Add Sprite" (copia pra `assets/sprites/`,
  adiciona o caminho em `animationFrames`); lista os frames já adicionados com
  botão de remover (mesmo padrão da lista da Hierarchy); campos de duração e
  loop.
- `SceneViewport` passa a **tocar a animação de verdade** quando
  `animationFrames` não está vazio (usando `com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>`
  + `Gdx.graphics.getDeltaTime()`), em vez de só mostrar `texture` parado —
  isso é o motivo original de o editor rodar em cima do LibGDX de verdade
  ("sem isso não tem WYSIWYG"), então animação tem que se ver rodando ali
  também, não só no jogo.
- Exportação: campos novos já funcionam automaticamente (schema evolutivo,
  ambos os parsers já ignoram campo desconhecido — ver correção recente em
  `DECISIONS.md`).

**Jogo** (`ClassCodeGenerator` + classes geradas):
- Quando o objeto tem `animationFrames`, o gerador produz uma classe diferente:
  campo `Animation<TextureRegion>` + `float stateTime` em vez de só `Sprite`.
  `update(float delta)` acumula `stateTime`; `render(SpriteBatch)` pega
  `animation.getKeyFrame(stateTime, loop)` e desenha.
- O carregamento de N texturas por objeto animado no `GameScreen`/`SceneLoader`
  do jogo é o mesmo padrão já usado pra textura única, só percorrendo uma
  lista por objeto em vez de um único caminho.

## Considerado, não implementado: sistema de eventos/sinais
Usuário sugeriu (com ressalva de "posso estar equivocado") um sistema de
eventos/sinais no pacote que faz o parse, pra coisas tipo "tocar animação X
num evento", "esconder objeto Y", etc. Minha avaliação: são duas coisas bem
diferentes escondidas atrás da mesma ideia —

1. **Um event bus/Signal simples, só do lado do jogo** (`eu.dev.events.Signal<T>`
   ou parecido, sem tocar no editor nem no JSON) pra classes geradas
   conversarem entre si (ex: `Player` notifica "coletei moeda", `Coin` reage
   escondendo-se e tocando um som). Baixo risco, útil, barato de adicionar —
   mas só vale a pena quando existir uma segunda classe de verdade
   precisando conversar com a primeira (hoje só existe `Player`). Não
   implementar especulativamente.
2. **Um sistema condição→ação autorado no editor** (ex: arrastar uma seta de
   "ao colidir com X" pra "esconder Y") — isso é bem maior, é essencialmente
   scripting visual, e quebraria a fronteira que toda decisão dessa sessão
   preservou de propósito (editor não roda/conhece lógica de jogo — colisão,
   vida, timers são conceitos de jogo, não de cena). Merece uma discussão de
   design própria depois, não é continuação natural do que já existe.

Recomendação: considerar (1) quando a segunda classe gerada realmente precisar
notificar a primeira; tratar (2) como uma frente separada, não deste MVP.

## Gaps pro primeiro jogo de teste
Usuário pediu uma varredura do que falta pra sair de "cena posicionada" pra
"jogo jogável". Sem implementar nada disso agora, só registrando:

- **Input/movimento**: hoje só o `Stage` do HUD tem `InputProcessor`
  (`Gdx.input.setInputProcessor(stage)` em `GameScreen.show()`). Não existe
  nada movendo o `Player` — sem isso não dá pra "jogar" de fato.
- **Fábrica `id → classe`**: `GameScreen.show()` hoje tem um
  `if (object.id.equals("player"))` hardcoded. Funciona com um objeto, não
  escala pra vários tipos (moeda, inimigo, ...). Um registro tipo
  `Map<String, ObjectFactory>` (cada fábrica recebe `SceneObject` + texturas e
  devolve um tipo comum) resolve isso.
- **Interface comum pros objetos gerados**: algo tipo
  `interface SceneEntity { void update(float delta); void render(SpriteBatch batch); }`
  deixaria `GameScreen` guardar `List<SceneEntity>` em vez de um campo solto
  por tipo (`player`, depois `coins`, depois...). Amarra bem com a fábrica
  acima e com o `update()`/`visible` que a classe gerada já tem agora.
- **Colisão**: não existe nenhuma. Pra um primeiro teste, checagem AABB simples
  (retângulo x/y/width/height, que todo objeto já tem) já basta — Box2D está
  como dependência no `core/build.gradle` mas não é usado em lugar nenhum, é
  mais peso do que precisa por enquanto.
- **Câmera/cena maior que uma tela**: `GameScreen` usa `FitViewport(640, 360)`
  fixo, sem scroll nem câmera seguindo o player. Um nível de uma tela só é um
  primeiro jogo de teste válido; só é bom saber disso antes de montar um nível
  maior esperando que "simplesmente funcione".
- **`Player` inline em `GameScreen.java`**: é uma classe aninhada estática, de
  antes do gerador de classe existir. Migrar pra uma classe própria
  (`eu.dev.Player`, no formato que `ClassCodeGenerator` produz agora, com
  `update()`/`visible`) deixaria consistente com qualquer próxima classe
  gerada — não fiz isso agora pra não mexer em arquivo que você editou à mão.

## Fora de escopo deste MVP (adiado)
- Parallax em camadas
- Tilemap
- Múltiplas cenas/níveis num só projeto
- Anchors resolvidos em tempo real no jogo (hoje só na hora de editar — ver
  `DECISIONS.md`)

## Possíveis próximos passos
- Zoom centralizado no cursor do mouse (hoje zoom só move o "zoom" da câmera,
  sem recentralizar)
- Undo/redo
- Geração de boilerplate: opção "escrever .java no jogo só se não existir"
  (idempotente), se o copy-paste do snippet incomodar no dia a dia
