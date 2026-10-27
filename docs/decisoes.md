# Decisões e pendências de planejamento

Registro inicial de 27/10/2026. **As propostas abaixo ainda precisam de revisão.** As hipóteses permitem escrever requisitos coerentes e explicitam questões anteriores à implementação. O formato da entrega da disciplina é livre, conforme informação do autor.

## DEC-01 - Papéis por livro

**Observado:** README e protótipo usam gestor/escritor/revisor; o protótipo troca perfil globalmente. SQL registra cargo em `colabora_em` como AUTOR/EDITOR/REVISOR e dono separado em `livro`.

**Proposta:** gestor é dono da obra; escritor/revisor são papéis locais de colaboração. Qualquer conta autenticada pode criar obra. Usar Gestor/Escritor/Revisor no produto e definir mapeamento de EDITOR/AUTOR antes de implementar.

**A resolver:** confirmar se EDITOR significa gestor, se dono também terá linha de colaboração e se pode acumular papéis. RN-02 permite um único vínculo por usuário/livro; acumular papéis exigiria outra representação, sem duplicar vínculos.

## DEC-02 - Quem escreve e em qual branch

**Observado:** router descreve gestor como leitor do editor, mas o produto lhe atribui autoridade editorial. MER não identifica dono de branch; SQL possui `branch.id_autor`.

**Proposta:** gestor escreve em branch de trabalho e decide integrações; escritor grava apenas em branch própria; revisor lê e sugere. Escrita na main fica restrita à integração e aceitação de sugestões.

**A resolver:** confirmar escrita do gestor e necessidade de branches compartilhadas. Histórias adotam branches individuais como simplificação inicial.

## DEC-03 - Sincronização do MER e SQL

| Tema | MER enviado | SQL atual | Encaminhamento |
| --- | --- | --- | --- |
| Nome do livro | `nome_book` | Ausente | Acrescentar título ao revisar modelagem física |
| Conteúdo | `Conteudo_Capitulo` e `Versão_Atual` | `capitulo.conteudo_atual` e referência opcional de capítulo no commit | Resolver DEC-04 antes de migrar |
| Sugestões | Entidade com versão, trecho, posições e decisão | Ausente; existe `revisao` ligada a PR | Distinguir revisão de PR de sugestão textual; não assumir equivalência |
| Prazos | `Deadlines` com usuário, livro e status | Ausente | Resolver permissões e estados em DEC-07 |
| Pull request | Capítulo e data de fechamento | Ambos ausentes | Resolver granularidade em DEC-05 e registrar encerramento |
| Responsável da branch | Ausente | `id_autor` | Definir propriedade conforme DEC-02 |

O script em `docs/db/` é referência de modelagem, sem migração Flyway correspondente. Esta etapa não altera SQL ou PDF nem declara os modelos sincronizados.

## DEC-04 - Conteúdo de cada branch

**Observado:** MER associa conteúdo a capítulo/commit, mas `Versão_Atual` aponta para capítulo/conteúdo sem branch. Há pais de commit, sem ponta explícita na branch. Cardinalidade mínima de capítulos no desenho precisa ser conciliada com criação de obra vazia.

**Problema:** um conteúdo atual por capítulo não identifica sozinho o estado de branches divergentes. Ordenar commits por data não define com segurança ponta ou snapshot da branch.

**Proposta para discussão:** cada branch aponta para sua ponta; cada commit registra pais e permite reconstruir versão consistente dos capítulos. Escolher entre snapshot completo ou capítulos alterados com herança dos demais. Nova branch tem base identificável na main; histórico forma grafo sem ciclos.

**A resolver antes de US-07 a US-09:** obra vazia e commit inicial; título/ordem de capítulos por versão; conteúdo vazio; função de `Versão_Atual`; persistência de rascunhos; remoção/reordenação de capítulos. MER indica até dois pais, mas SQL só impede autoparentesco e relação duplicada: limite de pais, ausência de ciclos e coerência entre livros ainda precisam de garantia.

## DEC-05 - Escopo e decisão de merge

**Observado:** MER liga PR a capítulo; README/protótipo descrevem integração de branch. SQL admite ABERTA, EM_ANALISE, APROVADA, REJEITADA, MERGEADA e FECHADA; protótipo resume a pending/approved/rejected.

**Proposta:** destino main, aprovação seguida de integração na mesma operação. ABERTA é estado inicial; MERGEADA/REJEITADA são encerramentos do núcleo. Conflito não encerra proposta nem altera main.

**A resolver:** PR por capítulo ou conjunto; congelamento da versão submetida ou atualização explícita após novos commits; duplicidade de solicitações abertas; comparação de três vias ou outra detecção; justificativa e decisor; necessidade de estados intermediários e fechamento sem decisão. Não remover estados do SQL sem essa revisão.

**Limite:** os requisitos exigem detectar impedimentos e preservar main; ainda não especificam algoritmo de merge nem interface de resolução de conflitos.

## DEC-06 - Sugestões desatualizadas

**Observado:** MER identifica capítulo, commit e posições; protótipo substitui texto no primeiro capítulo simulado.

**Proposta:** vincular sugestão à versão lida e revalidar trecho/posição antes de aceitar. Preservar trecho original e registrar resultado, data e avaliador. Aceitar produz commit na main; rejeitar registra decisão.

**A resolver:** categorias, unidade das posições em Unicode, interpretação após edição e representação de sugestão desatualizada. Substituição pela primeira ocorrência não garante o trecho correto.

## DEC-07 - Prazos e responsáveis

**Observado:** protótipo permite gestão por gestor/revisor. MER liga entrega a usuário/livro, sem separar criador/responsável; SQL não contém a entidade.

**Proposta:** usuário da entrega é responsável; gestor administra; colaborador atualiza a própria entrega. Atraso é condição calculada de prazo vencido e tarefa não concluída.

**A resolver:** gestão pelo revisor, responsável obrigatório, estados/transições, reabertura, datas passadas, fuso e destino de tarefas ao remover colaborador. Responsabilidade obrigatória nas histórias é hipótese revisável.

## DEC-08 - Configurações adicionais

**Observado:** protótipo apresenta arquivamento, exclusão da obra e proteção configurável da branch.

**Proposta:** P3 até definir efeitos sobre acesso e histórico. Proteção da main é regra fixa do núcleo; desligá-la não está incluído.

**A resolver:** necessidade acadêmica, exclusão, confirmação e retenção. Tela existente não inclui automaticamente uma função na primeira entrega integrada.

## DEC-09 - Decisões técnicas posteriores

`pom.xml` declara Java 17, Spring Boot 4.1.1, JPA, Security, Flyway e PostgreSQL. Código contém classe inicial e teste de contexto. Frontend atual é protótipo estático; React/Vite/Next citados no README ainda não constituem implementação.

Após estabilizar requisitos, definir stack única do frontend, sessão/expiração/logout, política de senha, limites de entrada, contrato de erros/API, migrações e execução local. Não copiar arquitetura ou controles do Simulacred sem necessidade deste domínio.

## Como registrar decisões futuras

Ao resolver item, registrar decisão, motivo, data e documentos afetados. Atualizar requisitos, histórias e modelagem em conjunto. Preservar alternativas apenas quando explicarem escolha. Evidência de teste e andamento ficam registrados quando existirem, sem apresentar intenção como conclusão.
