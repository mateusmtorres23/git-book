package com.gitbook.gitbook.service;

import com.gitbook.gitbook.service.SessaoAuthService.Renovacao;
import com.gitbook.gitbook.service.TokenAcessoService.TokenAcesso;

import java.util.Optional;

public interface AuthService {
    SessaoAutenticada login(String email, String senha);

    Optional<SessaoAutenticada> renovar(String tokenRenovacao);

    void logout(String tokenRenovacao);

    record SessaoAutenticada(TokenAcesso acesso, Renovacao renovacao) {
    }
}
