package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.capitulo.CapituloResponseDTO;
import com.gitbook.gitbook.service.CapituloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches/{idBranch}/chapters")
@RequiredArgsConstructor
public class CapituloController {
    private final CapituloService capituloService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @GetMapping
    public ResponseEntity<List<CapituloResponseDTO>> listarPorBranch(@PathVariable UUID idBranch,
            Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        List<CapituloResponseDTO> capitulos = capituloService.buscarPorBranch(idBranch, idUsuario);
        return ResponseEntity.ok(capitulos);
    }

    @GetMapping("/{idCapitulo}")
    public ResponseEntity<CapituloResponseDTO> buscarCapitulo(@PathVariable UUID idBranch,
            @PathVariable UUID idCapitulo, Authentication autenticacao) {
        UUID idUsuario = usuarioAutenticado.resolver(autenticacao);
        CapituloResponseDTO capitulo = capituloService.buscarCapitulo(idCapitulo, idUsuario, idBranch);
        return ResponseEntity.ok(capitulo);
    }
}

