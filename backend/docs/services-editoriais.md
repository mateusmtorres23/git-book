# Services de usuário, livro, capítulo e sugestão

As implementações seguem as interfaces criadas para essas quatro entidades. Livro, capítulo e sugestão usam as regras de `docs/requirements.md`, `docs/user-stories.md` e `backend/docs/decisions.md`.

## Retornos

Nos services de livro, capítulo e sugestão, operações recusadas por dados inválidos, recurso inexistente ou falta de permissão retornam `null`. Uma lista vazia significa consulta autorizada sem resultados. Os novos métodos não lançam exceções de negócio nem usam `try/catch`.

As permissões são verificadas com `if`s dentro de cada método, consultando usuário, dono e colaboração nos repositories. Esses três services não dependem de `AcessoLivroService`.

Falhas de infraestrutura continuam sendo tratadas pelo Spring e pelo banco. Não são convertidas em sucesso ou em lista vazia. A reconstrução de versões reutiliza o componente existente, que verifica a integridade do histórico.

## Usuário

- Permite buscar por ID, cadastrar e atualizar nome, e-mail e senha.
- Usuário não encontrado e e-mail já utilizado retornam `null`. Na atualização, manter o próprio e-mail é permitido.
- Senhas são gravadas como hash BCrypt; respostas não incluem senha nem hash.
- O DTO de atualização exige os três campos; não se trata de atualização parcial.
- A checagem de e-mail antes da gravação não resolve concorrência entre cadastros. A restrição única do banco permanece como proteção final.

## Livro

- Criar exige uma conta existente e título válido. O dono é o usuário informado; a versão inicial é `1.0`.
- Livro e uma main vazia, com o dono como autor, são gravados na mesma transação.
- Consulta permite dono e colaboradores. O catálogo reúne livros próprios e compartilhados sem duplicatas.
- Atualização de título e descrição exige o dono e preserva dono, versão e data de criação.

## Capítulo

- Os métodos são de leitura: usam o commit atual da branch e reconstroem o conteúdo pelo pai principal.
- Título e ordem vêm dessa versão. Capítulos herdados são incluídos, marcadores de exclusão são respeitados e a listagem segue a ordem dos capítulos.
- Criação, edição e exclusão continuam no fluxo de commits; nenhuma entidade histórica é alterada.

## Sugestão

- Criar exige colaborador com papel de revisor e Pull Request aberto. O dono não recebe automaticamente o papel de revisor.
- O capítulo e o trecho precisam existir no commit de origem fixado no Pull Request. Novos commits da branch não alteram essa base.
- Posições usam índices de texto Java/JavaScript (UTF-16), começando em zero, com início inclusivo e fim exclusivo.
- Retirar exige o próprio autor, ainda como revisor, e Pull Request aberto. O status passa a `RETIRADA`, preenchendo `dataAvaliacao` e mantendo o histórico. Retirada repetida preserva a primeira data.
- Dono e colaboradores podem consultar sugestões, inclusive de Pull Requests encerrados.
- Criação e retirada usam o mesmo lock do livro usado pelo merge, para serializar alterações de sugestões e decisões do Pull Request.

## Controllers e rotas

Os controllers usam `@Valid` para validar os DTOs recebidos e `ResponseEntity` para retornar status HTTP com verificações por `if`, sem lançar exceções manualmente. Os mecanismos de validação e segurança do Spring continuam ativos.

| Método | Rota | Operação |
| --- | --- | --- |
| POST | `/api/users` | Cadastrar conta |
| GET | `/api/users/{idUsuario}` | Consultar a própria conta |
| PUT | `/api/users/{idUsuario}` | Atualizar a própria conta, incluindo senha |
| POST | `/api/books` | Criar livro com main |
| GET | `/api/books` | Listar livros próprios e compartilhados |
| GET | `/api/books/{idLivro}` | Consultar livro autorizado |
| PUT | `/api/books/{idLivro}` | Atualizar título e descrição como dono |
| GET | `/api/branches/{idBranch}/chapters` | Listar capítulos da branch |
| GET | `/api/branches/{idBranch}/chapters/{idCapitulo}` | Consultar capítulo na branch |
| POST | `/api/pull-requests/{idPullRequest}/suggestions` | Criar sugestão |
| GET | `/api/pull-requests/{idPullRequest}/suggestions` | Listar sugestões |
| GET | `/api/suggestions/{idSugestao}` | Consultar sugestão |
| PATCH | `/api/suggestions/{idSugestao}/withdraw` | Retirar sugestão sem apagá-la |

Criações retornam `201` com cabeçalho `Location`. Consultas e alterações concluídas retornam `200`; listagens autorizadas sem resultados retornam `200` com `[]`.

Nos endpoints protegidos, cada controller obtém o usuário pelo e-mail em `Authentication.getName()`. Autenticação ausente, anônima ou sem conta correspondente retorna `401`, caso a requisição chegue ao controller. Consultar ou atualizar a conta de outra pessoa retorna `403`.

Cadastro e atualização de conta recusados pelo service retornam `409`. Criação de livro ou sugestão recusada retorna `400`. As demais operações retornam `404` quando o service devolve `null`, pois esse retorno não distingue recurso inexistente, falta de permissão ou estado incompatível. Os controllers não tentam deduzir a causa nem expõem a existência de recursos sem acesso.

## Estado de validação e pendências

- Verificação estática dos contratos e compilação Maven. Não foram criados nem executados testes nesta revisão.
- Persistência, rollback e concorrência no PostgreSQL ainda precisam de verificação no ambiente integrado.
- A migration `V1__migration_gitbook.sql` referencia tabelas que ainda não cria e não contém todos os campos atuais de conteúdo de capítulo. A inicialização de um banco vazio depende de completar as migrations.
- Os controllers de usuário, livro, capítulo e sugestão estão implementados e a compilação passou. As rotas ainda não foram verificadas com a aplicação e o banco em execução.
- Falta configurar o fluxo de autenticação e segurança para fornecer o e-mail da conta em `Authentication.getName()`, liberar o cadastro público e definir o tratamento de CSRF conforme o mecanismo de autenticação escolhido. Os controllers não substituem essa configuração; a segurança padrão do Spring pode bloquear a requisição antes de ela chegar ao método.
- Os IDs usados para verificar permissões vêm da autenticação no servidor. As assinaturas dos services, por si só, não autenticam o usuário; chamadas internas ao service de usuário precisam preservar a autorização aplicada no controller.
