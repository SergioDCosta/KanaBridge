# Validação da KanaBridge 3

Dispositivo de destino indicado pelo utilizador: Xiaomi 14T Pro, Android 16, HyperOS 3.

## Executado

- **73 testes de conversão, pesquisa e conteúdo**: saudações, sokuon (`いらっしゃいませ`, `ざっし`, `きって`, `まっちゃ`, `きっぷ`), repetição sonora, marcas isoladas, texto misto, pontuação, variantes de largura, macrons registados e pesquisa sem acentos.
- **44 testes de prática e revisão**: ordem reproduzível após reconstrução da sessão, resposta correta presente, ausência de opções duplicadas, impossibilidade de pontuar duas vezes, avanço da sessão e intervalos de revisão.
- Compilação Java e DEX com plataforma Android 36 e Build Tools 36.0.0.
- Geração e verificação criptográfica de APK assinado; `minSdkVersion=23`, `targetSdkVersion=36`, versão 3.0.0.
- Catálogo de símbolos e metadados do manifesto inspecionados. O APK não contém bibliotecas nativas nem solicita Internet, câmara ou microfone.

## Implementado para Android 16

- Desenho até às bordas com margens calculadas a partir de barras do sistema, recortes de ecrã e teclado. Navegação da aplicação oculta enquanto o teclado ocupa a parte inferior em API 30+.
- Navegação interna pelo `OnBackInvokedDispatcher` em API 33+, mantendo o comportamento antigo nas versões anteriores. Na raiz fica disponível o comportamento de regresso ao início do sistema.
- Orientação e redimensionamento livres, grelhas adaptativas e alturas de conteúdo flexíveis. Com texto ampliado, a navegação passa de quatro para duas colunas.
- Estado de pesquisa, conversor, secção, posição e exercício guardado no Bundle durante recriação; rascunho e progresso persistidos separadamente.
- Temas claro/escuro, ajuste das cores dos ícones do sistema, contraste revisto e alvos de toque de pelo menos 52 dp de altura.

Referências: [mudanças do Android 16](https://developer.android.com/about/versions/16/behavior-changes-16), [acessibilidade em Views](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views?hl=en), [preservação de estado](https://developer.android.com/topic/libraries/architecture/views/saving-states-views).

## Limitação de validação

Foi preparado um emulador oficial Android 16 x86_64. As ferramentas, a plataforma e a imagem foram verificadas pelos checksums do repositório oficial Google. O emulador não chegou a disponibilizar um dispositivo no ADB: ocorreram encerramentos antes de terminar o arranque, incluindo o código de saída 1073741845. Foram tentados caminhos curtos do Windows, Vulkan desativado e execução sem aceleração.

Assim, **os testes instrumentados e a inspeção visual não foram concluídos**. O respetivo APK de testes compila e o roteiro está em `tests/android/AndroidSmokeTest.java` e `run-android-tests.ps1`. A compilação não prova por si só o comportamento visual no HyperOS 3.

## Verificar no Xiaomi ou num emulador funcional

1. Instalar a variante `KanaBridge-preview.apk`, que aparece como KanaBridge 3 e pode coexistir com a instalação antiga.
2. Em modo de gestos, verificar topo junto à câmara e navegação inferior. Repetir com navegação por três botões.
3. Abrir Ferramentas → Conversor, escrever `いらっしゃいませ`, verificar `irasshaimase` e tocar em `っ / s` para ler a explicação.
4. Rodar o telemóvel com texto escrito e durante uma pergunta já respondida: texto, pergunta, feedback e pontuação devem permanecer.
5. Pesquisar `cafe`, `gakkō` e `ｺｰﾋｰ` no dicionário.
6. Guardar uma palavra, terminar a aplicação e confirmar o favorito ao reabrir. A revisão não usa tarefas de fundo, pelo que não requer exceções à gestão de bateria do HyperOS.
7. Experimentar o modo escuro e o tamanho máximo de texto do sistema. Verificar botões, grelhas e diálogos sem cortes horizontais.
8. Abrir o teclado em retrato e paisagem; confirmar que a entrada permanece acessível.
9. Usar Ouvir com voz japonesa offline instalada e também sem essa voz; deve haver reprodução ou uma instrução clara, respetivamente.
10. Com TalkBack, confirmar os rótulos da navegação, campos e respostas do exercício.

## Escopo desta entrega

Inclui as correções prioritárias, a experiência inicial e uma primeira implementação das coleções, favoritos e revisão. Usa síntese de voz offline disponível no sistema, em vez de um pacote de gravações humanas. Continuam como evolução posterior: revisão editorial especializada de todo o conteúdo, áudio humano, animação dos traços, exportação de progresso, furigana de frases, OCR e conversação. Não houve publicação no GitHub nem numa loja.
