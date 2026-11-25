package com.gitbook.gitbook.service;

import java.time.Instant;
import java.util.UUID;

public interface TokenAcessoService {
    TokenAcesso emitir(UUID idUsuario, Instant expiraSessaoEm);

    record TokenAcesso(String token, Instant expiraEm) {
        @Override
        public String toString() {
            return "TokenAcesso[expiraEm=" + expiraEm + "]";
        }
    }
}
