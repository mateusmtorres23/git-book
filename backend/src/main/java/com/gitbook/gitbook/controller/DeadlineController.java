package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineStatusRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;
import com.gitbook.gitbook.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping
    public ResponseEntity<DeadlineResponseDTO> cadastrar(@Valid @RequestBody DeadlineRequestDTO dto,
            Authentication autenticacao) {
        DeadlineResponseDTO resposta = deadlineService.cadastrar(dto, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.created(URI.create("/api/deadlines/" + resposta.idDeadline()))
                .body(resposta);
    }

    @GetMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> buscarPorId(@PathVariable("idDeadline") UUID idDeadline,
            Authentication autenticacao) {
        return ResponseEntity.ok(deadlineService.buscarPorId(idDeadline, usuarioAutenticado.resolver(autenticacao)));
    }

    @GetMapping
    public ResponseEntity<List<DeadlineResponseDTO>> listar(
            @RequestParam(name = "idLivro", required = false) UUID idLivro,
            @RequestParam(name = "idUsuario", required = false) UUID idUsuario, Authentication autenticacao) {
        if ((idLivro == null) == (idUsuario == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idLivro ou idUsuario.");
        }
        UUID idSolicitante = usuarioAutenticado.resolver(autenticacao);
        return ResponseEntity.ok(idLivro != null
                ? deadlineService.listarPorLivro(idLivro, idSolicitante)
                : deadlineService.listarPorUsuario(idUsuario, idSolicitante));
    }

    @PutMapping("/{idDeadline}")
    public ResponseEntity<DeadlineResponseDTO> atualizar(
            @PathVariable("idDeadline") UUID idDeadline, @Valid @RequestBody DeadlineUpdateRequestDTO dto,
            Authentication autenticacao) {
        DeadlineResponseDTO resposta = deadlineService.atualizar(idDeadline, dto, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/{idDeadline}/status")
    public ResponseEntity<DeadlineResponseDTO> atualizarStatus(
            @PathVariable("idDeadline") UUID idDeadline, @Valid @RequestBody DeadlineStatusRequestDTO dto,
            Authentication autenticacao) {
        DeadlineResponseDTO resposta = deadlineService.atualizarStatus(
                idDeadline, dto, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idDeadline}")
    public ResponseEntity<Void> remover(@PathVariable("idDeadline") UUID idDeadline, Authentication autenticacao) {
        deadlineService.remover(idDeadline, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.noContent().build();
    }
}
