package com.sorveteria.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record PedidoRequest(
        @NotEmpty List<@Valid ItemPedidoRequest> itens
) {}