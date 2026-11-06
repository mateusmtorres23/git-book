package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.service.ColaboraEmService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ColaboraEmResponseDTO> cadastrar(@RequestBody ColaboraEmRequestDTO dto) {
        ColaboraEmResponseDTO resposta = colaboraEmService.cadastrar(dto);
        if (resposta == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/colaboracoes/" + resposta.idColaboracao()))
                .body(resposta);
    }

    @GetMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> buscarPorId(@PathVariable UUID idColaboracao) {
        return ResponseEntity.ok(colaboraEmService.buscarPorId(idColaboracao));
    }

    @GetMapping(params = "idLivro")
    public ResponseEntity<List<ColaboraEmResponseDTO>> listarPorLivro(@RequestParam UUID idLivro) {
        return ResponseEntity.ok(colaboraEmService.listarPorLivro(idLivro));
    }

    @GetMapping(params = {"idUsuario", "!idLivro"})
    public ResponseEntity<List<ColaboraEmResponseDTO>> listarPorUsuario(@RequestParam UUID idUsuario) {
        return ResponseEntity.ok(colaboraEmService.listarPorUsuario(idUsuario));
    }

    @PatchMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> atualizarCargo(
            @PathVariable UUID idColaboracao, @RequestBody ColaboraEmUpdateRequestDTO dto) {
        ColaboraEmResponseDTO resposta = colaboraEmService.atualizarCargo(idColaboracao, dto);
        if (resposta == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idColaboracao}")
    public ResponseEntity<Void> remover(@PathVariable UUID idColaboracao) {
        if (!colaboraEmService.remover(idColaboracao)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}
