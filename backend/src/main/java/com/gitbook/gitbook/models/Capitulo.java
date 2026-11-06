package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Capitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_capitulo", nullable = false, updatable = false)
    private UUID idCapitulo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_livro", nullable = false, updatable = false)
    private Livro livro;

    public Capitulo(Livro livro) {
        this.livro = Objects.requireNonNull(livro, "O livro do capítulo é obrigatório.");
    }
}
