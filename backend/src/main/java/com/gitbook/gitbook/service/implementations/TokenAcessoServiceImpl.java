package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.service.TokenAcessoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TokenAcessoServiceImpl implements TokenAcessoService {
    private static final Duration PRAZO_ACESSO = Duration.ofMinutes(15);

    private final JwtEncoder encoder;
    private final String emissor;
    private final String audiencia;

    public TokenAcessoServiceImpl(@Lazy JwtEncoder encoder,
            @Value("${GITBOOK_JWT_EMISSOR:}") String emissor,
            @Value("${GITBOOK_JWT_AUDIENCIA:}") String audiencia) {
        this.encoder = encoder;
        this.emissor = emissor;
        this.audiencia = audiencia;
    }

    @Override
    public TokenAcesso emitir(UUID idUsuario, Instant expiraSessaoEm) {
        Instant agora = Instant.now();
        Instant limiteAcesso = agora.plus(PRAZO_ACESSO);
        Instant expiraEm = (expiraSessaoEm.isBefore(limiteAcesso) ? expiraSessaoEm : limiteAcesso)
                .truncatedTo(ChronoUnit.SECONDS);
        if (!agora.isBefore(expiraEm)) {
            throw new IllegalArgumentException("A sessão não tem tempo de validade restante para emitir o JWT.");
        }
        Assert.state(!emissor.isBlank() && !audiencia.isBlank(), "Configure o emissor e a audiência do JWT.");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emissor)
                .audience(List.of(audiencia))
                .subject(idUsuario.toString())
                .issuedAt(agora.truncatedTo(ChronoUnit.SECONDS))
                .expiresAt(expiraEm)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenAcesso(token, expiraEm);
    }
}
