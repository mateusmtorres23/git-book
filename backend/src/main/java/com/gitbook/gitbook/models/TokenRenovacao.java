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
@Table(name = "token_renovacao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRenovacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token_renovacao", updatable = false)
    private UUID idTokenRenovacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sessao_auth", nullable = false, updatable = false)
    private SessaoAuth sessao;

    @Column(name = "hash_token", nullable = false, unique = true, length = 64, updatable = false)
    private String hashToken;

    @Column(name = "consumido_em")
    private Instant consumidoEm;

    public TokenRenovacao(SessaoAuth sessao, String hashToken) {
        this.sessao = sessao;
        this.hashToken = hashToken;
    }

    public boolean foiConsumido() {
        return consumidoEm != null;
    }

    public void consumir(Instant agora) {
        if (!foiConsumido()) {
            consumidoEm = agora;
        }
    }
}
