# KanaBridge 5.0 — Plano de desenvolvimento UI/UX

> **Objetivo:** melhorar a apresentação, a consistência e a utilização da KanaBridge sem alterar o seu propósito: uma aplicação Android **simples, leve, rápida e offline**.
>
> **Estado:** planeado · **Base:** KanaBridge 4.0.0 · **Tipo de atualização:** refinamento e correção, não reescrita.

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
- [ ] Garantir que o teclado não tapa o campo de entrada, cursor, resultados ou ações essenciais.
- [ ] Confirmar que a navegação se adapta ao teclado sem saltos visuais ou zonas vazias.
- [ ] Verificar que o conteúdo respeita barra de estado, gestos, barra de navegação, recortes e edge-to-edge nas versões suportadas.
- [ ] Confirmar o comportamento ao abrir/fechar o teclado repetidamente, rodar o dispositivo e voltar à app.
- [ ] Manter o comportamento de `adjustResize`/insets existente, corrigindo apenas o que falhar.

### 2.2 Alinhamentos e conteúdo
- [ ] Corrigir margens e espaçamentos inconsistentes.
- [ ] Eliminar textos cortados e sobreposições em títulos, campos, botões e resultados.
- [ ] Garantir que mensagens de erro, listas vazias e textos japoneses longos são legíveis.
- [ ] Verificar scroll e seleção em listas, tabelas de kana e opções de kanji.
- [ ] Confirmar que os estados selecionado, premido, desativado e foco têm representação visual inequívoca.

### 2.3 Regra de implementação
- [ ] Corrigir cada problema isoladamente, com uma captura **antes/depois**.
- [ ] Acrescentar um teste automatizado quando o bug puder ser reproduzido por um teste estável.
- [ ] Não alterar o motor de conversão para corrigir questões de apresentação.

**Concluída quando:** nenhum bug visual de prioridade alta permanece reproduzível nos cenários suportados.

---

## 3. Criar um pequeno design system

**Objetivo:** tornar toda a aplicação coerente sem acrescentar bibliotecas pesadas.

- [ ] Definir uma paleta reduzida para tema claro e escuro: fundo, superfície, texto principal, texto secundário, cor de destaque, contorno e erro.
- [ ] Centralizar cores e dimensões reutilizáveis (`res/values/` e `res/values-night/`, ou uma classe de tokens já existente, evitando duplicações).
- [ ] Definir uma escala consistente de espaçamento (por exemplo 4, 8, 12, 16, 24 dp), ajustada ao layout real.
- [ ] Padronizar raios de cantos, alturas de campos, botões, divisórias, ícones e estilos de cartões.
- [ ] Definir hierarquia tipográfica: título, secção, texto normal, legenda e destaque japonês.
- [ ] Preservar uma fonte japonesa legível e os mecanismos nativos de fallback; não incluir fontes grandes sem necessidade.
- [ ] Unificar feedback visual de toque, foco, seleção, cópia e erro.
- [ ] Evitar gradientes, sombras e animações apenas decorativas.

**Regra:** desenhar e rever dois ecrãs (Converter e Kana) antes de espalhar alterações pelo resto da aplicação.

**Concluída quando:** componentes com a mesma função parecem e comportam-se da mesma forma nos dois temas.

---

## 4. Refinar o ecrã Converter (prioridade máxima)

**Objetivo:** tornar a função principal mais óbvia e agradável, sem adicionar passos.

- [ ] Dar prioridade visual ao campo de entrada, com indicação clara do que pode ser escrito ou colado.
- [ ] Tornar a seleção de resultado **Hiragana / Katakana / Kanji / Rōmaji** imediatamente compreensível.
- [ ] Destacar o resultado sem ocupar espaço excessivo nem o esconder com o teclado.
- [ ] Organizar as ações **Copiar**, **Ouvir** (quando aplicável) e **Limpar** de forma previsível.
- [ ] Mostrar feedback curto e acessível após copiar, sem interromper a utilização.
- [ ] Tratar entrada vazia, texto grande, quebras de linha e conversão parcial de forma consistente.
- [ ] Preservar rascunhos, seleção, scroll e comportamento esperado após rotação ou recriação da Activity.
- [ ] Rever os fluxos de escolha de kanji, incluindo paginação e distinção pelo significado.
- [ ] Confirmar que uma nova conversão não provoca reconstruções visuais desnecessárias ou perda de foco.

**Concluída quando:** o utilizador consegue introduzir texto, escolher resultado e copiar/ouvir com o mínimo de esforço, sem glitches.

---

## 5. Refinar Kana, Menu e ecrãs secundários

### Kana
- [ ] Manter alternância direta entre **Hiragana** e **Katakana**.
- [ ] Rever pesquisa, grelha gojūon, títulos, alinhamento e espaçamento das células.
- [ ] Garantir legibilidade com escala de texto a 200% e reconfiguração para menos colunas quando necessário.
- [ ] Preservar as ações de detalhe, copiar, ouvir e enviar para o Converter.
- [ ] Confirmar que a tabela começa próxima da pesquisa, sem espaço vazio injustificado.

### Menu e consulta opcional
- [ ] Uniformizar apresentação do Menu, Dicionário, Guardados, Referência, Definições e Fontes/licenças.
- [ ] Rever cabeçalhos, pesquisa, estados vazios, paginação e regresso ao ecrã anterior.
- [ ] Manter o acesso às funcionalidades secundárias discreto e fácil de descobrir.
- [ ] Não deslocar funcionalidades principais para menus escondidos.

**Concluída quando:** todos os ecrãs seguem a mesma linguagem visual e a navegação é previsível.

---

## 6. Polimento de UX, acessibilidade e temas

- [ ] Validar contraste dos textos, controlos e estados selecionados em claro e escuro.
- [ ] Testar fontes a 100%, 150% e 200%, sem cortes ou ações inacessíveis.
- [ ] Confirmar áreas de toque confortáveis (manter ou melhorar as dimensões documentadas na v4).
- [ ] Verificar etiquetas, ordem de leitura, descrição de kana e estados no TalkBack.
- [ ] Confirmar navegação por teclado e comportamento de foco quando aplicável.
- [ ] Testar mensagens de feedback sem depender exclusivamente de cor ou som.
- [ ] Adicionar apenas microanimações curtas onde explicam uma mudança de estado; respeitar as preferências de animação do sistema.
- [ ] Confirmar idioma português consistente, incluindo botões, estados vazios e mensagens de erro.

**Concluída quando:** toda a aplicação é utilizável com fontes grandes, TalkBack e ambos os temas.

---

## 7. Preservar leveza, fluidez e funcionalidade

**Objetivo:** não transformar uma atualização visual num retrocesso técnico.

- [ ] Confirmar que as operações de SQLite e inicialização do dicionário continuam fora da thread principal.
- [ ] Verificar se há trabalho desnecessário no arranque e reconstruções completas de ecrãs que causem bloqueios; otimizar apenas problemas medidos.
- [ ] Evitar imagens pesadas, fontes incorporadas e novas dependências sem benefício demonstrado.
- [ ] Usar ferramentas do Android (`adb shell am start -W`, Perfetto/Profiler, quando necessário) para comparar arranque e fluidez em condições equivalentes.
- [ ] Medir tamanho dos APKs da **mesma variante e tipo de build** (v4 vs. v5). Registar diferenças e justificar aumentos relevantes.
- [ ] Confirmar ausência de novas permissões inesperadas no `AndroidManifest.xml` final.
- [ ] Executar todos os testes de conversão, leitura, referências e integridade do dicionário.
- [ ] Confirmar que a aplicação funciona sem ligação à Internet (a instalação de uma voz TTS pode depender de ligação prévia, não a utilização da app).
- [ ] Testar arranque a frio e a quente, troca de tema, rotação e alternância rápida entre ecrãs.

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
- [ ] Dispositivo físico **Xiaomi 14T Pro / HyperOS 3**: gestos, barras, teclado, copy/paste, rotação e retorno à aplicação.
- [ ] Dispositivo físico: TalkBack, voz japonesa offline instalada e ecrãs longos.
- [ ] Pelo menos uma versão Android mais antiga suportada, se existir emulador/dispositivo disponível (a app declara API 23+).
- [ ] Teste sem Internet, incluindo o acesso ao dicionário e palavras guardadas.
- [ ] Testar a atualização por cima de uma instalação compatível, confirmando preservação de dados e **mesma assinatura**; caso contrário testar a variante preview sem substituir a instalação antiga.

### Antes de publicar
- [ ] Executar `run-tests.ps1`, validação SQLite e testes Android aplicáveis.
- [ ] Compilar APK final e preview, se necessário, usando os scripts existentes.
- [ ] Comparar medições v4/v5 e rever as capturas antes/depois.
- [ ] Atualizar `README.md` com as alterações da v5, limitações conhecidas e comandos de compilação.
- [ ] Atualizar a versão do projeto para **5.0.0** nos locais realmente utilizados pelo build.
- [ ] Rever licenças, atribuições e dados do dicionário; atualizar a fonte apenas se houver motivo e teste específico.
- [ ] Criar `CHANGELOG.md` (ou acrescentar secção, se já existir).
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
- [ ] A versão foi validada pelo menos num telemóvel real.
- [ ] README e changelog estão atualizados.

---

### Referência do projeto

- Repositório: https://github.com/SergioDCosta/KanaBridge
- Documentação base: `README.md`, `docs/SIMPLIFICACAO.md` e scripts de testes/compilação na raiz.
- Nota: o plano parte do estado documentado da versão 4 em outubro de 2026. Confirmar cada hipótese face ao código e aos resultados dos testes antes de a implementar.

