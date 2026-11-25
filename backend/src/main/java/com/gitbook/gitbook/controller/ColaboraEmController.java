package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.controller.support.UsuarioAutenticadoResolver;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.service.ColaboraEmService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/colaboracoes")
@RequiredArgsConstructor
public class ColaboraEmController {

    private final ColaboraEmService colaboraEmService;
    private final UsuarioAutenticadoResolver usuarioAutenticado;

    @PostMapping
    public ResponseEntity<ColaboraEmResponseDTO> cadastrar(@Valid @RequestBody ColaboraEmRequestDTO dto,
            Authentication autenticacao) {
        ColaboraEmResponseDTO resposta = colaboraEmService.cadastrar(dto, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.created(URI.create("/api/colaboracoes/" + resposta.idColaboracao()))
                .body(resposta);
    }

    @GetMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> buscarPorId(@PathVariable("idColaboracao") UUID idColaboracao,
            Authentication autenticacao) {
        return ResponseEntity.ok(colaboraEmService.buscarPorId(idColaboracao, usuarioAutenticado.resolver(autenticacao)));
    }

    @GetMapping
    public ResponseEntity<List<ColaboraEmResponseDTO>> listar(
            @RequestParam(name = "idLivro", required = false) UUID idLivro,
            @RequestParam(name = "idUsuario", required = false) UUID idUsuario, Authentication autenticacao) {
        if ((idLivro == null) == (idUsuario == null)) {
            throw new IllegalArgumentException("Informe exatamente um dos filtros: idLivro ou idUsuario.");
        }
        UUID idSolicitante = usuarioAutenticado.resolver(autenticacao);
        return ResponseEntity.ok(idLivro != null
                ? colaboraEmService.listarPorLivro(idLivro, idSolicitante)
                : colaboraEmService.listarPorUsuario(idUsuario, idSolicitante));
    }

    @PatchMapping("/{idColaboracao}")
    public ResponseEntity<ColaboraEmResponseDTO> atualizarCargo(
            @PathVariable("idColaboracao") UUID idColaboracao, @Valid @RequestBody ColaboraEmUpdateRequestDTO dto,
            Authentication autenticacao) {
        ColaboraEmResponseDTO resposta = colaboraEmService.atualizarCargo(
                idColaboracao, dto, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idColaboracao}")
    public ResponseEntity<Void> remover(@PathVariable("idColaboracao") UUID idColaboracao,
            Authentication autenticacao) {
        colaboraEmService.remover(idColaboracao, usuarioAutenticado.resolver(autenticacao));
        return ResponseEntity.noContent().build();
    }
}
