# KanaBridge 5

Aplicação Android em português para converter rōmaji e consultar kana e palavras japonesas. Simples, offline, sem contas, publicidade ou permissão de Internet.

## Download

[Descarregar KanaBridge.apk](https://github.com/SergioDCosta/KanaBridge/releases/latest/download/KanaBridge.apk) · [Releases e alterações](https://github.com/SergioDCosta/KanaBridge/releases)

No Android, abre o APK e permite a instalação pela aplicação que o abriu. Para atualizar uma instalação existente, a assinatura tem de ser a mesma. Compatibilidade prevista: Android 6 ou superior (API 23); compilada para Android 16 (API 36).

## Funcionalidades

- **Converter:** rōmaji para Hiragana/Katakana, consulta de opções de Kanji e conversão inversa para rōmaji. Permite copiar resultados.
- **Kana:** tabelas Hiragana/Katakana, pesquisa por som ou símbolo e detalhes dos caracteres.
- **Dicionário offline:** JMdict com 218 869 entradas e 325 004 pares de grafia/leitura; definições em inglês.
- **Guardados:** palavras favoritas armazenadas no telemóvel.
- **Referência:** 290 entradas em português sobre estruturas, verbos, termos e expressões.
- Temas claro, escuro e sistema; texto acompanha a escala do Android.
- Voz japonesa quando existe um pacote de síntese offline instalado no dispositivo.

Exemplos: `gakkou` → `がっこう`, `matcha` → `まっちゃ`, `ko-hi-` → `コーヒー`. Em Kanji, `nihongo` apresenta `日本語`; palavras ambíguas exigem escolher o significado.

Não é um tradutor automático de frases. Não faz análise sintática nem conjugação automática. Para compor kanji, separa palavras por espaços; as opções são paginadas por 30 e a seleção por segmento abrange as primeiras 40 palavras. Divide textos maiores. Nomes próprios e palavras fora do dicionário podem não ser reconhecidos.

## Privacidade e dados

Rascunhos e guardados ficam no telemóvel; backup automático desativado. A app não solicita Internet, microfone ou câmara. A instalação de uma voz japonesa pelo sistema pode precisar de Internet.

## Versão 5 e validação

Interface refinada, melhor utilização com teclado, componentes visuais partilhados, contraste reforçado e preservação de seleção/foco da entrada no Converter. Consulta o [changelog](CHANGELOG.md).

Passaram 797 verificações Java, integridade SQLite e 28 verificações de contraste. Fluxos de interface, dicionário e guardados offline foram verificados num Xiaomi Android 16 antes do ajuste final de versão. APK final compilado e assinatura verificada; instalação desse APK ainda pendente. A instrumentação v5 foi compilada, sem execução. Emulador, TalkBack e TTS foram dispensados nesta entrega. Outros dispositivos, paginação/composição de kanji e textos longos não receberam validação completa.

## Compilar e testar no Windows

Requer JDK 17/21, Android SDK com plataforma API 36, Build Tools 36.0.0 e Python para os testes SQLite/contraste. Define `ANDROID_SDK_ROOT` para a localização do SDK.

```powershell
.\run-tests.ps1
python tests/lexicon_test.py
python tests/ui_contrast_test.py
.\build-apk.ps1
.\run-android-tests.ps1 -CompileOnly
```

O APK fica em `dist/KanaBridge.apk`. `build-apk.ps1 -Preview` compila uma variante independente. Os testes Android executáveis recusam dispositivos físicos; num emulador, limpam os dados da app de teste.

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
