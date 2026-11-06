# Testes do backend

Este documento registra as decisões principais para a arquitetura de testes do Gitbook. A suíte deve validar as regras de negócio, a persistência no PostgreSQL e as permissões aplicadas pelo backend.

## JUnit 5

**JUnit 5**

A biblioteca de testes utilizada será o JUnit 5, integrado ao Spring Boot. Testes devem verificar comportamento e usar nomes que deixem claro o cenário validado.

## Banco de dados

**PostgreSQL com Testcontainers**

Testes que dependem de persistência usarão um container PostgreSQL real. H2 não será utilizado como substituto, pois pode apresentar comportamento diferente do banco da aplicação.

A imagem do PostgreSQL deve ser fixada em uma versão compatível com o ambiente do projeto. O uso de `latest` não é permitido.

**Flyway como fonte do schema**

Os testes iniciarão o banco vazio e executarão as migrations reais do Flyway. O Hibernate continuará usando `ddl-auto=validate`, para detectar divergências entre entidades e banco.

O SQL em [`docs/db/tables.sql`](../../docs/db/tables.sql) é referência de modelagem. Quando as migrations forem criadas, elas serão a fonte usada pelos testes.

## Tipos de teste

**Testes unitários**

Validam regras isoladas sem subir o Spring e sem acessar o banco. São usados para validações, permissões, transições de status e regras de comparação.

**Testes de camada**

Usam as anotações do Spring para testar uma parte da aplicação:

- `@DataJpaTest` para repositories, queries e mapeamentos;
- `@WebMvcTest` para controllers, validação e respostas HTTP.

**Testes de integração**

Usam `@SpringBootTest`, PostgreSQL via Testcontainers, migrations reais e os componentes necessários para validar o fluxo completo.

Devem cobrir principalmente criação de livros, colaboradores, branches, commits, merges, histórico e autorização.

**Testes de concorrência**

Serão usados quando a regra depender de requisições simultâneas, como unicidade de colaboradores, decisões de merge e confirmações concorrentes de commits.

## Isolamento dos testes

**Cada teste deve ser independente**

Os dados de um teste não podem influenciar outro. Os cenários devem criar seus próprios dados por builders ou fixtures e limpar o banco ao final da execução.

`@Transactional` pode ser usado quando o rollback representa corretamente o fluxo, mas não será considerado substituto universal para a limpeza do banco.

O script de limpeza ficará em `backend/src/test/resources/db/cleanup.sql` quando as migrations forem implementadas.

## Segurança e regras de negócio

**A autorização será validada no backend**

Os testes devem comprovar acesso permitido e negado. Entre os cenários importantes estão:

- usuário sem autenticação;
- usuário sem vínculo com o livro;
- revisor tentando gravar conteúdo;
- escritor tentando gravar na `main` ou na branch de outra pessoa;
- usuário que perdeu o vínculo tentando acessar o livro;
- usuário que não é gestor tentando decidir um merge ou sugestão.

**Operações compostas devem ser atômicas**

Testes de integração devem confirmar que falhas não deixam dados parciais. Isso vale especialmente para criação de livro com `main`, merge, histórico e decisões sobre sugestões.

Decisões repetidas ou concorrentes não podem gerar duplicidade nem aplicar a mesma alteração mais de uma vez.

## Organização dos arquivos

A organização das classes e recursos de teste ainda não foi definida pela equipe. A estrutura apresentada anteriormente era apenas uma sugestão e não deve ser tratada como decisão do projeto.

Essa definição será feita durante a implementação da base de testes, considerando a convenção do Maven, a organização dos pacotes do backend e a separação entre testes unitários e testes que dependem do banco.

## Execução

Os testes de integração exigem Docker em execução. A partir de `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

O CI deve executar a suíte completa com banco descartável. Falhas de Docker, migration ou schema devem quebrar o build e não ser ignoradas.
