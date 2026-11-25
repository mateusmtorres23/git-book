package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;
import com.gitbook.gitbook.service.ConteudoCapituloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conteudos-capitulo")
@RequiredArgsConstructor
public class ConteudoCapituloController {

    private final ConteudoCapituloService conteudoCapituloService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @GetMapping("/{idVersao}")
    public ResponseEntity<ConteudoCapituloResponseDTO> buscarPorId(@PathVariable("idVersao") UUID idVersao,
            Authentication autenticacao) {
        return ResponseEntity.ok(conteudoCapituloService.buscarPorId(idVersao, usuarioAutenticado.resolver(autenticacao)));
    }

    @GetMapping
    public ResponseEntity<List<ConteudoCapituloResponseDTO>> listar(
            @RequestParam(name = "idCapitulo", required = false) UUID idCapitulo,
            @RequestParam(name = "idCommit", required = false) UUID idCommit, Authentication autenticacao) {
        if ((idCapitulo == null) == (idCommit == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idCapitulo ou idCommit.");
        }
        UUID idSolicitante = usuarioAutenticado.resolver(autenticacao);
        return ResponseEntity.ok(idCapitulo != null
                ? conteudoCapituloService.listarPorCapitulo(idCapitulo, idSolicitante)
                : conteudoCapituloService.listarPorCommit(idCommit, idSolicitante));
    }

}
