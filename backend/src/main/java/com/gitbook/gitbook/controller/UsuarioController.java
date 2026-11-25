package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.usuario.UsuarioRequestDTO;
import com.gitbook.gitbook.dtos.usuario.UsuarioResponseDTO;
import com.gitbook.gitbook.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO dados) {
        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(dados);
        return ResponseEntity.created(URI.create("/api/users/" + usuario.idUsuario())).body(usuario);
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID idUsuario,
            Authentication autenticacao) {
        exigirPropriaConta(idUsuario, autenticacao);
        return ResponseEntity.ok(usuarioService.buscarPorId(idUsuario));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable UUID idUsuario,
            @Valid @RequestBody UsuarioRequestDTO dados, Authentication autenticacao) {
        exigirPropriaConta(idUsuario, autenticacao);
        return ResponseEntity.ok(usuarioService.atualizarUsuario(idUsuario, dados));
    }

    private void exigirPropriaConta(UUID idUsuario, Authentication autenticacao) {
        if (!usuarioAutenticado.resolver(autenticacao).equals(idUsuario)) {
            throw new AccessDeniedException("Você só pode acessar a própria conta.");
        }
    }
}

