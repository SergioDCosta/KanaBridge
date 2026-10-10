# KanaBridge 5.0 — Plano de desenvolvimento UI/UX

> **Objetivo:** melhorar a apresentação, a consistência e a utilização da KanaBridge sem alterar o seu propósito: uma aplicação Android **simples, leve, rápida e offline**.
>
> **Estado:** em curso · **Base:** KanaBridge 4.0.0 · **Tipo de atualização:** refinamento e correção, não reescrita.


> **Atualização de progresso — 10/10/2026:** `[x]` = ponto concluído no âmbito indicado; `[ ]` = pendente ou parcial. As notas **Parcial** identificam trabalho já realizado sem dar o requisito inteiro como concluído. Validação física feita no Xiaomi Android 16; instrumentação apenas compilada, sem execução em emulador. As secções 2–8 não estão concluídas globalmente.

| Secção | Estado atual |
| --- | --- |
| 1. Base de comparação | Concluída para o Xiaomi escolhido; referência v4 registada |
| 2. Bugs visuais | Quatro bugs corrigidos; matriz ampla pendente |
| 3. Design system | Primeira aplicação em Converter/Kana; revisão e expansão pendentes |
| 4. Converter | Cartão reutilizado, estado da entrada e conversão parcial refinados; validação ampla/kanji pendentes |
| 5. Kana e secundários | Kana e ecrãs secundários com design partilhado; paginação e matriz ampla pendentes |
| 6. Acessibilidade | 28 verificações de contraste aprovadas; fontes/fluxos observados; TalkBack pendente |
| 7. Leveza e regressão | Java/SQLite/contraste aprovados; arranque frio e núcleo offline verificados; fluidez/TTS pendentes |
| 8. Validação e lançamento | Kanateste instalada; matriz e lançamento pendentes |

## 0. Princípios da versão 5

1. **Funcionalidades primeiro:** preservar o conversor, as tabelas Kana, o dicionário SQLite, os guardados, a referência, a síntese de voz, os temas e as licenças.
2. **Arranque imediato:** sem splash screens artificiais, animações de entrada longas, inicializações bloqueantes ou carregamento integral do dicionário na thread principal.
3. **Offline e privacidade:** não introduzir Internet, analytics, publicidade, contas, microfone ou novas permissões sem uma necessidade demonstrada.
4. **APK compacto:** comparar sempre o APK final com a versão 4; justificar cada nova dependência.
5. **Mudanças incrementais:** um problema por alteração; executar testes antes de avançar.
6. **Java continua válido:** manter Java e as Android Views atuais enquanto responderem bem aos requisitos. XML pode ser introduzido apenas quando tornar um ecrã claramente mais simples de manter. Kotlin/Compose **não fazem parte do âmbito desta versão**.
7. **Sem funcionalidades novas:** não criar lições, gamificação, contas, tradução automática nem pesquisa online.

---

## 1. Preparar uma base de comparação

**Objetivo:** garantir que conseguimos avaliar se a versão 5 ficou efetivamente melhor.

- [x] Confirmar que a branch `main` corresponde à versão 4.0.0 funcional.
- [x] Criar uma branch para a atualização: `git switch -c feature/v5-ui-ux`.
- [x] Registar o commit de base e a versão do Android usada nos testes.
- [x] Compilar o APK atual e registar tamanho em bytes/MB e SHA-256.
- [x] Medir o arranque a frio da versão 4 no mesmo dispositivo e nas mesmas condições que serão usadas na versão 5 (várias repetições, mediana).
- [x] Registar capturas da versão 4: Converter, Kana, Menu, Dicionário, Guardados, Definições, temas claro/escuro, teclado aberto, texto a 200% e paisagem.
- [x] Criar uma lista de bugs com: **ecrã, passos para reproduzir, resultado atual, resultado esperado, dispositivo/API, captura e prioridade**.
- [x] Executar os testes existentes antes de alterar código.

### Comandos de referência (Windows / PowerShell)

```powershell
.\run-tests.ps1
.\build-apk.ps1
.\build-apk.ps1 -Preview
.\run-android-tests.ps1 -CompileOnly
# Com um emulador Android 16 aberto; substituir o serial se necessário:
.\run-android-tests.ps1 -Serial emulator-5556
.\run-android-tests.ps1 -Serial emulator-5556 -Orientation landscape -ScreenshotFolder android16-landscape
```

> Os scripts e os nomes indicados são os documentados no README da versão 4. O diretório `dist/` é ignorado pelo Git. Não publicar chaves de assinatura.

**Concluída quando:** existe uma referência visual, funcional e de desempenho da versão 4, e os bugs visuais são reproduzíveis.

---

> Registo local: [V5-BASE-COMPARACAO.md](V5-BASE-COMPARACAO.md). A base visual e de desempenho foi recolhida no Xiaomi Android 16 escolhido pelo utilizador; a suite de emulador foi compilada e a execução permanece para as fases de validação.

## 2. Corrigir primeiro os bugs visuais

**Objetivo:** eliminar falhas antes de alterar o aspeto geral.

### 2.1 Teclado, barras do sistema e insets
- [ ] Garantir que o teclado não tapa o campo de entrada, cursor, resultados ou ações essenciais. **Parcial:** Resultado curto e Copiar/Ouvir visíveis no Xiaomi a 100%; entrada de 15 linhas e cursor verificados. Textos longos e outras configurações exigem validação de scroll.
- [ ] Confirmar que a navegação se adapta ao teclado sem saltos visuais ou zonas vazias. **Parcial:** Navegação recolhe/regressa com IME no Xiaomi; falta matriz de dimensões/versões.
- [ ] Verificar que o conteúdo respeita barra de estado, gestos, barra de navegação, recortes e edge-to-edge nas versões suportadas. **Parcial:** Barras/gestos observados no Xiaomi API 36; API 23–35 e outros recortes pendentes.
- [ ] Confirmar o comportamento ao abrir/fechar o teclado repetidamente, rodar o dispositivo e voltar à app. **Parcial:** Abertura/fecho repetidos, rotação de entrada de 15 linhas e regresso à Referência verificados; matriz completa pendente.
- [x] Manter o comportamento de `adjustResize`/insets existente, corrigindo apenas o que falhar. **Feito:** Comportamento existente preservado; ajuste de apresentação pela visibilidade do IME em API 30+.

### 2.2 Alinhamentos e conteúdo
- [ ] Corrigir margens e espaçamentos inconsistentes. **Parcial:** Ajustes no Converter/Kana; restantes ecrãs por rever.
- [ ] Eliminar textos cortados e sobreposições em títulos, campos, botões e resultados. **Parcial:** Hints Kana/Referência corrigidos e textos grandes observados; revisão global pendente.
- [ ] Garantir que mensagens de erro, listas vazias e textos japoneses longos são legíveis. **Parcial:** Estados vazios observados na base; erros e japonês muito longo ainda sem validação abrangente.
- [ ] Verificar scroll e seleção em listas, tabelas de kana e opções de kanji. **Parcial:** Kana e entrada longa observados; listas e opções de kanji pendentes.
- [ ] Confirmar que os estados selecionado, premido, desativado e foco têm representação visual inequívoca. **Parcial:** Seleção em negrito, ripple, foco e estilo desativado implementados no Converter/Kana; validação de todos os estados/ecrãs pendente.

### 2.3 Regra de implementação
- [x] Corrigir cada problema isoladamente, com uma captura **antes/depois**. **Feito:** UI-001 a UI-004 têm comparação antes/depois no relatório de correções.
- [x] Acrescentar um teste automatizado quando o bug puder ser reproduzido por um teste estável. **Feito:** Regressões de teclado e Kana em paisagem acrescentadas; compiladas, execução pendente.
- [x] Não alterar o motor de conversão para corrigir questões de apresentação. **Feito:** Alterações limitadas à apresentação e documentação/testes.

**Concluída quando:** nenhum bug visual de prioridade alta permanece reproduzível nos cenários suportados.

---

> Progresso local: quatro problemas do inventário corrigidos e verificados no Xiaomi; ver [V5-CORRECOES-VISUAIS.md](V5-CORRECOES-VISUAIS.md). A validação ampla desta secção continua pendente. Correções incluídas no bloco de design seguinte; incluídas no commit 1fccc1d desta branch.

## 3. Criar um pequeno design system

**Objetivo:** tornar toda a aplicação coerente sem acrescentar bibliotecas pesadas.

- [x] Definir uma paleta reduzida para tema claro e escuro: fundo, superfície, texto principal, texto secundário, cor de destaque, contorno e erro. **Feito:** Paleta semântica clara/escura em Ui, incluindo token de erro.
- [ ] Centralizar cores e dimensões reutilizáveis (`res/values/` e `res/values-night/`, ou uma classe de tokens já existente, evitando duplicações). **Parcial:** Cores e tokens partilhados em Ui; cor de destaque obtida dos temas nativos. Estilo aplicado aos ecrãs secundários. Restantes dimensões literais específicas ainda por rever.
- [x] Definir uma escala consistente de espaçamento (por exemplo 4, 8, 12, 16, 24 dp), ajustada ao layout real. **Feito:** Tokens 4/8/12/16/24 dp definidos; aplicados nesta primeira revisão do Converter/Kana.
- [ ] Padronizar raios de cantos, alturas de campos, botões, divisórias, ícones e estilos de cartões. **Parcial:** Controlos 12 dp, cartões 16 dp e altura mínima 52 dp em todos os ecrãs; divisórias/ícones e auditoria final pendentes.
- [ ] Definir hierarquia tipográfica: título, secção, texto normal, legenda e destaque japonês. **Parcial:** Tokens para título, corpo, legenda e japonês definidos; título/legenda/japonês aplicados. Revisão completa de secções/corpo pendente.
- [x] Preservar uma fonte japonesa legível e os mecanismos nativos de fallback; não incluir fontes grandes sem necessidade. **Feito:** Fontes nativas e fallback preservados, sem fontes incorporadas.
- [ ] Unificar feedback visual de toque, foco, seleção, cópia e erro. **Parcial:** Toque, foco e seleção preparados; cópia nativa preservada, conversão parcial e erros de consulta com live region. Validação TalkBack/erros e ecrãs secundários pendentes.
- [x] Evitar gradientes, sombras e animações apenas decorativas. **Feito:** Controlos Ui de todos os ecrãs sem elevação/animação decorativa; ripple nativo mantido.

**Progresso:** primeira revisão Converter/Kana concluída visualmente e, após continuação autorizada, componentes aplicados aos restantes ecrãs. Ver [V5-DESIGN-CONVERTER-KANA.md](V5-DESIGN-CONVERTER-KANA.md) e [V5-SECUNDARIOS-CONTRASTE.md](V5-SECUNDARIOS-CONTRASTE.md). Validação ampla pendente.

**Regra:** desenhar e rever dois ecrãs (Converter e Kana) antes de espalhar alterações pelo resto da aplicação.

**Concluída quando:** componentes com a mesma função parecem e comportam-se da mesma forma nos dois temas.

---

## 4. Refinar o ecrã Converter (prioridade máxima)

**Objetivo:** tornar a função principal mais óbvia e agradável, sem adicionar passos.

- [x] Dar prioridade visual ao campo de entrada, com indicação clara do que pode ser escrito ou colado. **Feito:** Campo com foco destacado; introdução recolhe ao abrir o teclado.
- [x] Tornar a seleção de resultado **Hiragana / Katakana / Kanji / Rōmaji** imediatamente compreensível. **Feito:** Preenchimento, negrito e estado acessível Selecionado nos quatro modos.
- [ ] Destacar o resultado sem ocupar espaço excessivo nem o esconder com o teclado. **Parcial:** Cartões e resultado curto visíveis com teclado no Xiaomi; textos grandes/longos e outras dimensões pendentes.
- [x] Organizar as ações **Copiar**, **Ouvir** (quando aplicável) e **Limpar** de forma previsível. **Feito:** Copiar/Ouvir juntos no cartão, Limpar junto à entrada; resultado curto verificado com teclado.
- [ ] Mostrar feedback curto e acessível após copiar, sem interromper a utilização. **Parcial:** confirmação nativa Android 13+ e Toast antigo preservados; copiar/colar verificado no Xiaomi. TalkBack e versão antiga pendentes.
- [ ] Tratar entrada vazia, texto grande, quebras de linha e conversão parcial de forma consistente. **Parcial:** Vazio, 15 linhas e fonte 200% observados; conversão parcial sinalizada também no título e verificada com entrada q. Matriz abrangente pendente.
- [ ] Preservar rascunhos, seleção, scroll e comportamento esperado após rotação ou recriação da Activity. **Parcial:** Entrada de 15 linhas e kanji escolhido preservados após rotação; seleção, foco e scroll da entrada agora guardados/restaurados explicitamente; regressões compiladas, execução e validação ampla pendentes.
- [ ] Rever os fluxos de escolha de kanji, incluindo paginação e distinção pelo significado. **Parcial:** escolha 日本語 e leitura nos três modos verificadas no Xiaomi, incluindo rotação. Paginação, composição e detalhes ainda pendentes.
- [ ] Confirmar que uma nova conversão não provoca reconstruções visuais desnecessárias ou perda de foco. **Parcial:** três modos de texto reutilizam o cartão; modo já selecionado e estado vazio evitam reconstrução. Testes de reutilização/foco compilados. Fluxos de kanji e execução Android pendentes.

> Bloco de estado/resultados: [V5-CONVERTER-ESTADO-RESULTADOS.md](V5-CONVERTER-ESTADO-RESULTADOS.md).

**Concluída quando:** o utilizador consegue introduzir texto, escolher resultado e copiar/ouvir com o mínimo de esforço, sem glitches.

---

## 5. Refinar Kana, Menu e ecrãs secundários

### Kana
- [x] Manter alternância direta entre **Hiragana** e **Katakana**. **Feito:** Alternância Hiragana/Katakana preservada e verificada no Xiaomi.
- [x] Rever pesquisa, grelha gojūon, títulos, alinhamento e espaçamento das células. **Feito:** Pesquisa curta, espaçamentos/células revistos e cabeçalho compacto em paisagem.
- [x] Garantir legibilidade com escala de texto a 200% e reconfiguração para menos colunas quando necessário. **Feito:** Kana verificado no Xiaomi a 150%/200%; duas/uma coluna conforme a escala.
- [ ] Preservar as ações de detalhe, copiar, ouvir e enviar para o Converter. **Parcial:** Detalhe de あ, copiar e enviar ao Converter verificados no Xiaomi; Ouvir mantido no código, audição/TTS e regressão completa pendentes.
- [x] Confirmar que a tabela começa próxima da pesquisa, sem espaço vazio injustificado. **Feito:** Primeira linha visível em paisagem larga; capturas revistas.

### Menu e consulta opcional
- [x] Uniformizar apresentação do Menu, Dicionário, Guardados, Referência, Definições e Fontes/licenças. **Feito:** componentes partilhados, destaque dos diálogos alinhado com o tema e capturas físicas dos ecrãs secundários revistas.
- [ ] Rever cabeçalhos, pesquisa, estados vazios, paginação e regresso ao ecrã anterior. **Parcial:** Hints e nomes de temas corrigidos; pesquisa/detalhes/guardar/remover/Referência vazia/licença/regresso verificados no Xiaomi. Paginação e matriz ampla pendentes.
- [x] Manter o acesso às funcionalidades secundárias discreto e fácil de descobrir. **Feito:** acesso pelo Menu mantido e percorrido; Guardados vazios incluem Abrir dicionário.
- [x] Não deslocar funcionalidades principais para menus escondidos. **Feito:** Converter e Kana mantêm acesso direto na navegação.

**Concluída quando:** todos os ecrãs seguem a mesma linguagem visual e a navegação é previsível.

---

## 6. Polimento de UX, acessibilidade e temas

- [x] Validar contraste dos textos, controlos e estados selecionados em claro e escuro. **Feito:** 28 verificações numéricas das paletas, foco/seleção/erro e estado premido aprovadas em tests/ui_contrast_test.py; capturas nos dois temas revistas. Auditoria manual com TalkBack permanece separada.
- [ ] Testar fontes a 100%, 150% e 200%, sem cortes ou ações inacessíveis. **Parcial:** Converter/Kana observados a 100%/200%, Kana também a 150%; falta validar scroll/acesso a todas as ações e restantes ecrãs.
- [ ] Confirmar áreas de toque confortáveis (manter ou melhorar as dimensões documentadas na v4). **Parcial:** Botões mantêm mínimo de 52 dp e células Kana 72 dp; auditoria global pendente.
- [ ] Verificar etiquetas, ordem de leitura, descrição de kana e estados no TalkBack.
- [ ] Confirmar navegação por teclado e comportamento de foco quando aplicável.
- [ ] Testar mensagens de feedback sem depender exclusivamente de cor ou som.
- [ ] Adicionar apenas microanimações curtas onde explicam uma mudança de estado; respeitar as preferências de animação do sistema.
- [ ] Confirmar idioma português consistente, incluindo botões, estados vazios e mensagens de erro. **Parcial:** Aspeto: Sistema/Claro/Escuro corrigido; auditoria completa de mensagens pendente.

**Concluída quando:** toda a aplicação é utilizável com fontes grandes, TalkBack e ambos os temas.

---

## 7. Preservar leveza, fluidez e funcionalidade

**Objetivo:** não transformar uma atualização visual num retrocesso técnico.

- [ ] Confirmar que as operações de SQLite e inicialização do dicionário continuam fora da thread principal.
- [ ] Verificar se há trabalho desnecessário no arranque e reconstruções completas de ecrãs que causem bloqueios; otimizar apenas problemas medidos.
- [x] Evitar imagens pesadas, fontes incorporadas e novas dependências sem benefício demonstrado. **Feito:** Sem novos recursos pesados, fontes ou dependências neste bloco.
- [ ] Usar ferramentas do Android (`adb shell am start -W`, Perfetto/Profiler, quando necessário) para comparar arranque e fluidez em condições equivalentes. **Parcial:** cinco arranques COLD no mesmo Xiaomi: mediana atual 167 ms vs. 179 ms da v4. Ruído de fundo/térmico não controlado; fluidez ainda por medir.
- [x] Medir tamanho dos APKs da **mesma variante e tipo de build** (v4 vs. v5). Registar diferenças e justificar aumentos relevantes. **Feito:** Kanateste atual e v4: 19731190 bytes, diferença de 0 bytes. Repetir a comparação no APK de lançamento.
- [ ] Confirmar ausência de novas permissões inesperadas no `AndroidManifest.xml` final. **Parcial:** Manifestos compilados do APK v4 e da Kanateste atual inspecionados com aapt2: nenhuma permissão declarada. Repetir no APK de lançamento.
- [x] Executar todos os testes de conversão, leitura, referências e integridade do dicionário. **Feito:** 797 verificações Java e integridade SQLite aprovadas; instrumentação Android tratada na secção 8.
- [x] Confirmar que a aplicação funciona sem ligação à Internet (a instalação de uma voz TTS pode depender de ligação prévia, não a utilização da app). **Feito no núcleo:** Guardados após arranque a frio e dicionário hashi verificados com Wi-Fi/dados desligados; conversão é local e manifesto sem permissões de rede. TTS usa apenas voz offline instalada; reprodução física permanece na matriz da secção 8.
- [ ] Testar arranque a frio e a quente, troca de tema, rotação e alternância rápida entre ecrãs. **Parcial:** Arranque frio atual medido: 167 ms de mediana vs. 179 ms da v4; temas/rotação observados. Arranque quente e alternância rápida pendentes.

### Metas propostas, não garantias automáticas

| Métrica | Meta para v5 |
| --- | --- |
| Funcionalidades | Nenhuma regressão conhecida |
| Testes | Todos os testes v4 existentes a passar; novos testes para bugs corrigidos |
| Arranque | Mediana não superior à v4 nas mesmas condições de teste |
| APK | Sem aumento significativo e injustificado; documentar diferença |
| Internet e novas permissões | Nenhuma introduzida |
| Bugs visuais | Zero bugs de prioridade alta por resolver |

**Concluída quando:** o aspeto melhorou, mas o arranque, a dimensão e as funcionalidades não sofreram regressões relevantes.

---

## 8. Validar em equipamentos reais e lançar

### Matriz mínima de testes
- [ ] Emulador Android 16, retrato, tema claro.
- [ ] Emulador Android 16, retrato, tema escuro.
- [ ] Emulador Android 16, paisagem.
- [ ] Emulador Android 16, escala de texto a 200%.
- [ ] Dispositivo físico **Xiaomi 14T Pro / HyperOS 3**: gestos, barras, teclado, copy/paste, rotação e retorno à aplicação. **Parcial:** Teclado, barras, rotação, retorno e Copiar/Limpar/Colar verificados; matriz de gestos pendente. Versão exata HyperOS não recolhida.
- [ ] Dispositivo físico: TalkBack, voz japonesa offline instalada e ecrãs longos.
- [ ] Pelo menos uma versão Android mais antiga suportada, se existir emulador/dispositivo disponível (a app declara API 23+).
- [x] Teste sem Internet, incluindo o acesso ao dicionário e palavras guardadas. **Feito:** quatro verificações aprovadas no Xiaomi; rede e palavra temporária restauradas. Ver V5-SECUNDARIOS-CONTRASTE.md.
- [x] Testar a atualização por cima de uma instalação compatível, confirmando preservação de dados e **mesma assinatura**; caso contrário testar a variante preview sem substituir a instalação antiga. **Feito:** Usada variante isolada Kanateste, instalada e atualizada com install -r; instalação antiga não substituída. Preservação completa de dados ainda não validada.

### Antes de publicar
- [ ] Executar `run-tests.ps1`, validação SQLite e testes Android aplicáveis. **Parcial:** Java/SQLite aprovados e testes Android compilados; execução Android pendente.
- [ ] Compilar APK final e preview, se necessário, usando os scripts existentes. **Parcial:** Kanateste compilada/instalada; APK final v5 ainda não produzido.
- [ ] Comparar medições v4/v5 e rever as capturas antes/depois. **Parcial:** Capturas revistas, tamanho igual à v4 e arranque frio comparado (167 vs. 179 ms). Fluidez, arranque quente e comparação final de lançamento pendentes.
- [ ] Atualizar `README.md` com as alterações da v5, limitações conhecidas e comandos de compilação. **Parcial:** nota da branch v5, limites da instrumentação e comando do teste de contraste acrescentados. Texto final de lançamento pendente.
- [ ] Atualizar a versão do projeto para **5.0.0** nos locais realmente utilizados pelo build.
- [ ] Rever licenças, atribuições e dados do dicionário; atualizar a fonte apenas se houver motivo e teste específico.
- [x] Criar `CHANGELOG.md` (ou acrescentar secção, se já existir). **Feito:** secção de desenvolvimento v5 criada; completar dados de lançamento após validação final.
- [ ] Rever `git diff` para garantir que não entram APKs, keystores, capturas sensíveis ou ficheiros temporários.
- [ ] Criar tag `v5.0.0` apenas depois da validação final.

**Concluída quando:** existe um APK testado e um histórico de alterações claro, sem regressões conhecidas.

---

## Ordem de trabalho recomendada

1. **Semana/fase A:** referência da v4 + inventário de bugs (secção 1).
2. **Fase B:** bugs críticos de teclado, insets e layouts (secção 2).
3. **Fase C:** design system mínimo (secção 3).
4. **Fase D:** Converter (secção 4).
5. **Fase E:** Kana e ecrãs secundários (secção 5).
6. **Fase F:** acessibilidade e temas (secção 6).
7. **Fase G:** medições e testes de regressão (secção 7).
8. **Fase H:** validação real e release (secção 8).

> Não há prazos obrigatórios. Se uma fase aumentar demasiado a complexidade, reduzir o âmbito em vez de introduzir uma nova arquitetura.

## Modelo de registo de bug

```md
### [UI-001] Título curto
- Ecrã:
- Prioridade: Alta / Média / Baixa
- Dispositivo / versão Android:
- Passos para reproduzir:
  1.
  2.
  3.
- Resultado atual:
- Resultado esperado:
- Capturas antes/depois:
- Ficheiros alterados:
- Teste de regressão:
- Estado: Por fazer / Em curso / Concluído
```

## Definição de «KanaBridge 5.0 pronta»

- [ ] A aplicação abre prontamente e apresenta o Converter sem atrasos visíveis.
- [ ] A UI está consistente e sem bugs visuais de prioridade alta.
- [ ] Teclado, barras do sistema, rotação, temas e texto grande estão validados.
- [ ] As funcionalidades existentes permanecem intactas e os testes passam.
- [ ] APK e desempenho foram comparados com a v4 e os desvios estão documentados.
- [ ] Funciona offline, com as mesmas permissões essenciais.
- [ ] A versão foi validada pelo menos num telemóvel real. **Parcial:** Kanateste testada no Xiaomi Android 16; validação completa de lançamento pendente.
- [ ] README e changelog estão atualizados.

---

### Referência do projeto

- Repositório: https://github.com/SergioDCosta/KanaBridge
- Documentação base: `README.md`, `docs/SIMPLIFICACAO.md` e scripts de testes/compilação na raiz.
- Nota: o plano parte do estado documentado da versão 4 em outubro de 2026. Confirmar cada hipótese face ao código e aos resultados dos testes antes de a implementar.
