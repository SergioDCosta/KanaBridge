# KanaBridge 3

Uma porta de entrada no japonês: descobrir, compreender e praticar. Aplicação Android nativa, com conteúdo e progresso locais, preparada para Android 16 / API 36 e compatibilidade declarada desde Android 6 / API 23.

## O que inclui

- Navegação persistente: **Descobrir · Kana · Praticar · Ferramentas**.
- Primeiro percurso pelas cinco vogais, com equivalentes em katakana e desafio.
- Coleções sobre comida, viagens, saudações e cultura pop; curiosidades ligadas a exemplos.
- Tabela gojūon por linhas, grupos de kana, pesquisa, detalhes e favoritos.
- Exercícios de vogais, hiragana, katakana, kana parecidos e pequeno `っ`.
- Prática de coleções, favoritos e itens agendados para revisão, até dez perguntas por sessão.
- Revisão local: erros voltam após dez minutos; acertos em intervalos progressivos de 1, 3, 7, 14, 30 e 60 dias. Sem notificações nem serviço em segundo plano.
- Leitura em rōmaji com explicações por segmento, incluindo **いらっしゃいませ → irasshaimase**. Tocar em `っ` explica a breve pausa que prepara a consoante seguinte e o `s` adicional antes de `sha`.
- Leituras contextuais de `こんにちは → konnichiwa` e `こんばんは → konbanwa`; mensagem clara para resultados parciais.
- Conversão Hiragana ⇄ Katakana, sinais de repetição corrigidos e suporte a katakana de meia largura.
- Dicionário com pesquisa tolerante a acentos, macrons e largura: `cafe`, `gakkō` e `ｺｰﾋｰ`.
- Português por defeito; inglês opcional nas explicações. Rōmaji da tabela pode ser ocultado.
- Temas claro, escuro e automático; texto redimensionável, botões com altura mínima de 52 dp e margens para barras, recortes e teclado.
- Estado de ecrã e exercício restaurado após recriação da Activity; rascunho do conversor, favoritos e revisão persistidos no dispositivo.

## Áudio e privacidade

O botão **Ouvir** usa uma voz japonesa de síntese instalada no telemóvel que declare funcionamento offline. Não são gravações humanas. Quando a voz não está disponível, a aplicação explica como instalá-la nas definições de síntese de voz; o download dessa voz pode requerer Internet. Depois de instalada, reabre a KanaBridge.

A aplicação não pede permissão de Internet, conta, microfone ou câmara. Favoritos, rascunho e progresso são locais; o backup automático está desativado. Desinstalar a aplicação elimina estes dados. Não há exportação de progresso nesta versão.

## Limites linguísticos

Kana representa sons; o significado depende da palavra e do contexto. O mini-dicionário não traduz frases. Palavras em kanji só recebem leitura quando a entrada completa é reconhecida; outros segmentos são preservados e assinalados. As correções de saudações não são uma substituição global de partículas.

O estilo com macrons trata `ー` e leituras longas registadas, como `学校 → gakkō` e `すうがく → sūgaku`. Outras sequências de vogais são conservadas para evitar inferir fronteiras e pronúncias sem análise lexical. A leitura dos segmentos explica a construção; nem sempre a concatenação visual dos segmentos é igual à palavra formatada com macrons.

As extensões Ainu são referência gráfica complementar. Não constituem um conversor fonológico completo da língua Ainu. A aplicação não inclui ainda animações da ordem dos traços, furigana de frases, OCR ou conversação com IA.

## Instalar no Xiaomi 14T Pro / HyperOS 3

Foram gerados dois APKs na pasta `dist`:

- **KanaBridge-preview.apk** — aparece como **KanaBridge 3**, com identificador `com.kanabridge.preview`. Pode coexistir com a versão antiga; recomendado para experimentar esta compilação sem a substituir.
- **KanaBridge.apk** — identificador original `com.kanabridge`. Uma atualização por cima de um APK existente exige a mesma chave de assinatura da instalação antiga.

Abre o APK no telemóvel e, se o Android solicitar, permite a instalação a partir da aplicação que usaste para abrir o ficheiro. Esta compilação usa uma chave local nova: a chave de assinatura original não estava no repositório. Não desinstales uma versão com dados importantes apenas para ultrapassar uma incompatibilidade de assinatura; usa a variante paralela.

A compilação, assinatura e metadados API 36 foram verificados. O emulador local não terminou o arranque, pelo que a validação visual e os testes instrumentados permanecem pendentes. Não foi feita uma execução num Xiaomi físico. Consulte [VALIDACAO-ANDROID16.md](docs/VALIDACAO-ANDROID16.md).

## Desenvolvimento no Windows

Java 17 ou 21 no PATH, plataforma `android-36` e Build Tools `36.0.0`. O script procura o SDK em `ANDROID_SDK_ROOT`, `.tooling/android-sdk` ou `C:\Android`. Todas as ferramentas e imagens descarregadas para esta tarefa estão em `.tooling`, ignorada pelo Git; não são incluídas no APK.

```powershell
.\run-tests.ps1
.\build-apk.ps1
.\build-apk.ps1 -Preview
```

Os scripts usam argumentos relativos ao projeto para contornar limitações de ferramentas Android com caminhos Windows que contêm acentos. A chave local reutilizável fica em `tools/kanabridge-release.jks`, ignorada pelo Git. Preserva essa chave para futuras atualizações dos APKs desta compilação. A configuração de assinatura incluída destina-se a desenvolvimento local; prepara a gestão da chave de produção antes de publicar numa loja.

### Testes Android opcionais

Com um emulador API 36 já iniciado e `adb` disponível no SDK:

```powershell
.\run-android-tests.ps1 -Serial emulator-5556
```

O script recusa identificadores de telemóveis físicos. Os testes limpam apenas os dados da aplicação no emulador, instalam os dois APKs de aplicação/teste e verificam navegação, pesquisa, saudações, texto após recriação, prática e modo escuro. Quando executados com sucesso, as capturas são copiadas para `build/screenshots/android16`. Não foram concluídos neste ambiente devido à falha de arranque do emulador.

## Estrutura

- `KanaData`, `KanaEntry`, `KanaTransliterator`: catálogo, conversão e pesquisa sem dependências Android.
- `LearningContent`: percursos e conteúdo pedagógico incluído.
- `StudySession`, `ReviewSchedule`: perguntas reproduzíveis e regras de revisão testáveis.
- `LocalProgress`: armazenamento local de favoritos e progresso.
- `JapaneseSpeech`: seleção de voz japonesa offline e tratamento de indisponibilidade.
- `Ui`, `MainActivity`: componentes visuais, navegação, estado e integração Android.

O [relatório inicial](docs/ANALISE-E-ROADMAP.md) contém o diagnóstico da versão 2.0 e as fases futuras; não descreve o estado atual da versão 3.
