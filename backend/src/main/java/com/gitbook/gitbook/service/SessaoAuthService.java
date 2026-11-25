package com.gitbook.gitbook.service;

import com.gitbook.gitbook.models.Usuario;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessaoAuthService {
    /** A transação de login deve bloquear a conta e validar as credenciais antes desta chamada. */
    Renovacao iniciar(Usuario usuario);

    /** Uma recusa pode revogar a sessão. O chamador deve concluir a transação antes de responder. */
    Optional<Renovacao> renovar(String token);

    void encerrar(String token);

    /** Executar na mesma transação que altera a senha da conta. */
    void revogarSessoesDoUsuario(UUID idUsuario);

    record Renovacao(UUID idUsuario, String token, Instant expiraEm) {
        @Override
        public String toString() {
            return "Renovacao[idUsuario=" + idUsuario + ", expiraEm=" + expiraEm + "]";
        }
    }
}
