package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Capitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_capitulo")
    private UUID idCapitulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @NotBlank
    @Size(max = 150)
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @NotNull
    @Positive
    @Column(name = "ordem", nullable = false)
    private Integer ordem;
}
