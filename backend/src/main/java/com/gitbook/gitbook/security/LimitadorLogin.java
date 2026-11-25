package com.gitbook.gitbook.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class LimitadorLogin {
    private static final int TENTATIVAS_POR_MINUTO = 20;
    private static final int MAXIMO_DE_ORIGENS = 4096;

    private final Map<String, Integer> tentativas = new HashMap<>();
    private Instant janela = Instant.EPOCH;

    public synchronized void verificar(String origem) {
        Instant minutoAtual = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        if (!minutoAtual.equals(janela)) {
            tentativas.clear();
            janela = minutoAtual;
        }
        int total = tentativas.getOrDefault(origem, 0);
        if (total >= TENTATIVAS_POR_MINUTO || (total == 0 && tentativas.size() >= MAXIMO_DE_ORIGENS)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em instantes.");
        }
        tentativas.put(origem, total + 1);
    }
}
