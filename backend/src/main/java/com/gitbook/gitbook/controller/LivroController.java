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

import com.gitbook.gitbook.dtos.livro.LivroRequestDTO;
import com.gitbook.gitbook.dtos.livro.LivroResponseDTO;
import com.gitbook.gitbook.service.LivroService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class LivroController {
    private final LivroService livroService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<LivroResponseDTO> criar(@Valid @RequestBody LivroRequestDTO dados,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LivroResponseDTO livro = livroService.criarLivro(idUsuario, dados);
        if (livro == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/books/" + livro.idLivro())).body(livro);
    }

    @GetMapping
    public ResponseEntity<List<LivroResponseDTO>> listar(Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<LivroResponseDTO> livros = livroService.listarLivros(idUsuario);
        if (livros == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{idLivro}")
    public ResponseEntity<LivroResponseDTO> buscarPorId(@PathVariable UUID idLivro,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LivroResponseDTO livro = livroService.buscarLivro(idLivro, idUsuario);
        if (livro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(livro);
    }

    @PutMapping("/{idLivro}")
    public ResponseEntity<LivroResponseDTO> atualizar(@PathVariable UUID idLivro,
            @Valid @RequestBody LivroRequestDTO dados, Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LivroResponseDTO livro = livroService.atualizarLivro(idLivro, dados, idUsuario);
        if (livro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(livro);
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

