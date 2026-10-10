# Base de comparação — KanaBridge 4

Secção 1 do [roadmap v5](KANABRIDGE-V5-ROADMAP.md), concluída em 10 de outubro de 2026 para o dispositivo físico escolhido pelo utilizador. Não foram alterados o código da aplicação nem os scripts versionados. Esta referência não substitui a matriz completa de regressão e lançamento.

## Base e ambiente

- `main` limpa e sincronizada com `origin/main` ao iniciar.
- Commit: `4fc13015cccc69c7ac24b3bc8f832a17248a3d93`.
- Build v4: versionName 4.0.0, versionCode 4, minSdk 23, targetSdk 36.
- Branch: `feature/v5-ui-ux`; projeto: `C:\Dev\personal\KanaBridge-source`.
- JDK: Microsoft OpenJDK 21.0.12.8.
- SDK: `C:\Users\USER\AppData\Local\Android\Sdk`, plataforma 36, Build Tools 36.0.0. A variável herdada ANDROID_SDK_ROOT aponta para Documents; foi substituída apenas nos processos de compilação. C:\Android contém a plataforma 35.
- ADB usado na recolha: C:\Android\platform-tools\adb.exe, versão 37.0.1.
- Xiaomi 14T Pro, modelo 2407FPN8EG, Android 16 / API 36, build BP2A.250605.031.A3. A versão exata do HyperOS não foi recolhida.
- Resolução 1220 × 2712, densidade 520 dpi, escala de texto original 1.0, tema do sistema escuro, rotação inicialmente bloqueada em retrato.

## Resultados funcionais e compilação

| Verificação | Resultado |
| --- | --- |
| run-tests.ps1 | 73 + 724 = 797 verificações aprovadas |
| tests/lexicon_test.py | Integridade SQLite, 218869 entradas, 325004 grafias, exemplos, índices e atribuições aprovados |
| build-apk.ps1 | APK original 4.0.0 compilado; assinatura verificada |
| build-apk.ps1 -Preview | Preview compilada e assinatura verificada; instalação recusada por assinatura diferente da preview já existente |
| run-android-tests.ps1 -CompileOnly | APK de instrumentação compilado; não executado |
| Verificações ADB na Kanateste | 7 verificações aprovadas: tabela básica, pesquisa hashi → bridge, vazio de Guardados, referência com 290 entradas, irasshaimase → hiragana / katakana / rōmaji |

A suite Android existente continua restrita a emuladores. Não foi alterada nem executada no telemóvel. A recolha física usa navegação ADB e observação de capturas, sem apagar dados. Emulador, TalkBack, TTS offline e regressão completa continuam para as fases aplicáveis do roadmap.

Aviso do javac: MainActivity utiliza APIs obsoletas; a compilação terminou com sucesso.

## APKs de referência

| Variante | Bytes | SHA-256 |
| --- | ---: | --- |
| Original — dist/KanaBridge.apk | 19731191 | 481492D418229D974C794483B3D04F2037C2D5233F9937517A692519E0BAA1A4 |
| Kanateste — dist/Kanateste.apk | 19731190 | E3CDFB163760B15CA47511C69CF0ECB425926E55A95176CA06B165DD48C0E27F |

Original: 19.731191 MB / aproximadamente 18.82 MiB. Kanateste: 19.731190 MB.

A variante **Kanateste** foi autorizada pelo utilizador para coexistir com a preview anterior. Pacote `com.kanabridge.kanateste`, Activity `com.kanabridge.MainActivity`. Instalou e abriu com sucesso. A preview anterior não foi removida.

Para a compilar foi usada uma cópia temporária do script build-apk.ps1 na raiz, executada com -Preview, com três substituições: pacote com.kanabridge.preview → com.kanabridge.kanateste; etiqueta KanaBridge 4 → Kanateste; nome KanaBridge-preview.apk → Kanateste.apk. A cópia foi removida depois do build. Os restantes passos, versão e assinatura são os mesmos. Comparar v5 usando esta mesma variante e processo, e preservar a chave local. APKs, keystore e build permanecem ignorados pelo Git.

## Arranque a frio

Comando: `adb -s <serial> shell am force-stop com.kanabridge.kanateste`, seguido de `adb -s <serial> shell am start -W -n com.kanabridge.kanateste/com.kanabridge.MainActivity`.

| Execução | TotalTime (ms) |
| --- | ---: |
| 1 | 187 |
| 2 | 179 |
| 3 | 175 |
| 4 | 185 |
| 5 | 157 |
| Mediana | **179** |

Primeiro arranque após instalação: 446 ms, excluído da mediana. Todos os cinco arranques reportaram COLD e Status ok. Estes são tempos do Activity Manager, não medição de interatividade ou carregamento do dicionário.

Condições: aparelho ligado por USB e a carregar; dados preservados; sem limpeza de cache ou reinício; execuções consecutivas. Carga térmica e atividade de fundo não controladas. Repetir nas mesmas condições para v5, distinguindo o primeiro arranque de instalação dos seguintes. Logs: build/screenshots/v4-xiaomi/cold-start.txt.

## Referência visual local

Capturas e hierarquias ficam em `build/screenshots/v4-xiaomi`, ignorado pelo Git. As PNG foram inspecionadas visualmente. Os ficheiros funcionam como referência local; não acompanham o commit de documentação.

| Ficheiro PNG | Cenário |
| --- | --- |
| 01-converter | Converter vazio, retrato, tema do sistema escuro |
| 02-kana-dark | Kana, 5 colunas, escuro |
| 03-menu-dark | Menu aberto |
| 04-dictionary-empty-dark | Dicionário vazio |
| 05-dictionary-results-dark | Pesquisa hashi, significados e grafias |
| 06-saved-empty-dark | Guardados vazios |
| 07-reference-dark | Referência portuguesa |
| 08-settings-dark | Definições |
| 09-converter-light | Converter vazio, claro |
| 10-keyboard-light | Conversão com Gboard aberto |
| 11-conversion-light | Resultado hiragana, teclado fechado |
| 12-kana-light | Kana, claro |
| 13-kana-200-light | Kana com font_scale 2.0; tabela adapta-se para uma coluna |
| 14-kana-landscape-light | Paisagem real, 2712 × 1220 |
| 15-converter-200-light | Converter com font_scale 2.0 |

A primeira tentativa de paisagem por settings não rodou o ecrã; a PNG foi substituída por uma captura obtida com `wm user-rotation lock 1` e a rotação foi restaurada para lock 0. A hierarquia XML da primeira tentativa não representa a PNG final e foi descartada.

Escala de texto e rotação foram restauradas. O tema da Kanateste voltou a Seguir o sistema. Não foram apagados rascunhos nem palavras guardadas.

## Inventário para as fases seguintes

### [UI-001] Resultado fora da área visível com teclado aberto
- Ecrã: Converter, tema claro, texto a 100%.
- Prioridade: Média; avaliar também scroll antes de classificar como bloqueio.
- Dispositivo / API: Xiaomi 14T Pro / API 36.
- Passos: abrir Converter; tocar na entrada; escrever irasshaimase; manter Gboard aberto.
- Atual: entrada visível e navegação recolhida, mas o resultado e Copiar/Ouvir ficam abaixo da área disponível; apenas o topo do cartão aparece.
- Esperado: facilitar a consulta do resultado e acesso às ações enquanto se escreve, preservando cursor e scroll.
- Captura antes: 10-keyboard-light.png; comparação sem teclado: 11-conversion-light.png.
- Captura depois / ficheiros alterados / teste de regressão: pendentes.
- Estado: Por fazer (secções 2 e 4).

### [UI-002] Nome interno do tema exposto ao utilizador
- Ecrã: Definições.
- Prioridade: Baixa.
- Dispositivo / API: Xiaomi 14T Pro / API 36.
- Passos: abrir Menu → Definições, com tema do sistema selecionado.
- Atual: botão Aspeto: system, em inglês, ao contrário do resto da interface.
- Esperado: Aspeto: Sistema; também apresentar Claro/Escuro em português.
- Captura antes: 08-settings-dark.png.
- Captura depois / ficheiros alterados / teste de regressão: pendentes.
- Estado: Por fazer (secções 5 e 6).

### [UI-003] Indicação de pesquisa cortada com texto grande
- Ecrã: Kana; indicação longa também observada na Referência a 100%.
- Prioridade: Baixa.
- Dispositivo / API: Xiaomi 14T Pro / API 36, font_scale 2.0.
- Passos: aumentar texto para 200%; abrir Kana; deixar pesquisa vazia.
- Atual: indicação Procurar som ou kana... corta-se na extremidade direita. As células adaptam-se corretamente para uma coluna.
- Esperado: indicação curta e compreensível com texto grande, mantendo exemplos acessíveis por outro meio se necessário.
- Captura antes: 13-kana-200-light.png; 07-reference-dark.png.
- Captura depois / ficheiros alterados / teste de regressão: pendentes.
- Estado: Por fazer (secções 5 e 6).

### [UI-004] Primeira linha de kana quase fora da área visível em paisagem
- Ecrã: Kana.
- Prioridade: Média; problema de ocupação do espaço, não bloqueio confirmado.
- Dispositivo / API: Xiaomi 14T Pro / API 36, paisagem, texto a 100%.
- Passos: abrir Kana; rodar para paisagem; observar topo da tabela antes de fazer scroll.
- Atual: cabeçalho, seleção, pesquisa e navegação ocupam quase toda a altura; só aparece o início dos cartões da primeira linha.
- Esperado: rever alturas e espaçamentos para mostrar conteúdo útil em paisagem, preservando alvos de toque.
- Captura antes: 14-kana-landscape-light.png.
- Captura depois / ficheiros alterados / teste de regressão: pendentes.
- Estado: Por fazer (secções 2 e 5).

Esta lista regista observações reproduzidas; não é uma declaração de ausência de outros bugs. A secção 2 começa após revisão e commit desta base.
