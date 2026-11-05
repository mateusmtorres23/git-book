package com.gitbook.gitbook.models;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;
}
