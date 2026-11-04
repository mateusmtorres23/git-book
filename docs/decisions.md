## Decisões de codificação

Este arquivo serve para anotar e documentar decisões gerais de codificação do projeto. Toda decisão deve ser assinada por **Mateus**, **André**, **JP** ou **geral**, quando for uma decisão de toda a equipe.

As decisões específicas do backend ficam em [docs-backend/decisions.md](../backend/docs/decisions.md).

### 1. Containerização e orquestração com Docker Compose

O projeto terá três serviços orquestrados com Docker Compose: API REST em Spring, SPA em TypeScript com React e banco PostgreSQL.

A API e a SPA terão seus próprios arquivos `Dockerfile`, em `backend/` e `frontend/`, respectivamente. O PostgreSQL utilizará uma imagem oficial, com volume para persistir os dados. O arquivo `docker-compose.yml` ficará na raiz e definirá a execução e a comunicação entre os serviços.

Os arquivos e configurações da API ficam em `backend/`, e os da SPA em `frontend/`. A documentação geral fica em `docs/`, e a documentação específica do backend em `../backend/docs`. O README e as configurações gerais permanecem na raiz.

**Assinatura:** geral
