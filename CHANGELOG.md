# Changelog

## Em desenvolvimento — 5.1.0

- Dicionário e Referência acessíveis na navegação principal; ecrã inicial configurável.
- Detalhe de palavra em ecrã completo, com copiar palavra/leitura, guardar e retorno à consulta.
- Pesquisa japonesa combina correspondências exatas e prefixos de leitura/grafia, preservando a prioridade das exatas.
- Modo explícito de pesquisa nas definições em inglês; caracteres especiais tratados como texto literal.
- Mais resultados são acrescentados por páginas; consultas obsoletas em fila são descartadas.
- Testes de integração executam as consultas Java reais contra a base SQLite distribuída.
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
