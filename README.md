# GDX Atlas Animator

Ferramenta desktop pequena e focada: carrega um `TextureAtlas` (LibGDX),
monta uma sequência de animação a partir das regiões do atlas, mostra a
animação rodando de verdade (não um frame parado) e gera o snippet de código
Java pra colar no seu jogo.

Nasceu como um editor de cena maior (posicionar sprites, anchors, tilemap...)
que foi abandonado — só a parte de preview de animação por atlas sobreviveu,
porque era a que valia a pena manter.

## Como usar

```
./gradlew lwjgl3:run
```

1. "Selecionar atlas..." — escolhe um `.atlas`. O arquivo e a(s) imagem(ns)
   de página que ele referencia são copiados pra `assets/atlases/`.
2. Escolhe as regiões (na ordem que quiser que toquem) pelo combo + "Adicionar
   frame". Remove com o botão ao lado de cada frame na lista.
3. Ajusta duração do frame e repetição. Pausar/Reproduzir controla a prévia.
4. "Gerar snippet" + "Copiar" (mostra "Copiado!" por instantes) — cola o
   código no seu projeto.

## Estrutura

- `core`: `eu.dev.animator` — `AtlasAnimation` (modelo), `AnimatorApplication`
  (bootstrap + diálogo de arquivo), `AtlasAnimationSnippetGenerator`,
  `ui/AnimatorPanel`, `viewport/AnimationViewport`
- `lwjgl3`: launcher desktop

UI em `imgui-java`. Sem módulo Android — desktop-only.

## Documentação

- [`docs/CONTEXT.md`](docs/CONTEXT.md) — como cada peça funciona
- [`docs/DECISIONS.md`](docs/DECISIONS.md) — decisões de arquitetura,
  incluindo o histórico de antes de virar só o animador
- [`docs/TODO.md`](docs/TODO.md) — o que falta
