package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.security.AutenticadorCredenciais;
import com.gitbook.gitbook.service.AuthService;
import com.gitbook.gitbook.service.SessaoAuthService;
import com.gitbook.gitbook.service.SessaoAuthService.Renovacao;
import com.gitbook.gitbook.service.TokenAcessoService;
import com.gitbook.gitbook.service.support.EmailUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";

    private final UsuarioRepository usuarioRepository;
    private final AutenticadorCredenciais autenticadorCredenciais;
    private final SessaoAuthService sessaoAuthService;
    private final TokenAcessoService tokenAcessoService;

    @Override
    @Transactional
    public SessaoAutenticada login(String email, String senha) {
        String emailNormalizado = EmailUsuario.normalizar(email);
        Usuario usuario = usuarioRepository.findIdByEmail(emailNormalizado)
                .flatMap(usuarioRepository::findByIdForUpdate)
                .filter(conta -> conta.getEmail().equals(emailNormalizado))
                .orElse(null);

        // O provedor também executa a verificação de senha quando o e-mail não existe.
        try {
            autenticadorCredenciais.autenticar(emailNormalizado, senha);
        } catch (BadCredentialsException erro) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS, erro);
        }
        if (usuario == null) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }
        return emitirAcesso(sessaoAuthService.iniciar(usuario));
    }

    @Override
    @Transactional
    public Optional<SessaoAutenticada> renovar(String tokenRenovacao) {
        return sessaoAuthService.renovar(tokenRenovacao).map(this::emitirAcesso);
    }

    @Override
    public void logout(String tokenRenovacao) {
        sessaoAuthService.encerrar(tokenRenovacao);
    }

    private SessaoAutenticada emitirAcesso(Renovacao renovacao) {
        var acesso = tokenAcessoService.emitir(renovacao.idUsuario(), renovacao.expiraEm());
        return new SessaoAutenticada(acesso, renovacao);
    }
}
