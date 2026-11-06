package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineStatusRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;
import com.gitbook.gitbook.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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
    public ResponseEntity<DeadlineResponseDTO> cadastrar(@Valid @RequestBody DeadlineRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/api/deadlines/" + resposta.idDeadline()))
                .body(resposta);
    }

    @GetMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> buscarPorId(@PathVariable("idDeadline") UUID idDeadline) {
        return ResponseEntity.ok(deadlineService.buscarPorId(idDeadline));
    }

    @GetMapping
    public ResponseEntity<List<DeadlineResponseDTO>> listar(
            @RequestParam(name = "idLivro", required = false) UUID idLivro,
            @RequestParam(name = "idUsuario", required = false) UUID idUsuario) {
        if ((idLivro == null) == (idUsuario == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idLivro ou idUsuario.");
        }
        return ResponseEntity.ok(idLivro != null
                ? deadlineService.listarPorLivro(idLivro)
                : deadlineService.listarPorUsuario(idUsuario));
    }

    @PutMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> atualizar(
            @PathVariable("idDeadline") UUID idDeadline, @Valid @RequestBody DeadlineUpdateRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.atualizar(idDeadline, dto);
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/{idDeadline}/status")
    public ResponseEntity<DeadlineResponseDTO> atualizarStatus(
            @PathVariable("idDeadline") UUID idDeadline, @Valid @RequestBody DeadlineStatusRequestDTO dto) {
        DeadlineResponseDTO resposta = deadlineService.atualizarStatus(idDeadline, dto);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idDeadline}")
    public ResponseEntity<Void> remover(@PathVariable("idDeadline") UUID idDeadline) {
        deadlineService.remover(idDeadline);
        return ResponseEntity.noContent().build();
    }
}
