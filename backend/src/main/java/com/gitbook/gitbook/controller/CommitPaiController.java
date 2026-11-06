package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.commit.CommitPaiResponseDTO;
import com.gitbook.gitbook.service.CommitPaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommitPaiController {
    private final CommitPaiService commitPaiService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @GetMapping("/commit-parents/{idPaternidade}")
    public CommitPaiResponseDTO buscarPorId(@PathVariable UUID idPaternidade, Authentication autenticacao) {
        return commitPaiService.buscarPorId(idPaternidade, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/commits/{idCommit}/parents")
    public List<CommitPaiResponseDTO> listarPais(@PathVariable UUID idCommit, Authentication autenticacao) {
        return commitPaiService.listarPais(idCommit, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/commits/{idCommit}/parents/first")
    public ResponseEntity<CommitPaiResponseDTO> buscarPaiPrincipal(@PathVariable UUID idCommit,
            Authentication autenticacao) {
        return commitPaiService.buscarPaiPrincipal(idCommit, usuarioAutenticado.resolver(autenticacao))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/commits/{idCommit}/children")
    public List<CommitPaiResponseDTO> listarFilhos(@PathVariable UUID idCommit, Authentication autenticacao) {
        return commitPaiService.listarFilhos(idCommit, usuarioAutenticado.resolver(autenticacao));
    }
}
