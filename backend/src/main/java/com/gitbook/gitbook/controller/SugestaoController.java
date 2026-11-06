package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.gitbook.gitbook.dtos.sugestao.SugestaoRequestDTO;
import com.gitbook.gitbook.dtos.sugestao.SugestaoResponseDTO;
import com.gitbook.gitbook.service.SugestaoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SugestaoController {
    private final SugestaoService sugestaoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/pull-requests/{idPullRequest}/suggestions")
    public ResponseEntity<SugestaoResponseDTO> criar(@PathVariable UUID idPullRequest,
            @Valid @RequestBody SugestaoRequestDTO dados, Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SugestaoResponseDTO sugestao = sugestaoService.adicionarSugestao(idPullRequest, dados, idUsuario);
        if (sugestao == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/suggestions/" + sugestao.idSugestao())).body(sugestao);
    }

    @GetMapping("/pull-requests/{idPullRequest}/suggestions")
    public ResponseEntity<List<SugestaoResponseDTO>> listarPorPullRequest(@PathVariable UUID idPullRequest,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<SugestaoResponseDTO> sugestoes = sugestaoService.listarSugestoes(idPullRequest, idUsuario);
        if (sugestoes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sugestoes);
    }

    @GetMapping("/suggestions/{idSugestao}")
    public ResponseEntity<SugestaoResponseDTO> buscarPorId(@PathVariable UUID idSugestao,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SugestaoResponseDTO sugestao = sugestaoService.buscarPorId(idSugestao, idUsuario);
        if (sugestao == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sugestao);
    }

    @PatchMapping("/suggestions/{idSugestao}/withdraw")
    public ResponseEntity<SugestaoResponseDTO> retirar(@PathVariable UUID idSugestao,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SugestaoResponseDTO sugestao = sugestaoService.retirarSugestao(idSugestao, idUsuario);
        if (sugestao == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sugestao);
    }

    private UUID obterIdUsuario(Authentication autenticacao) {
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByEmail(autenticacao.getName()).orElse(null);
        if (usuario == null) {
            return null;
        }
        return usuario.getIdUsuario();
    }
}

