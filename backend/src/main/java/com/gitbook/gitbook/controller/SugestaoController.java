package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.sugestao.SugestaoRequestDTO;
import com.gitbook.gitbook.dtos.sugestao.SugestaoResponseDTO;
import com.gitbook.gitbook.service.SugestaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SugestaoController {
    private final SugestaoService sugestaoService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping("/pull-requests/{idPullRequest}/suggestions")
    public ResponseEntity<SugestaoResponseDTO> criar(@PathVariable UUID idPullRequest,
            @Valid @RequestBody SugestaoRequestDTO dados, Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        SugestaoResponseDTO sugestao = sugestaoService.adicionarSugestao(idPullRequest, dados, idUsuario);
        return ResponseEntity.created(URI.create("/api/suggestions/" + sugestao.idSugestao())).body(sugestao);
    }

    @GetMapping("/pull-requests/{idPullRequest}/suggestions")
    public ResponseEntity<List<SugestaoResponseDTO>> listarPorPullRequest(@PathVariable UUID idPullRequest,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        List<SugestaoResponseDTO> sugestoes = sugestaoService.listarSugestoes(idPullRequest, idUsuario);
        return ResponseEntity.ok(sugestoes);
    }

    @GetMapping("/suggestions/{idSugestao}")
    public ResponseEntity<SugestaoResponseDTO> buscarPorId(@PathVariable UUID idSugestao,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        SugestaoResponseDTO sugestao = sugestaoService.buscarPorId(idSugestao, idUsuario);
        return ResponseEntity.ok(sugestao);
    }

    @PatchMapping("/suggestions/{idSugestao}/withdraw")
    public ResponseEntity<SugestaoResponseDTO> retirar(@PathVariable UUID idSugestao,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        SugestaoResponseDTO sugestao = sugestaoService.retirarSugestao(idSugestao, idUsuario);
        return ResponseEntity.ok(sugestao);
    }
}
