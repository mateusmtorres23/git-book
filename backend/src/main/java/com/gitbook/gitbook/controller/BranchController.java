package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.branch.BranchRequestDTO;
import com.gitbook.gitbook.dtos.branch.BranchResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.service.BranchService;
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
public class BranchController {
    private final BranchService branchService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping("/books/{idLivro}/branches")
    public ResponseEntity<BranchResponseDTO> criar(@PathVariable UUID idLivro,
            @Valid @RequestBody BranchRequestDTO dados, Authentication autenticacao) {
        var branch = branchService.criar(idLivro, dados, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.created(URI.create("/api/branches/" + branch.idBranch())).body(branch);
    }

    @GetMapping("/books/{idLivro}/branches")
    public List<BranchResponseDTO> listarPorLivro(@PathVariable UUID idLivro, Authentication autenticacao) {
        return branchService.listarPorLivro(idLivro, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/branches/{idBranch}")
    public BranchResponseDTO buscarPorId(@PathVariable UUID idBranch, Authentication autenticacao) {
        return branchService.buscarPorId(idBranch, usuarioAutenticado.resolver(autenticacao));
    }

    @GetMapping("/branches/{idBranch}/content")
    public VersaoLivroResponseDTO consultarConteudo(@PathVariable UUID idBranch, Authentication autenticacao) {
        return branchService.consultarConteudo(idBranch, usuarioAutenticado.resolver(autenticacao));
    }
}
