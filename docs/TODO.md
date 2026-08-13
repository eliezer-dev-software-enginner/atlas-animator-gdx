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

## Fora de escopo deste MVP (adiado)
- Parallax em camadas
- Tilemap
- Animações
- Múltiplas cenas/níveis num só projeto

## Possíveis próximos passos
- Zoom centralizado no cursor do mouse (hoje zoom só move o "zoom" da câmera,
  sem recentralizar)
- Undo/redo
