package com.gitbook.gitbook.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idCommit;

    @Column(name = "mensagem", nullable = false, length = 255)
    private String mensagem;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "id_livro", nullable = false)
    private UUID idLivro;

    @Column(name = "id_capitulo")
    private UUID idCapitulo;

    @Column(name = "id_autor", nullable = false)
    private UUID idAutor;

    @Column(name = "id_branch", nullable = false)
    private UUID idBranch;
}
