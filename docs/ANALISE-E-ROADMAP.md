# KanaBridge — análise e proposta de evolução

> Documento histórico da versão 2. A implementação atual é descrita no README e em SIMPLIFICACAO.md; as propostas educativas abaixo foram substituídas pela versão 4.

Data: 8 de setembro de 2026. Versão analisada: commit `f84356da52345e8451e99ae61184c7605603d474` (2.0).

## Recomendação principal

Evoluir a KanaBridge para uma porta de entrada no japonês: **descobrir → compreender → experimentar → rever**. O conversor, a tabela e o mini-dicionário já permitem consultar informação; a oportunidade está em ligar essas peças e dar ao principiante um próximo passo claro.

Preservaria a identidade visual creme/coral, o funcionamento offline, a ausência de conta e a rapidez de acesso. A primeira evolução deve melhorar a confiança nas leituras, a navegação e a prática de kana. Conteúdo cultural contextualizado pode distinguir a aplicação sem exigir um curso completo.

## O que foi verificado

- Repositório clonado do GitHub para a pasta local `KanaBridge`.
- Leitura de todos os ficheiros Java, manifesto, recursos, testes e scripts de compilação.
- Os **36 testes existentes passaram**, compilados diretamente com o JDK 17 disponível. O README pede Java 21 para o desenvolvimento completo; isto só valida o núcleo Java neste ambiente.
- Executadas sondas adicionais ao conversor, catálogo e critérios de pesquisa do dicionário. O programa de diagnóstico está em `build/audit/AuditProbe.java`, uma pasta ignorada pelo Git.
- Não foi compilado nem executado o APK: não foi encontrado Android SDK em `C:\Android` ou na localização habitual do utilizador, nem `adb` no PATH. Contraste e dimensões foram analisados no código, sem observação num dispositivo.
- Não foram alterados ficheiros da aplicação nem enviados commits para o GitHub. Este documento é a entrega da análise.

## Base existente

| Área | Estado atual | Oportunidade |
|---|---|---|
| Conversão | Kana, combinações, consoantes duplas, variantes de largura e macrons parciais | Distinguir transliteração, pronúncia contextual e significado |
| Referência | Tabela, categorias, pesquisa e detalhes | Tornar cada entrada num ponto de aprendizagem e prática |
| Vocabulário | 87 entradas, incluindo grafias relacionadas, com PT/EN | Exploração por temas, exemplos, leituras e favoritos |
| Interface | Android nativo, fundo creme, coral, cartões e quatro secções | Hierarquia mais clara, navegação persistente e menos repetição |
| Arquitetura | Núcleo de kana independente de Android; UI numa Activity de 812 linhas | Separar ecrãs, estado, recursos e conteúdos à medida que cresce |

As 87 entradas não equivalem a 87 conceitos diferentes: por exemplo, `がっこう` e `学校` aparecem separadamente.

## Problemas e limitações prioritários

### 1. A leitura literal é apresentada como se garantisse a pronúncia

**Confirmado em execução:** `こんにちは → konnichiha`; `こんばんは → konbanha`. O primeiro é o exemplo do botão “Experimentar exemplo” e está explicitamente aceite em `tests/KanaTransliteratorTest.java:8`.

Isto pode ser uma transliteração mecânica dos sinais, mas não ensina a leitura habitual destas saudações. A Japan Foundation apresenta `こんにちは` como `konnichiwa` no [Marugoto no Kotoba](https://words.marugotoweb.jp/static_contents/sp/about/about.php?lang=en).

**Proposta:** guardar a leitura contextual das palavras conhecidas e mostrá-la como principal. Na explicação, indicar “は lê-se normalmente ha; nesta saudação lê-se wa”. Separar no modelo a leitura lexical e a transliteração literal. Não substituir globalmente todo o `は` por `wa`: as partículas exigem contexto. A [tabela ALA-LC de japonês](https://loc.gov/catdir/cpso/romanization/japanese.pdf) também distingue as leituras de は e へ quando são partículas; é uma referência de convenções, não uma especificação única obrigatória para a app.

Há ainda a mensagem “A leitura em rōmaji continua correta” em `MainActivity.java:305`. A entrada `日本語 あ` produz `日本語 a`: o resultado é parcial, embora `日本語` isolado exista no dicionário. Substituir a garantia por informação contextual: “Converti os kana. Este texto contém kanji cuja leitura não foi determinada.” Marcar os segmentos preservados e oferecer uma explicação curta.

**Aceitação:** as saudações conhecidas têm leitura e explicação adequadas; texto misto identifica claramente partes não lidas; testes distinguem o modo literal do modo contextual.

### 2. Marcas de repetição têm erros verificáveis

**Confirmado:** `しゞ → shizhi`, `ちゞ → chichi`, `ふゞ → fufu`. A função `voice()` em `KanaTransliterator.java:372` altera prefixos latinos e falha em leituras irregulares.

**Proposta:** aplicar a transformação ao kana anterior e consultar o catálogo existente. Para estes pares, obter respetivamente `shiji`, `chiji` e `fubu`. Definir também o comportamento para marcas sem antecedente válido, fronteiras de texto e `っ` isolado, que atualmente desaparece do resultado. São casos secundários para principiantes, mas precisam de coerência se a funcionalidade é anunciada.

### 3. O modo com macrons é incompleto

**Confirmado:** `がっこう → gakkō` e `スーパー → sūpā`, mas `すうがく` continua `suugaku` no modo MACRON. O percurso genérico trata `o+u/o`; as leituras do dicionário com rōmaji explícito passam por outra regra.

**Proposta:** documentar a convenção usada e centralizar a formatação. Preferir leituras lexicais validadas para palavras conhecidas. Evitar uma substituição global de todas as vogais adjacentes: fronteiras e composição de palavras podem exigir outra leitura. Acrescentar exemplos de vogais longas e testes de exceções revistos linguisticamente.

### 4. O estado de utilização não é restaurado

**Evidência estática:** `onCreate()` ignora os dados de `savedInstanceState` e abre sempre o conversor (`MainActivity.java:65–87`). Texto, preferências e categoria estão em campos da Activity; não existe persistência explícita nem IDs estáveis nos campos para a restauração automática. As pesquisas também são recriadas ao mudar de secção.

**Consequência esperada, a confirmar em Android:** perda de texto e contexto ao rodar o dispositivo ou após recriação da Activity. Dentro da mesma instância, o texto do conversor é preservado ao mudar de secção; o problema não é uma perda em todas as navegações.

**Proposta:** guardar secção, texto e pesquisas no estado de instância; persistir idioma, estilo e progresso localmente. A [documentação de estado das Views Android](https://developer.android.com/topic/libraries/architecture/views/saving-states-views) distingue o estado temporário das preferências duradouras.

### 5. A pesquisa é pouco tolerante e há conteúdo invisível na tabela

**Resultados obtidos com a mesma lógica usada pelo dicionário:**

| Pesquisa | Entradas encontradas |
|---|---:|
| `café` | 1 |
| `cafe` | 0 |
| `gakkou` | 2 |
| `gakkō` | 0 |
| `コーヒー` | 1 |
| `ｺｰﾋｰ` | 0 |

`renderDictionary()` (`MainActivity.java:639`) usa apenas minúsculas e correspondência textual. A tabela normaliza a largura, mas símbolos como `ゐ`, `ゑ`, `ヵ`, `ヶ` e `ー` são conhecidos pelo conversor e não estão registados como entradas pesquisáveis (`KanaData.java:117–126`).

**Proposta:** normalizar largura, tolerar acentos na pesquisa em português e indexar variantes explícitas de rōmaji. Unificar grafias da mesma palavra através de um identificador e aliases. Acrescentar entradas de referência para os sinais suportados. Mostrar o total e permitir “Ver mais”: atualmente o dicionário limita a lista inicial a 24 entradas e as pesquisas a 80 sem paginação.

## Design e usabilidade

### Navegação e primeiro contacto

Hoje a navegação está dentro do ScrollView principal, desaparecendo à medida que se percorre o conteúdo. Os quatro botões de 98 dp com margens excedem a largura útil de muitos telemóveis; o scroll horizontal esconde parte das opções.

Proponho quatro destinos persistentes: **Descobrir · Kana · Praticar · Ferramentas**. Ferramentas reúne Conversor e Dicionário. Sobre, idioma e aparência passam para definições. A secção Descobrir inclui atalhos diretos para o conversor e a possibilidade de retomar a última atividade; quem já usa a ferramenta deve conseguir continuar a chegar rapidamente a ela.

O primeiro contacto deve oferecer “Começar pelas vogais”, “Explorar palavras” e “Ler um texto”. Um interesse opcional — viagens, cultura pop ou língua — pode ordenar sugestões, sem formulário obrigatório nem bloqueio do resto da aplicação.

### Hierarquia visual

- Manter creme, tinta escura e coral; reservar o coral original para áreas e destaques adequados. Criar uma variante mais escura para texto pequeno e botões com letras brancas.
- Colocar o resultado imediatamente a seguir à entrada. Transformações de silabário e estilo de rōmaji podem ficar num menu de opções com o modo atual visível.
- Substituir a longa sequência de cartões por segmentos tocáveis; abrir a explicação do segmento selecionado. Renomear “Carácter a carácter” para “Como se lê”, já que algumas unidades agrupam vários caracteres.
- Apresentar português por defeito e inglês por opção. Evitar duplicar todos os parágrafos no mesmo ecrã.
- Nos detalhes de kana, usar uma ficha com símbolo grande, leitura, palavra de exemplo e ações “Ouvir”, “Praticar” e “Guardar”.
- Acrescentar modo escuro seguindo a preferência do sistema e organizar dimensões, cores e tipografia em recursos partilhados.

### Acessibilidade e adaptação

**Confirmado no código:** navegação e categorias têm 42 dp de altura; vários botões têm 46 dp e “Copiar” tem 38 dp. O contraste calculado de coral `#E65F4D` sobre branco é aproximadamente **3,44:1**; sobre creme, **3,21:1**. O coral é usado em texto de 12–15 sp e em fundos de botões com texto branco.

O Android recomenda alvos de toque de pelo menos 48 × 48 dp e contraste de 4,5:1 para texto pequeno. Aumentar as áreas interativas e rever estes pares de cor. [Referência oficial de acessibilidade Android](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views?hl=en).

A tabela gojūon usa aproximadamente 380 dp de largura incluindo margens, além dos 40 dp exteriores. Por isso exige deslocamento horizontal em ecrãs comuns. O cálculo das outras grelhas usa a largura do ecrã e atribui uma margem à última coluna que não desconta integralmente. Adaptar à largura efetivamente disponível; em ecrãs estreitos oferecer linhas expansíveis ou uma alternativa em lista, mantendo a tabela completa acessível.

Acrescentar estado selecionado acessível aos seletores, agrupamento semântico das células e feedback de toque. Validar com TalkBack, larguras de 320/360/411 dp, fonte a 200%, orientação horizontal e teclado aberto. Cortes de texto e comportamento com o teclado são riscos a verificar em execução, não observações feitas num emulador.

## Funcionalidades recomendadas para quem tem curiosidade

| Funcionalidade | Experiência concreta | Valor | Esforço relativo |
|---|---|---|---|
| Primeiros passos | Explicar hiragana, katakana e kanji com exemplos; aprender cinco vogais e reconhecer uma palavra | Dá um ponto de partida | Médio |
| Ouvir kana e palavras | Áudio num toque, associado à grafia e ao significado | Liga a escrita ao som | Médio, incluindo produção de conteúdo |
| Prática de 2–5 minutos | Escolher linhas, reconhecer kana, revelar rōmaji e rever erros | Transforma consulta em aprendizagem ativa | Médio |
| Kana parecidos | Comparar `シ/ツ`, `ソ/ン` e `ぬ/め`; destacar diferenças e testar | Resolve confusões concretas | Médio |
| Favoritos e revisão | Guardar a partir da tabela ou do dicionário e rever mais tarde | Liga as funcionalidades existentes | Baixo a médio |
| Coleções temáticas | Comida, viagens, saudações e palavras encontradas em anime, com registo de utilização | Dá relevância pessoal | Médio; exige revisão editorial |
| Curiosidade com uma ação | “Porque se usam três sistemas de escrita?” seguida de um exemplo e desafio | Une cultura e língua | Médio |
| Ordem dos traços | Animação e desenho livre sobre uma referência | Apoia quem quer escrever | Médio a alto, sobretudo dados e validação |
| Revisão espaçada | Agendar localmente os itens a rever, a partir dos resultados | Ajuda a organizar a continuidade | Médio; após existir prática |
| Leitor com furigana | Mostrar leituras sobre palavras com kanji e abrir a ficha ao tocar | Aproxima a app de texto real | Alto |

O áudio, a ordem dos traços e a ligação entre caracteres e som também aparecem nos recursos de iniciação da Japan Foundation; servem como referência pedagógica. [Recursos Marugoto](https://marugoto.jpf.go.jp/en/e-learning/), [notas para iniciação à escrita](https://marugoto.jpf.go.jp/assets/docs/teacher/resource/starter_c/starter_competences_teachers_notes_EN.pdf).

Para manter o núcleo offline, começaria com gravações revistas incluídas na aplicação. TTS pode complementar, mas requer verificação da disponibilidade da voz japonesa offline no dispositivo. Conteúdos, gravações e dados de traços precisam de proveniência e direitos de utilização registados.

As fichas culturais devem explicar situações e variantes de uso, com autoria/revisão, em vez de apresentar generalizações sobre “os japoneses”. Palavras de anime devem identificar contextos informais ou desadequados a certas situações. Ainu merece uma coleção contextualizada própria, para não parecer uma etapa obrigatória do japonês inicial.

OCR de fotografias, conversação com IA, sincronização entre dispositivos e um dicionário extenso ficam para uma fase posterior: acrescentam dependências, trabalho de qualidade e decisões sobre dados que não são necessárias para testar o valor do percurso inicial.

## Exemplo de percurso proposto

1. Em **Descobrir**, a pessoa abre “Palavras de um café”.
2. Vê `コーヒー`, a leitura e o significado; ouve a palavra.
3. Toca em `ー` e percebe como funciona o prolongamento da vogal.
4. Faz um desafio de três palavras com essa marca.
5. Guarda a palavra; mais tarde encontra-a na revisão.

Cada conteúdo deve terminar numa ação útil: ouvir, explorar a regra, experimentar uma palavra ou praticar. “Palavra do dia” isolada tem menos valor do que este encadeamento.

## Plano faseado

As dimensões abaixo são comparativas, não estimativas de calendário. A revisão linguística, os recursos de áudio e a validação em dispositivos afetam bastante o esforço.

| Fase | Entrega | Critério para avançar |
|---|---|---|
| 1 — Confiança e conforto | Leituras conhecidas, mensagens de resultados parciais, repetição, pesquisa normalizada, estado restaurado, contraste e alvos de toque | Casos linguísticos aprovados; texto preservado após rotação; pesquisa tolerante; fluxos principais utilizáveis com fonte grande |
| 2 — Primeira aprendizagem | Navegação persistente, primeiros passos, ficha de kana com áudio, prática curta, favoritos | Uma pessoa sem conhecimentos encontra o início, aprende um grupo e termina um desafio sem ajuda |
| 3 — Descoberta e continuidade | Coleções temáticas revistas, curiosidades ligadas à prática e revisão espaçada | A pessoa consegue retomar, perceber o que rever e escolher o seu interesse |
| 4 — Texto real | Dicionário mais rico, furigana e eventual OCR | Leituras, ambiguidade, desempenho e funcionamento offline validados com exemplos reais |

**Escolha para a próxima versão:** executar a fase 1 e um percurso pequeno da fase 2 — cinco vogais, áudio, cinco perguntas e favoritos. Este recorte testa a nova proposta sem exigir escrever um curso inteiro.

## Preparação técnica

- Conservar `KanaData` e `KanaTransliterator` independentes de Android. Separar gradualmente a Activity em componentes de ecrã e um estado explícito; não é necessário reescrever tudo para redesenhar os fluxos.
- Extrair textos para recursos de idioma e conteúdos para ficheiros estruturados. Cada palavra deve ter ID estável, grafias, leitura em kana, rōmaji, sentidos, exemplos, registo, origem e revisão. Favoritos devem usar IDs, não traduções.
- Introduzir armazenamento local apropriado para preferências e progresso; planear exportação/restauro quando os dados pessoais de aprendizagem tiverem valor.
- Evitar reconstruir todos os cartões a cada tecla (`MainActivity.java:273`). Usar listas que reutilizam elementos e atualizar apenas o necessário. Medir colagem de textos compridos; não foi feito um benchmark Android nesta análise.
- Acrescentar testes de regressão para os casos identificados e testes de UI para estado, navegação e acessibilidade. Os 36 testes atuais cobrem o núcleo, não a experiência completa.
- Automatizar compilação e testes. Os scripts Windows atuais são uma base; avaliar Gradle se forem necessários testes instrumentados e mais colaboradores. Rever documentação de instalação e o processo de assinatura antes de publicar atualizações.

## Validação com pessoas

Fazer uma primeira ronda com cinco participantes, incluindo principiantes sem teclado japonês configurado. Pedir-lhes que descubram como começar, pesquisem “cafe”, percebam `こんにちは`, guardem uma palavra e regressem à atividade após rodar o telefone.

Observar tarefas concluídas sem ajuda, dúvidas sobre som versus significado, dificuldade em encontrar botões e capacidade de retomar. Para a prática, observar reconhecimento após uma revisão, não apenas a conclusão do exercício. Estas são propostas de avaliação; não foram realizadas entrevistas nem medições com utilizadores nesta análise.
