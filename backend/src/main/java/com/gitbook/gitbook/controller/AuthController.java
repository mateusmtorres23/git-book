package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.auth.LoginRequestDTO;
import com.gitbook.gitbook.dtos.auth.TokenAcessoResponseDTO;
import com.gitbook.gitbook.security.LimitadorLogin;
import com.gitbook.gitbook.service.AuthService;
import com.gitbook.gitbook.service.AuthService.SessaoAutenticada;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String COOKIE_RENOVACAO = "__Secure-gitbook-refresh";
    private final AuthService authService;
    private final LimitadorLogin limitadorLogin;

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenAcessoResponseDTO> login(@Valid @RequestBody LoginRequestDTO dados,
            HttpServletRequest request, HttpServletResponse response) {
        limitadorLogin.verificar(request.getRemoteAddr());
        return responderComTokens(authService.login(dados.email(), dados.senha()), response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenAcessoResponseDTO> renovar(
            @CookieValue(name = COOKIE_RENOVACAO, required = false) String token, HttpServletResponse response) {
        var sessao = authService.renovar(token);
        if (sessao.isEmpty()) {
            gravarCookie(response, "", Duration.ZERO);
            throw new BadCredentialsException("A sessão de renovação é inválida ou expirou.");
        }
        return responderComTokens(sessao.get(), response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = COOKIE_RENOVACAO, required = false) String token, HttpServletResponse response) {
        authService.logout(token);
        gravarCookie(response, "", Duration.ZERO);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<TokenAcessoResponseDTO> responderComTokens(SessaoAutenticada sessao,
            HttpServletResponse response) {
        var acesso = sessao.acesso();
        var renovacao = sessao.renovacao();
        Duration validade = Duration.between(Instant.now(), renovacao.expiraEm());
        gravarCookie(response, renovacao.token(), validade.isNegative() ? Duration.ZERO : validade);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new TokenAcessoResponseDTO(acesso.token(), acesso.expiraEm()));
    }

    private void gravarCookie(HttpServletResponse response, String token, Duration validade) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_RENOVACAO, token)
                .path("/api/auth")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(validade)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public record CsrfResponse(String headerName, String token) {
    }
}
