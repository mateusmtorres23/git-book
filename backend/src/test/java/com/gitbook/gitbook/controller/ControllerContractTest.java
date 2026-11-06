package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.service.*;
import com.gitbook.gitbook.service.exceptions.*;
import com.gitbook.gitbook.dtos.colaboraEm.*;
import com.gitbook.gitbook.dtos.deadline.*;
import com.gitbook.gitbook.models.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllerContractTest {
    private final UUID id = UUID.randomUUID();
    private ColaboraEmService colaboracoes;
    private DeadlineService deadlines;
    private ConteudoCapituloService conteudos;
    private MockMvc mvc;

    @BeforeEach void setup() {
        colaboracoes = mock(ColaboraEmService.class);
        deadlines = mock(DeadlineService.class);
        conteudos = mock(ConteudoCapituloService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ColaboraEmController(colaboracoes),
                new DeadlineController(deadlines), new ConteudoCapituloController(conteudos))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test void filtrosDevemSerExclusivos() throws Exception {
        for (String path : List.of("colaboracoes", "deadlines", "conteudos-capitulo")) {
            String a = path.equals("conteudos-capitulo") ? "idCapitulo" : "idLivro";
            String b = path.equals("conteudos-capitulo") ? "idCommit" : "idUsuario";
            mvc.perform(get("/api/" + path)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").exists());
            mvc.perform(get("/api/" + path).param(a, id.toString()).param(b, id.toString()))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/api/" + path).param(a, id.toString())).andExpect(status().isOk());
            mvc.perform(get("/api/" + path).param(b, id.toString())).andExpect(status().isOk());
        }
    }

    @Test void corposInvalidosNaoChegamAoService() throws Exception {
        mvc.perform(post("/api/colaboracoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/colaboracoes/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/deadlines/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        for (String titulo : List.of("", "a".repeat(151))) {
            String body = "{\"idUsuario\":\"" + id + "\",\"idLivro\":\"" + id
                    + "\",\"titulo\":\"" + titulo + "\",\"dataLimite\":\"2026-01-01T12:00:00\"}";
            mvc.perform(post("/api/deadlines").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
            mvc.perform(put("/api/deadlines/" + id).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/deadlines").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/deadlines/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(colaboracoes, deadlines);
    }

    @Test void errosDoServicePossuemStatusEMensagem() throws Exception {
        when(colaboracoes.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Ausente"));
        when(deadlines.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Ausente"));
        when(conteudos.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Ausente"));
        for (String path : List.of("colaboracoes", "deadlines", "conteudos-capitulo"))
            mvc.perform(get("/api/" + path + "/" + id)).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Ausente"));
        doThrow(new ConflitoNegocioException("Conflito")).when(colaboracoes).remover(id);
        mvc.perform(delete("/api/colaboracoes/" + id)).andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Conflito"));
    }

    @Test void sucessoPreservaContrato() throws Exception {
        var colaboracao = new ColaboraEmResponseDTO(id, id, id, CargoColab.ESCRITOR);
        var deadline = new DeadlineResponseDTO(id, id, id, "Título", null, StatusTask.PENDENTE, null, null, false);
        when(colaboracoes.cadastrar(any())).thenReturn(colaboracao);
        when(colaboracoes.atualizarCargo(eq(id), any())).thenReturn(colaboracao);
        when(deadlines.cadastrar(any())).thenReturn(deadline);
        when(deadlines.atualizar(eq(id), any())).thenReturn(deadline);
        when(deadlines.atualizarStatus(eq(id), any())).thenReturn(deadline);
        String colab = "{\"idUsuario\":\"" + id + "\",\"idLivro\":\"" + id + "\",\"cargo\":\"ESCRITOR\"}";
        String body = "{\"idUsuario\":\"" + id + "\",\"idLivro\":\"" + id + "\",\"titulo\":\"Título\",\"dataLimite\":\"2026-01-01T12:00:00\"}";
        mvc.perform(post("/api/colaboracoes").contentType(MediaType.APPLICATION_JSON).content(colab))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/colaboracoes/" + id)).andExpect(jsonPath("$.idColaboracao").value(id.toString()));
        mvc.perform(post("/api/deadlines").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/deadlines/" + id));
        mvc.perform(put("/api/deadlines/" + id).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(patch("/api/deadlines/" + id).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isMethodNotAllowed());
        mvc.perform(patch("/api/colaboracoes/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"cargo\":\"REVISOR\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/deadlines/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONCLUIDA\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/deadlines/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
    }
}
