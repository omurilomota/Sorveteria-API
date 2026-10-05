package com.sorveteria.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProdutoRequest (
    @NotBlank String nome,
    @NotNull @Positive BigDecimal preco,
    @NotBlank String descricao,
    @NotNull @Positive int estoque,
    @NotBlank String categoria
) {

    public Object getQuantidade() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQuantidade'");
    }}
