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
