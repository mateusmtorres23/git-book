package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sugestao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id_sugestao;

    @ManyToMany
    @JoinColumn(name = "capitulo", nullable = false)
    Capitulo capitulo;

    @Column(name = "categoria", length = 50, nullable = false)
    private String categoria;

    @Column(name = "trecho_original", nullable = false, columnDefinition = "TEXT")
}
