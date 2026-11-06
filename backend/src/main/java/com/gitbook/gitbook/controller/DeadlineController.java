package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineStatusRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;
import com.gitbook.gitbook.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @PostMapping
    public ResponseEntity<DeadlineResponseDTO> cadastrar(@RequestBody DeadlineRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.cadastrar(dto);
        if (resposta == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/deadlines/" + resposta.idDeadline()))
                .body(resposta);
    }

    @GetMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> buscarPorId(@PathVariable UUID idDeadline) {
        return ResponseEntity.ok(deadlineService.buscarPorId(idDeadline));
    }

    @GetMapping(params = "idLivro")
    public ResponseEntity<List<DeadlineResponseDTO>> listarPorLivro(@RequestParam UUID idLivro) {
        return ResponseEntity.ok(deadlineService.listarPorLivro(idLivro));
    }

    @GetMapping(params = {"idUsuario", "!idLivro"})
    public ResponseEntity<List<DeadlineResponseDTO>> listarPorUsuario(@RequestParam UUID idUsuario) {
        return ResponseEntity.ok(deadlineService.listarPorUsuario(idUsuario));
    }

    @PatchMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> atualizar(
            @PathVariable UUID idDeadline, @RequestBody DeadlineUpdateRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.atualizar(idDeadline, dto);
        if (resposta == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/{idDeadline}/status")
    public ResponseEntity<DeadlineResponseDTO> atualizarStatus(
            @PathVariable UUID idDeadline, @RequestBody DeadlineStatusRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.atualizarStatus(idDeadline, dto);
        if (resposta == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idDeadline}")
    public ResponseEntity<Void> remover(@PathVariable UUID idDeadline) {
        if (!deadlineService.remover(idDeadline)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}
