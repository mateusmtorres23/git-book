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

import com.gitbook.gitbook.dtos.usuario.UsuarioRequestDTO;
import com.gitbook.gitbook.dtos.usuario.UsuarioResponseDTO;
import com.gitbook.gitbook.service.UsuarioService;
import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO dados) {
        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(dados);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.created(URI.create("/api/users/" + usuario.idUsuario())).body(usuario);
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID idUsuario,
            Authentication autenticacao) {
        UUID idAutenticado = obterIdUsuario(autenticacao);
        if (idAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!idAutenticado.equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(idUsuario);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable UUID idUsuario,
            @Valid @RequestBody UsuarioRequestDTO dados, Authentication autenticacao) {
        UUID idAutenticado = obterIdUsuario(autenticacao);
        if (idAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!idAutenticado.equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UsuarioResponseDTO usuario = usuarioService.atualizarUsuario(idUsuario, dados);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(usuario);
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

