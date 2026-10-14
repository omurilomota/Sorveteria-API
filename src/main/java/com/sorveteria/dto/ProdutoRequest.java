package com.sorveteria.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProdutoRequest(@NotBlank(message = "O nome é obrigatório") String nome,
		@NotNull(message = "O preço é obrigatório") @Positive(message = "O preço deve ser maior que zero") BigDecimal preco,
		@NotBlank(message = "A descrição é obrigatória") String descricao,
		@NotNull(message = "O estoque é obrigatório") @Positive(message = "O estoque deve ser maior que zero") int estoque,
		@NotBlank(message = "A categoria deve ser obrigatória") String categoria) {
}
