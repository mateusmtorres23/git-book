ALTER TABLE branch
    ADD COLUMN id_commit_origem UUID,
    ADD CONSTRAINT fk_branch_commit_origem
        FOREIGN KEY (id_commit_origem) REFERENCES commit (id_commit);

-- A criação de branches aceita apenas a linha de primeiro pai da main.
WITH RECURSIVE linha_main (id_livro, id_commit) AS (
    SELECT b.id_livro, c.id_commit
    FROM branch b
    JOIN commit c ON c.id_commit = b.id_commit_atual AND c.id_livro = b.id_livro
    WHERE b.is_main

    UNION ALL

    SELECT linha.id_livro, anterior.id_commit
    FROM linha_main linha
    JOIN commit_pai pai ON pai.id_commit_filho = linha.id_commit AND pai.ordem_pai = 1
    JOIN commit anterior ON anterior.id_commit = pai.id_commit_pai AND anterior.id_livro = linha.id_livro
) CYCLE id_commit SET ciclo USING caminho,
linha_trabalho (id_livro, id_branch, id_commit, id_branch_commit) AS (
    SELECT b.id_livro, b.id_branch, c.id_commit, c.id_branch
    FROM branch b
    JOIN commit c ON c.id_commit = b.id_commit_atual AND c.id_livro = b.id_livro
    WHERE NOT b.is_main

    UNION ALL

    SELECT linha.id_livro, linha.id_branch, anterior.id_commit, anterior.id_branch
    FROM linha_trabalho linha
    JOIN commit_pai pai ON pai.id_commit_filho = linha.id_commit AND pai.ordem_pai = 1
    JOIN commit anterior ON anterior.id_commit = pai.id_commit_pai AND anterior.id_livro = linha.id_livro
    WHERE linha.id_branch_commit = linha.id_branch
) CYCLE id_commit SET ciclo USING caminho
UPDATE branch b
SET id_commit_origem = origem.id_commit
FROM linha_trabalho origem
JOIN linha_main main ON main.id_livro = origem.id_livro AND main.id_commit = origem.id_commit
WHERE b.id_branch = origem.id_branch
    AND origem.id_branch_commit <> origem.id_branch
    AND NOT origem.ciclo
    AND NOT EXISTS (
        SELECT 1 FROM linha_main invalida
        WHERE invalida.id_livro = origem.id_livro AND invalida.ciclo
    );
