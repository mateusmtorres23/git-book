CREATE TYPE cargo_colab AS ENUM ('GESTOR', 'ESCRITOR', 'REVISOR');

CREATE TABLE colabora_em (
    id_colaboracao UUID PRIMARY KEY,
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

CREATE TABLE deadlines (
    id_deadline UUID PRIMARY KEY,
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

CREATE TABLE conteudo_capitulo (
    id_versao UUID PRIMARY KEY,
    id_commit UUID NOT NULL,
    id_capitulo UUID NOT NULL,
    conteudo TEXT NOT NULL,

    CONSTRAINT fk_conteudo_commit
        FOREIGN KEY (id_commit) REFERENCES commit (id_commit),

    CONSTRAINT fk_conteudo_capitulo
        FOREIGN KEY (id_capitulo) REFERENCES capitulo (id_capitulo)
);

CREATE TABLE versao_atual (
    id_versao_atual UUID PRIMARY KEY,
    id_capitulo UUID NOT NULL,
    id_conteudo UUID NOT NULL,

    CONSTRAINT uq_versao_atual_capitulo
        UNIQUE (id_capitulo),

    CONSTRAINT uq_versao_atual_conteudo
        UNIQUE (id_conteudo),

    CONSTRAINT fk_versao_atual_capitulo
        FOREIGN KEY (id_capitulo) REFERENCES capitulo (id_capitulo),

    CONSTRAINT fk_versao_atual_conteudo
        FOREIGN KEY (id_conteudo) REFERENCES conteudo_capitulo (id_versao)
);