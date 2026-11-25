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

import com.gitbook.gitbook.dtos.capitulo.CapituloResponseDTO;
import com.gitbook.gitbook.service.CapituloService;

import java.util.List;

@RestController
@RequestMapping("/api/branches/{idBranch}/chapters")
@RequiredArgsConstructor
public class CapituloController {
    private final CapituloService capituloService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<CapituloResponseDTO>> listarPorBranch(@PathVariable UUID idBranch,
            Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<CapituloResponseDTO> capitulos = capituloService.buscarPorBranch(idBranch, idUsuario);
        if (capitulos == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(capitulos);
    }

    @GetMapping("/{idCapitulo}")
    public ResponseEntity<CapituloResponseDTO> buscarCapitulo(@PathVariable UUID idBranch,
            @PathVariable UUID idCapitulo, Authentication autenticacao) {
        UUID idUsuario = obterIdUsuario(autenticacao);
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CapituloResponseDTO capitulo = capituloService.buscarCapitulo(idCapitulo, idUsuario, idBranch);
        if (capitulo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(capitulo);
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

