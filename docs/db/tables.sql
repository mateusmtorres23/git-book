CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cargo_colab') THEN
        CREATE TYPE cargo_colab AS ENUM ('AUTOR', 'EDITOR', 'REVISOR');
    END IF;
END $$;

CREATE TABLE usuario (
    id_usuario   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_usuario VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    senha_hash   VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE livro (
    id_livro     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_dono      UUID NOT NULL,
    versao       VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descricao    TEXT,

    CONSTRAINT fk_livro_dono
        FOREIGN KEY (id_dono)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE branch (
    id_branch    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_livro     UUID NOT NULL,
    id_autor     UUID NOT NULL,
    nome         VARCHAR(100) NOT NULL,
    is_main      BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_branch_livro_nome UNIQUE (id_livro, nome),

    CONSTRAINT fk_branch_livro
        FOREIGN KEY (id_livro)
        REFERENCES livro (id_livro)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_branch_autor
        FOREIGN KEY (id_autor)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE capitulo (
    id_capitulo    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_livro       UUID NOT NULL,
    ordem          INTEGER NOT NULL,
    conteudo_atual TEXT,
    titulo         VARCHAR(150) NOT NULL,

    CONSTRAINT uq_capitulo_livro_ordem UNIQUE (id_livro, ordem),

    CONSTRAINT fk_capitulo_livro
        FOREIGN KEY (id_livro)
        REFERENCES livro (id_livro)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT ck_capitulo_ordem CHECK (ordem > 0)
);

CREATE TABLE commit (
    id_commit     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mensagem      VARCHAR(255) NOT NULL,
    data_criacao  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_livro      UUID NOT NULL,
    id_capitulo   UUID,
    id_autor      UUID NOT NULL,
    id_branch     UUID NOT NULL,

    CONSTRAINT fk_commit_livro
        FOREIGN KEY (id_livro)
        REFERENCES livro (id_livro)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_commit_capitulo
        FOREIGN KEY (id_capitulo)
        REFERENCES capitulo (id_capitulo)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_commit_autor
        FOREIGN KEY (id_autor)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_commit_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch (id_branch)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE commit_pai (
    id_paternidade UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_commit_filho UUID NOT NULL,
    id_commit_pai   UUID NOT NULL,

    CONSTRAINT uq_commit_pai_relacao
        UNIQUE (id_commit_filho, id_commit_pai),

    CONSTRAINT ck_commit_pai_diferente
        CHECK (id_commit_filho <> id_commit_pai),

    CONSTRAINT fk_commit_pai_filho
        FOREIGN KEY (id_commit_filho)
        REFERENCES commit (id_commit)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_commit_pai_pai
        FOREIGN KEY (id_commit_pai)
        REFERENCES commit (id_commit)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE pull_request (
    id_pull_request   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_solicitante    UUID NOT NULL,
    id_branch_destino UUID NOT NULL,
    id_branch_origem  UUID NOT NULL,
    titulo            VARCHAR(150) NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    descricao         TEXT,
    data_abertura     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_pull_request_status
        CHECK (status IN ('ABERTA', 'EM_ANALISE', 'APROVADA', 'REJEITADA', 'MERGEADA', 'FECHADA')),

    CONSTRAINT ck_pull_request_branches_diferentes
        CHECK (id_branch_origem <> id_branch_destino),

    CONSTRAINT fk_pull_request_solicitante
        FOREIGN KEY (id_solicitante)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_pull_request_branch_destino
        FOREIGN KEY (id_branch_destino)
        REFERENCES branch (id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_pull_request_branch_origem
        FOREIGN KEY (id_branch_origem)
        REFERENCES branch (id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE revisao (
    id_revisao      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_revisor      UUID NOT NULL,
    id_pull_request UUID NOT NULL,
    mensagem        VARCHAR(255),

    CONSTRAINT fk_revisao_revisor
        FOREIGN KEY (id_revisor)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_revisao_pull_request
        FOREIGN KEY (id_pull_request)
        REFERENCES pull_request (id_pull_request)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE colabora_em (
    id_colaboracao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario     UUID NOT NULL,
    id_livro       UUID NOT NULL,
    cargo          cargo_colab NOT NULL,

    CONSTRAINT uq_colaboracao_usuario_livro
        UNIQUE (id_usuario, id_livro),

    CONSTRAINT fk_colaboracao_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_colaboracao_livro
        FOREIGN KEY (id_livro)
        REFERENCES livro (id_livro)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);