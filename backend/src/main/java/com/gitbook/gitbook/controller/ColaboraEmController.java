package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.service.ColaboraEmService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/colaboracoes")
@RequiredArgsConstructor
public class ColaboraEmController {

    private final ColaboraEmService colaboraEmService;

    @PostMapping
    public ResponseEntity<ColaboraEmResponseDTO> cadastrar(@Valid @RequestBody ColaboraEmRequestDTO dto) {
        ColaboraEmResponseDTO resposta = colaboraEmService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/api/colaboracoes/" + resposta.idColaboracao()))
                .body(resposta);
    }

    @GetMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> buscarPorId(@PathVariable("idColaboracao") UUID idColaboracao) {
        return ResponseEntity.ok(colaboraEmService.buscarPorId(idColaboracao));
    }

    @GetMapping
    public ResponseEntity<List<ColaboraEmResponseDTO>> listar(
            @RequestParam(name = "idLivro", required = false) UUID idLivro,
            @RequestParam(name = "idUsuario", required = false) UUID idUsuario) {
        if ((idLivro == null) == (idUsuario == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idLivro ou idUsuario.");
        }
        return ResponseEntity.ok(idLivro != null
                ? colaboraEmService.listarPorLivro(idLivro)
                : colaboraEmService.listarPorUsuario(idUsuario));
    }

    @PatchMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> atualizarCargo(
            @PathVariable("idColaboracao") UUID idColaboracao, @Valid @RequestBody ColaboraEmUpdateRequestDTO dto) {
        ColaboraEmResponseDTO resposta = colaboraEmService.atualizarCargo(idColaboracao, dto);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idColaboracao}")
    public ResponseEntity<Void> remover(@PathVariable("idColaboracao") UUID idColaboracao) {
        colaboraEmService.remover(idColaboracao);
        return ResponseEntity.noContent().build();
    }
}
