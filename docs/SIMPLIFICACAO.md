# Simplificação implementada — versão 4

7 de outubro de 2026. Base da implementação: `c2347c6` (versão 3). Esta especificação descreve a implementação da versão 4 e substitui a proposta anterior com três separadores, por instrução do utilizador. Os APKs são artefactos locais de desenvolvimento; a publicação do código não cria uma release com APKs automaticamente.

## Resultado

Dois destinos principais: **Converter** (inicial) e **Kana**. Descobrir, lições, coleções educativas, quizzes, revisão espaçada, pontuações e progresso foram removidos da interface e do código. Os favoritos existentes continuam acessíveis como Guardados. Preferências que apontavam para destinos removidos passam a abrir Converter.

O conversor recebe rōmaji e japonês. Quatro opções mostram um resultado de cada vez: Hiragana, Katakana, Kanji ou Rōmaji. Inclui sokuon, dakuten, handakuten, yōon, kana pequenos, variantes de digitação, n separado e vogais longas explícitas. Copiar e ouvir estão junto ao resultado.

Kanji exige seleção lexical explícita. Foi incluído o JMdict offline: 218 869 entradas, 325 004 pares grafia/leitura, com significados ingleses, restrições do dicionário e ordenação por prioridade. Pesquisa indexada fora da thread de interface; instalação atómica da base por digest da fonte; respostas obsoletas descartadas. Composição por palavras separadas por espaços, preservando o espaço original, sem análise sintática ou conjugação. Frases contínuas com kanji e texto desconhecido mantêm os caracteres e indicam conversão parcial.

Kana tem alternância Hiragana/Katakana, categorias com descrições e pesquisa. A tabela usa LinearLayouts sem pesos verticais, o ScrollView não força o preenchimento da altura e a margem acima da tabela é de 8 dp. Texto grande transforma a grelha numa lista de uma ou duas colunas. Células têm altura mínima de 72 dp e nomes acessíveis.

O teclado é tratado por `adjustResize`, insets de sistema/IME e callback de animação no Android 11+. O espaço disponível encolhe, a barra inferior recolhe e o campo pede que o retângulo do cursor fique visível. Digitação atualiza apenas o resultado, sem recriar o campo ou retirar o foco.

A única referência linguística está no Menu e é opcional: **290 entradas originais em português**, divididas em 26 estruturas, 104 verbos, 125 termos e 35 expressões. Inclui pesquisa e paginação, áudio e cópia. Não há sessões, metas nem exercícios.

## Verificação e limites

- **Passa:** 73 verificações existentes de conversão e 724 de conversão inversa/referência (797 no total). As leituras das frases de referência usam explicitamente os sons das partículas は → wa, へ → e e を → o, sem substituir estes símbolos dentro de palavras.
- **Passa:** integridade SQLite, contagem e consultas lexicais de exemplo; licenças e metadados de origem incluídos.
- **Passa:** compilação API 36 da app e do APK de testes; assinatura e metadados dos APKs normal e preview verificados na entrega local.
- **Passa:** 40 verificações Android por execução em retrato, texto a 200% e paisagem, incluindo confirmação explícita da orientação. Emulador oficial Android 16, configurado com 1220 × 2712 px / 480 dpi. Capturas inspecionadas; tabela sem o espaço excessivo, texto grande legível, cursor acima do teclado virtual real. O primeiro arranque demorou cerca de dois minutos; a inicialização WHPX acabou por concluir.
- **Pendente:** execução no Xiaomi físico / HyperOS 3, leitura manual com TalkBack e áudio com voz japonesa instalada. Não foi feita uma execução num telemóvel físico.

Os testes Android foram reescritos para esta interface. Incluem escolha de kanji e composição por palavras, preservação de partículas/espaços após recriação, pesquisa, ausência do espaço vazio acima da tabela, alvos de toque, abertura real do teclado e cursor visível. A opção `-CompileOnly` verifica apenas a compilação. A suite foi executada em modo normal, texto grande e paisagem. Foi corrigido um problema real identificado nos testes: a restauração automática do EditText apagava escolhas de kanji mesmo sem alteração do rascunho. No Xiaomi 14T Pro, confirmar ainda navegação por gestos e três botões, Gboard/teclado habitual e tema do sistema.

Os APKs são de desenvolvimento local; a variante preview coexiste com a app original. A assinatura original do repositório não estava disponível, pelo que não se promete atualização por cima de uma instalação assinada com outra chave. Guardados e rascunho continuam locais; backup desativado.

## Manutenção do JMdict

Base derivada CC BY-SA 4.0, com atribuição à EDRDG/James William Breen nos assets, README e ecrã Fontes e licenças. `tools/build-dictionary.py` reconstrói a base a partir do gzip oficial e regista digest, contagens e transformações. Atualizar antes de publicar cada release e verificar periodicamente a fonte; a base não se atualiza por rede na app, que continua sem permissão de Internet.

Não incluído: tradução automática de frases, OCR, furigana de frases, análise morfológica, teclado de sistema ou previsão de texto. As sugestões de kanji reconhecem entradas lexicais; não inferem o significado pretendido.
