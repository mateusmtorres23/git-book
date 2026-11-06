# User stories

Proposta inicial vinculada aos [requisitos](requirements.md). Prioridades indicam ordem de entrega sugerida, não implementação existente. Regras RN e decisões DEC estão nos documentos de referência.

Os critérios descrevem resultados observáveis. Validação e autorização precisam existir no backend, mesmo quando a interface bloqueia a ação. Operações sem permissão devem falhar sem alterar dados; vazio e falha de comunicação precisam ser distinguíveis.

## Épico 1 - Contas e acesso

### US-01 - Criar uma conta · P1 · RF-01

Como pessoa interessada em escrever colaborativamente, quero criar uma conta para participar de obras.

- **CA-01:** Dado nome, e-mail e senha válidos, quando concluir o cadastro, então uma única conta será criada e o sistema informará como acessar a plataforma.
- **CA-02:** Dados obrigatórios ausentes, e-mail inválido ou senha fora da política definida impedem o cadastro e apresentam orientação para correção. A política de senha precisa ser definida antes da implementação.
- **CA-03:** Um e-mail já cadastrado não gera outra conta, inclusive sob concorrência. Documentar a regra de normalização do e-mail.
- **CA-04:** Cadastro não concede privilégios sobre livros existentes nem permite escolher livremente um papel global com autoridade editorial. Depende de DEC-01.

### US-02 - Entrar e sair da plataforma · P1 · RF-01

Como usuário cadastrado, quero iniciar e encerrar meu acesso para utilizar minhas obras de forma protegida.

- **CA-01:** Credenciais válidas permitem acessar livros autorizados; credenciais inválidas recebem mensagem controlada sem expor senha ou hash.
- **CA-02:** Acesso a recurso protegido sem autenticação válida é recusado, inclusive fora da interface.
- **CA-03:** Sair remove a credencial utilizada pela interface e retorna ao acesso público. Expiração e eventual invalidação no servidor seguem a política a definir em DEC-09.

## Épico 2 - Livros e colaboração

### US-03 - Criar uma obra · P1 · RF-02

Como usuário autenticado, quero criar um livro para iniciar uma produção sob minha gestão.

- **CA-01:** Ao informar título não vazio e descrição opcional, o livro é criado com meu usuário como dono e uma única main.
- **CA-02:** A obra aparece no meu catálogo e seu hub pode ser acessado imediatamente. Representação inicial sem capítulos: DEC-04.
- **CA-03:** Se a criação da main falhar, a operação não deixa um livro parcialmente criado.

### US-04 - Encontrar e consultar minhas obras · P1 · RF-02

Como participante de livros, quero listar e buscar minhas obras para acessar o trabalho em que colaboro.

- **CA-01:** O catálogo apresenta somente livros dos quais sou dono ou colaborador; buscar por título respeita esse conjunto.
- **CA-02:** Abrir uma obra apresenta título, descrição, meu papel e acesso às áreas permitidas. Sem obras ou resultados de busca, apresenta estado vazio.
- **CA-03:** Informar diretamente o identificador de obra alheia não permite consultar seu conteúdo. Falha no carregamento apresenta erro, sem simular catálogo vazio.

### US-05 - Atualizar dados da obra · P1 · RF-02

Como gestor, quero atualizar título e descrição para manter a identificação da obra correta.

- **CA-01:** Alterações válidas são persistidas e aparecem no catálogo e no hub após nova consulta.
- **CA-02:** Título vazio é recusado, preservando dados anteriores.
- **CA-03:** Escritor, revisor e usuário sem vínculo não conseguem alterar metadados. Arquivar e excluir não integram esta história.

### US-06 - Administrar colaboradores · P1 · RF-03

Como gestor, quero associar usuários e definir papéis para organizar a equipe de cada livro.

- **CA-01:** Posso localizar conta existente por e-mail e associá-la como escritor ou revisor. Conta inexistente recebe mensagem informativa; convite por e-mail não integra a proposta.
- **CA-02:** Associar novamente a mesma pessoa ao livro não cria outro vínculo, inclusive sob concorrência, preservando RN-02.
- **CA-03:** Alterar papel atualiza permissões nas próximas operações protegidas, sem depender de trocar perfil manualmente na interface.
- **CA-04:** Remover colaborador revoga acesso ao livro e preserva autoria no histórico. O dono não pode ser removido por este fluxo.

## Épico 3 - Escrita e versionamento

### US-07 - Organizar e escrever capítulos · P1 · RF-04

Como escritor, quero criar capítulos e editar conteúdo em minha branch para desenvolver a obra sem alterar a versão oficial.

- **CA-01:** Em branch autorizada, posso criar capítulo com título e posição válidos e editar texto. Ordenação não admite posições inválidas ou duplicadas na mesma estrutura de capítulos.
- **CA-02:** Selecionar capítulo mostra conteúdo da branch escolhida, preservando conteúdo confirmado das outras branches e da main.
- **CA-03:** A interface distingue edição ainda não confirmada de conteúdo registrado por commit e avisa antes de navegação que descartaria alterações locais.
- **CA-04:** Revisor não grava conteúdo; escritor não grava na main ou na branch de outro escritor. Persistência de rascunhos, remoção e reordenação de capítulos: DEC-04.

### US-08 - Criar uma branch de trabalho · P1 · RF-05

Como escritor, quero abrir uma branch a partir da main para trabalhar sobre uma base conhecida.

- **CA-01:** Nome válido e ainda não utilizado no livro cria branch vinculada ao livro, responsável e versão de origem identificável.
- **CA-02:** A branch começa com conteúdo da versão escolhida da main; commits futuros na main não modificam automaticamente sua base.
- **CA-03:** Nome duplicado ou reservado é recusado; a branch aparece no seletor e hub após criação confirmada.
- **CA-04:** Não posso criar branch usando livro ou versão sem autorização. Representação do responsável e ponta da branch: DEC-04.

### US-09 - Registrar uma versão · P1 · RF-06

Como escritor, quero confirmar alterações com uma mensagem para registrar a evolução do meu trabalho.

- **CA-01:** Havendo alterações e mensagem não vazia, confirmar cria commit com conteúdo recuperável, autor autenticado, data, branch e ancestralidade correspondente.
- **CA-02:** Sem alterações ou com mensagem inválida, a operação não cria commit vazio e explica o impedimento.
- **CA-03:** Nova edição não modifica commits anteriores; erro de gravação não é apresentado como versão salva.
- **CA-04:** Se a ponta da branch mudou desde a edição, o sistema impede sobrescrita silenciosa e solicita atualização da base antes da confirmação.

## Épico 4 - Integração e rastreabilidade

### US-10 - Enviar um Pull Request · P1 · RF-07

Como escritor, quero enviar alterações confirmadas da minha branch em um Pull Request para que o gestor ou o revisor possa analisá-las.

- **CA-01:** Posso informar título e descrição para propor alterações da branch sob minha responsabilidade à main do mesmo livro.
- **CA-02:** O Pull Request registra solicitante, data, origem, destino e os commits existentes no momento do envio. Edições sem commit não são incluídas.
- **CA-03:** Origem igual ao destino, branches de livros diferentes ou ausência de diferenças elegíveis impedem abertura.
- **CA-04:** O Pull Request aparece aberto para acompanhamento. Novos commits na branch não entram automaticamente nele; o escritor pode enviar outro Pull Request com essas alterações.

### US-11 - Analisar uma proposta · P1 · RF-07

Como participante da obra, quero consultar propostas e diferenças para compreender o que foi submetido à avaliação.

- **CA-01:** A listagem distingue solicitações abertas e encerradas por decisão e permite abrir seu detalhe.
- **CA-02:** O detalhe apresenta solicitante, título, descrição, branches, versões comparadas e alterações, identificando adições e remoções.
- **CA-03:** O conteúdo mostrado corresponde aos commits registrados no Pull Request. Commits posteriores não alteram o Pull Request existente.
- **CA-04:** Gestor e revisor podem receber ações de decisão conforme suas permissões; o acesso direto à operação continua sujeito à autorização no servidor.

### US-12 - Aceitar ou rejeitar um Pull Request · P1 · RF-08

Como gestor ou revisor, quero aceitar ou rejeitar um Pull Request para controlar o conteúdo que passa a compor a versão oficial.

- **CA-01:** Aceitar um Pull Request aberto, sem conflito e sem sugestões ativas incorpora suas alterações à main, registra a integração no histórico e encerra o Pull Request como mergeado na mesma operação lógica.
- **CA-02:** Rejeitar um Pull Request com justificativa encerra a solicitação como rejeitada, preservando a main e os commits do escritor.
- **CA-03:** Antes de aceitar, o sistema compara o Pull Request com a versão atual da main. Commits posteriores na branch de origem não entram automaticamente na decisão.
- **CA-04:** Conflito impede a aceitação e mantém a main intacta. O escritor pode corrigir o código e enviar outro Pull Request.
- **CA-05:** Decisões repetidas ou concorrentes sobre o mesmo Pull Request produzem no máximo uma integração. Enviar outro Pull Request não altera automaticamente o status do anterior.

### US-13 - Consultar histórico e versões · P1 · RF-09

Como participante da obra, quero consultar commits e versões anteriores para compreender a evolução e recuperar a leitura de conteúdo passado.

- **CA-01:** O histórico identifica autor, data, mensagem e branch dos commits e permite filtrar por branch.
- **CA-02:** Selecionar commit apresenta conteúdo daquela versão, inclusive após novas edições e merges.
- **CA-03:** Leitura histórica não altera conteúdo atual; restauração de versão antiga por nova escrita não integra esta história.
- **CA-04:** Usuário sem vínculo não acessa histórico; sem commits, a interface informa esse estado.

## Épico 5 - Revisão editorial

### US-14 - Propor uma sugestão · P2 · RF-10

Como revisor, quero sugerir uma alteração em um trecho de um Pull Request sem modificar o conteúdo diretamente.

- **CA-01:** Na leitura do Pull Request, posso selecionar um trecho e sugerir uma melhoria, preservando o Pull Request, capítulo, posição e versão analisada.
- **CA-02:** A sugestão nasce pendente, identifica seu autor e fica vinculada ao Pull Request; o conteúdo da branch e da main permanece inalterado.
- **CA-03:** Trecho sem correspondência na versão informada ou dados obrigatórios ausentes impedem registro.

### US-15 - Retirar uma sugestão · P2 · RF-10

Como revisor, quero retirar uma sugestão para permitir que o Pull Request continue sendo analisado.

- **CA-01:** Posso consultar o trecho original, a proposta, a justificativa, o revisor e o Pull Request ao qual a sugestão pertence.
- **CA-02:** Retirar uma sugestão deixa de bloquear a aceitação do Pull Request, mas mantém seu registro no histórico.
- **CA-03:** Retirar uma sugestão não altera o conteúdo do Pull Request, da branch ou da main.
- **CA-04:** Criar um novo Pull Request não altera as sugestões do Pull Request anterior.

## Épico 6 - Prazos e entregas

### US-16 - Organizar entregas · P2 · RF-11

Como gestor, quero definir entregas, responsáveis e prazos para acompanhar a produção do livro.

- **CA-01:** Posso criar entrega com título, descrição opcional, responsável participante do livro e data limite, e consultar dados após nova leitura.
- **CA-02:** Posso editar ou excluir entrega com confirmação; dados inválidos e responsáveis de outra obra são recusados.
- **CA-03:** Entregas aparecem no livro correspondente com responsável, prazo e situação. A interface informa atraso a partir do prazo vencido e entrega ainda não concluída.
- **CA-04:** Escritores não administram entregas alheias. Permissão administrativa do revisor, fuso de apresentação e aceitação de datas passadas: DEC-07.

### US-17 - Atualizar minhas entregas · P2 · RF-11

Como colaborador responsável por uma entrega, quero atualizar seu andamento para que a equipe acompanhe meu trabalho.

- **CA-01:** Posso identificar minhas entregas e alterar estado entre pendente, em andamento e concluída, conforme transições a definir em DEC-07.
- **CA-02:** Concluir remove indicação de atraso ativo, preservando prazo e vínculo; concluir tarefa não aprova merges automaticamente.
- **CA-03:** Colaborador sem permissão administrativa não altera tarefa de outra pessoa. Revogar vínculo com a obra impede novas atualizações.

## Dependências para implementação

1. Resolver papéis, acesso e modelo de versionamento (DEC-01 a DEC-06).
2. Entregar contas e livros (US-01 a US-06).
3. Entregar escrita com isolamento e histórico recuperável (US-07 a US-09 e US-13).
4. Completar submissão, comparação e decisão (US-10 a US-12).
5. Evoluir revisão e entregas (US-14 a US-17), conforme capacidade da equipe.

A ordem não substitui decomposição técnica, estimativa ou divisão de tarefas. Cada etapa precisa funcionar com persistência real antes de ser declarada concluída.
