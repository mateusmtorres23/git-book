package com.gitbook.gitbook.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_commit", nullable = false, updatable = false)
    private UUID idCommit;

    @NotBlank
    @Size(max = 255)
    @Column(name = "mensagem", nullable = false, length = 255, updatable = false)
    private String mensagem;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @NotNull
    @Column(name = "id_livro", nullable = false, updatable = false)
    private UUID idLivro;

    @NotNull
    @Column(name = "id_autor", nullable = false, updatable = false)
    private UUID idAutor;

    @NotNull
    @Column(name = "id_branch", nullable = false, updatable = false)
    private UUID idBranch;

    public Commit(UUID idLivro, UUID idAutor, UUID idBranch, String mensagem) {
        this.idLivro = Objects.requireNonNull(idLivro, "O livro do commit é obrigatório.");
        this.idAutor = Objects.requireNonNull(idAutor, "O autor do commit é obrigatório.");
        this.idBranch = Objects.requireNonNull(idBranch, "A branch de criação do commit é obrigatória.");
        if (mensagem == null || mensagem.isBlank() || mensagem.length() > 255) {
            throw new IllegalArgumentException("A mensagem do commit deve ter entre 1 e 255 caracteres e não pode estar em branco.");
        }
        this.mensagem = mensagem;
    }
}
