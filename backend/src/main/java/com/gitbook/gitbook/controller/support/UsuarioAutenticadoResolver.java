package com.gitbook.gitbook.controller.support;

import com.gitbook.gitbook.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** O mecanismo de autenticação deve fornecer o e-mail em Authentication.getName(). */
@Component
@RequiredArgsConstructor
public class UsuarioAutenticadoResolver {
    private final UsuarioRepository usuarioRepository;

    public UUID resolver(Authentication autenticacao) {
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("É necessário estar autenticado.");
        }
        return usuarioRepository.findByEmail(autenticacao.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário autenticado não encontrado."))
                .getIdUsuario();
    }
}
