# KanaBridge 4

> **V5 em desenvolvimento:** a branch `feature/v5-ui-ux` contém correções visuais, design partilhado e melhorias do Converter. A versão do APK continua 4.0.0 até à validação de lançamento. Estado e limites em [KANABRIDGE-V5-ROADMAP.md](docs/KANABRIDGE-V5-ROADMAP.md); alterações em [CHANGELOG.md](CHANGELOG.md). As validações históricas da v4 abaixo não equivalem à execução da instrumentação das alterações v5.

Conversor e consulta de japonês para Android, com dois ecrãs principais: **Converter** e **Kana**. Sem Descobrir, lições, exercícios, pontuações ou revisão agendada. Android 6 / API 23 até Android 16 / API 36; interface em português.

## Estado atual — 7 de outubro de 2026

A versão **4.0.0** está implementada no código de `main`. A app centra-se na conversão de rōmaji e na consulta de kana e palavras; a referência de japonês é opcional, no Menu.

| Área | Estado |
| --- | --- |
| Conversão e referência | 797 verificações Java passam, incluindo consoantes duplas como `irasshaimase` → `いらっしゃいませ`. |
| Dicionário offline | SQLite validado: integridade, exemplos lexicais, índices e atribuição das fontes. |
| Interface Android 16 | 40 verificações por execução em emulador, com retrato, texto a 200% e paisagem; tabela e teclado verificados em capturas. |
| Compilação | APK original e preview compilados e assinados localmente com chave de desenvolvimento. Esta entrega não publica APKs numa release do GitHub. |
| Xiaomi 14T Pro / HyperOS 3 | Adaptado para Android 16; a validação no aparelho físico continua pendente, incluindo TalkBack e voz japonesa offline. |

As próximas verificações são instalar a preview no Xiaomi, confirmar o comportamento do teclado e das barras do HyperOS e testar a navegação com TalkBack e a síntese de voz japonesa. A compatibilidade no emulador não substitui estes testes no dispositivo.

## Converter

Escreve rōmaji ou cola japonês e escolhe o resultado: **Hiragana · Katakana · Kanji · Rōmaji**. Cada resultado permite copiar; japonês também permite ouvir com uma voz offline instalada.

- `irasshaimase` → `いらっしゃいませ` / `イラッシャイマセ`.
- `gakkou` → `がっこう`; `matcha` ou `maccha` → `まっちゃ`.
- Dakuten e handakuten: `ga` → `が`, `pa` → `ぱ`. Combinações: `kya` → `きゃ`, `sha` → `しゃ`.
- Kana pequenos: `xa` → `ぁ`, `xya` → `ゃ`, `xtsu` / `ltsu` → `っ`.
- Nasal: `kan'i` → `かんい`; `shin'you` → `しんよう`; `nna` → `んな`; `nn` no fim → `ん`.
- Variantes de entrada: `shi` / `si`, `chi` / `ti`, `tsu` / `tu`, `fu` / `hu`. Maiúsculas, kana de meia largura e dakuten combinado são normalizados.
- `ko-hi-` → `コーヒー`. Escreve vogais longas pela grafia japonesa (`ou`, `oo`, `uu`, `ei`); `ō` usa `ou` por convenção e não distingue `おう` de `おお`.
- Saudações convencionais: `konnichiwa` → `こんにちは`, `konbanwa` → `こんばんは`. Não há substituição global de partículas: tema `は` escreve-se `ha`, direção `へ` escreve-se `he`, objeto `を` escreve-se `wo`.

O dicionário **JMdict**, inteiramente offline, contém **218 869 entradas** e **325 004 pares de grafia/leitura** neste snapshot. `nihongo` → `にほんご` apresenta `日本語`; `hashi` apresenta alternativas com significados diferentes, como ponte e pauzinhos. **A escolha é explícita, pelo significado; as definições do JMdict estão em inglês.** Mantém uma opção premida para abrir os detalhes completos, ouvir e guardar.

Para compor uma frase com kanji, separa as palavras por espaços e escolhe a grafia de cada palavra. A app preserva os espaços e mudanças de linha. São apresentadas as primeiras 40 palavras para seleção por segmento; divide textos maiores. Não existe análise sintática nem conjugação automática. As opções de kanji são paginadas por 30, sem ocultar as restantes.

**Não é um tradutor automático de frases.** Sons iguais podem ter significados e grafias diferentes. Nomes próprios, flexões e palavras fora do dicionário podem não ser reconhecidos. Ao colar kanji, abre Kanji para escolher a leitura de uma palavra reconhecida; frases contínuas com kanji não recebem furigana nem leitura integral automática. Segmentos não reconhecidos são mantidos e a conversão parcial é indicada.

## Kana e acessibilidade

- Alternância direta Hiragana / Katakana; tabelas básicas, dakuten, combinações, pequenos kana e referência avançada.
- Gojūon alinhado por vogais; pesquisa por som ou símbolo, detalhes, copiar, ouvir e usar no conversor.
- Linhas com altura natural, sem pesos verticais nem GridLayout: a tabela começa imediatamente abaixo da pesquisa, com margem de 8 dp.
- Botões com alvo mínimo de 52 dp; células de kana de pelo menos 72 dp e descrições para TalkBack. Estado selecionado exposto ao leitor de ecrã.
- Texto segue a escala do Android; com texto grande a tabela muda para duas ou uma coluna.
- Temas claro, escuro e sistema; margens para barras, recortes e teclado.
- `adjustResize` e insets/animação do IME no Android 11+: a navegação recolhe ao abrir o teclado e o cursor é trazido para a área visível, incluindo campos com várias linhas. Em versões anteriores usa redimensionamento da janela.

As extensões Ainu são uma referência gráfica; não são um conversor fonológico completo dessa língua.

## Consulta opcional

No **Menu**: Dicionário offline, Guardados, Referência de japonês, Definições e Fontes/licenças. Nenhuma consulta obriga a fazer exercícios.

A referência original em português tem **290 entradas pesquisáveis**: **26 estruturas de frase, 104 verbos, 125 termos e 35 expressões**. Cobre quem, o quê, quando, onde, porquê, partículas, pedidos, convites, tratamento, passado e negativo. Os verbos incluem forma de dicionário, forma polida, forma て e passado simples, com notas nas diferenças relevantes. Pesquisa por português, rōmaji ou japonês; lista paginada para manter o ecrã leve.

## Áudio e dados

Ouvir usa síntese de voz japonesa instalada no dispositivo que declare funcionamento offline. A instalação da voz pode precisar de Internet. Sem permissão de Internet, conta, microfone ou câmara na app. Rascunho e guardados ficam no telemóvel; backup automático desativado. Favoritos antigos são preservados; dados antigos de revisão deixam de ser usados.

## APKs e Xiaomi 14T Pro / HyperOS 3

Compilação para Android 16 / API 36. Os APKs locais ficam em `dist`, ignorados pelo Git:

- `KanaBridge-preview.apk`: **KanaBridge 4**, pacote `com.kanabridge.preview`; pode coexistir com a versão original.
- `KanaBridge.apk`: pacote original `com.kanabridge`; atualizar uma instalação existente requer a mesma chave de assinatura.

Abre o APK no telemóvel e permite a instalação pela aplicação que usaste para abrir o ficheiro, se solicitado pelo Android. A chave disponível é uma chave de desenvolvimento local; se a instalação anterior tiver outra assinatura, usa a variante preview para preservar os dados antigos. A chave local é reutilizada e está ignorada pelo Git.

**Validação:** passam 73 verificações existentes e 724 da conversão inversa/referência (**797 no total**), integridade SQLite e consultas lexicais. A suite Android passa **40 verificações por execução**, em retrato, texto a **200%** e paisagem. Foram inspecionadas capturas reais; a tabela fica junto à pesquisa e o cursor permanece acima de um teclado virtual com altura real. A compilação e a assinatura dos APKs passam. O Xiaomi físico, HyperOS 3, leitura manual com TalkBack e voz japonesa instalada ainda precisam de validação. Ver [SIMPLIFICACAO.md](docs/SIMPLIFICACAO.md).

## Desenvolvimento Windows

JDK 17/21, plataforma API 36 e Build Tools 36.0.0. O SDK é procurado em `ANDROID_SDK_ROOT`, `.tooling/android-sdk` ou `C:\Android`. Os scripts usam caminhos relativos para contornar limitações de ferramentas Android com acentos.

```powershell
.\run-tests.ps1
python tests/ui_contrast_test.py
.\build-apk.ps1
.\build-apk.ps1 -Preview
.\run-android-tests.ps1 -CompileOnly
# Com emulador Android 16 iniciado:
.\run-android-tests.ps1 -Serial emulator-5556
.\run-android-tests.ps1 -Serial emulator-5556 -Orientation landscape -ScreenshotFolder android16-landscape
```

Os testes Android recusam dispositivos físicos e limpam os dados apenas da app no emulador. Cobrem navegação, conversão inversa, escolha/composição de kanji, recriação, pesquisa, geometria da tabela, abertura real do teclado e visibilidade do cursor. Capturas ficam em `build/screenshots/android16`; as outras execuções desta entrega estão em `android16-large-text` e `android16-landscape`. Para emuladores com teclado físico, ativa o teclado virtual nas definições de entrada antes de executar. A suite exige insets do teclado superiores a 100 dp, para não aceitar um teclado lógico sem área visível.

## Atualizar o dicionário e licenças

JMdict © James William Breen e Electronic Dictionary Research and Development Group, **CC BY-SA 4.0**. A base SQLite é um derivado sob a mesma licença; as modificações são extração, normalização das leituras, aplicação das restrições de grafia/sentido e ordenação por prioridade. Metadados e digest SHA-256 estão em `app/src/main/assets/lexicon-source.json`; licenças incluídas nos assets e acessíveis no Menu → Fontes e licenças.

Fonte: [JMdict oficial](https://www.edrdg.org/pub/Nihongo/JMdict_e.gz), [condições EDRDG](https://www.edrdg.org/edrdg/licence.html), [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).

Atualiza a base antes de cada release e verifica periodicamente a fonte oficial:

```powershell
New-Item -ItemType Directory -Force .tooling/dictionary | Out-Null
Invoke-WebRequest https://www.edrdg.org/pub/Nihongo/JMdict_e.gz -OutFile .tooling/dictionary/JMdict_e.gz
python tools/build-dictionary.py
python tests/lexicon_test.py
.\build-apk.ps1
```

O instalador interno usa o digest da fonte para substituir automaticamente a base local quando uma atualização da app incluir novos dados. A instalação e a pesquisa correm fora da thread da interface. Guarda a chave `tools/kanabridge-release.jks` para atualizar estes APKs; uma publicação em loja requer gestão de assinatura de produção.

## Ficheiros principais

- `MainActivity.java`: navegação, conversor, tabelas, consulta e teclado.
- `RomajiConverter.java` / `KanaTransliterator.java`: conversão inversa e leitura.
- `KanaData.java` / `KanaEntry.java`: catálogo de símbolos.
- `OfflineLexicon.java`: instalação e consulta indexada do JMdict.
- `JapaneseReference.java`: referência portuguesa opcional.
- `SavedWords.java`, `JapaneseSpeech.java`, `Ui.java`: guardados, áudio e interface.

Este README descreve o estado atual. [SIMPLIFICACAO.md](docs/SIMPLIFICACAO.md) detalha a implementação da versão 4. [ANALISE-E-ROADMAP.md](docs/ANALISE-E-ROADMAP.md) conserva a análise da versão 2 e [VALIDACAO-ANDROID16.md](docs/VALIDACAO-ANDROID16.md) o relatório histórico da versão 3; as propostas antigas foram substituídas pela simplificação atual.
