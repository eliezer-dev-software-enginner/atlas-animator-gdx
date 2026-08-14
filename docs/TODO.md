# TODO

## Feito
- [x] Selecionar atlas (`.atlas` + imagem de página, copiados pra
  `assets/atlases/`)
- [x] Montar sequência de regiões (picker + lista ordenada com remoção)
- [x] Duração do frame + loop
- [x] Preview tocando de verdade no viewport, com Pause/Play
- [x] Geração de snippet de código (sem classe) + copiar
- [x] Painel preso na área visível da janela
- [x] Renomeado pra "GDX Atlas Animator" (app, gradle, janela)
- [x] Removido tudo do escopo antigo (cena, anchors, tilemap, sprites
  estáticos, geração de classe) — ver `DECISIONS.md`

## Não testado ainda
Tudo isso é código novo (refactor completo) — só compilação e execução sem
crash foram verificadas, nenhum clique real:
- "Selecionar atlas..." (diálogo, cópia de arquivo)
- Picker de região / adicionar / remover frame
- Pause/Play
- "Gerar snippet" / "Copiar"
- Painel preso na tela ao tentar arrastar pra fora

## Possíveis próximos passos
- Lembrar a última animação montada entre execuções (hoje começa vazio
  sempre — não implementado porque não foi pedido, mas seria simples: salvar
  atlas+regions+duration+loop no `AppStorage`)
- Reordenar frames da lista sem precisar remover e readicionar
- Zoom centralizado no cursor (hoje só afeta o zoom da câmera)
- Múltiplas animações abertas ao mesmo tempo (hoje é sempre uma só)
