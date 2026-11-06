## Decisões de codificação do backend

Este arquivo serve para anotar e documentar decisões de codificação do backend. Toda decisão deve ser assinada por **Mateus**, **André**, **JP** ou **geral**, quando for uma decisão de toda a equipe.

As decisões gerais do projeto ficam em [docs/decisions.md](../../docs/decisions.md).

### 1. Usar Flyway para migrations

Usaremos o Flyway para gerenciar as migrations do banco de dados do projeto.

**Assinatura:** geral

### 2. Registrar o commit atual da branch

`Branch` terá o relacionamento opcional `commitAtual` com `Commit`, mapeado como `@ManyToOne(fetch = FetchType.LAZY)` pela coluna `id_commit_atual`. Branches podem apontar para o mesmo commit; uma branch sem commits pode manter essa referência nula.

Esta alteração adiciona apenas a referência no model. A criação e atualização da referência nos services, a recuperação de conteúdo por branch e o papel de `VersaoAtual` continuam pendentes. As migrations serão feitas posteriormente, conforme orientação de Mateus.

**Validação (05/11/2026):** `mvn -o compile`, executado em `backend/`, concluiu com `BUILD SUCCESS`. A persistência no PostgreSQL e o comportamento dos services não foram validados nesta alteração.

**Assinatura:** Mateus
