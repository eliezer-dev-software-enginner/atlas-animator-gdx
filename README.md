# scene-game-2d-editor

Editor de cena visual para jogos LibGDX. Roda em cima do próprio LibGDX
(desktop, LWJGL3) em vez de ser uma ferramenta genérica de imagem, pra ter
WYSIWYG de verdade: o que você vê no editor é o mesmo pipeline de renderização
que o jogo usa.

O editor não roda lógica de jogo — ele só posiciona objetos numa cena e
exporta um JSON. Do lado do jogo, um parser próprio (`eu.dev.scene.SceneObject`
+ `SceneLoader`, usando `com.badlogic.gdx.utils.Json`) lê esse JSON e
instancia os objetos reais.

## Funcionalidades

- Viewport com zoom/pan, seleção e arraste de objetos
- Sprites estáticos, animações via `TextureAtlas` (com reprodução ao vivo no
  editor, botão Pause/Play), e tilemaps Tiled (`.tmx`)
- Anchors: posicionar um objeto relativo a outro objeto ou aos bounds da cena
  (não só x/y absoluto — importante pra funcionar em telas de tamanhos
  diferentes)
- Objeto pode ser marcado invisível
- Geração de um snippet de classe Java (`Sprite` ou `Animation`-based, com
  `update()`/`render()`) a partir de um objeto selecionado, pra colar no
  projeto do jogo
- Importar/exportar cena em JSON; lembra os últimos caminhos usados e reabre
  a última cena automaticamente
- Layout dos painéis persiste entre execuções

## Como rodar

```
./gradlew lwjgl3:run
```

## Estrutura

- `core`: lógica do editor (`eu.dev.editor` — `scene/`, `viewport/`, `ui/`,
  `codegen/`)
- `lwjgl3`: launcher desktop

Sem módulo Android — é uma ferramenta desktop-only.

## Documentação

- [`docs/CONTEXT.md`](docs/CONTEXT.md) — como cada peça funciona
- [`docs/DECISIONS.md`](docs/DECISIONS.md) — decisões de arquitetura e o
  porquê de cada uma
- [`docs/TODO.md`](docs/TODO.md) — o que falta, o que foi adiado e por quê
