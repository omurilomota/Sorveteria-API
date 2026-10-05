package com.sorveteria.dto;

import com.sorveteria.model.Sabor;

public record SaborResponse (

    Long Id,
    String nome,
    Boolean ativo

) {

    public static SaborResponse from (Sabor sabor) {
        return new SaborResponse(
        sabor.getId(),
        sabor.getNome(),
        sabor.isAtivo()
        );
        }
}
