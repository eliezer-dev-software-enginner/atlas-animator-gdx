# TODO

## Feito
- [x] Selecionar atlas (`.atlas` + imagem de página, copiados pra
  `assets/atlases/`) — testado pelo usuário, funcionou, inclusive
  `lastAtlasPath` confirmado persistindo (agora em
  `~/.prefs/atlas-animator-gdx`, renomeado junto com o app — reseta essa
  única preferência lembrada uma vez, sem mais efeito)
- [x] Montar sequência de regiões (picker + lista ordenada com remoção)
- [x] Duração do frame + loop
- [x] Preview tocando de verdade no viewport, com Pausar/Reproduzir
- [x] Geração de snippet de código (sem classe) + copiar, com feedback
  "Copiado!" por 1.5s
- [x] Removido tudo do escopo antigo (cena, anchors, tilemap, sprites
  estáticos, geração de classe) — ver `DECISIONS.md`
- [x] Preview reposicionado pra área à direita do painel (antes aparecia no
  canto inferior esquerdo, atrás/fora da área visível)
- [x] Painel `Animator` agora é fixo de verdade (`NoMove`/`NoResize` +
  `ImGuiCond.Always`) em vez de só clampado — não dá mais pra arrastar nem
  redimensionar. `WindowBounds` removido (ficou sem uso)
- [x] Todos os textos da UI e o README traduzidos pra português (nome do
  app fica em inglês — "Atlas Animator GDX" — por pedido explícito)
- [x] Renomeado pra `atlas-animator-gdx` (era `gdx-atlas-animator`);
  empacotamento via `jpackage` igual ao `hud-creator-gdx` (`BUILD.md` +
  `.github/workflows/package.yml`), `construo`/GraalVM removidos — ver
  `DECISIONS.md`

## Não testado ainda
- Picker de região / adicionar / remover frame
- Pausar/Reproduzir
- "Gerar snippet" / "Copiar" (o feedback "Copiado!" incluso)
- Se o painel realmente não se move ao tentar arrastar o título
- Se o preview aparece mesmo à direita do painel na prática (matemática da
  câmera conferida à mão, não visualmente)
- `jpackage --type exe` (Windows) — só a parte Linux (`app-image` + `.deb`)
  foi testada de verdade, ver `DECISIONS.md`
- `.github/workflows/package.yml` nunca rodou de verdade ainda (nem
  manualmente nem por tag)

## Possíveis próximos passos
- Lembrar a última animação montada entre execuções (hoje começa vazio
  sempre — não implementado porque não foi pedido, mas seria simples: salvar
  atlas+regions+duration+loop no `AppStorage`)
- Reordenar frames da lista sem precisar remover e readicionar
- Zoom centralizado no cursor (hoje só afeta o zoom da câmera)
- Múltiplas animações abertas ao mesmo tempo (hoje é sempre uma só)
- Ícone próprio (hoje usa o genérico do template `gdx-liftoff`, ver
  `BUILD.md`)
