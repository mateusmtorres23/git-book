package com.gitbook.gitbook.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "commit_pai",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_commit_pai_relacao",
                columnNames = {"id_commit_filho", "id_commit_pai"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommitPai {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_paternidade", nullable = false, updatable = false)
    private UUID idPaternidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit_filho", nullable = false)
    private Commit commitFilho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit_pai", nullable = false)
    private Commit commitPai;
}
