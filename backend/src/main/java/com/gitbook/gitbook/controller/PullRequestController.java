package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRejeicaoRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestResponseDTO;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import com.gitbook.gitbook.service.PullRequestService;
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
public class PullRequestController {
    private final PullRequestService pullRequestService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping("/pull-requests")
    public ResponseEntity<PullRequestResponseDTO> criar(@Valid @RequestBody PullRequestRequestDTO dados,
            Authentication autenticacao) {
        var pr = pullRequestService.criar(dados, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.created(URI.create("/api/pull-requests/" + pr.idPullRequest())).body(pr);
    }

    @GetMapping("/pull-requests/{idPullRequest}")
    public PullRequestResponseDTO buscarPorId(@PathVariable UUID idPullRequest, Authentication autenticacao) {
        return pullRequestService.buscarPorId(idPullRequest, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/books/{idLivro}/pull-requests")
    public List<PullRequestResponseDTO> listarPorLivro(@PathVariable UUID idLivro,
            @RequestParam(required = false) StatusPullRequest status, Authentication autenticacao) {
        return pullRequestService.listarPorLivro(idLivro, status, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/pull-requests/{idPullRequest}/content")
    public VersaoLivroResponseDTO consultarConteudoProposto(@PathVariable UUID idPullRequest,
            Authentication autenticacao) {
        return pullRequestService.consultarConteudoProposto(idPullRequest, usuarioAutenticado.resolver(autenticacao));
    }

    @PatchMapping("/pull-requests/{idPullRequest}/approve")
    public PullRequestResponseDTO aprovar(@PathVariable UUID idPullRequest, Authentication autenticacao) {
        return pullRequestService.aprovar(idPullRequest, usuarioAutenticado.resolver(autenticacao));
    }

    @PatchMapping("/pull-requests/{idPullRequest}/reject")
    public PullRequestResponseDTO rejeitar(@PathVariable UUID idPullRequest,
            @Valid @RequestBody PullRequestRejeicaoRequestDTO dados, Authentication autenticacao) {
        return pullRequestService.rejeitar(idPullRequest, dados, usuarioAutenticado.resolver(autenticacao));
    }
}
