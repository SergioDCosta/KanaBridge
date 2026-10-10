# Secção 2 — Correções visuais

> Registo desta etapa de desenvolvimento; código incluído no commit 1fccc1d. A uniformização posterior e a validação offline estão registadas em [V5-SECUNDARIOS-CONTRASTE.md](V5-SECUNDARIOS-CONTRASTE.md).
10 de outubro de 2026. Base de documentação commitada em `e40180a`; código v4 de referência em `4fc1301`. Quatro problemas do inventário corrigidos e verificados no Xiaomi 14T Pro / Android 16 (API 36). Alterações incluídas no commit 1fccc1d. A secção não é declarada validada em todas as versões e configurações suportadas.

## Alterações e evidência

| Problema | Correção | Antes (v4-xiaomi) | Depois (v5-section2) |
| --- | --- | --- | --- |
| UI-001 | Com IME aberto, a introdução recolhe, entrada tem mínimo de uma linha e margens de entrada/ferramentas/resultado passam de 10 para 4 dp; regressa ao normal ao fechar | 10-keyboard-light.png | 01-keyboard-light.png e 02-keyboard-closed-light.png |
| UI-004 | Em Kana, paisagem larga (600 dp ou mais) e texto até 130%, o Menu partilha a linha de seleção e dispensa o cabeçalho separado; primeira linha fica visível | 14-kana-landscape-light.png | 03-kana-landscape.png |
| UI-002 | Nomes de tema apresentados como Sistema, Claro e Escuro; valores persistidos continuam system/light/dark | 08-settings-dark.png | 07-settings-system.png e 08-settings-light.png |
| UI-003 | Indicações curtas Som ou kana / Pesquisar; descrições de acessibilidade mantêm o contexto e exemplos | 13-kana-200-light.png e 07-reference-dark.png | 09-kana-200.png e 10-reference-search.png |

As PNG e XML estão em `build/screenshots`, ignorado pelo Git. Foram inspecionadas visualmente. Cada problema foi implementado separadamente; teclado e paisagem foram compilados, instalados e observados antes das correções de texto.

O layout de teclado é aplicado pela visibilidade do IME no Android 11+. O ajuste nativo de janela nas versões anteriores permanece como na v4. Não foi alterado o motor de conversão, o dicionário, o manifesto nem dependências.

## Validação executada

- 797 verificações Java aprovadas e validação SQLite aprovada após a alteração de teclado. As alterações seguintes apenas afetam apresentação; o motor não mudou.
- APK Kanateste final compilado e assinatura verificada; instalado com `install -r`.
- Regressões acrescentadas à suite Android para ações do resultado com IME e primeira linha de Kana em paisagem; APK de testes compilado. A suite permanece restrita a emuladores e não foi executada no Xiaomi.
- No Xiaomi: resultado irasshaimase e Copiar/Ouvir totalmente visíveis com Gboard aberto, retrato e texto a 100%; introdução e navegação voltam após fechar. Abertura/fecho repetidos duas vezes adicionais sem erro do script.
- Primeiro botão da tabela totalmente acima da navegação em paisagem; Menu aberto e verificado.
- Entrada de 15 linhas preservada após rotação; cursor observado acima do teclado em paisagem. Capturas 04-multiline-keyboard.png e 05-multiline-landscape.png. Textos longos continuam a exigir scroll para consultar o resultado.
- Captura do Converter a 200%: 06-converter-200.png. Captura de Kana a 200% inspecionada: uma coluna e indicação sem corte.
- Seis verificações adicionais aprovadas: nomes Sistema/Claro/Escuro, indicação Som ou kana, indicação Pesquisar e referência com 290 entradas após regressar do Home. Log local label-checks.json.
- Escala de texto, rotação bloqueada em retrato e tema Seguir o sistema restaurados no final. A Kanateste termina no Converter.
- `git diff --check` sem erros. Corrigida também uma linha vazia extra no fim do roadmap.

## APK da revisão inicial das correções

- `dist/Kanateste.apk`, pacote `com.kanabridge.kanateste`, ainda versionName 4.0.0 conforme o plano de atualizar a versão apenas na fase de lançamento.
- 19731190 bytes: o mesmo tamanho da variante Kanateste de referência.
- SHA-256: `9E1DAF95BD39547464F076B9A2CE40B19B959FF1FB9F8AF1E8CC33D8DE784732`.
- O APK original v4 em dist/KanaBridge.apk não foi recompilado nesta etapa.

## Limites e trabalho restante da secção

Ainda falta executar a instrumentação Android e ampliar a validação a uma versão anterior suportada, diferentes dimensões/teclados, textos e listas muito longos e estados de foco/seleção em todos os ecrãs. Os cenários verificados no Xiaomi não demonstram ausência de bugs em API 23–35.

Com texto longo, fonte muito grande ou pouca altura em paisagem, não há promessa de mostrar entrada, resultado e todas as ações simultaneamente; o scroll e o cursor devem ser avaliados na matriz ampla. O design system começou no bloco seguinte; ver V5-DESIGN-CONVERTER-KANA.md.

Estado dos quatro problemas de base: corrigidos nos cenários descritos e incluídos no commit 1fccc1d. Sem bugs de prioridade alta confirmados durante estas verificações, mas a secção 2 permanece em curso até à validação restante.
