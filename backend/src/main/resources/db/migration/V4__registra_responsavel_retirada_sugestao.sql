ALTER TABLE sugestao
    ADD COLUMN id_usuario_retirada UUID REFERENCES usuario (id_usuario);
