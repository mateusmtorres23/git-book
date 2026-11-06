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
    public ResponseEntity<ConteudoCapituloResponseDTO> buscarPorId(@PathVariable UUID idVersao) {
        return ResponseEntity.ok(conteudoCapituloService.buscarPorId(idVersao));
    }

    @GetMapping(params = "idCapitulo")
    public ResponseEntity<List<ConteudoCapituloResponseDTO>> listarPorCapitulo(@RequestParam UUID idCapitulo) {
        return ResponseEntity.ok(conteudoCapituloService.listarPorCapitulo(idCapitulo));
    }

    @GetMapping(params = {"idCommit", "!idCapitulo"})
    public ResponseEntity<List<ConteudoCapituloResponseDTO>> listarPorCommit(@RequestParam UUID idCommit) {
        return ResponseEntity.ok(conteudoCapituloService.listarPorCommit(idCommit));
    }
}
