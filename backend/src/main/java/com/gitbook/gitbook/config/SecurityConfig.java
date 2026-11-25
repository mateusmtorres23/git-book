package com.gitbook.gitbook.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    private final ObjectMapper json;

    public SecurityConfig(ObjectMapper json) {
        this.json = json;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain rotasPublicas(HttpSecurity http) throws Exception {
        var caminhos = PathPatternRequestMatcher.withDefaults();
        RequestMatcher operacoesAuth = new OrRequestMatcher(
                caminhos.matcher(HttpMethod.POST, "/api/auth/login"),
                caminhos.matcher(HttpMethod.POST, "/api/auth/refresh"),
                caminhos.matcher(HttpMethod.POST, "/api/auth/logout"));
        var csrf = new CookieCsrfTokenRepository();
        csrf.setCookieCustomizer(cookie -> cookie.httpOnly(true).secure(true)
                .sameSite("Strict").path("/api/auth"));

        return http
                .securityMatcher(new OrRequestMatcher(operacoesAuth,
                        caminhos.matcher(HttpMethod.GET, "/api/auth/csrf"),
                        caminhos.matcher(HttpMethod.POST, "/api/users")))
                .authorizeHttpRequests(acesso -> acesso.anyRequest().permitAll())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(config -> config.csrfTokenRepository(csrf).requireCsrfProtectionMatcher(operacoesAuth))
                .exceptionHandling(erros -> erros.accessDeniedHandler((request, response, erro) ->
                        responderProblema(response, HttpStatus.FORBIDDEN, "Token CSRF ausente ou inválido.")))
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public SecurityFilterChain rotasProtegidas(HttpSecurity http, ConfiguracaoJwt jwt) throws Exception {
        return http
                .authorizeHttpRequests(acesso -> acesso.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(bearer -> bearer.jwt(config -> config.decoder(jwt.criarDecoder()))
                        .authenticationEntryPoint((request, response, erro) -> responderSemBearer(response))
                        .accessDeniedHandler((request, response, erro) ->
                                responderProblema(response, HttpStatus.FORBIDDEN, "Acesso negado.")))
                .exceptionHandling(erros -> erros.authenticationEntryPoint(
                        (request, response, erro) -> responderSemBearer(response)))
                .build();
    }

    private void responderSemBearer(HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        responderProblema(response, HttpStatus.UNAUTHORIZED, "Token de acesso ausente ou inválido.");
    }

    private void responderProblema(HttpServletResponse response, HttpStatus status, String detalhe) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(status, detalhe));
    }
}
