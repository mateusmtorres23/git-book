package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.models.SessaoAuth;
import com.gitbook.gitbook.models.TokenRenovacao;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.SessaoAuthRepository;
import com.gitbook.gitbook.repository.TokenRenovacaoRepository;
import com.gitbook.gitbook.service.SessaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SessaoAuthServiceImpl implements SessaoAuthService {
    private static final Duration PRAZO_SESSAO = Duration.ofDays(7);
    private static final Pattern FORMATO_TOKEN = Pattern.compile("[A-Za-z0-9_-]{43}");

    private final SessaoAuthRepository sessaoRepository;
    private final TokenRenovacaoRepository tokenRepository;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Renovacao iniciar(Usuario usuario) {
        Instant agora = Instant.now();
        SessaoAuth sessao = sessaoRepository.save(new SessaoAuth(usuario, agora, agora.plus(PRAZO_SESSAO)));
        return emitirToken(sessao);
    }

    @Override
    @Transactional
    public Optional<Renovacao> renovar(String token) {
        if (!tokenTemFormatoValido(token)) {
            return Optional.empty();
        }

        String hash = calcularHash(token);
        SessaoAuth sessao = bloquearSessao(hash).orElse(null);
        Instant agora = Instant.now();
        if (sessao == null || !sessao.estaAtivaEm(agora)) {
            return Optional.empty();
        }

        TokenRenovacao anterior = tokenRepository.findByHashToken(hash).orElse(null);
        if (anterior == null) {
            return Optional.empty();
        }
        if (anterior.foiConsumido()) {
            sessao.revogar(agora);
            return Optional.empty();
        }

        anterior.consumir(agora);
        // Atualiza o índice de token pendente antes de inserir o sucessor.
        tokenRepository.flush();
        return Optional.of(emitirToken(sessao));
    }

    @Override
    @Transactional
    public void encerrar(String token) {
        if (tokenTemFormatoValido(token)) {
            bloquearSessao(calcularHash(token)).ifPresent(sessao -> sessao.revogar(Instant.now()));
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void revogarSessoesDoUsuario(UUID idUsuario) {
        sessaoRepository.revogarAtivasDoUsuario(idUsuario, Instant.now());
    }

    private Optional<SessaoAuth> bloquearSessao(String hash) {
        return tokenRepository.findIdSessaoByHashToken(hash).flatMap(sessaoRepository::findByIdForUpdate);
    }

    private Renovacao emitirToken(SessaoAuth sessao) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenRepository.save(new TokenRenovacao(sessao, calcularHash(token)));
        return new Renovacao(sessao.getUsuario().getIdUsuario(), token, sessao.getExpiraEm());
    }

    private boolean tokenTemFormatoValido(String token) {
        return token != null && FORMATO_TOKEN.matcher(token).matches();
    }

    private String calcularHash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException erro) {
            throw new IllegalStateException("SHA-256 indisponível.", erro);
        }
    }
}
