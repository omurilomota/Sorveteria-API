package com.sorveteria.dto;

import com.sorveteria.model.StatusPedido;

import jakarta.validation.constraints.NotNull;

public record PedidoStatusRequest(
	@NotNull(message = "O status é obrigatório")
	StatusPedido status
)
{}
