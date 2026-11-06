package com.gitbook.gitbook.service.support;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ValidacaoService {
    private final Validator validator;

    public <T> void validar(T dados) {
        Objects.requireNonNull(dados, "Os dados da operação são obrigatórios.");
        var violacoes = validator.validate(dados);
        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }
    }
}
