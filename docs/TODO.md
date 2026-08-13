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
- [x] Anchors (posicionamento relativo a outro objeto, resolvido no editor —
  ver `DECISIONS.md`)
- [x] Geração de snippet de classe (`Sprite`-based) com botão de copiar

## Não testado ainda
- Anchors: compilou e rodou sem crash, mas o fluxo completo (selecionar
  "Anchor" no combo, ver o objeto seguir a base ao arrastá-la, salvar/reabrir
  e a relação continuar editável) não foi clicado manualmente.
- Geração de classe: botão "Gerar classe" + "Copiar" não foi testado
  manualmente (só verificado que compila e o texto gerado bate com o
  esperado por leitura de código).

## Fora de escopo deste MVP (adiado)
- Parallax em camadas
- Tilemap
- Animações
- Múltiplas cenas/níveis num só projeto
- Anchors resolvidos em tempo real no jogo (hoje só na hora de editar — ver
  `DECISIONS.md`)

## Possíveis próximos passos
- Zoom centralizado no cursor do mouse (hoje zoom só move o "zoom" da câmera,
  sem recentralizar)
- Undo/redo
- Geração de boilerplate: opção "escrever .java no jogo só se não existir"
  (idempotente), se o copy-paste do snippet incomodar no dia a dia
