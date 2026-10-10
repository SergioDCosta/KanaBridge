# V5 — Converter: estado e resultados

> Registo desta etapa de desenvolvimento; código incluído no commit 1fccc1d. A uniformização posterior e a validação offline estão registadas em [V5-SECUNDARIOS-CONTRASTE.md](V5-SECUNDARIOS-CONTRASTE.md).
## Alterações deste bloco

- Hiragana, Katakana e Rōmaji reutilizam cartão, texto selecionável e ações ao escrever ou alternar entre estes modos. O texto só é substituído quando o resultado muda.
- Tocar no modo já selecionado não reconstrói os resultados. O estado vazio também evita reconstruções repetidas.
- Copiar e Ouvir usam o texto atual do cartão reutilizado, evitando capturar um valor antigo.
- Seleção, foco e scroll interno da entrada são guardados/restaurados explicitamente na recriação. O scroll da página é restaurado depois do estado das Views.
- Conversão parcial indicada no título do resultado, com explicação detalhada abaixo do cartão e live region acessível.
- Erro real da consulta de kanji usa a cor semântica de erro e live region. A conversão parcial permanece informação, não erro.
- Margens do Converter passam a usar os tokens 4/8 dp com/sem teclado.
- Feedback de cópia existente preservado: confirmação nativa em Android 13+ e Toast curto nas versões anteriores.

O motor de conversão, SQLite, permissões e dependências não mudaram. A escolha/composição de kanji continua a reconstruir o seu conteúdo; a otimização deste bloco abrange os três modos de texto.

## Testes

797 verificações Java e integridade SQLite passaram. Instrumentação compilada, não executada por ausência de emulador. Foram acrescentados testes de identidade da View de resultado ao escrever/tocar no modo selecionado, manutenção de foco, cópia do resultado atual e seleção da entrada após recriação.

Capturas e verificações físicas locais: build/screenshots/v5-converter. O primeiro auxiliar de rotação procurou o cartão fora da área visível; a inspeção com scroll confirmou Kanji escolhido / 日本語 preservados. O percurso foi corrigido para fazer scroll em paisagem.

## APK

Kanateste, com.kanabridge.kanateste, versionName ainda 4.0.0. SHA-256 da revisão final: BC8535EE37EAE858F98F9460FBD9CD5670E1C2387E6181CD3C7205DA99A45925.

Tamanho: 19731190 bytes; diferença de 0 bytes para a Kanateste v4 da mesma variante/processo de build.

## Pendente

Executar as regressões de seleção/foco num emulador; verificar seleção do resultado e scroll em textos longos/200%; validar paginação, composição e detalhes de kanji; TalkBack, TTS offline e Android anterior. A secção 4 permanece em curso, sem promessa de ausência de reconstruções nos fluxos de kanji.

## Resultado físico e comparação

No Xiaomi Android 16: gakkou → がっこう com teclado, Copiar → Limpar → Colar, Rōmaji, conversão parcial q, seleção 日本語, preservação do kanji após rotação com scroll, retorno a Hiragana/Katakana/Rōmaji e estado vazio verificados. A revisão final confirmou também a indicação curta de conversão parcial no tema do sistema escuro. Sete verificações do percurso final passaram; cópia/cola passaram no percurso anterior à interrupção do ADB. Fonte/rotação/tema permanecem restaurados e a app termina no Converter vazio.

O ADB caiu durante uma repetição; servidor restabelecido, Xiaomi novamente autorizado e os cenários restantes repetidos com sucesso. Não foi necessário apagar dados nem substituir a aplicação antiga.

Manifestos do APK original v4 e da Kanateste atual inspecionados com aapt2 dump permissions: nenhum uses-permission em ambos. Sem novas permissões.

| Arranque a frio atual | TotalTime (ms) |
| --- | ---: |
| 1 | 197 |
| 2 | 184 |
| 3 | 153 |
| 4 | 167 |
| 5 | 146 |
| Mediana | 167 |

Mediana da base v4: 179 ms. Diferença observada: -12 ms, cerca de -6,7%. Cinco force-stop/am start -W, mesmo Xiaomi, USB e dados preservados; todos reportaram COLD/Status ok. A atividade de fundo/carga térmica não foi controlada e os momentos de medição diferem: esta amostra não demonstra uma melhoria estatística. Mede Activity Manager, não interatividade completa/dicionário. Arranque quente e fluidez continuam pendentes. Log: build/screenshots/v5-converter/performance.json.
