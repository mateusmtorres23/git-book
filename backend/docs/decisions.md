## Decisões de codificação do backend

Este arquivo serve para anotar e documentar decisões de codificação do backend. Toda decisão deve ser assinada por **Mateus**, **André**, **JP** ou **geral**, quando for uma decisão de toda a equipe.

As decisões gerais do projeto ficam em [docs/decisions.md](../../docs/decisions.md).

### 1. Usar Flyway para migrations

Usaremos o Flyway para gerenciar as migrations do banco de dados do projeto.

**Assinatura:** geral

### 2. Registrar o commit atual da branch

`Branch` terá o relacionamento opcional `commitAtual` com `Commit`, mapeado como `@ManyToOne(fetch = FetchType.LAZY)` pela coluna `id_commit_atual`. Branches podem apontar para o mesmo commit; uma branch sem commits pode manter essa referência nula.

Esta alteração adiciona apenas a referência no model. A criação e atualização da referência nos services e a recuperação de conteúdo por branch continuam pendentes. A estratégia de leitura foi definida na decisão 4. As migrations serão feitas posteriormente, conforme orientação de Mateus.

**Validação (05/11/2026):** `mvn -o compile`, executado em `backend/`, concluiu com `BUILD SUCCESS`. A persistência no PostgreSQL e o comportamento dos services não foram validados nesta alteração.

**Assinatura:** Mateus

### 3. Versionar apenas os capítulos alterados em cada commit

Esta decisão substitui a proposta inicial de copiar todos os capítulos em cada commit. Cada commit registra o título, a ordem e o texto completos apenas dos capítulos criados ou alterados. A ausência de um capítulo nas alterações significa que sua versão anterior deve ser herdada, não que ele foi excluído.

`Capitulo` mantém somente a identidade e o vínculo com o livro. `ConteudoCapitulo` relaciona essa identidade ao commit e registra uma alteração. Para criação ou edição, `excluido` é falso e título, ordem e texto são obrigatórios; o texto pode estar vazio. Para exclusão, a fábrica `ConteudoCapitulo.excluir(commit, capitulo)` cria um registro imutável com `excluido` verdadeiro e título, ordem e texto nulos. Nenhum registro histórico nem a identidade do capítulo são apagados por essa ação. O marcador fica na alteração do commit, e não em um estado global de `Capitulo`, preservando o isolamento entre branches.

A ordem de um capítulo não excluído deve ser positiva, mas não precisa formar uma sequência contínua. O par commit/capítulo deve ser único. A unicidade de ordem dentro do commit vale para os registros de criação ou edição; os marcadores de exclusão não ocupam posições. Esses critérios ficam declarados no mapeamento JPA e deverão ser reproduzidos nas migrations. O service também deverá validar a unicidade de posições no livro reconstruído, incluindo os capítulos herdados.

`Commit`, `Capitulo`, `ConteudoCapitulo` e `CommitPai` não oferecem setters nem construtores que recebam identificadores gerados. Os registros são mapeados como imutáveis no Hibernate. Os construtores validam os dados obrigatórios, o conteúdo de capítulo do mesmo livro do commit e relações diretas de paternidade sem autorreferência e dentro do mesmo livro. Essas proteções não substituem as regras dos services contra exclusões, alterações em massa, ciclos ou decisões concorrentes.

O service de commits deverá reconstruir o estado da base, aplicar as alterações propostas, validar o estado resultante, persistir somente as alterações junto do commit e dos pais e avançar a branch na mesma transação. O commit de base deverá ser verificado para evitar sobrescrita concorrente. Conteúdos e pais só podem ser inseridos durante a criação do novo commit, sem acrescentar registros a uma versão já confirmada. Não será necessário um relacionamento bidirecional com uma lista em `Commit`. A identificação do pai principal para reconstrução segue a decisão 5.

Os campos UUID de livro, autor e branch de criação em `Commit` permanecem. A existência desses vínculos e a autorização serão verificadas nos services. A estratégia de leitura segue a decisão 4; as migrations seguem adiadas.

**Assinatura:** Mateus

### 4. Remover a entidade VersaoAtual

O conteúdo atual de uma branch será reconstruído a partir de `Branch.commitAtual`, percorrendo o pai principal conforme a decisão 5. Para cada capítulo, o primeiro registro encontrado nessa caminhada determina seu conteúdo ou sua exclusão; a ordenação é aplicada ao conjunto resultante. Os marcadores de exclusão devem participar dessa reconstrução para impedir que versões anteriores de capítulos excluídos reapareçam. Não será mantido outro apontamento global por capítulo.

Foram removidos o model `VersaoAtual`, seu repository e a definição da tabela `versao_atual` no SQL em desenvolvimento. A retirada não altera `Branch.commitAtual` nem os conteúdos históricos. Nenhuma operação foi executada em banco existente; a preparação completa das migrations continua adiada.

**Assinatura:** Mateus

### 5. Ordenar os pais dos commits

`CommitPai` terá o atributo obrigatório e imutável `ordemPai`, com valores 1 ou 2. O construtor exige essa ordem explicitamente. O primeiro commit não tem relações de paternidade; um commit comum tem apenas o pai de ordem 1; um merge entre duas branches tem pais de ordens 1 e 2.

O backend define a ordem ao criar os vínculos: no commit comum, o pai 1 é o commit atual da branch antes da gravação. No merge, o pai 1 é o commit atual da branch de destino antes da integração e o pai 2 é o commit de origem aprovado no Pull Request. Essa numeração identifica os papéis dos pais e não resolve conflitos de conteúdo.

O mapeamento mantém a unicidade do par filho/pai e adiciona a unicidade do par filho/ordem. A coluna `ordem_pai`, o intervalo de valores e as restrições deverão ser reproduzidos nas migrations. O repository permite listar os pais na ordem e buscar diretamente um pai pela ordem.

Na estratégia da decisão 3, a leitura herda o estado do pai 1 e aplica as alterações do commit. Por isso, o commit de merge precisa registrar todas as alterações do resultado em relação ao pai 1, incluindo exclusões. O pai 2 registra a ancestralidade da versão integrada e não fornece conteúdo automaticamente durante essa leitura.

O service deverá criar os vínculos na mesma transação do commit, garantir a quantidade e os papéis dos pais, exigir o pai 1 quando houver pai 2, impedir ciclos e verificar o avanço concorrente da branch. A ordem será definida pelo backend a partir da operação, não recebida como escolha livre do usuário. Esta etapa implementa o contrato no model e no repository; os services e as migrations permanecem pendentes.

**Assinatura:** Mateus

### 6. Organizar DTOs por recurso

Os DTOs ficam em subpacotes de `com.gitbook.gitbook.dtos`: `branch`, `commit`, `capitulo`, `livro`, `pullrequest`, `sugestao` e `usuario`. Requests e responses permanecem juntos no recurso correspondente. `CommitPaiResponseDTO` fica em `commit`; os DTOs de conteúdo ficam em `capitulo`; `VersaoLivroResponseDTO` fica em `livro`.

A reorganização altera somente caminhos, packages e imports, preservando nomes, campos e validações dos DTOs.

**Validação (06/11/2026):** `mvn -o clean compile` concluiu com `BUILD SUCCESS`. A comparação dos 20 DTOs com a revisão anterior confirmou que somente packages, imports e caminhos foram alterados; `git diff --check` não apontou problemas. Nenhum teste foi criado ou recuperado nesta etapa.

**Assinatura:** Mateus
