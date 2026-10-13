package com.sorveteria.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(
    Long id,
    String nome,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal subtotal
    ) {}
