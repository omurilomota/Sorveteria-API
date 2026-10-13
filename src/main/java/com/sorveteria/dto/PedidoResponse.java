package com.sorveteria.dto;

import com.sorveteria.model.StatusPedido;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
    Long id,
    StatusPedido status,
    int itensTotal,
    BigDecimal custoTotal,
    List<ItemPedidoResponse> itens

)
{}
