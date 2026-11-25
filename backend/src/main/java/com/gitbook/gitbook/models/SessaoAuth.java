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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessao_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessaoAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_sessao_auth", updatable = false)
    private UUID idSessaoAuth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "expira_em", nullable = false, updatable = false)
    private Instant expiraEm;

    @Column(name = "revogada_em")
    private Instant revogadaEm;

    public SessaoAuth(Usuario usuario, Instant criadaEm, Instant expiraEm) {
        this.usuario = usuario;
        this.criadaEm = criadaEm;
        this.expiraEm = expiraEm;
    }

    public boolean estaAtivaEm(Instant agora) {
        return revogadaEm == null && agora.isBefore(expiraEm);
    }

    public void revogar(Instant agora) {
        if (revogadaEm == null) {
            revogadaEm = agora;
        }
    }
}
