package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.livro.LivroRequestDTO;
import com.gitbook.gitbook.dtos.livro.LivroResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LivroServiceImpl implements LivroService {
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final BranchRepository branchRepository;
    private final ColaboraEmRepository colaboraEmRepository;

    @Override
    @Transactional
    public LivroResponseDTO criarLivro(UUID idUsuario, LivroRequestDTO dados) {
        if (idUsuario == null || !dadosValidos(dados)) {
            return null;
        }

        Usuario dono = usuarioRepository.findById(idUsuario).orElse(null);
        if (dono == null) {
            return null;
        }

        Livro livro = new Livro();
        livro.setDono(dono);
        livro.setNomeLivro(dados.nomeLivro().strip());
        livro.setDescricao(dados.descricao());
        livro.setVersao("1.0");
        Livro livroSalvo = livroRepository.saveAndFlush(livro);

        // Livro e main são gravados na mesma transação.
        Branch main = new Branch();
        main.setLivro(livroSalvo);
        main.setAutor(dono);
        main.setNome("main");
        main.setMain(true);
        branchRepository.saveAndFlush(main);

        return montarResposta(livroSalvo);
    }

    @Override
    public LivroResponseDTO buscarLivro(UUID idLivro, UUID idUsuario) {
        if (idLivro == null || idUsuario == null) {
            return null;
        }

        Livro livro = livroRepository.findById(idLivro).orElse(null);
        if (livro == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, idLivro)) {
            return null;
        }

        return montarResposta(livro);
    }

    @Override
    public List<LivroResponseDTO> listarLivros(UUID idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            return null;
        }

        // O mapa evita repetir um livro caso o dono também tenha uma colaboração.
        Map<UUID, Livro> livros = new LinkedHashMap<>();
        for (Livro livro : livroRepository.findByDono(usuario)) {
            livros.put(livro.getIdLivro(), livro);
        }
        for (ColaboraEm colaboracao : colaboraEmRepository.findByUsuario_IdUsuario(idUsuario)) {
            Livro livro = colaboracao.getLivro();
            livros.put(livro.getIdLivro(), livro);
        }

        List<Livro> ordenados = new ArrayList<>(livros.values());
        ordenados.sort(Comparator.comparing(Livro::getNomeLivro).thenComparing(Livro::getIdLivro));
        List<LivroResponseDTO> respostas = new ArrayList<>();
        for (Livro livro : ordenados) {
            respostas.add(montarResposta(livro));
        }
        return respostas;
    }

    @Override
    @Transactional
    public LivroResponseDTO atualizarLivro(UUID idLivro, LivroRequestDTO dados, UUID idUsuario) {
        if (idLivro == null || idUsuario == null || !dadosValidos(dados)) {
            return null;
        }

        Livro livro = livroRepository.findByIdForUpdate(idLivro).orElse(null);
        if (livro == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        if (!livro.getDono().getIdUsuario().equals(idUsuario)) {
            return null;
        }

        livro.setNomeLivro(dados.nomeLivro().strip());
        livro.setDescricao(dados.descricao());
        return montarResposta(livroRepository.saveAndFlush(livro));
    }

    private boolean dadosValidos(LivroRequestDTO dados) {
        return dados != null && dados.nomeLivro() != null
                && !dados.nomeLivro().isBlank() && dados.nomeLivro().length() <= 150;
    }

    private LivroResponseDTO montarResposta(Livro livro) {
        return new LivroResponseDTO(livro.getIdLivro(), livro.getNomeLivro(), livro.getDescricao(),
                livro.getVersao(), livro.getDono().getIdUsuario(), livro.getDataCriacao());
    }
}
