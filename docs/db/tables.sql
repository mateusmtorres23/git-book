-- Esquema de referência para um banco vazio, alinhado aos modelos JPA.
-- A adoção pelo Flyway depende da verificação do histórico da V1 existente.

CREATE TYPE cargo_colab AS ENUM ('GESTOR', 'ESCRITOR', 'REVISOR');

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_usuario VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE livro (
    id_livro UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_dono UUID NOT NULL REFERENCES usuario (id_usuario),
    versao VARCHAR(20) NOT NULL,
    nome_livro VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branch (
    id_branch UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_livro UUID NOT NULL REFERENCES livro (id_livro),
    id_autor UUID NOT NULL REFERENCES usuario (id_usuario),
    id_commit_atual UUID,
    nome VARCHAR(100) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_branch_livro_nome UNIQUE (id_livro, nome)
);

CREATE UNIQUE INDEX uq_branch_main_por_livro ON branch (id_livro) WHERE is_main;

CREATE TABLE capitulo (
    id_capitulo UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_livro UUID NOT NULL REFERENCES livro (id_livro)
);

CREATE TABLE commit (
    id_commit UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_livro UUID NOT NULL REFERENCES livro (id_livro),
    id_autor UUID NOT NULL REFERENCES usuario (id_usuario),
    id_branch UUID NOT NULL REFERENCES branch (id_branch),
    mensagem VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE branch
    ADD CONSTRAINT fk_branch_commit_atual
    FOREIGN KEY (id_commit_atual) REFERENCES commit (id_commit);

CREATE TABLE commit_pai (
    id_paternidade UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_commit_filho UUID NOT NULL REFERENCES commit (id_commit),
    id_commit_pai UUID NOT NULL REFERENCES commit (id_commit),
    ordem_pai INTEGER NOT NULL,
    CONSTRAINT uq_commit_pai_relacao UNIQUE (id_commit_filho, id_commit_pai),
    CONSTRAINT uq_commit_pai_ordem UNIQUE (id_commit_filho, ordem_pai),
    CONSTRAINT ck_commit_pai_diferente CHECK (id_commit_filho <> id_commit_pai),
    CONSTRAINT ck_commit_pai_ordem CHECK (ordem_pai IN (1, 2))
);

CREATE TABLE conteudo_capitulo (
    id_versao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_commit UUID NOT NULL,
    id_capitulo UUID NOT NULL,
    titulo VARCHAR(150),
    ordem INTEGER,
    conteudo TEXT,
    excluido BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_conteudo_commit_capitulo
        UNIQUE (id_commit, id_capitulo),

    CONSTRAINT uq_conteudo_commit_ordem
        UNIQUE (id_commit, ordem),

    CONSTRAINT ck_conteudo_estado
        CHECK (
            (
                excluido = TRUE
                AND titulo IS NULL
                AND ordem IS NULL
                AND conteudo IS NULL
            )
            OR
            (
                excluido = FALSE
                AND titulo IS NOT NULL
                AND titulo ~ '[^[:space:]]'
                AND ordem IS NOT NULL
                AND ordem > 0
                AND conteudo IS NOT NULL
            )
        ),

    CONSTRAINT fk_conteudo_commit
        FOREIGN KEY (id_commit) REFERENCES commit (id_commit),

    CONSTRAINT fk_conteudo_capitulo
        FOREIGN KEY (id_capitulo) REFERENCES capitulo (id_capitulo)
);

CREATE TABLE pull_request (
    id_pull_request UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_solicitante UUID NOT NULL REFERENCES usuario (id_usuario),
    id_branch_destino UUID NOT NULL REFERENCES branch (id_branch),
    id_branch_origem UUID NOT NULL REFERENCES branch (id_branch),
    id_commit_origem UUID NOT NULL REFERENCES commit (id_commit),
    id_commit_destino_abertura UUID REFERENCES commit (id_commit),
    id_commit_merge UUID REFERENCES commit (id_commit),
    id_autor_decisao UUID REFERENCES usuario (id_usuario),
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    justificativa_rejeicao TEXT,
    data_fechamento TIMESTAMP,
    CONSTRAINT ck_pull_request_status CHECK (status IN ('ABERTA', 'REJEITADA', 'MERGEADA')),
    CONSTRAINT ck_pull_request_branches_diferentes CHECK (id_branch_origem <> id_branch_destino)
);

CREATE TABLE colabora_em (
    id_colaboracao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    id_livro UUID NOT NULL,
    cargo cargo_colab NOT NULL,

    CONSTRAINT uq_colaboracao_usuario_livro
        UNIQUE (id_usuario, id_livro),

    CONSTRAINT fk_colaboracao_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),

    CONSTRAINT fk_colaboracao_livro
        FOREIGN KEY (id_livro) REFERENCES livro (id_livro)
);

CREATE TABLE sugestao (
    id_sugestao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_capitulo UUID NOT NULL REFERENCES capitulo (id_capitulo),
    id_usuario UUID NOT NULL REFERENCES usuario (id_usuario),
    id_pull_request UUID NOT NULL REFERENCES pull_request (id_pull_request),
    categoria VARCHAR(50) NOT NULL,
    trecho_original TEXT NOT NULL,
    trecho_sugerido TEXT NOT NULL,
    justificativa TEXT NOT NULL,
    posicao_inicial INTEGER NOT NULL,
    posicao_fim INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_sugestao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_avaliacao TIMESTAMP,
    CONSTRAINT ck_sugestao_status CHECK (status IN ('PENDENTE', 'RETIRADA'))
);

CREATE TABLE deadlines (
    id_deadline UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    id_livro UUID NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDENTE',
    data_limite TIMESTAMP NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_deadline_status
        CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),

    CONSTRAINT fk_deadline_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),

    CONSTRAINT fk_deadline_livro
        FOREIGN KEY (id_livro) REFERENCES livro (id_livro)
);

CREATE TABLE sessao_auth (
    id_sessao_auth UUID PRIMARY KEY,
    id_usuario UUID NOT NULL REFERENCES usuario (id_usuario),
    criada_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    revogada_em TIMESTAMPTZ,
    CONSTRAINT ck_sessao_auth_expiracao CHECK (expira_em > criada_em)
);

CREATE INDEX ix_sessao_auth_usuario ON sessao_auth (id_usuario);

CREATE TABLE token_renovacao (
    id_token_renovacao UUID PRIMARY KEY,
    id_sessao_auth UUID NOT NULL REFERENCES sessao_auth (id_sessao_auth),
    hash_token VARCHAR(64) NOT NULL,
    consumido_em TIMESTAMPTZ,
    CONSTRAINT uq_token_renovacao_hash UNIQUE (hash_token)
);

CREATE INDEX ix_token_renovacao_sessao ON token_renovacao (id_sessao_auth);

CREATE UNIQUE INDEX uq_token_renovacao_pendente_por_sessao
    ON token_renovacao (id_sessao_auth) WHERE consumido_em IS NULL;
