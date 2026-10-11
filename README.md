# KanaBridge 5.1

Aplicação Android em português para converter rōmaji e consultar kana e palavras japonesas. Simples, offline, sem contas, publicidade ou permissão de Internet.

## Download

[Descarregar KanaBridge.apk](https://github.com/SergioDCosta/KanaBridge/releases/latest/download/KanaBridge.apk) · [Releases e alterações](https://github.com/SergioDCosta/KanaBridge/releases)

No Android, abre o APK e permite a instalação pela aplicação que o abriu. Para atualizar uma instalação existente, a assinatura tem de ser a mesma. Compatibilidade prevista: Android 6 ou superior (API 23); compilada para Android 16 (API 36).

## Novidades da 5.1

A release estável 5.1.0 está disponível no download acima. O dicionário inclui 218 888 entradas, 325 038 pares de grafia/leitura e 253 766 sentidos. A Referência tem 570 entradas em português: 80 estruturas, 160 verbos, 250 termos e 80 expressões.

A pesquisa dispõe de modos japonês/rōmaji, significado em inglês e português parcial, com 356 fichas da Referência ligadas ao JMdict. Os sentidos completos continuam em inglês. Inclui paginação, detalhe por sentidos, ligações à Referência, favoritos com identidade e leitura próprias, filtro dos guardados e até 30 consultas recentes locais. As consultas só são registadas ao abrir um resultado e podem ser eliminadas nas definições.

Quando não há resultados japoneses, algumas flexões conhecidas dos verbos da Referência sugerem a forma de dicionário, mantendo alternativas ambíguas. A Referência inclui sete formas por verbo, explicações de uso e um segundo exemplo em cada uma das 80 estruturas. Esta cobertura é editorial e limitada; não constitui análise morfológica geral nem revisão linguística externa.

A base extraída ocupa cerca de 80,3 MB, abaixo dos 96,1 MB da primeira expansão da 5.1, preservando todas as palavras e sentidos. O APK final ocupa cerca de 28,2 MB, abaixo dos 31,2 MB desse bloco. A base vai dentro do APK comprimida e é extraída localmente na primeira consulta; não é descarregada da Internet. Consulta o [changelog](CHANGELOG.md).

## Funcionalidades

- **Converter:** rōmaji para Hiragana/Katakana, consulta de opções de Kanji e conversão inversa para rōmaji. Permite copiar resultados.
- **Kana:** tabelas Hiragana/Katakana, pesquisa por som ou símbolo e detalhes dos caracteres.
- **Dicionário offline:** JMdict com 218 888 entradas e 325 038 pares de grafia/leitura; sentidos completos em inglês e pesquisa em português com cobertura parcial.
- **Guardados:** palavras favoritas com identidade e leitura próprias, filtro e abertura direta do detalhe.
- **Referência:** 570 entradas em português sobre estruturas, verbos, termos e expressões.
- Temas claro, escuro e sistema; texto acompanha a escala do Android.
- Voz japonesa quando existe um pacote de síntese offline instalado no dispositivo.

Exemplos: `gakkou` → `がっこう`, `matcha` → `まっちゃ`, `ko-hi-` → `コーヒー`. Em Kanji, `nihongo` apresenta `日本語`; palavras ambíguas exigem escolher o significado.

Não é um tradutor automático de frases. Não faz análise sintática nem conjugação automática. Para compor kanji, separa palavras por espaços; as opções são paginadas por 30 e a seleção por segmento abrange as primeiras 40 palavras. Divide textos maiores. Nomes próprios e palavras fora do dicionário podem não ser reconhecidos.

## Privacidade e dados

Rascunhos e guardados ficam no telemóvel; backup automático desativado. A app não solicita Internet, microfone ou câmara. A instalação de uma voz japonesa pelo sistema pode precisar de Internet.

## Validação da 5.1

Passaram 1 378 verificações Java, integridade e hashes SQLite, consultas Java reais com um corpus lexical de 100 pesquisas, estrutura das 570 fichas e 28 verificações de contraste. APK principal e Kanateste compilados e assinaturas verificadas; instrumentação de interface compilada. O probe executado no Xiaomi Android 16 passou 132 verificações do repositório e da migração, confirmando que as preferências reais ficaram iguais. A repetição final passou as mesmas 132 verificações. Em 20 consultas por modo, p95 de 1,19 ms para japonês, 127,59 ms para inglês e 1,07 ms para português; medição do repositório com a base já extraída, sem incluir debounce ou renderização. O probe temporário foi removido. Emulador, TalkBack e TTS foram dispensados pelo utilizador. No Xiaomi, a interface final confirmou pesquisa PT, sentidos, ligação à Referência e retorno, recuperação das consultas recentes e pesquisa após reiniciar a app com Wi-Fi/dados desligados. Este ensaio utilizou a base já extraída; não foi uma instalação limpa offline. Não foi validada uma matriz ampla de dispositivos.

## Compilar e testar no Windows

Requer JDK 17/21, Android SDK com plataforma API 36, Build Tools 36.0.0 e Python 3 para a compressão do APK e os testes SQLite/Referência/contraste. Define `ANDROID_SDK_ROOT` para a localização do SDK.

```powershell
.\run-tests.ps1
python tests/lexicon_test.py
python tests/dictionary_search_test.py
python tests/reference_test.py
python tests/ui_contrast_test.py
.\build-apk.ps1
.\run-android-tests.ps1 -CompileOnly
```

O APK fica em `dist/KanaBridge.apk`. `build-apk.ps1 -Kanateste` compila a variante independente Kanateste; `-Preview` cria outra variante de desenvolvimento. `run-dictionary-device-tests.ps1 -Serial <serial>` instala temporariamente o probe de diagnóstico, testa o repositório e a migração com preferências separadas e remove-o no fim. O Android pode pedir autorização para essa instalação. Os testes Android executáveis recusam dispositivos físicos; num emulador, limpam os dados da app de teste.

A chave local `tools/kanabridge-release.jks` é reutilizada e ignorada pelo Git. Preserva-a para assinar futuras atualizações; APKs compilados com outra chave não atualizam diretamente as instalações existentes.

## Dicionário e atribuições

JMdict © James William Breen e Electronic Dictionary Research and Development Group (EDRDG). A base SQLite derivada é disponibilizada sob CC BY-SA 4.0; as modificações incluem extração, normalização de leituras, restrições de grafia/sentido e ordenação por prioridade. Metadados em `app/src/main/assets/lexicon-source.json`; licenças nos assets e em Menu → Fontes e licenças. A referência em português é original da app.

[Fonte JMdict](https://www.edrdg.org/pub/Nihongo/JMdict_e.gz) · [Condições EDRDG](https://www.edrdg.org/edrdg/licence.html) · [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

Para reconstruir a base a partir da fonte:

```powershell
New-Item -ItemType Directory -Force .tooling/dictionary | Out-Null
Invoke-WebRequest https://www.edrdg.org/pub/Nihongo/JMdict_e.gz -OutFile .tooling/dictionary/JMdict_e.gz
python tools/build-dictionary.py
python tests/lexicon_test.py
.\build-apk.ps1
```

## Estrutura

- `app/`: código Java, recursos, dicionário e licenças.
- `tests/`: testes Java, Android, SQLite e contraste.
- `tools/build-dictionary.py`: reconstrução do dicionário.
- Scripts PowerShell na raiz: compilação e testes.

Os documentos históricos de desenvolvimento estão preservados no histórico Git e na [tag v5.0.0](https://github.com/SergioDCosta/KanaBridge/tree/v5.0.0/docs).
