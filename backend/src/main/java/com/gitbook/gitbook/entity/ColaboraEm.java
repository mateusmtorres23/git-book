package com.gitbook.gitbook.entity;

import com.gitbook.gitbook.entity.enums.CargoColab;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "colabora_em",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_colaboracao_usuario_livro",
                columnNames = {"id_usuario", "id_livro"}
        )
)
@Getter
@NoArgsConstructor
public class ColaboraEm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_colaboracao", nullable = false, updatable = false)
    private UUID idColaboracao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @Setter
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "cargo", nullable = false, columnDefinition = "cargo_colab")
    private CargoColab cargo;
}
