# Requisitos do Gitbook

Base de trabalho do projeto da disciplina de Desenvolvimento Web, revisada em 27/10/2026. O professor deixou o formato da entrega em aberto.

**Estado: proposta para revisão da equipe.** Este catálogo descreve comportamento desejado; não significa que cada requisito esteja implementado ou aprovado individualmente. Escolhas ainda não resolvidas estão em [Decisões e pendências](decisoes.md).

## 1. Problema e objetivo

Autores que escrevem juntos precisam experimentar alterações sem sobrescrever o trabalho de outras pessoas, recuperar versões anteriores e revisar o que entrará na obra oficial. Trocas de arquivos e edições sobre uma única cópia dificultam autoria, comparação e aprovação.

O Gitbook deve organizar a escrita de livros em capítulos, permitir trabalho em branches e registrar versões por commits. Um gestor acompanha a produção e decide quais propostas entram na versão oficial. Revisores sugerem melhorias sem alterar diretamente o conteúdo da main.

O sucesso da entrega acadêmica será demonstrado por um fluxo completo: cadastrar usuários, criar um livro, associar colaboradores, escrever em uma branch, registrar um commit, solicitar e avaliar um merge, consultar a versão oficial e seu histórico.

## 2. Fontes e interpretação

| Fonte | Uso nesta base | Limite |
| --- | --- | --- |
| [README](../README.md) | Visão do produto, papéis e fluxo editorial | Mistura planejamento com afirmações de implementação; estrutura e versões não refletem integralmente o código |
| [Protótipo](prototype/index.html) | Telas, navegação e exemplos de interação | Dados e autenticação simulados não comprovam regras implementadas |
| MER enviado: `modeloER_gitbook (1).pdf` | Entidades, relacionamentos e conteúdo versionado | Referência externa ao repositório; divergências em DEC-03 a DEC-06 |
| [SQL](db/tables.sql) | Restrições e estrutura relacional propostas | Não sincronizado com o MER nem instalado como migração |
| Requisito anterior deste arquivo | Unicidade da colaboração por usuário/livro | Preservado na RN-02 |
| Planejamento e README do Simulacred | Organização por histórias, critérios e responsabilidades | Seu escopo, stack e instruções não são requisitos do Gitbook |

## 3. Atores e vocabulário

| Termo | Significado proposto |
| --- | --- |
| Usuário | Pessoa com conta; pode criar uma obra e participar de outras |
| Gestor | Dono do livro e responsável pelas decisões editoriais e colaboradores |
| Escritor | Colaborador que escreve em branches e propõe integração |
| Revisor | Colaborador que consulta a obra e propõe sugestões editoriais |
| Livro / obra | Unidade de colaboração, com capítulos, participantes e histórico |
| Main | Branch que representa o conteúdo oficial consolidado |
| Branch | Linha de trabalho de uma obra, iniciada a partir de uma versão identificável |
| Commit | Registro imutável de uma versão, com autoria, instante, mensagem e relação com versões anteriores |
| Merge request / pull request | Solicitação de integração de alterações de uma branch à main; os dois nomes representam o mesmo conceito nesta base |
| Sugestão | Proposta de substituição de um trecho, vinculada ao capítulo e à versão analisada |

**Hipótese:** papéis são definidos por livro; o dono exerce a função de gestor. Uma pessoa pode gerir uma obra e revisar outra. Ratificar em DEC-01; o seletor global do protótipo não define a autorização real.

## 4. Escopo e prioridades propostas

P1 é o núcleo da escrita colaborativa versionada. P2 completa o fluxo editorial presente nos artefatos. P3 é evolução candidata. Prioridades podem ser ajustadas conforme o tempo da equipe; não são datas ou estimativas.

| Grupo | Prioridade | Histórias |
| --- | --- | --- |
| Contas e acesso | P1 | US-01, US-02 |
| Livros e colaboradores | P1 | US-03 a US-06 |
| Capítulos, branches e commits | P1 | US-07 a US-09 |
| Integração e histórico | P1 | US-10 a US-13 |
| Sugestões editoriais | P2 | US-14, US-15 |
| Prazos e entregas | P2 | US-16, US-17 |
| Arquivamento, exclusão definitiva e proteção configurável da main | P3 | Sem história detalhada nesta revisão; DEC-08 |

Ficam fora da proposta inicial: edição simultânea em tempo real, sincronização com Git externo, exportação PDF/ePub, notificações, recuperação de senha por e-mail, publicação comercial, múltiplos gestores e resolução automática de conflitos. Inclusão futura exige definição de escopo.

## 5. Regras de negócio

Regras propostas para as histórias, sujeitas às decisões indicadas. RN-02 preserva o requisito já existente.

| ID | Regra |
| --- | --- |
| RN-01 | Acesso ao conteúdo e operações de um livro exige vínculo autorizado. A permissão considera o livro solicitado, não um papel enviado livremente pelo cliente. |
| RN-02 | Um usuário só pode ser registrado uma vez como colaborador de um livro. O par `(id_usuario, id_livro)` deve ser único, inclusive sob requisições concorrentes. O SQL propõe `uq_colaboracao_usuario_livro` em `colabora_em`. |
| RN-03 | Cada livro tem um dono e uma única main. Criar livro e main compõe uma operação lógica. A representação de livro vazio depende de DEC-04. |
| RN-04 | Nomes de branches são únicos dentro de um livro. A main não recebe escrita direta de escritores. |
| RN-05 | Alterar uma branch não modifica outras branches. Apenas conteúdo confirmado por commit pode compor uma solicitação de integração. |
| RN-06 | Commits preservam conteúdo histórico e autoria. Editar depois não reescreve uma versão registrada. A ancestralidade não pode conter ciclos. |
| RN-07 | Origem, destino, capítulos e commits de uma operação pertencem ao mesmo livro. Origem e destino de merge são distintos. |
| RN-08 | Somente o gestor decide integração à main. A aplicação revalida origem e destino antes de aplicar a decisão; mudanças posteriores à análise exigem nova verificação. |
| RN-09 | Merge sem conflito incorpora conteúdo aprovado e registra sua relação com o histórico. Conflito preserva a main e exige ajuste. Algoritmo e granularidade: DEC-04 e DEC-05. |
| RN-10 | Rejeitar proposta não altera a main nem apaga commits do escritor. Repetir decisão não pode gerar outro merge ou duplicar efeitos. |
| RN-11 | Sugestões identificam capítulo, versão de origem, trecho original e texto proposto. Aprovação só substitui o trecho se ele e seu contexto continuarem válidos; caso contrário, exige reavaliação. |
| RN-12 | Somente o gestor aceita ou rejeita sugestões. Aceitação registra nova versão oficial com autoria da operação e referência ao revisor; rejeição conserva o texto. |
| RN-13 | O responsável por uma entrega pertence ao livro. Escritores só alteram o andamento das próprias entregas; quem administra prazos depende de DEC-07. |
| RN-14 | Remover colaborador revoga seu acesso futuro sem apagar autoria, commits ou decisões anteriores. Remoção do dono e transferência de propriedade não integram o fluxo inicial. |

## 6. Requisitos funcionais e rastreabilidade

Critérios verificáveis estão nas [user stories](user-stories.md). Os IDs relacionam funcionalidades, regras, telas e futuras evidências sem repetir critérios em documentos diferentes.

| ID | O sistema deve permitir… | Histórias | Regras | Interface de referência |
| --- | --- | --- | --- | --- |
| RF-01 | Cadastrar conta e iniciar/encerrar acesso autenticado | US-01, US-02 | RN-01 | Login e cadastro |
| RF-02 | Criar, listar, consultar e atualizar livros autorizados | US-03 a US-05 | RN-01, RN-03 | Catálogo, hub e configurações |
| RF-03 | Associar, alterar papel e remover colaboradores | US-06 | RN-01, RN-02, RN-14 | Configurações |
| RF-04 | Organizar capítulos e escrever em uma branch | US-07 | RN-05, RN-07 | Editor |
| RF-05 | Criar e consultar branches | US-08 | RN-03 a RN-05 | Hub e editor |
| RF-06 | Registrar alterações em commits | US-09 | RN-05 a RN-07 | Editor |
| RF-07 | Solicitar e consultar integração à main | US-10, US-11 | RN-07, RN-08 | Editor e merges |
| RF-08 | Aprovar ou rejeitar integração preservando histórico | US-12 | RN-08 a RN-10 | Detalhe do merge |
| RF-09 | Consultar histórico e conteúdo de versões anteriores | US-13 | RN-01, RN-06 | Histórico |
| RF-10 | Propor e avaliar sugestões editoriais | US-14, US-15 | RN-11, RN-12 | Sugestões |
| RF-11 | Gerenciar prazos e acompanhar entregas | US-16, US-17 | RN-13 | Prazos |

## 7. Permissões propostas por livro

Matriz de produto, sujeita a DEC-01, DEC-02 e DEC-07; não é reprodução integral das permissões simuladas.

| Operação | Gestor | Escritor | Revisor |
| --- | --- | --- | --- |
| Consultar obra, branches, propostas e histórico | Sim | Sim | Sim |
| Atualizar metadados e colaboradores | Sim | Não | Não |
| Criar branch e registrar conteúdo | Sim, em branch de trabalho | Sim, na própria branch | Não |
| Solicitar merge | Sim | Sim, da própria branch | Não |
| Decidir merge e sugestão na main | Sim | Não | Não |
| Criar sugestão editorial | Não previsto inicialmente | Não previsto inicialmente | Sim |
| Criar, editar e excluir prazos | Sim | Não | A confirmar |
| Atualizar andamento de entrega | Sim | Somente própria | Somente própria; gestão a confirmar |

## 8. Requisitos não funcionais

Metas propostas, ainda sem evidência de cumprimento. Desempenho e capacidade serão definidos após estabelecer cenário de uso, evitando promessas arbitrárias.

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-01 | Autorização no backend em todas as operações protegidas | Tentar ler e escrever sem vínculo e com papel insuficiente; nenhuma tentativa pode revelar conteúdo ou persistir alteração |
| RNF-02 | Senhas com hash próprio para senhas, nunca em texto puro; credenciais ausentes de respostas e logs | Inspecionar persistência, configuração, respostas e logs de autenticação |
| RNF-03 | Operações compostas preservam integridade: obra/main e integração/conteúdo/histórico não ficam parcialmente gravados | Provocar falha e verificar rollback; testar unicidade e vínculos entre livros no PostgreSQL |
| RNF-04 | Dados confirmados sobrevivem ao reinício | Registrar versão, reiniciar aplicação e recuperar mesmo conteúdo e histórico |
| RNF-05 | Interface distingue carregamento, vazio, validação e falha; erro não aparece como sucesso | Verificar esses estados em catálogo, gravação e avaliação |
| RNF-06 | Fluxos principais operáveis por teclado, com rótulos, foco visível e mensagens independentes de cor | Percorrer cadastro, criação de livro, commit e avaliação por teclado; conferir foco e mensagens |
| RNF-07 | Interface utilizável em larguras de 360 px e 1280 px, com rolagem controlada em comparações extensas | Conferir formulários, navegação, editor e diff sem perda de ações |
| RNF-08 | Ambiente local reproduzível com configuração de exemplo, migrações e passos documentados | Executar fluxo principal em banco vazio seguindo documentação de execução a atualizar |
| RNF-09 | Acesso, isolamento de branches, imutabilidade e decisões concorrentes possuem verificações automatizadas relevantes | Relacionar cenários aos IDs RN/US e registrar comandos e resultados reais |

JWT, Spring Security, PostgreSQL e Flyway são meios técnicos previstos ou presentes nas dependências; não substituem os comportamentos acima. Política de sessão e stack do frontend continuam pendentes.

## 9. Conclusão e revisão

Uma história estará concluída quando seus critérios forem atendidos na interface e no backend aplicável, dados forem persistidos corretamente, permissões e falhas relevantes forem verificadas e houver evidência registrada. Um fluxo mockado não equivale à funcionalidade integrada.

Antes de implementar, revisar DEC-01 a DEC-06. Depois, implementar uma fatia completa P1 e registrar verificações reais. Estado de implementação fica separado deste catálogo; telas e tabelas, isoladamente, não concluem histórias.
