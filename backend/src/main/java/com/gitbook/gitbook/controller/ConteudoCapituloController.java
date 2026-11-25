package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;
import com.gitbook.gitbook.service.ConteudoCapituloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conteudos-capitulo")
@RequiredArgsConstructor
public class ConteudoCapituloController {

    private final ConteudoCapituloService conteudoCapituloService;

    @GetMapping("/{idVersao}")
    public ResponseEntity<ConteudoCapituloResponseDTO> buscarPorId(@PathVariable("idVersao") UUID idVersao) {
        return ResponseEntity.ok(conteudoCapituloService.buscarPorId(idVersao));
    }

    @GetMapping
    public ResponseEntity<List<ConteudoCapituloResponseDTO>> listar(
            @RequestParam(name = "idCapitulo", required = false) UUID idCapitulo,
            @RequestParam(name = "idCommit", required = false) UUID idCommit) {
        if ((idCapitulo == null) == (idCommit == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idCapitulo ou idCommit.");
        }
        return ResponseEntity.ok(idCapitulo != null
                ? conteudoCapituloService.listarPorCapitulo(idCapitulo)
                : conteudoCapituloService.listarPorCommit(idCommit));
    }

}
