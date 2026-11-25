package com.gitbook.gitbook.controller.support;

import com.gitbook.gitbook.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** O campo sub do JWT contém o UUID da conta. */
@Component
@RequiredArgsConstructor
public class UsuarioAutenticadoResolver {
    private final UsuarioRepository usuarioRepository;

    public UUID resolver(Authentication autenticacao) {
        if (!(autenticacao instanceof JwtAuthenticationToken jwt) || !jwt.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("É necessário estar autenticado.");
        }
        UUID idUsuario;
        try {
            idUsuario = UUID.fromString(jwt.getToken().getSubject());
        } catch (IllegalArgumentException | NullPointerException erro) {
            throw new AuthenticationCredentialsNotFoundException("Identidade do token inválida.", erro);
        }
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário autenticado não encontrado.");
        }
        return idUsuario;
    }
}
