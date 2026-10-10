# V5 — design do Converter e Kana

> Registo desta etapa de desenvolvimento; código incluído no commit 1fccc1d. A uniformização posterior e a validação offline estão registadas em [V5-SECUNDARIOS-CONTRASTE.md](V5-SECUNDARIOS-CONTRASTE.md).
Primeira aplicação limitada ao Converter e Kana, conforme a secção 3. Ecrãs secundários mantêm o estilo anterior até à revisão.

- Tokens em Ui: espaçamentos 4/8/12/16/24 dp, controlos com raio 12 dp, cartões 16 dp, botões com altura mínima 52 dp e hierarquia tipográfica.
- Paleta clara/escura preservada e token semântico de erro disponível.
- Botões secundários com contorno; seleção com preenchimento e negrito e estado acessível Selecionado.
- Sem sombras/elevação decorativa; ripple nativo de toque; foco com contorno de 2 dp.
- Campos com cursor na cor de destaque em API 29+; fontes nativas e fallback japonês preservados.
- Sem alterações ao motor, dependências ou permissões.
- Inclui as correções anteriores de teclado, paisagem e etiquetas. O teste da primeira linha de kana respeita agora a largura mínima da variante compacta.

## Validação

797 testes Java passaram. SQLite passou: integridade, índices, exemplos, atribuição, 218869 entradas e 325004 grafias. Instrumentação compilada, não executada: exige emulador. Kanateste instalada por atualização no Xiaomi autorizado. Capturas locais em build/screenshots/v5-design, fora do Git.

Versão mantida em 4.0.0 até ao lançamento. SHA-256: A8C8CFD8C2C7C2E15D5C587E7B35C45CD16A91EC6BB67D839FC8FFAE59AD4FE5.

## Limites

Secção 3 em curso: revisão destes dois ecrãs antes de espalhar os componentes. Feedback de cópia/erro, contraste quantitativo, TalkBack e Android mais antigo pendentes. O Xiaomi Android 16 não cobre a matriz API 23+.

Verificação visual no Xiaomi: Converter vazio e com teclado nos temas claro/escuro; Kana nos dois temas, alternância para Katakana, fontes 150% e 200%, Converter a 200% e Kana em paisagem. As capturas revistas mostram resultado curto e ações acima do teclado e primeira linha de kana visível em paisagem. Fonte, rotação e tema restaurados no fim do percurso.
