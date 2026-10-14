package com.sorveteria.dto;

import jakarta.validation.constraints.NotBlank;

public record PedidoRequest(
		@NotBlank(message = "O nome do pedido é obrigatório")
		String nome
) {}