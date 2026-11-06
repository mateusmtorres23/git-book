package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.commit.CommitDetalheResponseDTO;
import com.gitbook.gitbook.dtos.commit.CommitRequestDTO;
import com.gitbook.gitbook.dtos.commit.CommitResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.service.CommitService;
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
public class CommitController {
    private final CommitService commitService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping("/branches/{idBranch}/commits")
    public ResponseEntity<CommitDetalheResponseDTO> criar(@PathVariable UUID idBranch,
            @Valid @RequestBody CommitRequestDTO dados, Authentication autenticacao) {
        var commit = commitService.criar(idBranch, dados, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.created(URI.create("/api/commits/" + commit.commit().idCommit())).body(commit);
    }

    @GetMapping("/commits/{idCommit}")
    public CommitDetalheResponseDTO buscarPorId(@PathVariable UUID idCommit, Authentication autenticacao) {
        return commitService.buscarPorId(idCommit, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/books/{idLivro}/commits")
    public List<CommitResponseDTO> listarPorLivro(@PathVariable UUID idLivro, Authentication autenticacao) {
        return commitService.listarPorLivro(idLivro, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/branches/{idBranch}/commits")
    public List<CommitResponseDTO> listarPorBranch(@PathVariable UUID idBranch, Authentication autenticacao) {
        return commitService.listarPorBranch(idBranch, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/commits/{idCommit}/content")
    public VersaoLivroResponseDTO consultarVersao(@PathVariable UUID idCommit, Authentication autenticacao) {
        return commitService.consultarVersao(idCommit, usuarioAutenticado.resolver(autenticacao));
    }
}
