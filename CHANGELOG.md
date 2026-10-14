# Changelog

## 5.2.0 — 2026-10-14

- Novo separador **Iene** com conversor direto euro ⇄ iene, a partir da taxa de referência do Banco Central Europeu (serviço Frankfurter, endpoint `/v2/rate/EUR/JPY`). O resultado atualiza-se enquanto se escreve; aceita `12,5`, `12.5` e `1 234,50`.
- A taxa é pedida ao abrir o separador (no máximo uma vez por hora) ou em *Atualizar taxa*; a última taxa e a sua data ficam guardadas para uso sem ligação. Se o pedido falhar, mantém-se a taxa guardada e a interface explica-o.
- Acrescentada a permissão `INTERNET`, usada apenas por este pedido. O ecrã Fontes regista a origem das taxas.
- Dicionário limitado a japonês/rōmaji e significado em inglês: removidos o modo de pesquisa em português, as pré-visualizações em português e a ligação do detalhe de palavra à Referência. Consultas recentes em português guardadas pela 5.1 são ignoradas.
- Removido o código sem uso que ligava o dicionário à Referência. A secção Referência de japonês não foi alterada.
- Testes: 51 verificações do conversor de câmbio (interpretação da resposta, montantes, arredondamentos, formatação); testes do dicionário e do histórico ajustados ao fim do modo português.
- Assinatura: a chave das releases 5.0.0 e 5.1.0 (SHA-256 `6b3788…0a77`) não estava disponível; a 5.2.0 é assinada com a chave `cc4971…ab3d`. Consequência: não atualiza por cima da 5.1 e exige desinstalar primeiro, com perda dos guardados e do histórico locais. Daqui em diante manter `tools/kanabridge-release.jks` (fora do Git) e fazer cópia de segurança.
- APK principal 5.2.0 (versionCode 7).

## 5.1.0 — 2026-10-11

- Dicionário e Referência acessíveis na navegação principal; ecrã inicial configurável.
- Detalhe de palavra em ecrã completo, com copiar palavra/leitura, guardar e retorno à consulta.
- Pesquisa japonesa combina correspondências exatas e prefixos de leitura/grafia, preservando a prioridade das exatas.
- Modo explícito de pesquisa nas definições em inglês; caracteres especiais tratados como texto literal.
- Mais resultados são acrescentados por páginas; consultas obsoletas em fila são descartadas.
- Testes de integração executam as consultas Java reais contra a base SQLite distribuída.
- Base JMdict atualizada: 218 888 entradas, 325 038 pares e 253 766 sentidos separados, com classes gramaticais, notas de uso e leituras originais.
- Instalação da base validada por esquema, integridade e SHA-256 antes da substituição da versão anterior.
- Referência num asset estruturado com 570 entradas: 80 estruturas, 160 verbos, 250 termos e 80 expressões; temas, rōmaji, contexto de uso, sete formas por verbo e um segundo exemplo por estrutura.
- Pesquisa em português com 356 fichas ligadas ao JMdict e tolerância a acentos; cobertura explicitamente parcial, mantendo os sentidos completos em inglês.
- Sugestões de forma de dicionário para flexões conhecidas da Referência, preservando alternativas ambíguas.
- Favoritos identificam entrada, grafia e leitura; migração conserva favoritos antigos ambíguos até escolha explícita. Filtro local e abertura direta do detalhe.
- Histórico local limitado a 30 consultas selecionadas; limpeza nas definições e ligações do detalhe à Referência.
- Base reduzida de 96,1 para 80,3 MB através da partilha de metadados repetidos, preservando todas as palavras e sentidos; APK final cerca de 28,2 MB (antes: 31,2 MB nesta expansão).
- Hash calculado durante a extração, sem reler toda a base; instalação serializada entre instâncias e carregamento da Referência em segundo plano com índices reutilizados.
- Validação: 1 378 verificações Java, SQLite, consultas/paginação/corpus lexical, estrutura editorial e 28 contrastes aprovados. Probe no Xiaomi: 132 verificações aprovadas, incluindo preservação dos dados reais. Probe final aprovado: p95 de consulta JP 1,19 ms, EN 127,59 ms e PT 1,07 ms, com base já extraída; ferramenta temporária removida. Interface final no Xiaomi verificada em PT/detalhe/Referência/retorno e consultas recentes, incluindo pesquisa offline após reiniciar o processo com base já extraída. APKs assinados e instrumentação de interface compilada.

- APK principal 5.1.0 (versionCode 6), assinado e publicado na release GitHub; variante Kanateste excluída da release.

## 5.0.0 — 2026-10-10

- Corrigida a ocupação do Converter com teclado e a primeira linha Kana em paisagem larga.
- Corrigidas indicações de pesquisa cortadas e nomes internos dos temas expostos na interface.
- Introduzidos componentes/tokens partilhados e contornos com maior contraste nos dois temas.
- Converter reutiliza cartões nos modos de texto e restaura seleção/foco/scroll da entrada na recriação.
- Conversão parcial indicada no título; feedback acessível para informação e erros de consulta.
- Melhorados os estados vazios de Guardados/Referência e descrições dos resultados do dicionário.
- Acrescentados testes de contraste e regressões Android. Java/SQLite aprovados; instrumentação v5 compilada, execução pendente.
- Verificados dicionário e palavras guardadas após arranque a frio com Wi-Fi/dados desligados.
- APK 5.0.0 (versionCode 5), assinado e disponibilizado na release GitHub.
- Aceitação centrada no Xiaomi Android 16. Emulador, TalkBack e TTS dispensados pelo utilizador; matriz ampla e validação de paginação/composição de kanji e textos longos permanecem limitações de validação.
- Telefone indisponível no fecho: instalação do APK final pendente; testes físicos anteriores documentados nos relatórios da tag v5.0.0.

Registos detalhados de desenvolvimento preservados na tag v5.0.0 (pasta docs).
