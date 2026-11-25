package com.gitbook.gitbook.security;

import com.gitbook.gitbook.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AutenticadorCredenciais {
    private final AuthenticationManager authenticationManager;

    public AutenticadorCredenciais(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        // Mantém as contas do banco separadas da cadeia de segurança provisória.
        var provider = new DaoAuthenticationProvider(new UsuarioDetailsService(usuarioRepository));
        provider.setPasswordEncoder(passwordEncoder);
        this.authenticationManager = new ProviderManager(provider);
    }

    public Authentication autenticar(String email, String senha) {
        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, senha));
    }
}
