Você é o agente oficial deste projeto.

Antes de executar qualquer tarefa:

1. Leia docs/AI_RULES.md.
2. Leia README.md.
3. Analise a estrutura atual do projeto.
4. Identifique padrões já utilizados.
5. Siga os padrões existentes.
6. Nunca introduza tecnologias diferentes sem autorização.
7. Sempre explique brevemente o plano antes de modificar arquivos.
8. Mantenha um histórico das decisões em docs/DECISIONS.md.
9. Ao finalizar uma tarefa, registre:
    - O que foi alterado.
    - Motivo da alteração.
    - Arquivos modificados.
    - Próximos passos recomendados.

Toda nova sessão deve consultar AI_RULES.md e DECISIONS.md antes de iniciar.

Leia docs/AI_RULES.md, docs/CONTEXT.md, docs/DECISIONS.md e docs/TODO.md.

Entenda o projeto antes de agir.

Após cada tarefa:
- Atualize docs/CONTEXT.md.
- Atualize docs/DECISIONS.md se houver decisão arquitetural.
- Atualize docs/TODO.md.
- Mantenha os arquivos concisos.

Prompt:
# Prompt para Claude Code — MVP Editor de Cena LibGDX

Copie o bloco abaixo e cole no Claude Code na raiz de um novo projeto (ou peça pra ele criar o projeto do zero).

---

## Contexto

Estou criando um editor de cena visual para jogos feitos em LibGDX. Já tenho um framework JavaFX (Megalodonte) que uso para outras ferramentas, mas decidi que este editor específico deve ser um **projeto LibGDX separado**, rodando como app desktop (backend `lwjgl3`), porque o conteúdo que ele edita (sprites, parallax, tilemap) precisa usar o mesmo pipeline de renderização do jogo — sem isso não tem WYSIWYG de verdade.

O editor **não roda a lógica do jogo**. Ele apenas posiciona/edita objetos visuais e exporta um **JSON de cena**. No próprio projeto do jogo (fora do escopo deste prompt), uma classe de parser vai ler esse JSON e instanciar os objetos reais com a lógica correspondente. Ou seja: o editor cuida só da parte de conteúdo/posicionamento, a lógica fica 100% do lado do jogo.

## Objetivo deste MVP

Quero o mínimo possível que já seja útil: **posicionar sprites em uma cena e exportar isso em JSON**. Exemplo de caso de uso: posicionar o sprite do player (posição inicial, tamanho) numa cena e o jogo ler isso pra spawnar o player no lugar certo.

Não quero ainda (deixar para depois, não implementar neste MVP):
- Parallax em camadas
- Tilemap
- Animações
- Múltiplas cenas/níveis num só projeto (por enquanto uma cena = um arquivo JSON)

## Stack técnica

- Java + LibGDX, backend **lwjgl3** (app desktop)
- Gradle (pode usar o `gdx-setup` padrão ou gerar a estrutura manualmente, sem precisar do launcher Android/iOS — só o módulo `desktop` e `core`)
- **imgui-java** (bindings Java do Dear ImGui, via `io.github.spair:imgui-java-binding` + natives da plataforma) para a UI do editor (painéis, inspector de propriedades, lista de sprites na cena). Não usar Scene2D UI para a chrome do editor — reservar Scene2D só se necessário para o viewport da cena em si.
- Renderização da cena (viewport com os sprites posicionados) usando `SpriteBatch` + `OrthographicCamera` do próprio LibGDX, no mesmo contexto GL da janela do editor (sem bridge de captura de framebuffer).

## Escopo funcional do MVP

1. **Viewport de cena**: área que renderiza os sprites já posicionados, com zoom e pan básicos (scroll para zoom, botão do meio ou espaço+arraste para pan).
2. **Adicionar sprite**: botão/menu para importar uma imagem (de uma pasta de assets do projeto do editor, ex: `assets/sprites/`) e adicionar como um novo objeto na cena, na posição (0,0) ou no centro do viewport.
3. **Selecionar e mover sprite**: clicar num sprite no viewport para selecioná-lo, e poder:
    - Arrastar para reposicionar (drag no viewport)
    - Editar posição (x, y) por campos numéricos no painel de inspector
    - Editar tamanho (largura, altura) por campos numéricos
    - Dar um **nome/id** ao objeto (ex: "player", "inimigo_01") — isso é o campo mais importante, pois é a chave que o parser do jogo vai usar pra saber que classe/lógica instanciar
4. **Lista de objetos da cena**: painel lateral listando todos os sprites já adicionados, com clique para selecionar.
5. **Remover sprite** da cena.
6. **Exportar para JSON**: botão "Export" que salva um arquivo JSON com a lista de objetos da cena.
7. **Importar/carregar** um JSON já existente para continuar editando (load).
8. **Salvar direto na pasta assets do jogo**: permitir configurar (ou pedir via input) o caminho da pasta `assets` do projeto do jogo, e exportar o JSON ali.

## Formato do JSON (sugestão de schema — pode ajustar se fizer sentido)

```json
{
  "sceneName": "level_01",
  "objects": [
    {
      "id": "player",
      "type": "sprite",
      "texture": "sprites/player.png",
      "x": 120.0,
      "y": 340.0,
      "width": 32.0,
      "height": 48.0
    }
  ]
}
```

O campo `id` é o que o parser do lado do jogo vai usar para decidir qual classe/lógica associar ao objeto (ex: `"player"` → instancia a classe `Player`). O campo `type` deixa aberto para no futuro ter outros tipos além de `"sprite"` (tilemap, parallax layer, etc.) sem quebrar o schema atual.

## Estrutura de projeto sugerida

```
scene-editor/
  core/
    src/.../editor/
      EditorApplication.java     // ApplicationAdapter principal
      scene/
        SceneObject.java         // modelo de dados de um objeto na cena
        Scene.java                // modelo da cena inteira (lista de objects)
        SceneJsonExporter.java    // Scene -> JSON
        SceneJsonImporter.java    // JSON -> Scene
      ui/
        EditorUI.java              // wiring geral do imgui (janelas/painéis)
        InspectorPanel.java        // painel de propriedades do objeto selecionado
        HierarchyPanel.java        // lista de objetos da cena
      viewport/
        SceneViewport.java         // câmera, batch, seleção e drag no viewport
  desktop/
    src/.../DesktopLauncher.java  // Lwjgl3Application, setup da janela
```

## Entregáveis esperados

1. Projeto Gradle funcional, rodando com `./gradlew desktop:run` e abrindo a janela do editor.
2. As 8 funcionalidades do escopo acima implementadas e funcionando.
3. Uso de `com.badlogic.gdx.utils.Json` (utilitário nativo do LibGDX) para serialização, a menos que haja um motivo forte pra usar outra lib — evitar dependência extra se o `Json` do próprio gdx resolver.
4. Código organizado nos pacotes sugeridos acima (ou equivalente, se você tiver uma organização melhor, pode propor).
5. Não se preocupar com testes automatizados neste MVP — é uma ferramenta interna de uso pessoal.

## Perguntas que você pode me fazer antes de começar (se precisar)

- Se algo do escopo estiver ambíguo, pode perguntar antes de implementar, mas prefira assumir a decisão mais simples possível para o MVP em vez de me interromper toda hora.
