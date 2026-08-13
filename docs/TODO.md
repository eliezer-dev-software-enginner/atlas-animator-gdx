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
- [x] Animações via `TextureAtlas` (`atlas`+`animationRegions`), reprodução
  real no viewport com Pause/Play, geração de classe animada, e o jogo
  (`Gamescreen`) desenhando qualquer objeto animado genericamente — ver
  `DECISIONS.md`
- [x] Tilemap (Tiled `.tmx`) — "Add Tilemap..." referencia (não copia) o
  arquivo, `SceneViewport` desenha como camada de fundo, `Gamescreen` faz o
  mesmo genericamente. Verificado com o `mundo1.tmx` real (tileset externo +
  tilesets inline com path relativo pra fora de `assets/maps/`) rodando
  dentro dos dois projetos de verdade — ver `DECISIONS.md`

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
- Animações via atlas: compilou e rodou sem crash nos dois projetos, e a
  leitura do `bird.atlas` real foi validada num programa Java isolado (fora
  do editor) — mas não cliquei em "Selecionar atlas...", "Adicionar frame",
  Pause/Play, nem entrei na `Gamescreen` pelo menu do jogo pra ver a animação
  rodando de verdade. Isso ainda precisa ser testado na mão.
- Tilemap: o carregamento em si foi verificado dentro do contexto real dos
  dois projetos (não só compilação — ver `DECISIONS.md`), mas não cliquei em
  "Add Tilemap..." pelo diálogo de verdade nem vi o mapa desenhado na tela
  (só confirmei ausência de exceção no log). Selecionar o tilemap clicando
  nele no viewport (a passada de picking de menor prioridade) também não foi
  testada na mão.

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
- **Fábrica `id → classe`**: `Gamescreen.show()` agora tem
  `if (object.id.equals("player")) ... else if (object.id.equals("bullet")) ...`
  hardcoded — já dobrou de um pra dois `if`s, e não escala pra mais tipos
  (moeda, inimigo, ...). Um registro tipo `Map<String, ObjectFactory>` (cada
  fábrica recebe `SceneObject` + textura/animação e devolve um tipo comum)
  resolve isso. Ainda vale a pena, mesmo já tendo dois tipos funcionando.
- **Interface comum pros objetos gerados**: algo tipo
  `interface SceneEntity { void update(float delta); void render(SpriteBatch batch); }`
  deixaria `Gamescreen` guardar `List<SceneEntity>` em vez de um campo solto
  por tipo (`player`, `bullet`, depois mais...). `Player`/`Bullet` já têm
  `render(SpriteBatch)` no formato certo; só falta `visible` em ambos e
  `update()` no `Bullet` pra ficarem no mesmo formato exato que
  `ClassCodeGenerator` produz agora.
- **Colisão**: não existe nenhuma. Pra um primeiro teste, checagem AABB simples
  (retângulo x/y/width/height, que todo objeto já tem) já basta — Box2D está
  como dependência no `core/build.gradle` mas não é usado em lugar nenhum, é
  mais peso do que precisa por enquanto.
- **Câmera/cena maior que uma tela**: `Gamescreen` usa `FitViewport(640, 360)`
  fixo, sem scroll nem câmera seguindo o player. Um nível de uma tela só é um
  primeiro jogo de teste válido; só é bom saber disso antes de montar um nível
  maior esperando que "simplesmente funcione".

**Atualização**: o usuário já fez parte disso por conta própria, fora desta
sessão — `GameScreen` virou `eu.dev.screens.Gamescreen`, `Player` foi extraído
pra `eu.dev.objects.Player` (no formato que `ClassCodeGenerator` gera), e
apareceu um `eu.dev.objects.Bullet` novo (ancorado no player via
`anchorOf: "player"` na própria cena). A fábrica/interface comum acima ainda
não existem, mas a extração de classes já é exatamente o passo que este item
sugeria.

## Fora de escopo deste MVP (adiado)
- Parallax em camadas
- Múltiplas cenas/níveis num só projeto
- Anchors resolvidos em tempo real no jogo (hoje só na hora de editar — ver
  `DECISIONS.md`)
- Tilemap arrastável/posicionável (hoje sempre em 0,0 — ver `DECISIONS.md`)
- "Add Tilemap..." copiando o `.tmx` e suas dependências (hoje só referencia
  um arquivo que já precisa estar em `assets/` — ver `DECISIONS.md`)

## Possíveis próximos passos
- Zoom centralizado no cursor do mouse (hoje zoom só move o "zoom" da câmera,
  sem recentralizar)
- Undo/redo
- Geração de boilerplate: opção "escrever .java no jogo só se não existir"
  (idempotente), se o copy-paste do snippet incomodar no dia a dia
