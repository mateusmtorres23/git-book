package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.livro.LivroRequestDTO;
import com.gitbook.gitbook.dtos.livro.LivroResponseDTO;
import com.gitbook.gitbook.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class LivroController {
    private final LivroService livroService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping
    public ResponseEntity<LivroResponseDTO> criar(@Valid @RequestBody LivroRequestDTO dados,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        LivroResponseDTO livro = livroService.criarLivro(idUsuario, dados);
        return ResponseEntity.created(URI.create("/api/books/" + livro.idLivro())).body(livro);
    }

    @GetMapping
    public ResponseEntity<List<LivroResponseDTO>> listar(Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        List<LivroResponseDTO> livros = livroService.listarLivros(idUsuario);
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{idLivro}")
    public ResponseEntity<LivroResponseDTO> buscarPorId(@PathVariable UUID idLivro,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        LivroResponseDTO livro = livroService.buscarLivro(idLivro, idUsuario);
        return ResponseEntity.ok(livro);
    }

    @PutMapping("/{idLivro}")
    public ResponseEntity<LivroResponseDTO> atualizar(@PathVariable UUID idLivro,
            @Valid @RequestBody LivroRequestDTO dados, Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        LivroResponseDTO livro = livroService.atualizarLivro(idLivro, dados, idUsuario);
        return ResponseEntity.ok(livro);
    }
}

