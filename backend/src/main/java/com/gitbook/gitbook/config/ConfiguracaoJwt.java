package com.gitbook.gitbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
public class ConfiguracaoJwt {
    private final String caminhoChavePublica;
    private final String caminhoChavePrivada;
    private final String emissor;
    private final String audiencia;

    public ConfiguracaoJwt(
            @Value("${GITBOOK_JWT_CHAVE_PUBLICA:}") String caminhoChavePublica,
            @Value("${GITBOOK_JWT_CHAVE_PRIVADA:}") String caminhoChavePrivada,
            @Value("${GITBOOK_JWT_EMISSOR:}") String emissor,
            @Value("${GITBOOK_JWT_AUDIENCIA:}") String audiencia) {
        this.caminhoChavePublica = caminhoChavePublica;
        this.caminhoChavePrivada = caminhoChavePrivada;
        this.emissor = emissor;
        this.audiencia = audiencia;
    }

    @Bean
    @Lazy
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withKeyPair(lerChavePublica(), lerChave(caminhoChavePrivada, RsaKeyConverters.pkcs8()))
                .algorithm(SignatureAlgorithm.RS256)
                .build();
    }

    // A configuração da cadeia Bearer vai chamar esta fábrica explicitamente.
    public JwtDecoder criarDecoder() {
        Assert.hasText(emissor, "Configure GITBOOK_JWT_EMISSOR.");
        Assert.hasText(audiencia, "Configure GITBOOK_JWT_AUDIENCIA.");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(lerChavePublica())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(emissor),
                new JwtTimestampValidator(Duration.ZERO),
                new JwtClaimValidator<Instant>("exp", Objects::nonNull),
                new JwtClaimValidator<List<String>>("aud", valores -> valores != null && valores.contains(audiencia))));
        return decoder;
    }

    private RSAPublicKey lerChavePublica() {
        RSAPublicKey chave = lerChave(caminhoChavePublica, RsaKeyConverters.x509());
        Assert.isTrue(chave.getModulus().bitLength() >= 2048, "A chave RSA deve ter pelo menos 2048 bits.");
        return chave;
    }

    private <T> T lerChave(String caminho, Converter<InputStream, T> conversor) {
        Assert.hasText(caminho, "Configure os caminhos das chaves JWT.");
        try (InputStream arquivo = Files.newInputStream(Path.of(caminho))) {
            return conversor.convert(arquivo);
        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível ler a chave JWT configurada.", erro);
        }
    }
}
