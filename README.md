# KanaBridge

Aplicação Android simples e offline para converter hiragana e katakana em rōmaji.

## Funcionalidades

- Conversão em tempo real, sem Internet.
- Navegação simples entre conversor, tabela, mini-dicionário e informação da app.
- Tabela interativa completa de hiragana e katakana, com pesquisa por kana ou rōmaji.
- Referência visual de dakuten, handakuten, combinações yōon e kana pequenos.
- Katakana moderno e extensões fonéticas Ainu organizados por categoria.
- Detalhes de cada kana com equivalente no outro silabário e opção de copiar.
- Conversão direta Hiragana ⇄ Katakana, preservando caracteres sem equivalente.
- Rōmaji simples por predefinição e modo opcional com macrons (`gakkou` / `gakkō`).
- Hiragana e katakana, incluindo dakuten, handakuten, kana pequenos, katakana de meia largura e extensões fonéticas Ainu.
- Combinações yōon e katakana moderno (`きゃ`, `ティ`, `ファ`, `ヴァ`, etc.).
- Consoantes duplas com `っ`/`ッ`, vogais prolongadas com `ー` e marcas de repetição.
- Análise carácter a carácter em português e inglês.
- Mini-dicionário offline para palavras frequentes.
- Preserva kanji, letras, números e pontuação que não sejam kana.

## Tabela Kana

A secção **Tabela** reúne o gojūon tradicional, variantes com marcas, combinações e extensões. A pesquisa funciona inteiramente offline e aceita símbolos japoneses ou leituras em rōmaji, como `し`, `shi`, `きゃ` ou `va`.

## Nota linguística

Kana é um sistema fonético. Um carácter como `あ` representa o som **a**, mas não possui uma tradução isolada. O significado pertence normalmente à palavra completa. O mini-dicionário incluído mostra traduções quando reconhece a palavra; não pretende substituir um dicionário japonês completo.

## Instalação

1. Descarrega `KanaBridge.apk`.
2. Abre o ficheiro no Android.
3. Se necessário, permite temporariamente a instalação de aplicações desta fonte.

A aplicação não pede permissões e não recolhe dados.

## Desenvolvimento

O projeto usa Java e APIs Android nativas, sem Gradle, Kotlin ou bibliotecas externas. No Windows, requer Java 21 no `PATH` e o Android SDK em `C:\Android` (platform `android-35` e Build Tools `35.0.0`).

No PowerShell, na raiz do projeto:

```powershell
.\run-tests.ps1
.\build-apk.ps1
```

O APK assinado fica em `dist\KanaBridge.apk`. Na primeira compilação, é criada a chave local reutilizável `tools\kanabridge-release.jks`; não a apagues se quiseres publicar atualizações que possam ser instaladas sobre a app já existente.

Para instalar num telemóvel com a depuração USB ativa:

```powershell
& "C:\Android\platform-tools\adb.exe" install -r .\dist\KanaBridge.apk
```

Compatibilidade: Android 6.0 (API 23) ou superior.
