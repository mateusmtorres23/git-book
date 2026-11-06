package com.gitbook.gitbook.service;

import com.gitbook.gitbook.service.implementations.*;
import com.gitbook.gitbook.service.exceptions.*;
import com.gitbook.gitbook.repository.*;
import com.gitbook.gitbook.models.*;
import com.gitbook.gitbook.models.enums.*;
import com.gitbook.gitbook.dtos.colaboraEm.*;
import com.gitbook.gitbook.dtos.deadline.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerServicesTest {
    private final UUID id = UUID.randomUUID();
    private final UUID outro = UUID.randomUUID();
    private final ColaboraEmRepository colaboracoes = mock(ColaboraEmRepository.class);
    private final DeadlineRepository deadlines = mock(DeadlineRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final LivroRepository livros = mock(LivroRepository.class);
    private final ConteudoCapituloRepository conteudos = mock(ConteudoCapituloRepository.class);
    private final ColaboraEmServiceImpl colab = new ColaboraEmServiceImpl(colaboracoes, usuarios, livros);
    private final DeadlineServiceImpl deadline = new DeadlineServiceImpl(deadlines, usuarios, livros, colaboracoes);

    @Test void recursosAusentesLançamExcecaoEspecifica() {
        assertThrows(RecursoNaoEncontradoException.class, () -> colab.buscarPorId(id));
        assertThrows(RecursoNaoEncontradoException.class, () -> colab.remover(id));
        assertThrows(RecursoNaoEncontradoException.class, () -> colab.atualizarCargo(id, new ColaboraEmUpdateRequestDTO(CargoColab.REVISOR)));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.buscarPorId(id));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.remover(id));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.atualizar(id, new DeadlineUpdateRequestDTO(id, "Título", null, LocalDateTime.now())));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.atualizarStatus(id, new DeadlineStatusRequestDTO(StatusTask.CONCLUIDA)));
        assertThrows(RecursoNaoEncontradoException.class, () -> new ConteudoCapituloServiceImpl(conteudos).buscarPorId(id));
    }

    @Test void referenciasAusentesNaoSaoAceitas() {
        var pedido = new ColaboraEmRequestDTO(id, id, CargoColab.ESCRITOR);
        var tarefa = new DeadlineRequestDTO(id, id, "Título", null, LocalDateTime.now());
        assertThrows(RecursoNaoEncontradoException.class, () -> colab.cadastrar(pedido));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.cadastrar(tarefa));
        when(usuarios.findById(id)).thenReturn(Optional.of(mock(Usuario.class)));
        assertThrows(RecursoNaoEncontradoException.class, () -> colab.cadastrar(pedido));
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.cadastrar(tarefa));
        when(deadlines.findById(id)).thenReturn(Optional.of(mock(Deadline.class)));
        when(usuarios.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> deadline.atualizar(id, new DeadlineUpdateRequestDTO(id, "Título", null, LocalDateTime.now())));
    }

    @Test void duplicidadeEDonoGeramConflito() {
        when(colaboracoes.existsByUsuario_IdUsuarioAndLivro_IdLivro(id, id)).thenReturn(true);
        assertThrows(ConflitoNegocioException.class, () -> colab.cadastrar(new ColaboraEmRequestDTO(id, id, CargoColab.ESCRITOR)));
        Usuario dono = mock(Usuario.class);
        Livro livro = mock(Livro.class);
        ColaboraEm registro = mock(ColaboraEm.class);
        when(dono.getIdUsuario()).thenReturn(id);
        when(livro.getDono()).thenReturn(dono);
        when(registro.getUsuario()).thenReturn(dono);
        when(registro.getLivro()).thenReturn(livro);
        when(colaboracoes.findById(id)).thenReturn(Optional.of(registro));
        assertThrows(ConflitoNegocioException.class, () -> colab.atualizarCargo(id, new ColaboraEmUpdateRequestDTO(CargoColab.GESTOR)));
        assertThrows(ConflitoNegocioException.class, () -> colab.remover(id));
        verify(colaboracoes, never()).delete(any());
    }

    @Test void usuarioSemVinculoECargoIncompativelSaoInvalidos() {
        Usuario usuario = mock(Usuario.class);
        Usuario dono = mock(Usuario.class);
        Livro livro = mock(Livro.class);
        when(usuario.getIdUsuario()).thenReturn(id);
        when(dono.getIdUsuario()).thenReturn(outro);
        when(livro.getDono()).thenReturn(dono);
        when(usuarios.findById(id)).thenReturn(Optional.of(usuario));
        when(livros.findById(id)).thenReturn(Optional.of(livro));
        assertThrows(IllegalArgumentException.class, () -> colab.cadastrar(new ColaboraEmRequestDTO(id, id, CargoColab.GESTOR)));
        assertThrows(IllegalArgumentException.class, () -> deadline.cadastrar(new DeadlineRequestDTO(id, id, "Título", null, LocalDateTime.now())));
        Deadline registro = mock(Deadline.class);
        when(registro.getLivro()).thenReturn(livro);
        when(deadlines.findById(id)).thenReturn(Optional.of(registro));
        assertThrows(IllegalArgumentException.class, () -> deadline.atualizar(id, new DeadlineUpdateRequestDTO(id, "Título", null, LocalDateTime.now())));
    }

    @Test void putSubstituiDescricaoEPreservaLivroEStatus() {
        Usuario usuario = mock(Usuario.class);
        Livro livro = mock(Livro.class);
        when(usuario.getIdUsuario()).thenReturn(id);
        when(livro.getDono()).thenReturn(usuario);
        Deadline registro = new Deadline(usuario, livro, "Antes", "Descrição", LocalDateTime.now());
        registro.atualizarStatus(StatusTask.CONCLUIDA);
        when(deadlines.findById(id)).thenReturn(Optional.of(registro));
        when(usuarios.findById(id)).thenReturn(Optional.of(usuario));
        when(deadlines.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var resposta = deadline.atualizar(id, new DeadlineUpdateRequestDTO(id, "Depois", null, LocalDateTime.now()));
        assertNull(resposta.descricao());
        assertEquals("Depois", resposta.titulo());
        assertEquals(StatusTask.CONCLUIDA, resposta.status());
        assertSame(livro, registro.getLivro());
    }
}
