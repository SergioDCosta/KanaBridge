# V5 — ecrãs secundários e contraste

## Implementação

- Controlos, cartões, campos, margens e legendas partilhados em Converter, Kana, Dicionário, Guardados, Referência, Definições e Fontes/licenças. Removida a variante temporária de estilo dos dois primeiros ecrãs.
- Cor de destaque centralizada nos temas nativos e obtida por Ui a partir de colorAccent; diálogos e cursores nativos acompanham a mesma paleta.
- Contornos reforçados: claro #8B8790, escuro #75828F. Foco de botão selecionado usa onAccent; ripple discreto com alpha 32/255.
- Pesquisa do dicionário com indicação curta Pesquisar palavra e descrição acessível com exemplos.
- Resultados truncados do dicionário mantêm descrição acessível completa e indicação de acesso aos detalhes.
- Guardados vazios explicam como guardar e oferecem Abrir dicionário; listas preenchidas mostram contagem.
- Referência sem correspondências sugere outra palavra/categoria.
- Ações principais não são anunciadas como opções selecionadas; seleção semântica permanece nos modos de resultado, scripts e navegação.

## Verificação automática

797 testes Java e SQLite aprovados. Novo tests/ui_contrast_test.py: 28 verificações de contraste das paletas, texto/seleção/erro/contorno/foco e estado premido. Texto ativo mínimo observado 5,70:1 contra o fundo; contorno mínimo 3,29:1. Texto desativado em soft e texto em controlos premidos também verificados.

Instrumentação Android compilada, não executada; novas regressões para estado selecionado do modo, ação principal não selecionada e orientação do vazio da Referência. O runner continua limitado a emuladores.

APK Kanateste instalado, versionName 4.0.0. 19731190 bytes, igual à base da mesma variante. SHA-256: F5C590E3B9106B0052ADC18B9152FCCCD78EA913D913BE5025E31FD5F42AC290.

## Limites

Matriz física registada após o percurso de testes. TalkBack manual, navegação por teclado, voz offline, Android antigo e instrumentação em emulador continuam pendentes. O teste numérico cobre as cores definidas, não substitui a inspeção dos ecrãs e dos controlos nativos.

## Percurso físico concluído

Xiaomi Android 16: oito verificações aprovadas (indicação de pesquisa, bridge/chopsticks, ação Usar no conversor, palavra guardada 橋, Referência sem resultados, atribuição JMdict e licença CC BY-SA). Dez capturas em build/screenshots/v5-secondary: Definições claro/escuro, dicionário vazio/resultados/escuro/200%, detalhe, Guardados, Referência vazia e Fontes. Capturas relevantes revistas visualmente.

Guardar e remover a palavra de teste preservou o estado anterior dos favoritos. Tema Seguir o sistema, fonte 1.0 e rotação retrato restaurados; terminou no Converter. O telefone bloqueado impediu a primeira tentativa; depois de desbloqueado, o auxiliar foi ajustado para confirmar abertura do Menu, fechar o teclado antes de procurar o segundo resultado e aceitar FECHAR nos diálogos nativos. O percurso final passou.

README identifica esta branch como desenvolvimento v5; CHANGELOG acrescentado sem versão/tag de lançamento.

## Offline

Quatro verificações físicas aprovadas: 日本語 em Guardados antes e depois de encerrar/abrir a app a frio, e bridge/chopsticks na pesquisa hashi. Durante o teste, wifi_on=0 e mobile_data=0; estados originais/finais wifi_on=1 e mobile_data=0. A palavra criada apenas para este teste foi removida; favoritos anteriores preservados. Capturas em build/screenshots/v5-offline; log checks.json.

Após o arranque a frio, Guardados foi aberto explicitamente pelo Menu. Não se assume que force-stop permita à Activity concluir o callback que persiste o último ecrã. A verificação confirma persistência/acesso às palavras, não restauração do ecrã sob encerramento forçado.

JapaneseSpeech mantém a seleção exclusiva de uma voz japonesa instalada sem necessidade de rede. Reprodução/audição e leitura manual com TalkBack ainda exigem validação, tal como instrumentação e Android anterior.

Código e testes: commit 1fccc1d. Este bloco mantém a versão 4.0.0 e prepara a v5, sem tag de lançamento.

Percurso complementar aprovado: Guardados vazio com Abrir dicionário, detalhe do kana あ, ação principal sem estado selecionado, copiar あ e enviar ao Converter. Capturas 11–13 e extra-checks.json; terminou com entrada limpa.
